(ns scaruffi-tui.artist
  (:require [clojure.string :as string]
            [hickory.select :as s]
            [scaruffi-tui.cli :as cli]
            [scaruffi-tui.scraper :as scraper]
            [clojure.edn :as edn]))

(defn- get-artist-table
  [parsed-page]
  (s/select
   (s/descendant
    (s/and (s/tag :table)
           (s/attr :width #(or (= % "100%") (> (edn/read-string %) 610)))
           (s/attr :cellpadding #(not (= % "10"))))
    (s/and (s/tag :td)
           (s/not (s/has-descendant
                   (s/and (s/tag :a)
                          (s/attr :href
                                  #(string/includes? % "translate")))))))
   parsed-page))

(defn- get-artist-rating-table
  [parsed-page]
  (first (s/select (s/descendant (s/and (s/tag :table)
                                        (s/attr :width #(not (= % "100%"))))
                                 (s/and (s/tag :td) s/first-child)
                                 (s/tag :font))
                   parsed-page)))

(defn get-artist-page-content
  [artist-link]
  (let [parsed-page (scraper/parse-page artist-link)]
    [(get-artist-table parsed-page) (get-artist-rating-table parsed-page)]))

(defn print-artist
  [artist-tables]
  (doseq [table artist-tables]
    (let [artist-section (:content table)
          text (map #(if (:type %)
                       (cli/trim-paragraph (cli/get-internal-text %))
                       (string/trim (string/replace % #"\s" " ")))
                    artist-section)]
      (print (cli/trim-paragraph text))
      (print "\n"))
    (print "\n")))

(defn print-ratings
  [rating-table]
  (let [ratings (filter #(or (:type %)
                             (> (count (string/trim (string/trim-newline %)))
                                0))
                        rating-table)
        formatted (map #(if (:type %)
                          (cli/trim-paragraph (cli/get-internal-text %))
                          (str (string/trim (string/replace % #"\s" " ")) "\n"))
                       ratings)]
    (doseq [rating formatted] (print rating))
    (println "\n")))
