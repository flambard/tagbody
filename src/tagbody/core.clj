(ns tagbody.core
  (:use [clojure.tools.macro :only [macrolet]]
        tagbody.Goto))

(compile 'tagbody.Goto)

(defn tag? [form]
  (or (symbol? form)
      (keyword? form)
      (integer? form)))

(defn- expand-tags-and-forms [tag forms tag-map]
  (if-not (tag? tag)
    tag-map
    (let [[tag-forms [next-tag & rest]] (split-with (complement tag?) forms)]
      (recur next-tag rest (conj tag-map [`'~tag `(fn [] ~@tag-forms)])))))

(defn expand-tagbody [tags-and-forms]
  (let [[first & rest] tags-and-forms]
    (if (tag? first)
      (expand-tags-and-forms first rest (array-map))
      (expand-tags-and-forms (gensym "init") tags-and-forms (array-map)))))

(defn key-not= [tag]
  (fn [[key _]] (not= tag key)))


(defmacro tagbody [& tags-and-forms]
  (let [tag-bodies (expand-tagbody tags-and-forms)
        [init-tag _] (first tag-bodies)]
    `(macrolet
      [(~'goto [tag#]
        (if (.contains ~(vec (keys tag-bodies)) tag#)
          `(throw (tagbody.Goto. '~tag#))
          (throw (Error. (str "Cannot goto nonexisting tag: " tag#)))))]
      (let [tag-bodies# ~tag-bodies]
        (loop [goto-tag# ~init-tag]
          (let [bodies-to-eval# (drop-while (key-not= goto-tag#) tag-bodies#)]
            (when-let [tag# (try
                              (doseq [[_# body#] bodies-to-eval#]
                                (body#))
                              (catch tagbody.Goto tag#
                                (.state tag#)))]
              (recur tag#)))))) ))
