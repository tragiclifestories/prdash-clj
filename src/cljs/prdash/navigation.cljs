(ns prdash.navigation
  (:require [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.Uri
           goog.history.Html5History))

(def history (Html5History.))

(defn change-route [history]
  (fn [e]
    (let [path (->> e
                    .-target
                    .-href
                    (.parse Uri)
                    .getPath
                    )
          title (.-title (.-target e))]
      (when (secretary/locate-route path)
        (. e preventDefault)
        (. history (setToken path title))))))

(defn link-to [path text]
  [:a {:href path :on-click (change-route history)} text])

(defn hook-browser-navigation! []
  (doto history
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setUseFragment false)
    (.setPathPrefix "")
    (.setEnabled true)))
