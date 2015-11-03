(ns prdash.data
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs-http.client :as http]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [cljs.core.async :refer [<! chan put!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

;; Helper functions
;; ----------------

;; It's much nicer using keywords for map keys than strings.
;; These functions transform snake_case string keys into
;; kebab-kase keywords. Less Github API, more Clojurian.
(defn key-transform [data xform]
  (into {}  
        (map (fn [[key val]]
               (if (map? val)
                 [(xform key) (key-transform val xform)]
                 [(xform key) val]))
             data)))

(defn kebabify [in-map]
  (key-transform in-map ->kebab-case-keyword))


;; State and constants
;; -------------------

(def api-base "https://api.github.com")

;; Initialise the state of the app. An atom is a mutable data structure
;; that can be updated atomically. We are storing the PRs as a set
;; to ensure uniqueness: sorting and suchlike can be done at point of use.
;; We're using Reagent's implementation of atom, which has some handy extras
;; for client rendering.
(def open-prs (atom #{}))

;; Clojure's core.async library uses communicating sequential processes (CSP),
;; like Go. CSP programs work essentially by use of asynchronous queues (channels).
;; Here, we create a channel, and map over all inputs to create a url out of two
;; string inputs.
(def repo-chan (chan 10 (map
                         (fn [[owner repo]] ;; map callbacks expect one argument,
                                            ;; so we destructure an input vector
                           [owner repo (str api-base "/repos/" owner "/" repo "/pulls")]))))

;; Puts something on the above channel. The view code only knows about this function;
;; it otherwise calls nothing in the data layer. Such decoupling! 
(defn add-repo! [owner repo]
  (when-not (or (empty? owner) (empty? repo))
    (put! repo-chan [owner repo])))

;; A record is basically a hash-map with a schema.
(defrecord Repo [owner name])

(defrecord PR
    [id
     number
     title
     repo
     url
     opened
     updated])

;; When you define a record, you get two factory functions ->(record) and map->record.
;; Here, we use the second to define our own one, which can destructure a Github API
;; response and create a PR record.
(defn raw-map->PR [owner repo-name]
  (fn [response]
    (let [repo (->Repo owner repo-name)
          number (:number response)]
      (map->PR {:id (str (:owner repo) (:name repo) number)
                :number number
                :title (:title response)
                :repo repo
                :url (:html-url response)
                :opened (:created-at response)
                :updated (:updated-at response)}))))

;; Called without a collection as the last argument, map, filter and friends return
;; a 'transducer'. The basic idea is take the general thing all these things do - 
;; apply a function in turn to a series of values - and detach it from different sorts
;; of series of values (this will operate on a vector, for instance, but we are passing
;; another transducer to the repo-chan CSP channel above).
;;
;; As a neat side effect, transducers are composable in a way that obviates the need
;; for intermediate results - we process each value fully in turn.
(defn response->PRs [owner repo]
  (comp
   (map kebabify)
   (map (raw-map->PR owner repo))))

;; Common or garden Ajax call - except it returns a CSP channel as above.
(defn get-prs [url token]
  (http/get url
            {:with-credentials? false
             :oauth-token token
             :query-params {"state" "open"
                            "sort" "created"
                            "direction" "asc"}}))

;; Now for the fun part.
;;
;; The point of CSP is that processes are sequential. We can write code as
;; if it was synchronous, and execute it asynchronously. The go macro (cribbed,
;; obviously, from Go) accepts a standard bit of Clojure code, and 'blocks' while
;; waiting for a value to come out of the channel. In this case, we are waiting
;; on a response from Github. When we get it, we create PR records and append them
;; to the set. The go macro rewrites this to an ugly mass of callbacks for us.
(defn append-prs! [owner repo xhr-chan]
  (go
    (let [{raw-input :body status :status} (<! xhr-chan)]
      (case status
        200 (swap! open-prs #(into %1 (response->PRs owner repo) %2) raw-input)
        nil))))

;; go-loop is a sugar macro that gets rewritten to (go (loop ... )).
;; Same principle applies - wait for something to get put on the channel, and run
;; some code (in this case, make the Github request). This time around, we then
;; loop round with (recur) and wait some more.
(defn listen! [token]
  (go-loop []
    (let [[owner repo url] (<! repo-chan)
          xhr-chan (get-prs url @token)]
      (append-prs! owner repo xhr-chan)
      (recur))))
