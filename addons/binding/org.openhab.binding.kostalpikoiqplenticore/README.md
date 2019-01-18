# kostalpikoiqplenticore Binding


This binding allows you to communicate with KOSTAL solar inverters.
Currently the required communication board is embedded in PIKO IQ and PLENTICORE plus inverters.

The communication board (SCB = Smart Communication Board) offers a (not yet officially released, but stable) web API to consume all information, which are available in the inverter´s own web front end.
This includes current operating data as well as aggregated data from the past for statistical purposes. 


## Supported Things

This implementation was tested for the current KOSTAL PIKO PLENTICORE plus and PIKO IQ devices.
All of these devices contain the same communication board (SCB = <u>S</u>mart<u>C</u>onnection<u>B</u>oard)

Currently supported things are:
<ul>
<li>PIKO IQ 4.2</li>
<li>PIKO IQ 5.5</li>
<li>PIKO IQ 7.0</li>
<li>PIKO IQ 8.5</li>
<li>PIKO IQ 10.0</li>
<li>PLENTICORE plus 4.2 (with or without battery attached)</li>
<li>PLENTICORE plus 5.5 (with or without battery attached)</li>
<li>PLENTICORE plus 7.0 (with or without battery attached)</li>
<li>PLENTICORE plus 8.5 (with or without battery attached)</li>
<li>PLENTICORE plus 10.0 (with or without battery attached)</li>
</ul>

Others may be supported (like future devices using the same SCB or offering the same web api, branded oem devices, ...), but they were not tested!

## Discovery

No auto discovery supported

## Binding Configuration

_This binding only works with JRE > 8u162! Otherwise you will have to do some manual configuration to support the encryption technologies used by this binding (see https://golb.hplar.ch/2017/10/JCE-policy-changes-in-Java-SE-8u151-and-8u152.html) _

## Thing Configuration

All inverters supported by this binding require to define 3 mandatory configuration parameters:

```
url                       Host name or IP address of your devise
userPassword              Password you configured on the inverters web front end
refreshInternalInSeconds  Defines how often the device is polled for new values
```

If you are using the host name instead of the IP address, please make sure your DNS is configuration correctly!
The refresh interval should be chosen wisely. To small interval may led to high workload for the inverter. From my testing I recommend a interval of 30 seconds.

Full sample of thing configuration:

```
Thing kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="30"]
```

## Channels

The following channels are provided by this binding.
Note that not all channels are available to all inverters!
Please see the sample for your concrete device in the next section. 
Each sample contains all channels available to the concrete inverter!

```
DEVICE_LOCAL_DC_POWER                             Current DC power of the inverter
DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY         Current home consumption obtained from the battery
DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID            Current home consumption obtained from the grid
DEVICE_LOCAL_OWNCONSUMPTION                       Current own comsumption
DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV              Current home consumption obtained from photovoltaic
DEVICE_LOCAL_HOMECONSUMPTION_TOTAL                Current total homeconsumption
DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE                   Permitted feed-in quantity as absolute value as specified by the energy supplier 
DEVICE_LOCAL_LIMIT_EVU_RELATIV                    Permitted feed-in quantity as relative value as specified by the energy supplier 
DEVICE_LOCAL_WORKTIME                             Uptime of the inverter
DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE          Amperage of phase 1
DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER             Power of phase 1
DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE           Voltage of phase 1
DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE          Amperage of phase 2
DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER             Power of phase 2
DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE           Voltage of phase 2
DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE          Amperage of phase 3
DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER             Power of phase 3
DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE           Voltage of phase 3
DEVICE_LOCAL_AC_CURRENT_POWER                     Current AC power of the inverter
DEVICE_LOCAL_BATTERY_LOADING_CYCLES               Amount of loading cycles done by the battery
DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY         Capacity of the battery if charged fully
DEVICE_LOCAL_BATTERY_AMPERAGE                     Current amperage of the battery
DEVICE_LOCAL_BATTERY_POWER                        Current battery charge
DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE              Current battery charge status
DEVICE_LOCAL_BATTERY_VOLTAGE                      Current voltage of the battery
DEVICE_LOCAL_PVSTRING_1_AMPERAGE                  Current amperage of photovoltaic string 1
DEVICE_LOCAL_PVSTRING_1_POWER                     Current power  of photovoltaic string 1
DEVICE_LOCAL_PVSTRING_1_VOLTAGE                   Current voltage of photovoltaic string 1
DEVICE_LOCAL_PVSTRING_2_AMPERAGE                  Current amperage of photovoltaic string 2
DEVICE_LOCAL_PVSTRING_2_POWER                     Current power  of photovoltaic string 2
DEVICE_LOCAL_PVSTRING_2_VOLTAGE                   Current voltage of photovoltaic string 2
DEVICE_LOCAL_PVSTRING_3_AMPERAGE                  Current amperage of photovoltaic string 3
DEVICE_LOCAL_PVSTRING_3_POWER                     Current power  of photovoltaic string 2
DEVICE_LOCAL_PVSTRING_3_VOLTAGE                   Current voltage of photovoltaic string 3
SCB_EVENT_ERROR_COUNT_MC                          Number of errors reported by the main controller
SCB_EVENT_ERROR_COUNT_SFH                         Number of errors reported by the grid interface controller
SCB_EVENT_ERROR_COUNT_SCB                         Number of errors reported by the smart communication board
SCB_EVENT_WARNING_COUNT_SCB                       Number of warnings reported by the smart communication board
STATISTIC_AUTARKY_DAY                             Autarky ratio of this day
STATISTIC_AUTARKY_MONTH                           Autarky ratio of this month
STATISTIC_AUTARKY_TOTAL                           Autarky ratio overall
STATISTIC_AUTARKY_YEAR                            Autarky ratio of this year
STATISTIC_CO2SAVING_DAY                           Savings in Co2 emissions today
STATISTIC_CO2SAVING_MONTH                         Savings in Co2 emissions this month
STATISTIC_CO2SAVING_TOTAL                         Savings in Co2 emissions overall
STATISTIC_CO2SAVING_YEAR                          Savings in Co2 emissions this year
STATISTIC_HOMECONSUMPTION_DAY                     Home consumption today
STATISTIC_HOMECONSUMPTION_MONTH                   Home consumption this month
STATISTIC_HOMECONSUMPTION_TOTAL                   Home consumption overall
STATISTIC_HOMECONSUMPTION_YEAR                    Home consumption this year
STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY       Home consumption obtained from the battery today
STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH     Home consumption obtained from the battery this month
STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL     Home consumption obtained from the battery overall
STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR      Home consumption obtained from the battery this year
STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY           Home consumption obtained from the grid today
STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH         Home consumption obtained from the grid this month
STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL         Home consumption obtained from the grid overall
STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR          Home consumption obtained from the grid this year
STATISTIC_HOMECONSUMPTION_FROM_PV_DAY             Home consumption obtained from the photovoltaic plant today
STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH           Home consumption obtained from the photovoltaic plant this month
STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL           Home consumption obtained from the photovoltaic plant overall
STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR            Home consumption obtained from the photovoltaic plant this year
STATISTIC_OWNCONSUMPTION_RATE_DAY                 Percentage of electricity demand covered by photovoltaics today
STATISTIC_OWNCONSUMPTION_RATE_MONTH               Percentage of electricity demand covered by photovoltaics this month
STATISTIC_OWNCONSUMPTION_RATE_TOTAL               Percentage of electricity demand covered by photovoltaics overall
STATISTIC_OWNCONSUMPTION_RATE_YEAR                Percentage of electricity demand covered by photovoltaics this year
STATISTIC_YIELD_DAY                               Yield of the photovoltaic plant today
STATISTIC_YIELD_MONTH                             Yield of the photovoltaic plant this month
STATISTIC_YIELD_TOTAL                             Yield of the photovoltaic plant overall
STATISTIC_YIELD_YEAR                              Yield of the photovoltaic plant this year
```


## Full Example

<u><b>PIKO IQ 4.2 example</b></u>

thing configuration:

```
Thing kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42 [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="60"]
```

items configuration:

```
Number:Energy                MyPikoIQ42_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_DC_POWER"}
Number:Energy                MyPikoIQ42_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY     <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY"}
Number:Energy                MyPikoIQ42_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID"}
Number:Energy                MyPikoIQ42_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_OWNCONSUMPTION"}
Number:Energy                MyPikoIQ42_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV"}
Number:Energy                MyPikoIQ42_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPikoIQ42_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE"}
Number:Dimensionless         MyPikoIQ42_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_LIMIT_EVU_RELATIV"}
Number:Time                  MyPikoIQ42_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_WORKTIME"}
Number:ElectricCurrent       MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE"}
Number:Energy                MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER"}
Number:ElectricPotential     MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE"}
Number:Energy                MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER"}
Number:ElectricPotential     MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE"}
Number:Energy                MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER"}
Number:ElectricPotential     MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE"}
Number:Energy                MyPikoIQ42_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_AC_CURRENT_POWER"}
Number:ElectricCurrent       MyPikoIQ42_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_PVSTRING_1_AMPERAGE"}
Number:Energy                MyPikoIQ42_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_PVSTRING_1_POWER"}
Number:ElectricPotential     MyPikoIQ42_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_PVSTRING_1_VOLTAGE"}
Number:ElectricCurrent       MyPikoIQ42_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_PVSTRING_2_AMPERAGE"}
Number:Energy                MyPikoIQ42_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_PVSTRING_2_POWER"}
Number:ElectricPotential     MyPikoIQ42_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:DEVICE_LOCAL_PVSTRING_2_VOLTAGE"}
Number:Dimensionless         MyPikoIQ42_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:SCB_EVENT_ERROR_COUNT_MC"}
Number:Dimensionless         MyPikoIQ42_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:SCB_EVENT_ERROR_COUNT_SFH"}
Number:Dimensionless         MyPikoIQ42_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:SCB_EVENT_ERROR_COUNT_SCB"}
Number:Dimensionless         MyPikoIQ42_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:SCB_EVENT_WARNING_COUNT_SCB"}
Number:Dimensionless         MyPikoIQ42_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_AUTARKY_DAY"}
Number:Dimensionless         MyPikoIQ42_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_AUTARKY_MONTH"}
Number:Dimensionless         MyPikoIQ42_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_AUTARKY_TOTAL"}
Number:Dimensionless         MyPikoIQ42_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_AUTARKY_YEAR"}
Number:Mass                  MyPikoIQ42_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_CO2SAVING_DAY"}
Number:Mass                  MyPikoIQ42_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_CO2SAVING_MONTH"}
Number:Mass                  MyPikoIQ42_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_CO2SAVING_TOTAL"}
Number:Mass                  MyPikoIQ42_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_CO2SAVING_YEAR"}
Number:Energy                MyPikoIQ42_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_HOMECONSUMPTION_DAY"}
Number:Energy                MyPikoIQ42_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_HOMECONSUMPTION_MONTH"}
Number:Energy                MyPikoIQ42_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPikoIQ42_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_HOMECONSUMPTION_YEAR"}
Number:Energy                MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY"}
Number:Energy                MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH"}
Number:Energy                MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL"}
Number:Energy                MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR"}
Number:Energy                MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_HOMECONSUMPTION_FROM_PV_DAY"}
Number:Energy                MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH"}
Number:Energy                MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL"}
Number:Energy                MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR"}
Number:Dimensionless         MyPikoIQ42_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_OWNCONSUMPTION_RATE_DAY"}
Number:Dimensionless         MyPikoIQ42_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_OWNCONSUMPTION_RATE_MONTH"}
Number:Dimensionless         MyPikoIQ42_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_OWNCONSUMPTION_RATE_TOTAL"}
Number:Dimensionless         MyPikoIQ42_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_OWNCONSUMPTION_RATE_YEAR"}
Number:Energy                MyPikoIQ42_STATISTIC_YIELD_DAY                           <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_YIELD_DAY"}
Number:Energy                MyPikoIQ42_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_YIELD_MONTH"}
Number:Energy                MyPikoIQ42_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_YIELD_TOTAL"}
Number:Energy                MyPikoIQ42_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalpikoiqplenticore:PIKOIQ42:MyPikoIq42:STATISTIC_YIELD_YEAR"}
```

sitemap configuration:

```
sitemap PIKOIQ42  label="KOSTAL PIKO IQ 4.2" {
  Frame label="Live data" {
    Group item=MyPikoIQ42_DEVICE_LOCAL_DC_POWER label="Solar energy" icon="solarplant" {
      Text item=MyPikoIQ42_DEVICE_LOCAL_DC_POWER label="Solar energy (total)" icon="solarplant"
      Group item=MyPikoIQ42_DEVICE_LOCAL_PVSTRING_1_POWER label="PV string 1" icon="solarplant"
      {    
        Text item=MyPikoIQ42_DEVICE_LOCAL_PVSTRING_1_POWER label="Power"
        Text item=MyPikoIQ42_DEVICE_LOCAL_PVSTRING_1_AMPERAGE label="Amperage"  
        Text item=MyPikoIQ42_DEVICE_LOCAL_PVSTRING_1_VOLTAGE label="Voltage"
      }
      Group item=MyPikoIQ42_DEVICE_LOCAL_PVSTRING_2_POWER label="PV string 2" icon="solarplant"
      {
        Text item=MyPikoIQ42_DEVICE_LOCAL_PVSTRING_2_POWER label="Power"
        Text item=MyPikoIQ42_DEVICE_LOCAL_PVSTRING_2_AMPERAGE label="Amperage"  
        Text item=MyPikoIQ42_DEVICE_LOCAL_PVSTRING_2_VOLTAGE label="Voltage"
      }
    }
    Group item=MyPikoIQ42_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC" {
    Frame label="Overall" {
      Text item=MyPikoIQ42_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC"
    }
    Frame label="Phase 1" {
      Text item=MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER label="Current power"
      Text item=MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 2" {
      Text item=MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER label="Current power"
      Text item=MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 3" {
      Text item=MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER label="Current power"
      Text item=MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPikoIQ42_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE label="Current voltage"
    }
    }
    Group item=MyPikoIQ42_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Home comsumption" icon="house" {
      Text item=MyPikoIQ42_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Total" icon="poweroutlet"
      Text item=MyPikoIQ42_DEVICE_LOCAL_OWNCONSUMPTION label="Owncomsumption" icon="house"
      Text item=MyPikoIQ42_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID label="From grid" icon="energy"
      Text item=MyPikoIQ42_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV  label="From photovoltaic" icon="sun_clouds"
    }
  }
  Frame label="Daily report" icon="line"{
    Text item=MyPikoIQ42_STATISTIC_YIELD_DAY label="Yield" icon="line"
    Text item=MyPikoIQ42_STATISTIC_AUTARKY_DAY label="Autarchy level" icon="garden"
    Text item=MyPikoIQ42_STATISTIC_CO2SAVING_DAY label="Co2 saving" icon="carbondioxide"
    Group item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_DAY label="Homeconsumption" icon="pie"
    {
      Text item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_DAY label="Total consumption" icon="poweroutlet"
      Text item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY label="From grid" icon="energy"
      Text item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY label="From photovoltaic" icon="sun_clouds"
      Text item=MyPikoIQ42_STATISTIC_OWNCONSUMPTION_RATE_DAY label="Ownconsumption rate" icon="price"
    }
  }
  Frame label="Monthly report" {
      Text item=MyPikoIQ42_STATISTIC_YIELD_MONTH label="Yield" icon="line"
      Text item=MyPikoIQ42_STATISTIC_AUTARKY_MONTH label="Autarchy level" icon="garden"
      Text item=MyPikoIQ42_STATISTIC_CO2SAVING_MONTH label="Co2 saving" icon="carbondioxide"  
      Group item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_MONTH label="Homeconsumption" icon="pie"
      {
        Text item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_MONTH label="Total consumption" icon="poweroutlet"
        Text item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH label="From grid" icon="energy"
        Text item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH label="From photovoltaic" icon="sun_clouds"
        Text item=MyPikoIQ42_STATISTIC_OWNCONSUMPTION_RATE_MONTH label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Yearly report" {
      Text item=MyPikoIQ42_STATISTIC_YIELD_YEAR label="Yield" icon="line"
      Text item=MyPikoIQ42_STATISTIC_AUTARKY_YEAR label="Autarchy level" icon="garden"
      Text item=MyPikoIQ42_STATISTIC_CO2SAVING_YEAR label="Co2 saving" icon="carbondioxide"
      Group item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_YEAR label="Homeconsumption" icon="pie"
      {
        Text item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_YEAR label="Total consumption" icon="poweroutlet"
        Text item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR label="From grid" icon="energy"
        Text item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR label="From photovoltaic" icon="sun_clouds"
        Text item=MyPikoIQ42_STATISTIC_OWNCONSUMPTION_RATE_YEAR label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Overall report" {
      Text item=MyPikoIQ42_STATISTIC_YIELD_TOTAL label="Yield" icon="line"
      Text item=MyPikoIQ42_STATISTIC_AUTARKY_TOTAL label="Autarchy level" icon="garden"
      Text item=MyPikoIQ42_STATISTIC_CO2SAVING_TOTAL label="Co2 saving" icon="carbondioxide"    
      Group item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_TOTAL label="Homeconsumption" icon="pie"
      {
        Text item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_TOTAL label="Total consumption" icon="poweroutlet"
        Text item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL label="From grid" icon="energy"
        Text item=MyPikoIQ42_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL label="From photovoltaic" icon="sun_clouds"
        Text item=MyPikoIQ42_STATISTIC_OWNCONSUMPTION_RATE_TOTAL label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Generated errors" {
      Text item=MyPikoIQ42_SCB_EVENT_ERROR_COUNT_MC label="MainController error count" icon="error"
      Text item=MyPikoIQ42_SCB_EVENT_ERROR_COUNT_SFH label="Grid controller error count" icon="error"
      Text item=MyPikoIQ42_SCB_EVENT_ERROR_COUNT_SCB label="SmartCommunicationBoard error count" icon="error"  
      Text item=MyPikoIQ42_SCB_EVENT_WARNING_COUNT_SCB label="SmartCommunicationBoard warning count" icon="error"
  }
  Frame label="Permitted feed-in quantity" {
    Text item=MyPikoIQ42_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE label="Absolute"
    Text item=MyPikoIQ42_DEVICE_LOCAL_LIMIT_EVU_RELATIV label="Relative"
  }
}
```

<u><b>PIKO IQ 5.5 example</b></u>

thing configuration:

```
Thing kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55 [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="60"]
```

items configuration:

```
Number:Energy                MyPikoIq55_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_DC_POWER"}
Number:Energy                MyPikoIq55_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY     <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY"}
Number:Energy                MyPikoIq55_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID"}
Number:Energy                MyPikoIq55_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_OWNCONSUMPTION"}
Number:Energy                MyPikoIq55_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV"}
Number:Energy                MyPikoIq55_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPikoIq55_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE"}
Number:Dimensionless         MyPikoIq55_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_LIMIT_EVU_RELATIV"}
Number:Time                  MyPikoIq55_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_WORKTIME"}
Number:ElectricCurrent       MyPikoIq55_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE"}
Number:Energy                MyPikoIq55_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER"}
Number:ElectricPotential     MyPikoIq55_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPikoIq55_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE"}
Number:Energy                MyPikoIq55_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER"}
Number:ElectricPotential     MyPikoIq55_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPikoIq55_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE"}
Number:Energy                MyPikoIq55_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER"}
Number:ElectricPotential     MyPikoIq55_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE"}
Number:Energy                MyPikoIq55_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_AC_CURRENT_POWER"}
Number:ElectricCurrent       MyPikoIq55_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_PVSTRING_1_AMPERAGE"}
Number:Energy                MyPikoIq55_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_PVSTRING_1_POWER"}
Number:ElectricPotential     MyPikoIq55_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_PVSTRING_1_VOLTAGE"}
Number:ElectricCurrent       MyPikoIq55_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_PVSTRING_2_AMPERAGE"}
Number:Energy                MyPikoIq55_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_PVSTRING_2_POWER"}
Number:ElectricPotential     MyPikoIq55_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:DEVICE_LOCAL_PVSTRING_2_VOLTAGE"}
Number:Dimensionless         MyPikoIq55_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:SCB_EVENT_ERROR_COUNT_MC"}
Number:Dimensionless         MyPikoIq55_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:SCB_EVENT_ERROR_COUNT_SFH"}
Number:Dimensionless         MyPikoIq55_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:SCB_EVENT_ERROR_COUNT_SCB"}
Number:Dimensionless         MyPikoIq55_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:SCB_EVENT_WARNING_COUNT_SCB"}
Number:Dimensionless         MyPikoIq55_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_AUTARKY_DAY"}
Number:Dimensionless         MyPikoIq55_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_AUTARKY_MONTH"}
Number:Dimensionless         MyPikoIq55_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_AUTARKY_TOTAL"}
Number:Dimensionless         MyPikoIq55_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_AUTARKY_YEAR"}
Number:Mass                  MyPikoIq55_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_CO2SAVING_DAY"}
Number:Mass                  MyPikoIq55_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_CO2SAVING_MONTH"}
Number:Mass                  MyPikoIq55_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_CO2SAVING_TOTAL"}
Number:Mass                  MyPikoIq55_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_CO2SAVING_YEAR"}
Number:Energy                MyPikoIq55_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_HOMECONSUMPTION_DAY"}
Number:Energy                MyPikoIq55_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_HOMECONSUMPTION_MONTH"}
Number:Energy                MyPikoIq55_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPikoIq55_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_HOMECONSUMPTION_YEAR"}
Number:Energy                MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY"}
Number:Energy                MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH"}
Number:Energy                MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL"}
Number:Energy                MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR"}
Number:Energy                MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_HOMECONSUMPTION_FROM_PV_DAY"}
Number:Energy                MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH"}
Number:Energy                MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL"}
Number:Energy                MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR"}
Number:Dimensionless         MyPikoIq55_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_OWNCONSUMPTION_RATE_DAY"}
Number:Dimensionless         MyPikoIq55_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_OWNCONSUMPTION_RATE_MONTH"}
Number:Dimensionless         MyPikoIq55_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_OWNCONSUMPTION_RATE_TOTAL"}
Number:Dimensionless         MyPikoIq55_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_OWNCONSUMPTION_RATE_YEAR"}
Number:Energy                MyPikoIq55_STATISTIC_YIELD_DAY                           <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_YIELD_DAY"}
Number:Energy                MyPikoIq55_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_YIELD_MONTH"}
Number:Energy                MyPikoIq55_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_YIELD_TOTAL"}
Number:Energy                MyPikoIq55_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalpikoiqplenticore:PIKOIQ55:MyPikoIq55:STATISTIC_YIELD_YEAR"}
```

sitemap configuration:

```
sitemap PIKOIQ55  label="KOSTAL PIKO IQ 5.5" {
  Frame label="Live data" {
    Group item=MyPikoIq55_DEVICE_LOCAL_DC_POWER label="Solar energy" icon="solarplant" {
      Text item=MyPikoIq55_DEVICE_LOCAL_DC_POWER label="Solar energy (total)" icon="solarplant"
      Group item=MyPikoIq55_DEVICE_LOCAL_PVSTRING_1_POWER label="PV string 1" icon="solarplant"
      {    
        Text item=MyPikoIq55_DEVICE_LOCAL_PVSTRING_1_POWER label="Power"
        Text item=MyPikoIq55_DEVICE_LOCAL_PVSTRING_1_AMPERAGE label="Amperage"  
        Text item=MyPikoIq55_DEVICE_LOCAL_PVSTRING_1_VOLTAGE label="Voltage"
      }
      Group item=MyPikoIq55_DEVICE_LOCAL_PVSTRING_2_POWER label="PV string 2" icon="solarplant"
      {
        Text item=MyPikoIq55_DEVICE_LOCAL_PVSTRING_2_POWER label="Power"
        Text item=MyPikoIq55_DEVICE_LOCAL_PVSTRING_2_AMPERAGE label="Amperage"  
        Text item=MyPikoIq55_DEVICE_LOCAL_PVSTRING_2_VOLTAGE label="Voltage"
      }
    }
    Group item=MyPikoIq55_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC" {
    Frame label="Overall" {
      Text item=MyPikoIq55_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC"
    }
    Frame label="Phase 1" {
      Text item=MyPikoIq55_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER label="Current power"
      Text item=MyPikoIq55_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPikoIq55_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 2" {
      Text item=MyPikoIq55_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER label="Current power"
      Text item=MyPikoIq55_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPikoIq55_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 3" {
      Text item=MyPikoIq55_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER label="Current power"
      Text item=MyPikoIq55_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPikoIq55_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE label="Current voltage"
    }
    }
    Group item=MyPikoIq55_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Home comsumption" icon="house" {
      Text item=MyPikoIq55_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Total" icon="poweroutlet"
      Text item=MyPikoIq55_DEVICE_LOCAL_OWNCONSUMPTION label="Owncomsumption" icon="house"
      Text item=MyPikoIq55_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID label="From grid" icon="energy"
      Text item=MyPikoIq55_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV  label="From photovoltaic" icon="sun_clouds"
    }
  }
  Frame label="Daily report" icon="line"{
    Text item=MyPikoIq55_STATISTIC_YIELD_DAY label="Yield" icon="line"
    Text item=MyPikoIq55_STATISTIC_AUTARKY_DAY label="Autarchy level" icon="garden"
    Text item=MyPikoIq55_STATISTIC_CO2SAVING_DAY label="Co2 saving" icon="carbondioxide"
    Group item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_DAY label="Homeconsumption" icon="pie"
    {
      Text item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_DAY label="Total consumption" icon="poweroutlet"
      Text item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY label="From grid" icon="energy"
      Text item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY label="From photovoltaic" icon="sun_clouds"
      Text item=MyPikoIq55_STATISTIC_OWNCONSUMPTION_RATE_DAY label="Ownconsumption rate" icon="price"
    }
  }
  Frame label="Monthly report" {
      Text item=MyPikoIq55_STATISTIC_YIELD_MONTH label="Yield" icon="line"
      Text item=MyPikoIq55_STATISTIC_AUTARKY_MONTH label="Autarchy level" icon="garden"
      Text item=MyPikoIq55_STATISTIC_CO2SAVING_MONTH label="Co2 saving" icon="carbondioxide"  
      Group item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_MONTH label="Homeconsumption" icon="pie"
      {
        Text item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_MONTH label="Total consumption" icon="poweroutlet"
        Text item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH label="From grid" icon="energy"
        Text item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH label="From photovoltaic" icon="sun_clouds"
        Text item=MyPikoIq55_STATISTIC_OWNCONSUMPTION_RATE_MONTH label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Yearly report" {
      Text item=MyPikoIq55_STATISTIC_YIELD_YEAR label="Yield" icon="line"
      Text item=MyPikoIq55_STATISTIC_AUTARKY_YEAR label="Autarchy level" icon="garden"
      Text item=MyPikoIq55_STATISTIC_CO2SAVING_YEAR label="Co2 saving" icon="carbondioxide"
      Group item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_YEAR label="Homeconsumption" icon="pie"
      {
        Text item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_YEAR label="Total consumption" icon="poweroutlet"
        Text item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR label="From grid" icon="energy"
        Text item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR label="From photovoltaic" icon="sun_clouds"
        Text item=MyPikoIq55_STATISTIC_OWNCONSUMPTION_RATE_YEAR label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Overall report" {
      Text item=MyPikoIq55_STATISTIC_YIELD_TOTAL label="Yield" icon="line"
      Text item=MyPikoIq55_STATISTIC_AUTARKY_TOTAL label="Autarchy level" icon="garden"
      Text item=MyPikoIq55_STATISTIC_CO2SAVING_TOTAL label="Co2 saving" icon="carbondioxide"    
      Group item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_TOTAL label="Homeconsumption" icon="pie"
      {
        Text item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_TOTAL label="Total consumption" icon="poweroutlet"
        Text item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL label="From grid" icon="energy"
        Text item=MyPikoIq55_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL label="From photovoltaic" icon="sun_clouds"
        Text item=MyPikoIq55_STATISTIC_OWNCONSUMPTION_RATE_TOTAL label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Generated errors" {
      Text item=MyPikoIq55_SCB_EVENT_ERROR_COUNT_MC label="MainController error count" icon="error"
      Text item=MyPikoIq55_SCB_EVENT_ERROR_COUNT_SFH label="Grid controller error count" icon="error"
      Text item=MyPikoIq55_SCB_EVENT_ERROR_COUNT_SCB label="SmartCommunicationBoard error count" icon="error"  
      Text item=MyPikoIq55_SCB_EVENT_WARNING_COUNT_SCB label="SmartCommunicationBoard warning count" icon="error"
  }
  Frame label="Permitted feed-in quantity" {
    Text item=MyPikoIq55_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE label="Absolute"
    Text item=MyPikoIq55_DEVICE_LOCAL_LIMIT_EVU_RELATIV label="Relative"
  }
}
```

<u><b>PIKO IQ 7.0 example</b></u>

thing configuration:

```
Thing kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70 [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="60"]
```

items configuration:

```
Number:Energy                MyPikoIq70_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_DC_POWER"}
Number:Energy                MyPikoIq70_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY     <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY"}
Number:Energy                MyPikoIq70_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID"}
Number:Energy                MyPikoIq70_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_OWNCONSUMPTION"}
Number:Energy                MyPikoIq70_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV"}
Number:Energy                MyPikoIq70_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPikoIq70_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE"}
Number:Dimensionless         MyPikoIq70_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_LIMIT_EVU_RELATIV"}
Number:Time                  MyPikoIq70_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_WORKTIME"}
Number:ElectricCurrent       MyPikoIq70_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE"}
Number:Energy                MyPikoIq70_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER"}
Number:ElectricPotential     MyPikoIq70_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPikoIq70_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE"}
Number:Energy                MyPikoIq70_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER"}
Number:ElectricPotential     MyPikoIq70_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPikoIq70_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE"}
Number:Energy                MyPikoIq70_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER"}
Number:ElectricPotential     MyPikoIq70_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE"}
Number:Energy                MyPikoIq70_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_AC_CURRENT_POWER"}
Number:ElectricCurrent       MyPikoIq70_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_PVSTRING_1_AMPERAGE"}
Number:Energy                MyPikoIq70_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_PVSTRING_1_POWER"}
Number:ElectricPotential     MyPikoIq70_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_PVSTRING_1_VOLTAGE"}
Number:ElectricCurrent       MyPikoIq70_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_PVSTRING_2_AMPERAGE"}
Number:Energy                MyPikoIq70_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_PVSTRING_2_POWER"}
Number:ElectricPotential     MyPikoIq70_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:DEVICE_LOCAL_PVSTRING_2_VOLTAGE"}
Number:Dimensionless         MyPikoIq70_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:SCB_EVENT_ERROR_COUNT_MC"}
Number:Dimensionless         MyPikoIq70_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:SCB_EVENT_ERROR_COUNT_SFH"}
Number:Dimensionless         MyPikoIq70_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:SCB_EVENT_ERROR_COUNT_SCB"}
Number:Dimensionless         MyPikoIq70_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:SCB_EVENT_WARNING_COUNT_SCB"}
Number:Dimensionless         MyPikoIq70_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_AUTARKY_DAY"}
Number:Dimensionless         MyPikoIq70_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_AUTARKY_MONTH"}
Number:Dimensionless         MyPikoIq70_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_AUTARKY_TOTAL"}
Number:Dimensionless         MyPikoIq70_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_AUTARKY_YEAR"}
Number:Mass                  MyPikoIq70_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_CO2SAVING_DAY"}
Number:Mass                  MyPikoIq70_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_CO2SAVING_MONTH"}
Number:Mass                  MyPikoIq70_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_CO2SAVING_TOTAL"}
Number:Mass                  MyPikoIq70_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_CO2SAVING_YEAR"}
Number:Energy                MyPikoIq70_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_HOMECONSUMPTION_DAY"}
Number:Energy                MyPikoIq70_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_HOMECONSUMPTION_MONTH"}
Number:Energy                MyPikoIq70_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPikoIq70_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_HOMECONSUMPTION_YEAR"}
Number:Energy                MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY"}
Number:Energy                MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH"}
Number:Energy                MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL"}
Number:Energy                MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR"}
Number:Energy                MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_HOMECONSUMPTION_FROM_PV_DAY"}
Number:Energy                MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH"}
Number:Energy                MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL"}
Number:Energy                MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR"}
Number:Dimensionless         MyPikoIq70_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_OWNCONSUMPTION_RATE_DAY"}
Number:Dimensionless         MyPikoIq70_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_OWNCONSUMPTION_RATE_MONTH"}
Number:Dimensionless         MyPikoIq70_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_OWNCONSUMPTION_RATE_TOTAL"}
Number:Dimensionless         MyPikoIq70_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_OWNCONSUMPTION_RATE_YEAR"}
Number:Energy                MyPikoIq70_STATISTIC_YIELD_DAY                           <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_YIELD_DAY"}
Number:Energy                MyPikoIq70_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_YIELD_MONTH"}
Number:Energy                MyPikoIq70_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_YIELD_TOTAL"}
Number:Energy                MyPikoIq70_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalpikoiqplenticore:PIKOIQ70:MyPikoIq70:STATISTIC_YIELD_YEAR"}
```

sitemap configuration:

```
sitemap PIKOIQ70  label="KOSTAL PIKO IQ 7.0" {
  Frame label="Live data" {
    Group item=MyPikoIq70_DEVICE_LOCAL_DC_POWER label="Solar energy" icon="solarplant" {
      Text item=MyPikoIq70_DEVICE_LOCAL_DC_POWER label="Solar energy (total)" icon="solarplant"
      Group item=MyPikoIq70_DEVICE_LOCAL_PVSTRING_1_POWER label="PV string 1" icon="solarplant"
      {    
        Text item=MyPikoIq70_DEVICE_LOCAL_PVSTRING_1_POWER label="Power"
        Text item=MyPikoIq70_DEVICE_LOCAL_PVSTRING_1_AMPERAGE label="Amperage"  
        Text item=MyPikoIq70_DEVICE_LOCAL_PVSTRING_1_VOLTAGE label="Voltage"
      }
      Group item=MyPikoIq70_DEVICE_LOCAL_PVSTRING_2_POWER label="PV string 2" icon="solarplant"
      {
        Text item=MyPikoIq70_DEVICE_LOCAL_PVSTRING_2_POWER label="Power"
        Text item=MyPikoIq70_DEVICE_LOCAL_PVSTRING_2_AMPERAGE label="Amperage"  
        Text item=MyPikoIq70_DEVICE_LOCAL_PVSTRING_2_VOLTAGE label="Voltage"
      }
    }
    Group item=MyPikoIq70_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC" {
    Frame label="Overall" {
      Text item=MyPikoIq70_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC"
    }
    Frame label="Phase 1" {
      Text item=MyPikoIq70_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER label="Current power"
      Text item=MyPikoIq70_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPikoIq70_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 2" {
      Text item=MyPikoIq70_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER label="Current power"
      Text item=MyPikoIq70_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPikoIq70_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 3" {
      Text item=MyPikoIq70_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER label="Current power"
      Text item=MyPikoIq70_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPikoIq70_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE label="Current voltage"
    }
    }
    Group item=MyPikoIq70_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Home comsumption" icon="house" {
      Text item=MyPikoIq70_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Total" icon="poweroutlet"
      Text item=MyPikoIq70_DEVICE_LOCAL_OWNCONSUMPTION label="Owncomsumption" icon="house"
      Text item=MyPikoIq70_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID label="From grid" icon="energy"
      Text item=MyPikoIq70_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV  label="From photovoltaic" icon="sun_clouds"
    }
  }
  Frame label="Daily report" icon="line"{
    Text item=MyPikoIq70_STATISTIC_YIELD_DAY label="Yield" icon="line"
    Text item=MyPikoIq70_STATISTIC_AUTARKY_DAY label="Autarchy level" icon="garden"
    Text item=MyPikoIq70_STATISTIC_CO2SAVING_DAY label="Co2 saving" icon="carbondioxide"
    Group item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_DAY label="Homeconsumption" icon="pie"
    {
      Text item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_DAY label="Total consumption" icon="poweroutlet"
      Text item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY label="From grid" icon="energy"
      Text item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY label="From photovoltaic" icon="sun_clouds"
      Text item=MyPikoIq70_STATISTIC_OWNCONSUMPTION_RATE_DAY label="Ownconsumption rate" icon="price"
    }
  }
  Frame label="Monthly report" {
      Text item=MyPikoIq70_STATISTIC_YIELD_MONTH label="Yield" icon="line"
      Text item=MyPikoIq70_STATISTIC_AUTARKY_MONTH label="Autarchy level" icon="garden"
      Text item=MyPikoIq70_STATISTIC_CO2SAVING_MONTH label="Co2 saving" icon="carbondioxide"  
      Group item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_MONTH label="Homeconsumption" icon="pie"
      {
        Text item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_MONTH label="Total consumption" icon="poweroutlet"
        Text item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH label="From grid" icon="energy"
        Text item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH label="From photovoltaic" icon="sun_clouds"
        Text item=MyPikoIq70_STATISTIC_OWNCONSUMPTION_RATE_MONTH label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Yearly report" {
      Text item=MyPikoIq70_STATISTIC_YIELD_YEAR label="Yield" icon="line"
      Text item=MyPikoIq70_STATISTIC_AUTARKY_YEAR label="Autarchy level" icon="garden"
      Text item=MyPikoIq70_STATISTIC_CO2SAVING_YEAR label="Co2 saving" icon="carbondioxide"
      Group item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_YEAR label="Homeconsumption" icon="pie"
      {
        Text item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_YEAR label="Total consumption" icon="poweroutlet"
        Text item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR label="From grid" icon="energy"
        Text item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR label="From photovoltaic" icon="sun_clouds"
        Text item=MyPikoIq70_STATISTIC_OWNCONSUMPTION_RATE_YEAR label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Overall report" {
      Text item=MyPikoIq70_STATISTIC_YIELD_TOTAL label="Yield" icon="line"
      Text item=MyPikoIq70_STATISTIC_AUTARKY_TOTAL label="Autarchy level" icon="garden"
      Text item=MyPikoIq70_STATISTIC_CO2SAVING_TOTAL label="Co2 saving" icon="carbondioxide"    
      Group item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_TOTAL label="Homeconsumption" icon="pie"
      {
        Text item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_TOTAL label="Total consumption" icon="poweroutlet"
        Text item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL label="From grid" icon="energy"
        Text item=MyPikoIq70_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL label="From photovoltaic" icon="sun_clouds"
        Text item=MyPikoIq70_STATISTIC_OWNCONSUMPTION_RATE_TOTAL label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Generated errors" {
      Text item=MyPikoIq70_SCB_EVENT_ERROR_COUNT_MC label="MainController error count" icon="error"
      Text item=MyPikoIq70_SCB_EVENT_ERROR_COUNT_SFH label="Grid controller error count" icon="error"
      Text item=MyPikoIq70_SCB_EVENT_ERROR_COUNT_SCB label="SmartCommunicationBoard error count" icon="error"  
      Text item=MyPikoIq70_SCB_EVENT_WARNING_COUNT_SCB label="SmartCommunicationBoard warning count" icon="error"
  }
  Frame label="Permitted feed-in quantity" {
    Text item=MyPikoIq70_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE label="Absolute"
    Text item=MyPikoIq70_DEVICE_LOCAL_LIMIT_EVU_RELATIV label="Relative"
  }
}
```

<u><b>PIKO IQ 8.5 example</b></u>

thing configuration:

```
Thing kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85 [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="60"]
```

items configuration:

```
Number:Energy                MyPikoIq85_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_DC_POWER"}
Number:Energy                MyPikoIq85_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY     <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY"}
Number:Energy                MyPikoIq85_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID"}
Number:Energy                MyPikoIq85_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_OWNCONSUMPTION"}
Number:Energy                MyPikoIq85_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV"}
Number:Energy                MyPikoIq85_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPikoIq85_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE"}
Number:Dimensionless         MyPikoIq85_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_LIMIT_EVU_RELATIV"}
Number:Time                  MyPikoIq85_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_WORKTIME"}
Number:ElectricCurrent       MyPikoIq85_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE"}
Number:Energy                MyPikoIq85_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER"}
Number:ElectricPotential     MyPikoIq85_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPikoIq85_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE"}
Number:Energy                MyPikoIq85_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER"}
Number:ElectricPotential     MyPikoIq85_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPikoIq85_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE"}
Number:Energy                MyPikoIq85_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER"}
Number:ElectricPotential     MyPikoIq85_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE"}
Number:Energy                MyPikoIq85_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_AC_CURRENT_POWER"}
Number:ElectricCurrent       MyPikoIq85_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_PVSTRING_1_AMPERAGE"}
Number:Energy                MyPikoIq85_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_PVSTRING_1_POWER"}
Number:ElectricPotential     MyPikoIq85_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_PVSTRING_1_VOLTAGE"}
Number:ElectricCurrent       MyPikoIq85_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_PVSTRING_2_AMPERAGE"}
Number:Energy                MyPikoIq85_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_PVSTRING_2_POWER"}
Number:ElectricPotential     MyPikoIq85_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:DEVICE_LOCAL_PVSTRING_2_VOLTAGE"}
Number:Dimensionless         MyPikoIq85_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:SCB_EVENT_ERROR_COUNT_MC"}
Number:Dimensionless         MyPikoIq85_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:SCB_EVENT_ERROR_COUNT_SFH"}
Number:Dimensionless         MyPikoIq85_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:SCB_EVENT_ERROR_COUNT_SCB"}
Number:Dimensionless         MyPikoIq85_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:SCB_EVENT_WARNING_COUNT_SCB"}
Number:Dimensionless         MyPikoIq85_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_AUTARKY_DAY"}
Number:Dimensionless         MyPikoIq85_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_AUTARKY_MONTH"}
Number:Dimensionless         MyPikoIq85_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_AUTARKY_TOTAL"}
Number:Dimensionless         MyPikoIq85_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_AUTARKY_YEAR"}
Number:Mass                  MyPikoIq85_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_CO2SAVING_DAY"}
Number:Mass                  MyPikoIq85_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_CO2SAVING_MONTH"}
Number:Mass                  MyPikoIq85_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_CO2SAVING_TOTAL"}
Number:Mass                  MyPikoIq85_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_CO2SAVING_YEAR"}
Number:Energy                MyPikoIq85_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_HOMECONSUMPTION_DAY"}
Number:Energy                MyPikoIq85_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_HOMECONSUMPTION_MONTH"}
Number:Energy                MyPikoIq85_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPikoIq85_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_HOMECONSUMPTION_YEAR"}
Number:Energy                MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY"}
Number:Energy                MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH"}
Number:Energy                MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL"}
Number:Energy                MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR"}
Number:Energy                MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_HOMECONSUMPTION_FROM_PV_DAY"}
Number:Energy                MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH"}
Number:Energy                MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL"}
Number:Energy                MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR"}
Number:Dimensionless         MyPikoIq85_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_OWNCONSUMPTION_RATE_DAY"}
Number:Dimensionless         MyPikoIq85_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_OWNCONSUMPTION_RATE_MONTH"}
Number:Dimensionless         MyPikoIq85_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_OWNCONSUMPTION_RATE_TOTAL"}
Number:Dimensionless         MyPikoIq85_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_OWNCONSUMPTION_RATE_YEAR"}
Number:Energy                MyPikoIq85_STATISTIC_YIELD_DAY                           <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_YIELD_DAY"}
Number:Energy                MyPikoIq85_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_YIELD_MONTH"}
Number:Energy                MyPikoIq85_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_YIELD_TOTAL"}
Number:Energy                MyPikoIq85_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalpikoiqplenticore:PIKOIQ85:MyPikoIq85:STATISTIC_YIELD_YEAR"}
```

sitemap configuration:

```
sitemap PIKOIQ85  label="KOSTAL PIKO IQ 8.5" {
  Frame label="Live data" {
    Group item=MyPikoIq85_DEVICE_LOCAL_DC_POWER label="Solar energy" icon="solarplant" {
      Text item=MyPikoIq85_DEVICE_LOCAL_DC_POWER label="Solar energy (total)" icon="solarplant"
      Group item=MyPikoIq85_DEVICE_LOCAL_PVSTRING_1_POWER label="PV string 1" icon="solarplant"
      {    
        Text item=MyPikoIq85_DEVICE_LOCAL_PVSTRING_1_POWER label="Power"
        Text item=MyPikoIq85_DEVICE_LOCAL_PVSTRING_1_AMPERAGE label="Amperage"  
        Text item=MyPikoIq85_DEVICE_LOCAL_PVSTRING_1_VOLTAGE label="Voltage"
      }
      Group item=MyPikoIq85_DEVICE_LOCAL_PVSTRING_2_POWER label="PV string 2" icon="solarplant"
      {
        Text item=MyPikoIq85_DEVICE_LOCAL_PVSTRING_2_POWER label="Power"
        Text item=MyPikoIq85_DEVICE_LOCAL_PVSTRING_2_AMPERAGE label="Amperage"  
        Text item=MyPikoIq85_DEVICE_LOCAL_PVSTRING_2_VOLTAGE label="Voltage"
      }
    }
    Group item=MyPikoIq85_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC" {
    Frame label="Overall" {
      Text item=MyPikoIq85_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC"
    }
    Frame label="Phase 1" {
      Text item=MyPikoIq85_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER label="Current power"
      Text item=MyPikoIq85_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPikoIq85_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 2" {
      Text item=MyPikoIq85_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER label="Current power"
      Text item=MyPikoIq85_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPikoIq85_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 3" {
      Text item=MyPikoIq85_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER label="Current power"
      Text item=MyPikoIq85_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPikoIq85_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE label="Current voltage"
    }
    }
    Group item=MyPikoIq85_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Home comsumption" icon="house" {
      Text item=MyPikoIq85_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Total" icon="poweroutlet"
      Text item=MyPikoIq85_DEVICE_LOCAL_OWNCONSUMPTION label="Owncomsumption" icon="house"
      Text item=MyPikoIq85_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID label="From grid" icon="energy"
      Text item=MyPikoIq85_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV  label="From photovoltaic" icon="sun_clouds"
    }
  }
  Frame label="Daily report" icon="line"{
    Text item=MyPikoIq85_STATISTIC_YIELD_DAY label="Yield" icon="line"
    Text item=MyPikoIq85_STATISTIC_AUTARKY_DAY label="Autarchy level" icon="garden"
    Text item=MyPikoIq85_STATISTIC_CO2SAVING_DAY label="Co2 saving" icon="carbondioxide"
    Group item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_DAY label="Homeconsumption" icon="pie"
    {
      Text item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_DAY label="Total consumption" icon="poweroutlet"
      Text item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY label="From grid" icon="energy"
      Text item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY label="From photovoltaic" icon="sun_clouds"
      Text item=MyPikoIq85_STATISTIC_OWNCONSUMPTION_RATE_DAY label="Ownconsumption rate" icon="price"
    }
  }
  Frame label="Monthly report" {
      Text item=MyPikoIq85_STATISTIC_YIELD_MONTH label="Yield" icon="line"
      Text item=MyPikoIq85_STATISTIC_AUTARKY_MONTH label="Autarchy level" icon="garden"
      Text item=MyPikoIq85_STATISTIC_CO2SAVING_MONTH label="Co2 saving" icon="carbondioxide"  
      Group item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_MONTH label="Homeconsumption" icon="pie"
      {
        Text item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_MONTH label="Total consumption" icon="poweroutlet"
        Text item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH label="From grid" icon="energy"
        Text item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH label="From photovoltaic" icon="sun_clouds"
        Text item=MyPikoIq85_STATISTIC_OWNCONSUMPTION_RATE_MONTH label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Yearly report" {
      Text item=MyPikoIq85_STATISTIC_YIELD_YEAR label="Yield" icon="line"
      Text item=MyPikoIq85_STATISTIC_AUTARKY_YEAR label="Autarchy level" icon="garden"
      Text item=MyPikoIq85_STATISTIC_CO2SAVING_YEAR label="Co2 saving" icon="carbondioxide"
      Group item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_YEAR label="Homeconsumption" icon="pie"
      {
        Text item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_YEAR label="Total consumption" icon="poweroutlet"
        Text item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR label="From grid" icon="energy"
        Text item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR label="From photovoltaic" icon="sun_clouds"
        Text item=MyPikoIq85_STATISTIC_OWNCONSUMPTION_RATE_YEAR label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Overall report" {
      Text item=MyPikoIq85_STATISTIC_YIELD_TOTAL label="Yield" icon="line"
      Text item=MyPikoIq85_STATISTIC_AUTARKY_TOTAL label="Autarchy level" icon="garden"
      Text item=MyPikoIq85_STATISTIC_CO2SAVING_TOTAL label="Co2 saving" icon="carbondioxide"    
      Group item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_TOTAL label="Homeconsumption" icon="pie"
      {
        Text item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_TOTAL label="Total consumption" icon="poweroutlet"
        Text item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL label="From grid" icon="energy"
        Text item=MyPikoIq85_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL label="From photovoltaic" icon="sun_clouds"
        Text item=MyPikoIq85_STATISTIC_OWNCONSUMPTION_RATE_TOTAL label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Generated errors" {
      Text item=MyPikoIq85_SCB_EVENT_ERROR_COUNT_MC label="MainController error count" icon="error"
      Text item=MyPikoIq85_SCB_EVENT_ERROR_COUNT_SFH label="Grid controller error count" icon="error"
      Text item=MyPikoIq85_SCB_EVENT_ERROR_COUNT_SCB label="SmartCommunicationBoard error count" icon="error"  
      Text item=MyPikoIq85_SCB_EVENT_WARNING_COUNT_SCB label="SmartCommunicationBoard warning count" icon="error"
  }
  Frame label="Permitted feed-in quantity" {
    Text item=MyPikoIq85_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE label="Absolute"
    Text item=MyPikoIq85_DEVICE_LOCAL_LIMIT_EVU_RELATIV label="Relative"
  }
}
```

<u><b>PIKO IQ 10.0 example</b></u>

thing configuration:

```
Thing kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100 [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="60"]
```

items configuration:

```
Number:Energy                MyPikoIq100_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_DC_POWER"}
Number:Energy                MyPikoIq100_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY     <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY"}
Number:Energy                MyPikoIq100_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID"}
Number:Energy                MyPikoIq100_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_OWNCONSUMPTION"}
Number:Energy                MyPikoIq100_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV"}
Number:Energy                MyPikoIq100_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPikoIq100_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE"}
Number:Dimensionless         MyPikoIq100_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_LIMIT_EVU_RELATIV"}
Number:Time                  MyPikoIq100_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_WORKTIME"}
Number:ElectricCurrent       MyPikoIq100_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE"}
Number:Energy                MyPikoIq100_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER"}
Number:ElectricPotential     MyPikoIq100_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPikoIq100_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE"}
Number:Energy                MyPikoIq100_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER"}
Number:ElectricPotential     MyPikoIq100_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPikoIq100_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE"}
Number:Energy                MyPikoIq100_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER"}
Number:ElectricPotential     MyPikoIq100_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE"}
Number:Energy                MyPikoIq100_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_AC_CURRENT_POWER"}
Number:ElectricCurrent       MyPikoIq100_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_PVSTRING_1_AMPERAGE"}
Number:Energy                MyPikoIq100_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_PVSTRING_1_POWER"}
Number:ElectricPotential     MyPikoIq100_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_PVSTRING_1_VOLTAGE"}
Number:ElectricCurrent       MyPikoIq100_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_PVSTRING_2_AMPERAGE"}
Number:Energy                MyPikoIq100_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_PVSTRING_2_POWER"}
Number:ElectricPotential     MyPikoIq100_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:DEVICE_LOCAL_PVSTRING_2_VOLTAGE"}
Number:Dimensionless         MyPikoIq100_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:SCB_EVENT_ERROR_COUNT_MC"}
Number:Dimensionless         MyPikoIq100_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:SCB_EVENT_ERROR_COUNT_SFH"}
Number:Dimensionless         MyPikoIq100_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:SCB_EVENT_ERROR_COUNT_SCB"}
Number:Dimensionless         MyPikoIq100_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:SCB_EVENT_WARNING_COUNT_SCB"}
Number:Dimensionless         MyPikoIq100_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_AUTARKY_DAY"}
Number:Dimensionless         MyPikoIq100_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_AUTARKY_MONTH"}
Number:Dimensionless         MyPikoIq100_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_AUTARKY_TOTAL"}
Number:Dimensionless         MyPikoIq100_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_AUTARKY_YEAR"}
Number:Mass                  MyPikoIq100_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_CO2SAVING_DAY"}
Number:Mass                  MyPikoIq100_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_CO2SAVING_MONTH"}
Number:Mass                  MyPikoIq100_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_CO2SAVING_TOTAL"}
Number:Mass                  MyPikoIq100_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_CO2SAVING_YEAR"}
Number:Energy                MyPikoIq100_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_HOMECONSUMPTION_DAY"}
Number:Energy                MyPikoIq100_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_HOMECONSUMPTION_MONTH"}
Number:Energy                MyPikoIq100_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPikoIq100_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_HOMECONSUMPTION_YEAR"}
Number:Energy                MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY"}
Number:Energy                MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH"}
Number:Energy                MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL"}
Number:Energy                MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR"}
Number:Energy                MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_HOMECONSUMPTION_FROM_PV_DAY"}
Number:Energy                MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH"}
Number:Energy                MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL"}
Number:Energy                MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR"}
Number:Dimensionless         MyPikoIq100_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_OWNCONSUMPTION_RATE_DAY"}
Number:Dimensionless         MyPikoIq100_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_OWNCONSUMPTION_RATE_MONTH"}
Number:Dimensionless         MyPikoIq100_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_OWNCONSUMPTION_RATE_TOTAL"}
Number:Dimensionless         MyPikoIq100_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_OWNCONSUMPTION_RATE_YEAR"}
Number:Energy                MyPikoIq100_STATISTIC_YIELD_DAY                           <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_YIELD_DAY"}
Number:Energy                MyPikoIq100_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_YIELD_MONTH"}
Number:Energy                MyPikoIq100_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_YIELD_TOTAL"}
Number:Energy                MyPikoIq100_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalpikoiqplenticore:PIKOIQ100:MyPikoIq100:STATISTIC_YIELD_YEAR"}
```

sitemap configuration:

```
sitemap PIKOIQ100  label="KOSTAL PIKO IQ 10.0" {
  Frame label="Live data" {
    Group item=MyPikoIq100_DEVICE_LOCAL_DC_POWER label="Solar energy" icon="solarplant" {
      Text item=MyPikoIq100_DEVICE_LOCAL_DC_POWER label="Solar energy (total)" icon="solarplant"
      Group item=MyPikoIq100_DEVICE_LOCAL_PVSTRING_1_POWER label="PV string 1" icon="solarplant"
      {    
        Text item=MyPikoIq100_DEVICE_LOCAL_PVSTRING_1_POWER label="Power"
        Text item=MyPikoIq100_DEVICE_LOCAL_PVSTRING_1_AMPERAGE label="Amperage"  
        Text item=MyPikoIq100_DEVICE_LOCAL_PVSTRING_1_VOLTAGE label="Voltage"
      }
      Group item=MyPikoIq100_DEVICE_LOCAL_PVSTRING_2_POWER label="PV string 2" icon="solarplant"
      {
        Text item=MyPikoIq100_DEVICE_LOCAL_PVSTRING_2_POWER label="Power"
        Text item=MyPikoIq100_DEVICE_LOCAL_PVSTRING_2_AMPERAGE label="Amperage"  
        Text item=MyPikoIq100_DEVICE_LOCAL_PVSTRING_2_VOLTAGE label="Voltage"
      }
    }
    Group item=MyPikoIq100_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC" {
    Frame label="Overall" {
      Text item=MyPikoIq100_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC"
    }
    Frame label="Phase 1" {
      Text item=MyPikoIq100_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER label="Current power"
      Text item=MyPikoIq100_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPikoIq100_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 2" {
      Text item=MyPikoIq100_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER label="Current power"
      Text item=MyPikoIq100_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPikoIq100_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 3" {
      Text item=MyPikoIq100_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER label="Current power"
      Text item=MyPikoIq100_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPikoIq100_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE label="Current voltage"
    }
    }
    Group item=MyPikoIq100_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Home comsumption" icon="house" {
      Text item=MyPikoIq100_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Total" icon="poweroutlet"
      Text item=MyPikoIq100_DEVICE_LOCAL_OWNCONSUMPTION label="Owncomsumption" icon="house"
      Text item=MyPikoIq100_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID label="From grid" icon="energy"
      Text item=MyPikoIq100_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV  label="From photovoltaic" icon="sun_clouds"
    }
  }
  Frame label="Daily report" icon="line"{
    Text item=MyPikoIq100_STATISTIC_YIELD_DAY label="Yield" icon="line"
    Text item=MyPikoIq100_STATISTIC_AUTARKY_DAY label="Autarchy level" icon="garden"
    Text item=MyPikoIq100_STATISTIC_CO2SAVING_DAY label="Co2 saving" icon="carbondioxide"
    Group item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_DAY label="Homeconsumption" icon="pie"
    {
      Text item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_DAY label="Total consumption" icon="poweroutlet"
      Text item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY label="From grid" icon="energy"
      Text item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY label="From photovoltaic" icon="sun_clouds"
      Text item=MyPikoIq100_STATISTIC_OWNCONSUMPTION_RATE_DAY label="Ownconsumption rate" icon="price"
    }
  }
  Frame label="Monthly report" {
      Text item=MyPikoIq100_STATISTIC_YIELD_MONTH label="Yield" icon="line"
      Text item=MyPikoIq100_STATISTIC_AUTARKY_MONTH label="Autarchy level" icon="garden"
      Text item=MyPikoIq100_STATISTIC_CO2SAVING_MONTH label="Co2 saving" icon="carbondioxide"  
      Group item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_MONTH label="Homeconsumption" icon="pie"
      {
        Text item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_MONTH label="Total consumption" icon="poweroutlet"
        Text item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH label="From grid" icon="energy"
        Text item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH label="From photovoltaic" icon="sun_clouds"
        Text item=MyPikoIq100_STATISTIC_OWNCONSUMPTION_RATE_MONTH label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Yearly report" {
      Text item=MyPikoIq100_STATISTIC_YIELD_YEAR label="Yield" icon="line"
      Text item=MyPikoIq100_STATISTIC_AUTARKY_YEAR label="Autarchy level" icon="garden"
      Text item=MyPikoIq100_STATISTIC_CO2SAVING_YEAR label="Co2 saving" icon="carbondioxide"
      Group item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_YEAR label="Homeconsumption" icon="pie"
      {
        Text item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_YEAR label="Total consumption" icon="poweroutlet"
        Text item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR label="From grid" icon="energy"
        Text item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR label="From photovoltaic" icon="sun_clouds"
        Text item=MyPikoIq100_STATISTIC_OWNCONSUMPTION_RATE_YEAR label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Overall report" {
      Text item=MyPikoIq100_STATISTIC_YIELD_TOTAL label="Yield" icon="line"
      Text item=MyPikoIq100_STATISTIC_AUTARKY_TOTAL label="Autarchy level" icon="garden"
      Text item=MyPikoIq100_STATISTIC_CO2SAVING_TOTAL label="Co2 saving" icon="carbondioxide"    
      Group item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_TOTAL label="Homeconsumption" icon="pie"
      {
        Text item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_TOTAL label="Total consumption" icon="poweroutlet"
        Text item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL label="From grid" icon="energy"
        Text item=MyPikoIq100_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL label="From photovoltaic" icon="sun_clouds"
        Text item=MyPikoIq100_STATISTIC_OWNCONSUMPTION_RATE_TOTAL label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Generated errors" {
      Text item=MyPikoIq100_SCB_EVENT_ERROR_COUNT_MC label="MainController error count" icon="error"
      Text item=MyPikoIq100_SCB_EVENT_ERROR_COUNT_SFH label="Grid controller error count" icon="error"
      Text item=MyPikoIq100_SCB_EVENT_ERROR_COUNT_SCB label="SmartCommunicationBoard error count" icon="error"  
      Text item=MyPikoIq100_SCB_EVENT_WARNING_COUNT_SCB label="SmartCommunicationBoard warning count" icon="error"
  }
  Frame label="Permitted feed-in quantity" {
    Text item=MyPikoIq100_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE label="Absolute"
    Text item=MyPikoIq100_DEVICE_LOCAL_LIMIT_EVU_RELATIV label="Relative"
  }
}
```

<u><b>PIKO PLENTICORE plus 4.2 (with battery) example</b></u>

thing configuration:

```
Thing kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="60"]
```

items configuration:

```
Number:Energy                MyPlentiCore42WithBattery_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_DC_POWER"}
Number:Energy                MyPlentiCore42WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY"}
Number:Energy                MyPlentiCore42WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID"}
Number:Energy                MyPlentiCore42WithBattery_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_OWNCONSUMPTION"}
Number:Energy                MyPlentiCore42WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV"}
Number:Energy                MyPlentiCore42WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore42WithBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE"}
Number:Dimensionless         MyPlentiCore42WithBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_LIMIT_EVU_RELATIV"}
Number:Time                  MyPlentiCore42WithBattery_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_WORKTIME"}
Number:ElectricCurrent       MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE"}
Number:Energy                MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_AC_CURRENT_POWER"}
Number:Dimensionless         MyPlentiCore42WithBattery_DEVICE_LOCAL_BATTERY_LOADING_CYCLES           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_BATTERY_LOADING_CYCLES"}
Number:ElectricCharge        MyPlentiCore42WithBattery_DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY"}
Number:ElectricCurrent       MyPlentiCore42WithBattery_DEVICE_LOCAL_BATTERY_AMPERAGE                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_BATTERY_AMPERAGE"}
Number:Energy                MyPlentiCore42WithBattery_DEVICE_LOCAL_BATTERY_POWER                    <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_BATTERY_POWER"}
Number:Dimensionless         MyPlentiCore42WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE"}
Number:ElectricPotential     MyPlentiCore42WithBattery_DEVICE_LOCAL_BATTERY_VOLTAGE                  <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_BATTERY_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore42WithBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_PVSTRING_1_AMPERAGE"}
Number:Energy                MyPlentiCore42WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_PVSTRING_1_POWER"}
Number:ElectricPotential     MyPlentiCore42WithBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_PVSTRING_1_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore42WithBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_PVSTRING_2_AMPERAGE"}
Number:Energy                MyPlentiCore42WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_PVSTRING_2_POWER"}
Number:ElectricPotential     MyPlentiCore42WithBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:DEVICE_LOCAL_PVSTRING_2_VOLTAGE"}
Number:Dimensionless         MyPlentiCore42WithBattery_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:SCB_EVENT_ERROR_COUNT_MC"}
Number:Dimensionless         MyPlentiCore42WithBattery_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:SCB_EVENT_ERROR_COUNT_SFH"}
Number:Dimensionless         MyPlentiCore42WithBattery_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:SCB_EVENT_ERROR_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore42WithBattery_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:SCB_EVENT_WARNING_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore42WithBattery_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_AUTARKY_DAY"}
Number:Dimensionless         MyPlentiCore42WithBattery_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_AUTARKY_MONTH"}
Number:Dimensionless         MyPlentiCore42WithBattery_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_AUTARKY_TOTAL"}
Number:Dimensionless         MyPlentiCore42WithBattery_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_AUTARKY_YEAR"}
Number:Mass                  MyPlentiCore42WithBattery_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_CO2SAVING_DAY"}
Number:Mass                  MyPlentiCore42WithBattery_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_CO2SAVING_MONTH"}
Number:Mass                  MyPlentiCore42WithBattery_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_CO2SAVING_TOTAL"}
Number:Mass                  MyPlentiCore42WithBattery_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_CO2SAVING_YEAR"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_DAY"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_MONTH"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_YEAR"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR  <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_DAY"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR"}
Number:Dimensionless         MyPlentiCore42WithBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_OWNCONSUMPTION_RATE_DAY"}
Number:Dimensionless         MyPlentiCore42WithBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_OWNCONSUMPTION_RATE_MONTH"}
Number:Dimensionless         MyPlentiCore42WithBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_OWNCONSUMPTION_RATE_TOTAL"}
Number:Dimensionless         MyPlentiCore42WithBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_OWNCONSUMPTION_RATE_YEAR"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_YIELD_DAY                           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_YIELD_DAY"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_YIELD_MONTH"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_YIELD_TOTAL"}
Number:Energy                MyPlentiCore42WithBattery_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHBATTERY:MyPlentiCore42WithBattery:STATISTIC_YIELD_YEAR"}
```

sitemap configuration:

```
sitemap PLENTICORE42WITHBATTERY  label="KOSTAL PLENTICORE 4.2 (with battery)" {
  Frame label="Live data" {
    Group item=MyPlentiCore42WithBattery_DEVICE_LOCAL_DC_POWER label="Solar energy" icon="solarplant" {
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_DC_POWER label="Solar energy (total)" icon="solarplant"
      Group item=MyPlentiCore42WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="PV string 1" icon="solarplant"
      {    
        Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="Power"
        Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE label="Voltage"
      }
      Group item=MyPlentiCore42WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="PV string 2" icon="solarplant"
      {
        Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="Power"
        Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE label="Voltage"
      }
    }
    Group item=MyPlentiCore42WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE label="Battery charge" icon="batterylevel" {
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_BATTERY_LOADING_CYCLES label="Loading cycles" icon="battery"
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY label="Full charge capacity" icon="batterylevel"
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_BATTERY_AMPERAGE label="Battery amperage"
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_BATTERY_POWER label="Battery power"
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE label="Battery charge" icon="batterylevel"
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_BATTERY_VOLTAGE label="Battery voltage"
    }
    Group item=MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC" {
    Frame label="Overall" {
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC"
    }
    Frame label="Phase 1" {
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 2" {
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 3" {
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE label="Current voltage"
    }
    }
    Group item=MyPlentiCore42WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Home comsumption" icon="house" {
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Total" icon="poweroutlet"
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_OWNCONSUMPTION label="Owncomsumption" icon="house"
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY label="From battery" icon="battery"
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID label="From grid" icon="energy"
      Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV  label="From photovoltaic" icon="sun_clouds"
    }
  }
  Frame label="Daily report" icon="line"{
    Text item=MyPlentiCore42WithBattery_STATISTIC_YIELD_DAY label="Yield" icon="line"
    Text item=MyPlentiCore42WithBattery_STATISTIC_AUTARKY_DAY label="Autarchy level" icon="garden"
    Text item=MyPlentiCore42WithBattery_STATISTIC_CO2SAVING_DAY label="Co2 saving" icon="carbondioxide"
    Group item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_DAY label="Homeconsumption" icon="pie"
    {
      Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_DAY label="Total consumption" icon="poweroutlet"
      Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY label="From battery" icon="battery"
      Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY label="From grid" icon="energy"
      Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY label="From photovoltaic" icon="sun_clouds"
      Text item=MyPlentiCore42WithBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY label="Ownconsumption rate" icon="price"
    }
  }
  Frame label="Monthly report" {
      Text item=MyPlentiCore42WithBattery_STATISTIC_YIELD_MONTH label="Yield" icon="line"
      Text item=MyPlentiCore42WithBattery_STATISTIC_AUTARKY_MONTH label="Autarchy level" icon="garden"
      Text item=MyPlentiCore42WithBattery_STATISTIC_CO2SAVING_MONTH label="Co2 saving" icon="carbondioxide"  
      Group item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH label="From battery" icon="battery"
        Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH label="From grid" icon="energy"
        Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore42WithBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Yearly report" {
      Text item=MyPlentiCore42WithBattery_STATISTIC_YIELD_YEAR label="Yield" icon="line"
      Text item=MyPlentiCore42WithBattery_STATISTIC_AUTARKY_YEAR label="Autarchy level" icon="garden"
      Text item=MyPlentiCore42WithBattery_STATISTIC_CO2SAVING_YEAR label="Co2 saving" icon="carbondioxide"
      Group item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR label="From battery" icon="battery"
        Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR label="From grid" icon="energy"
        Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore42WithBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Overall report" {
      Text item=MyPlentiCore42WithBattery_STATISTIC_YIELD_TOTAL label="Yield" icon="line"
      Text item=MyPlentiCore42WithBattery_STATISTIC_AUTARKY_TOTAL label="Autarchy level" icon="garden"
      Text item=MyPlentiCore42WithBattery_STATISTIC_CO2SAVING_TOTAL label="Co2 saving" icon="carbondioxide"    
      Group item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL label="From battery" icon="battery"
        Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL label="From grid" icon="energy"
        Text item=MyPlentiCore42WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore42WithBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Generated errors" {
      Text item=MyPlentiCore42WithBattery_SCB_EVENT_ERROR_COUNT_MC label="MainController error count" icon="error"
      Text item=MyPlentiCore42WithBattery_SCB_EVENT_ERROR_COUNT_SFH label="Grid controller error count" icon="error"
      Text item=MyPlentiCore42WithBattery_SCB_EVENT_ERROR_COUNT_SCB label="SmartCommunicationBoard error count" icon="error"  
      Text item=MyPlentiCore42WithBattery_SCB_EVENT_WARNING_COUNT_SCB label="SmartCommunicationBoard warning count" icon="error"
  }
  Frame label="Permitted feed-in quantity" {
    Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE label="Absolute"
    Text item=MyPlentiCore42WithBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV label="Relative"
  }
}
```

<u><b>PIKO PLENTICORE plus 4.2 (without battery) example</b></u>

thing configuration:

```
Thing kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="60"]
```

items configuration:

```
Number:Energy                MyPlentiCore42WithoutBattery_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_DC_POWER"}
Number:Energy                MyPlentiCore42WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID"}
Number:Energy                MyPlentiCore42WithoutBattery_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_OWNCONSUMPTION"}
Number:Energy                MyPlentiCore42WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV"}
Number:Energy                MyPlentiCore42WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore42WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE"}
Number:Dimensionless         MyPlentiCore42WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_LIMIT_EVU_RELATIV"}
Number:Time                  MyPlentiCore42WithoutBattery_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_WORKTIME"}
Number:ElectricCurrent       MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE"}
Number:Energy                MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_AC_CURRENT_POWER"}
Number:ElectricCurrent       MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_PVSTRING_1_AMPERAGE"}
Number:Energy                MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_PVSTRING_1_POWER"}
Number:ElectricPotential     MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_PVSTRING_1_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_PVSTRING_2_AMPERAGE"}
Number:Energy                MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_PVSTRING_2_POWER"}
Number:ElectricPotential     MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_PVSTRING_2_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_3_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_PVSTRING_3_AMPERAGE"}Number:Energy                MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_3_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_PVSTRING_3_POWER"}
Number:ElectricPotential     MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_3_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:DEVICE_LOCAL_PVSTRING_3_VOLTAGE"}
Number:Dimensionless         MyPlentiCore42WithoutBattery_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:SCB_EVENT_ERROR_COUNT_MC"}
Number:Dimensionless         MyPlentiCore42WithoutBattery_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:SCB_EVENT_ERROR_COUNT_SFH"}
Number:Dimensionless         MyPlentiCore42WithoutBattery_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:SCB_EVENT_ERROR_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore42WithoutBattery_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:SCB_EVENT_WARNING_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore42WithoutBattery_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_AUTARKY_DAY"}
Number:Dimensionless         MyPlentiCore42WithoutBattery_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_AUTARKY_MONTH"}
Number:Dimensionless         MyPlentiCore42WithoutBattery_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_AUTARKY_TOTAL"}
Number:Dimensionless         MyPlentiCore42WithoutBattery_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_AUTARKY_YEAR"}
Number:Mass                  MyPlentiCore42WithoutBattery_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_CO2SAVING_DAY"}
Number:Mass                  MyPlentiCore42WithoutBattery_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_CO2SAVING_MONTH"}
Number:Mass                  MyPlentiCore42WithoutBattery_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_CO2SAVING_TOTAL"}
Number:Mass                  MyPlentiCore42WithoutBattery_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_CO2SAVING_YEAR"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_HOMECONSUMPTION_DAY"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_HOMECONSUMPTION_MONTH"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_HOMECONSUMPTION_YEAR"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_DAY"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR"}
Number:Dimensionless         MyPlentiCore42WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_DAY"}
Number:Dimensionless         MyPlentiCore42WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_MONTH"}
Number:Dimensionless         MyPlentiCore42WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_TOTAL"}
Number:Dimensionless         MyPlentiCore42WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_YEAR"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_YIELD_DAY                           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_YIELD_DAY"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_YIELD_MONTH"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_YIELD_TOTAL"}
Number:Energy                MyPlentiCore42WithoutBattery_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS42WITHOUTBATTERY:MyPlentiCore42WithoutBattery:STATISTIC_YIELD_YEAR"}
```

sitemap configuration:

```
sitemap PLENTICORE42WITHOUTBATTERY  label="KOSTAL PLENTICORE 4.2 (no battery)" {
  Frame label="Live data" {
    Group item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_DC_POWER label="Solar energy" icon="solarplant" {
      Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_DC_POWER label="Solar energy (total)" icon="solarplant"
      Group item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="PV string 1" icon="solarplant"
      {    
        Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="Power"
        Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE label="Voltage"
      }
      Group item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="PV string 2" icon="solarplant"
      {
        Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="Power"
        Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE label="Voltage"
      }
      Group item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_3_POWER label="PV string 3" icon="solarplant"
      {
        Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_3_POWER label="Power"
        Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_3_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_PVSTRING_3_VOLTAGE label="Voltage"
      }
    }
    Group item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC" {
    Frame label="Overall" {
      Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC"
    }
    Frame label="Phase 1" {
      Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 2" {
      Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 3" {
      Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE label="Current voltage"
    }
    }
    Group item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Home comsumption" icon="house" {
      Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Total" icon="poweroutlet"
      Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_OWNCONSUMPTION label="Owncomsumption" icon="house"
      Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID label="From grid" icon="energy"
      Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV  label="From photovoltaic" icon="sun_clouds"
    }
  }
  Frame label="Daily report" icon="line"{
    Text item=MyPlentiCore42WithoutBattery_STATISTIC_YIELD_DAY label="Yield" icon="line"
    Text item=MyPlentiCore42WithoutBattery_STATISTIC_AUTARKY_DAY label="Autarchy level" icon="garden"
    Text item=MyPlentiCore42WithoutBattery_STATISTIC_CO2SAVING_DAY label="Co2 saving" icon="carbondioxide"
    Group item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_DAY label="Homeconsumption" icon="pie"
    {
      Text item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_DAY label="Total consumption" icon="poweroutlet"
      Text item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY label="From grid" icon="energy"
      Text item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY label="From photovoltaic" icon="sun_clouds"
      Text item=MyPlentiCore42WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY label="Ownconsumption rate" icon="price"
    }
  }
  Frame label="Monthly report" {
      Text item=MyPlentiCore42WithoutBattery_STATISTIC_YIELD_MONTH label="Yield" icon="line"
      Text item=MyPlentiCore42WithoutBattery_STATISTIC_AUTARKY_MONTH label="Autarchy level" icon="garden"
      Text item=MyPlentiCore42WithoutBattery_STATISTIC_CO2SAVING_MONTH label="Co2 saving" icon="carbondioxide"  
      Group item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH label="From grid" icon="energy"
        Text item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore42WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Yearly report" {
      Text item=MyPlentiCore42WithoutBattery_STATISTIC_YIELD_YEAR label="Yield" icon="line"
      Text item=MyPlentiCore42WithoutBattery_STATISTIC_AUTARKY_YEAR label="Autarchy level" icon="garden"
      Text item=MyPlentiCore42WithoutBattery_STATISTIC_CO2SAVING_YEAR label="Co2 saving" icon="carbondioxide"
      Group item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR label="From grid" icon="energy"
        Text item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore42WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Overall report" {
      Text item=MyPlentiCore42WithoutBattery_STATISTIC_YIELD_TOTAL label="Yield" icon="line"
      Text item=MyPlentiCore42WithoutBattery_STATISTIC_AUTARKY_TOTAL label="Autarchy level" icon="garden"
      Text item=MyPlentiCore42WithoutBattery_STATISTIC_CO2SAVING_TOTAL label="Co2 saving" icon="carbondioxide"    
      Group item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL label="From grid" icon="energy"
        Text item=MyPlentiCore42WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore42WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Generated errors" {
      Text item=MyPlentiCore42WithoutBattery_SCB_EVENT_ERROR_COUNT_MC label="MainController error count" icon="error"
      Text item=MyPlentiCore42WithoutBattery_SCB_EVENT_ERROR_COUNT_SFH label="Grid controller error count" icon="error"
      Text item=MyPlentiCore42WithoutBattery_SCB_EVENT_ERROR_COUNT_SCB label="SmartCommunicationBoard error count" icon="error"  
      Text item=MyPlentiCore42WithoutBattery_SCB_EVENT_WARNING_COUNT_SCB label="SmartCommunicationBoard warning count" icon="error"
  }
  Frame label="Permitted feed-in quantity" {
    Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE label="Absolute"
    Text item=MyPlentiCore42WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV label="Relative"
  }
}
```

<u><b>PIKO PLENTICORE plus 5.5 (with battery) example</b></u>

thing configuration:

```
Thing kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="60"]
```

items configuration:

```
Number:Energy                MyPlentiCore55WithBattery_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_DC_POWER"}
Number:Energy                MyPlentiCore55WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY"}
Number:Energy                MyPlentiCore55WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID"}
Number:Energy                MyPlentiCore55WithBattery_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_OWNCONSUMPTION"}
Number:Energy                MyPlentiCore55WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV"}
Number:Energy                MyPlentiCore55WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore55WithBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE"}
Number:Dimensionless         MyPlentiCore55WithBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_LIMIT_EVU_RELATIV"}
Number:Time                  MyPlentiCore55WithBattery_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_WORKTIME"}
Number:ElectricCurrent       MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE"}
Number:Energy                MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_AC_CURRENT_POWER"}
Number:Dimensionless         MyPlentiCore55WithBattery_DEVICE_LOCAL_BATTERY_LOADING_CYCLES           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_BATTERY_LOADING_CYCLES"}
Number:ElectricCharge        MyPlentiCore55WithBattery_DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY"}
Number:ElectricCurrent       MyPlentiCore55WithBattery_DEVICE_LOCAL_BATTERY_AMPERAGE                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_BATTERY_AMPERAGE"}
Number:Energy                MyPlentiCore55WithBattery_DEVICE_LOCAL_BATTERY_POWER                    <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_BATTERY_POWER"}
Number:Dimensionless         MyPlentiCore55WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE"}
Number:ElectricPotential     MyPlentiCore55WithBattery_DEVICE_LOCAL_BATTERY_VOLTAGE                  <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_BATTERY_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore55WithBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_PVSTRING_1_AMPERAGE"}
Number:Energy                MyPlentiCore55WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_PVSTRING_1_POWER"}
Number:ElectricPotential     MyPlentiCore55WithBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_PVSTRING_1_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore55WithBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_PVSTRING_2_AMPERAGE"}
Number:Energy                MyPlentiCore55WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_PVSTRING_2_POWER"}
Number:ElectricPotential     MyPlentiCore55WithBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:DEVICE_LOCAL_PVSTRING_2_VOLTAGE"}
Number:Dimensionless         MyPlentiCore55WithBattery_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:SCB_EVENT_ERROR_COUNT_MC"}
Number:Dimensionless         MyPlentiCore55WithBattery_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:SCB_EVENT_ERROR_COUNT_SFH"}
Number:Dimensionless         MyPlentiCore55WithBattery_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:SCB_EVENT_ERROR_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore55WithBattery_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:SCB_EVENT_WARNING_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore55WithBattery_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_AUTARKY_DAY"}
Number:Dimensionless         MyPlentiCore55WithBattery_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_AUTARKY_MONTH"}
Number:Dimensionless         MyPlentiCore55WithBattery_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_AUTARKY_TOTAL"}
Number:Dimensionless         MyPlentiCore55WithBattery_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_AUTARKY_YEAR"}
Number:Mass                  MyPlentiCore55WithBattery_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_CO2SAVING_DAY"}
Number:Mass                  MyPlentiCore55WithBattery_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_CO2SAVING_MONTH"}
Number:Mass                  MyPlentiCore55WithBattery_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_CO2SAVING_TOTAL"}
Number:Mass                  MyPlentiCore55WithBattery_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_CO2SAVING_YEAR"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_DAY"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_MONTH"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_YEAR"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR  <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_DAY"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR"}
Number:Dimensionless         MyPlentiCore55WithBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_OWNCONSUMPTION_RATE_DAY"}
Number:Dimensionless         MyPlentiCore55WithBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_OWNCONSUMPTION_RATE_MONTH"}
Number:Dimensionless         MyPlentiCore55WithBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_OWNCONSUMPTION_RATE_TOTAL"}
Number:Dimensionless         MyPlentiCore55WithBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_OWNCONSUMPTION_RATE_YEAR"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_YIELD_DAY                           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_YIELD_DAY"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_YIELD_MONTH"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_YIELD_TOTAL"}
Number:Energy                MyPlentiCore55WithBattery_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHBATTERY:MyPlentiCore55WithBattery:STATISTIC_YIELD_YEAR"}
```

sitemap configuration:

```
sitemap PLENTICORE55WITHBATTERY  label="KOSTAL PLENTICORE 5.5 (with battery)" {
    Frame label="Live data" {
        Group item=MyPlentiCore55WithBattery_DEVICE_LOCAL_DC_POWER label="Solar energy" icon="solarplant" {
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_DC_POWER label="Solar energy (total)" icon="solarplant"
            Group item=MyPlentiCore55WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="PV string 1" icon="solarplant"
            {       
                Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="Power"
                Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE label="Amperage"   
                Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE label="Voltage"
            }
            Group item=MyPlentiCore55WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="PV string 2" icon="solarplant"
            {
                Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="Power"
                Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE label="Amperage"   
                Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE label="Voltage"
            }
        }
        Group item=MyPlentiCore55WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE label="Battery charge" icon="batterylevel" {
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_BATTERY_LOADING_CYCLES label="Loading cycles" icon="battery"
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY label="Full charge capacity" icon="batterylevel"
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_BATTERY_AMPERAGE label="Battery amperage"
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_BATTERY_POWER label="Battery power"
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE label="Battery charge" icon="batterylevel"
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_BATTERY_VOLTAGE label="Battery voltage"
        }
        Group item=MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC" {
        Frame label="Overall" {
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC"
        }
        Frame label="Phase 1" {
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER label="Current power"
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE label="Current amperage"
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE label="Current voltage"
        }
        Frame label="Phase 2" {
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER label="Current power"
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE label="Current amperage"
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE label="Current voltage"
        }
        Frame label="Phase 3" {
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER label="Current power"
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE label="Current amperage"
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE label="Current voltage"
        }
        }
        Group item=MyPlentiCore55WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Home comsumption" icon="house" {
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Total" icon="poweroutlet"
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_OWNCONSUMPTION label="Owncomsumption" icon="house"
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY label="From battery" icon="battery"
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID label="From grid" icon="energy"
            Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV  label="From photovoltaic" icon="sun_clouds"
        }
    }
    Frame label="Daily report" icon="line"{
        Text item=MyPlentiCore55WithBattery_STATISTIC_YIELD_DAY label="Yield" icon="line"
        Text item=MyPlentiCore55WithBattery_STATISTIC_AUTARKY_DAY label="Autarchy level" icon="garden"
        Text item=MyPlentiCore55WithBattery_STATISTIC_CO2SAVING_DAY label="Co2 saving" icon="carbondioxide"
        Group item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_DAY label="Homeconsumption" icon="pie"
        {
            Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_DAY label="Total consumption" icon="poweroutlet"
            Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY label="From battery" icon="battery"
            Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY label="From grid" icon="energy"
            Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY label="From photovoltaic" icon="sun_clouds"
            Text item=MyPlentiCore55WithBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY label="Ownconsumption rate" icon="price"
        }
    }
    Frame label="Monthly report" {
            Text item=MyPlentiCore55WithBattery_STATISTIC_YIELD_MONTH label="Yield" icon="line"
            Text item=MyPlentiCore55WithBattery_STATISTIC_AUTARKY_MONTH label="Autarchy level" icon="garden"
            Text item=MyPlentiCore55WithBattery_STATISTIC_CO2SAVING_MONTH label="Co2 saving" icon="carbondioxide"   
            Group item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Homeconsumption" icon="pie"
            {
                Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Total consumption" icon="poweroutlet"
                Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH label="From battery" icon="battery"
                Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH label="From grid" icon="energy"
                Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH label="From photovoltaic" icon="sun_clouds"
                Text item=MyPlentiCore55WithBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH label="Ownconsumption rate" icon="price"
            }
    }
    Frame label="Yearly report" {
            Text item=MyPlentiCore55WithBattery_STATISTIC_YIELD_YEAR label="Yield" icon="line"
            Text item=MyPlentiCore55WithBattery_STATISTIC_AUTARKY_YEAR label="Autarchy level" icon="garden"
            Text item=MyPlentiCore55WithBattery_STATISTIC_CO2SAVING_YEAR label="Co2 saving" icon="carbondioxide"
            Group item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Homeconsumption" icon="pie"
            {
                Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Total consumption" icon="poweroutlet"
                Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR label="From battery" icon="battery"
                Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR label="From grid" icon="energy"
                Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR label="From photovoltaic" icon="sun_clouds"
                Text item=MyPlentiCore55WithBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR label="Ownconsumption rate" icon="price"
            }
    }
    Frame label="Overall report" {
            Text item=MyPlentiCore55WithBattery_STATISTIC_YIELD_TOTAL label="Yield" icon="line"
            Text item=MyPlentiCore55WithBattery_STATISTIC_AUTARKY_TOTAL label="Autarchy level" icon="garden"
            Text item=MyPlentiCore55WithBattery_STATISTIC_CO2SAVING_TOTAL label="Co2 saving" icon="carbondioxide"       
            Group item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Homeconsumption" icon="pie"
            {
                Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Total consumption" icon="poweroutlet"
                Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL label="From battery" icon="battery"
                Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL label="From grid" icon="energy"
                Text item=MyPlentiCore55WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL label="From photovoltaic" icon="sun_clouds"
                Text item=MyPlentiCore55WithBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL label="Ownconsumption rate" icon="price"
            }
    }
    Frame label="Generated errors" {
            Text item=MyPlentiCore55WithBattery_SCB_EVENT_ERROR_COUNT_MC label="MainController error count" icon="error"
            Text item=MyPlentiCore55WithBattery_SCB_EVENT_ERROR_COUNT_SFH label="Grid controller error count" icon="error"
            Text item=MyPlentiCore55WithBattery_SCB_EVENT_ERROR_COUNT_SCB label="SmartCommunicationBoard error count" icon="error"  
            Text item=MyPlentiCore55WithBattery_SCB_EVENT_WARNING_COUNT_SCB label="SmartCommunicationBoard warning count" icon="error"
    }
    Frame label="Permitted feed-in quantity" {
        Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE label="Absolute"
        Text item=MyPlentiCore55WithBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV label="Relative"
    }
}
```


<u><b>PIKO PLENTICORE plus 5.5 (without battery) example</b></u>

thing configuration:

```
Thing kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="60"]
```

items configuration:

```
Number:Energy                MyPlentiCore55WithoutBattery_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_DC_POWER"}
Number:Energy                MyPlentiCore55WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID"}
Number:Energy                MyPlentiCore55WithoutBattery_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_OWNCONSUMPTION"}
Number:Energy                MyPlentiCore55WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV"}
Number:Energy                MyPlentiCore55WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore55WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE"}
Number:Dimensionless         MyPlentiCore55WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_LIMIT_EVU_RELATIV"}
Number:Time                  MyPlentiCore55WithoutBattery_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_WORKTIME"}
Number:ElectricCurrent       MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE"}
Number:Energy                MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_AC_CURRENT_POWER"}
Number:ElectricCurrent       MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_PVSTRING_1_AMPERAGE"}
Number:Energy                MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_PVSTRING_1_POWER"}
Number:ElectricPotential     MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_PVSTRING_1_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_PVSTRING_2_AMPERAGE"}
Number:Energy                MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_PVSTRING_2_POWER"}
Number:ElectricPotential     MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_PVSTRING_2_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_3_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_PVSTRING_3_AMPERAGE"}
Number:Energy                MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_3_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_PVSTRING_3_POWER"}
Number:ElectricPotential     MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_3_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:DEVICE_LOCAL_PVSTRING_3_VOLTAGE"}
Number:Dimensionless         MyPlentiCore55WithoutBattery_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:SCB_EVENT_ERROR_COUNT_MC"}
Number:Dimensionless         MyPlentiCore55WithoutBattery_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:SCB_EVENT_ERROR_COUNT_SFH"}
Number:Dimensionless         MyPlentiCore55WithoutBattery_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:SCB_EVENT_ERROR_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore55WithoutBattery_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:SCB_EVENT_WARNING_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore55WithoutBattery_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_AUTARKY_DAY"}
Number:Dimensionless         MyPlentiCore55WithoutBattery_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_AUTARKY_MONTH"}
Number:Dimensionless         MyPlentiCore55WithoutBattery_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_AUTARKY_TOTAL"}
Number:Dimensionless         MyPlentiCore55WithoutBattery_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_AUTARKY_YEAR"}
Number:Mass                  MyPlentiCore55WithoutBattery_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_CO2SAVING_DAY"}
Number:Mass                  MyPlentiCore55WithoutBattery_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_CO2SAVING_MONTH"}
Number:Mass                  MyPlentiCore55WithoutBattery_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_CO2SAVING_TOTAL"}
Number:Mass                  MyPlentiCore55WithoutBattery_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_CO2SAVING_YEAR"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_HOMECONSUMPTION_DAY"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_HOMECONSUMPTION_MONTH"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_HOMECONSUMPTION_YEAR"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_DAY"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR"}
Number:Dimensionless         MyPlentiCore55WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_DAY"}
Number:Dimensionless         MyPlentiCore55WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_MONTH"}
Number:Dimensionless         MyPlentiCore55WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_TOTAL"}
Number:Dimensionless         MyPlentiCore55WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_YEAR"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_YIELD_DAY                           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_YIELD_DAY"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_YIELD_MONTH"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_YIELD_TOTAL"}
Number:Energy                MyPlentiCore55WithoutBattery_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS55WITHOUTBATTERY:MyPlentiCore55WithoutBattery:STATISTIC_YIELD_YEAR"}

```

sitemap configuration:

```
sitemap PLENTICORE55WITHOUTBATTERY  label="KOSTAL PLENTICORE 5.5 (no battery)" {
  Frame label="Live data" {
    Group item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_DC_POWER label="Solar energy" icon="solarplant" {
      Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_DC_POWER label="Solar energy (total)" icon="solarplant"
      Group item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="PV string 1" icon="solarplant"
      {    
        Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="Power"
        Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE label="Voltage"
      }
      Group item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="PV string 2" icon="solarplant"
      {
        Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="Power"
        Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE label="Voltage"
      }
      Group item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_3_POWER label="PV string 3" icon="solarplant"
      {
        Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_3_POWER label="Power"
        Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_3_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_PVSTRING_3_VOLTAGE label="Voltage"
      }
    }
    Group item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC" {
    Frame label="Overall" {
      Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC"
    }
    Frame label="Phase 1" {
      Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 2" {
      Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 3" {
      Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE label="Current voltage"
    }
    }
    Group item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Home comsumption" icon="house" {
      Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Total" icon="poweroutlet"
      Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_OWNCONSUMPTION label="Owncomsumption" icon="house"
      Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID label="From grid" icon="energy"
      Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV  label="From photovoltaic" icon="sun_clouds"
    }
  }
  Frame label="Daily report" icon="line"{
    Text item=MyPlentiCore55WithoutBattery_STATISTIC_YIELD_DAY label="Yield" icon="line"
    Text item=MyPlentiCore55WithoutBattery_STATISTIC_AUTARKY_DAY label="Autarchy level" icon="garden"
    Text item=MyPlentiCore55WithoutBattery_STATISTIC_CO2SAVING_DAY label="Co2 saving" icon="carbondioxide"
    Group item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_DAY label="Homeconsumption" icon="pie"
    {
      Text item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_DAY label="Total consumption" icon="poweroutlet"
      Text item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY label="From grid" icon="energy"
      Text item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY label="From photovoltaic" icon="sun_clouds"
      Text item=MyPlentiCore55WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY label="Ownconsumption rate" icon="price"
    }
  }
  Frame label="Monthly report" {
      Text item=MyPlentiCore55WithoutBattery_STATISTIC_YIELD_MONTH label="Yield" icon="line"
      Text item=MyPlentiCore55WithoutBattery_STATISTIC_AUTARKY_MONTH label="Autarchy level" icon="garden"
      Text item=MyPlentiCore55WithoutBattery_STATISTIC_CO2SAVING_MONTH label="Co2 saving" icon="carbondioxide"  
      Group item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH label="From grid" icon="energy"
        Text item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore55WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Yearly report" {
      Text item=MyPlentiCore55WithoutBattery_STATISTIC_YIELD_YEAR label="Yield" icon="line"
      Text item=MyPlentiCore55WithoutBattery_STATISTIC_AUTARKY_YEAR label="Autarchy level" icon="garden"
      Text item=MyPlentiCore55WithoutBattery_STATISTIC_CO2SAVING_YEAR label="Co2 saving" icon="carbondioxide"
      Group item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR label="From grid" icon="energy"
        Text item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore55WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Overall report" {
      Text item=MyPlentiCore55WithoutBattery_STATISTIC_YIELD_TOTAL label="Yield" icon="line"
      Text item=MyPlentiCore55WithoutBattery_STATISTIC_AUTARKY_TOTAL label="Autarchy level" icon="garden"
      Text item=MyPlentiCore55WithoutBattery_STATISTIC_CO2SAVING_TOTAL label="Co2 saving" icon="carbondioxide"    
      Group item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL label="From grid" icon="energy"
        Text item=MyPlentiCore55WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore55WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Generated errors" {
      Text item=MyPlentiCore55WithoutBattery_SCB_EVENT_ERROR_COUNT_MC label="MainController error count" icon="error"
      Text item=MyPlentiCore55WithoutBattery_SCB_EVENT_ERROR_COUNT_SFH label="Grid controller error count" icon="error"
      Text item=MyPlentiCore55WithoutBattery_SCB_EVENT_ERROR_COUNT_SCB label="SmartCommunicationBoard error count" icon="error"  
      Text item=MyPlentiCore55WithoutBattery_SCB_EVENT_WARNING_COUNT_SCB label="SmartCommunicationBoard warning count" icon="error"
  }
  Frame label="Permitted feed-in quantity" {
    Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE label="Absolute"
    Text item=MyPlentiCore55WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV label="Relative"
  }
}
```

<u><b>PIKO PLENTICORE plus 7.0 (with battery) example</b></u>

thing configuration:

```
Thing kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="60"]
```

items configuration:

```
Number:Energy                MyPlentiCore70WithBattery_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_DC_POWER"}
Number:Energy                MyPlentiCore70WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY"}
Number:Energy                MyPlentiCore70WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID"}
Number:Energy                MyPlentiCore70WithBattery_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_OWNCONSUMPTION"}
Number:Energy                MyPlentiCore70WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV"}
Number:Energy                MyPlentiCore70WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore70WithBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE"}
Number:Dimensionless         MyPlentiCore70WithBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_LIMIT_EVU_RELATIV"}
Number:Time                  MyPlentiCore70WithBattery_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_WORKTIME"}
Number:ElectricCurrent       MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE"}
Number:Energy                MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_AC_CURRENT_POWER"}
Number:Dimensionless         MyPlentiCore70WithBattery_DEVICE_LOCAL_BATTERY_LOADING_CYCLES           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_BATTERY_LOADING_CYCLES"}
Number:ElectricCharge        MyPlentiCore70WithBattery_DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY"}
Number:ElectricCurrent       MyPlentiCore70WithBattery_DEVICE_LOCAL_BATTERY_AMPERAGE                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_BATTERY_AMPERAGE"}
Number:Energy                MyPlentiCore70WithBattery_DEVICE_LOCAL_BATTERY_POWER                    <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_BATTERY_POWER"}
Number:Dimensionless         MyPlentiCore70WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE"}
Number:ElectricPotential     MyPlentiCore70WithBattery_DEVICE_LOCAL_BATTERY_VOLTAGE                  <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_BATTERY_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore70WithBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_PVSTRING_1_AMPERAGE"}
Number:Energy                MyPlentiCore70WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_PVSTRING_1_POWER"}
Number:ElectricPotential     MyPlentiCore70WithBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_PVSTRING_1_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore70WithBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_PVSTRING_2_AMPERAGE"}
Number:Energy                MyPlentiCore70WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_PVSTRING_2_POWER"}
Number:ElectricPotential     MyPlentiCore70WithBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:DEVICE_LOCAL_PVSTRING_2_VOLTAGE"}
Number:Dimensionless         MyPlentiCore70WithBattery_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:SCB_EVENT_ERROR_COUNT_MC"}
Number:Dimensionless         MyPlentiCore70WithBattery_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:SCB_EVENT_ERROR_COUNT_SFH"}
Number:Dimensionless         MyPlentiCore70WithBattery_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:SCB_EVENT_ERROR_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore70WithBattery_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:SCB_EVENT_WARNING_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore70WithBattery_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_AUTARKY_DAY"}
Number:Dimensionless         MyPlentiCore70WithBattery_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_AUTARKY_MONTH"}
Number:Dimensionless         MyPlentiCore70WithBattery_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_AUTARKY_TOTAL"}
Number:Dimensionless         MyPlentiCore70WithBattery_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_AUTARKY_YEAR"}
Number:Mass                  MyPlentiCore70WithBattery_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_CO2SAVING_DAY"}
Number:Mass                  MyPlentiCore70WithBattery_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_CO2SAVING_MONTH"}
Number:Mass                  MyPlentiCore70WithBattery_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_CO2SAVING_TOTAL"}
Number:Mass                  MyPlentiCore70WithBattery_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_CO2SAVING_YEAR"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_DAY"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_MONTH"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_YEAR"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR  <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_DAY"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR"}
Number:Dimensionless         MyPlentiCore70WithBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_OWNCONSUMPTION_RATE_DAY"}
Number:Dimensionless         MyPlentiCore70WithBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_OWNCONSUMPTION_RATE_MONTH"}
Number:Dimensionless         MyPlentiCore70WithBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_OWNCONSUMPTION_RATE_TOTAL"}
Number:Dimensionless         MyPlentiCore70WithBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_OWNCONSUMPTION_RATE_YEAR"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_YIELD_DAY                           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_YIELD_DAY"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_YIELD_MONTH"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_YIELD_TOTAL"}
Number:Energy                MyPlentiCore70WithBattery_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHBATTERY:MyPlentiCore70WithBattery:STATISTIC_YIELD_YEAR"}
```

sitemap configuration:

```
sitemap PLENTICORE70WITHBATTERY  label="KOSTAL PLENTICORE 7.0 (with battery)" {
  Frame label="Live data" {
    Group item=MyPlentiCore70WithBattery_DEVICE_LOCAL_DC_POWER label="Solar energy" icon="solarplant" {
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_DC_POWER label="Solar energy (total)" icon="solarplant"
      Group item=MyPlentiCore70WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="PV string 1" icon="solarplant"
      {    
        Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="Power"
        Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE label="Voltage"
      }
      Group item=MyPlentiCore70WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="PV string 2" icon="solarplant"
      {
        Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="Power"
        Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE label="Voltage"
      }
    }
    Group item=MyPlentiCore70WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE label="Battery charge" icon="batterylevel" {
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_BATTERY_LOADING_CYCLES label="Loading cycles" icon="battery"
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY label="Full charge capacity" icon="batterylevel"
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_BATTERY_AMPERAGE label="Battery amperage"
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_BATTERY_POWER label="Battery power"
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE label="Battery charge" icon="batterylevel"
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_BATTERY_VOLTAGE label="Battery voltage"
    }
    Group item=MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC" {
    Frame label="Overall" {
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC"
    }
    Frame label="Phase 1" {
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 2" {
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 3" {
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE label="Current voltage"
    }
    }
    Group item=MyPlentiCore70WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Home comsumption" icon="house" {
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Total" icon="poweroutlet"
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_OWNCONSUMPTION label="Owncomsumption" icon="house"
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY label="From battery" icon="battery"
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID label="From grid" icon="energy"
      Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV  label="From photovoltaic" icon="sun_clouds"
    }
  }
  Frame label="Daily report" icon="line"{
    Text item=MyPlentiCore70WithBattery_STATISTIC_YIELD_DAY label="Yield" icon="line"
    Text item=MyPlentiCore70WithBattery_STATISTIC_AUTARKY_DAY label="Autarchy level" icon="garden"
    Text item=MyPlentiCore70WithBattery_STATISTIC_CO2SAVING_DAY label="Co2 saving" icon="carbondioxide"
    Group item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_DAY label="Homeconsumption" icon="pie"
    {
      Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_DAY label="Total consumption" icon="poweroutlet"
      Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY label="From battery" icon="battery"
      Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY label="From grid" icon="energy"
      Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY label="From photovoltaic" icon="sun_clouds"
      Text item=MyPlentiCore70WithBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY label="Ownconsumption rate" icon="price"
    }
  }
  Frame label="Monthly report" {
      Text item=MyPlentiCore70WithBattery_STATISTIC_YIELD_MONTH label="Yield" icon="line"
      Text item=MyPlentiCore70WithBattery_STATISTIC_AUTARKY_MONTH label="Autarchy level" icon="garden"
      Text item=MyPlentiCore70WithBattery_STATISTIC_CO2SAVING_MONTH label="Co2 saving" icon="carbondioxide"  
      Group item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH label="From battery" icon="battery"
        Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH label="From grid" icon="energy"
        Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore70WithBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Yearly report" {
      Text item=MyPlentiCore70WithBattery_STATISTIC_YIELD_YEAR label="Yield" icon="line"
      Text item=MyPlentiCore70WithBattery_STATISTIC_AUTARKY_YEAR label="Autarchy level" icon="garden"
      Text item=MyPlentiCore70WithBattery_STATISTIC_CO2SAVING_YEAR label="Co2 saving" icon="carbondioxide"
      Group item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR label="From battery" icon="battery"
        Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR label="From grid" icon="energy"
        Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore70WithBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Overall report" {
      Text item=MyPlentiCore70WithBattery_STATISTIC_YIELD_TOTAL label="Yield" icon="line"
      Text item=MyPlentiCore70WithBattery_STATISTIC_AUTARKY_TOTAL label="Autarchy level" icon="garden"
      Text item=MyPlentiCore70WithBattery_STATISTIC_CO2SAVING_TOTAL label="Co2 saving" icon="carbondioxide"    
      Group item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL label="From battery" icon="battery"
        Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL label="From grid" icon="energy"
        Text item=MyPlentiCore70WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore70WithBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Generated errors" {
      Text item=MyPlentiCore70WithBattery_SCB_EVENT_ERROR_COUNT_MC label="MainController error count" icon="error"
      Text item=MyPlentiCore70WithBattery_SCB_EVENT_ERROR_COUNT_SFH label="Grid controller error count" icon="error"
      Text item=MyPlentiCore70WithBattery_SCB_EVENT_ERROR_COUNT_SCB label="SmartCommunicationBoard error count" icon="error"  
      Text item=MyPlentiCore70WithBattery_SCB_EVENT_WARNING_COUNT_SCB label="SmartCommunicationBoard warning count" icon="error"
  }
  Frame label="Permitted feed-in quantity" {
    Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE label="Absolute"
    Text item=MyPlentiCore70WithBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV label="Relative"
  }
}
```

<u><b>PIKO PLENTICORE plus 7.0 (without battery) example</b></u>

thing configuration:

```
Thing kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="60"]
```

items configuration:

```
Number:Energy                MyPlentiCore70WithoutBattery_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_DC_POWER"}
Number:Energy                MyPlentiCore70WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID"}
Number:Energy                MyPlentiCore70WithoutBattery_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_OWNCONSUMPTION"}
Number:Energy                MyPlentiCore70WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV"}
Number:Energy                MyPlentiCore70WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore70WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE"}
Number:Dimensionless         MyPlentiCore70WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_LIMIT_EVU_RELATIV"}
Number:Time                  MyPlentiCore70WithoutBattery_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_WORKTIME"}
Number:ElectricCurrent       MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE"}
Number:Energy                MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_AC_CURRENT_POWER"}
Number:ElectricCurrent       MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_PVSTRING_1_AMPERAGE"}
Number:Energy                MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_PVSTRING_1_POWER"}
Number:ElectricPotential     MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_PVSTRING_1_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_PVSTRING_2_AMPERAGE"}
Number:Energy                MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_PVSTRING_2_POWER"}
Number:ElectricPotential     MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_PVSTRING_2_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_3_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_PVSTRING_3_AMPERAGE"}
Number:Energy                MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_3_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_PVSTRING_3_POWER"}
Number:ElectricPotential     MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_3_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:DEVICE_LOCAL_PVSTRING_3_VOLTAGE"}
Number:Dimensionless         MyPlentiCore70WithoutBattery_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:SCB_EVENT_ERROR_COUNT_MC"}
Number:Dimensionless         MyPlentiCore70WithoutBattery_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:SCB_EVENT_ERROR_COUNT_SFH"}
Number:Dimensionless         MyPlentiCore70WithoutBattery_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:SCB_EVENT_ERROR_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore70WithoutBattery_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:SCB_EVENT_WARNING_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore70WithoutBattery_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_AUTARKY_DAY"}
Number:Dimensionless         MyPlentiCore70WithoutBattery_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_AUTARKY_MONTH"}
Number:Dimensionless         MyPlentiCore70WithoutBattery_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_AUTARKY_TOTAL"}
Number:Dimensionless         MyPlentiCore70WithoutBattery_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_AUTARKY_YEAR"}
Number:Mass                  MyPlentiCore70WithoutBattery_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_CO2SAVING_DAY"}
Number:Mass                  MyPlentiCore70WithoutBattery_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_CO2SAVING_MONTH"}
Number:Mass                  MyPlentiCore70WithoutBattery_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_CO2SAVING_TOTAL"}
Number:Mass                  MyPlentiCore70WithoutBattery_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_CO2SAVING_YEAR"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_HOMECONSUMPTION_DAY"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_HOMECONSUMPTION_MONTH"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_HOMECONSUMPTION_YEAR"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_DAY"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR"}
Number:Dimensionless         MyPlentiCore70WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_DAY"}
Number:Dimensionless         MyPlentiCore70WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_MONTH"}
Number:Dimensionless         MyPlentiCore70WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_TOTAL"}
Number:Dimensionless         MyPlentiCore70WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_YEAR"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_YIELD_DAY                           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_YIELD_DAY"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_YIELD_MONTH"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_YIELD_TOTAL"}
Number:Energy                MyPlentiCore70WithoutBattery_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS70WITHOUTBATTERY:MyPlentiCore70WithoutBattery:STATISTIC_YIELD_YEAR"}
```

sitemap configuration:

```
sitemap PLENTICORE70WITHOUTBATTERY  label="KOSTAL PLENTICORE 7.0 (no battery)" {
  Frame label="Live data" {
    Group item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_DC_POWER label="Solar energy" icon="solarplant" {
      Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_DC_POWER label="Solar energy (total)" icon="solarplant"
      Group item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="PV string 1" icon="solarplant"
      {    
        Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="Power"
        Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE label="Voltage"
      }
      Group item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="PV string 2" icon="solarplant"
      {
        Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="Power"
        Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE label="Voltage"
      }
      Group item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_3_POWER label="PV string 3" icon="solarplant"
      {
        Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_3_POWER label="Power"
        Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_3_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_PVSTRING_3_VOLTAGE label="Voltage"
      }
    }
    Group item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC" {
    Frame label="Overall" {
      Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC"
    }
    Frame label="Phase 1" {
      Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 2" {
      Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 3" {
      Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE label="Current voltage"
    }
    }
    Group item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Home comsumption" icon="house" {
      Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Total" icon="poweroutlet"
      Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_OWNCONSUMPTION label="Owncomsumption" icon="house"
      Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID label="From grid" icon="energy"
      Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV  label="From photovoltaic" icon="sun_clouds"
    }
  }
  Frame label="Daily report" icon="line"{
    Text item=MyPlentiCore70WithoutBattery_STATISTIC_YIELD_DAY label="Yield" icon="line"
    Text item=MyPlentiCore70WithoutBattery_STATISTIC_AUTARKY_DAY label="Autarchy level" icon="garden"
    Text item=MyPlentiCore70WithoutBattery_STATISTIC_CO2SAVING_DAY label="Co2 saving" icon="carbondioxide"
    Group item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_DAY label="Homeconsumption" icon="pie"
    {
      Text item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_DAY label="Total consumption" icon="poweroutlet"
      Text item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY label="From grid" icon="energy"
      Text item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY label="From photovoltaic" icon="sun_clouds"
      Text item=MyPlentiCore70WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY label="Ownconsumption rate" icon="price"
    }
  }
  Frame label="Monthly report" {
      Text item=MyPlentiCore70WithoutBattery_STATISTIC_YIELD_MONTH label="Yield" icon="line"
      Text item=MyPlentiCore70WithoutBattery_STATISTIC_AUTARKY_MONTH label="Autarchy level" icon="garden"
      Text item=MyPlentiCore70WithoutBattery_STATISTIC_CO2SAVING_MONTH label="Co2 saving" icon="carbondioxide"  
      Group item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH label="From grid" icon="energy"
        Text item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore70WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Yearly report" {
      Text item=MyPlentiCore70WithoutBattery_STATISTIC_YIELD_YEAR label="Yield" icon="line"
      Text item=MyPlentiCore70WithoutBattery_STATISTIC_AUTARKY_YEAR label="Autarchy level" icon="garden"
      Text item=MyPlentiCore70WithoutBattery_STATISTIC_CO2SAVING_YEAR label="Co2 saving" icon="carbondioxide"
      Group item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR label="From grid" icon="energy"
        Text item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore70WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Overall report" {
      Text item=MyPlentiCore70WithoutBattery_STATISTIC_YIELD_TOTAL label="Yield" icon="line"
      Text item=MyPlentiCore70WithoutBattery_STATISTIC_AUTARKY_TOTAL label="Autarchy level" icon="garden"
      Text item=MyPlentiCore70WithoutBattery_STATISTIC_CO2SAVING_TOTAL label="Co2 saving" icon="carbondioxide"    
      Group item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL label="From grid" icon="energy"
        Text item=MyPlentiCore70WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore70WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Generated errors" {
      Text item=MyPlentiCore70WithoutBattery_SCB_EVENT_ERROR_COUNT_MC label="MainController error count" icon="error"
      Text item=MyPlentiCore70WithoutBattery_SCB_EVENT_ERROR_COUNT_SFH label="Grid controller error count" icon="error"
      Text item=MyPlentiCore70WithoutBattery_SCB_EVENT_ERROR_COUNT_SCB label="SmartCommunicationBoard error count" icon="error"  
      Text item=MyPlentiCore70WithoutBattery_SCB_EVENT_WARNING_COUNT_SCB label="SmartCommunicationBoard warning count" icon="error"
  }
  Frame label="Permitted feed-in quantity" {
    Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE label="Absolute"
    Text item=MyPlentiCore70WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV label="Relative"
  }
}
```

<u><b>PIKO PLENTICORE plus 8.5 (with battery) example</b></u>

thing configuration:

```
Thing kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="60"]
```

items configuration:

```
Number:Energy                MyPlentiCore85WithBattery_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_DC_POWER"}
Number:Energy                MyPlentiCore85WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY"}
Number:Energy                MyPlentiCore85WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID"}
Number:Energy                MyPlentiCore85WithBattery_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_OWNCONSUMPTION"}
Number:Energy                MyPlentiCore85WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV"}
Number:Energy                MyPlentiCore85WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore85WithBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE"}
Number:Dimensionless         MyPlentiCore85WithBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_LIMIT_EVU_RELATIV"}
Number:Time                  MyPlentiCore85WithBattery_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_WORKTIME"}
Number:ElectricCurrent       MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE"}
Number:Energy                MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_AC_CURRENT_POWER"}Number:Dimensionless         MyPlentiCore85WithBattery_DEVICE_LOCAL_BATTERY_LOADING_CYCLES           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_BATTERY_LOADING_CYCLES"}
Number:ElectricCharge        MyPlentiCore85WithBattery_DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY"}
Number:ElectricCurrent       MyPlentiCore85WithBattery_DEVICE_LOCAL_BATTERY_AMPERAGE                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_BATTERY_AMPERAGE"}
Number:Energy                MyPlentiCore85WithBattery_DEVICE_LOCAL_BATTERY_POWER                    <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_BATTERY_POWER"}
Number:Dimensionless         MyPlentiCore85WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE"}
Number:ElectricPotential     MyPlentiCore85WithBattery_DEVICE_LOCAL_BATTERY_VOLTAGE                  <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_BATTERY_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore85WithBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_PVSTRING_1_AMPERAGE"}
Number:Energy                MyPlentiCore85WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_PVSTRING_1_POWER"}
Number:ElectricPotential     MyPlentiCore85WithBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_PVSTRING_1_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore85WithBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_PVSTRING_2_AMPERAGE"}
Number:Energy                MyPlentiCore85WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_PVSTRING_2_POWER"}
Number:ElectricPotential     MyPlentiCore85WithBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:DEVICE_LOCAL_PVSTRING_2_VOLTAGE"}
Number:Dimensionless         MyPlentiCore85WithBattery_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:SCB_EVENT_ERROR_COUNT_MC"}
Number:Dimensionless         MyPlentiCore85WithBattery_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:SCB_EVENT_ERROR_COUNT_SFH"}
Number:Dimensionless         MyPlentiCore85WithBattery_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:SCB_EVENT_ERROR_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore85WithBattery_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:SCB_EVENT_WARNING_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore85WithBattery_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_AUTARKY_DAY"}
Number:Dimensionless         MyPlentiCore85WithBattery_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_AUTARKY_MONTH"}
Number:Dimensionless         MyPlentiCore85WithBattery_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_AUTARKY_TOTAL"}
Number:Dimensionless         MyPlentiCore85WithBattery_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_AUTARKY_YEAR"}
Number:Mass                  MyPlentiCore85WithBattery_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_CO2SAVING_DAY"}
Number:Mass                  MyPlentiCore85WithBattery_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_CO2SAVING_MONTH"}
Number:Mass                  MyPlentiCore85WithBattery_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_CO2SAVING_TOTAL"}
Number:Mass                  MyPlentiCore85WithBattery_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_CO2SAVING_YEAR"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_DAY"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_MONTH"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_YEAR"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR  <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_DAY"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR"}
Number:Dimensionless         MyPlentiCore85WithBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_OWNCONSUMPTION_RATE_DAY"}
Number:Dimensionless         MyPlentiCore85WithBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_OWNCONSUMPTION_RATE_MONTH"}
Number:Dimensionless         MyPlentiCore85WithBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_OWNCONSUMPTION_RATE_TOTAL"}
Number:Dimensionless         MyPlentiCore85WithBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_OWNCONSUMPTION_RATE_YEAR"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_YIELD_DAY                           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_YIELD_DAY"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_YIELD_MONTH"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_YIELD_TOTAL"}
Number:Energy                MyPlentiCore85WithBattery_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHBATTERY:MyPlentiCore85WithBattery:STATISTIC_YIELD_YEAR"}
```

sitemap configuration:

```
sitemap PLENTICORE85WITHBATTERY  label="KOSTAL PLENTICORE 8.5 (with battery)" {
  Frame label="Live data" {
    Group item=MyPlentiCore85WithBattery_DEVICE_LOCAL_DC_POWER label="Solar energy" icon="solarplant" {
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_DC_POWER label="Solar energy (total)" icon="solarplant"
      Group item=MyPlentiCore85WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="PV string 1" icon="solarplant"
      {    
        Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="Power"
        Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE label="Voltage"
      }
      Group item=MyPlentiCore85WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="PV string 2" icon="solarplant"
      {
        Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="Power"
        Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE label="Voltage"
      }
    }
    Group item=MyPlentiCore85WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE label="Battery charge" icon="batterylevel" {
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_BATTERY_LOADING_CYCLES label="Loading cycles" icon="battery"
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY label="Full charge capacity" icon="batterylevel"
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_BATTERY_AMPERAGE label="Battery amperage"
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_BATTERY_POWER label="Battery power"
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE label="Battery charge" icon="batterylevel"
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_BATTERY_VOLTAGE label="Battery voltage"
    }
    Group item=MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC" {
    Frame label="Overall" {
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC"
    }
    Frame label="Phase 1" {
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 2" {
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 3" {
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE label="Current voltage"
    }
    }
    Group item=MyPlentiCore85WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Home comsumption" icon="house" {
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Total" icon="poweroutlet"
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_OWNCONSUMPTION label="Owncomsumption" icon="house"
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY label="From battery" icon="battery"
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID label="From grid" icon="energy"
      Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV  label="From photovoltaic" icon="sun_clouds"    }
  }
  Frame label="Daily report" icon="line"{
    Text item=MyPlentiCore85WithBattery_STATISTIC_YIELD_DAY label="Yield" icon="line"
    Text item=MyPlentiCore85WithBattery_STATISTIC_AUTARKY_DAY label="Autarchy level" icon="garden"
    Text item=MyPlentiCore85WithBattery_STATISTIC_CO2SAVING_DAY label="Co2 saving" icon="carbondioxide"
    Group item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_DAY label="Homeconsumption" icon="pie"
    {
      Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_DAY label="Total consumption" icon="poweroutlet"
      Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY label="From battery" icon="battery"
      Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY label="From grid" icon="energy"
      Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY label="From photovoltaic" icon="sun_clouds"
      Text item=MyPlentiCore85WithBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY label="Ownconsumption rate" icon="price"
    }
  }
  Frame label="Monthly report" {
      Text item=MyPlentiCore85WithBattery_STATISTIC_YIELD_MONTH label="Yield" icon="line"
      Text item=MyPlentiCore85WithBattery_STATISTIC_AUTARKY_MONTH label="Autarchy level" icon="garden"
      Text item=MyPlentiCore85WithBattery_STATISTIC_CO2SAVING_MONTH label="Co2 saving" icon="carbondioxide"  
      Group item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH label="From battery" icon="battery"
        Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH label="From grid" icon="energy"
        Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore85WithBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Yearly report" {
      Text item=MyPlentiCore85WithBattery_STATISTIC_YIELD_YEAR label="Yield" icon="line"
      Text item=MyPlentiCore85WithBattery_STATISTIC_AUTARKY_YEAR label="Autarchy level" icon="garden"
      Text item=MyPlentiCore85WithBattery_STATISTIC_CO2SAVING_YEAR label="Co2 saving" icon="carbondioxide"
      Group item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR label="From battery" icon="battery"
        Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR label="From grid" icon="energy"
        Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore85WithBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Overall report" {
      Text item=MyPlentiCore85WithBattery_STATISTIC_YIELD_TOTAL label="Yield" icon="line"
      Text item=MyPlentiCore85WithBattery_STATISTIC_AUTARKY_TOTAL label="Autarchy level" icon="garden"
      Text item=MyPlentiCore85WithBattery_STATISTIC_CO2SAVING_TOTAL label="Co2 saving" icon="carbondioxide"    
      Group item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL label="From battery" icon="battery"
        Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL label="From grid" icon="energy"
        Text item=MyPlentiCore85WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore85WithBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Generated errors" {
      Text item=MyPlentiCore85WithBattery_SCB_EVENT_ERROR_COUNT_MC label="MainController error count" icon="error"
      Text item=MyPlentiCore85WithBattery_SCB_EVENT_ERROR_COUNT_SFH label="Grid controller error count" icon="error"
      Text item=MyPlentiCore85WithBattery_SCB_EVENT_ERROR_COUNT_SCB label="SmartCommunicationBoard error count" icon="error"  
      Text item=MyPlentiCore85WithBattery_SCB_EVENT_WARNING_COUNT_SCB label="SmartCommunicationBoard warning count" icon="error"
  }
  Frame label="Permitted feed-in quantity" {
    Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE label="Absolute"
    Text item=MyPlentiCore85WithBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV label="Relative"
  }
}
```

<u><b>PIKO PLENTICORE plus 8.5 (without battery) example</b></u>

thing configuration:

```
Thing kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="60"]
```

items configuration:

```
Number:Energy                MyPlentiCore85WithoutBattery_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_DC_POWER"}
Number:Energy                MyPlentiCore85WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID"}
Number:Energy                MyPlentiCore85WithoutBattery_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_OWNCONSUMPTION"}
Number:Energy                MyPlentiCore85WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV"}
Number:Energy                MyPlentiCore85WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore85WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE"}
Number:Dimensionless         MyPlentiCore85WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_LIMIT_EVU_RELATIV"}
Number:Time                  MyPlentiCore85WithoutBattery_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_WORKTIME"}
Number:ElectricCurrent       MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE"}
Number:Energy                MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_AC_CURRENT_POWER"}
Number:ElectricCurrent       MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_PVSTRING_1_AMPERAGE"}
Number:Energy                MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_PVSTRING_1_POWER"}
Number:ElectricPotential     MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_PVSTRING_1_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_PVSTRING_2_AMPERAGE"}
Number:Energy                MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_PVSTRING_2_POWER"}
Number:ElectricPotential     MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_PVSTRING_2_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_3_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_PVSTRING_3_AMPERAGE"}
Number:Energy                MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_3_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_PVSTRING_3_POWER"}
Number:ElectricPotential     MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_3_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:DEVICE_LOCAL_PVSTRING_3_VOLTAGE"}
Number:Dimensionless         MyPlentiCore85WithoutBattery_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:SCB_EVENT_ERROR_COUNT_MC"}
Number:Dimensionless         MyPlentiCore85WithoutBattery_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:SCB_EVENT_ERROR_COUNT_SFH"}
Number:Dimensionless         MyPlentiCore85WithoutBattery_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:SCB_EVENT_ERROR_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore85WithoutBattery_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:SCB_EVENT_WARNING_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore85WithoutBattery_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_AUTARKY_DAY"}
Number:Dimensionless         MyPlentiCore85WithoutBattery_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_AUTARKY_MONTH"}
Number:Dimensionless         MyPlentiCore85WithoutBattery_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_AUTARKY_TOTAL"}
Number:Dimensionless         MyPlentiCore85WithoutBattery_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_AUTARKY_YEAR"}
Number:Mass                  MyPlentiCore85WithoutBattery_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_CO2SAVING_DAY"}
Number:Mass                  MyPlentiCore85WithoutBattery_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_CO2SAVING_MONTH"}
Number:Mass                  MyPlentiCore85WithoutBattery_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_CO2SAVING_TOTAL"}
Number:Mass                  MyPlentiCore85WithoutBattery_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_CO2SAVING_YEAR"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_HOMECONSUMPTION_DAY"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_HOMECONSUMPTION_MONTH"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_HOMECONSUMPTION_YEAR"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_DAY"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR"}
Number:Dimensionless         MyPlentiCore85WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_DAY"}
Number:Dimensionless         MyPlentiCore85WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_MONTH"}
Number:Dimensionless         MyPlentiCore85WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_TOTAL"}
Number:Dimensionless         MyPlentiCore85WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_YEAR"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_YIELD_DAY                           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_YIELD_DAY"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_YIELD_MONTH"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_YIELD_TOTAL"}
Number:Energy                MyPlentiCore85WithoutBattery_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS85WITHOUTBATTERY:MyPlentiCore85WithoutBattery:STATISTIC_YIELD_YEAR"}
```

sitemap configuration:

```
sitemap PLENTICORE85WITHOUTBATTERY  label="KOSTAL PLENTICORE 8.5 (no battery)" {
  Frame label="Live data" {
    Group item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_DC_POWER label="Solar energy" icon="solarplant" {
      Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_DC_POWER label="Solar energy (total)" icon="solarplant"
      Group item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="PV string 1" icon="solarplant"
      {    
        Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="Power"
        Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE label="Voltage"
      }
      Group item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="PV string 2" icon="solarplant"
      {
        Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="Power"
        Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE label="Voltage"
      }
      Group item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_3_POWER label="PV string 3" icon="solarplant"
      {
        Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_3_POWER label="Power"
        Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_3_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_PVSTRING_3_VOLTAGE label="Voltage"
      }
    }
    Group item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC" {
    Frame label="Overall" {
      Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC"
    }
    Frame label="Phase 1" {
      Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 2" {
      Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 3" {
      Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE label="Current voltage"
    }
    }
    Group item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Home comsumption" icon="house" {
      Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Total" icon="poweroutlet"
      Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_OWNCONSUMPTION label="Owncomsumption" icon="house"
      Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID label="From grid" icon="energy"
      Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV  label="From photovoltaic" icon="sun_clouds"
    }
  }
  Frame label="Daily report" icon="line"{
    Text item=MyPlentiCore85WithoutBattery_STATISTIC_YIELD_DAY label="Yield" icon="line"
    Text item=MyPlentiCore85WithoutBattery_STATISTIC_AUTARKY_DAY label="Autarchy level" icon="garden"
    Text item=MyPlentiCore85WithoutBattery_STATISTIC_CO2SAVING_DAY label="Co2 saving" icon="carbondioxide"
    Group item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_DAY label="Homeconsumption" icon="pie"
    {
      Text item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_DAY label="Total consumption" icon="poweroutlet"
      Text item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY label="From grid" icon="energy"
      Text item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY label="From photovoltaic" icon="sun_clouds"
      Text item=MyPlentiCore85WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY label="Ownconsumption rate" icon="price"
    }
  }
  Frame label="Monthly report" {
      Text item=MyPlentiCore85WithoutBattery_STATISTIC_YIELD_MONTH label="Yield" icon="line"
      Text item=MyPlentiCore85WithoutBattery_STATISTIC_AUTARKY_MONTH label="Autarchy level" icon="garden"
      Text item=MyPlentiCore85WithoutBattery_STATISTIC_CO2SAVING_MONTH label="Co2 saving" icon="carbondioxide"  
      Group item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH label="From grid" icon="energy"
        Text item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore85WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Yearly report" {
      Text item=MyPlentiCore85WithoutBattery_STATISTIC_YIELD_YEAR label="Yield" icon="line"
      Text item=MyPlentiCore85WithoutBattery_STATISTIC_AUTARKY_YEAR label="Autarchy level" icon="garden"
      Text item=MyPlentiCore85WithoutBattery_STATISTIC_CO2SAVING_YEAR label="Co2 saving" icon="carbondioxide"
      Group item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR label="From grid" icon="energy"
        Text item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore85WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Overall report" {
      Text item=MyPlentiCore85WithoutBattery_STATISTIC_YIELD_TOTAL label="Yield" icon="line"
      Text item=MyPlentiCore85WithoutBattery_STATISTIC_AUTARKY_TOTAL label="Autarchy level" icon="garden"
      Text item=MyPlentiCore85WithoutBattery_STATISTIC_CO2SAVING_TOTAL label="Co2 saving" icon="carbondioxide"    
      Group item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL label="From grid" icon="energy"
        Text item=MyPlentiCore85WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore85WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Generated errors" {
      Text item=MyPlentiCore85WithoutBattery_SCB_EVENT_ERROR_COUNT_MC label="MainController error count" icon="error"
      Text item=MyPlentiCore85WithoutBattery_SCB_EVENT_ERROR_COUNT_SFH label="Grid controller error count" icon="error"
      Text item=MyPlentiCore85WithoutBattery_SCB_EVENT_ERROR_COUNT_SCB label="SmartCommunicationBoard error count" icon="error"  
      Text item=MyPlentiCore85WithoutBattery_SCB_EVENT_WARNING_COUNT_SCB label="SmartCommunicationBoard warning count" icon="error"
  }
  Frame label="Permitted feed-in quantity" {
    Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE label="Absolute"
    Text item=MyPlentiCore85WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV label="Relative"
  }
}
```

<u><b>PIKO PLENTICORE plus 10.0 (with battery) example</b></u>

thing configuration:

```
Thing kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="60"]
```

items configuration:

```
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_DC_POWER"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_OWNCONSUMPTION"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE"}
Number:Dimensionless         MyPlentiCore100WithBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_LIMIT_EVU_RELATIV"}
Number:Time                  MyPlentiCore100WithBattery_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_WORKTIME"}
Number:ElectricCurrent       MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_AC_CURRENT_POWER"}
Number:Dimensionless         MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_LOADING_CYCLES           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_BATTERY_LOADING_CYCLES"}
Number:ElectricCharge        MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY"}
Number:ElectricCurrent       MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_AMPERAGE                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_BATTERY_AMPERAGE"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_POWER                    <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_BATTERY_POWER"}
Number:Dimensionless         MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE"}
Number:ElectricPotential     MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_VOLTAGE                  <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_BATTERY_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_PVSTRING_1_AMPERAGE"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_PVSTRING_1_POWER"}
Number:ElectricPotential     MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_PVSTRING_1_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_PVSTRING_2_AMPERAGE"}
Number:Energy                MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_PVSTRING_2_POWER"}
Number:ElectricPotential     MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:DEVICE_LOCAL_PVSTRING_2_VOLTAGE"}
Number:Dimensionless         MyPlentiCore100WithBattery_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:SCB_EVENT_ERROR_COUNT_MC"}
Number:Dimensionless         MyPlentiCore100WithBattery_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:SCB_EVENT_ERROR_COUNT_SFH"}
Number:Dimensionless         MyPlentiCore100WithBattery_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:SCB_EVENT_ERROR_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore100WithBattery_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:SCB_EVENT_WARNING_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_AUTARKY_DAY"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_AUTARKY_MONTH"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_AUTARKY_TOTAL"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_AUTARKY_YEAR"}
Number:Mass                  MyPlentiCore100WithBattery_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_CO2SAVING_DAY"}
Number:Mass                  MyPlentiCore100WithBattery_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_CO2SAVING_MONTH"}
Number:Mass                  MyPlentiCore100WithBattery_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_CO2SAVING_TOTAL"}
Number:Mass                  MyPlentiCore100WithBattery_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_CO2SAVING_YEAR"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_DAY"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_MONTH"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_YEAR"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR  <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_DAY"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_OWNCONSUMPTION_RATE_DAY"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_OWNCONSUMPTION_RATE_MONTH"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_OWNCONSUMPTION_RATE_TOTAL"}
Number:Dimensionless         MyPlentiCore100WithBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_OWNCONSUMPTION_RATE_YEAR"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_YIELD_DAY                           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_YIELD_DAY"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_YIELD_MONTH"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_YIELD_TOTAL"}
Number:Energy                MyPlentiCore100WithBattery_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHBATTERY:MyPlentiCore100WithBattery:STATISTIC_YIELD_YEAR"}
```

sitemap configuration:

```
sitemap PLENTICORE100WITHBATTERY  label="KOSTAL PLENTICORE 10.0 (with battery)" {
  Frame label="Live data" {
    Group item=MyPlentiCore100WithBattery_DEVICE_LOCAL_DC_POWER label="Solar energy" icon="solarplant" {
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_DC_POWER label="Solar energy (total)" icon="solarplant"
      Group item=MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="PV string 1" icon="solarplant"
      {    
        Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="Power"
        Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE label="Voltage"
      }
      Group item=MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="PV string 2" icon="solarplant"
      {
        Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="Power"
        Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE label="Voltage"
      }
    }
    Group item=MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE label="Battery charge" icon="batterylevel" {
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_LOADING_CYCLES label="Loading cycles" icon="battery"
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY label="Full charge capacity" icon="batterylevel"
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_AMPERAGE label="Battery amperage"
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_POWER label="Battery power"
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE label="Battery charge" icon="batterylevel"
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_BATTERY_VOLTAGE label="Battery voltage"
    }
    Group item=MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC" {
    Frame label="Overall" {
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC"
    }
    Frame label="Phase 1" {
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 2" {
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 3" {
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE label="Current voltage"
    }
    }
    Group item=MyPlentiCore100WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Home comsumption" icon="house" {
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Total" icon="poweroutlet"
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_OWNCONSUMPTION label="Owncomsumption" icon="house"
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY label="From battery" icon="battery"
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID label="From grid" icon="energy"
      Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV  label="From photovoltaic" icon="sun_clouds"
    }
  }
  Frame label="Daily report" icon="line"{
    Text item=MyPlentiCore100WithBattery_STATISTIC_YIELD_DAY label="Yield" icon="line"
    Text item=MyPlentiCore100WithBattery_STATISTIC_AUTARKY_DAY label="Autarchy level" icon="garden"
    Text item=MyPlentiCore100WithBattery_STATISTIC_CO2SAVING_DAY label="Co2 saving" icon="carbondioxide"
    Group item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_DAY label="Homeconsumption" icon="pie"
    {
      Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_DAY label="Total consumption" icon="poweroutlet"
      Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY label="From battery" icon="battery"
      Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY label="From grid" icon="energy"
      Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY label="From photovoltaic" icon="sun_clouds"
      Text item=MyPlentiCore100WithBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY label="Ownconsumption rate" icon="price"
    }
  }
  Frame label="Monthly report" {
      Text item=MyPlentiCore100WithBattery_STATISTIC_YIELD_MONTH label="Yield" icon="line"
      Text item=MyPlentiCore100WithBattery_STATISTIC_AUTARKY_MONTH label="Autarchy level" icon="garden"
      Text item=MyPlentiCore100WithBattery_STATISTIC_CO2SAVING_MONTH label="Co2 saving" icon="carbondioxide"  
      Group item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH label="From battery" icon="battery"
        Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH label="From grid" icon="energy"
        Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore100WithBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Yearly report" {
      Text item=MyPlentiCore100WithBattery_STATISTIC_YIELD_YEAR label="Yield" icon="line"
      Text item=MyPlentiCore100WithBattery_STATISTIC_AUTARKY_YEAR label="Autarchy level" icon="garden"
      Text item=MyPlentiCore100WithBattery_STATISTIC_CO2SAVING_YEAR label="Co2 saving" icon="carbondioxide"
      Group item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR label="From battery" icon="battery"
        Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR label="From grid" icon="energy"
        Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore100WithBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Overall report" {
      Text item=MyPlentiCore100WithBattery_STATISTIC_YIELD_TOTAL label="Yield" icon="line"
      Text item=MyPlentiCore100WithBattery_STATISTIC_AUTARKY_TOTAL label="Autarchy level" icon="garden"
      Text item=MyPlentiCore100WithBattery_STATISTIC_CO2SAVING_TOTAL label="Co2 saving" icon="carbondioxide"    
      Group item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL label="From battery" icon="battery"
        Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL label="From grid" icon="energy"
        Text item=MyPlentiCore100WithBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore100WithBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Generated errors" {
      Text item=MyPlentiCore100WithBattery_SCB_EVENT_ERROR_COUNT_MC label="MainController error count" icon="error"
      Text item=MyPlentiCore100WithBattery_SCB_EVENT_ERROR_COUNT_SFH label="Grid controller error count" icon="error"
      Text item=MyPlentiCore100WithBattery_SCB_EVENT_ERROR_COUNT_SCB label="SmartCommunicationBoard error count" icon="error"  
      Text item=MyPlentiCore100WithBattery_SCB_EVENT_WARNING_COUNT_SCB label="SmartCommunicationBoard warning count" icon="error"
  }
  Frame label="Permitted feed-in quantity" {
    Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE label="Absolute"
    Text item=MyPlentiCore100WithBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV label="Relative"
  }
}
```

<u><b>PIKO PLENTICORE plus 10.0 (without battery) example</b></u>

thing configuration:

```
Thing kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery [ url = "192.168.1.2", userPassword="myPassword", refreshInternalInSeconds="60"]
```

items configuration:

```
Number:Energy                MyPlentiCore100WithoutBattery_DEVICE_LOCAL_DC_POWER                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_DC_POWER"}
Number:Energy                MyPlentiCore100WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID"}
Number:Energy                MyPlentiCore100WithoutBattery_DEVICE_LOCAL_OWNCONSUMPTION                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_OWNCONSUMPTION"}
Number:Energy                MyPlentiCore100WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV"}
Number:Energy                MyPlentiCore100WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore100WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE"}
Number:Dimensionless         MyPlentiCore100WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_LIMIT_EVU_RELATIV"}
Number:Time                  MyPlentiCore100WithoutBattery_DEVICE_LOCAL_WORKTIME                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_WORKTIME"}
Number:ElectricCurrent       MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE"}
Number:Energy                MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER"}
Number:ElectricPotential     MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE"}
Number:Energy                MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_CURRENT_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_AC_CURRENT_POWER"}
Number:ElectricCurrent       MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_PVSTRING_1_AMPERAGE"}
Number:Energy                MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_1_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_PVSTRING_1_POWER"}
Number:ElectricPotential     MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_PVSTRING_1_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_PVSTRING_2_AMPERAGE"}
Number:Energy                MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_2_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_PVSTRING_2_POWER"}
Number:ElectricPotential     MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_PVSTRING_2_VOLTAGE"}
Number:ElectricCurrent       MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_3_AMPERAGE              <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_PVSTRING_3_AMPERAGE"}
Number:Energy                MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_3_POWER                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_PVSTRING_3_POWER"}
Number:ElectricPotential     MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_3_VOLTAGE               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:DEVICE_LOCAL_PVSTRING_3_VOLTAGE"}
Number:Dimensionless         MyPlentiCore100WithoutBattery_SCB_EVENT_ERROR_COUNT_MC                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:SCB_EVENT_ERROR_COUNT_MC"}
Number:Dimensionless         MyPlentiCore100WithoutBattery_SCB_EVENT_ERROR_COUNT_SFH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:SCB_EVENT_ERROR_COUNT_SFH"}
Number:Dimensionless         MyPlentiCore100WithoutBattery_SCB_EVENT_ERROR_COUNT_SCB                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:SCB_EVENT_ERROR_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore100WithoutBattery_SCB_EVENT_WARNING_COUNT_SCB                   <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:SCB_EVENT_WARNING_COUNT_SCB"}
Number:Dimensionless         MyPlentiCore100WithoutBattery_STATISTIC_AUTARKY_DAY                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_AUTARKY_DAY"}
Number:Dimensionless         MyPlentiCore100WithoutBattery_STATISTIC_AUTARKY_MONTH                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_AUTARKY_MONTH"}
Number:Dimensionless         MyPlentiCore100WithoutBattery_STATISTIC_AUTARKY_TOTAL                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_AUTARKY_TOTAL"}
Number:Dimensionless         MyPlentiCore100WithoutBattery_STATISTIC_AUTARKY_YEAR                        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_AUTARKY_YEAR"}
Number:Mass                  MyPlentiCore100WithoutBattery_STATISTIC_CO2SAVING_DAY                       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_CO2SAVING_DAY"}
Number:Mass                  MyPlentiCore100WithoutBattery_STATISTIC_CO2SAVING_MONTH                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_CO2SAVING_MONTH"}
Number:Mass                  MyPlentiCore100WithoutBattery_STATISTIC_CO2SAVING_TOTAL                     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_CO2SAVING_TOTAL"}
Number:Mass                  MyPlentiCore100WithoutBattery_STATISTIC_CO2SAVING_YEAR                      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_CO2SAVING_YEAR"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_DAY                 <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_HOMECONSUMPTION_DAY"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_MONTH               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_HOMECONSUMPTION_MONTH"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_TOTAL               <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_HOMECONSUMPTION_TOTAL"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_YEAR                <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_HOMECONSUMPTION_YEAR"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL     <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR      <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_DAY"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL       <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR        <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR"}
Number:Dimensionless         MyPlentiCore100WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY             <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_DAY"}
Number:Dimensionless         MyPlentiCore100WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_MONTH"}
Number:Dimensionless         MyPlentiCore100WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_TOTAL"}
Number:Dimensionless         MyPlentiCore100WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR            <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_OWNCONSUMPTION_RATE_YEAR"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_YIELD_DAY                           <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_YIELD_DAY"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_YIELD_MONTH                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_YIELD_MONTH"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_YIELD_TOTAL                         <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_YIELD_TOTAL"}
Number:Energy                MyPlentiCore100WithoutBattery_STATISTIC_YIELD_YEAR                          <energy> { channel="kostalpikoiqplenticore:PLENTICOREPLUS100WITHOUTBATTERY:MyPlentiCore100WithoutBattery:STATISTIC_YIELD_YEAR"}
```

sitemap configuration:

```
sitemap PLENTICORE100WITHOUTBATTERY  label="KOSTAL PLENTICORE 10.0 (no battery)" {
  Frame label="Live data" {
    Group item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_DC_POWER label="Solar energy" icon="solarplant" {
      Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_DC_POWER label="Solar energy (total)" icon="solarplant"
      Group item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="PV string 1" icon="solarplant"
      {    
        Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_1_POWER label="Power"
        Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_1_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_1_VOLTAGE label="Voltage"
      }
      Group item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="PV string 2" icon="solarplant"
      {
        Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_2_POWER label="Power"
        Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_2_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_2_VOLTAGE label="Voltage"
      }
      Group item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_3_POWER label="PV string 3" icon="solarplant"
      {
        Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_3_POWER label="Power"
        Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_3_AMPERAGE label="Amperage"  
        Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_PVSTRING_3_VOLTAGE label="Voltage"
      }
    }
    Group item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC" {
    Frame label="Overall" {
      Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_CURRENT_POWER label="Current AC"
    }
    Frame label="Phase 1" {
      Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 2" {
      Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE label="Current voltage"
    }
    Frame label="Phase 3" {
      Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER label="Current power"
      Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE label="Current amperage"
      Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE label="Current voltage"
    }
    }
    Group item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Home comsumption" icon="house" {
      Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL label="Total" icon="poweroutlet"
      Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_OWNCONSUMPTION label="Owncomsumption" icon="house"
      Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID label="From grid" icon="energy"
      Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV  label="From photovoltaic" icon="sun_clouds"
    }
  }
  Frame label="Daily report" icon="line"{
    Text item=MyPlentiCore100WithoutBattery_STATISTIC_YIELD_DAY label="Yield" icon="line"
    Text item=MyPlentiCore100WithoutBattery_STATISTIC_AUTARKY_DAY label="Autarchy level" icon="garden"
    Text item=MyPlentiCore100WithoutBattery_STATISTIC_CO2SAVING_DAY label="Co2 saving" icon="carbondioxide"
    Group item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_DAY label="Homeconsumption" icon="pie"
    {
      Text item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_DAY label="Total consumption" icon="poweroutlet"
      Text item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY label="From grid" icon="energy"
      Text item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY label="From photovoltaic" icon="sun_clouds"
      Text item=MyPlentiCore100WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_DAY label="Ownconsumption rate" icon="price"
    }
  }
  Frame label="Monthly report" {
      Text item=MyPlentiCore100WithoutBattery_STATISTIC_YIELD_MONTH label="Yield" icon="line"
      Text item=MyPlentiCore100WithoutBattery_STATISTIC_AUTARKY_MONTH label="Autarchy level" icon="garden"
      Text item=MyPlentiCore100WithoutBattery_STATISTIC_CO2SAVING_MONTH label="Co2 saving" icon="carbondioxide"  
      Group item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_MONTH label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH label="From grid" icon="energy"
        Text item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore100WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_MONTH label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Yearly report" {
      Text item=MyPlentiCore100WithoutBattery_STATISTIC_YIELD_YEAR label="Yield" icon="line"
      Text item=MyPlentiCore100WithoutBattery_STATISTIC_AUTARKY_YEAR label="Autarchy level" icon="garden"
      Text item=MyPlentiCore100WithoutBattery_STATISTIC_CO2SAVING_YEAR label="Co2 saving" icon="carbondioxide"
      Group item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_YEAR label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR label="From grid" icon="energy"
        Text item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore100WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_YEAR label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Overall report" {
      Text item=MyPlentiCore100WithoutBattery_STATISTIC_YIELD_TOTAL label="Yield" icon="line"
      Text item=MyPlentiCore100WithoutBattery_STATISTIC_AUTARKY_TOTAL label="Autarchy level" icon="garden"
      Text item=MyPlentiCore100WithoutBattery_STATISTIC_CO2SAVING_TOTAL label="Co2 saving" icon="carbondioxide"    
      Group item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Homeconsumption" icon="pie"
      {
        Text item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_TOTAL label="Total consumption" icon="poweroutlet"
        Text item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL label="From grid" icon="energy"
        Text item=MyPlentiCore100WithoutBattery_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL label="From photovoltaic" icon="sun_clouds"
        Text item=MyPlentiCore100WithoutBattery_STATISTIC_OWNCONSUMPTION_RATE_TOTAL label="Ownconsumption rate" icon="price"
      }
  }
  Frame label="Generated errors" {
      Text item=MyPlentiCore100WithoutBattery_SCB_EVENT_ERROR_COUNT_MC label="MainController error count" icon="error"
      Text item=MyPlentiCore100WithoutBattery_SCB_EVENT_ERROR_COUNT_SFH label="Grid controller error count" icon="error"
      Text item=MyPlentiCore100WithoutBattery_SCB_EVENT_ERROR_COUNT_SCB label="SmartCommunicationBoard error count" icon="error"  
      Text item=MyPlentiCore100WithoutBattery_SCB_EVENT_WARNING_COUNT_SCB label="SmartCommunicationBoard warning count" icon="error"
  }
  Frame label="Permitted feed-in quantity" {
    Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE label="Absolute"
    Text item=MyPlentiCore100WithoutBattery_DEVICE_LOCAL_LIMIT_EVU_RELATIV label="Relative"
  }
}
```
