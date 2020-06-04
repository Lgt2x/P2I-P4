# P2I2P4
Simulating wireless sensor data processing, storage and visualization using Cisco PacketTracer and JDBC.
This repo contains two independent apps : `ServiceCapteur` receives data from Packettracer
and stores it in a MySQL database, and `Interface` is used to plot and visualize sensors data stored in DB.

Both apps are built using [Gradle](https://gradle.org/), and we recommand using [IntelliJ IDEA](https://www.jetbrains.com/idea/) as we provide build configurations.

## ServiceCapteur
This utility can be used both with PacketTracer working, receiving data from a socket (`.pkt` provided),
or it can fake data to be inserted into the database. Change `arduino = new StationPacketTracer(20001, 20002);` to `arduino = new StationFaker(5000);` if you wish to do that.

Compile using configuration `allShadowJars` and run the script using ` java -jar ServiceCapteur/build/libs/servicecapteur.jar` in a terminal window to allow Interface to run at the same time

## Interface
This script provides an nice and easy way to display data. Run it using configuration `Interface`