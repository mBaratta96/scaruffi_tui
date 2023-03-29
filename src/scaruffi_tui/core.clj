(ns scaruffi-tui.core
  (:gen-class)
  (:require [scaruffi-tui.scraper :as scraper]
            [scaruffi-tui.cli :as cli]))

(defn print-options
  [rows]
  (doseq [[i row] (map-indexed vector rows)]
    (println (format "%d. %s" i (cli/get-own-text row)))))

(defn navigate-home
  []
  (let [table (scraper/get-table)
        rows (scraper/get-section-headers table)
        sections (scraper/get-section-content rows)]
    (print-options rows)
    (let [section (nth sections (cli/check-input (count rows)))]
      (cli/clear-console)
      (print-options section)
      (let [chapter (nth section (cli/check-input (count section)))]
        (cli/clear-console)
        (scraper/get-link chapter)))))

(defn -main
  ([]
   (let [chapter-page (navigate-home)
         table (scraper/get-table chapter-page)
         ; headers (scraper/get-chapter-headers table)
         indexes (scraper/get-dir-indexes table)]
     ;(print-options headers)
     (doseq [[i paragraphs] (map-indexed vector (cli/create-section indexes))]
       (println "SECTION:" i)
       (doseq [paragraph paragraphs]
         (println (cli/get-internal-text paragraph) "\n"))))))
       ;(println "PARAGRAPHS:" paragraphs)
       ;(println (reduce #(str %1 (scraper/get-own-text %2)) ""
       ;paragraphs))))))
