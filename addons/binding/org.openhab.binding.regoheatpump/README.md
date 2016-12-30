# RegoHeatPump Binding

The Rego binding supports the Rego 6xx family, used in many heat pumps, like IVT/Bosch/Autotherm/Carrier and others.

Rego 6xx units contain an interface marked as service. Header of this interface is close to the control unit. This is 5V (TTL) serial interface, it means their cable may be not longer than ~50 cm. Interface is connected by a 9 pin can/d-sub connector. Pinout of this connector is the following:

2 - RxD

3 - TxD

4 - +5V

5 - GND

Communication is allways using 19200 bps, 8 bit, no parity, 1 stop bit. This is 5V (TTL) signals, galvanic separator is highly recommended. 

##Thing configuration

In order to use the binding, two connections to the heat pump are supported:
* TCP/IP based connection
* Serial connection

The easiest way is to add the thing within the Paper UI, below are details for manual setup.

###TCP/IP connection

A transparent bridge between serial interface of the heat pump and WiFi is used. This way no additional wires are required between heat pump and computer, running openhab.

Board:
![board](https://community-openhab-org.s3-eu-central-1.amazonaws.com/optimized/2X/8/8d3037f272397d1b7448902ce355c40e0ca5f41b_1_690x312.png)

The bridge uses a TTL to RS232 (3.3V) convertor with galvanic separation plus a ESP 8266 module.
The cost of components to build the above board is ~5€. Code for transparent bridge running on ESP module can be found [here](https://github.com/crnjan/esp8266-bridge). 

Configuration of the TCP/IP thing:

 - address: the hostname/ipAddress of the transparent bridge on the local network. (mandatory)
 - tcpPort: the port number to use to connect to the transparent bridge. (optional, default to 9265)


###Serial connection 

In order to connect directly, one needs to adjust the TTL levels coming from the rego controller to RS232 used within computers, using MAX232 or similar.

Parameters:

 - portName: the name of the serial port on your computer. (mandatory)

Example:

```
regoheatpump:ipRego6xx:ivtIP [ address="192.168.2.50", tcpPort="9265" ]
regoheatpump:serialRego6xx:ivtSerial [ portName="COM3" ]
```


## Channels

| Channel Type ID        | Item Type           | Description    |
| ------------------------ |:-------------------:| --------------:|
| sensors#radiatorReturn   | Temperature         |                |
| sensors#outdoor          | Temperature         |                |
| sensors#hotWater         | Temperature         |                |
| sensors#radiatorForward  | Temperature         |                |
| sensors#indoor           | Temperature         |                |
| sensors#compressor       | Temperature         |                |
| sensors#heatFluidOut     | Temperature         |                |
| sensors#heatFluidIn      | Temperature         |                |
| sensors#coldFluidIn      | Temperature         |                |
| sensors#coldFluidOut     | Temperature         |                |
| sensors#externalHotWater | Temperature         |                |
| registers#targetValueGT1 | Temperature         |                |
| registers#onValueGT1     | Temperature
| registers#offValueGT1    | Temperature
| registers#targetValueGT3 | Temperature
| registers#onValueGT3     | Temperature
| registers#offValueGT3 	| Temperature
| registers#targetValueGT4 | Temperature
| registers#addHeatPower | Temperature
| registers#coldFluidPumpP3 | shortValue
| registers#compressor | shortValue
| registers#additionalHeat3kW | shortValue
| registers#additionalHeat6kW | shortValue
| registers#radiatorPumpP1 | shortValue
| registers#heatFluidPumpP2 | shortValue
| registers#threeWayValve | shortValue
| registers#alarm | shortValue
| registers#heatCurve | temperature
| registers#heatCurveFineAdj | Temperature
| registers#indoorTempSetting | Temperature
| registers#curveInflByInTemp | Temperature
| registers#adjCurveAt20 | Temperature
| registers#adjCurveAt15 | Temperature
| registers#adjCurveAt10 | Temperature
| registers#adjCurveAt5 | Temperature
| registers#adjCurveAt0 | Temperature
| registers#adjCurveAtMinus5 | Temperature
| registers#adjCurveAtMinus10 | Temperature
| registers#adjCurveAtMinus15 | Temperature
| registers#adjCurveAtMinus20 | Temperature
| registers#adjCurveAtMinus25 | Temperature
| registers#adjCurveAtMinus30 | Temperature
| registers#adjCurveAtMinus35 | Temperature
| registers#heatCurveCouplingDiff | Temperature
