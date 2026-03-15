# Autelis Pool Control Binding

Autelis manufactures a network enabled pool interface for many popular pool systems.
See [the Autelis website](https://www.autelis.com) and the [Autelis Command Protocol](https://www.autelis.com/wiki/index.php?title=Pool_Control_(PI)_HTTP_Command_Reference) for more information.

This binding supports:

- Jandy and Pentair models
- Read circuit, auxiliary, temperature, pump, chemistry and system values
- Control circuit, auxiliary lighting scenes, and temperature set points

## Auto Discovery

The binding will automatically discover Autelis controllers using UPnP.
If found it will try and connect with the factory default username and password.

## Binding Configuration

The binding requires no special configuration

## Manual Thing Configuration

The Autelis binding requires the host, port, username and password

In the thing file, this looks e.g. like

```java
Thing autelis:pentair:myPool [ host="192.168.1.10", port="80", user="admin", password="admin"]
```

or

```java
Thing autelis:jandy:myPool [ host="192.168.1.10", port="80", user="admin", password="admin"]
```

## Channels

### Pentair

Pentair devices support the following channels:

| Channel Type ID     | Item Type |
|---------------------|-----------|
| system-runstate     | Number    |
| system-model        | Number    |
| system-haddr        | Number    |
| system-opmode       | Number    |
| system-freeze       | Number    |
| system-sensor1      | Number    |
| system-sensor2      | Number    |
| system-sensor3      | Number    |
| system-sensor4      | Number    |
| system-sensor5      | Number    |
| system-version      | String    |
| system-time         | Number    |
| equipment-circuit1  | Switch    |
| equipment-circuit2  | Switch    |
| equipment-circuit3  | Switch    |
| equipment-circuit4  | Switch    |
| equipment-circuit5  | Switch    |
| equipment-circuit6  | Switch    |
| equipment-circuit7  | Switch    |
| equipment-circuit8  | Switch    |
| equipment-circuit9  | Switch    |
| equipment-circuit10 | Switch    |
| equipment-feature1  | Number    |
| equipment-feature2  | Number    |
| equipment-feature3  | Number    |
| equipment-feature4  | Number    |
| equipment-feature5  | Number    |
| equipment-feature6  | Number    |
| equipment-feature7  | Number    |
| equipment-feature8  | Number    |
| equipment-feature9  | Number    |
| equipment-feature10 | Number    |
| temp-poolht         | Number    |
| temp-spaht          | Number    |
| temp-htstatus       | Number    |
| temp-poolsp         | Number    |
| temp-spasp          | Number    |
| temp-pooltemp       | Number    |
| temp-spatemp        | Number    |
| temp-airtemp        | Number    |
| temp-soltemp        | Number    |
| temp-tempunits      | String    |
| temp-htpump         | Number    |
| pump-pump1          | String    |
| pump-pump2          | String    |
| pump-pump3          | String    |
| pump-pump4          | String    |
| pump-pump5          | String    |
| pump-pump6          | String    |
| pump-pump7          | String    |
| pump-pump8          | String    |
| chlor-chloren       | Number    |
| chlor-poolsp        | Number    |
| chlor-spasp         | Number    |
| chlor-salt          | Number    |
| chlor-super         | Number    |
| chlor-chlorerr      | Number    |
| chlor-chlorname     | String    |
| lightscmd           | String    |
| reboot              | Switch    |

### Jandy

Jandy devices support the following channels:

| Channel Type ID     | Item Type |
|---------------------|-----------|
| system-model        | Number    |
| system-dip          | Number    |
| system-opmode       | Number    |
| system-vbat         | Number    |
| system-lowbat       | Number    |
| system-time         | Number    |
| equipment-pump      | Number    |
| equipment-pumplo    | Number    |
| equipment-spa       | Number    |
| equipment-waterfall | Number    |
| equipment-cleaner   | Number    |
| equipment-poolht    | Number    |
| equipment-poolht2   | Number    |
| equipment-spaht     | Number    |
| equipment-solarht   | Number    |
| equipment-htpmp     | Number    |
| equipment-aux1      | Number    |
| equipment-aux2      | Number    |
| equipment-aux3      | Number    |
| equipment-aux4      | Number    |
| equipment-aux5      | Number    |
| equipment-aux6      | Number    |
| equipment-aux7      | Number    |
| equipment-aux8      | Number    |
| equipment-aux9      | Number    |
| equipment-aux10     | Number    |
| equipment-aux11     | Number    |
| equipment-aux12     | Number    |
| equipment-aux13     | Number    |
| equipment-aux14     | Number    |
| equipment-aux15     | Number    |
| temp-poolsp         | Number    |
| temp-poolsp2        | Number    |
| temp-pooltemp       | Number    |
| temp-spatemp        | Number    |
| temp-airtemp        | Number    |
| temp-solartemp      | Number    |
| temp-tempunits      | String    |
| pump-vsp1           | String    |
| pump-vsp2           | String    |
| pump-vsp3           | String    |
| pump-vsp4           | String    |
| chem-avail          | Number    |
| chem-chlrp          | Number    |
| chem-saltp          | Number    |
| chem-chlrs          | Number    |
| chem-slats          | Number    |
| chem-orp1           | Number    |
| chem-orp2           | Number    |
| chem-ph1            | Number    |
| chem-ph2            | Number    |
| chem-orpfd1         | Number    |
| chem-orpfd2         | Number    |
| chem-phfd1          | Number    |
| chem-phfd2          | Number    |
| reboot              | Switch    |
