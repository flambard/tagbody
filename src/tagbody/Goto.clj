(ns tagbody.Goto
  (:gen-class :extends java.lang.Throwable
              :state state
              :init init
              :constructors {[Object] []}))


(defn -init [tag]
  [[] tag])

(defn -fillInStackTrace [this]
  this)
