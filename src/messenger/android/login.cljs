(ns messenger.android.login
  (:require-macros
   [natal-shell.components :refer [view text image touchable-highlight list-view
                                   toolbar-android text-input]]
   [natal-shell.core :refer [with-error-view]])
  (:require [om.next :as om :refer-macros [defui]]
            [re-natal.support :as sup]
            [messenger.state :as state]
            [messenger.comm.intercom :as intercom :refer [set-user-phone-number]]
            [messenger.utils.utils :refer [log toast http-post]]
            [messenger.utils.phone-number :refer [format-phone-number]]
            [messenger.utils.resources :as res]
            [messenger.components.spinner :refer [spinner]]
            [messenger.android.sign-up-confirm :refer [sign-up-confirm]]
            [messenger.constants :refer [ethereum-rpc-url]]
            [messenger.components.iname :as in]))

(def nav-atom (atom nil))

(defn show-confirm-view []
  (swap! state/app-state assoc :loading false)
  (intercom/show-signup-confirm @nav-atom))

(defn sign-up []
  (swap! state/app-state assoc :loading true)
  (let [app-state (state/state)
        phone-number (:user-phone-number app-state)
        whisper-identity (:public (:user-identity app-state))]
    (intercom/sign-up phone-number whisper-identity show-confirm-view)))

(defn update-phone-number [value]
  (let [formatted (format-phone-number value)]
    (set-user-phone-number formatted)))

(defui Login
  static in/IName
  (get-name [this]
    :login/login)
  static om/IQuery
  (query [this]
         '[:user-phone-number :user-identity :loading])
  Object
  (render [this]
          (let [{{:keys [user-phone-number user-identity loading]} :login/login} (om/props this)
                {:keys [nav]} (om/get-computed this)]
            (reset! nav-atom nav)
            (view
             {:style {:flex 1}}
             (view
              {:style {:flex 1
                       :backgroundColor "white"}}
              (toolbar-android {:logo res/logo-icon
                                :title "Login"
                                :titleColor "#4A5258"
                                :style {:backgroundColor "white"
                                        :height 56
                                        :elevation 2}})
              (view {:style { ;; :alignItems "center"
                             }}
                    (text-input {:underlineColorAndroid "#9CBFC0"
                                 :placeholder "Enter your phone number"
                                 :keyboardType "phone-pad"
                                 :onChangeText (fn [value]
                                                 (update-phone-number value))
                                 :style {:flex 1
                                         :marginHorizontal 18
                                         :lineHeight 42
                                         :fontSize 14
                                         :fontFamily "Avenir-Roman"
                                         :color "#9CBFC0"}}
                                user-phone-number)
                    (touchable-highlight {:onPress #(sign-up)
                                          :style {:alignSelf "center"
                                                  :borderRadius 7
                                                  :backgroundColor "#E5F5F6"
                                                  :width 100}}
                                         (text {:style {:marginVertical 10
                                                        :textAlign "center"}}
                                               "Sign up"))))
             (when (or loading (not user-identity))
               (spinner {:visible true}))))))

(def login (om/factory Login))