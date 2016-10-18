(ns lymchat.shared.scene.language
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [lymchat.styles :refer [styles pl-style]]
            [lymchat.shared.ui :refer [react-native list-view view text input touchable-opacity colors]]
            [lymchat.locales :refer [locales]]
            [lymchat.util :as util]))

(defn row-cp
  [row]
  [touchable-opacity {:on-press #(dispatch [:set-native-lang row])}
   [text {:style {:padding-top 15
                  :padding-bottom 15
                  :padding-left 10
                  :font-size 16}}
    row]])

(defn filter-langs
  [pattern langs]
  (->
   (if pattern
     (->
      (filter #(clojure.string/starts-with? % pattern) langs)
      (distinct)
      (sort))
     ["Chinese" "Spanish" "English" "Hindi" "Arabic" "Portuguese" "Bengali" "Russian" "Japanese" "Javanese"])))

(defn original-language-cp
  []
  (let [current-input (r/atom nil)
        ds (.-DataSource (.-ListView react-native))
        list-view-ds (new ds #js {:rowHasChanged #(not= %1 %2)})]
    (fn []
      [view {:style (assoc (pl-style :header-container)
                           :background-color (:white800 colors))}
       [view {:style (:search-row styles)}
        [input {:style (:search-text-input styles)
                :auto-correct false
                :clear-button-mode "always"
                :on-change-text (fn [value] (reset! current-input value))
                :placeholder "Search..."}]
        [list-view {:keyboardShouldPersistTaps true
                    :enableEmptySections true
                    :initialListSize 20
                    :automaticallyAdjustContentInsets false
                    :dataSource (.cloneWithRows list-view-ds (clj->js (filter-langs @current-input (vals locales))))
                    :renderRow (fn [row] (r/as-element (row-cp row)))
                    :renderSeparator (fn [section-id row-id]
                                       (r/as-element
                                        [view {:key (str section-id "-" row-id)
                                               :style {:height 0.5
                                                       :background-color "#ccc"}}]))}]]])))

(defn language-cp
  [props]
  (let [{:keys [title]} (util/keywordize props)]
    (util/wrap-route original-language-cp
                     {:navigationBar {:title title
                                      :titleStyle {:color "#333"
                                                   :fontFamily "pacifico"}
                                      :translucent true
                                      :backgroundColor "rgba(255,255,255,0.5)"}})))
