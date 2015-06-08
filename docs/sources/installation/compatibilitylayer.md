# Compatibility Layer for openHAB 1.x Add-ons

openHAB 2 used [Eclipse SmartHome](https://www.eclipse.org/smarthome/) as its core framework. Although many classes are similar, all of them have at least a different namespace (`org.eclipse.smarthome` instead of `org.openhab`) - as a result, none the existing 1.x add-ons would work on openHAB 2.

To still make it possible to use 1.x add-ons, there is a special bundle in openHAB 2, which serves as a compatibility layer. It effectively exposes and consumes all relevant classes and services from the `org.openhab` namespace and internally delegates or proxies them to the according `org.eclipse.smarthome` classes and services.

Currently, the compatibility layer focuses on the official APIs, i.e. an add-ons that does no dirty things should be able to run. Taking the huge number of 1.x add-ons into account, it is likely that there are a couple of problems with one or another. Some problems might be due to bugs in the compatibility bundle, others might be solvable within the add-on.

In order to find out about the current status of the compatibility, the [compatibility matrix](compatibility.md) can be consulted. Please help filling and updating this matrix (simply edit it using the edit button at the top right).

## How to use openHAB 1.x Add-ons
 
This is very straight forward: As with openHAB 1.x, simply take the jar file of your add-on and place it in the `${openhab.home}/addons` folder.
Furthermore, copy your personal `openhab.cfg` file to `${openhab.home}/conf/services/openhab.cfg`.
Now start up the runtime and touch wood.
 
## How to solve problems with a certain add-on?
 
All developers are encouraged to help on this in order to quickly make as many add-ons compatible with the openHAB 2 runtime as possible.
Assuming, you have an openHAB 1.x IDE setup with your add-on in the workspace, here is what you need to do:
 - Setup an additional [openHAB 2 IDE](../development/ide.md).
 - Import your 1.x add-on from your local openHAB 1 git clone into your workspace.
 - If it compiles, the first major step is already done. If not, try to figure out why there are compilation problems and if you cannot solve them, ask on the mailing list for help. 
 - After adding some configuration, start up the runtime through the launch configuration (make sure your bundle is activated and started by default) from within the IDE.
 - Go and test and report your findings by creating issues or pull requests for either the add-on in openHAB 1 or for the compatibility layer in openHAB 2.

## Future Plans

Our wish is to discontinue the maintenance of the openHAB 1.x runtime and the openHAB 1.x Designer in mid-term. Once the majority of openHAB 1.x add-ons works smoothly on the openHAB 2 runtime, it will be time for this step. Whether there will be a 1.8 or 1.9 runtime will depend on the progress of these efforts.  

Nonetheless, the maintenance and support of the 1.x add-ons will continue much much longer. There is absolutely no need and no rush to port them to the new 2.x APIs - especially as there are only new APIs for bindings so far, but nothing yet for actions, persistence services or other types of add-ons. Even for bindings you have to be aware that the new APIs are not yet stable and are likely to change over time. Nonetheless, if you [start a completely new binding](../development/bindings.md) for openHAB, you are encouraged to go for openHAB 2 directly - especially, if your devices can be discovered and formally described. A positive side effect of implementing a binding against the new APIs is the fact that your code is potentially automatically compatible with other Eclipse-SmartHome-based systems (of which there will be more in future).
