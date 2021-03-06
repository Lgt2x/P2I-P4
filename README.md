# P2I2P4
Simulating wireless sensor data processing, storage and visualization leveraging Cisco PacketTracer.
This repo is organized as a gradle multi module project and contains two independent apps : `ServiceCapteur`
receives data from PacketTracer and stores it in an SQL database, and `Interface` is used to plot and
visualize sensors data stored in the database.

Both apps are built using [Gradle](https://gradle.org/), and we recommend using
[IntelliJ IDEA](https://www.jetbrains.com/idea/) as we provide build configurations in `.run` directory.

## ServiceCapteur
This utility can be used both with PacketTracer working, receiving data from a socket (`.pkt` provided),
or it can fake data to be inserted into the database. Change `arduino = new StationPacketTracer(20001, 20002);`
to `arduino = new StationFaker(5000);` if you wish to do that.

Compile using configuration `allShadowJars` and run the script using `java -jar build/libs/servicecapteur.jar`
in a terminal window to allow Interface to run at the same time.

## Interface
This script provides a nice and easy way to display data. Run it using the intellij launch configuration `Interface`
or with `java -jar build/libs/interface.jar`.

## Compile and test manually
```shell script
# Compile both apps and pack in .jar files
gradlew allShadowJars
# Run ServiceCapteur
gradlew :ServiceCapteur:run
# Run interface
gradlew :Interface:run
```
