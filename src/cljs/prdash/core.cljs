(ns prdash.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [cljs.core.match :refer-macros [match]]
              [goog.history.EventType :as EventType]
              [prdash.navigation :as nav]
              [cljs.core.async :refer [put!]]
              [prdash.data :as d])
    (:import goog.history.Html5History
             goog.Uri))

;; ------------------------
;; Session state

(def token (atom nil))

;; -------------------------
;;     Views
;;
;; --- Form ---

(defn text-input [name val & [display-name]]
  (let [placeholder (or display-name name)]
    [:div.form-group
     [:label.sr-only {:for ""}]
     [:input.form-control
      {:type "text"
       :placeholder placeholder
       :id name
       :name name
       :value @val
       :on-change #(reset! val (-> % .-target .-value))
       }]]))

(defn repo-form []
  (let [owner (atom "") repo (atom "")]
    [:div
     [:h2 "Add a new repo"]
     [:form.form-inline
      [text-input "owner" owner "Owner/organisation"]
      [text-input "repo" repo "Repository"]
      [:button.btn.btn-primary
       {:on-click  (fn [e]
                     (. e preventDefault)
                     (d/add-repo! @owner @repo))}
       "Add repo"]
      ]]))

;; --- Table ---



(defn home-page []
  [:div.container-fluid [:div.row
                         [:div.col-md-8.col-md-offset-2
                          [repo-form]
                          [:h2 "Open PRs"]
                          [:div [:p "List goes here"]
                           [:ul (for [pr @d/open-prs]
                                  [:li (:number pr)])]]]]])



(defn current-page []
  (if (nil? @token)
    [:div
     [:h1 
      [:a {:href "/login"} "Login"]]]
    [:div [(session/get :current-page)]]))

;; -------------------------
;; Routes
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
    (reagent/render [current-page] (.getElementById js/document "app"))))

(defn init! []
  (nav/hook-browser-navigation!)
  (mount-root))
