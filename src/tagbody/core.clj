(ns tagbody.core
  (:use [clojure.tools.macro :only [macrolet]]
        [clojure.set :only [union]]
        tagbody.Goto))

(compile 'tagbody.Goto)

(defn- tag? [form]
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

(defn- expand-tagbody [tags-and-forms]
  (let [[first & rest] tags-and-forms]
    (if (tag? first)
      (expand-tags-and-forms first rest (array-map))
      (expand-tags-and-forms (gensym "init") tags-and-forms (array-map)))))

(defn- key-not= [tag]
  (fn [[key _]] (not= tag key)))

(defn- literal-binding-value [binding]
  (set (.. binding init v)))


(defn goto* [tag]
  (throw (tagbody.Goto. tag)))

(defn tagbody* [tag-bodies]
  (let [local-tags (-> tag-bodies keys set)]
    (loop [goto-tag (-> tag-bodies first key)]
      (let [bodies (vals (drop-while (key-not= goto-tag) tag-bodies))]
        (when-let [tag (try
                         (doseq [body bodies] (body))
                         (catch tagbody.Goto goto
                           (let [tag (.state goto)]
                             (if (local-tags tag)
                               tag
                               (goto* tag)))))]
          (recur tag))))))


(let [tagbody-env-sym (gensym "tagbody-env")]

  (defmacro tagbody [& tags-and-forms]
    (let [tag-bodies (expand-tagbody tags-and-forms)
          local-tags (-> tag-bodies keys set)
          environment-tags (some-> (tagbody-env-sym &env) literal-binding-value)
          all-visible-tags (union local-tags environment-tags)]
      `(let [~tagbody-env-sym '~local-tags]
         (macrolet
          [(~'goto [tag#]
            (let [exptag# (expand-tag tag#)]
              (if (~all-visible-tags exptag#)
                `(goto* ~exptag#)
                (throw (Error. (str "Cannot goto nonexisting tag: " exptag#))))))]
          (tagbody* ~tag-bodies))) )))
