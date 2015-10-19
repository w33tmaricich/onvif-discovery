(ns camera-detection.core
  (:import [de.onvif.discovery OnfivDiscovery OnvifPointer]
           [de.onvif.soap OnvifDevice]
           [de.onvif.soap.devices InitialDevices MediaDevices]
           [org.me.javawsdiscovery DeviceDiscovery]
           [java.util List])
  (:gen-class))

(defn device-info
  "Creates a datastructure that contains device information"
  [onvif-device]
  (if (not (nil? onvif-device))
    {:device onvif-device
     :username (.getUsername onvif-device)
     :time (.getUTCTime onvif-device)
     :date (.getDate onvif-device)
     :uri (.getDeviceUri onvif-device)}
    nil))

(defn create-ip-list
  "Converts a list of strings to a list of ip addresses"
  [strings]
  (into [] (map #(re-find #"\d+\.\d+\.\d+\.\d+" %) strings)))

(defn discover-camera-uris
  "Retrieves IP addresses of discovered cameras."
  []
  (println "Searching for onvif devices...")
  (let [found-cameras (into [] (DeviceDiscovery/discoverWsDevices))]
    (if (> (count found-cameras) 0)
      (do
        (println (count found-cameras) "devices found.\n")
        found-cameras)
      (do
        (println "No onvif devices found.")
        []))))

(defn discover-camera-ips
  "Retrieves URI's of discovered cameras."
  []
  (create-ip-list (discover-camera-uris)))

(defn camera-info
  "Retrieves information from a camera with a given ip."
  [ip]
  (try
    (let [device (OnvifDevice. ip)
          i-device (InitialDevices. device)
          media-device (MediaDevices. device)]
      (device-info device))
    (catch Exception e (println "failed"))))

(defn -main
  "Runs when the application boots."
  [& args]
  (let [found-addresses (into [] (remove #{"172.28.12.120"} (discover-camera-ips))) ; Temporary ip removal. the device hangs the script. must be resolved.
        found-info (into #{} (remove nil? (map camera-info found-addresses)))]
    (println found-info)))
