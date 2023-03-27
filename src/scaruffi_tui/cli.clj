(ns scaruffi-tui.cli
  (:require [clojure.edn :as edn]))

(defn check_input
  [row_length]
  (loop []
    (println "Enter your input:")
    (let [in (edn/read-string (read-line))]
      (cond (not (and (integer? in) (< in row_length))) (recur)
            :else in))))
