# TeslaPowerwall Binding

This binding enables the capture of key data from a Tesla Powerwall 2 into openHAB.

## Supported Things

Tesla Powerwall 2

## Discovery

No auto discovery. The IP of the Powerwall should be added to /etc/hosts and a hostname powerwall used in the binding.

## Thing Configuration

As a minimum, the IP address is needed:

* hostname - The hostname of the Tesla Powerwall 2. Defaults to powerwall to avoid SSL certificate issues
* email - the email of the local account on the Powerwall that the installer provided
* password - the password of the local account on the Powerwall that the installer provided
* refresh - The frequency with which to refresh information from the Tesla Powerwall2 specified in seconds. Defaults to 10 seconds.

## Channels

| channel                | type           | description                                                                           |
|------------------------|----------------|---------------------------------------------------------------------------------------|
| gridstatus             | String         | Current status of the Power Grid
| batterysoe             | Number:Percent | Current battery state of charge
| mode                   | String         | Current operating mode
| reserve                | Number:Percent | Current battery reserve %
| grid_instpower         | Number:Power   | Instantaneous Grid Power Supply
| battery_instpower      | Number:Power   | Instantaneous Battery Power Supply
| home_instpower         | Number:Power   | Instantaneous Home Power Supply
| solar_instpower        | Number:Power   | Instantaneous Solar Power Supply
| grid_energyexported    | Number:Energy  | Total Grid Energy Exported
| battery_energyexported | Number:Energy  | Total Battery Energy Exported
| home_energyexported    | Number:Energy  | Total Home Energy Exported
| solar_energyexported   | Number:Energy  | Total Solar Energy Exported
| grid_energyimported    | Number:Energy  | Total Grid Energy Imported
| battery_energyimported | Number:Energy  | Total Battery Energy Imported
| home_energyimported    | Number:Energy  | Total Home Energy Imported
| solar_energyimported   | Number:Energy  | Total Solar Energy Imported

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
