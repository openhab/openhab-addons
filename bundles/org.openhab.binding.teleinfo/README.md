# Teleinfo Binding

The Teleinfo binding supports an interface to ENEDIS/ERDF [Teleinfo protocol](https://www.enedis.fr/sites/default/files/Enedis-NOI-CPT_54E.pdf) for (French) Electricity Meter. This binding works with a Teleinfo modem plugged to the I1 and I2 terminals of your electricity meter. Teleinfo modems can be ordered (see the [list of tested hardware](#tested-hardware) below) or build by yourself (see [this example](http://bernard.lefrancois.free.fr)).

Teleinfo is a protocol to read many electrical statistics of your electricity meter: instantaneous power consumption, current price period, meter reading...
These values can be used to

- send your meter reading to your electricity provider with a simple copy/paste,
- improve your rules and minimize electricity costs,
- check if your subscription is relevant for your needs,
- monitor your electricity consumption,

### Tic Mode

There are two different TIC modes, corresponding to two distinct frame formats:

- Historical TIC mode (older version)
  - Uses a serial transmission rate of 1200 baud.

- Standard TIC mode (newer version)
  - Uses a serial transmission rate of 9600 baud.
  - Provides more information from the meter.
  - Only available on Linky meters.
  - Offers a faster refresh rate.

The method for changing the TIC mode of a Linky meter is explained [here](https://forum.gce-electronics.com/t/comment-passer-un-cpt-linky-en-mode-standard/8206/7).


## Supported Things

### Historical TIC Mode

Historical TIC mode is the only mode of all telemeters before Linky models and the default mode for Linky telemeters.

The Teleinfo binding provides support for both single-phase and three-phase connection, ICC evolution and the following pricing modes:

- HCHP mode
- Base mode
- Tempo mode
- EJP mode

| Thing type                                 | Connection   | Pricing mode | ICC evolution |
|--------------------------------------------|--------------|--------------|---------------|
| cbemm_base_electricitymeter                | single-phase | Base         |               |
| cbemm_ejp_electricitymeter                 | single-phase | EJP          |               |
| cbemm_hc_electricitymeter                  | single-phase | HCHP         |               |
| cbemm_tempo_electricitymeter               | single-phase | Tempo        |               |
| cbemm_evolution_icc_base_electricitymeter  | single-phase | Base         | [x]           |
| cbemm_evolution_icc_ejp_electricitymeter   | single-phase | EJP          | [x]           |
| cbemm_evolution_icc_hc_electricitymeter    | single-phase | HCHP         | [x]           |
| cbemm_evolution_icc_tempo_electricitymeter | single-phase | Tempo        | [x]           |
| cbetm_base_electricitymeter                | three-phase  | Base         |               |
| cbetm_ejp_electricitymeter                 | three-phase  | EJP          |               |
| cbetm_hc_electricitymeter                  | three-phase  | HCHP         |               |
| cbetm_tempo_electricitymeter               | three-phase  | Tempo        |               |

### Standard TIC Mode

Linky telemeters add a new `Standard` mode with more detailed information but still provide information on the legacy format under the `Historical` denomination.

Standard mode doesn't depend on the pricing options, but it adds some useful information for electricity producers.

| Thing type                                 | Connection   | Producer mode |
|--------------------------------------------|--------------|--------------|
| lsmm_electricitymeter                      | single-phase |              |
| lsmm_prod_electricitymeter                 | single-phase | [x]          |
| lstm_electricitymeter                      | three-phase  |              |
| lstm_prod_electricitymeter                 | three-phase  | [x]          |

## Bridge

Consumption data will be retrieve directly from your meters (Linky or even older blue meters).<br/>
More information about Teleinfo protocols can be found here:  [Teleinfo protocol](https://www.enedis.fr/sites/default/files/Enedis-NOI-CPT_54E.pdf) 

To achieve this, you need to connect the Teleinfo output to your OpenHAB server.<br/>
This can be done by plugging a Teleinfo modem into the I1 and I2 terminals of your electricity meter.
There are two main ways to do this:

- Direct connection: Using a Teleinfo-to-serial modem converter (typically provides data with a granularity between 2 to 5 seconds).
- Remote connection: Using an ERL dongle put into your counter (typically provides data with a granularity of around 1 minute).


Before the binding can be used, a controller must be added. There is currently two sort of controller (serial or D2l).

Remote controller connection can use different technologies to transmit the Teleinfo frame.
I have tested it using a D2L ERL, which uses Wi-Fi technology to send the frame over a TCP/IP port.
However, other ERLs use different radio technologies, such as:

- 433 MHz transmission
- LoRa or Sigfox (long-range, low-bandwidth networks)
- KNX technology
- Zigbee technology

The binding currently supports only Wi-Fi/D2L.
Support for 433 MHz transmission may be added in the future.
KNX and Zigbee are out of scope as they have their own bindings.

This needs to be done manually. 

### Serial Bridge

Select **Teleinfo Serial Controller** and enter the serial port.

If you want to place the Teleinfo modem apart from your openHAB server, you can forward its serial messages over TCP/IP (_ser2net_).
In this case you have to define the serial port of your Teleinfo modem like this `rfc2217://ip:port`. When using _ser2net_ make sure to use _telnet_  instead of _raw_ in the _ser2net_ config file.


| Parameter                        | Meaning                                          | Possible values                     |
|----------------------------------|--------------------------------------------------|-------------------------------------|
| `serialport`                     | Path to the serial controller                    | /dev/ttyXXXX, rfc2217://ip:port     |
| `ticMode`                        | TIC mode                                         | `STANDARD`, `HISTORICAL` (default)  |
| `adco`                           | Electricity meter identifier                     | 12 digits number                    |
| `verifyChecksum`                 | If we check the checksum of the Teleinfo frame   | true, false (default=true)          |
| `autoRepairInvalidADPSgroupLine` | If we try to repair corrupted frame              | true, false (default=true)          |

### D2L Bridge

The D2L bridge will open a TCP port, listen on it, and wait for Teleinfo frames.
If you have multiple meters, you can use a single port for all of them.
The bridge will decode the ID of the D2L device sending the frame and dispatch it to the corresponding thing.

| Parameter                      | Sample         | Description                                                       |
|--------------------------------|----------------|-------------------------------------------------------------------|
| listenningPort                 | 7845           | The tcp port we will listen for Teleinfo frame coming from D2L    |


## Discovery

This binding provides a discovery service only for things.
Once the bridge added, electricity meters will automatically appear after starting discovery.
They meter will have a default label **[MeterType] ADCO #adco** where **#adco** is your electricity meter identifier.
For D2L-connected meters, you will need to enter the appKey and ivKey for decryption after the discovering phase by editing the discovered thing.


## Channels

### Historical TIC Mode

Channel availability depends on the electricity connection (single or three-phase) and on the pricing mode (Base, HCHP, EJP or Tempo).

| Channel  | Type                      | Description                                              | Connection | Mode  |
|----------|---------------------------|----------------------------------------------------------|--------|-------|
| isousc   | `Number:ElectricCurrent`  | Subscribed electric current                              | All    | All   |
| ptec     | `String`                  | Current pricing period                                   | All    | All   |
| imax     | `Number:ElectricCurrent`  | Maximum consumed electric current                        | Single-phase | All   |
| imax1    | `Number:ElectricCurrent`  | Maximum consumed electric current on phase 1             | Three-phase  | All   |
| imax2    | `Number:ElectricCurrent`  | Maximum consumed electric current on phase 2             | Three-phase  | All   |
| imax3    | `Number:ElectricCurrent`  | Maximum consumed electric current on phase 3             | Three-phase  | All   |
| adps     | `Number:ElectricCurrent`  | Excess electric current warning                          | Single-phase | All   |
| adir1    | `Number:ElectricCurrent`  | Excess electric current on phase 1 warning               | Three-phase  | All   |
| adir2    | `Number:ElectricCurrent`  | Excess electric current on phase 2 warning               | Three-phase  | All   |
| adir3    | `Number:ElectricCurrent`  | Excess electric current on phase 3 warning               | Three-phase  | All   |
| iinst    | `Number:ElectricCurrent`  | Instantaneous electric current                           | Single-phase | All   |
| iinst1   | `Number:ElectricCurrent`  | Instantaneous electric current on phase 1                | Three-phase  | All   |
| iinst2   | `Number:ElectricCurrent`  | Instantaneous electric current on phase 2                | Three-phase  | All   |
| iinst3   | `Number:ElectricCurrent`  | Instantaneous electric current on phase 3                | Three-phase  | All   |
| ppot     | `String`                  | Electrical potential presence                            | Three-phase  | All   |
| pmax     | `Number:Energy`           | Maximum consumed electric power on all phases            | Three-phase  | All   |
| papp     | `Number:Power`            | Instantaneous apparent power                             | Three-phase, single-phase (ICC evolution only) | All   |
| hhphc    | `String`                  | Pricing schedule group                                   | All    | HCHP  |
| hchc     | `Number:Energy`           | Total consumed energy at low rate pricing                | All    | HCHP  |
| hchp     | `Number:Energy`           | Total consumed energy at high rate pricing               | All    | HCHP  |
| base     | `Number:Energy`           | Total consumed energy                                    | All    | Base  |
| ejphn    | `Number:Energy`           | Total consumed energy at low rate pricing                | All    | EJP   |
| ejphpm   | `Number:Energy`           | Total consumed energy at high rate pricing               | All    | EJP   |
| bbrhcjb  | `Number:Energy`           | Total consumed energy at low rate pricing on blue days   | All    | Tempo |
| bbrhpjb  | `Number:Energy`           | Total consumed energy at high rate pricing on blue days  | All    | Tempo |
| bbrhcjw  | `Number:Energy`           | Total consumed energy at low rate pricing on white days  | All    | Tempo |
| bbrhpjw  | `Number:Energy`           | Total consumed energy at high rate pricing on white days | All    | Tempo |
| bbrhcjr  | `Number:Energy`           | Total consumed energy at low rate pricing on red days    | All    | Tempo |
| bbrhpjr  | `Number:Energy`           | Total consumed energy at high rate pricing on red days   | All    | Tempo |
| pejp     | `Number:Time`             | Prior notice to EJP start                                | All    | EJP   |
| demain   | `String`                  | Following day color                                      | All    | Tempo |

### Standard TIC Mode

#### Common channels

The following channels are available on all Linky telemeters in standard TIC mode.

| Channel                         		| Type                           | Description                                                                 |
|---------------------------------------|--------------------------------|-----------------------------------------------------------------------------|
| commonLSMGroup#ngtf             		| `String`                       | Provider schedule name                                                      |
| commonLSMGroup#ltarf            		| `String`                       | Current pricing label                                                       |
| commonLSMGroup#east             		| `Number:Energy`                | Total active energy withdrawn                                               |
| commonLSMGroup#easf_XX_         		| `Number:Energy`                | Active energy withdrawn from provider on index _XX, XX_ in {01,...,10}      |
| commonLSMGroup#easd_XX_         		| `Number:Energy`                | Active energy withdrawn from distributor on index _XX, XX_ in {01,...,04}   |
| commonLSMGroup#irms1            		| `Number:ElectricCurrent`       | RMS Current on phase 1                                                      |
| commonLSMGroup#urms1            		| `Number:Potential`             | RMS Voltage on phase 1                                                      |
| commonLSMGroup#pref             		| `Number:Power`                 | Reference apparent power                                                    |
| commonLSMGroup#pcoup            		| `Number:Power`                 | Apparent power rupture capacity                                             |
| commonLSMGroup#sinsts           		| `Number:Power`                 | Instantaneous withdrawn apparent power                                      |
| commonLSMGroup#smaxsn           		| `Number:Power`                 | Maximum withdrawn apparent power of the day                                 |
| commonLSMGroup#smaxsnMinus1    		| `Number:Power`                 | Maximum withdrawn apparent power of the previous day                        |
| commonLSMGroup#ccasn            		| `Number:Power`                 | Active charge point N                                                       |
| commonLSMGroup#ccasnMinus1      		| `Number:Power`                 | Active charge point N-1                                                     |
| commonLSMGroup#umoy1            		| `Number:Potential`             | Mean Voltage on phase 1                                                     |
| commonLSMGroup#dpm_X_           		| `String`                       | Start of mobile peak period _X, X_ in {1,2,3}                               |
| commonLSMGroup#fpm_X_           		| `String`                       | End of mobile peak period _X, X_ in {1,2,3}                                 |
| commonLSMGroup#msg1             		| `String`                       | Short message                                                               |
| commonLSMGroup#msg2             		| `String`                       | Very short message                                                          |
| commonLSMGroup#ntarf            		| `String`                       | Index of current pricing                                                    |
| commonLSMGroup#njourf           		| `String`                       | Number of current provider schedule                                         |
| commonLSMGroup#njourfPlus1      		| `String`                       | Number of next day provider schedule                                        |
| commonLSMGroup#pjourfPlus1      		| `String`                       | Profile of next day provider schedule                                       |
| commonLSMGroup#ppointe          		| `String`                       | Profile of next rush day                                                    |
| commonLSMGroup#date             		| `DateTime`                     | Date and Time                                                               |
| commonLSMGroup#smaxsnDate       		| `DateTime`                     | Timestamp of SMAXSN value                                                   |
| commonLSMGroup#smaxsnMinus1Date 		| `DateTime`                     | Timestamp of SMAXSN-1 value                                                 |
| commonLSMGroup#ccasnDate        		| `DateTime`                     | Timestamp of CCASN value                                                    |
| commonLSMGroup#ccasnMinus1Date  		| `DateTime`                     | Timestamp of CCASN-1 value                                                  |
| commonLSMGroup#umoy1Date        		| `DateTime`                     | Timestamp of UMOY1 value                                                    |
| commonLSMGroup#dpm_X_Date     		| `DateTime`                     | Date of DPM_X_, _X_ in {1,2,3}                                              |
| commonLSMGroup#fpm_X_Date      		| `DateTime`                     | Date of FPM_X_, _X_ in {1,2,3}                                              |
| commonLSMGroup#relais_X_        		| `Switch`                       | state of relais _X, X_ in {1,...,8}                                         |
| commonLSMGroup#irms1f                 | `current`                      | Floating value for Irms1                                                          |
| commonLSMGroup#cosphi                 | `powerFactor`                  | Channel to feed external cosPhi calculation                                       |
| commonLSMGroup#sactive                | `power`                    	 | Active power calculate from apparent power and Cosphi                             |
| commonLSMGroup#sreactive              | `power`              	     	 | Reactive power calculate from apparent power and Cosphi                           |
| commonLSMGroup#contac-sec             | `contact`                  	 | Stge decode : contact Sec Value                                                   |
| commonLSMGroup#cut-off                | `cutoff`                   	 | Stge decode :  of cutoff                                                      |
| commonLSMGroup#cache                  | `contact`                  	 | Stge decode : linky cache state                                                   |
| commonLSMGroup#over-voltage           | `over-voltage-state`       	 | Stge decode : overvoltage state                                               |
| commonLSMGroup#exceeding-power        | `exceeding-power-state`    	 | Stge decode : exceding power state                                            |
| commonLSMGroup#function               | `function`                 	 | Stge decode : function                                                        |
| commonLSMGroup#direction              | `direction`                	 | Stge decode : direction                                                       |
| commonLSMGroup#supplier-rate          | `rate`                     	 | Stge decode : supplier rate index                                                 |
| commonLSMGroup#distributor-rate       | `rate`                     	 | Stge decode : distributor rate index                                              |
| commonLSMGroup#clock                  | `contact`                  	 | Stge decode : clock state                                                         |
| commonLSMGroup#plc                    | `plc`                      	 | Stge decode : PLC                                                             |
| commonLSMGroup#outputcom              | `outputcomState`            	 | Stge decode : Output com state                                                |
| commonLSMGroup#plc-state              | `plcState`                 	 | Stge decode : PLC state                                                       |
| commonLSMGroup#plc-synchro            | `synchroPlcState`         	 | Stge decode : PLC Synchro state                                               |
| commonLSMGroup#tempo-today            | `tempo`                    	 | Stge decode : Today tempo color                                                   |
| commonLSMGroup#tempo-tomorrow         | `tempo`                    	 | Stge decode : Tomorrow tempo color                                                |
| commonLSMGroup#advice-moving-tips     | `movingTips`               	 | Stge decode : Advice of moving tips                                           |
| commonLSMGroup#moving-tips            | `movingTips`               	 | Stge decode : Current moving tips                                             |
| commonLSMGroup#pjourf1-plus1          | `String`                   	 | Pjourf decode : Slot 1                                                            |
| commonLSMGroup#pjourf2-plus1          | `String`                   	 | Pjourf decode : Slot 2                                                            |
| commonLSMGroup#pjourf3-plus1          | `String`                   	 | Pjourf decode : Slot 3                                                            |
| commonLSMGroup#pjourf4-plus1          | `String`                   	 | Pjourf decode : Slot 4                                                            |
| commonLSMGroup#pjourf5-plus1          | `String`                   	 | Pjourf decode : Slot 5                                                            |
| commonLSMGroup#pjourf6-plus1          | `String`                   	 | Pjourf decode : Slot 6                                                            |
| commonLSMGroup#pjourf7-plus1          | `String`                   	 | Pjourf decode : Slot 7                                                            |
| commonLSMGroup#pjourf8-plus1          | `String`                   	 | Pjourf decode : Slot 8                                                            |
| commonLSMGroup#ppointe1               | `String`                   	 | PPointe decode : Slot 1                                                           |
| commonLSMGroup#ppointe2               | `String`                   	 | PPointe decode : Slot 2                                                           |
| commonLSMGroup#ppointe3               | `String`                   	 | PPointe decode : Slot 3                                                           |
| commonLSMGroup#ppointe4               | `String`                   	 | PPointe decode : Slot 4                                                           |
| commonLSMGroup#ppointe5               | `String`                   	 | PPointe decode : Slot 5                                                           |
| commonLSMGroup#ppointe6               | `String`                   	 | PPointe decode : Slot 6                                                           |
| commonLSMGroup#ppointe7               | `String`                   	 | PPointe decode : Slot 7                                                           |
| commonLSMGroup#ppointe8               | `String`                   	 | PPointe decode : Slot 8                                                           |

#### Calculated Channels

The binding also offer a number of "calculated" channels.

These channels can help decode existing data into more readable content, such as Relais, Stage State, PjourF, and PPointe advice.
Additionally, they can provide new values like irms1f, sactive, and sreactive.

- irms1f provides a more precise value of irms1, calculated as papp / urms1.
- cosphi is a specific channel designed to receive the cosphi value calculated by an external device.
- If cosphi is exposed, sactive and sreactive will provide the Active and Reactive power, respectively, derived from Apparent Power and Cosphi.

Note:<br/>
Cosphi, Active Power, and Reactive Power are not directly available on Linky meters.
Active power is particularly important as it is used to calculate consumption, which is what your supplier bills you for.


How to feed cosphi ?

You will have to create a specific channel Cosphi.
First channel will be the one you get your cosphi from.
Second one, with profile="follow" will feed the cosphi to teleinfo binding.


```java
Number											
	CompteurEDF_xxx_Cosphi
	"Teleinfo Cosphi [%s]"			
	(gTeleinfo)
  [ "Measurement" ]	     
	{ 
		channel="mqtt:topic:local:CompteurPi1:PFac_ComptGenerale",
		channel="teleinfo:lsmm_electricitymeter:myElectricityMeter:commonLSMGroup#cosphi"[profile="follow"]
  }
```


#### Three Phase Only Channels

These channels are available on the following telemeters:

- lstm_electricitymeter
- lsmt_prod_electricitymeter

| Channel                                 | Type                      | Description                                                                       |
|-----------------------------------------|---------------------------|-----------------------------------------------------------------------------------|
| threePhasedLSMGroup#irms_X_             | `Number:ElectricCurrent`  | RMS Current on phase _X, X_ in {2,3}                                              |
| threePhasedLSMGroup#urms_X_             | `Number:Potential`        | RMS Voltage on phase _X, X_ in {2,3}                                              |
| threePhasedLSMGroup#umoy_X_             | `Number:Potential`        | Mean Voltage on phase _X, X_ in {2,3}                                             |
| threePhasedLSMGroup#sinsts_X_           | `Number:Power`            | Instantaneous withdrawn apparent power on phase _X, X_ in {1,2,3}                 |
| threePhasedLSMGroup#smaxsn_X_           | `Number:Power`            | Maximum withdrawn apparent power of the day on phase _X, X_ in {1,2,3}            |
| commonLSMGroup#umoy_X_Date              | `DateTime`                | Timestamp of UMOY_X_ value, _X_ in {2,3}                                          |
| threePhasedLSMGroup#smaxsn_X_Minus1     | `Number:Power`            | Maximum withdrawn apparent power on the previous day on phase _X, X_ in {1,2,3}   |
| threePhasedLSMGroup#smaxs_X_nDate       | `DateTime`                | Timestamp of SMAXSN_X_ value, _X_ in {1,2,3}                                      |
| threePhasedLSMGroup#smaxsn_X_Minus1Date | `DateTime`                | Timestamp of SMAXSN_X_-1 value, _X_ in {1,2,3}                                    |

#### Producer Only Channels

These channels are available on the following telemeters:

- lsmm_prod_electricitymeter
- lsmt_prod_electricitymeter

| Channel                           | Type            | Description                                              |
|-----------------------------------|-----------------|----------------------------------------------------------|
| producerLSMGroup#eait             | `Number:Energy` | Total active energy injected                             |
| producerLSMGroup#erq_X_           | `Number:Energy` | Total reactive energy on index _X, X_ in {1,...,4}       |
| producerLSMGroup#sinsti           | `Number:Energy` | Instantaneous injected apparent power                    |
| producerLSMGroup#smaxin           | `Number:Power`  | Maximum injected apparent power of the day               |
| producerLSMGroup#smaxinMinus1     | `Number:Power`  | Maximum injected apparent power of the previous day      |
| producerLSMGroup#ccain            | `Number:Power`  | Injected active charge point N                           |
| producerLSMGroup#ccainMinus1      | `Number:Power`  | Injected active charge point N-1                         |
| producerLSMGroup#smaxinDate       | `DateTime`      | Timestamp of SMAXIN value                                |
| producerLSMGroup#smaxinMinus1Date | `DateTime`      | Timestamp of SMAXIN-1 value                              |
| producerLSMGroup#ccainDate        | `DateTime`      | Timestamp of CCAIN value                                 |
| producerLSMGroup#ccainMinus1Date  | `DateTime`      | Timestamp of CCAIN-1 value                               |

## Full Example

### Historical TIC Mode

The following `things` file declare a serial USB controller on `/dev/ttyUSB0` for a Single-phase Electricity meter with HC/HP option - CBEMM Evolution ICC and adco `031528042289`:

```java
Bridge teleinfo:serialcontroller:teleinfoUSB [ serialport="/dev/ttyUSB0" ]{
    Thing cbemm_evolution_icc_hc_electricitymeter myElectricityMeter [ adco="031528042289"]
}
```

`adco` is a 12-digit number written on the electricity meter (There might be two additional digits on the electricity meter, in this case the two last digits must be omitted to obtain 12 digits). The first 6 digits of `adco` can also be retrieved by pushing 6 times the `selection` button of your electricity meter, and the last 6 digits by pushing the `defilement` button.

This `items` file links some supported channels to items:

```java
Number:Power TLInfoEDF_PAPP "PAPP" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:papp"}
Number:ElectricCurrent TLInfoEDF_ISOUSC "ISOUSC" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:isousc"}
String TLInfoEDF_PTEC "PTEC" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:ptec"}
Number:ElectricCurrent TLInfoEDF_IMAX "IMAX" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:imax"}
Number:ElectricCurrent TLInfoEDF_ADPS "ADPS" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:adps"}
Number:ElectricCurrent TLInfoEDF_IINST "IINST" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:iinst"}
Number:Energy TLInfoEDF_HCHC "HCHC" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:hchc"}
Number:Energy TLInfoEDF_HCHP "HCHP" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:hchp"}
String TLInfoEDF_HHPHC "HHPHC" <energy> {channel="teleinfo:cbemm_evolution_icc_hc_electricitymeter:teleinfoUSB:myElectricityMeter:hhphc"}
```

### Standard TIC Mode

The following `things` file declare a serial USB controller on `/dev/ttyUSB0` for a Linky Single-phase Electricity meter in standard TIC mode and adsc `031528042289`:

```java
Bridge teleinfo:serialcontroller:teleinfoUSB [ serialport="/dev/ttyUSB0", ticMode="STANDARD" ]{
    Thing lsmm_electricitymeter myElectricityMeter [ adco="031528042289"]
}
```

This `items` file links some supported channels to items:

```java
Number:Power TLInfoEDF_SINSTS "SINSTS" <energy> ["Measurement","Power"] {channel="teleinfo:lsmm_electricitymeter:teleinfoUSB:myElectricityMeter:commonLSMGroup#sinsts"}
Number:ElectricCurrent TLInfoEDF_PREF "PREF" <energy> ["Measurement","Power"] {channel="teleinfo:lsmm_electricitymeter:teleinfoUSB:myElectricityMeter:commonLSMGroup#pref"}
String TLInfoEDF_LTARF "LTARF" <energy> ["Status"] {channel="teleinfo:lsmm_electricitymeter:teleinfoUSB:myElectricityMeter:commonLSMGroup#ltarf"}
Number:ElectricCurrent TLInfoEDF_SMAXSN "SMAXSN" <energy> ["Measurement","Energy"] {channel="teleinfo:lsmm_electricitymeter:teleinfoUSB:myElectricityMeter:commonLSMGroup#smaxsn"}
Number:ElectricCurrent TLInfoEDF_IRMS1 "IRMS1" <energy> ["Measurement","Current"] {channel="teleinfo:lsmm_electricitymeter:teleinfoUSB:myElectricityMeter:commonLSMGroup#irms1"}
Number:Energy TLInfoEDF_EASF01 "EASF01" <energy> ["Measurement","Energy"] {channel="teleinfo:lsmm_electricitymeter:teleinfoUSB:myElectricityMeter:commonLSMGroup#easf01"}
Number:Energy TLInfoEDF_EASF02 "EASF02" <energy> ["Measurement","Energy"] {channel="teleinfo:lsmm_electricitymeter:teleinfoUSB:myElectricityMeter:commonLSMGroup#easf02"}
String TLInfoEDF_NGTF "NGTF" <energy> ["Status"] {channel="teleinfo:lsmm_electricitymeter:teleinfoUSB:myElectricityMeter:commonLSMGroup#ngtf"}
DateTime TLInfoEDF_SMAXSN_DATE "SMAXSN_DATE" <energy> ["Measurement","Energy"] {channel="teleinfo:lsmm_electricitymeter:teleinfoUSB:myElectricityMeter:commonLSMGroup#smaxsnDate"}
```

### Standard TIC Mode With D2l

The following `things` file declare a D2L controller listenning on tcp port 7845 for a Linky Single-phase Electricity meter in standard TIC mode and adsc `031528042289`:
AppKey and ivKey will be used to decrypt the traffic.

```java
Bridge teleinfo:d2lcontroller:teleinfoD2L "D2lBridge" [ listenningPort="7845"] {
	Thing lsmm_electricitymeter myElectricityMeter [ adco="031528042289", appKey="b9c94b3d84045264e99c12903d7ff983", ivKey="19d2a8b23eba48749baf66fc5e2ff3ab", idd2l="021802000384"]
```


## Tested Hardware

The Teleinfo binding has been successfully validated with below hardware configuration:

| Serial interface | Power Energy Meter model    | Mode(s)                   | TIC mode |
|------------------|-----------------------------|---------------------------|----------|
| GCE Electronics USB Teleinfo module [(more details)](https://gce-electronics.com/fr/usb/655-module-teleinfo-usb.html) | Actaris A14C5 | Single-phase HCHP & Base | Historical |
| Cartelectronic USB Teleinfo modem [(more details)](https://www.cartelectronic.fr/teleinfo-compteur-enedis/17-teleinfo-1-compteur-usb-rail-din-3760313520028.html) | Sagem S10C4 | Single-phase HCHP | Historical |
| GCE Electronics USB Teleinfo module [(more details)](https://gce-electronics.com/fr/usb/655-module-teleinfo-usb.html) | Linky | Single-phase HCHP | Standard |
| Cartelectronic USB Teleinfo modem [(more details)](https://www.cartelectronic.fr/teleinfo-compteur-enedis/17-teleinfo-1-compteur-usb-rail-din-3760313520028.html) | Linky | Three-phase TEMPO | Standard |

You can also build a Teleinfo modem by yourself (see [this example](http://bernard.lefrancois.free.fr)).

| Wifi interface                      | Power Energy Meter model    | Mode(s)                   | TIC mode   |                                                                                     |
|-------------------------------------|-----------------------------|---------------------------|------------|-------------------------------------------------------------------------------------|
| D2L                                 | Linky                       | Single-phase TEMPO        | Standard   | [(more details)](https://eesmart.fr/modulesd2l/erl-wifi-compteur-linky/)            |


### Verify Communication

The communication can be verified using software like picocom

picocom -b 9600 -d 7 -p e -f n /dev/ttyUSB1 (for Standard mode)
picocom -b 1200 -d 7 -p e -f n /dev/ttyUSB1 (for Historical mode)

After a few seconds, you should see Linky frame displayed in your terminal

```java
ADSC    81187xxxxxx    M
VTIC    02      J
DATE    H250314152111           ;
NGTF         TEMPO              F
LTARF       HP  BLEU            +
EAST    120684765       6
EASF01  083957312       H
EASF02  031765917       J
EASF03  001219877       G
EASF04  002681581       D
EASF05  000607543       ?
EASF06  000452535       ?
EASF07  000000000       (
EASF08  000000000       )
EASF09  000000000       *
EASF10  000000000       "
EASD01  076241272       ?
EASD02  033890466       H
EASD03  003663842       B
EASD04  006889185       P
IRMS1   006     4
URMS1   233     B
PREF    12      B
PCOUP   12      \
SINSTS  01389   [
SMAXSN  H250314052302   07720   8
SMAXSN-1        H250308233739   09340   (
CCASN   H250314150000   05090   >
CCASN-1 H250314143000   06668   *
UMOY1   H250314152000   231     +
STGE    013AC401        R
MSG1    PAS DE          MESSAGE                 <
PRM     2145499yyyyyyyy  4
RELAIS  000     B
NTARF   02      O
NJOURF  00      &
NJOURF+1        00      B
PJOURF+1        00004001 06004002 16004001 NONUTILE NONUTILE NONUTILE NONUTILE NONUTILE NONUTILE NONUTILE NONUTILE      1
```
