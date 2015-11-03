(ns prdash.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.response :refer [redirect]]
            [environ.core :refer [env]]
            [camel-snake-kebab.core :refer [->snake_case ->kebab-case-keyword]]
            [prdash.oauth :as o]))

(def auth-endpoint (str (:base-url env) "/authorize"))

(def my-defaults (assoc site-defaults
                       :params
                       {:keywordize true
                        :urlencoded true}))

(defn filter-env [& keys]
  (select-keys env keys))

(defn key-transform [data xform]
  (into {}  
        (map (fn [[key val]]
               [(xform key)
                val])
             data)))

(defn snakify [query-map]
  (key-transform query-map ->snake_case))

(defn kebabify [data]
  (key-transform data ->kebab-case-keyword))

(defn ghu [] 
  (o/github-auth-url 
   (snakify (into (filter-env :client-id) {:redirect-uri auth-endpoint}))))

(def mount-target
    [:div#app
      [:h3 "Loading ... "]
      ])

(def home-page
  (html
   [:html
    [:head
     [:title "Open PR wall of shame"]
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (include-css "/css/site.css")]
    [:body
     mount-target
     (include-js "https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.10.6/moment.min.js")
     (include-js "/js/app.js")]]))

(def login-route
  (redirect (ghu)))

(defn auth-route [{code :code state :state}]
  (println code state)
  (let [keys
        (snakify
         (into
          (filter-env :client-id :client-secret :base-uri)
          {:code code :state state}))
        token-data (kebabify (o/get-auth-token keys))]
    (println token-data)
    (redirect (str "/token/" (:access-token token-data)))))

(defroutes routes
  (GET "/login" [] login-route)
  (GET "/authorize*" {params :query-params}
       (auth-route (kebabify params)))
  (resources "/")
  (GET "*" [] home-page)
  (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults #'routes my-defaults)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))

(macroexpand-1  (GET "/authorize*" {params :query-params}
       params))
