(ns lymchat.storage
  (:refer-clojure :exclude [update])
  (:require [lymchat.core-async-storage :as s]
            [re-frame.core :refer [dispatch]]
            [cljs.core.async :refer [<!]]
            [re-frame.db :refer [app-db]]
            [lymchat.util :as util]
            [cljs-time.core :as t]
            [cljs-time.coerce :as tc])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; all reads from memory, synchronous
;; all writes to both memory (synchronous) and storage (asynchronous)

(defn development?
  []
  (true? js/__DEV__))

(def get-item s/get-item)
(def set-item s/set-item)
(def remove-item s/remove-item)

(defn kv-set
  [k v]
  (set-item k v)
  (swap! app-db (fn [db]
                  (assoc db k v))))

(defn kv-get
  [k]
  (get @app-db k))

(defn transform
  [table handler]
  (go
    (let [[err v] (<! (get-item table))]
      (if (nil? err)
        (set-item table (handler v))
        (throw v)))))

(defn create
  [table record]
  (when (and table record)
    (transform table
               (fn [col]
                 (-> (vec col)
                     (conj record)
                     (distinct)
                     (vec))))))

(defn update
  [table record]
  (when (and table record)
    (transform table
               (fn [col]
                 (vec
                  (doall
                   (map
                     (fn [x]
                       (if (= (:id x) (:id record))
                         (merge x record)
                         x))
                     col)))))))

(defn batch-create
  [table records]
  (when (and table records)
    (transform table
               (fn [col]
                 (->
                  (concat col records)
                  (distinct)
                  (vec))))))

(defn delete
  [table id]
  (transform table
             (fn [col]
               (-> (remove #(= (:id %) id) col)
                   (vec)))))

(defn set-latest-message-id
  [id]
  (set-item :latest-message-id id))

(defn delete-records-by-fk-id
  [records-table fk fk-value]
  (transform records-table
             (fn [col]
               (-> (remove #(= (get % fk) fk-value) col)
                   (vec)))))

(defn delete-conversation
  [conversation-id]
  ;; delete messages
  (delete-records-by-fk-id :messages :conversation_id conversation-id)
  ;; delete conversation
  (delete :conversations conversation-id))

(defn get-col
  [k]
  (get @app-db k))

(defn get-by-fk
  [table fk fk-value]
  (->> (get-col table)
       (filter #(= fk-value (get % fk)))
       (first)))

(defn get-by-id
  [table id]
  (get-by-fk table :id id))

(defn get-channel-by-id
  [id]
  (->> (get-in @app-db [:current-user :channels])
       (filter #(= id (get % :id)))
       (first)))

(defn get-channel-members-count
  [channel-id]
  (-> (get-by-id :channels channel-id)
      (:members_count)))

(defn get-largest-conversation-id
  []
  (let [id (->> (get-col :conversations)
                (sort :id)
                (last)
                (:id))]
    (if (nil? id)
      0
      id)))

(defn me
  []
  (:current-user @app-db))

(defn get-count
  [table]
  (->> (get-col table)
       (count)))

(defn get-latest-message-id
  []
  (:latest-message-id @app-db))

(defn get-conversations
  []
  (some->>
   (get-col :conversations)
   (distinct)
   (sort :last_message_at)
   (reverse)))

(defn get-invites
  []
  (get-col :invites))

(defn invite-exists?
  [user]
  (some? (get-by-id :invites (:id user))))

(defn get-contacts
  []
  (get-col :contacts))

(defn get-contacts-count
  []
  (get-count :contacts))

(defn get-contacts-ids
  []
  (map :id (get-contacts)))

(defn friend?
  [id]
  (contains? (set (get-contacts-ids)) id))

(defn get-user-by-id
  [id]
  (if id
    (let [me (me)]
      (if (= id (:id me))
        me
        (get-by-id :contacts id)))))

(defn conversaction-exists?
  [id]
  (some #(and (= id (:to %))
              (false? (:is_channel %)))
        (get-conversations)))

(defn conversation-exists?
  [id]
  (some #(and (= id (:to %))
              (false? (:is_channel %)))
        (get-conversations)))

(defn channel-conversation-exists?
  [id]
  (some #(and (= id (:to %))
              (true? (:is_channel %)))
        (get-conversations)))

(defn create-conversation
  [to msg msg-created-at]
  (when-not (conversation-exists? to)
    (let [largest-id (get-largest-conversation-id)]
      (when-let [contact (get-by-id :contacts to)]
        (let [new-id (inc largest-id)
              conversation {:id new-id
                            :to to
                            :user_id (:id contact)
                            :name (:name contact)
                            :avatar (:avatar contact)
                            :last_message msg
                            :last_message_at msg-created-at}]
          (create :conversations conversation)
          (dispatch [:new-conversation conversation])
          new-id)))))

(defn update-conversations
  [m]
  (js/setTimeout
   (fn [] (update :conversations m))
   50))

(defn get-conversation-id-by-to
  [to]
  (when-let [conversation (get-by-fk :conversations :to to)]
    (:id conversation)))

(defn msg-convert
  [me-id msg]
  (when msg
    (let [body (:body msg)
          ret {:_id (:id msg)
               :createdAt (:created_at msg)
               :user {:_id (:user_id msg)
                      :_username (:username msg)
                      :_avatar (:avatar msg)
                      :_timezone (:timezone msg)
                      :_language (:language msg)
                      :_name (:name msg)}
               :is_delivered (:is_delivered msg)}]
      (if (util/photo-message? body)
        (assoc ret :image (util/real-photo-url body))
        (assoc ret :text body)))))

(defn ->user
  [msg]
  {:id (:_id msg)
   :username (:_username msg)
   :avatar (:_avatar msg)
   :timezone (:_timezone msg)
   :language (:_language msg)
   :name (:_name msg)})

(defn wrap-conversation-id
  ([msg]
   (wrap-conversation-id msg :user_id))
  ([{:keys [body created_at] :as msg} k]
   (let [cid (get-conversation-id-by-to (get msg k))
         cid (if cid cid (create-conversation (get msg k) body created_at))]
     (assoc msg :conversation_id cid))))

(defn get-conversation-messages
  [current-user-id current-callee n]
  (if-let [conversation-id (get-conversation-id-by-to current-callee)]
    (->> (get-col :messages)
         (filter #(= conversation-id (:conversation_id %)))
         (sort :created_at)
         (reverse)
         (take n)
         (mapv (partial msg-convert current-user-id)))
    []))

(defn get-groups-messages
  []
  (some->>
   (get-col :group-messages)
   (group-by :channel_id)))

(defn delete-channel-old-messages
  [channel-id message-id]
  (transform :messages
             (fn [col]
               (->> col
                    (remove #(and (= (:channel_id %) channel-id)
                                  (< (:id %) message-id)))
                    (vec)))))

(defn load-earlier-messages
  [current-user-id current-callee last-msg-id n]
  (if-let [conversation-id (get-conversation-id-by-to current-callee)]
    (->> (get-col :messages)
         (filter #(and (= (:conversation_id %) conversation-id)
                       (< (:id %) last-msg-id)))
         (sort :created_at)
         (reverse)
         (take n)
         (mapv (partial msg-convert current-user-id)))
    []))

(defn batch-insert-messages
  [messages]
  (when-let [messages (doall (map #(wrap-conversation-id % :user_id) messages))]
    (let [conversations (group-by :conversation_id messages)]
      (doseq [[id msgs] conversations]
        (let [message (last msgs)]
          (update :conversations {:id id
                                  :last_message (:body message)
                                  :last_message_at (:created_at message)}))))
    ;; update conversation
    (batch-create :messages messages)))

(defn create-channel-conversation
  [channel-id message]
  (when-not (channel-conversation-exists? channel-id)
    (let [channel (get-by-id :channels channel-id)
          {:keys [user_id name username avatar body created_at]} message
          largest-id (get-largest-conversation-id)
          new-conversation {:id (inc largest-id)
                            :is_channel true
                            :message_id (:id message)
                            :to channel-id
                            :user_id user_id
                            :name name
                            :username name
                            :avatar avatar
                            :last_message body
                            :last_message_at (if created_at (tc/to-date created_at))
                            :is_read true}]
      (create :conversations new-conversation)
      (dispatch [:new-conversation new-conversation]))))

(defn delete-channel-conversation
  [channel-id]
  ;; TODO delete messages
  (transform :conversations
             (fn [col]
               (-> (remove #(and (= (:to %) channel-id)
                                 (true? (:is_channel %))) col)
                   (vec)))))

(defn join-channel
  [channel]
  (transform :current-user
             (fn [user]
               (clojure.core/update user :channels conj channel))))

(defn leave-channel
  [channel-id]
  (transform :current-user
             (fn [user]
               (clojure.core/update user
                                    :channels
                                    (fn [xs]
                                      (remove #(= (str channel-id) (str (:id %)))
                                              xs)))))
  (delete-channel-conversation channel-id))

;; debug
(defn debug
  [command & args]
  (go
    (prn "Result: "
         (<! (apply command args)))))

(defn query
  ([table]
   (query table identity))
  ([table handler]
   (go
     (let [[err v] (<! (get-item table))]
       (when (development?) (prn {:table table
                                  :error err
                                  :val v}))
       (if (nil? err)
         (set-item table (handler v))
         (throw v))))))

(defonce lym-channel
  {:need_invite false,
   :block false,
   :is_private false,
   :locale "english",
   :purpose nil,
   :name "Lymchat",
   :type "lymchat",
   :members_count 0,
   :id "10000000-3c59-4887-995b-cf275db86343",
   :picture nil,
   :user_id "10000000-3c59-4887-995b-cf275db86343",
   :created_at #inst "2016-08-17T13:59:52.604000000-00:00"})
