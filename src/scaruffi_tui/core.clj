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
    (cli/print-options headers)
    (let [index (cli/check-input (count headers))
          section (nth sections index)
          header (nth headers index)]
      (cli/clear-console)
      (cli/print-header header)
      (doseq [parapgraph section]
        (println (cli/get-internal-text parapgraph) "\n")))))

(defn -main
  ([] (let [chapter-page (navigate-home)] (navigate-page chapter-page))))
