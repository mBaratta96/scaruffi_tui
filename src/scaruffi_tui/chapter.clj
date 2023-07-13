(ns scaruffi-tui.chapter
  (:require [clojure.string :as string]
            [hickory.select :as s]
            [scaruffi-tui.scraper :as scraper]
            [scaruffi-tui.cli :as cli]
            [io.aviso.columns :as columns]))

(def ^:private ^:const SCARUFFI-URL "https://scaruffi.com/history/")

(def ^:private ^:const BASE-URL "https://scaruffi.com")

(def ^:private is-header?
  (fn [page-section]
    (or (= :h4 (:tag page-section)) (= :i (:tag page-section)))))

(def ^:private is-paragraph?
  (fn [page-section]
    (and (= :p (:tag page-section))
         (some? (:content page-section))
         (> (count (:content page-section)) 1))))

(defn filter-headers [content] (filter is-header? content))

(defn- get-upper-bound [bounds el] (last (filter #(< % el) bounds)))

(defn create-section
  [page]
  (let [indexed-page (map-indexed vector page)
        bound-indexes (map first (filter #(is-header? (second %)) indexed-page))
        paragraphs (filter #(is-paragraph? (second %)) indexed-page)]
    (partition-by #(get-upper-bound bound-indexes (first %)) paragraphs)))

(defn get-headers-content
  [table]
  (s/select (s/or (s/descendant (s/tag :h4) (s/tag :i))
                  (s/descendant (s/and (s/tag :h4)
                                       (s/not (s/has-child (s/tag :i)))))
                  (s/descendant (s/tag :p)))
            table))

(defn- get-page-content
  [table]
  (-> (s/select (s/descendant (s/and (s/tag :dir)
                                     (s/not (s/has-child (s/tag :dir)))
                                     (s/not (s/has-child (s/tag :ol)))))
                table)
      first
      get-headers-content))

(defn get-chapter
  [page]
  (-> SCARUFFI-URL
      (str page)
      scraper/parse-page
      scraper/get-table
      get-page-content))

(defn print-header
  [header]
  (let [header-string (-> header
                          :content
                          first
                          string/trim
                          string/upper-case)]
    (println (cli/color-text header-string :h))
    "\n"))

(defn- get-links
  [el]
  (let [content (:content el)
        links (filter #(and (:type %) (some? (:content %)) (= :a (:tag %)))
                      content)]
    (for [content links]
      {:name (cli/color-text (first (:content content)) :band-name),
       :link (str BASE-URL (subs (:href (:attrs content)) 2))})))

(defn- print-links
  [paragraph]
  (let [name-links (get-links paragraph)
        formatter (columns/format-columns
                   [:right (columns/max-value-length name-links :name)]
                   ": "
                   [:left (columns/max-value-length name-links :link)])]
    (columns/write-rows formatter [:name :link] name-links)))

(defn print-paragraphs
  [paragraphs]
  (doseq [paragraph paragraphs]
    (-> paragraph
        cli/get-internal-text
        cli/trim-paragraph
        cli/format-long-text)
    (print-links paragraph)
    (print "\n"))
  (flatten (map get-links paragraphs)))
