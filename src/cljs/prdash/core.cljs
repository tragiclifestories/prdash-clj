(ns prdash.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [cljs.core.match :refer-macros [match]]
              [goog.history.EventType :as EventType]
              [prdash.navigation :as nav]
              [cljs.core.async :refer [put!]]
              [prdash.data :as d]
              [prdash.views :as v])
    (:import goog.history.Html5History
             goog.Uri))

;; ------------------------
;; Session state

(def token (atom nil))

;; -------------------------
;; Routes

(defn home-page []
  [:div
   [v/repo-form d/repo-chan]
   [v/pr-list d/open-prs]])

(defn current-page []
  [:div.container-fluid
   [:div.row
    [:div.col-md-10.col-md-offset-1                          
     (if (nil? @token)
       [v/login]
       [(session/get :current-page)])]]])


(secretary/defroute "/token/:auth-token" [auth-token]
  (do  (reset! token auth-token)
       (nav/push-state "/")
       (secretary/dispatch! "/")))

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

;; -------------------------
;; Initialize app
(defn mount-root []
  (do
    (d/listen! token)
    (.setInterval js/window d/get-updates! 30000)
    (reagent/render [current-page] (.getElementById js/document "app"))))

(defn init! []
  (nav/hook-browser-navigation!)
  (mount-root))
