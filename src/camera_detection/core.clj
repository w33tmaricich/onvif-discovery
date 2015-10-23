(ns camera-detection.core
  (:import [de.onvif.discovery OnfivDiscovery OnvifPointer]
           [de.onvif.soap OnvifDevice]
           [de.onvif.soap.devices InitialDevices MediaDevices]
           [org.me.javawsdiscovery DeviceDiscovery]
           [java.util List])
  (:require [monger.core :as mg])
  (:gen-class))

(def mgdb-name "databaseName")
(def table-name "tableName")

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
    (let [device (OnvifDevice. ip)]
      (device-info device))
    (catch Exception e nil)))

(defn store-in-mongo
  "Stores the given information in mongo."
  [db-name table data]
  (try
    (let [conn (mg/connect)
          db (mg/get-db conn db-name)]
      (println :connection-established))
    (catch Exception e (println :connection-failed))))

(defn -main
  "Returns a list of URIs of devices found on the network."
  [& args]
  (let [uris (discover-camera-uris)
        found-data {:uris uris
                    :ips (create-ip-list uris)}]
    (if (> (count found-data) 0)
      (store-in-mongo mgdb-name table-name found-data)
      (println :no-data-found))))
