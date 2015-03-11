(ns demo.highlight
  (:require [om.core :as om]
            [om.dom :as dom :include-macros true])
  
  (:import [goog.net XhrIo]
           [goog Uri]))

(defn ppr [edn cont]
  (.send XhrIo
         "/pprint/"
         (fn [e]
           (cont (.getResponseText (.-target e))))
         "POST"
         (-> (Uri.QueryData.)
             (doto (.set "edn" edn))
             (.toString))))

(defn- highlight
  [owner]
  (.highlightBlock js/hljs (om/get-node owner "code-block")))

(defn- code* [code owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (ppr (str code) #(om/set-state! owner :indented-code %)))
    om/IDidMount
    (did-mount [_]
      (highlight owner))
    om/IDidUpdate
    (did-update [_ _ _]
      (highlight owner))
    om/IWillReceiveProps
    (will-receive-props [_ next-props]
      (ppr (str next-props) #(om/set-state! owner :indented-code %)))
    om/IRenderState
    (render-state [_ {:keys [indented-code]}]
      (dom/pre #js{:className "clojure"
                   :ref "code-block"}
        (dom/code #js{:style #js{:fontFamily "monospace"
                                 :fontSize "16px"}}
          indented-code)))))

(defn code [code]
  (om/build code* code))
