(ns scaruffi-tui.scraper
  (:require [clj-http.lite.client :as client]
            [hickory.core :refer [parse as-hickory]]
            [hickory.select :as s]
            [clojure.string :as string]))

(defn get-page [link] (client/get link))

(def ^:private ^:const SCARUFFI-URL "https://scaruffi.com/history/")

(defn parse-page
  [page]
  (-> page
      get-page
      :body
      parse
      as-hickory))

(defn get-table
  ([page]
   (let [parsed (parse-page page)]
     (first (s/select (s/descendant
                       (s/and (s/tag :table)
                              (s/attr :width #(> (Integer/parseInt %) 600))))
                      parsed))))
  ([page base-url] (get-table (str base-url page))))

(defn get-artist-table
  [page]
  (let [parsed (parse-page page)]
    (s/select (s/descendant (s/and (s/tag :table) (s/attr :width #(= % "100%")))
                            (s/and (s/tag :td)
                                   (s/attr :width #(= % "50%"))
                                   (s/not (s/has-descendant
                                           (s/and (s/tag :a)
                                                  (s/attr :href
                                                          #(string/includes?
                                                            %
                                                            "translate")))))))
              parsed)))

(defn get-artist-rating
  [page]
  (let [parsed (parse-page page)]
    (first (s/select (s/descendant (s/and (s/tag :table)
                                          (s/attr :width #(not (= % "100%"))))
                                   (s/tag :font))
                     parsed))))

(defn get-section-headers
  [table]
  (s/select (s/and (s/descendant (s/tag :li)) (s/has-child (s/tag :ol))) table))

(defn get-rows-content
  [rows]
  (map #(s/select (s/descendant (s/tag :ol) (s/tag :li) (s/tag :a)) %) rows))

(defn get-chapter-headers
  [table]
  (s/select (s/or (s/descendant (s/tag :h4) (s/tag :i))
                  (s/descendant (s/and (s/tag :h4)
                                       (s/not (s/has-child (s/tag :i)))))
                  (s/descendant (s/tag :p)))
            table))

(defn get-page-content
  [table]
  (-> (s/select (s/descendant (s/and (s/tag :dir)
                                     (s/not (s/has-child (s/tag :dir)))
                                     (s/not (s/has-child (s/tag :ol)))))
                table)
      first
      get-chapter-headers))

(defn get-artist-page-content
  [artist-link]
  (-> artist-link
      get-artist-table))

(defn get-artist-ratingss
  [artist-link]
  (-> artist-link
      get-artist-rating))

(defn get-link
  [el]
  (-> el
      :attrs
      :href))

(def ^:private ^:const SCARUFFI-HOME "long.html")

(defn get-homepage-rows
  []
  (-> SCARUFFI-URL
      (str SCARUFFI-HOME)
      get-table
      get-section-headers))

(defn get-chapter
  [page]
  (-> SCARUFFI-URL
      (str page)
      get-table
      get-page-content))
