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
                (goto 'lp)
                (var-set val (+ (var-get val) 3))
                lp
                (var-set val (+ (var-get val) 4)))
               (var-get val))))))

(deftest clhs-example-2
  (testing "Example 2 from the Common Lisp HyperSpec"
    (is (= 15 (with-local-vars [val 0]
                (tagbody
                 (var-set val 1)
                 (goto 'point-a)
                 (var-set val (+ (var-get val) 16))
                 point-c
                 (var-set val (+ (var-get val) 4))
                 (goto 'point-b)
                 (var-set val (+ (var-get val) 32))
                 point-a
                 (var-set val (+ (var-get val) 2))
                 (goto 'point-c)
                 (var-set val (+ (var-get val) 64))
                 point-b
                 (var-set val (+ (var-get val) 8)))
                (var-get val))))))

