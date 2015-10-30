(ns prdash.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [prdash.navigation :as nav])
    (:import goog.history.Html5History
             goog.Uri))

;; -------------------------
;; Views

(def history (Html5History.))



(defn home-page []
  [:div [:h2 "Open PRs"]
   [:div [nav/link-to "/about" "Go to about page"]]])

(defn about-page []
  [:div [:h2 "About prdash"]
   [:div [nav/link-to "/" "Go to home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (nav/hook-browser-navigation!)
  (mount-root))
