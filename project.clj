(defproject demo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.stuartsierra/component "0.2.2"]
                 [http-kit "2.1.19"]
                 [hiccup "1.0.5"]
                 [ring "1.3.2"]
                 [ring/ring-codec "1.0.0"]
                 [org.commos/delta "0.2.2"]
                 [org.commos/delta.compscribe "0.1.5"]
                 
                 [org.clojure/clojurescript "0.0-3058"]
                 [org.omcljs/om "0.8.8"]
                 [racehub/om-bootstrap "0.4.2"]]
  :source-paths ["src/clj" "src/cljs"]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.9"]
                                  [com.cemerick/piggieback "0.1.5"]
                                  [weasel "0.6.0-SNAPSHOT"]
                                  [figwheel "0.2.3-SNAPSHOT"]]
                   :plugins [[lein-cljsbuild "1.0.4"]
                             [lein-figwheel "0.2.5-SNAPSHOT"]
                             [com.keminglabs/cljx "0.5.0"]]
                   :repl-options {:nrepl-middleware
                                  [cemerick.piggieback/wrap-cljs-repl]}
                   :source-paths ["src/dev/clj"
                                  ;; cljs:
                                  "src/cljs"
                                  "src/dev/cljs"]
                   :cljsbuild
                   {:builds
                    [{:id "dev"
                      :source-paths ["src/cljs"]
                      :compiler {:main demo.core
                                 :output-to "resources/public/js/main.js"
                                 :output-dir "resources/public/js/out"
                                 :asset-path "/js/out"
                                 :optimizations :none
                                 :source-map true}}]}
                   :clean-targets ^{:protect false}
                   ["resources/public/js/main.js"
                    "resources/public/js/out"]}})
