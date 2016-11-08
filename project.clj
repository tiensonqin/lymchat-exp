(defproject lymchat "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha10"]
                 [org.clojure/clojurescript "1.9.293"]
                 [org.clojure/core.async "0.2.385"]
                 [com.taoensso/sente "1.10.0-SNAPSHOT"]
                 [core-async-storage "0.1.1"]
                 [com.taoensso/encore      "2.64.1"]
                 [org.clojure/tools.reader "0.10.0"]
                 [com.taoensso/timbre      "4.6.0"]
                 [reagent "0.6.0-SNAPSHOT" :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server]]
                 [re-frame "0.7.0"]
                 [prismatic/schema "1.0.4"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]]
  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.4-7"]]
  :clean-targets ["target/" "main.js"]
  :aliases {"figwheel" ["run" "-m" "user" "--figwheel"]
            "externs" ["do" "clean"
                       ["run" "-m" "externs"]]
            "prod-build" ^{:doc "Recompile code with prod profile."}
            ["do" "clean"
             ["with-profile" "prod" "cljsbuild" "once" "main"]]}
  :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.4-7"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :source-paths ["src" "env/dev"]
                   :cljsbuild    {:builds [{:id "main"
                                            :source-paths ["src" "env/dev"]
                                            :figwheel     true
                                            :compiler     {:output-to     "target/not-used.js"
                                                           :main          "env.main"
                                                           :output-dir    "target"
                                                           :optimizations :none
                                                           :source-map    true}}]}
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}
             :prod {:cljsbuild {:builds [{:id "main"
                                          :source-paths ["src" "env/prod"]
                                          :compiler     {:output-to     "main.js"
                                                         :main          "env.main"
                                                         :output-dir    "target"
                                                         :static-fns    true
                                                         :externs       ["js/externs.js"]
                                                         :parallel-build     true
                                                         :optimize-constants true
                                                         :optimizations :advanced
                                                         :closure-defines {"goog.DEBUG" false}}}]}}})
