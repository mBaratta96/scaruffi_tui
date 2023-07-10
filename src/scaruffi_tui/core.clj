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

(defn navigate-page
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

(defn -main
  ([] (let [chapter-page (navigate-home)] (navigate-page chapter-page))))
