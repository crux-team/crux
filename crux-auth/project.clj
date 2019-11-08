(defproject juxt/crux-auth "derived-from-git"
  :description "Crux authenication layer"
  :url "https://github.com/juxt/crux"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [juxt/crux-core "derived-from-git"]]
  :profiles {:dev {}}
  :middleware [leiningen.project-version/middleware])
