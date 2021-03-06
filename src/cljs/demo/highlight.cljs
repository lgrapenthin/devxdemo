(ns demo.highlight
  (:require [om.core :as om]
            [om.dom :as dom :include-macros true])
  
  (:import [goog.net XhrIo]
           [goog Uri]))

(def ^:private ppr-cache (atom nil))

(defn ppr [edn cont]
  (if-let [cached (get @ppr-cache edn)]
    (cont cached)
    (.send XhrIo
           "/pprint/"
           (fn [e]
             (let [r (.getResponseText (.-target e))]
               (cont r)
               (swap! ppr-cache assoc edn r)))
           "POST"
           (-> (Uri.QueryData.)
               (doto (.set "edn" edn))
               (.toString)))))

(defn- highlight
  [owner]
  (.highlightBlock js/hljs (om/get-node owner "code-block")))

(defn- update-code
  [owner code]
  (om/update-state! owner (fn [{:keys [indented-code]}]
                            {:indented-code code
                             :render? (not= indented-code code)})))

(defn- code* [code owner]
  (reify
    om/IInitState
    (init-state [_]
      {:render? true})
    om/IWillMount
    (will-mount [_]
      (ppr (str code) #(update-code owner %)))
    om/IDidMount
    (did-mount [_]
      (highlight owner))
    om/IWillReceiveProps
    (will-receive-props [_ next-code]
      (ppr (str next-code)
           #(update-code owner %)))
    om/IDidUpdate
    (did-update [_ _ {:keys [render?]}]
      (when render?
        (highlight owner)))
    om/IRenderState
    (render-state [_ {:keys [indented-code]}]
      (dom/pre #js{:className "clojure"
                   :ref "code-block"}
        (dom/code #js{:style #js{:fontFamily "monospace"
                                 :fontSize "16px"}}
          indented-code)))))

(defn code [code]
  (om/build code* code))
