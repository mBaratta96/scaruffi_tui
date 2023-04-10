(ns scaruffi-tui.data)

(def ^:private ^:const GET-PAGE-HEADERS-METHODS
  {:no-index (fn [page] (filter #(or (= :h4 (:tag %)) (= :i (:tag %))) page)),
   :index (fn [page]
            (filter #(or (= :h4 (:tag (second %))) (= :i (:tag (second %))))
                    page))})

(defn get-page-headers
  ([page] ((:no-index GET-PAGE-HEADERS-METHODS) page))
  ([page method] ((get GET-PAGE-HEADERS-METHODS method) page)))

(defn get-upper-bound [bounds el] (last (filter #(< % el) bounds)))

(defn create-section
  [page]
  (let [indexed-page (map-indexed vector page)
        bounds (map #(first %) (get-page-headers indexed-page :index))
        paragraphs (filter #(and (= :p (:tag (second %)))
                                 (some? (:content (second %)))
                                 (> (count (:content (second %))) 1))
                           indexed-page)]
    (partition-by #(get-upper-bound bounds (first %)) paragraphs)))

(defn get-own-text
  [el]
  (-> el
      :content
      first))
