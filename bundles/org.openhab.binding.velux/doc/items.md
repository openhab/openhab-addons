```java
//  Group for simulating push buttons

Group:Switch:OR(ON, OFF) gV "PushButton"

// Velux Bridge channels

String  V_BRIDGE_STATUS     "Velux Bridge Status [%s]"          { channel="velux:klf200:home:status" }
Switch  V_BRIDGE_RELOAD     "Velux Bridge Reload"         (gV)  { channel="velux:klf200:home:reload" }
String  V_BRIDGE_TIMESTAMP  "Velux Bridge Timestamp [%d]"       { channel="velux:klf200:home:timestamp" }

String  V_BRIDGE_FIRMWARE   "Velux Bridge Firmware version [%s]" { channel="velux:klf200:home:firmware" }
String  V_BRIDGE_IPADDRESS  "Velux Bridge LAN IP Address"       { channel="velux:klf200:home:ipAddress" }
String  V_BRIDGE_SUBNETMASK "Velux Bridge LAN IP Subnet Mask"   { channel="velux:klf200:home:subnetMask" }
String  V_BRIDGE_DEFAULTGW  "Velux Bridge LAN Default Gateway"  { channel="velux:klf200:home:defaultGW" }
String  V_BRIDGE_DHCP       "Velux Bridge LAN DHCP Enabled"     { channel="velux:klf200:home:DHCP" }
String  V_BRIDGE_WLANSSID   "Velux Bridge WLAN SSID"            { channel="velux:klf200:home:WLANSSID" }
String  V_BRIDGE_WLANPASSWD "Velux Bridge WLAN Password"        { channel="velux:klf200:home:WLANPassword" }

Switch  V_BRIDGE_DETECTION  "Velux Bridge Detection mode"  (gV) { channel="velux:klf200:home:doDetection" }
String  V_BRIDGE_CHECK      "Velux Bridge Check"                { channel="velux:klf200:home:check" }
String  V_BRIDGE_SCENES     "Velux Bridge Scenes"               { channel="velux:klf200:home:scenes" }
String  V_BRIDGE_PRODUCTS   "Velux Bridge Products"             { channel="velux:klf200:home:products" }

// Velux Scene channels

Switch  V_DG_M_W_OPEN       "Velux DG Window open"     	    (gV) { channel="velux:scene:home:windowOpened:action" }
Switch  V_DG_M_W_UNLOCKED   "Velux DG Window a little open" (gV) { channel="velux:scene:home:windowUnlocked:action" }
Switch  V_DG_M_W_CLOSED	    "Velux DG Window closed"        (gV) { channel="velux:scene:home:windowClosed:action" }

// Velux Bridge channel

Rollershutter RS2 	    "Velux Rolladen 2 [%d]"   		 { channel="velux:klf200:home:shutter#0,V_DG_Shutter_Ost_000,100,V_DG_Shutter_Ost_100", channel="knx:device:bridge:control:VeluxFenster" }


// Velux Actuator channels

Rollershutter	V_DG_M_W	"DG Fenster Bad [%d]"    	{ channel="velux:klf200:home:V_DG_M_W" }
Rollershutter	V_DG_M_W2	"DG Fenster Bad [%d]"		{ channel="velux:klf200:home:V_DG_M_W2" }
Rollershutter	V_DG_M_S    	"DG Bad  [%d]"			{ channel="velux:klf200:home:V_DG_M_S" }
Rollershutter	V_DG_W_S    	"DG West [%d]"			{ channel="velux:klf200:home:V_DG_W_S" }
Rollershutter	V_DG_O_S    	"DG Ost  [%d]"			{ channel="velux:klf200:home:V_DG_O_S" }
```
