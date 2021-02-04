# modbus.SolaxX3Mic Binding

This binding has been developed to handle Modbus communication via RS-458 port of Solax X3 Mic PhotoVoltaic Inverter.
Handles collection of all important information about inverters state.

It has been found that every inverter has own map of registers and really small amount of available inverters are following SunSpec standard.
It has been also found that nearly all inverters are publishing the same set of data, but in different order.
So I have doesn't hardcoded modbus register map inside this binding, but I have put it into thing and channel configuration.

## Supported Things

It has been designed as highly configurable, so at this moment it is confirmed that after additional configuration it is working for Growatt TL3-S Photovoltaic Inverter as well.

Generally binding should be able to handle any one and two channels Inverter what publishes modbus data in a single continuous block of register shorter than 64 registers.

Moreover, this binding should be really easy to extend to other devices with single block of registers, because it is required only to edit thing description xml files from resources\OH-INF folder to transform it to completelly different binding. 
Java code is written that way, that is scans all defined and mapped to items channels and maps received modbus registers block to them dynamically.

## Discovery

Because of flexibility of this binding discovery is not supported.

## Binding Configuration

Binding does not requires any special configuration.

## Thing Configuration

Thing configuration is necessary in configuration file because of extended amount of information required by thing.

Basically this thing requires Bridge of type Modbus TCP or Modbus Serial.
It also requires definition of following parameters:
1. refresh - amount of time between polls [s]
2. inputAddress - begin of modbus type 3 registers block
3. inputBlockLength - length of modbus type 3 registers block
4. maxTries - maximal amount of tries during failure condition
5. holdingAddress - begin of modbus type 4 registers block
6. holdingBlockLength - length of modbus type 4 registers block


## Channels

Channels are splitted into several groups.
All channels are read only.
Not all channels are required to use.

Groups:
1. Device information
2. AC Line General
3. AC Phases A, B, C as separate Groups
4. DC Lines 1 and 2 as separate Groups

Channels in every group type:

1. Device Information (device-information)
    	a. status (status-type)
		b. cabinet-temperature (cabinet-temperature-type)
		c. heatsink-temperature(heatsink-temperature-type)
2. AC Summary (ac-general)
    	a. ac-power (ac-power-type)
		b. ac-dayly-energy (ac-dayly-energy-type)
		c. ac-lifetime-energy (ac-lifetime-energy-type)
3. AC Phase (ac-phase)
    	a. ac-phase-voltage-to-n (ac-phase-voltage-to-n-type)
		b. ac-phase-current (ac-phase-current-type)
		c. ac-phase-frequency (ac-phase-frequency-type)
		d. ac-phase-power (ac-power-type)
4. DC summary (dc-general )
    	a. dc-current (dc-current-type)
		b. dc-voltage (dc-voltage-type)
		c. dc-power (dc-power-type)

Every channel configuration consists of five parameters:
1. registerFunction - identification of block where register resides.
   Possible are:
   3 - Input register
   4 - Holding register
2. registerNumber - number of register inside of block
3. registerType - type of register:
   INT
   INT_BIGENDIAN
   SHORT
   USHORT
   STATUS
4. registerUnit - unit of value in register. Possible are all names from org.openhab.core.library.unit.Units constants. It is required to use exact names with capital Letters only. It is not verified at this moment, but when coversion fails, there is an exception thrown and message to logs is written: "Illegal access exception during reflection to Units!"
5. registerScaleFactor - power of then that register's value should be multiplied. Eg. Daily power produced by inverter is given in Watt*Hours, and we have channel with KILO Watt*Hours, so there is need to divide register value by 1000. It meams registerScaleFactor is -3.


## Full Example

SolaxX3Mic.things
```
Bridge modbus:tcp:Solax-X3-Mic-TCP "Solax X3 Mic TCP Connection" @ "HeatingRoom"[ host="10.0.0.100", port=502, id=1, enableDiscovery=false ]
Thing  modbus:inverter-solax-x3-mic:Solax-X3-Mic "Solax X3 Mic Inverter" (modbus:tcp:Solax-X3-Mic-TCP) @ "Kanciapa" [ refresh=2, inputAddress=1024, inputBlockLength=53, maxTries=3, holdingAddress=769, holdingBlockLength=59 ] {
        Channels:
                Type cabinet-temperature-type: deviceInformation#cabinet-temperature [
                        registerNumber=13,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="KELVIN",
                        registerScaleFactor=0
                ]
                Type heatsink-temperature-type: deviceInformation#heatsink-temperature [
                        registerNumber=51,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="KELVIN",
                        registerScaleFactor=0
                ]
                Type status-type: deviceInformation#status [
                        registerNumber=16,
                        registerFunction=3,
                        registerType="STATUS",
                        registerUnit="KELVIN",
                        registerScaleFactor=0
                ]
                Type ac-power-type: acGeneral#ac-power [
                        registerNumber=14,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="WATT",
                        registerScaleFactor=0
                ]
                Type ac-dayly-energy-type: acGeneral#ac-dayly-energy [
                        registerNumber=37,
                        registerFunction=3,
                        registerType="INT",
                        registerUnit="KILOWATT_HOUR",
                        registerScaleFactor=-3
                ]
                Type ac-lifetime-energy-type: acGeneral#ac-lifetime-energy [
                        registerNumber=35,
                        registerFunction=3,
                        registerType="INT",
                        registerUnit="KILOWATT_HOUR",
                        registerScaleFactor=-3
                ]
                Type ac-phase-voltage-to-n-type: acPhaseA#ac-phase-voltage-to-n [
                        registerNumber=4,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="VOLT",
                        registerScaleFactor=-1
                ]
                Type ac-phase-current-type: acPhaseA#ac-phase-current [
                        registerNumber=10,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="AMPERE",
                        registerScaleFactor=-1
                ]
                Type ac-phase-frequency-type: acPhaseA#ac-phase-frequency [
                        registerNumber=7,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="HERTZ",
                        registerScaleFactor=-2
                ]
                Type ac-phase-power-type: acPhaseA#ac-phase-power [
                        registerNumber=16,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="WATT",
                        registerScaleFactor=0
                ]
                Type ac-phase-voltage-to-n-type: acPhaseB#ac-phase-voltage-to-n [
                        registerNumber=5,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="VOLT",
                        registerScaleFactor=-1
                ]
                Type ac-phase-current-type: acPhaseB#ac-phase-current [
                        registerNumber=11,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="AMPERE",
                        registerScaleFactor=-1
                ]
                Type ac-phase-frequency-type: acPhaseB#ac-phase-frequency [
                        registerNumber=8,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="HERTZ",
                        registerScaleFactor=-2
                ]
                Type ac-phase-power-type: acPhaseB#ac-phase-power [
                        registerNumber=17,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="WATT",
                        registerScaleFactor=0
                ]
                Type ac-phase-voltage-to-n-type: acPhaseC#ac-phase-voltage-to-n [
                        registerNumber=6,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="VOLT",
                        registerScaleFactor=-1
                ]
                Type ac-phase-current-type: acPhaseC#ac-phase-current [
                        registerNumber=12,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="AMPERE",
                        registerScaleFactor=-1
                ]
                Type ac-phase-frequency-type: acPhaseC#ac-phase-frequency [
                        registerNumber=9,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="HERTZ",
                        registerScaleFactor=-2
                ]
                Type ac-phase-power-type: acPhaseC#ac-phase-power [
                        registerNumber=18,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="WATT",
                        registerScaleFactor=0
                ]
                Type dc-current-type: dcGeneral1#dc-current [
                        registerNumber=2,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="AMPERE",
                        registerScaleFactor=-1
                ]
                Type dc-voltage-type: dcGeneral1#dc-voltage [
                        registerNumber=0,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="VOLT",
                        registerScaleFactor=-1
                ]
                Type dc-power-type: dcGeneral1#dc-power [
                        registerNumber=19,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="WATT",
                        registerScaleFactor=0
                ]
                Type dc-current-type: dcGeneral2#dc-current [
                        registerNumber=3,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="AMPERE",
                        registerScaleFactor=-1
                ]
                Type dc-voltage-type: dcGeneral2#dc-voltage [
                        registerNumber=1,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="VOLT",
                        registerScaleFactor=-1
                ]
                Type dc-power-type: dcGeneral2#dc-power [
                        registerNumber=21,
                        registerFunction=3,
                        registerType="SHORT",
                        registerUnit="WATT",
                        registerScaleFactor=0
                ]
}
```

SolaxX3Mic.items
```
Group House
Group Sensors_Solax_X3_Mic
Number modbus_inverter_Solax_X3_Mic_deviceInformation_cabinet_temperature "Cabinet Temperature [%.1f °C]" <temperature> (Sensors_Solax_X3_Mic) {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:deviceInformation#cabinet-temperature" }
Number modbus_inverter_Solax_X3_Mic_deviceInformation_heatsink_temperature "Heat Sink Temperature [%.1f °C]" <temperature> (Sensors_Solax_X3_Mic) {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:deviceInformation#heatsink-temperature" }
Number modbus_inverter_Solax_X3_Mic_acGeneral_ac_power "AC Output Power [%.1f W]" <power> (Sensors_Solax_X3_Mic) {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:acGeneral#ac-power" }
Number modbus_inverter_Solax_X3_Mic_acGeneral_ac_dayly_energy "AC Dayly Energy [%.1f kWh]" <energy> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:acGeneral#ac-dayly-energy" }
Number modbus_inverter_Solax_X3_Mic_acGeneral_ac_lifetime_energy "AC Lifetime Energy [%.1f kWh]" <energy> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:acGeneral#ac-lifetime-energy" }
Number modbus_inverter_Solax_X3_Mic_acPhaseA_ac_phase_voltage_to_n "AC Phase A Voltage [%.1f V]" <voltage> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:acPhaseA#ac-phase-voltage-to-n" }
Number modbus_inverter_Solax_X3_Mic_acPhaseA_ac_phase_current "AC Phase A Current [%.1f A]" <current> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:acPhaseA#ac-phase-current" }
Number modbus_inverter_Solax_X3_Mic_acPhaseA_ac_phase_frequency "AC Phase A Frequency [%.1f Hz]" <frequency> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:acPhaseA#ac-phase-frequency" }
Number modbus_inverter_Solax_X3_Mic_acPhaseA_ac_phase_power "AC Phase A Power [%.1f W]" <power> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:acPhaseA#ac-phase-power" }
Number modbus_inverter_Solax_X3_Mic_acPhaseB_ac_phase_voltage_to_n "AC Phase B Voltage [%.1f V]" <voltage> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:acPhaseB#ac-phase-voltage-to-n" }
Number modbus_inverter_Solax_X3_Mic_acPhaseB_ac_phase_current "AC Phase B Current [%.1f A]" <current> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:acPhaseB#ac-phase-current" }
Number modbus_inverter_Solax_X3_Mic_acPhaseB_ac_phase_frequency "AC Phase B Frequency [%.1f Hz]" <frequency> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:acPhaseB#ac-phase-frequency" }
Number modbus_inverter_Solax_X3_Mic_acPhaseB_ac_phase_power "AC Phase B Power [%.1f W]" <power> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:acPhaseB#ac-phase-power" }
Number modbus_inverter_Solax_X3_Mic_acPhaseC_ac_phase_voltage_to_n "AC Phase C Voltage [%.1f V]" <voltage> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:acPhaseC#ac-phase-voltage-to-n" }
Number modbus_inverter_Solax_X3_Mic_acPhaseC_ac_phase_current "AC Phase C Current [%.1f A]" <current> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:acPhaseC#ac-phase-current" }
Number modbus_inverter_Solax_X3_Mic_acPhaseC_ac_phase_frequency "AC Phase C Frequency [%.1f Hz]" <frequency> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:acPhaseC#ac-phase-frequency" }
Number modbus_inverter_Solax_X3_Mic_acPhaseC_ac_phase_power "AC Phase C Power [%.1f W]" <power> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:acPhaseC#ac-phase-power" }
Number modbus_inverter_Solax_X3_Mic_dcGeneral2_dc_current "DC Chain 2 Current [%.1f A]" <current> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:dcGeneral2#dc-current" }
Number modbus_inverter_Solax_X3_Mic_dcGeneral2_dc_voltage "DC Chain 2 Voltage [%.1f V]" <voltage> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:dcGeneral2#dc-voltage" }
Number modbus_inverter_Solax_X3_Mic_dcGeneral2_dc_power "DC Chain 2 Power [%.1f W]" <power> [Sensors_Solax_X3_Mic] {channel="modbus:inverter-solax-x3-mic:Solax-X3-Mic:dcGeneral2#dc-power" }
```