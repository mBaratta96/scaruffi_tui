(ns scaruffi-tui.cli
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [io.aviso.columns :as columns]))

(defn check-input
  [row-length]
  (loop []
    (println "Enter your input:")
    (let [in (edn/read-string (read-line))]
      (cond (not (and (integer? in) (< in row-length))) (recur)
            :else in))))

(defprotocol Page-Headers
  (get-page-headers [this]))

(defrecord Page [page]
  Page-Headers
  (get-page-headers [this]
    (filter #(or (= :h4 (:tag %)) (= :i (:tag %))) (:page this))))

(defrecord Indexed-Page [page]
  Page-Headers
  (get-page-headers [this]
    (filter #(or (= :h4 (:tag (second %))) (= :i (:tag (second %))))
            (:page this))))

(defn get-upper-bound [bounds el] (last (filter #(< % el) bounds)))

(defn create-section
  [page]
  (let [indexed-page (map-indexed vector page)
        bounds (map #(first %) (get-page-headers (Indexed-Page. indexed-page)))
        paragraphs (filter #(and (= :p (:tag (second %)))
                                 (some? (:content (second %)))
                                 (> (count (:content (second %))) 1))
                           indexed-page)]
    (partition-by #(get-upper-bound bounds (first %)) paragraphs)))

(defn get-own-text
  [el]
  (-> el
      :content
      first
      string/trim))

(def ANSI-CODES
  {:reset "[0m",
   :blue "[34m",
   :red "[31m",
   :green "[32m",
   :yellow "[33m",
   :cyan "[36m",
   :underline "[4m",
   :bold "[1m",
   :italic "[3m"})
(defn ansi [code] (str \u001b (get ANSI-CODES code (:reset ANSI-CODES))))
(defn style [s & codes] (str (apply str (map ansi codes)) s (ansi :reset)))
(defprotocol Color
  (colorize [this]))
(defrecord Link [s]
  Color
  (colorize [this] (style (:s this) :blue :underline)))
(defrecord Song [s]
  Color
  (colorize [this] (style (:s this) :green :italic)))
(defrecord Album [s]
  Color
  (colorize [this] (style (:s this) :yellow :bold)))
(defrecord BandName [s]
  Color
  (colorize [this] (style (:s this) :blue :italic)))
(defrecord Header [s]
  Color
  (colorize [this] (style (:s this) :red :bold)))
(defrecord Option [index s]
  Color
  (colorize [this]
    (str (style (str (:index this) ". ") :cyan :bold)
         (style (:s this) :bold))))

(defn get-internal-text
  [el]
  (let [content (:content (second el))]
    (string/trim-newline
     (string/join " "
                  (map #(if (:type %)
                          (if (some? (:content %))
                            (let [tag (:tag %)
                                  content (string/trim (first (:content %)))]
                              (cond (= :a tag) (colorize (Link. content))
                                    (= :i tag) (colorize (Song. content))
                                    (= :b tag) (colorize (Album. content))
                                    :else "")))
                          (string/trim (string/replace % #"\s" " ")))
                       content)))))

(def base-url "https://scaruffi.com")

(defn get-links
  [el]
  (let [content (:content (second el))
        links (filter #(and (:type %) (some? (:content %)) (= :a (:tag %)))
                      content)
        name-links (for [c links]
                     {:name (colorize (BandName. (first (:content c)))),
                      :link (str base-url (subs (:href (:attrs c)) 2))})
        formatter (columns/format-columns
                   [:right (columns/max-value-length name-links :name)]
                   ": "
                   [:left (columns/max-value-length name-links :link)])]
    (columns/write-rows formatter [:name :link] name-links)))

(defn clear-console [] (print "\033\143"))

(defn print-header
  [header]
  (let [header-string (string/upper-case (get-own-text header))]
    (println (colorize (Header. header-string)) "\n")))

(defn print-options
  [rows]
  (doseq [[i row] (map-indexed vector rows)]
    (println (colorize (Option. i (get-own-text row))))))

(defn print-paragraphs
  [paragraphs]
  (doseq [parapgraph paragraphs]
    (let [full-text (get-internal-text parapgraph)]
      (println full-text)
      (get-links parapgraph)
      (print "\n"))))
