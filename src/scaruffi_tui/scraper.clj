(ns scaruffi-tui.scraper
  (:require [clojure.string :as string]
            [clj-http.client :as client]
            [hickory.core :refer [parse as-hickory as-hiccup]]
            [hickory.select :as s]))

(defn get-page [link] (client/get link))

(def scaruffi-url "https://scaruffi.com/history/")
(def scaruffi-home "long.html")

(defn parse-page
  [page]
  (-> (get-page page)
      :body
      parse
      as-hickory))

(defn get-table
  ([] (get-table scaruffi-home))
  ([page]
   (first (s/select (s/descendant (s/and (s/tag :table)
                                         (s/attr :width #(= % "700"))))
                    (parse-page (str scaruffi-url page))))))

(defn get-section-headers
  [table]
  (s/select (s/and (s/descendant (s/tag :li)) (s/has-child (s/tag :ol))) table))

(defn get-section-content
  [rows]
  (map #(s/select (s/descendant (s/tag :ol) (s/tag :li) (s/tag :a)) %) rows))

(defn get-chapter-headers
  [table]
  (s/select (s/or (s/descendant (s/tag :h4) (s/tag :i))
                  (s/descendant (s/and (s/tag :h4)
                                       (s/not (s/has-child (s/tag :i)))))
                  (s/descendant (s/tag :p)))
            table))

(defn get-dir-indexes
  [table]
  (-> (s/select (s/descendant (s/and (s/tag :dir)
                                     (s/not (s/has-child (s/tag :dir)))
                                     (s/not (s/has-child (s/tag :ol)))))
                table)
      first
      get-chapter-headers))

(defn get-own-text
  [el]
  (-> el
      :content
      first
      string/trim))

(defn get-link
  [el]
  (-> el
      :attrs
      :href))

(defn get-internal-text
  [el]
  (let [content (:content el)]
    (string/trim-newline
     (string/join " "
                  (map #(cond (:type %) (cond (some? (:content %))
                                              (string/upper-case
                                               (first (:content %)))
                                              :else "\n\n")
                              (= % "\n") "\n"
                              :else (string/trim (string/replace % "\n" " ")))
                       content)))))
