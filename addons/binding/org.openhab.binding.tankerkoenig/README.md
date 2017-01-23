# Tankerkoenig Binding
---
This binding uses the tankerkoenig api (https://www.tankerkoenig.de) for collecting gas prices data of german gas stations.
So special thanks at first to the creators of tankerkoenig providing an easy way getting data from the [MTS-K]  (Markttransparentstelle für Krafstoffe).

# Preparation
---
In order to use this binding you need to prepare two things:
* Get your free tankerkoenig api key here: https://creativecommons.tankerkoenig.de/
* Search for the gas station ids of your favourite gas stations: https://creativecommons.tankerkoenig.de/configurator/index.html
On this map you can drag the red marker to the location your gas station is located. Select the gas stations and click "Tankstellen übernehmen" on the right. This will donwload a file where you can find the location ids. For example:
a7cdd9cf-b467-4aac-8eab-d662f082511e

# How it works
---
The binding supports two types of things.
First of all you need a tankerkoenig config which is implemented as a bridge. This config holds information such as the api key and the refresh intervall. 
To see some gas prices you need to add a "Tankstelle" thing, which needs to have the gas station id you prepared in step "Preparation". Additionally you have to select the config bridge you configured right before.

One tankerkoenig config (the bridge) can hold up to 10 "Tankstelle" things.

When the tankerkoenig config updates the data, it uses one api request to fetch the data of all the gas stations.

# Available prices
---
The prices itself are represented by Channels: Diesel, E5 and E10


# Best way to set up
---
Since the binding should not claim the tankerkeonig api too much (and avoid a temporary ban), it's implemented that there are as few requests as possible.
Therefore the tankerkoenig config (bridge) has an setup mode. It's turned on by default and it's preventing the binding to make a request to the tankerkoenig api. 
If you're setting up you binding it's recommended to first add all gas stations ("Tankstelle" things) and then switching the setup mode to off. Now the config will collect the data all gas stations at once.

The data will be updated 15 seconds after the binding initializing (after starting openhab), every time you change something at the config (brige) (changing the config will also cause the refresh intervall to restart) and of course with the refresh intervall.

   [MTS-K]: <https://www.bundeskartellamt.de/DE/Wirtschaftsbereiche/Mineral%C3%B6l/MTS-Kraftstoffe/Verbraucher/verbraucher_node.html>


# FAQ & troubleshooting
---

coming soon