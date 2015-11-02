(ns prdash.views
  (:require [reagent.core :refer [atom]]
            [prdash.data :refer [add-repo! open-prs]]))

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
                     (add-repo! @owner @repo))}
       "Add repo"]
      ]]))

(defn pr-list []
  [:div [:h2 "Open PRs"]
   [:ul (for [pr @open-prs]
          [:li (:number pr)])]])
(defn login []  
  [:div
   [:h1 
    [:a {:href "/login"} "Login"]]])
