(ns camera-detection.core
  (:import [de.onvif.discovery OnfivDiscovery OnvifPointer]
           [org.me.javawsdiscovery DeviceDiscovery]
           [java.util List])
  (:gen-class))

(defn print-onvifpointer
  "Prints all information within an OnvifPointer object"
  [op]
  (println "faddr?  " (.getAddress op))
  (println "fname?  " (.getName op))
  (println "fsnap?  " (.getSnapshotUrl op))
  (println "fstr?   " (.toString op)))

(defn create-ip-list
  "Converts a list of strings to a list of ip addresses"
  [strings]
  (into [] (map #(re-find #"\d+\.\d+\.\d+\.\d+" %) strings)))

(defn -main
  "Discovers cameras using onvif."
  [& args]
  (let [found-camera-urls (DeviceDiscovery/discoverWsDevicesAsUrls)
        found-cameras (into [] (DeviceDiscovery/discoverWsDevices))
        camera-ips (create-ip-list found-cameras)]
    (if (> (count found-camera-urls) 0)
      (do
        ; Do some debugging.
        (println "found-camera-urls")
        (println found-camera-urls)
        (println)
        (println "found-cameras")
        (println found-cameras)
        (println)
        (println "ip addresses")
        (println camera-ips))

        ;Create onvif pointers to get more info.
        ;(def pointer (OnvifPointer. (first found-camera-urls))))

      (println "No onvif devices found."))))
