# YamahaReceiver Binding - Developer documentation

There exist two protocols so far. An xml based one for the following receiver types:
RX-A3010,RX-A2010,RX-A1010,RX-A810,RX-A710,RX-V3071,RX-V2071,RX-V1071,RX-V871,RX-V771,RX-V671,RX-S601D
and a json based protocol for newer receivers.

## XML Protocol

### Overview

The http port 80 on the receiver is used with http POST requests for retrieving and changing the device state. The actual url is http://HOST/YamahaRemoteControl/ctrl.

An example for retrieving data:
```xml
<?xml version="1.0" encoding="utf-8"?>
<YAMAHA_AV cmd="GET">
<ZONE><Basic_Status>GetParam</Basic_Status><ZONE>
</YAMAHA_AV>
```
where ZONE is Main_Zone, Zone_2, Zone_3 or Zone_4.

An example for changing the state of the receiver:
```xml
<YAMAHA_AV cmd="PUT">
<ZONE><Power_Control><Power>On</Power></Power_Control><ZONE>
</YAMAHA_AV>
```
### XML Structure
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
				DAB
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
				Spotify
			Name
				Input
					... (for all HDMI_x, AV_x, USB, Spotify, etc)


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


### XML Descriptor

The AVR provides an XML file that describes features and commands available using this URL:
```
http://<Your_Yamaha_ID>/YamahaRemoteControl/desc.xml
```
Or this URL:
```
http://<Your_Yamaha_ID>/YamahaRemoteControl/UnitDesc.xml
```

As Yamaha introduces new models there may be variations between XML structure.
In an attempt to improve the addon maintenance and troubleshooting selected model's `desc.xml`' has been collected from community users:

* [HTR-4069](desc_HTR-4069.xml) 
* [RX-A2000](desc_RX-A2000.xml) 
* [RX-A3070](desc_RX-A3070.xml) 
* [RX-S601D](desc_RX-S601D.xml) 
* [RX-V479](desc_RX-V479.xml) 
* [RX-V583](desc_RX-V583.xml) 
* [RX-V675](desc_RX-V675.xml) 
* [RX-V3900](desc_RX-V3900.xml) 

### Key differences between models

| Key element | Models   | Desc                                                                                                                                |
|-------------|----------|-------------------------------------------------------------------------------------------------------------------------------------|
| Volume      | RX-V3900 | Volume command uses `Vol` element, while other models have `Volume`. There is no `Feature_Existence` element on system status.      |
| Zone B      | HTR-4069 | `Zone_2` feature does not exist, but instead there is `Zone_B` commands under `Main_Zone` with only power, mute and volume control. |
| Preset      | RX-V3900 | The preset values on this model are strings `A1`, `A2`, `B1`, `B2` instead of numbers.                                              |
| Party Mode  | RX-A2000 | Has party mode support, although its XML descriptor does not mention it.                                                            |