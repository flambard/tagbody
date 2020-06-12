(ns tagbody.core
  (:use [clojure.tools.macro :only [macrolet]]
        tagbody.Goto))

(compile 'tagbody.Goto)

(defn tag? [form]
  (or (symbol? form)
      (keyword? form)
      (integer? form)))

(defn expand-tag [tag]
  (if (symbol? tag)
    (str tag)
    tag))

(defn- expand-tags-and-forms [tag forms tag-map]
  (if-not (tag? tag)
    tag-map
    (let [[tag-forms [next-tag & rest]] (split-with (complement tag?) forms)
          forms-tag (expand-tag tag)]
      (recur next-tag rest (conj tag-map {forms-tag `(fn [] ~@tag-forms)})))))

(defn expand-tagbody [tags-and-forms]
  (let [[first & rest] tags-and-forms]
    (if (tag? first)
      (expand-tags-and-forms first rest (array-map))
      (expand-tags-and-forms (gensym "init") tags-and-forms (array-map)))))

(defn key-not= [tag]
  (fn [[key _]] (not= tag key)))

(defn- literal-binding-value [binding]
  (.. binding init v))


(let [tagbody-env-sym (gensym "tagbody-env")]

  (defmacro tagbody [& tags-and-forms]
    (let [tag-bodies (expand-tagbody tags-and-forms)
          local-tags (keys tag-bodies)
          init-tag (first local-tags)
          tags-from-environment (when-let [binding (tagbody-env-sym &env)]
                                  (literal-binding-value binding))
          all-visible-tags (set (concat local-tags tags-from-environment))]
      `(let [~tagbody-env-sym '~local-tags]
         (macrolet
          [(~'goto [tag#]
            (let [exptag# (expand-tag tag#)]
              (if (~all-visible-tags exptag#)
                `(throw (tagbody.Goto. ~exptag#))
                (throw (Error. (str "Cannot goto nonexisting tag: " exptag#))))))]
          (let [tag-bodies# ~tag-bodies]
            (loop [goto-tag# ~init-tag]
              (let [bodies# (vals (drop-while (key-not= goto-tag#) tag-bodies#))]
                (when-let [tag# (try
                                  (doseq [body# bodies#] (body#))
                                  (catch tagbody.Goto goto#
                                    (let [tag# (.state goto#)]
                                      (if (~(set local-tags) tag#)
                                        tag#
                                        (throw (tagbody.Goto. tag#))))))]
                  (recur tag#))))))) )) )
