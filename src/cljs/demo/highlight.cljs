(ns demo.highlight
  (:require [om.core :as om]
            [om.dom :as dom :include-macros true]))

(defn- highlight
  [owner]
  (.highlightBlock js/hljs (om/get-node owner "code-block")))

(defn- code* [code owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (highlight owner)
      (om/set-state! owner :mounted? true))
    om/IDidUpdate
    (did-update [_ _ _]
      (highlight owner))
    om/IRender
    (render [_]
      (dom/pre #js{:className "clojure"
                   :ref "code-block"}
        (dom/code #js{:style #js{:fontFamily "monospace"}}
          code)))))

(defn code [code]
  (om/build code* code))
