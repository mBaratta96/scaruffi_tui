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

(defn get-indexed-page-headers
  [page]
  (filter #(or (= :h4 (:tag (second %))) (= :i (:tag (second %)))) page))

(defn get-upper-bound [bounds el] (last (filter #(< % el) bounds)))

(defn create-section
  [page]
  (let [indexed-page (map-indexed vector page)
        bounds (map #(first %) (get-indexed-page-headers indexed-page))
        paragraphs (filter #(and (= :p (:tag (second %)))
                                 (some? (:content (second %)))
                                 (> (count (:content (second %))) 1))
                           indexed-page)]
    (partition-by #(get-upper-bound bounds (first %)) paragraphs)))

(defn get-own-text
  [el]
  (-> el
      :content
      first
      string/trim))

(defn get-internal-text
  [el]
  (let [content (:content (second el))]
    (string/trim-newline
     (string/join " "
                  (map #(if (:type %)
                          (if (some? (:content %))
                            (let [tag (:tag %)
                                  content (string/trim (first (:content %)))]
                              (cond (= :a tag) (color/underline (color/blue
                                                                 content))
                                    (= :i tag) (color/green content)
                                    (= :b tag) (color/bold content)
                                    :else "")))
                          (string/trim (string/replace % #"\s" " ")))
                       content)))))

(def base-url "https://scaruffi.com")

(defn get-links
  [el]
  (let [content (:content (second el))]
    (string/join "\n"
                 (map #(format "%64s: %s"
                               (color/yellow (first (:content %)))
                               (str base-url (subs (:href (:attrs %)) 2)))
                      (filter #(and (:type %) (some? (:content %)) (= :a (:tag %)))
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
