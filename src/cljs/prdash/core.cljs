(ns prdash.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType])
    (:import goog.history.Html5History
             goog.Uri))

;; -------------------------
;; Views

(def history (Html5History.))

(defn change-route [e]
    (let [path (->> e
                  .-target
                  .-href
                  (.parse Uri)
                  .getPath
                  )
        title (.-title (.-target e))]
    (when (secretary/locate-route path)
      (. e preventDefault)
      (. history (setToken path title)))))

(defn home-page []
  [:div [:h2 "Welcome to prdash"]
   [:div [:a {:href "/about" :on-click change-route} "go to about page"]]])

(defn about-page []
  [:div [:h2 "About prdash"]
   [:div [:a {:href "/" :on-click change-route} "go to the home page"]]])

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
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto history
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setUseFragment false)
    (.setPathPrefix "")
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
