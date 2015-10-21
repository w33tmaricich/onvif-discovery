(defproject camera-detection "0.1.0"
  :description "Discovers onvif compatable cameras."
  :url ""
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.onvif/onvif "2015.08.14"]
                 [org.onvif/onvif-sources "2015.08.14"]
                 [org.onvif/javaWsDiscovery "0.1"]
                 [commons-codec/commons-codec "1.4"]
                 [com.novemberain/monger "3.0.0-rc2"]]
  :main ^:skip-aot camera-detection.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
