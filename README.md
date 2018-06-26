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
