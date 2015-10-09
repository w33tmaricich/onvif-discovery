(ns camera-detection.core
  (:import [de.onvif.discovery OnfivDiscovery OnvifPointer]
           [de.onvif.soap OnvifDevice]
           [de.onvif.soap.devices InitialDevices MediaDevices]
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

(defn print-onvifdevice
  "Prints key onvifdevice information."
  [onvif-device]
  (if (not (nil? onvif-device))
    (do
      (println "| OnvifDevice:" onvif-device)
      (println "| ===")
      (println "| Device is online.")
      (println "| Username:" (.getUsername onvif-device))
      (println "| Time:    " (.getUTCTime onvif-device))
      (println "| Date:    " (.getDate onvif-device))
      (println "| Soap:    " (.getSoap onvif-device))
      (println "| Devices: " (.getXAddr (.getMedia (.getCapabilities (.getDevices onvif-device)))))
      (println "| Main URI:" (.getDeviceUri onvif-device))
      (println "| "))
    (println "Device is onffline.")))

(defn print-mediadevice
  "Prints key mediadevice information."
  [media-device]
  (if (not (nil? media-device))
    (do
      (println "| MediaDevices:" media-device)
      (println "| ===")
    (println "No media devices."))))

(defn create-ip-list
  "Converts a list of strings to a list of ip addresses"
  [strings]
  (into [] (map #(re-find #"\d+\.\d+\.\d+\.\d+" %) strings)))

(defn discover-camera-ips
  "Discovers cameras using onvif."
  []
  (println "Searching for onvif devices...")
  (let [found-cameras (into [] (DeviceDiscovery/discoverWsDevices))
        camera-ips (create-ip-list found-cameras)]
    (if (> (count found-cameras) 0)
      (do
        (println (count camera-ips) "devices found.\n")
        camera-ips)
      (println "No onvif devices found."))))

(defn print-camera-info
  "Retrieves information from a camera with a given ip."
  [ip]
  (println)
  (println "Getting" ip "info.")
  (println "/---")
  (let [device (OnvifDevice. ip)
        i-device (InitialDevices. device)
        media-device (MediaDevices. device)]
    (print-onvifdevice device)
    (print-mediadevice media-device))
  (println "\\---")
  (println))

(defn -main
  "Not much yet"
  [& args]
  (let [ip-addresses (discover-camera-ips)]
    (loop [unchecked-ip-addresses ip-addresses
           valid-ip-addresses []]
      (if (empty? unchecked-ip-addresses)
        (println "Valid:" valid-ip-addresses)
        (do
          (try
            (print-camera-info (first unchecked-ip-addresses))
            (catch Exception e (println "...failed.\n")))
          (recur (rest unchecked-ip-addresses) valid-ip-addresses))))))
