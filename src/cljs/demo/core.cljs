(ns demo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require [om.core :as om]
            [om.dom :as dom :include-macros true]
            
            [cljs.core.async :refer [chan pub take! alts! close! put!]]

            [om-bootstrap.nav :as n]
            [om-bootstrap.random :as r]
            [om-bootstrap.button :as b]
            [om-bootstrap.panel :as p]
            [om-bootstrap.grid :as g]

            [cljs.core.async :refer [chan <! >! put! timeout take!]]

            [demo.pages.reactive :refer [reactive]]
            [demo.pages.delta :refer [delta]]
            [demo.pages.compscribe :refer [compscribe]]
            [demo.highlight :as highlight]
            [demo.helpers :as helpers]
            
            [weasel.repl :as ws-repl]
            [figwheel.client :as fw :include-macros true]))

(def pages {:reactive-ui {:title "Reaktive UI"
                          :init {:celsius 0}}
            :commos.delta
            {:title "commos.delta"
             :url "https://github.com/commos/delta"
             :documentation "On crossclj.info"
             :documentation-link "http://crossclj.info/doc/org.commos/delta/latest/commos.delta.html"
             :init {:deltas
                    [[:is :first-name "Leon"]
                     [:is :last-name "Grapenthin"]
                     [:in :orders [4002 4003 4004]]
                     [:ex :orders 4002]
                     [:is [:friends 5000]
                      {:first-name "Ludwig"
                       :last-name "Beethoven"}]
                     [:on {:orders [4020]
                           :friends
                           {5000 {:orders #{3010 3012}
                                  :middle-name "Van"}}}]
                     [:off {:orders [4003]
                            :friends {5000 {:orders [3010]}}}]]}}
            :commos.delta.compscribe
            {:title "commos.delta.compscribe"
             :url "https://github.com/commos/delta.compscribe"
             :documentation "On crossclj.info"
             :documentation-link "http://crossclj.info/doc/org.commos/delta.compscribe/0.1.5/commos.delta.compscribe.html#_compscribe"}})

(def default-page :reactive-ui)

(defn- select-page [state k]
  (-> state
      (update-in [:pages k]
                 (fnil identity (get-in pages [k :init] {})))
      (assoc :page k)))

(defn root
  [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_])
    om/IWillUnmount
    (will-unmount [_])
    om/IRenderState
    (render-state [_ _]
      (let [page (:page cursor default-page)
            
            {:keys [title
                    url
                    documentation
                    documentation-link]}
            (get pages page)]
        (g/grid nil
                (dom/div nil
                  (if (= page :reactive-ui)
                    (highlight/code (pr-str cursor)))
                  (n/nav {:bs-style "pills"
                          :active-key page
                          :on-select (fn [k _]
                                       (om/transact! cursor #(select-page % k)))}
                    (n/nav-item {:key :reactive-ui}
                                "Reaktive UI")
                    (n/nav-item {:key :commos.delta}
                                "commos.delta")
                    (n/nav-item {:key :commos.delta.compscribe}
                                "commos.delta.compscribe"))
                  (r/page-header
                    {}
                    (cond->> title
                      url (dom/a #js{:href url
                                     :target "_blank"})))
                  (if documentation
                    (dom/div nil "Documentation: "
                      (dom/a #js{:href documentation-link
                                 :target "_blank"}
                        (dom/i nil documentation))))
                  (dom/hr nil)
                  (om/build (case page
                              :reactive-ui
                              reactive
                              :commos.delta
                              delta
                              :commos.delta.compscribe
                              compscribe)
                            (get-in cursor [:pages page]))))))))

(defn main
  [app-state out]
  (let [global (chan)]
    (om/root root app-state
             {:target (.getElementById js/document "om")
              :shared {:out out
                       :global global
                       :app-state app-state}})))

(defonce loaded?
  (do ;; figwheel safe once
    (enable-console-print!)

    (def app-state (atom (select-page {} default-page)))

    (def out (chan))

    (fw/watch-and-reload
     :websocket-url "ws://localhost:3449/figwheel-ws"
     :jsload-callback (fn []
                        (close! out)
                        (set! out (chan)) ;; per reload we need a new chan.
                        (main app-state out)))

    (try
      (ws-repl/connect "ws://localhost:9001" :verbose true)
      (catch :default e
        nil))
    
    (main app-state out)
    
    true))
