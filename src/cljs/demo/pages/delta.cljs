(ns demo.pages.delta
  (:require [om.core :as om]
            [om.dom :as dom :include-macros true]))

(defn delta [cursor owner]
  (om/component
   (dom/h1 nil "Test")))
