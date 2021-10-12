# Teleinfo Binding

The Teleinfo binding supports an interface to ENEDIS/ERDF [Teleinfo protocol](https://www.enedis.fr/sites/default/files/Enedis-NOI-CPT_54E.pdf) for (French) Electricity Meter. This binding works with a Teleinfo modem plugged to the I1 and I2 terminals of your electricity meter. Teleinfo modems can be ordered (see the [list of tested hardware](#tested-hardware) below) or build by yourself (see [this example](http://bernard.lefrancois.free.fr)).

Teleinfo is a protocol to read many electrical statistics of your electricity meter: instantaneous power consumption, current price period, meter reading...
These values can be used to

- send your meter reading to your electricity provider with a simple copy/paste,
- improve your rules and minimize electricity costs,
- check if your subscription is relevant for your needs,
- monitor your electricity consumption,

## Supported Things

### Historical TIC mode

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

### Standard TIC mode

Linky telemeters add a new `Standard` mode with more detailed information but still provide information on the legacy format under the `Historical` denomination.

Standard mode doesn't depend on the pricing options, but it adds some useful information for electricity producers.

| Thing type                                 | Connection   | Producer mode |
|--------------------------------------------|--------------|--------------|
| lsmm_electricitymeter                      | single-phase |              |
| lsmm_prod_electricitymeter                 | single-phase | [x]          |
| lstm_electricitymeter                      | three-phase  |              |
| lstm_prod_electricitymeter                 | three-phase  | [x]          |

## Discovery

Before the binding can be used, a serial controller must be added. This needs to be done manually. Select __Teleinfo Serial Controller__ and enter the serial port.

If you want to place the Teleinfo modem apart from your openHAB server, you can forward its serial messages over TCP/IP (_ser2net_).
In this case you have to define the serial port of your Teleinfo modem like this `rfc2217://ip:port`. When using _ser2net_ make sure to use _telnet_  instead of _raw_ in the _ser2net_ config file.

Once the serial controller added, electricity meters will automatically appear after starting discovery, with default label __Teleinfo ADCO #adco__ where __#adco__ is  your electricity meter identifier.

## Thing Configuration

| Thing type           | Parameter    | Meaning                               | Possible values                 |
|----------------------|--------------|---------------------------------------|---------------------------------|
| `serialcontroller`   | `serialport` | Path to the serial controller         | /dev/ttyXXXX, rfc2217://ip:port |
| `*_electricitymeter` | `adco`       | Electricity meter identifier          | 12 digits number                |
|                      | `ticMode`    | TIC mode                              | `STANDARD`, `HISTORICAL` (default) |

## Channels

### Historical TIC mode

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
| pejp     | `Number:Duration`         | Prior notice to EJP start                                | All    | EJP   |
| demain   | `String`                  | Following day color                                      | All    | Tempo |

### Standard TIC mode

| Channel  | Type                      | Description                                              | Connection  | Mode  |
|----------|---------------------------|----------------------------------------------------------|--------|-------|
| ngtf     | `String`                  | Provider schedule name                                   | All    | All   |
| ltarf    | `String`                  | Current pricing label                                    | All    | All   |
| east     | `Number:Energy`           | Total active energy withdrawn                            | All    | All   |
| easf*XX* | `Number:Energy`           | Active energy withdrawn from provider on index <img src="https://render.githubusercontent.com/render/math?math=XX \in  \{01,\dots,10\}"/>  | All  | All   |
| easd*XX* | `Number:Energy`           | Active energy withdrawn from distributor on index <img src="https://render.githubusercontent.com/render/math?math=XX \in  \{01,\dots,04\}"/> | All  | All   |
| irms*X*  | `Number:ElectricCurrent`  | RMS Current on phase *X*                                 | All for <img src="https://render.githubusercontent.com/render/math?math=X=1"/>, Three-phase for <img src="https://render.githubusercontent.com/render/math?math=X\in \{2,3\}"/> | All   |
| urms*X*  | `Number:Potential`        | RMS Voltage on phase *X*                                 | All for <img src="https://render.githubusercontent.com/render/math?math=X=1"/>, Three-phase for <img src="https://render.githubusercontent.com/render/math?math=X\in \{2,3\}"/> | All   |
| pref     | `Number:Power`            | Reference apparent power                                 | All    | All   |
| pcoup    | `Number:Power`            | Apparent power rupture capacity                          | All    | All   |
| sinsts   | `Number:Power`            | Instantaneous withdrawn apparent power                   | Single-phase | All   |
| smaxsn   | `Number:Power`            | Maximum withdrawn apparent power of the day              | Single-phase | All   |
| smaxsnMinus1 | `Number:Power`        | Maximum withdrawn apparent power of the previous day     | Single-phase | All   |
| ccasn    | `Number:Power`            | Active charge point N                                    | All    | All   |
| ccasnMinus1 | `Number:Power`         | Active charge point N-1                                  | All    | All   |
| umoy*X*  | `Number:Potential`        | Mean Voltage on phase *X*                              | All for <img src="https://render.githubusercontent.com/render/math?math=X=1"/>, Three-phase for <img src="https://render.githubusercontent.com/render/math?math=X\in \{2,3\}"/> | All   |
| dpm*X*   | `String`                  | Start of mobile peak period <img src="https://render.githubusercontent.com/render/math?math=X\in  \{1,\dots,3\}"/>  | All    | All   |
| fpm*X*   | `String`                  | End of mobile peak period <img src="https://render.githubusercontent.com/render/math?math=X\in  \{1,\dots,3\}"/> | All    | All   |
| msg1     | `String`                  | Short message                                            | All    | All   |
| msg2     | `String`                  | Very short message                                       | All    | All   |
| ntarf    | `String`                  | Index of current pricing                                 | All    | All   |
| njourf   | `String`                  | Number of current provider schedule                      | All    | All   |
| njourfPlus1 | `String`               | Number of next day provider schedule                     | All    | All   |
| pjourfPlus1 | `String`               | Profile of next day provider schedule                    | All    | All   |
| ppointe  | `String`                  | Profile of next rush day                                 | All    | All   |
| date     | `DateTime`                | Date and Time                                            | All    | All   |
| smaxsnDate | `DateTime`              | Timestamp of SMAXSN value                                | All    | All   |
| smaxsnMinus1Date | `DateTime`        | Timestamp of SMAXSN-1 value                              | All    | All   |
| ccasnDate | `DateTime`               | Timestamp of CCASN value                                 | All    | All   |
| ccasnMinus1Date  | `DateTime`        | Timestamp of CCASN-1 value                               | All    | All   |
| umoy*X*Date | `DateTime`             | Timestamp of UMOY*X* value                               | All for <img src="https://render.githubusercontent.com/render/math?math=X=1"/>, Three-phase for <img src="https://render.githubusercontent.com/render/math?math=X\in \{2,3\}"/> | All   |
| dpm*X*Date  | `DateTime`             | Date of DPM*X*                                           | All    | All   |
| fpm*X*Date | `DateTime`              | Date of FPM*X*                                           | All    | All   |
| relais*X* | `Switch`                 | relais status (<img src="https://render.githubusercontent.com/render/math?math=X\in  {1,\dots,8}"/> )                   | All    | All   |
| sinsts*X* | `Number:Power`           | Instantaneous withdrawn apparent power on phase *X*    | Three-phase | All   |
| smaxsn*X*   | `Number:Power`         | Maximum withdrawn apparent power of the day on phase *X* | Three-phase | All   |
| smaxsn*X*Minus1 | `Number:Power`     | Maximum withdrawn apparent power on the previous day on phase *X* | Three-phase | All   |
| smaxs*X*nDate | `DateTime`           | Timestamp of SMAXSN*X* value                             | Three-phase | All   |
| smaxsn*X*Minus1Date | `DateTime`     | Timestamp of SMAXSN*X*-1 value                           | Three-phase | All   |
| eait     | `Number:Energy`           | Total active energy withdrawn                            | All    | All   |
| erq*X* | `Number:Energy`             | Active energy withdrawn from provider on index <img src="https://render.githubusercontent.com/render/math?math=XX \in  \{01,\dots,10\}"/>  | All  | All   |
| sinsti | `Number:Energy`             | Active energy withdrawn from distributor on index <img src="https://render.githubusercontent.com/render/math?math=XX \in  \{01,\dots,04\}"/> | All  | All   |
| smaxin | `Number:Power`              | Maximum injected apparent power of the day               | All for <img src="https://render.githubusercontent.com/render/math?math=X=1"/>, Three-phase for <img src="https://render.githubusercontent.com/render/math?math=X\in \{2,3\}"/> | All   |
| smaxinMinus1 | `Number:Power`        | Maximum injected apparent power of the previous day      | All for <img src="https://render.githubusercontent.com/render/math?math=X=1"/>, Three-phase for <img src="https://render.githubusercontent.com/render/math?math=X\in \{2,3\}"/> | All   |
| ccain    | `Number:Power`            | Injected active charge point N                           | All    | Producer |
| ccainMinus1 | `Number:Power`         | Injected active charge point N-1                         | All    | Producer |
| smaxinDate | `DateTime`              | Timestamp of SMAXIN value                                | All    | Producer |
| smaxinMinus1Date | `DateTime`        | Timestamp of SMAXIN-1 value                              | All    | Producer |
| ccainDate | `DateTime`               | Timestamp of CCAIN value                                 | All    | Producer |
| ccainMinus1Date  | `DateTime`        | Timestamp of CCAIN-1 value                               | All    | Producer |

## Full Example

### Historical TIC mode

The following `things` file declare a serial USB controller on `/dev/ttyUSB0` for a Single-phase Electricity meter with HC/HP option - CBEMM Evolution ICC and adco `031528042289` :

```
Bridge teleinfo:serialcontroller:teleinfoUSB [ serialport="/dev/ttyUSB0" ]{
    Thing cbemm_evolution_icc_hc_electricitymeter myElectricityMeter [ adco="031528042289"]
}
```

`adco` is a 12-digit number written on the electricity meter (There might be two additional digits on the electricity meter, in this case the two last digits must be omitted to obtain 12 digits). The first 6 digits of `adco` can also be retrieved by pushing 6 times the `selection` button of your electricity meter, and the last 6 digits by pushing the `defilement` button.

This `items` file links some supported channels to items:

```
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

## Tested hardware

The Teleinfo binding has been successfully validated with below hardware configuration:

| Serial interface | Power Energy Meter model    | Mode(s)                   |
|----------|--------|------------------------------|
| GCE Electronics USB Teleinfo module [(more details)](https://gce-electronics.com/fr/usb/655-module-teleinfo-usb.html) | Actaris A14C5 | Single-phase HCHP & Base |
| Cartelectronic USB Teleinfo modem [(more details)](https://www.cartelectronic.fr/teleinfo-compteur-enedis/17-teleinfo-1-compteur-usb-rail-din-3760313520028.html) | Sagem S10C4 | Single-phase HCHP |

On Linky telemeters, only *historical* TIC mode is currently supported.
The method for changing the TIC mode of a Linky telemeter is explained [here](https://forum.gce-electronics.com/t/comment-passer-un-cpt-linky-en-mode-standard/8206/7).
