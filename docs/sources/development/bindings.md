# Developing a new binding for openHAB 2

This page describes the necessary steps in order to implement a new binding for openHAB 2.

_Note:_ Please note that in contrast to openHAB 1.x, openHAB 2 is based on the [Eclipse SmartHome](http://eclipse.org/smarthome/) project. So the APIs and concepts have changed, so please read this documentation carefully, if you are coming from openHAB 1.x development.

## Choosing a namespace

As a first step, you need to decide in which namespace you want to develop your binding - assuming that you want to contribute it back to the community, you have two options:

* You can choose `org.eclipse.smarthome`, if you want to directly contribute it to the Eclipse SmartHome project. The advantage of this option is that you make it available to a wider audience as your binding will also be available for other solutions than openHAB that are based on Eclipse SmartHome. The disadvantage is that the contribution process is stricter as it involves intellectual property checks and in general makes it harder or even impossible to include third-party libraries with copy-left licenses such as LGPL or code that you have written by reverse engineering some protocol.
* You can choose `org.openhab`, if you want it to be used for openHAB only. This is the better option, if your binding is not interesting for other solutions, requires special libraries or has technical dependencies on openHAB specific things (although this should be avoided as much as possible).

## Creating a skeleton

_Note:_ Here you can find a [screencast of the binding skeleton creation](http://youtu.be/30nhm0yIcvA).

For the openHAB namespace: Once you have [set up your IDE](ide.md), you can go ahead and create a skeleton for your binding. For this, go into your git repository under `<your repository>/addons/binding` and call the script `create_openhab_binding_skeleton.sh` with a single parameter, which is your binding name in camel case (e.g. 'ACMEProduct' or 'SomeSystem'). When prompted, enter your name as author and hit "Y" to start the skeleton generation.

For the Eclipse SmartHome namespace: You need to have a private fork of the Eclipse SmartHome project (https://github.com/eclipse/smarthome). In the local checkout of this git repository, go to `<your eclipse smarthome repository>/binding` and call the script `create_esh_binding_skeleton.sh` with a single parameter, which is your binding name in camel case (e.g. 'ACMEProduct' or 'SomeSystem'). When prompted, enter your name as author and hit "Y" to start the skeleton generation.

Now switch in Eclipse and choose `File->Import->General->Existing Projects into Workspace`, enter the folder of the newly created skeleton as the root directory and press "Finish".

This should give you an easy starting point for your developments. To learn about the internal structure and the concepts of a binding, please see the [Eclipse tutorial on binding development](https://github.com/eclipse/smarthome/blob/master/docs/sources/howtos/bindings.md).

## Setup and Run the Binding

To setup the binding you need to configure at least one *Thing* and link an *Item* to it. Inside the `openhabhome/conf/things` folder of the distribution project you can define and configure *Things* in file with a `*.things` extensions. The following file defines a thing for the Yahoo Weather binding:

```
yahooweather:weather:berlin     [ location="638242" ]
```

In this example a *Thing* of the *ThingType* `yahooweather:weather` is defined with a configuration for the location.

Next you need to create *Items* and link them to the *Channel* of your binding. Here is the example of the Yahoo Weather binding:

```
Number Berlin_Temperature       "Temperature in Berlin [%.1f °C]"   { channel="yahooweather:weather:berlin:temperature" }
Number Berlin_Humidity          "Humidity in Berlin [%d %%]"        { channel="yahooweather:weather:berlin:humidity" }
```

The syntax for a channel link is `{ channel = "<binding-id>:<thing-type-id>:<thing-id>:<channel-id>" }`.

If you start the openHAB runtime including the binding now (make sure that your binding is checked in the launch configuration dialog!), the code inside your `ThingHandler` implementation is executed.
