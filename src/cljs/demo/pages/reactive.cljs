(ns demo.pages.reactive
  (:require [om.core :as om]
            [om.dom :as dom :include-macros true]
            
            [om-bootstrap.random :as r]
            [om-bootstrap.input :as i]
            [om-bootstrap.panel :as p]))

(defn- celsius->fahrenheit [c]
  (-> (/ 9 5)
      (* c)
      (+ 32)))

(defn- fahrenheit->celsius [f]
  (-> f
      (- 32)
      (* (/ 5 9))))

(defn- nan-safe [n]
  (if (js/isNaN n)
    0
    n))

(defn reactive [cursor owner]
  (om/component
   (dom/div nil
     (dom/form nil
       (i/input {:label "Celsius"
                 :type "text"
                 :value (:celsius cursor)
                 :on-change
                 (fn [e]
                   (om/update! cursor :celsius
                               (->> (.. e -target -value)
                                    (js/parseInt)
                                    nan-safe)))})
       (i/input {:label "Fahrenheit"
                 :type "text"
                 :value (->> (:celsius cursor)
                             (celsius->fahrenheit)
                             (.round js/Math))
                 :on-change
                 (fn [e]
                   (om/update! cursor :celsius
                               (->> (.. e -target -value)
                                    (js/parseInt)
                                    nan-safe
                                    fahrenheit->celsius
                                    (.round js/Math))))})))))
