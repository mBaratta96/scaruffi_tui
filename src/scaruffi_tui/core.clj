(ns scaruffi-tui.core
  (:gen-class)
  (:require [scaruffi-tui.scraper :as scraper]
            [scaruffi-tui.cli :as cli]))

(defn -main
  ([]
   (let [table (scraper/get_table)]
     (let [rows (scraper/get_section_headers table)
           sections (scraper/get_section_content rows)]
       (doseq [[i row] (map-indexed vector rows)]
         (println (format "%d. %s" i (scraper/get_own_text row))))
       (let [section (nth sections (cli/check_input (count rows)))]
         (doseq [[i s] (map-indexed vector section)]
           (println (format "%d. %s" i (scraper/get_own_text s)))))))))
