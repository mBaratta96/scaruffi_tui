(ns scaruffi-tui.core
  (:gen-class)
  (:require [scaruffi-tui.scraper :as scraper]
            [scaruffi-tui.cli :as cli]))
(defn -main
  ([]
   (let [rows (scraper/get_table)]
     (doseq [[i row] (map-indexed vector rows)]
       (println (format "%d. %s" i (scraper/get_own_text row))))
     (cli/check_input (count rows)))))
