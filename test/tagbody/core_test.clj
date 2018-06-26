(ns tagbody.core-test
  (:require [clojure.test :refer :all]
            [tagbody.core :refer :all]))

(deftest empty-body
  (testing "Empty tagbody"
    (is (nil? (tagbody)))))

(deftest initial-tag
  (testing "Putting a tag at the top"
    (is (= 13 (with-local-vars [x 0]
                (tagbody
                 :top
                 (var-set x (+ (var-get x) 1))
                 (if (= 10 (var-get x))
                   (goto :bottom)
                   (goto :top))
                 :bottom
                 (var-set x (+ (var-get x) 3)))
                (var-get x))))))

(deftest clhs-example-1
  (testing "Example 1 from the Common Lisp HyperSpec"
    (is (= 6 (with-local-vars [val 0]
               (tagbody
                (var-set val 2)
                (goto lp)
                (var-set val (+ (var-get val) 3))
                lp
                (var-set val (+ (var-get val) 4)))
               (var-get val))))))

(deftest clhs-example-2
  (testing "Example 2 from the Common Lisp HyperSpec"
    (is (= 15 (with-local-vars [val 0]
                (tagbody
                 (var-set val 1)
                 (goto point-a)
                 (var-set val (+ (var-get val) 16))
                 point-c
                 (var-set val (+ (var-get val) 4))
                 (goto point-b)
                 (var-set val (+ (var-get val) 32))
                 point-a
                 (var-set val (+ (var-get val) 2))
                 (goto point-c)
                 (var-set val (+ (var-get val) 64))
                 point-b
                 (var-set val (+ (var-get val) 8)))
                (var-get val))))))

(defn- f2 [flag escape]
  (if flag (escape) 2))

(defn- f1 [flag]
  (with-local-vars [n 1]
    (tagbody
     (var-set n (f2 flag (fn [] (goto :out))))
     :out)
    (var-get n)))

(deftest clhs-example-3
  (testing "Example 3 from the Common Lisp HyperSpec"
    (is (= 2 (f1 nil)))
    (is (= 1 (f1 true)))))

(deftest integer-tags
  (testing "Using integers as tags"
    (is (= 20 (with-local-vars [i 0]
                (tagbody
                 10 (var-set i (+ (var-get i) 2))
                 20 (when (< (var-get i) 10)
                      (goto 10))
                 30 (var-set i (* (var-get i) 2)))
                (var-get i))))))

(deftest multiple-tags
  (testing "Using multiple tags at the same location"
    (is (= 42 (with-local-vars [x 0]
                (tagbody
                 (goto :middle)
                 :top
                 (goto :bottom)
                 :middle
                 :center
                 (goto :top)
                 :bottom
                 :end
                 (var-set x 42))
                (var-get x))))))
