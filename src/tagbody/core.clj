(ns tagbody.core
  (:use [clojure.tools.macro :only [macrolet]]
        [clojure.set :only [union]]
        tagbody.Goto))

(compile 'tagbody.Goto)

(defn- key-not= [tag]
  (fn [[key _]] (not= tag key)))

(defn- run-bodies-catching-goto [bodies]
  (try
    (run! #(%) bodies)
    (catch tagbody.Goto goto
      (.state goto))))

(defn- tag? [form]
  (or (symbol? form)
      (keyword? form)
      (integer? form)))

(defn- expand-tags-and-forms [tags-and-forms]
  (loop [[tag & forms] tags-and-forms, tag-map (array-map)]
    (if-not (tag? tag)
      tag-map
      (let [[tag-forms more-tags-and-forms] (split-with (complement tag?) forms)
            clause {(str tag) `(fn [] ~@tag-forms)}]
        (recur more-tags-and-forms (conj tag-map clause))))))

(defn- expand-tagbody [body]
  (expand-tags-and-forms (if (tag? (first body))
                           body
                           `(~(gensym "init") ~@body))))

(defn- literal-binding-value [binding]
  (set (.. binding init v)))


(defn goto* [tag]
  (throw (tagbody.Goto. tag)))

(defn tagbody* [tag-bodies]
  (let [local-tags (-> tag-bodies keys set)]
    (loop [goto-tag (-> tag-bodies first key)]
      (when-let [tag (->> tag-bodies
                          (drop-while (key-not= goto-tag))
                          vals
                          run-bodies-catching-goto)]
        (if (local-tags tag)
          (recur tag)
          (goto* tag))))))


(let [tagbody-env-sym (gensym "tagbody-env")]

  (defmacro tagbody [& tags-and-forms]
    (let [tag-bodies (expand-tagbody tags-and-forms)
          local-tags (-> tag-bodies keys set)
          environment-tags (some-> (tagbody-env-sym &env) literal-binding-value)
          all-visible-tags (union local-tags environment-tags)]
      `(let [~tagbody-env-sym ~local-tags]
         (macrolet
          [(~'goto [tag#]
            (let [exptag# (str tag#)]
              (if (~all-visible-tags exptag#)
                `(goto* ~exptag#)
                (throw (Error. (str "Cannot goto nonexisting tag: " exptag#))))))]
          (tagbody* ~tag-bodies))) )))
