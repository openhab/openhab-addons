## Introduction

The open Home Automation Bus (openHAB) project aims at providing a universal integration platform for all things around home automation. It is a pure Java solution, fully based on OSGi. The Equinox OSGi runtime and Jetty as a web server build the core foundation of the runtime.

It is designed to be absolutely vendor-neutral as well as hardware/protocol-agnostic. openHAB brings together different bus systems, hardware devices and interface protocols by dedicated bindings. These bindings send and receive commands and status updates on the openHAB event bus. This concept allows designing user interfaces with a unique look&feel, but with the possibility to operate devices based on a big number of different technologies. Besides the user interfaces, it also brings the power of automation logics across different system boundaries.

For further Information please refer to our homepage [http://www.openhab.org](http://www.openhab.org). 

## openHAB 1 vs. openHAB 2

So far the "productive" openHAB code is at http://github.com/openhab/openhab.
This project here is the home of the next-generation openHAB aka openHAB 2.
A major focus of openHAB 2 is adding administration UIs to the system to make it more user-friendly for "regular" users (i.e. not the developer type). For this, openHAB 2 is now based on the [Eclipse SmartHome](http://www.eclipse.org/smarthome) project. You can find information about the relation of these projects in [this blogpost](http://kaikreuzer.blogspot.de/2014/06/openhab-20-and-eclipse-smarthome.html).
There is a configuration UI prototype already available, called [Paper UI](https://www.youtube.com/watch?v=NolVoL8ewO0&feature=youtu.be).

openHAB 2 is currently under development and so far there is only an [alpha release available](https://github.com/openhab/openhab2/releases/tag/2.0.0-alpha2). Additionally, we have snapshot builds [available at CloudBees](https://openhab.ci.cloudbees.com/job/openHAB2/). openHAB 2.0 includes a [1.x compatibility layer](docs/sources/installation/compatibilitylayer.md), which allows using 1.x add-ons with the new runtime.

If you want to start playing openHAB 2.0, please [read here about the runtime and its changes to version 1.x](docs/sources/intro.md). For some background information, you can also check out [this blog post](http://kaikreuzer.blogspot.de/2014/11/openhab-16-and-20-alpha-release.html) and [this one](http://kaikreuzer.blogspot.de/2015/05/openhab-17-and-20-alpha-2-release.html).

## Community: How to get involved

As any good open source project, openHAB welcomes community participation in the project. Read more in the [how to contribute](CONTRIBUTING.md) guide.

If you are a developer and want to jump right into the sources and execute openHAB 2 from within Eclipse, please have a look at the [IDE setup](docs/sources/development/ide.md) procedures.

If you are not afraid of work in progress, you can have a learn [how openHAB 2 bindings are developed](docs/sources/development/bindings.md). These concepts and the APIs are not yet finalized, so please do not expect much support from us on this and do not be surprised if these things change over time. If you prefer something stable, openHAB 1.x is definitely the place to be for the moment!

In case of problems or questions, please refer to the [openHAB 2 issue tracker](https://github.com/openhab/openhab2/issues?page=1&state=open).

## Trademark Disclaimer

Product names, logos, brands and other trademarks referred to within the openHAB website are the property of their respective trademark holders. These trademark holders are not affiliated with openHAB or our website. They do not sponsor or endorse our materials.
