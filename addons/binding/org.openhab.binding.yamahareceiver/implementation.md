# Yamahareceiver Binding - Developer documentation

An overview is presented with the following diagram:
![Class Diagram](doc/classes.png "Class Diagram")

The control flow is visualised here:
![ControlFlow](doc/ControlFlow.png "ControlFlow")

The YamahaReceiverCommunication class is a pure protocol implementation.
Yamaha uses a XML API. The receiver does not allow long polling or any
other type of state-update notification, therefore we use a timer for
periodically refreshing the state.

The thing handler needs a host and zone configuration that is usually
given by the discovery service (YamahaDiscoveryParticipant via UPNP).
createCommunicationObject() is called in initialize(). There we create
a communication object (YamahaReceiverCommunication) which needs the host
and zone parameters and hand it over to a newly created YamahaReceiverState.
We also setup the refresh timer.

There is a second discovery service, the ZoneDiscoveryService which is created
and used in createCommunicationObject() Main_Zone thing handler.
For every other zone a discovery result will be generated and can be added by the user.

## Protocol

There exist two protocols so far. An xml based one for the following receiver types:
RX-A3010,RX-A2010,RX-A1010,RX-A810,RX-A710,RX-V3071,RX-V2071,RX-V1071,RX-V871,RX-V771,RX-V671,
and a json based protocol for newer reeivers. At the moment there exists only an implementation
for the xml based protocol.

### XML protocol overview

The http port 80 on the receiver is used with http POST requests for retrieving and changing the device state. The actual url is http://HOST/YamahaRemoteControl/ctrl.

An example for retrieving data:
<?xml version="1.0" encoding="utf-8"?>
<YAMAHA_AV cmd="GET">
<ZONE><Basic_Status>GetParam</Basic_Status><ZONE>
</YAMAHA_AV>

where ZONE is Main_Zone, Zone_2, Zone_3 or Zone_4.

An example for changing the state of the receiver:

<YAMAHA_AV cmd="PUT">
<ZONE><Power_Control><Power>On</Power></Power_Control><ZONE>
</YAMAHA_AV>

### Xml Structure
	System	
		Config
			Model_Name
			System_ID	
			Version	
			Feature_Existence	
				Main_Zone
				Zone_2
				Zone_3
				Zone_4
				Tuner
				HD_Radio
				SIRIUS
				iPod
				Bluetooth
				UAW
				Rhapsody
				SIRIUS_IR
				Pandora
				Napster
				PC
				NET_RADIO
				USB
				iPod_USB
			Name
				Input
					... (for all HDMI_x, AV_x, USB, etc)


		Power_Control
			Power
			Auto_Power_Down
		Party_Mode
			Mode ("On"/"Off")
			Target_Zone
			Volume
		Sound_Video
		Input_Output
		Speaker_Preout
		Misc
		Unit_Desc
			Version
			URL
		Service_Info
			Destination
			Freq_Step

	Main Zone	
		Config
			Feature_Availability
			Name -> Zone/Scene
		Basic_Status
			Power_Control
			Volume
			Input
			Surround
			Party_Info
			Pure_Direct
			Sound_Video
		Power_Control
			Power
			Sleep
		Volume
			Lvl
			Mute
			Max_Lvl
			Init_Lvl
			Memory
		Input
			Input_Sel
			Input_Sel_Item_Info
			Input_Sel_Item
			Audio_Sel
			Decoder_Sel
		Scene
			...
		Sound_Video
			...
		Surround
		Play_Control
		List_Control

	Zone2,3	
		Config
		Basic_Status
		Power_Control
		Volume
		Input
		Scene
		Sound_Video
		Play_Control
		List_Control

	Zone4	
		Config
		Basic_Status
		Power_Control
		Input
		Scene
		Play_Control
		List_Control

	USB	
		Config
		Play_Control
		Play_Info
		List_Control
		List_Info

	Tuner	
		Config
		Play_Control
		Play_Info

	iPod/USB/PC/NET_RADIO
		Config
		Play_Control
		Play_Info
		List_Control
		List_Info
