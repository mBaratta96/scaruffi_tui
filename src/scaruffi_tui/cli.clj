(ns scaruffi-tui.cli
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [io.aviso.columns :as columns]
            [scaruffi-tui.data :as data]))

(defn check-input
  [row-length]
  (loop []
    (println "Enter your input:")
    (let [in (edn/read-string (read-line))]
      (cond (not (and (integer? in) (< in row-length))) (recur)
            :else in))))

(def ^:private ^:const ANSI-CODES
  {:reset "[0m",
   :blue "[34m",
   :red "[31m",
   :green "[32m",
   :yellow "[33m",
   :cyan "[36m",
   :underline "[4m",
   :bold "[1m",
   :italic "[3m"})

(defn ^:private ansi
  [code]
  (str \u001b (get ANSI-CODES code (:reset ANSI-CODES))))
(defn ^:private style
  [s & codes]
  (str (apply str (map ansi codes)) s (ansi :reset)))

(def ^:private ^:const COLOR-TYPES
  {:link (fn [s] (style s :blue :underline)),
   :song (fn [s] (style s :green :italic)),
   :album (fn [s] (style s :yellow :bold)),
   :band-name (fn [s] (style s :blue :italic)),
   :header (fn [s] (style s :red :bold)),
   :option (fn [index s]
             (str (style (str index ". ") :cyan :bold) (style s :bold)))})

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
  (let [content (:content (second el))]
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
  (let [content (:content (second el))
        links (filter #(and (:type %) (some? (:content %)) (= :a (:tag %)))
                      content)
        name-links (for [c links]
                     {:name ((:band-name COLOR-TYPES) (first (:content c))),
                      :link (str BASE-URL (subs (:href (:attrs c)) 2))})
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
  (doseq [parapgraph paragraphs]
    (let [full-text (get-internal-text parapgraph)]
      (println full-text)
      (get-links parapgraph)
      (print "\n"))))
