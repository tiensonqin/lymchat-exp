(ns lymchat.shared.scene.login
  (:require
   [reagent.core :as r]
   [re-frame.core :refer [subscribe dispatch]]
   [lymchat.styles :refer [styles]]
   [lymchat.shared.ui :refer [text view image touchable-highlight icon-button colors status-bar dimensions Image gradient] :as ui]
   [lymchat.shared.login :as login]
   [lymchat.util :as util]))

(def logo (js/require "./assets/images/logo.png"))

(defn login-scene
  []
  (r/create-class {:component-will-mount
                   (fn []
                     (util/hide-statusbar))

                   :reagent-render
                   (fn []
                     (let [{:keys [width height]} (js->clj (.get dimensions "window") :keywordize-keys true)]
                       [view {:style (:login styles)}
                        [gradient {:style (:gradient styles)
                                   :colors #js [(:teal500 colors) (:teal400 colors) "rgba(255,255,255,0.1)"]}]
                        [view {:style {:position "absolute"
                                       :top 10
                                       :right 10
                                       :background-color "transparent"}}
                         [view {:flex 1
                                :flex-direction "row"}
                          [image {:style {:width 40
                                          :height 40
                                          :resizeMode "cover"}
                                  :source logo}]]]

                        [view
                         [view {:style {:margin-bottom 30}}
                          [icon-button {:name "facebook"
                                        :width 184.5
                                        :background-color "#3b5998"
                                        :on-press (fn []
                                                    (login/login-with-facebook))}
                           "Sign in with Facebook"]]]]))}))
