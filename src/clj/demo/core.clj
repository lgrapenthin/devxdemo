(ns demo.core
  (:require [org.httpkit.server :as server]
            [ring.util.response :as resp]
            [hiccup.page :as h]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]

            [weasel.repl.websocket :as weasel-ws]
            [cemerick.piggieback])
  (:use [clojure.repl]))

(defn cljs-repl []
  (cemerick.piggieback/cljs-repl
   :repl-env (weasel-ws/repl-env :ip "0.0.0.0"
                                 :port 9001)))

(defn- ui-handler [req]
  (->
   (h/html5
    [:head
     [:link {:href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css", :rel "stylesheet"}]]
    [:body
     [:div {:id "om"}]
     [:script {:src "/js/main.js"}]])
   (resp/response)
   (resp/content-type "text/html; charset=utf-8")))

(def server nil)

(defn start []
  (-> ui-handler
      (wrap-resource "/public")
      (wrap-file-info)
      (wrap-keyword-params)
      (wrap-params "UTF-8")
      (server/run-server {:port 8080})
      (constantly)
      (->> (alter-var-root #'server))))

(defn stop []
  (when server
    (server)
    (alter-var-root #'server (constantly nil))))

(defn reset []
  (stop)
  (start))
