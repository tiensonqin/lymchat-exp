(ns lymchat.navigation.tab
  (:require [lymchat.shared.ui :as ui]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe]]))

(defn icon-cp
  [icon-name selected?]
  [ui/view {:style {:align-items "center"
                    :justify-content "center"}}
   [ui/material-icon {:name icon-name
                      :size 24
                      :color (if selected? "#65bc54" "#888")}]])

(defn tab-item-cp
  [id icon-name badge]
  (r/create-element
   ui/TabNavigationItem
   #js{:className "cljs-tab-item"
       :id id
       :badgeText badge
       :renderIcon (fn [selected?]
                     (r/as-element [icon-cp icon-name selected?]))}
   (r/as-element
    [ui/stack-navigation
     {:defaultRouteConfig {:navigationBar
                           {:tintColor "#666"
                            :backgroundColor "#efefef"}}
      :initialRoute id}])))

(defn navigation-tab
  []
  (let [invites (subscribe [:invites])
        unread-mentions-count (subscribe [:unread-mentions-count])]
    (fn []
      (r/as-element
       [ui/tab-navigation
        {:tabBarColor "#fefefe"
         :tabBarHeight 56
         :initialTab "home"}
        (tab-item-cp "home" "chat-bubble-outline" nil)
        (tab-item-cp "mentions" "notifications-none" @unread-mentions-count)
        (tab-item-cp "groups" "group-work" nil)
        (tab-item-cp "me" "person-outline" (if (> (count @invites) 0) (count @invites)))]))))
