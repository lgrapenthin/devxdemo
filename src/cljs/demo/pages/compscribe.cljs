(ns demo.pages.compscribe
  (:require [om.core :as om]
            [om.dom :as dom :include-macros true]

            [demo.highlight :as highlight]
            [demo.helpers :refer [safe-read]]

            [om-bootstrap.input :as i]
            [om-bootstrap.button :as b]
            [om-bootstrap.grid :as g]
            [om-bootstrap.panel :as p]
            
            [commos.delta :as delta]
            [commos.delta.compscribe :as compscribe]

            [cljs.core.async :refer [chan mult tap untap timeout
                                     take! alts! close! put! >! <!]]
            [cljs.core.async.impl.protocols :refer [Channel]])
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]))

(defn simulate-api
  "Helper to simulate different API endpoints via endpoints, a map
  endpoint->spec, where spec consists of

  :deltas - a map value->deltas.  A pseudo delta [:timeout ms] may be
  used to delay successive deltas.
  
  :unsubs-mode - either :nop (default) or :close.  Whether the
  channel should be closed when unsubs-fn is called.

  Deltas are put on the returned channels \"as is\".

  Returns [subs-fn unsubs-fn channels] where subs-state is an atom
  endpoint->value->channels, for inspection purposes."
  [endpoints]
  (let [subs (atom {})
        unsubscribable (atom #{})]
    [(fn [endpoint value]
       (let [ch (chan)]
         (swap! subs update-in [endpoint value] conj ch)
         (when (= :close (get-in endpoints [endpoint :unsubs-mode]))
           (swap! unsubscribable conj ch ))
         (go-loop [[x & xs] (get-in endpoints
                                    [endpoint :deltas value])]
           (if x
             (do
               (if (= :timeout (first x))
                 (<! (timeout (second x)))
                 (>! ch x))
               (recur xs))
             (close! ch)))
         ch))
     (fn [ch]
       (when (@unsubscribable ch)
         (close! ch)
         (swap! unsubscribable disj ch)))
     [subs unsubscribable]]))


(defn compscription
  [{:keys [subscribable],
    {:keys [spec id]} :subscription, :as cursor} owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (js/console.log "spec/id: " (pr-str [spec id]))
      (let [ch-in (chan)
            end-subs (compscribe/compscribe
                      ch-in
                      (first subscribable)
                      (second subscribable)
                      spec
                      id)]
        (go-loop [sum nil]
          (when-some [v (<! ch-in)]
            (let [sum (delta/add sum v)]
              (om/update-state! owner :deltas (fnil #(conj % v) []))
              (om/set-state! owner :streamed-val sum)
              (recur sum)))
          (om/set-state! owner :simulation-over? true))
        (om/set-state! owner :end-subs end-subs)))
    om/IWillUnmount
    (will-unmount [_]
      (when-let [end-subs (om/get-state owner :end-subs)]
        (end-subs)
        (om/update-state! owner #(dissoc % :end-subs))))
    om/IRenderState
    (render-state [_ {:keys [streamed-val simulation-over? deltas]}]
      (p/panel
       {:header (str "Simulation"
                     (if simulation-over?
                       " (beendet)"))}
       (dom/h4 nil
         "Received deltas:"
         (apply
          dom/div nil
          (for [delta deltas]
            (highlight/code delta))))
       (dom/h4 nil
         "Sum:"
         (highlight/code streamed-val))))))

(def demo-deltas
  {"/customers/" {:deltas {3
                           [[:is {:first-name "George"
                                  :email "george@foo.de"}]
                            [:timeout 5000]
                            [:is :email "george@bar.de"]]

                           4
                           [[:is {:first-name "Lisa"
                                  :email "lisa@foo.de"}]
                            [:timeout 5000]
                            [:in :orders 4004]
                            [:timeout 5000]
                            [:in :orders 4005]]

                           6
                           [[:is {:first-name "Gina"
                                  :email "gina@foo.de"}]]

                           7
                           [[:is {:first-name "Hans"
                                  :email "hans@foo.de"}]]}}
   "/orders/" {:deltas {4004 [[:is {:quantity 1
                                    :article 3010}]]
                        4005 [[:is {:quantity 3
                                    :article 3012}]]}}
   "/articles/" {:deltas {3010 [[:is {:title "Schminkst 4000"
                                   :description "Das Schminkset 4000 beinhaltet alles zum Schminken..."
                                   :article 3010}]
                             [:timeout 10000]
                             [:is :title "Schminkset 4000"]]
                       3012 [[:is {:title "Schminkspiegel Dolores"
                                   :description "Schminkspiegel Dolores"
                                   :article 3012}]]}}})

(defn compscribe [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      (let [ch-in (chan)]
        {:ch-in ch-in
         :subscribable
         (simulate-api demo-deltas)}))
    om/IRenderState
    (render-state [_ {:keys [subscribable input-val input-delta]}]
      (apply
       dom/div nil
       (dom/h4 nil "Simulated endpoints: ")
       (highlight/code demo-deltas)
       (concat
        [(dom/form #js{:onSubmit (fn [e]
                                    (.preventDefault e)
                                    (when-some [v (safe-read input-val)]
                                      (om/transact! cursor :subs
                                                    #(conj % v))
                                      #_(om/set-state! owner :input-val nil)))}
            (i/input {:label "Neue Compscription:"
                      :type "text"
                      :value input-val
                      :on-change #(om/set-state! owner :input-val
                                                 (.. % -target -value))}))
         (apply dom/div nil
           (dom/h5 nil "Beispiele:")
           (for [spec [{:spec ["/customers/"]
                        :id 3}

                       {:spec ["/customers/"]
                        :id 4}

                       {:spec ["/orders/"],
                        :id 4004}

                       {:spec ["/customers/" {:orders ["/orders/"]}],
                        :id 4}

                       {:spec ["/customers/" {:orders ["/orders/" {:article ["/articles/"]}]}],
                        :id 4}]]
             (g/row
              {:md 12}
              (g/col {:md 10}
                (highlight/code spec))
              (g/col {:md 2}
                (dom/a #js{:href "javascript:void(0);"
                           :onClick
                           (fn [_]
                             (om/transact! cursor :subs
                                           #(conj % spec))
                             (om/set-state! owner :input-val (pr-str spec)))}
                  "Ausprobieren"))))
           )]
        (mapcat
         (fn [sub]
           [(dom/hr nil)
            (highlight/code (om/value sub))
            (om/build compscription
                      {:subscribable subscribable
                       :subscription sub})
            (b/button {:on-click (fn [_]
                                   (om/transact! cursor :subs
                                                 #(->> %
                                                       (remove #{sub})
                                                       vec)))}
              "Entfernen")])
         (:subs cursor)))))))
