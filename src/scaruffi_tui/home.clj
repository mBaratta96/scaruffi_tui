(ns scaruffi-tui.home
  (:require [hickory.select :as s]
            [scaruffi-tui.scraper :as scraper]))

(def ^:private ^:const SCARUFFI-URL "https://scaruffi.com/history/")

(def ^:private ^:const SCARUFFI-HOME "long.html")

; Ex: From Subculture to Counterculture (roughly 1951-1966)
(defn- get-section-headers
  [table]
  (s/select (s/and (s/descendant (s/tag :li)) (s/has-child (s/tag :ol))) table))

; Ex: 1. Background: The 20th Century (*)
(defn get-rows-content
  [rows]
  (map #(s/select (s/descendant (s/tag :ol) (s/tag :li) (s/tag :a)) %) rows))

(defn get-home
  []
  (-> SCARUFFI-URL
      (str SCARUFFI-HOME)
      scraper/parse-page
      scraper/get-table
      get-section-headers))

(defn get-link
  [el]
  (-> el
      :attrs
      :href))
