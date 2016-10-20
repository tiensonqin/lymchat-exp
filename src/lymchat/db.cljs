(ns lymchat.db)

(def app-db {:app-ready? false
             :nav nil
             :tab-bar-visible? true
             :current-user nil
             :conversations []
             :invites []
             :channel-messages []
             :contacts []
             :latest-message-id nil
             :current-tab "Lymchat"
             :contact-search-input nil
             :channels-search-input nil
             :username-input nil
             :hidden-input nil
             :net-state nil
             :loading? false
             :uploading? false
             :sync? false
             :signing? false

             :no-disturb? false

             ;; chat
             :current-messages []
             :new-message? false

             :current-channel nil
             :mentions nil


             :channel-members {}
             :channels-search-result nil
             :channel-auto-focus false
             :recommend-channels []

             :photo-modal? {}

             :temp-avatar nil
             :header? true
             :guide-step nil
             :username-set? false
             :search-members-result nil

             ;; audio, video
             :current-callee nil
             :open-video-call-modal? false
             :in-call? false
             :local-stream nil
             :remote-stream nil})
