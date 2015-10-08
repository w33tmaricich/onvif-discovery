(ns camera-detection.core
  (:import [de.onvif.discovery OnfivDiscovery OnvifPointer]
           [java.util List])
  (:gen-class))

(defn discover-cameras
  "Searches for cameras on the network and returns any that were found."
  []
  (OnfivDiscovery/discoverOnvifDevices))

(defn print-onvifpointer
  "Prints all information within an OnvifPointer object"
  [op]
  (println "faddr?  " (.getAddress op))
  (println "fname?  " (.getName op))
  (println "fsnap?  " (.getSnapshotUrl op))
  (println "fstr?   " (.toString op)))


(defn -main
  "Discovers cameras using onvif."
  [& args]
  (let [found-cameras (discover-cameras)]
    (println "is empty? " (.isEmpty found-cameras))
    (println "# items?  " (.size found-cameras))
    (if (> (.size found-cameras) 0)
      (print-onvifpointer (.get found-cameras 0))
      (println "try again"))))
