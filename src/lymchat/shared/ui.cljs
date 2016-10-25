(ns lymchat.shared.ui
  (:require [reagent.core :as r]
            [lymchat.shared.colors :as color]
            [clojure.string :as str]
            [re-frame.core :refer [dispatch]]))

(enable-console-print!)

(def react-native (js/require "react-native"))

(def platform (aget react-native "Platform"))

(def moment (js/require "moment"))

(defn get-platform
  []
  (.-OS platform))

(defn ios?
  []
  (= "ios" (get-platform)))

(defn android?
  []
  (= "android" (get-platform)))

(def text (r/adapt-react-class (aget react-native "Text")))
(def view (r/adapt-react-class (aget react-native "View")))
(def scroll (r/adapt-react-class (aget react-native "ScrollView")))
(def Image (aget react-native "Image"))
(def image (r/adapt-react-class Image))
(defn image-prefetch
  [url]
  (.prefetch Image url))
(def interaction-manager (aget react-native "InteractionManager"))

(defn run-after-interactions
  [cb]
  (.runAfterInteractions interaction-manager
                         cb))

;; refresh-control
(def refresh-control (r/adapt-react-class (aget react-native "RefreshControl")))
(def StatusBar (aget react-native "StatusBar"))
(def status-bar (r/adapt-react-class StatusBar))

(def touchable-highlight (r/adapt-react-class (aget react-native "TouchableHighlight")))
(def touchable-opacity (r/adapt-react-class (aget react-native "TouchableOpacity")))
(def touchable-without-feedback (r/adapt-react-class (aget react-native "TouchableWithoutFeedback")))
(def touchable-native-feedback (r/adapt-react-class (aget react-native "TouchableNativeFeedback")))
(def input (r/adapt-react-class (aget react-native "TextInput")))
(def switch (r/adapt-react-class (aget react-native "Switch")))
(def vibration (aget react-native "Vibration"))
(def device-event-emitter (aget react-native "DeviceEventEmitter"))


(defn vibrate
  []
  (.vibrate vibration))

(defn button
  [{:keys [style text-style on-press]
    :or {style {}
         text-style {}}}
   title]
  [touchable-opacity {:on-press on-press
                      :style (merge style
                                    {:justify-content "center"
                                     :align-items "center"})}
   [text {:style text-style}
    title]])
(def ListView (aget react-native "ListView"))
(def list-view (r/adapt-react-class (aget react-native "ListView")))
(def SwipeableListView (js/require "SwipeableListView"))
(def swipeable-list-view (r/adapt-react-class SwipeableListView))

;; dismisskeyboard
(def Keyboard (aget react-native "Keyboard"))

(def activity-indicator (r/adapt-react-class (aget react-native "ActivityIndicator")))
(def linking (aget react-native "Linking"))
(def app-registry (aget react-native "AppRegistry"))

(defn open-url [url]
  (.openURL linking url))

(def dimensions (aget react-native "Dimensions"))
(def modal (r/adapt-react-class (aget react-native "Modal")))

;; net-info
(def net-info (aget react-native "NetInfo"))

;; clipboard
(def clipboard (aget react-native "Clipboard"))

;; aws3
(def AWS3 (js/require "react-native-aws3"))
(def RNS3 (aget AWS3 "RNS3"))

;; app intro
(def AppIntro (js/require "react-native-app-intro"))
(def app-intro (r/adapt-react-class (aget AppIntro "default")))

(def GiftedChat (aget (js/require "react-native-gifted-chat") "GiftedChat"))
(def gifted-chat (r/adapt-react-class GiftedChat))

(def ParsedText (js/require "react-native-parsed-text"))
(def parsed-text (r/adapt-react-class (aget ParsedText "default")))

(def colors color/colors)

(defn alert
  ([title]
   (.alert (aget react-native "Alert") title))
  ([title message buttons]
   (.alert (aget react-native "Alert") title message (clj->js buttons))))

(defn prompt
  [title message cb-or-buttons]
  (if (ios?)
    (.prompt (aget react-native "AlertIOS") title message cb-or-buttons)))

(defn actions
  [options handler]
  (dispatch [:show-action-sheet (clj->js options) handler]))

;; Exponentjs
(def Exponent (js/require "exponent"))
;; Exponentjs api
(def Amplitude (aget Exponent "Amplitude"))
(def Asset (aget Exponent "Asset"))
(def Components (aget Exponent "Components"))
(def app-loading (r/adapt-react-class Components.AppLoading))
(def blur-view (r/adapt-react-class Components.BlurView))
(def gradient (r/adapt-react-class Components.LinearGradient))
(def Constants (aget Exponent "Constants"))
(def Contacts (aget Exponent "Contacts"))
(def Crypto (aget Exponent "Crypto"))
(def Fabric (aget Exponent "Fabric"))
(def Facebook (aget Exponent "Facebook"))
(def FileSystem (aget Exponent "FileSystem"))
(def Font (aget Exponent "Font"))
(def ImagePicker (aget Exponent "ImagePicker"))
(def Notifications (aget Exponent "Notifications"))
(def Permissions (aget Exponent "Permissions"))
(def Util (aget Exponent "Util"))
;; Webrtc
;; (def RTCPeerConnection (aget Components "RTCPeerConnection"))
;; (def RTCIceCandidate (aget Components "RTCIceCandidate"))
;; (def RTCSessionDescription (aget Components "RTCSessionDescription"))
;; (def RTCView (aget Components "RTCView"))
;; (def MediaStream (aget Components "MediaStream"))
;; (def MediaStreamTrack (aget Components "MediaStreamTrack"))
;; (def getUserMedia (aget Components "getUserMedia"))

;; ex-navigation
(def ExNavigation (js/require "@exponent/ex-navigation"))
(def create-router (aget ExNavigation "createRouter"))
(def NavigationProvider (aget ExNavigation "NavigationProvider"))
(def navigation-provider (r/adapt-react-class NavigationProvider))
(def StackNavigation (aget ExNavigation "StackNavigation"))
(def stack-navigation (r/adapt-react-class StackNavigation))
(def TabNavigation (aget ExNavigation "TabNavigation"))
(def tab-navigation (r/adapt-react-class TabNavigation))
(def TabNavigationItem (aget ExNavigation "TabNavigationItem"))
(def tab-navigation-item (r/adapt-react-class TabNavigationItem))

;; action-sheet
(def ActionSheet (js/require "@exponent/react-native-action-sheet"))
(def action-sheet (r/adapt-react-class (aget ActionSheet "default")))

;; vector-icons
(def FontAwesome (js/require "@exponent/vector-icons/FontAwesome"))

(def icon (r/adapt-react-class (aget FontAwesome "default")))
(def FontAwesomeButton (aget FontAwesome "default" "Button"))
(def icon-button (r/adapt-react-class FontAwesomeButton))

(def MaterialIcons (js/require "@exponent/vector-icons/MaterialIcons"))
(def material-icon (r/adapt-react-class (aget MaterialIcons "default")))
(def MaterialIconButton (aget MaterialIcons "default" "Button"))
(def material-icon-button (r/adapt-react-class MaterialIconButton))

;; helper

(defn ->ds
  ([]
   (->ds {:rowHasChanged (partial not=)
          :sectionHeaderHasChanged (partial not=)}))
  ([options]
   (let [ds (aget ListView "DataSource")]
     (new ds (clj->js options)))))

(defn clone-ds
  [ds data]
  (.cloneWithRowsAndSections ds (clj->js data)))

(defn refresh
  [value on-refresh-cb]
  (r/as-element
   [refresh-control
    {:refreshing value
     :onRefresh on-refresh-cb}]))

(defn ring
  []
  (prn "Ring ..."))
(defn stop-ring
  []
  (prn "Stop ring"))
