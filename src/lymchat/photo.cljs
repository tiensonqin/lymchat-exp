(ns lymchat.photo
  (:require [lymchat.shared.ui :refer [rns3 alert image] :as ui]
            [lymchat.config :as config]
            [lymchat.util :refer [uuid-v4 development?]]
            [re-frame.core :refer [dispatch]]
            [re-frame.db :refer [app-db]]
            [lymchat.storage :as storage]))

(defn new-photo-name
  [user-id]
  (str (uuid-v4) user-id ".jpg"))

(defn avatar-name
  [user-id]
  (str user-id ".jpg"))

(defn s3-upload
  [uri file-name success-cb]
  (let [file (clj->js {:uri uri
                       :name (if (development?)
                               (str "developments/" file-name)
                               file-name)
                       :type "image/JPEG"})
        options (clj->js (:s3-options @config/xxxxx))]
    (-> (.put rns3 file options)
        (.then (fn [response]
                 (if-not (= 201 (aget response "status"))
                   (alert "Failed to upload photo.")
                   (let [url (str "http://d24ujvixi34248.cloudfront.net/" (aget response "body" "postResponse" "key"))]
                     (dispatch [:set-uploading? false])
                     (success-cb url))))))))

(defn upload-handler
  [user-id type channel-id response]
  (let [original-uri (.-uri response)]
    (cond
      (.-cancelled response)
      (prn "User cancelled image picker")

      (.-error response)
      (prn "ImagePicker Error: " (.-error response))

      :else
      (case type
        :avatar
        (do
          (dispatch [:set-uploading? true])

          (dispatch [:set-temp-avatar original-uri])
          (s3-upload original-uri (avatar-name user-id) #(dispatch [:set-avatar %])))

        ;; (resize original-uri
        ;;         (fn [uri]
        ;;           (dispatch [:set-uploading? true])

        ;;           (dispatch [:set-temp-avatar original-uri])
        ;;           (s3-upload uri (avatar-name user-id) #(dispatch [:set-avatar %]))
        ;;           )
        ;;         1080
        ;;         100)

        :message
        (do
          (dispatch [:set-uploading? true])
          (if channel-id
            (dispatch [:send-channel-photo-message channel-id original-uri])
            (dispatch [:send-photo-message original-uri])))

        ;; (resize original-uri
        ;;         (fn [uri]
        ;;           (dispatch [:set-uploading? true])
        ;;           (if channel-id
        ;;             (dispatch [:send-channel-photo-message channel-id uri])
        ;;             (dispatch [:send-photo-message uri])))
        ;;         600
        ;;         100)

        nil))))

(defn upload
  ([user-id type]
   (upload user-id type nil))
  ([user-id type channel-id]
   (let [buttons ["Take Photo…"
                  "Choose from Library…"
                  "Cancel"]
         options {:options buttons
                  :cancelButtonIndex 2}]
     (ui/actions options
                 (fn [i]
                   (case i
                     ;; camera
                     0
                     (->
                      (.launchCameraAsync
                       ui/ImagePicker
                       #js {:allowsEditing true})
                      (.then (fn [response]
                               (upload-handler user-id type channel-id response))))

                     ;; library
                     1
                     (->
                      (.launchImageLibraryAsync
                       ui/ImagePicker
                       #js {:allowsEditing true})
                      (.then (fn [response]
                               (upload-handler user-id type channel-id response))))

                     ;; cancel
                     nil))))))

(defn offline-avatar-cp
  [user-id user-avatar style]
  [image {:source {:uri user-avatar}
          :style style}])
