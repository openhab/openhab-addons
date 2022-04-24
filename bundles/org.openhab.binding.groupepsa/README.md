# Groupe PSA Binding

Binding to retrieve information via the Groupe PSA Web API for cars from Opel, Peugeot, Citroen, DS and Vauxhall.

## Supported Things

Groupe PSA Web Api Bridge - The Thing to auto discover your cars
Groupe PSA Car - The actual car thing.

## Discovery

Use the "Groupe PSA Web Api bridge" to auto discover your cars. You need to select the brand for the bridge binding and only cars for the brand will be auto discovered. If you need to add for multiple brands or multiple different users, add multiple bridges.

## Binding Configuration

You need to select a brand and enter the User Name and Password.
The Polling interval (in minutes) determines how often the API will polled for new cars.
The Client ID and Client Secret should not need to be updated. (However you can register your own app via https://developer.groupe-psa.com/inc/ and use this clien tinformation if you wish.)

## Thing Configuration

The ID is the vehicle API ID (not equal to the VIN), which is autodiscoverd by the bridge.
The Polling interval (in minutes) determines how often teh car is polled for updated information.
The Online Timeout is the time in minutes after which the car is deemed to be offline.

## Channels

The channels should be mainly self explanatory. Further documentation can be found at: https://developer.groupe-psa.io/webapi/b2c/api-reference/specification/#article
