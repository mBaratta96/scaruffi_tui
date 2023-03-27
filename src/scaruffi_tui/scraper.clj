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
  (s/select (s/and (s/descendant (s/and (s/tag :table)
                                        (s/attr :width #(= % "700")))
                                 (s/tag :li))
                   (s/has-child (s/tag :ol)))
            (parse_page scaruffi_home)))

(defn get_own_text
  [el]
  (-> el
      :content
      first
      string/trim))
