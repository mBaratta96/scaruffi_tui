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
  [el]
  (let [tag (:tag el)
        content (string/trim (first (:content el)))]
    (cond (= :a tag) ((:link COLOR-TYPES) content)
          (= :i tag) ((:song COLOR-TYPES) content)
          (= :b tag) ((:album COLOR-TYPES) content)
          :else "")))

(defn get-internal-text
  [el]
  (let [content (:content el)]
    (string/trim-newline (string/join
                          " "
                          (map #(if (and (:type %) (some? (:content %)))
                                  (color-text %)
                                  (string/trim (string/replace % #"\s" " ")))
                               (filter #(not (and (:type %) (nil? (:content %))))
                                       content))))))

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
  [name-links]
  (let [formatter (columns/format-columns
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
    (let [full-text (get-internal-text paragraph)
          name-link (get-links paragraph)]
      (println full-text)
      (print-links name-link)
      (print "\n")))
  (let [links (flatten (map get-links paragraphs))]
    (println (map :name links))))
