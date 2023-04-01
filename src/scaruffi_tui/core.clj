(ns scaruffi-tui.core
  (:gen-class)
  (:require [scaruffi-tui.scraper :as scraper]
            [scaruffi-tui.cli :as cli])
  (:import [scaruffi_tui.cli Page]))

(set! *warn-on-reflection* true)
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
        headers (cli/get-page-headers (Page. indexes))
        sections (cli/create-section indexes)]
    (cli/print-options headers)
    (let [index (cli/check-input (count headers))
          section (nth sections index)
          header (nth headers index)]
      (cli/clear-console)
      (cli/print-header header)
      (doseq [parapgraph section]
        (let [links (cli/get-links parapgraph)
              full-text (cli/get-internal-text parapgraph)]
          (println full-text "\n" links "\n"))))))

(defn -main
  ([] (let [chapter-page (navigate-home)] (navigate-page chapter-page))))
