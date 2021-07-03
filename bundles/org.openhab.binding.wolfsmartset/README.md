# Wolf Smartset Binding

This binding communicates with the www.wolf-smartset.de API and provides values readonly. 
Wolf systems are connected with official gateways (Wolf Link Home or Wolf Link Pro) https://www.wolf.eu/produkte/smarthome/ 

## Configuration via the UI

1. Manually add a thing
    ![Manually add a thing](doc/images/step1.png "step 1 - Manually add a thing")
2. Select the WolfSmartset Binding
    ![Select the WolfSmartset Binding](doc/images/step2.png "step 2 - Select the WolfSmartset Binding")
3. Select the WolfSmartset account
    ![Select the WolfSmartset account](doc/images/step3.png "step 3 - Select the WolfSmartset account")
4. Enter account credentials and create the system thing
    ![Enter account credentials](doc/images/step4.png "step 4 - Enter account credentials")
5. Wait until the the systems show up in the inbox. Select system from inbox and create thing.
    ![Select system from inbox](doc/images/inbox-system.png "step 5 - Select system from inbox")
5. Wait until the the units show up in the inbox. Select unit from inbox and create thing
    ![Select unit from inbox](doc/images/inbox-units.png "step 5 - Select unit from inbox")
6. Add points or equipment to your model
    ![Add points or equipment to your model](doc/images/channels-unit.png "step 6 - Add points or equipment to your model")

## Supported Things

- Account (bridge)
- System (bridge)
- Unit

## Thing configuration

### Account

| Parameter       | Type    | Defaut | Description                                                         |
|-----------------|---------|----------|---------------------------------------------------------------------|
| username | text | | username to authenticate to www.wolf-smartset.de |
| password | text  | | password to authenticate to www.wolf-smartset.de |
| refreshIntervalConfiguration | integer | 10 | Specifies the refresh interval to refresh the configuration in minutes |
| refreshIntervalValues | integer | 15 | Specifies time in seconds to refresh values |
| discoveryEnabled | boolean | true | disable the Thing discovery |

### System

| Parameter       | Type    | Defaut | Description                                                         |
|-----------------|---------|----------|---------------------------------------------------------------------|
| systemId | integer | | System ID assigned to the system by WolfSmartset webiste |

### Unit

| Parameter       | Type    | Defaut | Description                                                         |
|-----------------|---------|----------|---------------------------------------------------------------------|
| unitId | integer | | The BundleId assigned to the unit by WolfSmartset webiste |

## Tested WOLF-Devices

| WOLF Equipment    | openhab Version | Used gateway  |
|-------------------|-----------------|---------------|
| CSZ (CGB and SM1) | 3.1             | WOLF Link Pro |
| CGB-2             | 3.1             | WOLF Link home|

Note: Please update this table if you did a successfull test

## Supported Heating-Devices

All devices able to be connected to www.wolf-smartset.de

### Regarding documentation from WOLF

https://www.wolf.eu/fileadmin/Wolf_Daten/Dokumente/FAQ/3065655_201711.pdf

| Heating system                            | WOLF Link home        | WOLF Link pro      |
|-------------------------------------------|-----------------------|--------------------|
| Gas condensing boiler CGB-2, CGW-2, CGS-2 | ✅ | ✅ |
| Oil condensing boiler TOB | ✅ | ✅ |
| MGK-2 gas condensing boiler | ✅ | ✅ |
| split air/water heat pump BWL-1S | ✅ | ✅ |
| Oil condensing boiler COB |  | ✅ |
| gas condensing boiler MGK |   | ✅ |
| Gas condensing boilers CGB, CGW, CGS, FGB |   | ✅ |
| Gas condensing boilers CGG-2, CGU-2 |   | ✅ |
| Boiler controls R2, R3, R21 |   | ✅ |
| Monobloc heat pumps BWW-1, BWL-1, BWS-1 |   | ✅ |
| mixer module MM, MM-2 | ⬜ | ✅ |
| cascade module KM, KM-2 | ⬜ | ✅ |
| solar modules SM1, SM1-2, SM-2, SM2-2 | ⬜ | ✅ |
| Comfort apartment ventilation CWL Excellent | ⬜ | ✅ |
| Air handling units KG Top, CKL Pool``*`` |   | ✅ |
| Air handling units CKL, CFL, CRL``*`` |   | ✅ |
| Combined heat and power units | | ✅ |


Note: 

⬜ possible in connection with a WOLF Link home compatible heater,
full functionality only for devices with current software version.

``*`` Modbus interface required in the device,
Special programming cannot be mapped.

