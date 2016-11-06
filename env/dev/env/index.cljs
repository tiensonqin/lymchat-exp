(ns env.index
  (:require [env.dev :as dev]))

;; undo main.js goog preamble hack
(set! js/window.goog js/undefined)

(-> (js/require "figwheel-bridge")
    (.withModules #js {"exponent" (js/require "exponent"), "moment" (js/require "moment"), "./assets/images/intro/s1.png" (js/require "../../assets/images/intro/s1.png"), "@exponent/ex-navigation" (js/require "@exponent/ex-navigation"), "react" (js/require "react"), "@exponent/vector-icons/FontAwesome" (js/require "@exponent/vector-icons/FontAwesome"), "react-native-gifted-chat" (js/require "react-native-gifted-chat"), "./assets/fonts/IndieFlower.ttf" (js/require "../../assets/fonts/IndieFlower.ttf"), "./assets/fonts/Pacifico.ttf" (js/require "../../assets/fonts/Pacifico.ttf"), "react-native-parsed-text" (js/require "react-native-parsed-text"), "@exponent/react-native-action-sheet" (js/require "@exponent/react-native-action-sheet"), "SwipeableListView" (js/require "SwipeableListView"), "./assets/images/intro/s3.png" (js/require "../../assets/images/intro/s3.png"), "react-native-aws3" (js/require "react-native-aws3"), "./assets/images/logo.png" (js/require "../../assets/images/logo.png"), "react-native" (js/require "react-native"), "./assets/images/lym.png" (js/require "../../assets/images/lym.png"), "@exponent/vector-icons/MaterialIcons" (js/require "@exponent/vector-icons/MaterialIcons"), "react-native-app-intro" (js/require "react-native-app-intro"), "./assets/images/intro/s2.png" (js/require "../../assets/images/intro/s2.png"), "./assets/images/default.png" (js/require "../../assets/images/default.png")}
)
    (.start "main"))
