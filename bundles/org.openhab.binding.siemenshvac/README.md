# SiemensHVAC Binding

This binding provides support for the Siemens HVAC controller ecosystem, and the Web Gateway interface OZW672.
A typical system is composed of:
         
![Diagram](doc/Diagram.png)                 
 
There's a lot of different HVAC controllers depending on model in lot of different PAC constructors.
Siemens RVS41.813/327 inside a Atlantic Hybrid Duo was used for the development, and is fully supported and tested.

Siemens have a complete set of controller references under the name "Siemens Albatros".
Here is a picture of such device.
You can also find this device in other types of heating systems: boiler or solar based.

![](doc/Albatros.jpg)

You will find some information about the OZW672.01 gateway on the Siemens web site: 

[OZW 672 Page](https://hit.sbt.siemens.com/RWD/app.aspx?rc=FR&lang=fr&module=Catalog&action=ShowProduct&key=BPZ:OZW672.01)

With this binding, you will be able to:

- Consult the different parameters of your system, like temperature, current heating mode, water temperature, and many more.
- Modify the functioning mode of your device: temperature set point, heating mode, and others.

## Supported Things

Support many different things as the thing type is handle by autodiscovery.

Mainly, it will first discover the gateway.
Currently test and support is the OZW672.x series.
No test done with OZW772.x series, but it should work as well.

After, it will discover thing inside your PAC, mainly main controller of type RVS...
Only test in real condition with RVS41.813/327 but should work with all other type as the access interface is standard.

## Discovery

Discovery of Gateway can be done using UPnP.
Just switch off/on your gateway to make it annonce itself on the network.
The gateway should appear in the Inbox a few minutes after.
Be aware what you will have to modify the password in Gateway parameters just after the discovery to make it work properly.
Be also aware that first initialization is a little long because the binding needs to read all the metadata from the device.

Discovery of HVAC device have to be done through the Scan button inside the binding.
Go to the Thing page, click on the "+" button, select the SiemensHVAC binding, and then click Scan.
Your device should appear on the page after a few seconds.

## Bridge Configuration

Parameter       | Required       | Default        | Description
----------------|----------------|----------------|------------------
baseUrl         | yes            |                | The address of the OZW672 devices
userName        | yes            | Administrator  | The user name to log into the OZW672
userPass        | yes            |                | The user password to log into the OZW672

## Thing Configuration

## Channels

Channels are auto-discovered, you will find them on the RVS things.
They are organized the same way as the LCD screen of your PAC device, by top level menu functionality, and sub-functionalities.
Each channel is strongly typed, so for example, for heating mode, openHAB will provide you with a list of choices supported by the device.

Channel                | Description                                              | Type          | Unit     | Security Access Level   |  ReadOnly | Advanced
-----------------------|----------------------------------------------------------|---------------|----------|-------------------------|-----------|-------------------
controlBoilerApproval  | Set Boiler Approval (`AUTO`, `OFF`, `ON`)                | String        |          |                         |  R/W      | true
controlProgram         | Set Program (`OFF`, `NORMAL`, `WARMWATER`, `MANUAL`)     | String        |          |                         |  R/W      | true

Channel Type ID        | Item Type                                | Description
-----------------------|------------------------------------------|----------------------------------------------
Numeric                | Number                                   | Handle basic numeric value
String                 | String                                   | a String
TimeOfDay              | TimeOfDay                                |
Datetime               | Datetime                                 |
RadioButton            | RadioButton                              |
Scheduler              | Scheduler                                |
Temperature            | Number                                   | Use to handle reading of  temperatur
Setpoint               | Number                                   | Handle the setting of a temperature
Regime                 | Number                                   | Enumeration for handling mode change
   
## Full Example

Things file `.things`

```java
Bridge siemenshvac:ozw672:local "Ozw672" [ baseUrl="https://192.168.254.42/", userName="Administrator", userPassword="mypass"  ] 
{
    Thing RVS41_813_327 local "RVS41.813/327"  [  ]
    {
        Channels:
            Type Setpoint:temperature                 "Temperature"  [ id="1726" ]
            Type Regime:cc1                           "CC1"          [ id="1725" ]
    
    }
```

Items file `.items`

```java
String              Boiler_State_Pump_HWS       "HWS Pump State [%s]"                   { channel = "siemenshvac:RVS41_813_327:local:local:2237#2259_PpeChargeECS"              }       
Number              Boiler_State_HWS            "HWS State [%s]"                        { channel = "siemenshvac:RVS41_813_327:local:local:2032#2035_Etat_ECS"                  }
Number:Temperature  Flow_Temperature_Real       "Flow Temparature Real [%.1f °C]"       { channel = "siemenshvac:RVS41_813_327:local:local:2237#2248_ValReelleTempDep_CC1"      }   
Number:Temperature  Flow_Temperature_Setpoint   "Flow Temperature Setpoint [%.1f °C]"   { channel = "siemenshvac:RVS41_813_327:local:local:2237#2249_ConsTDepResultCC1"         }   
Number              Hour_fct_HWS                "HWS Hour function"                     { channel = "siemenshvac:RVS41_813_327:local:local:2237#2263_HeuresFoncPompeECS"        }   
Number              Nb_Start_HWS                "HWS Number of start [%.1f]"            { channel = "siemenshvac:RVS41_813_327:local:local:2237#2266_ComptDemarResEl_ECS"       }
Number:Temperature  Thermostat_Temperature      "Thermostat tempeature [%.1f °C]"       { channel = "siemenshvac:RVS41_813_327:local:local:2237#2246_TAmbAct_CC1"               }
Number:Temperature  Thermostat_Setpoint         "Thermostat setpoint [%.1f °C]"         { channel = "siemenshvac:RVS41_813_327:local:local:1724#1726_ConsConfort_TA_CC1"        }
Number              Heat_Mode                   "Heat mode [%s]"                        { channel = "siemenshvac:RVS41_813_327:local:local:1724#1725_Regime_CC1"                }
``` 

