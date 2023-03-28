(ns scaruffi-tui.core
  (:gen-class)
  (:require [scaruffi-tui.scraper :as scraper]
            [scaruffi-tui.cli :as cli]))

(defn print-options
  [rows]
  (doseq [[i row] (map-indexed vector rows)]
    (println (format "%d. %s" i (scraper/get-own-text row)))))

(defn navigate-home
  []
  (let [table (scraper/get-table)
        rows (scraper/get-section-headers table)
        sections (scraper/get-section-content rows)]
    (print-options rows)
    (let [section (nth sections (cli/check-input (count rows)))]
      (print-options section)
      (let [chapter (nth section (cli/check-input (count section)))]
        (scraper/get-link chapter)))))

(defn -main
  ([]
   (let [chapter-page (navigate-home)
         table (scraper/get-table chapter-page)
         headers (scraper/get-chapter-headers table)
         indexes (scraper/get-dir-indexes table)]
     (print-options headers)
     (doseq [content (:content indexes)] (println "CONTENT:" content)))))
