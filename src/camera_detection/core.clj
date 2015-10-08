(ns camera-detection.core
  (:import [de.onvif.discovery OnfivDiscovery OnvifPointer]
           [java.util List])
  (:gen-class))

(defn discover-cameras
  "Searches for cameras on the network and returns any that were found."
  []
  (OnfivDiscovery/discoverOnvifDevices))

(defn -main
  "Discovers cameras using onvif."
  [& args]
  (let [found-cameras (discover-cameras)]
    (println "is empty? " (.isEmpty found-cameras))
    (println "# items?  " (.size found-cameras))
    (if (> (.size found-cameras) 0)
      (do
        (println "faddr?  " (.getAddress (.get found-cameras 0)))
        (println "fname?  " (.getName (.get found-cameras 0)))
        (println "fsnap?  " (.getSnapshotUrl (.get found-cameras 0)))
        (println "fstr?   " (.toString (.get found-cameras 0))))
      (println "try again"))))
