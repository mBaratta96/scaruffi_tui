(ns scaruffi-tui.data)

(def is-header?
  (fn [page-section]
    (or (= :h4 (:tag page-section)) (= :i (:tag page-section)))))

(def is-paragraph?
  (fn [page-section]
    (and (= :p (:tag page-section))
         (some? (:content page-section))
         (> (count (:content page-section)) 1))))

(defn get-upper-bound [bounds el] (last (filter #(< % el) bounds)))

(defn create-section
  [page]
  (let [indexed-page (map-indexed vector page)
        bound-indexes (map first (filter #(is-header? (second %)) indexed-page))
        paragraphs (filter #(is-paragraph? (second %)) indexed-page)]
    (partition-by #(get-upper-bound bound-indexes (first %)) paragraphs)))

(defn get-own-text
  [el]
  (-> el
      :content
      first))
