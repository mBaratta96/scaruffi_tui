(ns scaruffi-tui.cli
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [clojure.term.colors :as color]))

(defn check-input
  [row-length]
  (loop []
    (println "Enter your input:")
    (let [in (edn/read-string (read-line))]
      (cond (not (and (integer? in) (< in row-length))) (recur)
            :else in))))

(defn get-page-headers
  [page]
  (filter #(or (= :h4 (:tag %)) (= :i (:tag %))) page))

(defn get-upper-bound [bounds el] (last (filter #(< % el) bounds)))

(defn create-section
  [page]
  (let [bounds (map #(.indexOf page %) (get-page-headers page))
        paragraphs (filter #(and (= :p (:tag %))
                                 (some? (:content %))
                                 (> (count (:content %)) 1))
                           page)]
    ;;(println bounds)
    ;;(println (map #(get-upper-bound bounds (.indexOf page %)) paragraphs))
    (partition-by #(get-upper-bound bounds (.indexOf page %)) paragraphs)))

(defn get-own-text
  [el]
  (-> el
      :content
      first
      string/trim))

(defn get-internal-text
  [el]
  (let [content (:content el)]
    (string/trim-newline
     (string/join
      " "
      (map #(cond (:type %) (cond (some? (:content %))
                                  (let [tag (:tag %)
                                        content (string/trim
                                                 (first (:content %)))]
                                    (cond (= :a tag) (color/underline
                                                      (color/blue content))
                                          (= :i tag) (color/green content)
                                          (= :b tag) (color/bold content)
                                          :else "")))
                  :else (string/trim (string/replace % "\n" " ")))
           content)))))

(defn clear-console [] (print "\033\143"))

(defn print-header
  [header]
  (println (color/yellow (string/upper-case (get-own-text header)) "\n")))

(defn print-options
  [rows]
  (doseq [[i row] (map-indexed vector rows)]
    (println (color/bold (format "%s. %s"
                                 (color/cyan i)
                                 (color/bold (get-own-text row)))))))
