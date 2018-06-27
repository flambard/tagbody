> *There once was a master programmer who wrote unstructured programs. A novice programmer, seeking to imitate him, also began to write unstructured programs. When the novice asked the master to evaluate his progress, the master criticized him for writing unstructured programs, saying, "What is appropriate for the master is not appropriate for the novice. You must understand the Tao before transcending structure."*
>
>-- from The Tao of Programming

# tagbody

Common Lisp-style TAGBODY for Clojure.

## Example

```clojure
(with-local-vars [val 0]
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
  (var-get val))
; => 15
```

## License

Copyright © 2018 Markus Flambard

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
