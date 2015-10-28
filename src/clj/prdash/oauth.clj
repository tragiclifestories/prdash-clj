(ns prdash.oauth
  (:require [environ.core :refer [env]]
            [clojure.data.json :as json]
            [clj-http.client :refer [post]]
            [ring.util.codec :refer [form-encode]]))

(defn github-auth-url [keys]
  (let [query-string (form-encode 
                        (into keys {:scope "repo,repo:status"
                                     :state (System/currentTimeMillis)}))]
    (str "https://github.com/login/oauth/authorize?" query-string)))

(defn get-auth-token [keys]
  (let [response (post "https://github.com/login/oauth/access_token" 
                       {:form-params keys :accept :json})]
   (->> response
        :body
        json/read-str)))
