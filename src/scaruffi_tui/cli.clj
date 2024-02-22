(ns scaruffi-tui.cli
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [clj-commons.ansi :refer [compose]]
            [clojure.string :as str]))

(def ^:private ^:const MAX-LINE-LEN 120)

(defn format-long-text
  [text]
  (let [splitted (string/split text #" ")
        incremental-word-count (reductions (fn [previous-str-len str]
                                             (+ previous-str-len (count str) 1))
                                           0
                                           splitted)
        lines-partitions (partition-by
                          #(quot (second %) MAX-LINE-LEN)
                          (map vector splitted incremental-word-count))]
    (doseq [line lines-partitions]
      (println (string/join " " (map first line))))))

(defn check-input
  [row-length]
  (loop []
    (println "Enter your input:")
    (let [in (edn/read-string (read-line))]
      (cond (not (and (integer? in) (< in row-length))) (recur)
            :else in))))

(defn ask-continuation
  []
  (loop []
    (println "Do you wish to check the artist? [y/n]")
    (let [in (read-line)]
      (cond (not (or (= in "y") (= in "n"))) (recur)
            :else in))))

(def ^:private ^:const COLOR-TYPES
  {:a (fn [s] (compose [:blue.underlined s])),
   :i (fn [s] (compose [:green.italic s])),
   :b (fn [s] (compose [:yellow.bold s])),
   :band-name (fn [s] (compose [:blue.italic s])),
   :band-name-pad (fn [s]
                    (compose [{:font :blue.italic, :width 40, :pad :left} s])),
   :band-link (fn [s] (compose [{:font :italic, :width 50, :pad :right} s])),
   :h (fn [s] (compose [:red.bold s])),
   :option (fn [s]
             (let [[index option] (str/split s #"\." 2)]
               (str (compose [:cyan.bold (str index ". ")])
                    (compose [:bold option]))))})

(defn color-text
  [el tag]
  (let [content (string/trim (string/replace el #"\s" " "))]
    (cond (contains? COLOR-TYPES tag) ((tag COLOR-TYPES) content)
          (= :p tag) content
          :else "")))

(defn trim-paragraph
  [paragraph]
  (string/trim (string/trim-newline (string/join " " paragraph))))

(defn get-internal-text
  [paragraph]
  (let [content (:content paragraph)]
    (map #(if (and (:type %) (some? (:content %)))
            (color-text (first (:content %)) (:tag %))
            (color-text % (:tag paragraph)))
         (filter #(not (and (:type %) (nil? (:content %)))) content))))

(defn clear-console [] (print "\033\143"))

(defn get-own-text
  [el]
  (if (:content el)
    (-> el
        :content
        first)
    el))

(defn print-options
  [rows]
  (doseq [[i row] (map-indexed vector rows)]
    (println (color-text (str i "." (get-own-text row)) :option))))
