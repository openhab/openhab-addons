# Tankerkönig Binding

This binding uses the Tankerkönig API (https://www.tankerkoenig.de) for collecting gas price data of german gas stations.
So special thanks at first to the creators of Tankerkönig providing an easy way getting data from the [MTS-K]  (Markttransparenzstelle für Kraftstoffe).

## Preparation

In order to use this binding you need to prepare two things:
* Get your free Tankerkönig API key here: https://creativecommons.tankerkoenig.de/
* Search for the gas station IDs of your favourite gas stations: https://creativecommons.tankerkoenig.de/configurator/index.html
On this map you can drag the red marker to the location of your gas station. Select the gas stations and click "Tankstellen übernehmen" on the right. This will donwload a file where you can find the location IDs. For example:
a7cdd9cf-b467-4aac-8eab-d662f082511e

## How it works

The binding supports two types of things.
First of all you need the Tankerkönig Webservice which is implemented as a bridge. This bridge holds information such as the API key and the Refresh Intervall. 
To see some gas prices you need to add a Station thing, which needs to have the gas station ID you prepared in step "Preparation". Additionally you have to select the Tankerkönig Webservice (the bridge) you configured right before.

One Tankerkönig Webservice (the bridge) can hold up to 10 Station things.

When the Tankerkönig Webservice updates the data, it uses a single API request to fetch the data of all the gas stations.

## Available prices

The prices itself are represented by Channels: Diesel, E5 and E10


## Best way to set up

Since the binding should not claim the Tankerkönig API too much (and avoid a temporary ban), it's implemented that there are as few requests as possible.
The data will be updated 30 seconds after the binding initialises, every time you change something at the Webservice (bridge) (changing the Webservice will also cause the refresh intervall to restart) and of course with the Refresh Intervall.
Once setup with one Station the Webservice will continue to poll data using the selected Refresh Intervall. When adding additional Stations this schedule will be maintained, therefore it might take some time until the newly added Station gets updated.

Additionally you may select the mode Opening-Times in which only those Stations get polled which are actually open. 

   [MTS-K]: <https://www.bundeskartellamt.de/DE/Wirtschaftsbereiche/Mineral%C3%B6l/MTS-Kraftstoffe/Verbraucher/verbraucher_node.html>

