(ns scaruffi-tui.core
  (:gen-class)
  (:require [scaruffi-tui.cli :as cli]
            [scaruffi-tui.home :as home]
            [scaruffi-tui.artist :as artist]
            [scaruffi-tui.chapter :as chapter]))

(set! *warn-on-reflection* true)

(defn navigate-home
  []
  (let [rows (home/get-home)
        sections (home/get-rows-content rows)]
    (cli/print-options rows)
    (let [section (nth sections (cli/check-input (count rows)))]
      (cli/clear-console)
      (cli/print-options section)
      (let [chapter (nth section (cli/check-input (count section)))]
        (cli/clear-console)
        (home/get-link chapter)))))

(defn navigate-chapter
  [page]
  (let [content (chapter/get-chapter page)
        headers (chapter/filter-headers content)
        sections (chapter/create-section content)]
    (cli/print-options headers)
    (let [index (cli/check-input (count headers))
          section (nth sections index)
          header (nth headers index)]
      (cli/clear-console)
      (chapter/print-header header)
      (chapter/print-paragraphs (map second section)))))

(defn get-artists
  [artist-links]
  (cli/clear-console)
  (cli/print-options (vec (map :name artist-links)))
  (let [index (cli/check-input (count artist-links))
        link (:link (nth artist-links index))
        tables (artist/get-artist-page-content link)
        ratings (artist/get-artist-ratings link)]
    (cli/clear-console)
    (artist/print-artist tables)
    (println link "\n")
    (println (artist/print-ratings (:content ratings)))))

(defn navigate-artists
  [artist-links]
  (let [answer (cli/ask-continuation)]
    (if (= answer "y") (get-artists artist-links))))

(defn -main
  ([]
   (let [chapter-page (navigate-home)
         artist-links (navigate-chapter chapter-page)]
     (navigate-artists artist-links))))
