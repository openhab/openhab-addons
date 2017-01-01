## Thing Configuration

DSC Alarm things can be configured either through the online configuration utility via discovery, or manually through the 'dscalarm.thing' configuration file.  The following table shows the available configuration parameters for each thing.

<table>
	<tr><td><b>Thing</b></td><td><b>Configuration Parameters</b></td></tr>	
	<tr><td>envisalink</td><td><table><tr><td><b>ipAddress</b> - IP address for the Envisalink adapter - Required.</td></tr><tr><td><b>port</b> - TCP port for the Envisalink adapter - Not Required - default = 4025.</td></tr><tr><td><b>password</b> - Password to login to the Envisalink bridge - Not Required.</td></tr><tr><td><b>connectionTimeout</b> - TCP socket connection timeout in milliseconds - Not Required - default=5000.<br/></td></tr><tr><td><b>pollPeriod</b> - Period of time in minutes between the poll command being sent to the Envisalink bridge - Not Required - default=1.</td></tr></table></td></tr>
	<tr><td>it100</td><td><table><tr><td><b>serialPort</b> - Serial port for the IT-100s bridge - Required.</td></tr><tr><td><b>baud</b> - Baud rate of the IT-100 bridge - Not Required - default = 9600.</td></tr><tr><td><b>pollPeriod</b> - Period of time in minutes between the poll command being sent to the IT-100 bridge - Not Required - default=1.</td></tr></table></td></tr>
    <tr><td>tcpserver</td><td><table><tr><td><b>ipAddress</b> - IP address for the TCP Server - Required.</td></tr><tr><td><b>port</b> - TCP port for the TCP Server - Required.</td></tr><tr><td><b>connectionTimeout</b> - TCP socket connection timeout in milliseconds - Not Required - default=5000.<br/></td></tr><tr><td><b>pollPeriod</b> - Period of time in minutes between the poll command being sent to the TCP Server bridge - Not Required - default=1.</td></tr></table></td></tr>
    <tr><td>panel</td><td><table><tr><td><b>userCode</b> - User code for the DSC alarm panel - Not Required.</td></tr><tr><td><b>suppressAcknowledgementMsgs</b> - Suppress the display of acknowledgement messages when received - Not Required - default = false.</td></tr></table></td></tr>
	<tr><td>partition</td><td><b>partitionNumber</b> - Partition number (1-8) - Required.</td></tr>
	<tr><td>zone</td><td><table><tr><td><b>partitionNumber</b> - Partition number (1-8) - Not Required - default=1.</td></tr><tr><td><b>zoneNumber</b> - Zone number (1-64) - Required.</td></tr></table></td></tr>
	<tr><td>keypad</td><td>No parameters</td></tr>
</table>

The binding can be configured manually if discovery is not used.  A thing configuration file in the format 'bindingName.thing' would need to be created, and placed in the 'conf/things' folder.  Here is an example of a thing configuration file called 'dscalarm.thing':

```
Bridge dscalarm:envisalink:MyBridgeName [ ipAddress="192.168.0.100" ]
{
	Thing panel panel
	Thing partition partition1 [ partitionNumber=1 ]
	Thing zone zone1 [ partitionNumber=1, zoneNumber=1 ]
	Thing zone zone9 [ partitionNumber=1, zoneNumber=9 ]
	Thing zone zone10 [ partitionNumber=1, zoneNumber=10 ]
	Thing zone zone11 [ partitionNumber=1, zoneNumber=11 ]
	Thing zone zone12 [ partitionNumber=1, zoneNumber=12 ]
	Thing zone zone13 [ partitionNumber=1, zoneNumber=13 ]
	Thing zone zone14 [ partitionNumber=1, zoneNumber=14 ]
	Thing zone zone15 [ partitionNumber=1, zoneNumber=15 ]
	Thing zone zone21 [ partitionNumber=1, zoneNumber=21 ]
	Thing zone zone22 [ partitionNumber=1, zoneNumber=22 ]
	Thing zone zone23 [ partitionNumber=1, zoneNumber=23 ]
	Thing zone zone24 [ partitionNumber=1, zoneNumber=24 ]
	Thing zone zone25 [ partitionNumber=1, zoneNumber=25 ]
	Thing keypad keypad
}
``` 
