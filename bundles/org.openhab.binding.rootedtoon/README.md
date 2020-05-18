# RootedToon Binding

This binding is meant to connect your openHAB installation to a rooted Toon. It currently supports:
 * changing the setpoint (target temperature)
 * changing the program mode
 * enabling/disabling the program mode
 * reading the room temperature
 * reading boiler state/settings
 * reading smart power meter readings (electricity usage, electricity generation from e.g. solar, gas usage)

You can find how to root your Toon [here](https://www.youtube.com/watch?v=0ojFe4Q7vDs).

## Supported Things

Currently lightly tested by some forum users on [this topic](https://community.openhab.org/t/rooted-locally-accessible-toon/65958). It should work with the rooted version of Toon, having the web interface available.

## Discovery

Autodiscovery is not supported.

## Thing Configuration

The "Rooted Toon" thing has two configuration parameters: 
 * the url to access the Toon (preferably on your local network, for security reasons), in the form of `http://ip.of.your.toon` (or using a hostname of course).
 * the interval between updates, which defaults to 8 seconds (recommended, as Toon refreshes its data every 8 seconds)

## Channels

Here are some of the main channels of interest. To see all the channels, don't forget to click the "SHOW MORE" button in the Paper UI.


| channel           | type     | description                                                      |
|-------------------|----------|------------------------------------------------------------------|
| Temperature       | Number   | The measured room temperature in degrees Celcius                 |
| Setpoint          | Number   | The desired room temperature in degrees Celcius                  |
| SetpointMode      | Number   | What programmable mode is active (Comfort, Active, Sleep, Away)  |
| PowerConsumption  | Number   | Current power consumption, low and high rate combined            |
| NextSetpointTime  | DateTime | The moment when the next Setpoint will become active             |
