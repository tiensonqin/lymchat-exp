(ns lymchat.shared.scene.profile
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [lymchat.styles :refer [styles pl-style]]
            [lymchat.shared.ui :refer [text view image touchable-highlight touchable-opacity button icon icon-button material-icon-button colors gradient scroll alert input dimensions run-after-interactions prompt] :as ui]
            [lymchat.util :as util]))

(defn seperator-cp
  []
  [view {:style {:border-width 0.5
                 :border-color "#ccc"
                 :margin-top 10
                 :margin-bottom 10}}])

(defn buttons-cp
  [user]
  (let [invite? (r/atom false)]
    (fn []
      [view {:style {:flex-direction "row"
                     :justify-content "space-between"
                     :margin-bottom 15}}
       [button {:style {:border-width 1.5
                        :border-color "#rgba(0,0,0,0.3)"
                        :border-radius 4
                        :width 120
                        :height 40
                        :background-color "transparent"
                        :justify-content "center"
                        :align-items "center"}
                :text-style {:font-size 20
                             :font-weight "500"
                             :color "#rgba(0,0,0,0.6)"}
                :on-press #(do
                             (dispatch [:pop-jump-in-conversation user]))}
        "Message"]])))

(defn original-profile-cp
  ([user]
   (original-profile-cp user false))
  ([user channel?]
   (r/create-class
    {:reagent-render
     (fn []
      (if user
       (let [current-user (subscribe [:current-user])
             self? (= (str (:id @current-user)) (str (:id user)))]
         (let [{:keys [id name username avatar language timezone]} user
               {:keys [width height]} (js->clj (.get dimensions "window") :keywordize-keys true)]
           [view {:style (pl-style :header-container)}
            [image {:style {:width width
                            :height 250
                            :resizeMode "cover"}
                    :source {:uri (util/get-avatar avatar :large)}}]
            [view {:style {:position "absolute"
                           :left 10
                           :top 215
                           :background-color "transparent"}}
             [text {:style {:font-size 16
                            :font-weight "bold"
                            :color "#FFFFFF"
                            }}
              (str "@" username)]]

            [view {:style {:background-color "#FFF"
                           :flex 1
                           :padding-top 15
                           :padding-left 15
                           :padding-right 15}}
             (if (and channel? (not self?))
               [buttons-cp user])

             [text {:style {:color "grey"}}
              "Native language"]
             [text {:style {:margin-top 5
                            :font-size 16}}
              language]

             [seperator-cp]

             (when timezone
               [view
                [text {:style {:color "grey"}}
                 "Timezone"]
                [text {:style {:margin-top 5
                               :font-size 16}}
                 timezone]

                [seperator-cp]])]]))
       [view {:style (merge
                      (pl-style :header-container)
                      {:justify-content "center"
                       :align-items "center"})}
        [text {:style {:font-size 20}}
         "Username not exists."]]))})))

(defn show-profile-action-sheet
  [user channel?]
  (let [{:keys [id name avatar]} user
        buttons (if channel?
                  ["Report"
                   "Cancel"]
                  ["Report"
                   "Delete"
                   "Cancel"])
        options (if channel?
                  {:options buttons
                   :cancelButtonIndex 1}
                  {:options buttons
                   :destructiveButtonIndex 1
                   :cancelButtonIndex 2})
        handler (fn [i] (if channel?
                         (case i
                           0 (prompt (str "I want to report " name ".")
                                     nil
                                     (fn [title]
                                       (when title
                                         (dispatch [:report {:type "user"
                                                             :type_id id
                                                             :title title
                                                             :picture avatar}]))))
                           ;; cancel
                           nil)
                         (case i
                           0 (prompt (str "I want to report " name ".")
                                     nil
                                     (fn [title]
                                       (when title
                                         (dispatch [:report {:type "user"
                                                             :type_id id
                                                             :title title
                                                             :picture avatar}]))))

                           1 (alert (str "Do you want to delete contact " name "?")
                                    ""
                                    [{:text "Yes"
                                      :onPress #(do
                                                  (dispatch [:delete-contact id]))}
                                     {:text "Cancel"
                                      :style "cancel"}])

                           ;; cancel
                           nil)))]
    (ui/actions options handler)))

(defn profile-right-button
  [user channel?]
  [touchable-opacity {:on-press #(show-profile-action-sheet user channel?)}
   [text {:style (assoc (pl-style :right-menu)
                        :margin-right 10)}
    "..."]])

(defn profile-cp
  [props]
  (let [{:keys [title user channel?]} (util/keywordize props)]
    (prn {:title title
          :user user
          :channel? channel?})
    (util/wrap-route (original-profile-cp user channel?)
                     {:navigationBar {:title title
                                      :titleStyle {:color "#333"
                                                   :fontFamily "pacifico"}
                                      :translucent true
                                      :backgroundColor "rgba(255,255,255,0.5)"
                                      :renderRight (fn [] (r/as-element [profile-right-button user channel?]))}})))

(defn original-change-name-cp
  []
  (let [current-user (subscribe [:current-user])
        first-name (r/atom "")
        last-name (r/atom "")]
    (fn []
      [view {:style (assoc (pl-style :header-container)
                           :padding 20
                           :background-color "#efefef")}
       [input {:style {:padding-left 10
                       :font-size 16
                       :border-width 2
                       :border-color "rgba(0,0,0,0.4)"
                       :border-radius 6}
               :height 40
               :auto-correct true
               :maxLength 32
               :clear-button-mode "always"
               :value @first-name
               :placeholder "First name"
               :on-change-text (fn [value]
                                 (reset! first-name value)
                                 (r/flush))}]

       [input {:style {:margin-top 30
                       :padding-left 10
                       :font-size 16
                       :border-width 2
                       :border-color "rgba(0,0,0,0.4)"
                       :border-radius 6}
               :height 40
               :auto-correct true
               :maxLength 32
               :clear-button-mode "always"
               :value @last-name
               :placeholder "Last name"
               :returnKeyType "go"
               :onSubmitEditing (fn []
                                  ;; validate
                                  (if (or (nil? (re-find util/name-pattern @first-name))
                                          (nil? (re-find util/name-pattern @last-name)))
                                    (alert "Sorry, please input valid name!")
                                    (dispatch [:set-name @first-name @last-name])))
               :on-change-text (fn [value]
                                 (reset! last-name value)
                                 (r/flush))}]])))

(defn change-name-cp
  [props]
  (let [{:keys [title]} (util/keywordize props)]
    (util/wrap-route original-change-name-cp
                     {:navigationBar {:title title
                                      :titleStyle {:color "#333"
                                                   :fontFamily "pacifico"}
                                      :translucent true
                                      :backgroundColor "rgba(255,255,255,0.5)"}})))
