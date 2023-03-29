(ns scaruffi-tui.cli
  (:require [clojure.edn :as edn]))

(defn check-input
  [row-length]
  (loop []
    (println "Enter your input:")
    (let [in (edn/read-string (read-line))]
      (cond (not (and (integer? in) (< in row-length))) (recur)
            :else in))))

(defn get-upper-bound [bounds el] (last (filter #(< % el) bounds)))

(defn create-section
  [page]
  (let [bounds (map #(.indexOf page %)
                    (filter #(or (= :h4 (:tag %)) (= :i (:tag %))) page))]
    (partition-by #(get-upper-bound bounds (.indexOf page %))
                  (filter #(= :p (:tag %)) page))))
