(ns demo.helpers
  (:require [cljs.reader :as reader]))

(defn safe-read [code-input]
  (try
    (reader/read-string code-input)
    (catch :default _
      nil)))
