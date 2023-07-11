(ns scaruffi-tui.scraper
  (:require [clj-http.lite.client :as client]
            [hickory.core :refer [parse as-hickory]]
            [hickory.select :as s]))

(defn get-page [link] (client/get link))

(defn parse-page
  [page]
  (-> page
      get-page
      :body
      parse
      as-hickory))

(defn get-table
  ([parsed-table]
   (first (s/select (s/descendant
                     (s/and (s/tag :table)
                            (s/attr :width #(> (Integer/parseInt %) 600))))
                    parsed-table))))
