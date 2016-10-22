(ns lymchat.shared.ui
  (:require [reagent.core :as r]
            [lymchat.shared.colors :as color]
            [clojure.string :as str]
            [re-frame.core :refer [dispatch]]))

(enable-console-print!)

(def react-native (js/require "react-native"))

(def platform (.-Platform react-native))

(defn get-platform
  []
  (.-OS platform))

(defn ios?
  []
  (= "ios" (get-platform)))

(defn android?
  []
  (= "android" (get-platform)))

(def text (r/adapt-react-class (.-Text react-native)))
(def view (r/adapt-react-class (.-View react-native)))
(def scroll (r/adapt-react-class (.-ScrollView react-native)))
(def Image (.-Image react-native))
(def image (r/adapt-react-class Image))
(defn image-prefetch
  [url]
  (.prefetch Image url))
(def interaction-manager (.-InteractionManager react-native))
(def device-event-emitter (.-DeviceEventEmitter react-native))

(defn run-after-interactions
  [cb]
  (.runAfterInteractions interaction-manager
                         cb))

;; refresh-control
(def refresh-control (r/adapt-react-class (.-RefreshControl react-native)))
(def StatusBar (.-StatusBar react-native))
(def status-bar (r/adapt-react-class StatusBar))

(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight react-native)))
(def touchable-opacity (r/adapt-react-class (.-TouchableOpacity react-native)))
(def touchable-without-feedback (r/adapt-react-class (.-TouchableWithoutFeedback react-native)))
(def touchable-native-feedback (r/adapt-react-class (.-TouchableNativeFeedback react-native)))
(def input (r/adapt-react-class (.-TextInput react-native)))
(def switch-ios (r/adapt-react-class (.-SwitchIOS react-native)))
(def switch (r/adapt-react-class (.-Switch react-native)))
(def vibration (.-Vibration react-native))

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
(def ListView (.-ListView react-native))
(def list-view (r/adapt-react-class (.-ListView react-native)))
(def SwipeableListView (js/require "SwipeableListView"))
(def swipeable-list-view (r/adapt-react-class SwipeableListView))

;; dismisskeyboard
(def Keyboard (.-Keyboard react-native))
(def dismiss-keyboard (js/require "dismissKeyboard"))
(defn dismiss-keyboard-cp
  [child]
  (let [dismissed? (r/atom false)]
    (fn []
      (if dismissed?
        child

        [touchable-opacity {:style {:flex 1
                                    :activeOpacity 1
                                    :background-color "transparent"}
                            :on-press (fn []
                                        (dismiss-keyboard)
                                        (reset! dismissed? true))}
         child]))))

(def activity-indicator (r/adapt-react-class (.-ActivityIndicator react-native)))
(def linking (.-Linking react-native))
(def app-registry (.-AppRegistry react-native))

(defn open-url [url]
  (.openURL linking url))

(def dimensions (.-Dimensions react-native))
(def modal (r/adapt-react-class (.-Modal react-native)))

;; net-info
(def net-info (.-NetInfo react-native))

;; clipboard
(def clipboard (.-Clipboard react-native))

;; aws3
(def aws3 (js/require "react-native-aws3"))
(def rns3 (.-RNS3 aws3))

;; navigationexperimental
(def card-stack (r/adapt-react-class (.-CardStack (.-NavigationExperimental react-native))))
(def navigation-header-comp (.-Header (.-NavigationExperimental react-native)))
(def navigation-header (r/adapt-react-class navigation-header-comp))
(def header-title (r/adapt-react-class (.-Title (.-Header (.-NavigationExperimental react-native)))))

;; app intro
(def app-intro (r/adapt-react-class (.-default (js/require "react-native-app-intro"))))

(def gifted-chat (r/adapt-react-class (.-GiftedChat (js/require "react-native-gifted-chat"))))

(def parsed-text (r/adapt-react-class (.-default (js/require "react-native-parsed-text"))))

;; moment
(def moment (js/require "moment"))

(def colors color/colors)

(defn alert
  ([title]
   (.alert (.-Alert react-native) title))
  ([title message buttons]
   (.alert (.-Alert react-native) title message (clj->js buttons))))

(defn prompt
  [title message cb-or-buttons]
  (if (ios?)
    (.prompt (.-AlertIOS react-native) title message cb-or-buttons)))

(defn actions
  [options handler]
  (dispatch [:show-action-sheet (clj->js options) handler]))

;; Exponentjs
(def exponent (js/require "exponent"))
;; Exponentjs api
(def Amplitude (aget exponent "Amplitude"))
(def Asset (aget exponent "Asset"))
(def Components (aget exponent "Components"))
(def app-loading (r/adapt-react-class Components.AppLoading))
(def blur-view (r/adapt-react-class Components.BlurView))
(def gradient (r/adapt-react-class Components.LinearGradient))
(def video (r/adapt-react-class Components.Video))
(def Constants (aget exponent "Constants"))
(def Contacts (aget exponent "Contacts"))
(def Crypto (aget exponent "Crypto"))
(def Fabric (aget exponent "Fabric"))
(def Facebook (aget exponent "Facebook"))
(def FileSystem (aget exponent "FileSystem"))
(def Font (aget exponent "Font"))
(def ImageCropper (aget exponent "ImageCropper"))
(def ImagePicker (aget exponent "ImagePicker"))
(def Notifications (aget exponent "Notifications"))
(def Permissions (aget exponent "Permissions"))
(def Util (aget exponent "Util"))
(def registerRootComponent (aget exponent "registerRootComponent"))
;; Webrtc
(def RTCPeerConnection (aget Components "RTCPeerConnection"))
(def RTCIceCandidate (aget Components "RTCIceCandidate"))
(def RTCSessionDescription (aget Components "RTCSessionDescription"))
(def RTCView (aget Components "RTCView"))
(def MediaStream (aget Components "MediaStream"))
(def MediaStreamTrack (aget Components "MediaStreamTrack"))
(def getUserMedia (aget Components "getUserMedia"))

;; ex-navigation
(def ex-navigation (js/require "@exponent/ex-navigation"))
(def create-router (aget ex-navigation "createRouter"))
(def NavigationProvider (aget ex-navigation "NavigationProvider"))
(def navigation-provider (r/adapt-react-class NavigationProvider))
(def StackNavigation (aget ex-navigation "StackNavigation"))
(def stack-navigation (r/adapt-react-class StackNavigation))
(def TabNavigation (aget ex-navigation "TabNavigation"))
(def tab-navigation (r/adapt-react-class TabNavigation))
(def TabNavigationItem (aget ex-navigation "TabNavigationItem"))
(def tab-navigation-item (r/adapt-react-class TabNavigationItem))

;; action-sheet
(def action-sheet (r/adapt-react-class (.-default (js/require "@exponent/react-native-action-sheet"))))

;; vector-icons
(def font-awesome (.-default (js/require "@exponent/vector-icons/FontAwesome")))
(def icon (r/adapt-react-class font-awesome))
(def icon-button (r/adapt-react-class (.-Button font-awesome)))

(def material-icons (.-default (js/require "@exponent/vector-icons/MaterialIcons")))
(def material-icon (r/adapt-react-class material-icons))
(def material-icon-button (r/adapt-react-class (.-Button material-icons)))

;; helper

(defn ->ds
  ([]
   (->ds {:rowHasChanged (partial not=)
          :sectionHeaderHasChanged (partial not=)}))
  ([options]
   (let [ds (.-DataSource ListView)]
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
