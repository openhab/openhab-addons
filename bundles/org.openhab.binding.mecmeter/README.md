# MecMeter Binding

This binding reads data from MEC power meter for providing electrical information for the electric circuit.

To use this binding the meter must be installed, initialized and connected to the same network as openHAB.

## Supported Things

The mecMeter is supported with firmware version starting from 2.0.0.
There is exactly one supported thing type `meter`, which represents the electric meter.
Its unique ID is the serial number.

## Discovery

MecMeters are automatically discovered via mDNS.
The IP of the Power Meter is automatically set and can be changed manually if needed.
The default update interval is set to 5 seconds. Intervals from 1 to 300 seconds can be set manually.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter       | Description                                                           |
|-----------------|-----------------------------------------------------------------------|
| password        | User-defined password during initializing the Power Meter. Mandatory. |
| ip              | The IP address of the meter. Mandatory.                               |
| refreshInterval | Refresh interval in second. Optional, the default value is 5 seconds. |

## Channels

The meter has the following channels:

| Channel Type ID                                              | Item Type                | Label                             | Description                              |
|--------------------------------------------------------------|--------------------------|-----------------------------------|------------------------------------------|
| general_group#frequency                                      | Number:Frequency         | Main Frequency                    | Frequency in Hertz                       |
| general_group#temperature                                    | Number:Temperature       | Internal Temperature              | Internal Temperature of the energy meter |
| general_group#op_time                                        | Number:Time              | Time in Operation                 | Time in Operation                        |
| voltage_group#voltage_phase1                                 | Number:ElectricPotential | Voltage P1                        | Voltage in Volt                          |
| voltage_group#voltage_phase2                                 | Number:ElectricPotential | Voltage P2                        | Voltage in Volt                          |
| voltage_group#voltage_phase3                                 | Number:ElectricPotential | Voltage P3                        | Voltage in Volt                          |
| voltage_group#avg_phase_phase_voltage                        | Number:ElectricPotential | Average Phase – Phase Voltage     | Average Phase – Phase Voltage in Volt    |
| voltage_group#avg_neutral_phase_voltage                      | Number:ElectricPotential | Average Voltage                   | Average N – Phase Voltage in Volt        |
| current_group#current_allphase                               | Number:ElectricCurrent   | Current                           | Current in Ampere                        |
| current_group#current_phase1                                 | Number:ElectricCurrent   | Current P1                        | Current in Ampere                        |
| current_group#current_phase2                                 | Number:ElectricCurrent   | Current P2                        | Current in Ampere                        |
| current_group#current_phase3                                 | Number:ElectricCurrent   | Current P3                        | Current in Ampere                        |
| angle_group#phase_angle_currvolt_phase1                      | Number:Angle             | Current P1                        | Angle Current to Voltage in Degree       |
| angle_group#phase_angle_currvolt_phase2                      | Number:Angle             | Current P2                        | Angle Current to Voltage in Degree       |
| angle_group#phase_angle_currvolt_phase3                      | Number:Angle             | Current P3                        | Angle Current to Voltage in Degree       |
| angle_group#phase_angle_phase1-3                             | Number:Angle             | Angle Voltage to Voltage          | Angle Voltage to Voltage in Degree       |
| angle_group#phase_angle_phase2-3                             | Number:Angle             | Angle Voltage to Voltage          | Angle Voltage to Voltage in Degree       |
| activepower_group#activepower_allphase                       | Number:Power             | Active Power                      | Active power consumed                    |
| activepower_group#activepower_phase1                         | Number:Power             | Active Power P1                   | Active power consumed                    |
| activepower_group#activepower_phase2                         | Number:Power             | Active Power P2                   | Active power consumed                    |
| activepower_group#activepower_phase3                         | Number:Power             | Active Power P3                   | Active power consumed                    |
| activefundpower_group#activefundpower_allphase               | Number:Power             | Active Fundamental Power          | Active fundamental power                 |
| activefundpower_group#activefundpower_phase1                 | Number:Power             | Active Fund Power P1              | Active fundamental power                 |
| activefundpower_group#activefundpower_phase2                 | Number:Power             | Active Fund Power P2              | Active fundamental power                 |
| activefundpower_group#activefundpower_phase3                 | Number:Power             | Active Fund Power P3              | Active fundamental power                 |
| activeharmpower_group#activeharmpower_allphase               | Number:Power             | Active Harmonic Power             | Active harmonic power                    |
| activeharmpower_group#activeharmpower_phase1                 | Number:Power             | Active Harm Power P1              | Active harmonic power                    |
| activeharmpower_group#activeharmpower_phase2                 | Number:Power             | Active Harm Power P2              | Active harmonic power                    |
| activeharmpower_group#activeharmpower_phase3                 | Number:Power             | Active Harm Power P3              | Active harmonic power                    |
| reactivepower_group#reactivepower_allphase                   | Number:Power             | Reactive Power                    | Reactive power consumed                  |
| reactivepower_group#reactivepower_phase1                     | Number:Power             | Reactive Power P1                 | Reactive power consumed                  |
| reactivepower_group#reactivepower_phase2                     | Number:Power             | Reactive Power P2                 | Reactive power consumed                  |
| reactivepower_group#reactivepower_phase3                     | Number:Power             | Reactive Power P3                 | Reactive power consumed                  |
| powerfactor_group#powerFactor_allphase                       | Number:Dimensionless     | Power Factor                      | Power Factor                             |
| powerfactor_group#powerFactor_phase1                         | Number:Dimensionless     | Power Factor P1                   | Power Factor                             |
| powerfactor_group#powerFactor_phase2                         | Number:Dimensionless     | Power Factor P2                   | Power Factor                             |
| powerfactor_group#powerFactor_phase3                         | Number:Dimensionless     | Power Factor P3                   | Power Factor                             |
| apppower_group#apppower_allphase                             | Number:Power             | Apparent Power                    | Apparent power consumed                  |
| apppower_group#apppower_phase1                               | Number:Power             | Apparent Power P1                 | Apparent power consumed                  |
| apppower_group#apppower_phase2                               | Number:Power             | Apparent Power P2                 | Apparent power consumed                  |
| apppower_group#apppower_phase3                               | Number:Power             | Apparent Power P3                 | Apparent power consumed                  |
| fwd_active_energy_group#fwd_active_energy_allphase           | Number:Energy            | Forward Active Energy             | Forward Active Energy in kWh             |
| fwd_active_energy_group#fwd_active_energy_phase1             | Number:Energy            | Fwd Active Energy P1              | Forward Active Energy in kWh             |
| fwd_active_energy_group#fwd_active_energy_phase2             | Number:Energy            | Fwd Active Energy P2              | Forward Active Energy in kWh             |
| fwd_active_energy_group#fwd_active_energy_phase3             | Number:Energy            | Fwd Active Energy P3              | Forward Active Energy in kWh             |
| fwd_active_fund_energy_group#fwd_active_fund_energy_allphase | Number:Energy            | Forward Active Fundamental Energy | Forward Active Fundamental Energy in kWh |
| fwd_active_fund_energy_group#fwd_active_fund_energy_phase1   | Number:Energy            | Fwd Active Fund Energy P1         | Forward Active Fundamental Energy in kWh |
| fwd_active_fund_energy_group#fwd_active_fund_energy_phase2   | Number:Energy            | Fwd Active Fund Energy P2         | Forward Active Fundamental Energy in kWh |
| fwd_active_fund_energy_group#fwd_active_fund_energy_phase3   | Number:Energy            | Fwd Active Fund Energy P3         | Forward Active Fundamental Energy in kWh |
| fwd_active_harm_energy_group#fwd_active_harm_energy_allphase | Number:Energy            | Forward Active Harmonic Energy    | Forward Active Harmonic Energy in kWh    |
| fwd_active_harm_energy_group#fwd_active_harm_energy_phase1   | Number:Energy            | Fwd Active Harm Energy P1         | Forward Active Harmonic Energy in kWh    |
| fwd_active_harm_energy_group#fwd_active_harm_energy_phase2   | Number:Energy            | Fwd Active Harm Energy P2         | Forward Active Harmonic Energy in kWh    |
| fwd_active_harm_energy_group#fwd_active_harm_energy_phase3   | Number:Energy            | Fwd Active Harm Energy P3         | Forward Active Harmonic Energy in kWh    |
| fwd_reactive_energy_group#fwd_reactive_energy_allphase       | Number:Energy            | Forward Reactive Energy           | Forward Reactive Energy in VArh          |
| fwd_reactive_energy_group#fwd_reactive_energy_phase1         | Number:Energy            | Fwd Reactive Energy P1            | Forward Reactive Energy in VArh          |
| fwd_reactive_energy_group#fwd_reactive_energy_phase2         | Number:Energy            | Fwd Reactive Energy P2            | Forward Reactive Energy in VArh          |
| fwd_reactive_energy_group#fwd_reactive_energy_phase3         | Number:Energy            | Fwd Reactive Energy P3            | Forward Reactive Energy in VArh          |
| rev_active_energy_group#rev_active_energy_allphase           | Number:Energy            | Reverse Active Energy             | Reverse Active Energy in kWh             |
| rev_active_energy_group#rev_active_energy_phase1             | Number:Energy            | Rev Active Energy P1              | Reverse Active Energy in kWh             |
| rev_active_energy_group#rev_active_energy_phase2             | Number:Energy            | Rev Active Energy P2              | Reverse Active Energy in kWh             |
| rev_active_energy_group#rev_active_energy_phase3             | Number:Energy            | Rev Active Energy P3              | Reverse Active Energy in kWh             |
| rev_active_fund_energy_group#rev_active_fund_energy_allphase | Number:Energy            | Reverse Active Fundamental Energy | Reverse Active Fundamental Energy in kWh |
| rev_active_fund_energy_group#rev_active_fund_energy_phase1   | Number:Energy            | Rev Active Fund Energy P1         | Reverse Active Fundamental Energy in kWh |
| rev_active_fund_energy_group#rev_active_fund_energy_phase2   | Number:Energy            | Rev Active Fund Energy P2         | Reverse Active Fundamental Energy in kWh |
| rev_active_fund_energy_group#rev_active_fund_energy_phase3   | Number:Energy            | Rev Active Fund Energy P3         | Reverse Active Fundamental Energy in kWh |
| rev_active_harm_energy_group#rev_active_harm_energy_allphase | Number:Energy            | Reverse Active Harmonic Energy    | Reverse Active Harmonic Energy in kWh    |
| rev_active_harm_energy_group#rev_active_harm_energy_phase1   | Number:Energy            | Rev Active Harm Energy P1         | Reverse Active Harmonic Energy in kWh    |
| rev_active_harm_energy_group#rev_active_harm_energy_phase2   | Number:Energy            | Rev Active Harm Energy P2         | Reverse Active Harmonic Energy in kWh    |
| rev_active_harm_energy_group#rev_active_harm_energy_phase3   | Number:Energy            | Rev Active Harm Energy P3         | Reverse Active Harmonic Energy in kWh    |
| rev_reactive_energy_group#rev_reactive_energy_allphase       | Number:Energy            | Reverse Reactive Energy           | Reverse Reactive Energy in VArh          |
| rev_reactive_energy_group#rev_reactive_energy_phase1         | Number:Energy            | Rev Reactive Energy P1            | Reverse Reactive Energy in VArh          |
| rev_reactive_energy_group#rev_reactive_energy_phase2         | Number:Energy            | Rev Reactive Energy P2            | Reverse Reactive Energy in VArh          |
| rev_reactive_energy_group#rev_reactive_energy_phase3         | Number:Energy            | Rev Reactive Energy P3            | Reverse Reactive Energy in VArh          |
| app_energy_group#appenergy_consumption_allphase              | Number:Energy            | Apparent Energy Consumption       | Apparent Energy Consumption in VArh      |
| app_energy_group#appenergy_consumption_phase1                | Number:Energy            | Apparent Energy P1                | Apparent Energy Consumption in VArh      |
| app_energy_group#appenergy_consumption_phase2                | Number:Energy            | Apparent Energy P2                | Apparent Energy Consumption in VArh      |
| app_energy_group#appenergy_consumption_phase3                | Number:Energy            | Apparent Energy P3                | Apparent Energy Consumption in VArh      |

## Full Example

### mecmeter.things

```java
mecmeter:meter:1 [ password="Test1234", ip="192.168.1.16", refreshInterval="10" ]
```

### mecmeter.items

```java
Number:Frequency    MainFrequency           { channel="mecmeter:meter:1:general_group#frequency" }
Number:Temperature  InternalTemperature     { channel="mecmeter:meter:1:general_group#temperature" }
Number:Time         TimeinOperation         { channel="mecmeter:meter:1:general_group#op_time" }

Number:Power ActivePower { channel="mecmeter:meter:1:activepower_group#activepower_allphase" }
Number:Power ActivePowerP1 { channel="mecmeter:meter:1:activepower_group#activepower_phase1" }
Number:Power ActivePowerP2 { channel="mecmeter:meter:1:activepower_group#activepower_phase2" }
Number:Power ActivePowerP3 { channel="mecmeter:meter:1:activepower_group#activepower_phase3" }

Number:ElectricPotential VoltageP1 { channel="mecmeter:meter:1:voltage_group#voltage_phase1" }
Number:ElectricPotential VoltageP2 { channel="mecmeter:meter:1:voltage_group#voltage_phase2" }
Number:ElectricPotential VoltageP3 { channel="mecmeter:meter:1:voltage_group#voltage_phase3" }
Number:ElectricPotential AveragePhasePhaseVoltage { channel="mecmeter:meter:1:voltage_group#avg_phase_phase_voltage" }
Number:ElectricPotential AverageVoltage { channel="mecmeter:meter:1:voltage_group#avg_neutral_phase_voltage" }

Number:ElectricCurrent Current { channel="mecmeter:meter:1:current_group#current_allphase" }
Number:ElectricCurrent Current_Group_Current_Phase1 { channel="mecmeter:meter:1:current_group#current_phase1" }
Number:ElectricCurrent Current_Group_Current_Phase2 { channel="mecmeter:meter:1:current_group#current_phase2" }
Number:ElectricCurrent Current_Group_Current_Phase3 { channel="mecmeter:meter:1:current_group#current_phase3" }

Number:Power ActiveFundamentalPower { channel="mecmeter:meter:1:activefundpower_group#activefundpower_allphase" }
Number:Power ActiveFundPowerP1 { channel="mecmeter:meter:1:activefundpower_group#activefundpower_phase1" }
Number:Power ActiveFundPowerP2 { channel="mecmeter:meter:1:activefundpower_group#activefundpower_phase2" }
Number:Power ActiveFundPowerP3 { channel="mecmeter:meter:1:activefundpower_group#activefundpower_phase3" }

Number:Power ActiveHarmonicPower { channel="mecmeter:meter:1:activeharmpower_group#activeharmpower_allphase" }
Number:Power ActiveHarmPowerP1 { channel="mecmeter:meter:1:activeharmpower_group#activeharmpower_phase1" }
Number:Power ActiveHarmPowerP2 { channel="mecmeter:meter:1:activeharmpower_group#activeharmpower_phase2" }
Number:Power ActiveHarmPowerP3 { channel="mecmeter:meter:1:activeharmpower_group#activeharmpower_phase3" }

Number:Angle Angle_Group_Phase_Angle_Currvolt_Phase1 { channel="mecmeter:meter:1:angle_group#phase_angle_currvolt_phase1" }
Number:Angle Angle_Group_Phase_Angle_Currvolt_Phase2 { channel="mecmeter:meter:1:angle_group#phase_angle_currvolt_phase2" }
Number:Angle Angle_Group_Phase_Angle_Currvolt_Phase3 { channel="mecmeter:meter:1:angle_group#phase_angle_currvolt_phase3" }
Number:Angle Angle_Group_Phase_Angle_Phase13 { channel="mecmeter:meter:1:angle_group#phase_angle_phase1-3" }
Number:Angle Angle_Group_Phase_Angle_Phase23 { channel="mecmeter:meter:1:angle_group#phase_angle_phase2-3" }

Number:Energy ApparentEnergyConsumption { channel="mecmeter:meter:1:app_energy_group#appenergy_consumption_allphase" }
Number:Energy ApparentEnergyP1 { channel="mecmeter:meter:1:app_energy_group#appenergy_consumption_phase1" }
Number:Energy ApparentEnergyP2 { channel="mecmeter:meter:1:app_energy_group#appenergy_consumption_phase2" }
Number:Energy ApparentEnergyP3 { channel="mecmeter:meter:1:app_energy_group#appenergy_consumption_phase3" }

Number:Power ApparentPower { channel="mecmeter:meter:1:apppower_group#apppower_allphase" }
Number:Power ApparentPowerP1 { channel="mecmeter:meter:1:apppower_group#apppower_phase1" }
Number:Power ApparentPowerP2 { channel="mecmeter:meter:1:apppower_group#apppower_phase2" }
Number:Power ApparentPowerP3 { channel="mecmeter:meter:1:apppower_group#apppower_phase3" }

Number:Energy ForwardActiveEnergy { channel="mecmeter:meter:1:fwd_active_energy_group#fwd_active_energy_allphase" }
Number:Energy ForwardActiveFundamentalEnergy { channel="mecmeter:meter:1:fwd_active_fund_energy_group#fwd_active_fund_energy_allphase" }
Number:Energy ForwardActiveHarmonicEnergy { channel="mecmeter:meter:1:fwd_active_harm_energy_group#fwd_active_harm_energy_allphase" }
Number:Energy ForwardReactiveEnergy { channel="mecmeter:meter:1:fwd_reactive_energy_group#fwd_reactive_energy_allphase" }

Number:Energy FwdActiveEnergyP1 { channel="mecmeter:meter:1:fwd_active_energy_group#fwd_active_energy_phase1" }
Number:Energy FwdActiveEnergyP2 { channel="mecmeter:meter:1:fwd_active_energy_group#fwd_active_energy_phase2" }
Number:Energy FwdActiveEnergyP3 { channel="mecmeter:meter:1:fwd_active_energy_group#fwd_active_energy_phase3" }

Number:Energy FwdActiveFundEnergyP1 { channel="mecmeter:meter:1:fwd_active_fund_energy_group#fwd_active_fund_energy_phase1" }
Number:Energy FwdActiveFundEnergyP2 { channel="mecmeter:meter:1:fwd_active_fund_energy_group#fwd_active_fund_energy_phase2" }
Number:Energy FwdActiveFundEnergyP3 { channel="mecmeter:meter:1:fwd_active_fund_energy_group#fwd_active_fund_energy_phase3" }

Number:Energy FwdActiveHarmEnergyP1 { channel="mecmeter:meter:1:fwd_active_harm_energy_group#fwd_active_harm_energy_phase1" }
Number:Energy FwdActiveHarmEnergyP2 { channel="mecmeter:meter:1:fwd_active_harm_energy_group#fwd_active_harm_energy_phase2" }
Number:Energy FwdActiveHarmEnergyP3 { channel="mecmeter:meter:1:fwd_active_harm_energy_group#fwd_active_harm_energy_phase3" }

Number:Energy FwdReactiveEnergyP1 { channel="mecmeter:meter:1:fwd_reactive_energy_group#fwd_reactive_energy_phase1" }
Number:Energy FwdReactiveEnergyP2 { channel="mecmeter:meter:1:fwd_reactive_energy_group#fwd_reactive_energy_phase2" }
Number:Energy FwdReactiveEnergyP3 { channel="mecmeter:meter:1:fwd_reactive_energy_group#fwd_reactive_energy_phase3" }

Number:Energy PowerFactor { channel="mecmeter:meter:1:powerfactor_group#powerFactor_allphase" }
Number:Energy PowerFactorP1 { channel="mecmeter:meter:1:powerfactor_group#powerFactor_phase1" }
Number:Energy PowerFactorP2 { channel="mecmeter:meter:1:powerfactor_group#powerFactor_phase2" }
Number:Energy PowerFactorP3 { channel="mecmeter:meter:1:powerfactor_group#powerFactor_phase3" }

Number:Power ReactivePower { channel="mecmeter:meter:1:reactivepower_group#reactivepower_allphase" }
Number:Power ReactivePowerP1 { channel="mecmeter:meter:1:reactivepower_group#reactivepower_phase1" }
Number:Power ReactivePowerP2 { channel="mecmeter:meter:1:reactivepower_group#reactivepower_phase2" }
Number:Power ReactivePowerP3 { channel="mecmeter:meter:1:reactivepower_group#reactivepower_phase3" }

Number:Energy ReverseActiveEnergy { channel="mecmeter:meter:1:rev_active_energy_group#rev_active_energy_allphase" }
Number:Energy RevActiveEnergyP1 { channel="mecmeter:meter:1:rev_active_energy_group#rev_active_energy_phase1" }
Number:Energy RevActiveEnergyP2 { channel="mecmeter:meter:1:rev_active_energy_group#rev_active_energy_phase2" }
Number:Energy RevActiveEnergyP3 { channel="mecmeter:meter:1:rev_active_energy_group#rev_active_energy_phase3" }

Number:Energy ReverseActiveFundamentalEnergy { channel="mecmeter:meter:1:rev_active_fund_energy_group#rev_active_fund_energy_allphase" }
Number:Energy RevActiveFundEnergyP1 { channel="mecmeter:meter:1:rev_active_fund_energy_group#rev_active_fund_energy_phase1" }
Number:Energy RevActiveFundEnergyP2 { channel="mecmeter:meter:1:rev_active_fund_energy_group#rev_active_fund_energy_phase2" }
Number:Energy RevActiveFundEnergyP3 { channel="mecmeter:meter:1:rev_active_fund_energy_group#rev_active_fund_energy_phase3" }

Number:Energy ReverseActiveHarmonicEnergy { channel="mecmeter:meter:1:rev_active_harm_energy_group#rev_active_harm_energy_allphase" }
Number:Energy RevActiveHarmEnergyP1 { channel="mecmeter:meter:1:rev_active_harm_energy_group#rev_active_harm_energy_phase1" }
Number:Energy RevActiveHarmEnergyP2 { channel="mecmeter:meter:1:rev_active_harm_energy_group#rev_active_harm_energy_phase2" }
Number:Energy RevActiveHarmEnergyP3 { channel="mecmeter:meter:1:rev_active_harm_energy_group#rev_active_harm_energy_phase3" }

Number:Energy ReverseReactiveEnergy { channel="mecmeter:meter:1:rev_reactive_energy_group#rev_reactive_energy_allphase" }
Number:Energy RevReactiveEnergyP1 { channel="mecmeter:meter:1:rev_reactive_energy_group#rev_reactive_energy_phase1" }
Number:Energy RevReactiveEnergyP2 { channel="mecmeter:meter:1:rev_reactive_energy_group#rev_reactive_energy_phase2" }
Number:Energy RevReactiveEnergyP3 { channel="mecmeter:meter:1:rev_reactive_energy_group#rev_reactive_energy_phase3" }
```

### mecmeter.sitemap

```perl
sitemap mecmeter label="MecMeter"
{
    Frame label="General" {
            Text item=MainFrequency
            Text item=InternalTemperature
    }

    Frame label="Power" {
        Text item=ActivePower
        Text item=ActivePowerP1
        Text item=ActivePowerP2
        Text item=ActivePowerP3
    }

    Frame label="Electric Potential" {
        Text item=VoltageP1
        Text item=VoltageP2
        Text item=VoltageP3
    }

}
```
