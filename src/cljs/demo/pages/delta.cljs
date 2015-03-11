(ns demo.pages.delta
  (:require [om.core :as om]
            [om.dom :as dom :include-macros true]
            
            [om-bootstrap.grid :as g]
            [om-bootstrap.input :as i]
            [om-bootstrap.random :as r]
            [om-bootstrap.button :as b]
            
            [commos.delta :as delta]

            [cljs.reader :as reader]

            [demo.highlight :as highlight]))

(defn inspect-deltas
  [deltas owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [code-input]}]
      (apply
       dom/div nil
       (concat
        (map-indexed
         (fn [i delta]
           (g/row
            {}
            (g/col {:xs 11}
              (highlight/code delta))
            (g/col {:xs 1}
              (b/button {:on-click
                         (fn [_]
                           (om/transact! deltas
                                         (fn [deltas]
                                           (vec (concat
                                                 (take i deltas)
                                                 (drop (inc i) deltas))))))}
                (r/glyphicon {:glyph "remove"})))))
         @deltas)
        [(dom/form #js{:onSubmit
                       (fn [e]
                         (.preventDefault e)
                         (when-let [v (try
                                        (reader/read-string code-input)
                                        (catch :default _
                                          nil))]
                           (om/transact! deltas #(conj % v))
                           (om/set-state! owner :code-input "")))}
           (i/input {:type "text"
                     :value code-input
                     :on-change (fn [e]
                                  (om/set-state! owner :code-input
                                                 (.. e -target -value)))}))])))))

(defn delta [cursor owner]
  (om/component
   (dom/div nil
     (om/build inspect-deltas (:deltas cursor))
     (dom/hr nil)
     (highlight/code (reduce delta/add nil (:deltas cursor))))))
