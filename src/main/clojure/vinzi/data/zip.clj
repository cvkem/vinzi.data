(ns vinzi.data.zip
  (:use	[clojure pprint]
        [clojure.tools logging]
        [clojure.data.zip.xml :only [xml-> xml1->]]
)
  (:require 
            [clojure [zip :as zip]]
            [clojure.data
             [json :as json]
             [zip :as dZip]]
            [clojure.data.zip
             [xml :as xZip]]
	    ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;   Extension of clojure.data.zip.xml
;;   This code could easily be merged with clojure.data.zip.xml WITHOUT changing 
;;   the existing functionality. These are just additions, 
;;   and functions like (attr= ...) that extend the existing functionality.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn is-zipper?
  "Routine to test whether 'zipper' is a real zipper or nil. 
   Used in pre-conditions to prevent extracted nodes from being passed as an argument when actually a zipper is needed.
   NOTE: nil is always flagged as a (potential) zipper!"
  [zipper]
  (or (nil? zipper)
      (when (:zip/make-node (meta zipper)) true)))

(defn zip-top
  "Move the zipper to the top of the tree (materializing all changes). 
   If the object is at top already the original object is returned (so you can use identical? to see there are no changes)."
  [z]
  (if-let [u (zip/up z)]
    (recur u)
      z))

(defn make-matcher "Prepare a function that takes a map
  as input and matches it against value 'v'. If 'v' 
  is a set or function it will be applied, otherwise 
  a comparison will be made (assume string or numeral)." [k v]
  (let [field (if (or (keyword k) (fn? k)) 
                k #(get % k))]
    (if (or (fn? v) (set? v))
               (comp v field)
                 #(= v (field %)))))


(defn tag=
  "Returns a query predicate that matches a node when its is a tag
named tagname."
  [tagname]
  (let [match (make-matcher #(:tag (zip/node %)) tagname)]
    (fn [loc]
      (filter #(and (zip/branch? %) (match %))
              (if (dZip/auto? loc)
                (dZip/children-auto loc)
                (list (dZip/auto true loc)))))))

(defn attr=
  "Returns a query predicate that matches a node when it has an
  attribute named attrname whose value is attrval."
  ([attrmap] {:pre [(map? attrmap)]}
             (let [matchers (map attr= (keys attrmap) (vals attrmap))]
               (fn [loc] (letfn [(and-attr [cumm matcher] (and cumm (matcher loc)))]
                                (reduce and-attr true matchers)))))
  ([attrname attrval] 
    (fn [loc]
      (= attrval (xZip/attr loc attrname)))))

 ;; not used  (old code?)
;(defn attr1=
;  "Returns a query predicate that matches a node when it has an
;  attribute named attrname whose value is attrval."
;  ([attrmap] {:pre [(map? attrmap)]}
;             (let [kvs (seq attrmap)]
;               (fn [loc]
;                 (when (zip/branch? loc) 
;                   (let [attrs (-> loc zip/node :attrs)]
;                     (letfn [(and-attr [cumm [k v]] 
;                                       (and cumm (= (k attrs) v)))]
;                                (reduce and-attr true kvs)))))))
;  ([attrname attrval] 
;    (fn [loc]
;      (= attrval (xZip/attr loc attrname)))))


(defn tagattrs= "Return items that match on tag AND
 on the sequence of attributes."
  [tagname & attrkv]
  {:pre [(even? (count attrkv))]}
  (let [tagmatch (tag= tagname)
        matchers (map (fn [[k v]] (make-matcher k v))
                      (partition 2 attrkv))
        match (fn [locs]
                ;; function processes list of loc
                (letfn [(match-loc ;; reduce one loc 
                          [loc]
                          
                          (let [node (zip/node loc)
                                attr (:attrs node)]
                            (reduce (fn [cumm matcher]
                                      (let [res (matcher attr)]
                                      (and cumm res)))
                                 loc matchers)))]
                       (filter match-loc locs)))]
  (fn [loc] 
    (when-let [locs (tagmatch loc)]
      (if (seq matchers)
        (match locs)
        locs)))))



(defn construct-path 
  "Construct a path that can be applied to a similar xml-zip structure.
   Path matches :tag and :attrs along the path. 
   Note: XML allows multiple nodes with same path, so beware of duplicates. 
     Use (is-zipper?) to check whether you receive a zipper, or a lazy sequence of zippers, if
      duplicates can exist."
  ;; Use tagattrsCnt= instead of tagattrs=  to exclude nodes with additional attrs
  [loc]
  {:pre [loc (is-zipper? loc)]}
  (letfn [(gen-tagattrs= [node]
                         (if-let [attrs (:attrs node)]
                                  (list (tag= (:tag node))
                                        (attr= attrs))
                                  (list (tag= (:tag node)))))]
         (let [path (zip/path loc)
               path (conj (vec path) (zip/node loc))]
           (apply concat (map gen-tagattrs= path))))) 

(defn reconstruct-loc 
  "Find the location indicated by 'path' in the 'zipper'.
   Warning: reconstruction does not check whether a node has more attributes than specified as 
     parameter. This might result in multiple matches due to similar locs with additional attributes." 
  ;; TODO: solve issue by introduction of tagattrsCnt= and use is in contruct-path
  [zipper path]
  {:pre [(is-zipper? zipper)]}
  ;; (auto false zipper) used, such that root is matched against first item of path.
  (let [lpf "(reconstruct-loc): "
        zipper (zip-top zipper)
        locs (apply xml-> (dZip/auto false zipper) path)
        cnt (count locs)]
    (if (= cnt 1)
      (first locs)  ;; reconstruct-loc succeeded
      (if (> cnt 1)
        (do
          (warn lpf "obtained " cnt " locations for path. Expected only one (return sequence).")
          locs)
        (warn lpf "could not find location")))))


(defn xml->oper 
  "Select all nodes of 'cdp' that match the 'select-tags' via xml-> and subsequently apply 'oper'  to each of the
   nodes and merge the result in a single zipper (so 'oper' takes a zipper located
   at the corrected node and returns a modified zipper). 
   NOTE: xml-> returns a series of independent zippers, so additional
   steps are needed to merge changes)."
  [oper cdp & select-tags] 
  {:pre [(fn? oper) (is-zipper? cdp)]}
  (let [locs (apply xml->  cdp select-tags)
        fixCdp (fn [cdp loc]
                 (trace " Apply fix for loc: " (zip/node loc)
                        " with path: " (construct-path loc))
                 (let [ncdp (reconstruct-loc cdp (construct-path loc))]
                   (if (nil? ncdp)
                     cdp   ;; can happen if paths are not unique (had multiple matches earlier)
                     (if (is-zipper? ncdp)
                       (oper ncdp)
                       (let [fcdp (first ncdp)]
                         (debug "Received: " (count ncdp) " matches. Processing matches recursively.")
                         (doseq [c ncdp]
                           (trace "\tMULTI-MATCH: is-zipper? = " (is-zipper? c) (zip/node c)))
                         (recur (oper fcdp) loc))))))]
    (reduce fixCdp cdp locs)))




(defn replace-branch-node 
  "Replace a branch node of a zipper. 
  Preferably you should use (replace-node ...)"  
  [zipper newValue]
  {:pre [(zip/branch? zipper)]}
  ;; TODO: make inner function of replace-node (and remove pre-condition)
  ;; separate function for unit-testing
  (let [children (zip/children zipper)]
    (zip/make-node zipper newValue children)))


(defn replace-leaf-node "Replace a leaf-node of a zipper. 
  Preferably you should use (replace-node ...)"
  [zipper newValue]
  {:pre [(not (zip/branch? zipper))]}
  ;; TODO: make inner function of replace-node (and remove pre-condition)
  ;; separate function for unit-testing
  (let [lpf "(replace-leaf-node): "
        ;; prepare modified list of children
        lefts (zip/lefts zipper)
        children (concat (list newValue) (zip/rights zipper))
        parent (zip/up zipper)
        newNode (zip/make-node parent (zip/node parent) children)
        newZip (zip/replace parent newNode)]
    ;; reconstruct location of the zipper
    (loop [newZip (zip/down newZip)
           lefts lefts]
      (if (seq lefts)
        (recur (zip/right newZip) (rest lefts))
        newZip))))


(defn replace-node "Replace in the target (a zipper) the node at location as indicated by 'locateZip' 
  and return a zipper pointing to the replaced location." 
  [target path newNode]
  {:pre [(is-zipper? target)]}
  (let [lpf "(replace-node): " 
        target (reconstruct-loc target path)]
    (warn lpf "UNIT-TESTING still needs to be performed!!\n"
          "Refactor the code to include the two subfunctions")
    (if (zip/branch? target)
      (replace-branch-node target newNode)
      (replace-leaf-node target newNode))))



