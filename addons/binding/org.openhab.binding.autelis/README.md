# Autelis Pool Control Binding

Autelis manufactures a network enabled pool interface for many popular pool systems.
See [the Autelis website](http://www.autelis.com) and the  [Autelis Command Protocol](http://www.autelis.com/wiki/index.php?title=Pool_Control_&lparPI&rpar_HTTP_Command_Reference) for more information.

This binding supports:

*   Read circuit, auxiliary, temperature, pump, chemistry and system values  
*   Control circuit, auxiliary lighting scenes, and temperature set points


## Discovery

The binding will automatically look for a device with the DNS name 'poolcontrol'.
If found it will try and connect with the factory default username and password.

## Binding Configuration

The binding requires no special configuration

## Thing Configuration

The Autelis binding requires the host, port, username and password

In the thing file, this looks e.g. like

```
Thing autelis:myPool [ host="192.168.1.10", port="80", user="admin", password="admin"]
```

## Channels

All devices support some of the following channels:

| Channel Type ID     | Item Type |
|---------------------|-----------|
| system-runstate     | Switch    |
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
