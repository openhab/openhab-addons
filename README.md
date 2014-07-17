## Introduction

The open Home Automation Bus (openHAB) project aims at providing a universal integration platform for all things around home automation. It is a pure Java solution, fully based on OSGi. The Equinox OSGi runtime and Jetty as a web server build the core foundation of the runtime.

It is designed to be absolutely vendor-neutral as well as hardware/protocol-agnostic. openHAB brings together different bus systems, hardware devices and interface protocols by dedicated bindings. These bindings send and receive commands and status updates on the openHAB event bus. This concept allows designing user interfaces with a unique look&feel, but with the possibility to operate devices based on a big number of different technologies. Besides the user interfaces, it also brings the power of automation logics across different system boundaries.

For further Information please refer to our homepage http://www.openhab.org. 

## openHAB 1 vs. openHAB 2

So far the "productive" openHAB code is at http://github.com/openhab/openhab.
This project here is the home of the next-generation openHAB aka openHAB 2.
A major focus of openHAB 2 is adding administration UIs to the system to make it more user-friendly for "regular" users (i.e. not the developer type). For this, openHAB 2 is now based on the [Eclipse SmartHome](http://www.eclipse.org/smarthome) project. You can find information about the relation of these projects in [this blogpost](kaikreuzer.blogspot.de/2014/06/openhab-20-and-eclipse-smarthome.html).

openHAB 2 is currently under heavy development and so far there is no end-user release available. Nonetheless we have snapshot builds [available at CloudBees](https://openhab.ci.cloudbees.com/job/openHAB2/) - they even include a 1.x compatibility bundle, so that a couple of openHAB 1.x addons can be used with it (we are working on further improving this compatibility layer).

## Community: How to get involved

As any good open source project, openHAB welcomes community participation in the project. Read more in the [how to contribute](CONTRIBUTING.md) guide.

If you are a developer and want to jump right into the sources and execute openHAB 2 from within Eclipse, please have a look at the [IDE setup](docs/sources/development/ide.md) procedures.

If you are not afraid of work in progress, you can have a [sneak preview on how openHAB 2 bindings are developed](docs/sources/development/bindings.md). These concepts and the APIs are still in an early stage, so please do not expect much support from us on this and do not be surprised if these things change over time. If you prefer something stable, openHAB 1.x is definitely the place to be for the moment! 

## Trademark Disclaimer

Product names, logos, brands and other trademarks referred to within the openHAB website are the property of their respective trademark holders. These trademark holders are not affiliated with openHAB or our website. They do not sponsor or endorse our materials.
