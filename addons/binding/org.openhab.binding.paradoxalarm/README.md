## Example things configuration

# Main panel thing configuration
**refresh** - value is in seconds. Defines the refresh interval when the binding polls from paradox system.<br>
**ip150Password** - pretty obvious. The password to your IP150 (not your panel PIN).<br>
**pcPassword** - The code 3012 setting. Default value is 0000 for Paradox<br>
**ipAddress** - pretty obvious. IP address of your IP150<br>
**port** - the port used for data communication. Default is 10000 for Paradox<br>
<code>paradoxalarm:paradoxCommunication:panel [refresh=5, ip150Password="<YOUR IP150 PASSWORD>", pcPassword="0000", ipAddress="10.10.10.10", port=10000 ] </code><br><br>
# Partition thing configuration
**id** - the number how it's configured in babyware. For Evo192 it's 1-8.<br>
**refresh** - refresh when partition polls from local cache. Not really important. Just defines regular time. Default=30sec<br>
<code>
paradoxalarm:paradoxPartition:partition1 [id=1, refresh=10]<br>
paradoxalarm:paradoxPartition:partition2 [id=2, refresh=17]<br>
paradoxalarm:paradoxPartition:partition3 [id=3, refresh=30]<br>
paradoxalarm:paradoxPartition:partition4 [id=4, refresh=3]<br>
</code>
# Zone thing configuration<br>
**id** - the number how it's configured in babyware. For Evo192 it's 1-192.<br>
**refresh** - refresh when partition polls from local cache. Not really important. Just defines regular time.paradoxalarm:zone:bedroomBathMUC [id=20, refresh=10] Default=30sec<br>
<code>
paradoxalarm:zone:bedroomPir [id=19, refresh=10]
</code>

## Example items configuration
<code>
String panelType "Paradox panel type: [%s]" <lock> (Security) { channel = "paradoxalarm:paradoxCommunication:panel:panelType" } <br>
String serialNumber "Paradox Serial number: [%s]" <lock> (Security) { channel = "paradoxalarm:paradoxCommunication:panel:serialNumber" }<br>
String hardwareVersion "Paradox HW version: [%s]" <lock> (Security) { channel = "paradoxalarm:paradoxCommunication:panel:hardwareVersion" }<br>
String applicationVersion "Paradox Application version: [%s]" <lock> (Security) { channel = "paradoxalarm:paradoxCommunication:panel:applicationVersion" }<br>
String bootloaderVersion "Paradox Bootloader version: [%s]" <lock> (Security) { channel = "paradoxalarm:paradoxCommunication:panel:bootloaderVersion" }<br>

String partition1Label "Partition1 label: [%s]" <lock> (Security) { channel = "paradoxalarm:paradoxPartition:partition1:label" }<br>
String partition1State "Partition1 state: [%s]" <lock> (Security) { channel = "paradoxalarm:paradoxPartition:partition1:state" }<br>
String partition1AdditionalStates "Partition1 additional states: [%s]" <lock> (Security) { channel = "paradoxalarm:paradoxPartition:partition1:addidionalStates" }<br><br>
String  partition2Label "Partition2 label: [%s]" <lock> (Security) { channel = "paradoxalarm:paradoxPartition:partition2:label" }<br>
String  partition2State "Partition2 state: [%s]" <lock> (Security) { channel = "paradoxalarm:paradoxPartition:partition2:state" }<br>
String  partition2AdditionalStates "Partition2 additional state: [%s]" <lock> (Security) { channel = "paradoxalarm:paradoxPartition:partition2:addidionalStates" }<br><br>
String zoneBedRoomLabel "Zone label: [%s]" <lock> (Security) { channel = "paradoxalarm:zone:bedroomBathMUC:label" }<br>
Contact zoneBedRoomIsOpened "Zone opened: [%s]" <lock> (Security) { channel = "paradoxalarm:zone:bedroomBathMUC:isOpened" }<br>
Contact zoneBedRoomIsTampered "Zone tampered: [%s]" <lock> (Security) { channel = "paradoxalarm:zone:bedroomBathMUC:isTampered" }<br>
Contact zoneBedRoomHasLowBattery "Zone low battery: [%s]" <lock> (Security) { channel = "paradoxalarm:zone:bedroomBathMUC:hasLowBattery" }<br>
<br>