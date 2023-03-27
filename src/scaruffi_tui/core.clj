(ns scaruffi-tui.core
  (:gen-class)
  (:require [clj-http.client :as client])
  (:require [hickory.core :refer [parse as-hickory]])
  (:require [hickory.select :as s])
  (:require [clojure.string :as string]))

(defn get_page [link] (client/get link))

(def scaruffi_home "https://scaruffi.com/history/long.html")

(defn parse_website
  []
  (-> (get_page scaruffi_home)
      :body
      parse
      as-hickory))

(defn get_table
  []
  (s/select (s/and (s/descendant (s/and (s/tag :table)
                                        (s/attr :width #(= % "700")))
                                 (s/tag :li))
                   (s/has-child (s/tag :ol)))
            (parse_website)))

(defn -main
  ([]
   (let [rows (get_table)]
     (doseq [el rows]
       (println (-> el
                    :content
                    first
                    string/trim))))))
