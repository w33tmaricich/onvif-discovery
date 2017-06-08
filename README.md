# camera-detection

camera-detection is a simple application that searches for onvif compatable
devices on the network you are connected to. In order to function properly you
must be connected to some local network and have mongodb running on your system.

## Installation
1. Install java and leiningen on your system.
2. Download from http://52.21.135.231/stream-manager/camera-discovery.git
3. cd to the downloaded directory and enter the command "lein uberjar"
4. Run the jar as described below.

## Usage
To run the application, simply run the command below.

    $ java -jar camera-detection-1.0.0-standalone.jar

## Output?
Once the application has run successfully, in order to retrieve your devices,
you must pull the data from mongo. The information is stored as follows:
 - Database = camera-detection
 - Collection = discovered-cameras

Each device has three pieces of information associated with it:
 - :\_id = :uri md5 hash.
 - :ip = IP address of the device.
 - :uri = Uniform Resource Identifier for the stream.

## License
Alexander Maricich © 2015
