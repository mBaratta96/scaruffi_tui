(ns scaruffi-tui.cli
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [io.aviso.columns :as columns]
            [io.aviso.ansi :refer [compose]]
            [scaruffi-tui.data :as data]))

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
  {:link (fn [s] (compose [:blue.underlined s])),
   :song (fn [s] (compose [:green.italic s])),
   :album (fn [s] (compose [:yellow.bold s])),
   :band-name (fn [s] (compose [:blue.italic s])),
   :header (fn [s] (compose [:red.bold s])),
   :option (fn [index s]
             (str (compose [:cyan.bold (str index ". ")])
                  (compose [:bold s])))})

(defn color-text
  [el tag]
  (let [content (string/trim (string/replace el #"\s" " "))]
    (cond (= :a tag) ((:link COLOR-TYPES) content)
          (= :i tag) ((:song COLOR-TYPES) content)
          (= :b tag) ((:album COLOR-TYPES) content)
          (= :p tag) content
          :else "")))

(defn trim-paragraph
  [paragraph]
  (string/trim-newline (string/join " " paragraph)))

(defn get-internal-text
  [paragraph]
  (let [content (:content paragraph)]
    ;(println content)
    (map #(if (and (:type %) (some? (:content %)))
            (color-text (first (:content %)) (:tag %))
            (color-text % (:tag paragraph)))
         (filter #(not (and (:type %) (nil? (:content %)))) content))))

(def ^:private ^:const BASE-URL "https://scaruffi.com")

(defn get-links
  [el]
  (let [content (:content el)
        links (filter #(and (:type %) (some? (:content %)) (= :a (:tag %)))
                      content)]
    (for [content links]
      {:name ((:band-name COLOR-TYPES) (first (:content content))),
       :link (str BASE-URL (subs (:href (:attrs content)) 2))})))

(defn print-links
  [paragraph]
  (let [name-links (get-links paragraph)
        formatter (columns/format-columns
                   [:right (columns/max-value-length name-links :name)]
                   ": "
                   [:left (columns/max-value-length name-links :link)])]
    (columns/write-rows formatter [:name :link] name-links)))

(defn clear-console [] (print "\033\143"))

(defn print-header
  [header]
  (let [header-string (-> header
                          data/get-own-text
                          string/trim
                          string/upper-case)]
    (println ((:header COLOR-TYPES) header-string))
    "\n"))

(defn print-options
  [rows]
  (doseq [[i row] (map-indexed vector rows)]
    (println ((:option COLOR-TYPES)
              i
              (-> row
                  data/get-own-text
                  string/trim)))))

(defn print-paragraphs
  [paragraphs]
  (doseq [paragraph paragraphs]
    (println (trim-paragraph (get-internal-text paragraph)))
    (print-links paragraph)
    (print "\n"))
  (flatten (map get-links paragraphs)))

(defn print-names
  [names-links]
  (doseq [[i name] (map-indexed vector (map :name names-links))]
    (println ((:option COLOR-TYPES) i name))))

(defn print-artist
  [artist-tables]
  (doseq [table artist-tables]
    (let [artist-section (:content table)
          text (map #(if (:type %)
                       (trim-paragraph (get-internal-text %))
                       (string/trim (string/replace % #"\s" " ")))
                    artist-section)]
      (print (trim-paragraph text))
      (print "\n"))
    (print "\n")))

(defn print-ratings
  [rating-table]
  (doseq [rating rating-table] (println "RATING" rating)))
