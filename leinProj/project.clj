(defproject
 vinzi/vinzi.data
 "0.1.0-SNAPSHOT"
 :dependencies
 [[org.clojure/clojure "1.5.1"]
  [org.clojure/tools.logging "0.2.6"]
  [org.slf4j/slf4j-api "1.6.5"]
  [ch.qos.logback/logback-core "1.0.6"]
  [ch.qos.logback/logback-classic "1.0.6"]
  [org.clojure/data.csv "0.1.2"]
  [org.clojure/data.json "0.2.2"]
  [org.clojure/data.xml "0.0.7"]
  [org.clojure/data.zip "0.1.1"]]
 :deploy-repositories
 {:releases "https://clojars.org/repo", :snapshots nil}
 :url
 "https://github.com/cvkem/vinzi.data"
 :repositories
 {"clojars" "http://clojars.org/repo",
  "Clojure Releases" "http://build.clojure.org/releases"}
 :description
 "extensions to the contrib clojure.data modules (could/should be included there)"
)
