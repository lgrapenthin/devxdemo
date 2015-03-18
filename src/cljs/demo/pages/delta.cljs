(ns demo.pages.delta
  (:require [om.core :as om]
            [om.dom :as dom :include-macros true]
            
            [om-bootstrap.grid :as g]
            [om-bootstrap.input :as i]
            [om-bootstrap.random :as r]
            [om-bootstrap.button :as b]
            
            [commos.delta :as delta]

            [demo.helpers :refer [safe-read]]
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
                         (when-let [v (safe-read code-input)]
                           (om/transact! deltas #(conj % v))
                           (om/set-state! owner :code-input "")))}
           (g/row
            {}
            (g/col {:xs 1}
              (dom/h5 nil "+"))
            (g/col {:xs 11}
              (i/input {:type "text"
                        :value code-input
                        :on-change
                        (fn [e]
                          (om/set-state! owner :code-input
                                         (.. e -target -value)))}))))])))))

(defn basic-add [cursor owner]
  (om/component
   (dom/div nil
     (dom/h4 nil (dom/code nil "add"))
     (dom/i nil "experiment with adding and removing of different deltas here")
     (om/build inspect-deltas (:deltas cursor))
     (g/row
      {}
      (g/col {:xs 1}
        (dom/h5 nil "="))
      (g/col {:xs 11}
        (highlight/code (reduce delta/add nil @(:deltas cursor))))))))

(defn unpack-op [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:code-input (pr-str [:on {:orders [4020]
                                         :friends
                                         {5000 {:orders #{3010 3012}
                                                :middle-name "Van"}}}])})
    om/IRenderState
    (render-state [_ {:keys [code-input]}]
      (apply
       dom/div nil
       (dom/h4 nil (dom/code nil "unpack"))
       (dom/i nil "via unpack, you always get a sequence of exclusively :is, :in or :ex deltas, while :batch, :on and :off deltas are unpacked.")
       (i/input {:type "text"
                 :value code-input
                 :on-change
                 (fn [e]
                   (om/set-state! owner :code-input
                                  (.. e -target -value)))})
       (for [delta (-> (safe-read code-input) delta/unpack)]
         (highlight/code delta))))))

(defn diagnostic-op [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:code-input (pr-str [:on {:orders [4020]
                                 :friends
                                 {5000 {:orders #{3010 3012}
                                        :middle-name "Van"}}}])})
    om/IRenderState
    (render-state [_ {:keys [code-input]}]
      (apply
       dom/div nil
       (dom/h4 nil (dom/code nil "diagnostic-delta"))
       (dom/i nil "after using unpack, conforming deltas via diagnostic-delta makes it easier to write validation code (this example applies unpack on your input before mapping diagnostic-delta)")
       (i/input {:type "text"
                 :value code-input
                 :on-change
                 (fn [e]
                   (om/set-state! owner :code-input
                                  (.. e -target -value)))})
       (for [delta (->> (safe-read code-input)
                        (delta/unpack)
                        (map delta/diagnostic-delta))]
         (highlight/code delta))))))

(defn delta [cursor owner]
  (om/component
   (dom/div nil
     (om/build basic-add cursor)
     (dom/hr nil)
     (om/build unpack-op cursor)
     (dom/hr nil)
     (om/build diagnostic-op cursor))))
