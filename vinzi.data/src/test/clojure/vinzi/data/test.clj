(ns vinzi.data.test
  (:use clojure.test
        [clojure.data.zip.xml :only [xml-> xml1->]])
  (:require
    [vinzi.data.zip :as vZip] ;; vZip is the subject of these tests
    [clojure
     [xml :as xml]
     [string :as str]
     [zip :as zip]]
    [clojure.data
     [zip :as dZip]]
    [clojure.data.zip 
     [xml :as xZip]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  These tests also perform tests on clojure.data.zip.xml to developed to disover/show it's functionality
;;


;; determine whether to cleanUp temporary files and database-tables after running tests
(def cleanUp 
;;  true
  false
)



(def content_test0_cdp "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<level0>
 <clojure>
 <![CDATA[

(defn hello-cdp \"test function\" [] 
  (println \"code generated via cdp\"))

 ]]>
 </clojure>
 <level1>
  <level2  accessId=\"action1\">
    <sql datasource =\"Tdat\" operation=\"output\">
      SELECT b FROM public.cdp_test WHERE a = 2;
    </sql>
     <params>
        <param id=\"param1\" default=\"default1\"/>
        <param id=\"param2\" default=\"default2\"/>
     </params>
   </level2>
 </level1>
</level0>
")




(def simpleXML "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<level0>
 <level1 para=\"1a\" parb=\"1b\">
  <level2 para=\"2a\" parb=\"2b\">
   content-level2-line-a
   content-level2-line-b
   </level2>
   content-level1-line-a
   content-level1-line-b
 </level1>
<level1 para=\"1a\" parb=\"1b-different\">
</level1>
</level0>")




(defn string-to-bytestream "Convert a string to a bytestream."
  [xml]
  {:pre [(string? xml)]}
  (java.io.ByteArrayInputStream. (.getBytes (.trim xml))))


(defn xml-parse-string "clojure.xml/parse interprets strings as a file/URI,
   so in order to parse a string it needs to be converted to a stream."
   [xml]
  {:pre [(string? xml)]}
;;  (xml/parse (convert-string-to-stream xml)))
  (xml/parse (string-to-bytestream xml)))



(deftest test-basic_clojure-data-zip
  (println "these tests are a copy of test-basic_vinzi-data-zip"
            "\n\t however they use vinzi.data.zip/tag=   and  vinzi.data.zip/attr= "
            "\n\t (a very basic check on compatibility of both libraries, however both should be the same for basis use-cases.)")
  ;; these tests are a copy of test-basic-vinzi-data-zip
  ;; however they use clojure.data.zip/tag=   and  clojure.data.zip/attr= 
  (println "Testing the a few basic vinzi.data.zip operations")
  (let [s (zip/xml-zip (xml-parse-string simpleXML))]
    ;; match elements with :tag :level1
    (let [matches (xml-> s (xZip/tag= :level1))]
      (is (= (count matches) 2) "There should be two matches for (xZip/tag= :level1)")
      (is (empty? (remove vZip/is-zipper? matches)) "All returned matches are zippers."))
    ;; match elements with :tag :level1 and attribute :para 'a1'
    (let [matches (xml-> s (xZip/tag= :level1) (xZip/attr= :para "1a"))]
      (is (= (count matches) 2) "There should be two matches for this query (xZip/tag= :level1) (xZip/attr= :para '1a')")
      (is (empty? (remove vZip/is-zipper? matches)) "All returned matches are zippers."))
    ;; match elements with :tag :level1 and attributes {:para 'a1' :parb 'a2'}  
    (let [matches (xml-> s (xZip/tag= :level1) (xZip/attr= :para "1a") (xZip/attr= :parb "1b"))]
      (is (= (count matches) 1) "There should be one match for this query (xZip/tag= :level1) (xZip/attr= :para '1a') (xZip/attr= :parb '1b')")
      (is (empty? (remove vZip/is-zipper? matches)) "All returned matches are zippers."))
    ;; match elements with :tag :level1 and attribute :para 'a1' having a child with :tag :level2
    (let [matches (xml-> s (xZip/tag= :level1) [(xZip/attr= :para "1a") (xZip/tag= :level2)])]
      (is (= (count matches) 1) "There should be one match for this query (xZip/tag= :level1) (xZip/attr= :para '1a') (xZip/attr= :parb '1b')")
      (is (empty? (remove vZip/is-zipper? matches)) "All returned matches are zippers."))
    ))

(deftest test-basic_vinzi-data-zip
  (println "these tests are a copy of test-basic_clojure-data-zip"
            "\n\t however they use clojure.data.zip/tag=   and  clojure.data.zip/attr= "
            "\n\t (a very basic check on compatibility of both libraries, however both should be the same for basis use-cases.)")
  (println "Testing the basic vinzi.data.zip operations")
  (let [s (zip/xml-zip (xml-parse-string simpleXML))]
    ;; match elements with :tag :level1
    (let [matches (xml-> s (vZip/tag= :level1))]
      (is (= (count matches) 2) "There should be two matches for (vZip/tag= :level1)")
      (is (empty? (remove vZip/is-zipper? matches)) "All returned matches are zippers."))
    ;; match elements with :tag :level1 and attribute :para 'a1'
    (let [matches (xml-> s (vZip/tag= :level1) (vZip/attr= :para "1a"))]
      (is (= (count matches) 2) "There should be two matches for this query (vZip/tag= :level1) (vZip/attr= :para '1a')")
      (is (empty? (remove vZip/is-zipper? matches)) "All returned matches are zippers."))
    ;; match elements with :tag :level1 and attributes {:para 'a1' :parb 'a2'}  
    (let [matches (xml-> s (vZip/tag= :level1) (vZip/attr= :para "1a") (vZip/attr= :parb "1b"))]
      (is (= (count matches) 1) "There should be one match for this query (vZip/tag= :level1) (vZip/attr= :para '1a') (vZip/attr= :parb '1b')")
      (is (empty? (remove vZip/is-zipper? matches)) "All returned matches are zippers."))
    ;; match elements with :tag :level1 and attribute :para 'a1' having a child with :tag :level2
    (let [matches (xml-> s (vZip/tag= :level1) [(vZip/attr= :para "1a") (vZip/tag= :level2)])]
      (is (= (count matches) 1) "There should be one match for this query (vZip/tag= :level1) (vZip/attr= :para '1a') (vZip/attr= :parb '1b')")
      (is (empty? (remove vZip/is-zipper? matches)) "All returned matches are zippers."))
    ))

(deftest test-extended-vinzi-data-zip-1
  (println "Testing the (extended) vinzi.data.zip operations (using maps to identify attrs)")
  ;; now going to test attrs with maps
  (let [simple (zip/xml-zip (xml-parse-string simpleXML))]

    ;; match elements with :tag :level1 and attributes {:para 'a1' :parb 'a2'} 
    ;;  (alternative and more compact formulation)
    (let [matches (xml-> simple (vZip/tag= :level1) (vZip/attr= {:para "1a" :parb "1b"}))]
      (is (= (count matches) 1) "There should be one match for this query (vZip/tag= :level1) (vZip/attr= :para '1a') (vZip/attr= :parb '1b')")
      (is (empty? (remove vZip/is-zipper? matches)) "All returned matches are zippers."))

    (let [org (xml-> simple (vZip/tag= :level1) (vZip/attr= :para "1a"))
          mod (xml-> simple (vZip/tag= :level1) (vZip/attr= {:para "1a"}))]
      (assert (= (count org) 2))
      (is (= (count org) (count mod))
          "vZip/attr= returns same number when matching on map with one key-value pair")
      (is (= org mod)
          "vZip/attr= returns same result when matching on map with one key-value pair"))
    (let [org (xml-> simple (vZip/tag= :level1) (vZip/attr= :parb "1b") (vZip/attr= :para "1a"))
          mod (xml-> simple (vZip/tag= :level1) (vZip/attr= {:parb "1b" :para "1a"}))]
      (is (= (count org) (count mod))
          "vZip/attr= returns same number when matching on map with two key-value pairs(run")
      (is (= org mod)
          "vZip/attr= returns same result when matching on map with two key-value pairs"))
    )
  )



(deftest test-extended-vinzi-data-zip-2
  (println "Testing the (extended) vinzi.data.zip operations ")
  (let [actKey "action1"
        cdp  (xml-parse-string content_test0_cdp)
        act1 (xml-> (zip/xml-zip cdp) (vZip/tag= :level1))]
      (is act1 "tag :level1 obtained from cdp")
      (is (= (count act1) 1) "And it contains 1 node")
      (let [act1a (xml-> (zip/xml-zip cdp) (vZip/tagattrs= :level1))]
        (is act1a "tag :level1 obtained from cdp (via vZip/tagattrs= without attrs)")
        (is (= (count act1) 1) "And it contains 1 node")
        (is (= (:tag (zip/node (first act1))) :level1)))
     (let [act2 (xml-> (first act1) (vZip/tagattrs= :level2 :accessId actKey))]
        (is act2 (str "action " actKey "resulted in a match (in two steps)"))
        (is (= (count act2) 1) "And it contains 1 node")
        (let [act12 (xml-> (zip/xml-zip cdp) (vZip/tag= :level1) (vZip/tagattrs= :level2 :accessId actKey))]
          (is act12 (str "action " actKey "resulted in a match (in one step)"))
          (is (= (count act12) 1) "And it contains 1 node")
          (is (= (xZip/attr (first act2) :accessId) (xZip/attr (first act12) :accessId)) "one- and two-step process return same result?")
          (let [act12a (xml-> (zip/xml-zip cdp) :level1 (vZip/tagattrs= :level2 :accessId actKey))]
            (is act12a (str "action " actKey "resulted in a match (in one step + keyword-tag)"))
            (is (= (count act12) 1) "And it contains 1 node")
            (is (= (xZip/attr (first act12a) :accessId) (xZip/attr (first act12) :accessId)) "one- and two-step process return same result?")
            )))
      (let [tagFunc (fn [x] (.startsWith (str x) ":level1"))
            act1 (xml-> (zip/xml-zip cdp) (vZip/tag= tagFunc))]
        (is act1 "tag :level1 matched via a function as parameter (vZip/tag= tagFunc)")
        (is (= (count act1) 1) "And it contains 1 node"))
      (let [act1 (xml-> (zip/xml-zip cdp) (vZip/tag= #{:level1}))]
        (is act1 "tag :level1 matched via a set as parameter (vZip/tag= #{...})")
        (is (= (count act1) 1) "And it contains 1 node"))
      (let [loc (first (xml-> (zip/xml-zip cdp) :level1 :level2 :params (vZip/tagattrs= :param :id "param1")))
;;            _ (println "loc= " (zip/node loc))
            org (zip/node loc)
            path (vZip/construct-path loc)
;;            _ (println "path= " path)
            base (zip/xml-zip cdp)
            location (vZip/reconstruct-loc base path)]
        ;;        (println "reconstructed location= " location)
        (is (and location (vZip/is-zipper? location)) "Reconstructed location should return one node")
        (let [rec (zip/node location)]
          (is (= org rec) "Reconstructed path ends up at same location."))
        (let [base (zip/down base)
              location (vZip/reconstruct-loc base path)]
          (is (and location (vZip/is-zipper? location)) "Reconstructed location should return one node")
          (let [rec (zip/node location)]
            (is (= org rec) "Reconstructed path ends up at same location (base zipper is one down of root))."))
          (let [base (zip/right base)
                location (vZip/reconstruct-loc base path)]
            (is (and location (vZip/is-zipper? location)) "Reconstructed location should return one node")
            (let [rec (zip/node location)]
              (is (= org rec) "Reconstructed path ends up at same location (base zipper is one down-one left of root ))."))))))

    ;; now going to test the (replace-node ...)
  (let []
    
    )

)


(deftest test-reconstruct
  (println "Testing whether a location in zipper-tree A can be reconstructed in a different zipper-tree B"
           " as long as the path from root to node exists in both.")
  (let [simple (zip/xml-zip (xml-parse-string simpleXML))]
    
    ;; s2 located at root
    (let [s1 (zip/down simple)
          path1 (vZip/construct-path s1) 
          s2 simple
          s2a (vZip/reconstruct-loc s2 path1)]
      (is (not= (zip/node s1) (zip/node s2)) "both zippers at different location")
      (is (= (str (zip/node s1))  (str (zip/node s2a))) 
               "However, after matching location show same node.")
      )
    
    ;; s2 located below s1 before reconstruct
    (let [s1 (zip/down simple)
          path1 (vZip/construct-path s1) 
          s2 (zip/down (zip/down simple))
          s2a (vZip/reconstruct-loc s2 path1)]
      (is (not= (zip/node s1) (zip/node s2)) "both zippers at different location")
      (is (= (str (zip/node s1))  (str (zip/node s2a))) 
               "However, after matching location show same node.")
      )
    ;; s2 located ro the right of s1 before reconstruct
    (let [s1 (zip/down simple)
          path1 (vZip/construct-path s1) 
          s2 (zip/right (zip/down simple))
          s2a (vZip/reconstruct-loc s2 path1)]
      (is (not= (zip/node s1) (zip/node s2)) "both zippers at different location")
      (is (= (str (zip/node s1))  (str (zip/node s2a))) 
               "However, after matching location show same node.")
      )
    ))
    



