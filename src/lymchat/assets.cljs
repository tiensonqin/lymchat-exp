(ns lymchat.assets
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [lymchat.shared.ui :as ui]
            [cljs.core.async :refer [<!]]))

(defn- cache-images
  [images]
  (for [image images]
    (when image
      (-> (.fromModule ui/Asset image)
          (.downloadAsync)))))

(defn- cache-fonts
  [fonts]
  (for [font fonts]
    (when font
      (.loadAsync ui/Font font))))

(defn- cast-as-array
  [coll]
  (if (or (array? coll)
          (not (reduceable? coll)))
    coll
    (into-array coll)))

(defn all
  [coll]
  (.all js/Promise (cast-as-array coll)))

(defn cache-assets
  [images fonts cb]
  (->
   (all (concat (cache-fonts (clj->js fonts)) (cache-images (clj->js images))))
   (.then (fn [resp]
            (if cb (cb))))
   (.catch (fn [err]
             (println "Loading assets failed: " (aget err "message"))))))
