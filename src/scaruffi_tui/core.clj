(ns scaruffi-tui.core
  (:gen-class)
  (:require [scaruffi-tui.scraper :as scraper]
            [scaruffi-tui.cli :as cli]))

(defn navigate-home
  []
  (let [table (scraper/get-table)
        rows (scraper/get-section-headers table)
        sections (scraper/get-section-content rows)]
    (cli/print-options rows)
    (let [section (nth sections (cli/check-input (count rows)))]
      (cli/clear-console)
      (cli/print-options section)
      (let [chapter (nth section (cli/check-input (count section)))]
        (cli/clear-console)
        (scraper/get-link chapter)))))

(defn navigate-page
  [page]
  (let [table (scraper/get-table page)
        indexes (scraper/get-dir-indexes table)
        headers (cli/get-page-headers indexes)
        sections (cli/create-section indexes)]
    (doseq [[i paragraphs] (map-indexed vector sections)]
      (cli/print-header (nth headers i))
      (doseq [paragraph paragraphs]
        (println (cli/get-internal-text paragraph) "\n")))))

(defn -main
  ([] (let [chapter-page (navigate-home)] (navigate-page chapter-page))))
