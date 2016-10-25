(ns lymchat.exponent.notification
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [lymchat.shared.ui :as ui]
            [lymchat.util :refer [promise->chan]]
            [lymchat.ws :as ws]
            [cljs.core.async :as async]))

(defn register-for-push-notifications
  []
  ;; Android remote notification permissions are granted during the app
  ;; install, so this will only ask on iOS
  (go
    (let [status (-> (.askAsync ui/Permissions ui/Permissions.REMOTE_NOTIFICATIONS)
                     (promise->chan)
                     (async/<!)
                     (aget "status"))]
      ;; (when (= "granted" status)
      ;;   (let [token (-> (.getExponentPushTokenAsync ui/Notifications)
      ;;                   (promise->chan)
      ;;                   (async/<!))]
      ;;     (ws/register-token token)))
      (let [token (-> (.getExponentPushTokenAsync ui/Notifications)
                      (promise->chan)
                      (async/<!))]
        (prn {:token token})
        (ws/register-token token)))))
