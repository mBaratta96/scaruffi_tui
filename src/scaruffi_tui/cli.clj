(ns scaruffi-tui.cli
  (:require [clojure.edn :as edn]))

(defn check_input
  [row_length]
  (loop [not_correct true]
    (when not_correct
      (println "Enter your input:")
      (let [user_input (edn/read-string (read-line))]
        (recur (not (and (integer? user_input) (< user_input row_length))))))))
