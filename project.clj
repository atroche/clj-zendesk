(defproject clj-zendesk "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-http "1.0.1"]
                 [inflections "0.9.13"]
                 [com.cemerick/url "0.1.1"]
                 [camel-snake-kebab "0.2.5" :exclusions  [org.clojure/clojure]]
                 [cheshire "5.4.0"]])
