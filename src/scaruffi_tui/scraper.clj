(ns scaruffi-tui.scraper
  (:require [clojure.string :as string]
            [clj-http.client :as client]
            [hickory.core :refer [parse as-hickory]]
            [hickory.select :as s]))

(defn get_page [link] (client/get link))

(def scaruffi_home "https://scaruffi.com/history/long.html")

(defn parse_page
  [page]
  (-> (get_page page)
      :body
      parse
      as-hickory))

(defn get_table
  []
  (first (s/select (s/descendant (s/and (s/tag :table)
                                        (s/attr :width #(= % "700"))))
                   (parse_page scaruffi_home))))

(defn get_section_headers
  [table]
  (s/select (s/and (s/descendant (s/tag :li)) (s/has-child (s/tag :ol))) table))

(defn get_section_content
  [rows]
  (map #(s/select (s/descendant (s/tag :ol) (s/tag :li) (s/tag :a)) %) rows))

(defn get_own_text
  [el]
  (-> el
      :content
      first
      string/trim))
