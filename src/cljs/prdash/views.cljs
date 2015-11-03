(ns prdash.views
  (:require [reagent.core :refer [atom]]
            [prdash.date :refer [from-now]]
            [cljs.core.async :refer [put!]]
            [clojure.string :as string]))

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

(defn repo-form [c]
  (let [owner (atom "") repo (atom "")]
    [:div
     [:h2 "Add a new repo"]
     [:form.form-inline
      [text-input "owner" owner "Owner/organisation"]
      [text-input "repo" repo "Repository"]
      [:button.btn.btn-primary
       {:on-click  (fn [e]
                     (. e preventDefault)
                     (put! c [@owner @repo]))}
       "Add repo"]]]))
 
(defn login []  
  [:div
   [:h1 
    [:a {:href "/login"} "Login"]]])

(defn pr-link [{:keys [repo url number]}]
  (let [path (string/join "/"
                          [(:owner repo)
                           (:name repo)
                           number])]
    [:a {:href url} path]))

(defn one-pr [pr]
  [:tr
   [:td [pr-link pr]]
   [:td (:title pr)]
   [:td (from-now (:opened pr))]
   [:td (from-now (:updated pr))]])

(defn pr-list [open-prs]
  (when-not (empty? @open-prs)
    [:div [:h2 "Open PRs"]
     [:table.table
      [:thead
       [:tr
        [:td "Link"]
        [:td "Title"]
        [:td "Opened"]
        [:td "Updated"]]]
      [:tbody
       (for [pr (sort-by :opened @open-prs)]
         ^{:key (:id pr)} [one-pr pr])]]]))
