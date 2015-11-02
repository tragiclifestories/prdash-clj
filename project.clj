(defproject prdash "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [ring-server "0.4.0"]
                 [camel-snake-kebab "0.3.2"]
                 [clj-http "2.0.0"]
                 [org.clojure/data.json "0.2.6"]
                 [reagent "0.5.1"]
                 [reagent-forms "0.5.12"]
                 [reagent-utils "0.1.5"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [prone "0.8.2"]
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]
                 [environ "1.0.1"]
                 [cljs-http "0.1.37"]
                 [com.andrewmcveigh/cljs-time "0.3.14"]
                 [org.clojure/clojurescript "1.7.145" :scope "provided"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/core.async "0.2.371"]
                 [secretary "1.2.3"]
                 ]

  :plugins [[lein-environ "1.0.1"]
            [lein-asset-minifier "0.2.2"]]

  :ring {:handler prdash.handler/app
         :uberwar-name "prdash.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "prdash.jar"

  :main prdash.server

  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]

  :minify-assets
  {:assets
    {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:app {:source-paths ["src/cljs" "src/cljc"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :asset-path   "/js/out"
                                        :optimizations :none
                                        :source-map true
                                        :pretty-print  true}}}}

  :profiles {:dev {:repl-options {:init-ns prdash.repl}

                   :dependencies [[ring/ring-mock "0.3.0"]
                                  [ring/ring-devel "1.4.0"]
                                  [lein-figwheel "0.4.1"]
                                  [org.clojure/tools.nrepl "0.2.11"]
                                  [com.cemerick/piggieback "0.1.5"]
                                  [pjstadig/humane-test-output "0.7.0"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.4.1"]
                             [lein-cljsbuild "1.1.0"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :nrepl-port 7002
                              :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"
                                                 "refactor-nrepl.middleware/wrap-refactor"
                                                 ]
                              :css-dirs ["resources/public/css"]
                              :ring-handler prdash.handler/app}

                   :env {:dev true}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {:main "prdash.dev"
                                                         :source-map true}}
                                        }
                               }}

             :uberjar {:hooks [leiningen.cljsbuild minify-assets.plugin/hooks]
                       :env {:production true}
                       :aot :all
                       :omit-source true
                       :cljsbuild {:jar true
                                   :builds {:app
                                             {:source-paths ["env/prod/cljs"]
                                              :compiler
                                              {:optimizations :advanced
                                               :pretty-print false}}}}}})
