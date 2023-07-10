(ns scaruffi-tui.core
  (:gen-class)
  (:require [scaruffi-tui.scraper :as scraper]
            [scaruffi-tui.cli :as cli]
            [scaruffi-tui.data :as data]))

(set! *warn-on-reflection* true)

(defn navigate-home
  []
  (let [rows (scraper/get-homepage-rows)
        sections (scraper/get-rows-content rows)]
    (cli/print-options rows)
    (let [section (nth sections (cli/check-input (count rows)))]
      (cli/clear-console)
      (cli/print-options section)
      (let [chapter (nth section (cli/check-input (count section)))]
        (cli/clear-console)
        (scraper/get-link chapter)))))

(defn navigate-chapter
  [page]
  (let [content (scraper/get-chapter page)
        headers (filter data/is-header? content)
        sections (data/create-section content)]
    (cli/print-options headers)
    (let [index (cli/check-input (count headers))
          section (nth sections index)
          header (nth headers index)]
      (cli/clear-console)
      (cli/print-header header)
      (cli/print-paragraphs (map second section)))))

(defn get-artists
  [artist-links]
  (cli/print-names artist-links)
  (let [index (cli/check-input (count artist-links))
        link (:link (nth artist-links index))
        tables (scraper/get-artist-page-content link)]
    (doseq [table tables]
      (cli/print-artist (:content table))
      (print "\n"))
    (println link)))

(defn navigate-artists
  [artist-links]
  (let [answer (cli/ask-continuation)]
    (if (= answer "y") (get-artists artist-links))))

(defn -main
  ([]
   (let [chapter-page (navigate-home)
         artist-links (navigate-chapter chapter-page)]
     (navigate-artists artist-links))))
