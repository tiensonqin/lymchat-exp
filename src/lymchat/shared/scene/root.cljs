(ns lymchat.shared.scene.root
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [lymchat.styles :refer [styles]]
            [lymchat.shared.ui :refer [text view image touchable-highlight icon-button colors status-bar gradient image-prefetch activity-indicator] :as ui]
            [lymchat.ws :as ws]
            [lymchat.util :as util]
            [lymchat.assets :as assets]
            [lymchat.notification :as noti]
            [lymchat.navigation.router :as router]
            [lymchat.shared.scene.intro :as intro]
            [lymchat.shared.scene.guide :as guide]
            [lymchat.exponent.notification :as notification]))

(aset js/console "disableYellowBox" true)

(defn status-bar-cp []
  (cond
    (ui/ios?)
    [ui/status-bar {:bar-style "default"}]

    :else
    nil))

(defn app-root []
  (let [current-user (subscribe [:current-user])
        signing? (subscribe [:signing?])
        guide-step (subscribe [:guide-step])
        app-ready? (subscribe [:app-ready?])]

    (assets/cache-assets []
                         [{"indie-flower" (js/require "./assets/fonts/IndieFlower.ttf")}
                          {"pacifico" (js/require "./assets/fonts/Pacifico.ttf")}]
                         #(dispatch [:set-app-ready? true]))

    (fn []
      (cond
        (false? @app-ready?)
        [ui/app-loading]

        (nil? @current-user)
        [intro/intro-cp]

        @signing?
        [view {:style {:flex 1
                       :justify-content "center"
                       :align-items "center"
                       :background-color "rgba(0,0,0,0.7)"}}
         [activity-indicator {:animating true
                              :size "large"
                              :color "#FFFFFF"}]]

        (and (or (false? (:username @current-user))
                 (empty? (:channels @current-user)))
             (not= :done @guide-step))
        (do
          (ws/start!)
          [guide/guide-cp])

        :else
        (r/create-class
         {:component-will-mount
          (fn [this]
            (util/show-statusbar)

            (notification/register-for-push-notifications)

            (.addListener ui/device-event-emitter
                          "Exponent.notification"
                          noti/handler))

          :component-did-mount
          (fn [this]
            (ws/start!)

            (util/net-handler
             (fn [net-state]
               (if (true? net-state)
                 (dispatch [:set-net-state true])
                 (dispatch [:set-net-state false])))))

          :reagent-render
          (fn []
            [ui/action-sheet {:ref (fn [c] (dispatch [:set-action-sheet c]))}
             [view {:style {:flex 1}}
              [ui/navigation-provider {:router router/router}
               [ui/stack-navigation {:id "root"
                                     :initialRoute "tabs"}]]
              [status-bar-cp]]])})))))
