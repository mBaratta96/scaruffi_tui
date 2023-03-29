(ns scaruffi-tui.cli
  (:require [clojure.edn :as edn]
            [clojure.string :as string]))

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

(defn get-own-text
  [el]
  (-> el
      :content
      first
      string/trim))

(defn get-internal-text
  [el]
  (let [content (:content el)]
    (string/trim-newline
     (string/join " "
                  (map #(cond (:type %) (cond (some? (:content %))
                                              (string/upper-case
                                               (first (:content %)))
                                              :else "\n\n")
                              (= % "\n") "\n"
                              :else (string/trim (string/replace % "\n" " ")))
                       content)))))

(defn clear-console [] (print "\033\143"))
