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

| Channel                 | Type                 | Description                                                      |
|-------------------------|----------------------|------------------------------------------------------------------|
| Temperature             | Number:Temperature   | The measured room temperature in degrees Celcius                 |
| Setpoint                | Number               | The desired room temperature in degrees Celcius                  |
| SetpointMode            | Number               | What programmable mode is active (Comfort, Active, Sleep, Away)  |
| GasMeterReading         | Number               | Gas meter reading                                                |
| GasConsumption          | Number               | Gas flow in l/h                                                  |
| PowerMeterReading       | Number               | Power consumed meter reading in Wh                               |
| PowerMeterReadingLow    | Number               | Power consumed meter reading in Wh (low)                         |
| PowerMeterFlow          | Number               | Power consumed flow in W                                         |
| PowerMeterFlowLow       | Number               | Power consumed flow in W (low)                                   |
| PowerProducedReading    | Number               | Power produced meter reading in Wh                               |
| PowerProducedReadingLow | Number               | Power produced meter reading in Wh (low)                         |
| PowerProducedFlow       | Number               | Power produced flow in W                                         |
| PowerProducedFlowLow    | Number               | Power produced flow in W (low)                                   |
| SolarPowerReading       | Number               | Solar power meter reading in Wh                                  |
| SolarPowerFlow          | Number               | Solar power flow in W                                            |
| ProgramEnabled          | Switch               | Whether the program is enabled                                   |
| PowerConsumption        | Number               | Current power consumption, low and high rate combined            |
| ModulationLevel         | Number               | Current burner modulation level                                  |
| Heating                 | Switch               | Whether the heating is on                                        |
| Tapwater                | Switch               | Whether the hot tapwater is on                                   |
| Preheat                 | Switch               | Whether the boiler is preheating                                 |
| NextSetpoint            | Number               | Numerical representation of the next Setpoint in the program     |
| NextSetpointTime        | DateTime             | The moment when the next Setpoint will become active             |
| BoilerSetpoint          | Number               |   |






















NextSetpointTime
