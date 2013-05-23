# vinzi.data

An extension of data.zip with additional funcionality.
A proposal for merge in clojure.data.zip was posted,
but no response received yet.

This file also contains a test-file showing the usage of 
the existing clojure.data.zip.

## Releases and Dependency Information

Latest stable release is 0.1.0.

The libary will be published as binary on clojars.

```clojure
[org.clojars.cvkem/vinzi.data "0.1.0"]
```

Maven dependency information:

```XML
<dependency>
  <groupId>org.clojars.cvkem</groupId>
  <artifactId>vinzi.data</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Usage
During the process of using data.zip.xml I also made a few small extensions to the data.zip.xml library. I would like to know whether it makes sense to merge some these changes back into data.zip.xml. The extensions are:


1. Modification of 'tag=  to accept Sets and Functions as arguments to match xml-tag strings, so:
```clojure 
(tag= #{:A :B}) 
```
will match elements with tag <A> or tag <B> and

```clojure
(tag= match-func)
```
 will match all elements where (match-func …) returns true

2. Similar modifications for the 'attr= predicate.

3. When the 'attr= is called with a map it matches each of the key-value pairs in this map, so
```clojure
(attr= {:par1 “value 1” :par2 “value 2”}) 
```
returns the same matches as the following two predicates applied in sequence

(attr= :par1 “value 1”) (attr= :par2 “value 2”)

4. Synchronization of the location of two xml-zippers A and B via construct-path and reconstruct-loc. So:
```clojure
(reconstruct-loc A (construct-path B))
```
moves A to the same zipper-position as B (use-case: merge changes made in a subtree of B back into zipper-tree A, while leaving the rest of A untouched.

5. Addition (is-zipper? x) which tests whether x is a zipper (convenient for unit-testing and as a pre-condition on end-user functions like (xml-> zipper …))


Furthermore I developed unit-tests to discover/dissect the functionality of data/zip/xml/test.xml, as the documentation and examples of this library are fairly limited (basically you need to dive fairly deep into the source-code of to understand how it works. I could merge my tests and extended documentation into clojure.data.zip if that helps the accessibility of this library.


## Developer Information

- [GitHub Project](https://github.com/cvkem/vinzi.data)


## License

Copyright © Vinzi/C. van Kemenade.

Licensed under the EPL (see the file epl.html), same as clojure.
