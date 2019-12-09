# Kostal Inverter Binding

Scrapes the web interface of the inverter for the metrics of the supported channels below.

![Kostal Pico](doc/kostalpico.jpg)

![Kostal Piko 10-20](doc/kostalpiko10_20.jpg)

![Kostal PLENTICORE / PIKI IQ](doc/plenticore.jpg)

## Supported Things

### First generation devices (PIKO)

Tested with Kostal Inverter PIKO but might work with other inverters from Kostal too.

### Second generation devices (PIKO 10-20, PIKO NEW GENERATION)

Tested with Kostal Inverter PIKO 10-20, PIKO NEW GENERATION.

### Third generation devices (PIKO IQ / PLENTICORE plus)

This implementation was tested for the current KOSTAL PIKO PLENTICORE plus and PIKO IQ devices.
All of these devices contain the same communication board (SCB = **S**mart**C**onnection**B**oard)

Currently supported things are:

* PIKO IQ 4.2
* PIKO IQ 5.5
* PIKO IQ 7.0
* PIKO IQ 8.5
* PIKO IQ 10.0
* PLENTICORE plus 4.2 (with or without battery attached)
* PLENTICORE plus 5.5 (with or without battery attached)
* PLENTICORE plus 7.0 (with or without battery attached)
* PLENTICORE plus 8.5 (with or without battery attached)
* PLENTICORE plus 10.0 (with or without battery attached)

Others may be supported (like future devices using the same SCB or offering the same Web API, branded OEM devices, ...), but they were not tested!

## Discovery

None

## Channels

### First generation devices (PIKO)

-   acPower
-   totalEnergy
-   dayEnergy
-   status
-   str1Voltage
-   str1Current
-   str2Voltage
-   str2Current
-   l1Voltage
-   l1Power
-   l2Voltage
-   l2Power
-   l3Voltage
-   l3Power

### Second generation devices (PIKO 10-20, PIKO NEW GENERATION)

Channel Type ID                 Item Type                   Description
-   gridOutputPower             Number:Power                WATT
-   yield_Day                   Number:Power                WATT
-   yield_Total                 Number:Energy               KILOWATT_HOUR
-   operatingStatus             String
-   gridVoltageL1               Number:ElectricPotential    VOLT
-   gridCurrentL1               Number:ElectricCurrent      AMPERE
-   gridPowerL1                 Number:Power                WATT
-   gridVoltageL2               Number:ElectricPotential    VOLT
-   gridCurrentL2               Number:ElectricCurrent      AMPERE
-   gridPowerL2                 Number:Power                WATT
-   gridVoltageL3               Number:ElectricPotential    VOLT
-   gridCurrentL3               Number:ElectricCurrent      AMPERE
-   gridPowerL3                 Number:Power                WATT
-   dcPowerPV                   Number:Power                WATT
-   dc1Voltage                  Number:ElectricPotential    VOLT
-   dc1Current                  Number:ElectricCurrent      AMPERE
-   dc1Power                    Number:Power                WATT
-   dc2Voltage                  Number:ElectricPotential    VOLT
-   dc2Current                  Number:ElectricCurrent      AMPERE
-   dc2Power                    Number:Power                WATT
-   dc3Voltage                  Number:ElectricPotential    VOLT
-   dc3Current                  Number:ElectricCurrent      AMPERE
-   dc3Power                    Number:Power                WATT

-   aktHomeConsumptionSolar     Number:Power                WATT
-   aktHomeConsumptionBat       Number:Power                WATT
-   aktHomeConsumptionGrid      Number:Power                KILOWATT_HOUR
-   phaseSelHomeConsumpL1       Number:Power                WATT
-   phaseSelHomeConsumpL2       Number:Power                WATT
-   phaseSelHomeConsumpL3       Number:Power                WATT
-   gridFreq                    Number:Frequency            HERTZ
-   gridCosPhi                  Number:Angle                DEGREE_ANGLE
-   homeConsumption_Day         Number:Energy               KILOWATT_HOUR
-   ownConsumption_Day          Number:Energy               KILOWATT_HOUR
-   ownConsRate_Day             Number:Dimensionless        PERCENT
-   autonomyDegree_Day          Number:Dimensionless        PERCENT
-   homeConsumption_Total       Number:Energy               KILOWATT_HOUR
-   ownConsumption_Total        Number:Energy               KILOWATT_HOUR
-   totalOperatingTime          Number:Time                 HOUR
-   current                     Number:ElectricCurrent      AMPERE
-   currentDir                  Number:ElectricCurrent      AMPERE
-   chargeCycles                String 
-   batteryTemperature          Number:Temperature          CELCIUS
-   loginterval                 Number:Time                 MINUTE
-   s0InPulseCnt                String
-   ownConsRate_Total           Number:Dimensionless        PERCENT
-   autonomyDegree_Total        Number:Dimensionless        PERCENT
-   batteryVoltage              Number:ElectricPotential    VOLT
-   batStateOfCharge            Dimensionless               PERCENT

The following Channels are changeable

-  batteryType                  String          Battery type, Value = 1 = None, Value = 2 = PIKO Battery Li, Value = 3 = BYD B-Box HV
-  batteryUsageConsumption      String          Value = 100
-  batteryUsageStrategy         String          Value = 1 = Automatic, Value = 2 = Automatic economical             
-  smartBatteryControl          Switch          Value = False / True
-  smartBatteryControl_Text     String          Value = False / True
-  batterChargeTimeFrom         Number:Time     Battery charge time, Value = 00:00
-  batteryChargeTimeTo          Number:Time     Battery charge time, Value = 23:59
-  maxDepthOfDischarge          String          Max.depth of discharge (SoC), Value = 10
-  shadowManagement             Number          Shadow management, Value = 0 = None, Value = 1 = Shadow management + String 1 activated, Value = 2 = Shadow  management + String 2 activated, Value = 3 = Shadow management + String 1 and 2 activated
-  externalModuleControl        String          External module control, Value = 0 = True
-  inverterName                 String          Value = 'Name of inverter'


### Third generation devices (PIKO IQ / PLENTICORE plus)

| Channel Type ID                          | Item Type                | Description                                                                      | Read Write |
|------------------------------------------|--------------------------|----------------------------------------------------------------------------------|:----------:|
| deviceLocalDCPower                       | Number:Energy            | Current DC power of the inverter                                                 |      R     |
| deviceLocalHomeconsumptionFromBattery    | Number:Energy            | Current home consumption obtained from the battery                               |      R     |
| deviceLocalHomeconsumptionFromGrid       | Number:Energy            | Current home consumption obtained from the grid                                  |      R     |
| deviceLocalOwnconsumption                | Number:Energy            | Current own comsumption                                                          |      R     |
| deviceLocalHomeconsumptionFromPV         | Number:Energy            | Current home consumption obtained from photovoltaic                              |      R     |
| deviceLocalHomeconsumptionTotal          | Number:Energy            | Current total homeconsumption                                                    |      R     |
| deviceLocalLimitEVUAbsolute              | Number:Energy            | Permitted feed-in quantity as absolute value as specified by the energy supplier |      R     |
| deviceLocalLimitEVURelativ               | Number:Dimensionless     | Permitted feed-in quantity as relative value as specified by the energy supplier |      R     |
| deviceLocalWorktime                      | Number:Time              | Uptime of the inverter                                                           |      R     |
| deviceLocalACPhase1CurrentAmperage       | Number:ElectricCurrent   | Amperage of phase 1                                                              |      R     |
| deviceLocalACPhase1CurrentPower          | Number:Energy            | Power of phase 1                                                                 |      R     |
| deviceLocalACPhase1CurrentVoltage        | Number:ElectricPotential | Voltage of phase 1                                                               |      R     |
| deviceLocalACPhase2CurrentAmperage       | Number:ElectricCurrent   | Amperage of phase 2                                                              |      R     |
| deviceLocalACPhase2CurrentPower          | Number:Energy            | Power of phase 2                                                                 |      R     |
| deviceLocalACPhase2CurrentVoltage        | Number:ElectricPotential | Voltage of phase 2                                                               |      R     |
| deviceLocalACPhase3CurrentAmperage       | Number:ElectricCurrent   | Amperage of phase 3                                                              |      R     |
| deviceLocalACPhase3CurrentPower          | Number:Energy            | Power of phase 3                                                                 |      R     |
| deviceLocalACPhase3CurrentVoltage        | Number:ElectricPotential | Voltage of phase 3                                                               |      R     |
| deviceLocalACCurrentPower                | Number:Energy            | Current AC power of the inverter                                                 |      R     |
| deviceLocalBatteryLoadingCycles          | Number:Dimensionless     | Amount of loading cycles done by the battery                                     |      R     |
| deviceLocalBatteryFullChargeCapacity     | Number:ElectricCharge    | Capacity of the battery if charged fully                                         |      R     |
| deviceLocalBatteryAmperage               | Number:ElectricCurrent   | Current amperage of the battery                                                  |      R     |
| deviceLocalBatteryPower                  | Number:Energy            | Current battery charge                                                           |      R     |
| deviceLocalBatteryStageOfCharge          | Number:Dimensionless     | Current battery charge status                                                    |      R     |
| deviceLocalBatteryVoltage                | Number:ElectricPotential | Current voltage of the battery                                                   |      R     |
| deviceLocalPVString1Amperage             | Number:ElectricCurrent   | Current amperage of photovoltaic string 1                                        |      R     |
| deviceLocalPVString1Power                | Number:Energy            | Current power of photovoltaic string 1                                           |      R     |
| deviceLocalPVString1Voltage              | Number:ElectricPotential | Current voltage of photovoltaic string 1                                         |      R     |
| deviceLocalPVString2Amperage             | Number:ElectricCurrent   | Current amperage of photovoltaic string 2                                        |      R     |
| deviceLocalPVString2Power                | Number:Energy            | Current power of photovoltaic string 2                                           |      R     |
| deviceLocalPVString2Voltage              | Number:ElectricPotential | Current voltage of photovoltaic string 2                                         |      R     |
| deviceLocalPVString3Amperage             | Number:ElectricCurrent   | Current amperage of photovoltaic string 3                                        |      R     |
| deviceLocalPVString3Power                | Number:Energy            | Current power of photovoltaic string 3                                           |      R     |
| deviceLocalPVString3Voltage              | Number:ElectricPotential | Current voltage of photovoltaic string 3                                         |      R     |
| SCBEventErrorCountMc                     | Number:Dimensionless     | Number of errors reported by the main controller                                 |      R     |
| SCBEventErrorCountSFH                    | Number:Dimensionless     | Number of errors reported by the grid interface controller                       |      R     |
| SCBEventErrorCountSCB                    | Number:Dimensionless     | Number of errors reported by the smart communication board                       |      R     |
| SCBEventWarningCountSCB                  | Number:Dimensionless     | Number of warnings reported by the smart communication board                     |      R     |
| statisticAutarkyDay                      | Number:Dimensionless     | Autarky ratio of this day                                                        |      R     |
| statisticAutarkyMonth                    | Number:Dimensionless     | Autarky ratio of this month                                                      |      R     |
| statisticAutarkyTotal                    | Number:Dimensionless     | Autarky ratio overall                                                            |      R     |
| statisticAutarkyYear                     | Number:Dimensionless     | Autarky ratio of this year                                                       |      R     |
| statisticCo2SavingDay                    | Number:Mass              | Savings in Co2 emissions today                                                   |      R     |
| statisticCo2SavingMonth                  | Number:Mass              | Savings in Co2 emissions this month                                              |      R     |
| statisticCo2SavingTotal                  | Number:Mass              | Savings in Co2 emissions overall                                                 |      R     |
| statisticCo2SavingYear                   | Number:Mass              | Savings in Co2 emissions this year                                               |      R     |
| statisticHomeconsumptionDay              | Number:Energy            | Home consumption today                                                           |      R     |
| statisticHomeconsumptionMonth            | Number:Energy            | Home consumption this month                                                      |      R     |
| statisticHomeconsumptionTotal            | Number:Energy            | Home consumption overall                                                         |      R     |
| statisticHomeconsumptionYear             | Number:Energy            | Home consumption this year                                                       |      R     |
| statisticHomeconsumptionFromBatteryDay   | Number:Energy            | Home consumption obtained from the battery today                                 |      R     |
| statisticHomeconsumptionFromBatteryMonth | Number:Energy            | Home consumption obtained from the battery this month                            |      R     |
| statisticHomeconsumptionFromBatteryTotal | Number:Energy            | Home consumption obtained from the battery overall                               |      R     |
| statisticHomeconsumptionFromBatteryYear  | Number:Energy            | Home consumption obtained from the battery this year                             |      R     |
| statisticHomeconsumptionFromGridDay      | Number:Energy            | Home consumption obtained from the grid today                                    |      R     |
| statisticHomeconsumptionFromGridMonth    | Number:Energy            | Home consumption obtained from the grid this month                               |      R     |
| statisticHomeconsumptionFromGridTotal    | Number:Energy            | Home consumption obtained from the grid overall                                  |      R     |
| statisticHomeconsumptionFromGridYear     | Number:Energy            | Home consumption obtained from the grid this year                                |      R     |
| statisticHomeconsumptionFromPVDay        | Number:Energy            | Home consumption obtained from the photovoltaic plant today                      |      R     |
| statisticHomeconsumptionFromPVMonth      | Number:Energy            | Home consumption obtained from the photovoltaic plant this month                 |      R     |
| statisticHomeconsumptionFromPVTotal      | Number:Energy            | Home consumption obtained from the photovoltaic plant overall                    |      R     |
| statisticHomeconsumptionFromPVYear       | Number:Energy            | Home consumption obtained from the photovoltaic plant this year                  |      R     |
| statisticOwnconsumptionRateDay           | Number:Dimensionless     | Percentage of electricity demand covered by photovoltaics today                  |      R     |
| statisticOwnconsumptionRateMonth         | Number:Dimensionless     | Percentage of electricity demand covered by photovoltaics this month             |      R     |
| statisticOwnconsumptionRateTotal         | Number:Dimensionless     | Percentage of electricity demand covered by photovoltaics overall                |      R     |
| statisticOwnconsumptionRateYear          | Number:Dimensionless     | Percentage of electricity demand covered by photovoltaics this year              |      R     |
| statisticYieldDay                        | Number:Energy            | Yield of the photovoltaic plant today                                            |      R     |
| statisticYieldMonth                      | Number:Energy            | Yield of the photovoltaic plant this month                                       |      R     |
| statisticYieldTotal                      | Number:Energy            | Yield of the photovoltaic plant overall                                          |      R     |
| statisticYieldYear                       | Number:Energy            | Yield of the photovoltaic plant this year                                        |      R     |

## Thing Configuration

### First generation devices (PIKO)

demo.things

```
Thing kostalinverter:kostalinverter:inverter [ url="http://192.168.0.128" ]
```

If the thing goes online then the connection to the web interface is successful.
In case it is offline you should see an error message.
You optionally can define a `userName` and a `password` parameter if the access to the webinterface is protected and a desired `refreshInterval` (the time interval between updates, default 60 seconds).


### Second generation devices (PIKO 10-20, PIKO NEW GENERATION)

demo.things

```

Thing kostalinverter:kostalinverterpiko1020:inverter [ url="http://'inverter-ip'", username="'username'", password="'password'", refreshInterval=60]
```


### Third generation devices (PIKO IQ / PLENTICORE plus)

All third generation inverters require to define 3 mandatory configuration parameters:

| Parameter                | Description                                            | Type    |  Unit   | Default value | Example value |
|--------------------------|--------------------------------------------------------|---------|---------|---------------|---------------|
| url                      | Host name or IP address of your device                 | Text    | ---     | ---           | 192.168.1.2   |
| userPassword             | Password you configured on the inverters web front end | Text    | ---     | ---           | myPassword    |
| refreshInternalInSeconds | Defines how often the device is polled for new values  | Integer | Seconds | 30            | 30            |

If you are using the hostname instead of the IP address, please make sure your DNS is configuration correctly!
The refresh interval should be chosen wisely.
To small interval may led to high workload for the inverter.
It is recommended to use an interval of 30 seconds.

Full sample of thing configuration:

```
Thing kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="30"]
```

## Items

### First generation devices (PIKO)

demo.items:

```
Number:Power SolarPower "Solar power [%.1f %unit%]" <energy> { channel="kostalinverter:kostalinverter:inverter:acPower" }
Number:Energy SolarEnergyDay "Solar day energy [%.3f %unit%]" <energy> { channel="kostalinverter:kostalinverter:inverter:dayEnergy" }
Number:Energy SolarTotalEnergy "Solar total energy [%.3f %unit%]" <energy> { channel="kostalinverter:kostalinverter:inverter:totalEnergy" }
String SolarStatus "Solar status [%s]" <energy> { channel="kostalinverter:kostalinverter:inverter:status" }
```

### Second generation devices (PIKO NEW GENERATION)

demo.items:

```
Number:Power SolarPower                 "AC Power [%.2f %unit%]"                    <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:gridOutputPower" }
Number SolarPower_Max                   "Todays Maximum [%.2f %unit%]"              <energy> (gGF) 
Number SolarPower_Min                   "Todays Mimimum [%.2f %unit%]"              <energy> (gGF) 
Number SolarPowerChart                  "Chart Period Solar Power"
DateTime SolarPowerTimestamp            "Last Update AC Power [%1$ta %1$tR]"        <clock>

Number:Power SolarEnergyDay             "Day Energy [%.2f %unit%]"                  <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:yield_Day" }
Number SolarEnergyDay_Max               "Todays Maximum [%.2f %unit%]"              <energy> (gGF) 
Number SolarEnergyDay_Min               "Todays Mimimum [%.2f %unit%]"              <energy> (gGF) 
Number SolarEnergyDayChart              "Chart Period SolarEnergyDay "
DateTime SolarEnergyDayTimestamp        "Last Update Day Energy  [%1$ta %1$tR]"     <clock>

Number:Energy SolarTotalEnergy          "Total Energy [%.2f %unit%]"                <energy> (gGF) { channel="kostalinverter:kostalinverterpikon1020:inverter:yield_Total" }
Number SolarTotalEnergy_Max             "Todays Maximum [%.2f %unit%]"              <energy> (gGF) 
Number SolarTotalEnergy_Min             "Todays Mimimum [%.2f %unit%]"              <energy> (gGF) 
Number SolarTotalEnergyChart            "Chart Period SolarTotalEnergy "
DateTime SolarTotalEnergyTimestamp      "Last Update Total Energy  [%1$ta %1$tR]"   <clock>

String SolarStatus                      "Status[%s]"                                <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:operatingStatus" }
Number SolarStatus_Max                  "Todays Maximum [%s]"                       <energy> (gGF) 
Number SolarStatus_Min                  "Todays Mimimum [%s]"                       <energy> (gGF) 
Number SolarStatusChart                 "Chart Period SolarStatus "
DateTime SolarStatusTimestamp           "Last Update Solar Status  [%1$ta %1$tR]"   <clock>

Number:ElectricPotential        GridVoltageL1       "L1 Voltage[%.2f %unit%]"       <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:gridVoltageL1" }
Number:ElectricCurrent          GridCurrentL1       "L1 Current[%.2f %unit%]"       <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:gridCurrentL1" }
Number:Power                    GridPowerL1         "L1 Power[%.2f %unit%]"         <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:gridPowerL1" }
Number:ElectricPotential        GridVoltageL2       "L2 Voltage[%.2f %unit%]"       <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:gridVoltageL2" }
Number:ElectricCurrent          GridCurrentL2       "L2 Current[%.2f %unit%]"       <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:gridCurrentL2" }
Number:Power                    GridPowerL2         "L2 Power[%.2f %unit%]"         <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:gridPowerL2" }
Number:ElectricPotential        GridVoltageL3       "L3 Voltage[%.2f %unit%]"       <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:gridVoltageL3" }
Number:ElectricCurrent          GridCurrentL3       "L3 Current[%.2f %unit%]"       <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:gridCurrentL3" }
Number:Power                    GridPowerL3         "L3 Power[%.2f %unit%]"         <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:gridPowerL3" }
Number:Power                    DcPvPower           "PV Power[%.2f %unit%]"         <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:dcPowerPV" }
Number:ElectricPotential        DC1Voltage          "DC1 Voltage[%.2f %unit%]"      <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:dc1Voltage" }
Number:ElectricCurrent          DC1Current          "DC1 Current[%.2f %unit%]"      <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:dc1Current" }
Number:Power                    DC1Power            "DC1 Power[%.2f %unit%]"        <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:dc1Power" }
Number:ElectricPotential        DC2Voltage          "DC2 Voltage[%.2f %unit%]"      <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:dc2Voltage" }
Number:ElectricCurrent          DC2Current          "DC2 Current[%.2f %unit%]"      <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:dc2Current" }
Number:Power                    DC2Power            "DC2 Power[%.2f %unit%]"        <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:dc2Power" }
Number:ElectricPotential        DC3Voltage          "DC3 Voltage[%.2f %unit%]"      <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:dc3Voltage" }
Number:ElectricCurrent          Dc3Current          "DC3 Current[%.2f %unit%]"      <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:dc3Current" }
Number:Power                    DC3Power            "DC3 Power[%.2f %unit%]"        <energy> (gGF) { channel="kostalinverter:kostalinverterpiko1020:inverter:dc3Power" }

Number:Power                    AktHomeConsumptionSolar "Home Consumption Solar[%.2f %unit%]"   <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:aktHomeConsumptionSolar" }
Number:Power                    AktHomeConsumptionBat   "Home Consumption Battery[%.2f %unit%]" <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:aktHomeConsumptionBat" }
Number:Power                    AktHomeConsumptionGrid  "Home Consumption Grid[%.2f %unit%]"    <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:aktHomeConsumptionGrid" }
Number:Power                    PhaseSelHomeConsumpL1   "Home Consumption L1[%.2f %unit%]"      <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:phaseSelHomeConsumpL1" }
Number:Power                    PhaseSelHomeConsumpL2   "Home Consumption L2[%.2f %unit%]"      <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:phaseSelHomeConsumpL2" }
Number:Power                    PhaseSelHomeConsumpL3   "Home Consumption L3[%.2f %unit%]"      <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:phaseSelHomeConsumpL3" }
Number:Frequency                GridFreq                "Grid Frequency[%.2f %unit%]"           <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:gridFreq" }
Number:Angle                    GridCosPhi              "Grid Phase Shift[%.2f %unit%]"         <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:gridCosPhi" }
Number:Energy                   HomeConsumption_Day     "Home Consumption Daily[%.2f %unit%]"   <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:homeConsumption_Day" }
Number:Energy                   OwnConsumption_Day      "Own Consumption Daily[%.2f %unit%]"    <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:ownConsumption_Day" }
Number:Dimensionless            OwnConsRate             "Own Cons Rate Daily[%.2f %unit%]"      <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:ownConsRate_Day" }
Number:Dimensionless            AutonomyDegree          "Autonomy Degree Daily[%.2f %unit%]"    <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:autonomyDegree_day" }
Number:Energy                   HomeConsumption_Total   "Home Consumption Total[%.2f %unit%]"   <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:homeConsumption_Total" }
Number:Energy                   OwnConsumption_Total    "Own Consumption Total[%.2f %unit%]"    <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:ownConsumption_Total" }
Number:Time                     OperatingTime           "Operating Time Total[%.2f %unit%]"     <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:totalOperatingTime" }
Number:ElectricCurrent          Current                 "Current[%.2f %unit%]"                  <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:current" }
Number:ElectricCurrent          CurrentDir              "Current Dir[%.2f %unit%]"              <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:currentDir" }
String                          ChargeCycles            "Charge Cycles[%s]"                     <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:chargeCycles" }
Number:Temperature              Temperature             "Temperature[%.2f %unit%]"              <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:batteryTemperature" }
Number:Time                     Loginterval             "Log Interval[%.2f %unit%]"             <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:loginterval" }
String                          S0InPulseCnt            "S0 In Pulse Counter[%s]"               <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:s0InPulseCnt" }
Number:Dimensionless            OwnConsRate_Total       "Own Cons Rate Total[%.2f %unit%]"      <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:ownConsRate_Total" }
Number:Dimensionless            AutonomyDegree_Total    "Autonomy Degree Total[%.2f %unit%]"    <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:autonomyDegree_Total" }

Number:Dimensionless            ChargeLevelBattery      "Charge Level Battery[%.2f %unit%]"     <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:chargeLevelBattery" }
Number:Dimensionless            GridLimitation          "Grid Limitation[%.2f %unit%]"          <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:gridLimitation" }
Number:ElectricPotential        BatteryVoltage          "Battery Voltage[%.2f %unit%]"          <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:batteryVoltage" }
Number:Dimensionless            BatStateOfCharge        "Bat State Of Charge[%.2f %unit%]"      <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:batStateOfCharge" }

String                          BatteryType             "Battery Type[%s]"                      <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:batteryType" }
String                          BatteryUsageConsumption "Battery Usage Consumption[%s]"         <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:batteryUsageConsumption" }
String                          BatteryUsageStrategy    "Battery Usage Strategy[%s]"            <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:batteryUsageStrategy" }
Switch                          SmartBatteryControl     "Smart Battery Control[%s]"             <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:smartBatteryControl" }
String                          SmartBatteryControl_Text"Smart Battery Control_Text[%s]"        <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:smartBatteryControl_Text" }
Number:Time                     BatteryChargeTimeFrom   "Battery Charge Time From[%.2f %unit%]" <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:batteryChargeTimeFrom" }
Number:Time                     BatteryChargeTimeTo     "Battery Charge Time To[%.2f %unit%]"   <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:batteryChargeTimeTo" }
String                          MaxDephtOfDischarge     "Max Depht Of Discharge[%s]"            <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:maxDephtOfDischarge" }
String                          ShadowManagement        "Shadow Management[%s]"                 <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:shadowManagement" }
String                          ExternalModuleControl   "External Module Control[%s]"           <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:externalModuleControl" }
String                          InverterName            "InverterName[%s]"                      <energy>  { channel="kostalinverter:kostalinverterpiko1020:inverter:inverterName" }

```


### Third generation devices (PIKO IQ / PLENTICORE plus)

demo.items:

```
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalDCPower"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY     <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalHomeconsumptionFromBattery"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalHomeconsumptionFromGrid"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalOwnconsumption"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalHomeconsumptionFromPV"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalHomeconsumptionTotal"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalLimitEVUAbsolute"}
Number:Dimensionless         MyPlentiCore100WithBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalLimitEVURelativ"}
Number:Time                  MyPlentiCore100WithBattery_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalWorktime"}
Number:ElectricCurrent       MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalACPhase1CurrentAmperage"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalACPhase1CurrentPower"}
Number:ElectricPotential     MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalACPhase1CurrentVoltage"}
Number:ElectricCurrent       MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalACPhase2CurrentAmperage"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalACPhase2CurrentPower"}
Number:ElectricPotential     MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalACPhase2CurrentVoltage"}
Number:ElectricCurrent       MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalACPhase3CurrentAmperage"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalACPhase3CurrentPower"}
Number:ElectricPotential     MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalACPhase3CurrentVoltage"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalACCurrentPower"}
Number:Dimensionless         MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_LOADING_CYCLES           <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalBatteryLoadingCycles"}
Number:ElectricCharge        MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY     <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalBatteryFullChargeCapacity"}
Number:ElectricCurrent       MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_AMPERAGE                 <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalBatteryAmperage"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_POWER                    <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalBatteryPower"}
Number:Dimensionless         MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE          <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalBatteryStageOfCharge"}
Number:ElectricPotential     MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_VOLTAGE                  <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalBatteryVoltage"}
Number:ElectricCurrent       MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalPVString1Amperage"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalPVString1Power"}
Number:ElectricPotential     MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalPVString1Voltage"}
Number:ElectricCurrent       MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalPVString2Amperage"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalPVString2Power"}
Number:ElectricPotential     MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:deviceLocalPVString2Voltage"}
Number:Dimensionless         MyPlentiCore100WithBattery_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:SCBEventErrorCountMc"}
Number:Dimensionless         MyPlentiCore100WithBattery_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:SCBEventErrorCountSFH"}
Number:Dimensionless         MyPlentiCore100WithBattery_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:SCBEventErrorCountSCB"}
Number:Dimensionless         MyPlentiCore100WithBattery_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:SCBEventWarningCountSCB"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticAutarkyDay"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticAutarkyMonth"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticAutarkyTotal"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticAutarkyYear"}
Number:Mass                  MyPlentiCore100WithBattery_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticCo2SavingDay"}
Number:Mass                  MyPlentiCore100WithBattery_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticCo2SavingMonth"}
Number:Mass                  MyPlentiCore100WithBattery_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticCo2SavingTotal"}
Number:Mass                  MyPlentiCore100WithBattery_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticCo2SavingYear"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionDay"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionMonth"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionTotal"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionYear"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY   <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionFromBatteryDay"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionFromBatteryMonth"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionFromBatteryTotal"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR  <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionFromBatteryYear"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionFromGridDay"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionFromGridMonth"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionFromGridTotal"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionFromGridYear"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionFromPVDay"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionFromPVMonth"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionFromPVTotal"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticHomeconsumptionFromPVYear"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticOwnconsumptionRateDay"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticOwnconsumptionRateMonth"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticOwnconsumptionRateTotal"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticOwnconsumptionRateYear"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_YIELD_DAY                           <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticYieldDay"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticYieldMonth"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticYieldTotal"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalinverter:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:statisticYieldYear"}

```
