(ns lymchat.navigation.router
  (:require [lymchat.shared.ui :as ui]
            [lymchat.shared.scene.chat :as chat]
            [lymchat.shared.scene.group :as group]
            [lymchat.shared.scene.me :as me]
            [lymchat.shared.scene.profile :as profile]
            [lymchat.shared.scene.contact :as contact]
            [lymchat.shared.scene.guide :as guide]
            [lymchat.shared.scene.mention :as mention]
            [lymchat.shared.scene.member :as member]
            [lymchat.shared.scene.invite :as invite]
            [lymchat.shared.scene.language :refer [language-cp]]
            [lymchat.shared.scene.call :as call]
            [lymchat.navigation.tab :as tab]))

(def router (ui/create-router
             (fn []
               #js {:tabs tab/navigation-tab
                    :home chat/chats
                    :mentions mention/mentions-cp
                    :groups group/groups-cp
                    :search-groups group/search-cp
                    :me me/me-cp
                    :conversation chat/conversation-cp
                    :channel-conversation chat/channel-conversation-cp
                    :profile profile/profile-cp
                    :contacts contact/contacts-cp
                    :set-native-language language-cp
                    :show-avatar me/avatar-cp
                    :channel-members member/members-cp
                    :change-name profile/change-name-cp
                    :invitations invite/invite-request-cp
                    :video-call call/call-cp})))
