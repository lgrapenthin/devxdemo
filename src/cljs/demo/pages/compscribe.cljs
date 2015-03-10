(ns demo.pages.compscribe
  (:require [om.core :as om]
            [om.dom :as dom :include-macros true]))

(defn compscribe [cursor owner]
  (om/component
   (dom/h1 nil "Test")))
