# Hyperion Binding

This binding integrates the Hyperion software with openHAB.
It currently allows you to send commands to set brightness, color and effect, as well as clearing priorities.

## Supported Things

* Hyperion Server

## Binding Configuration

The binding itself does not require any special configuration.

## Discovery

Currently there is no automatic discovery.  You must manually add a Hyperion Server Thing either through a UI / REST or .thing file.

## Thing Configuration

In order to use a Hyperion Server Thing it must be properly configured.  You can do this either through a UI / REST or through static .thing files.

|Parameter | Data type | Required | Example  |
|------- | -------- | ---- | ---- |
|host	 | String | Y | "192.168.0.10" |
|port	 | Integer | Y | 19444 |
|priority	 | Integer | Y | 50 |
|poll_frequency	 | Integer | Y | 15 |
 
To manually configure a Hyperion Server Thing you must specify the following parameters: host, port, priority and polling frequency. 
 
In the thing file, this looks e.g. like
```
Thing hyperion:server:myServer [ host="192.168.0.10", port=19444, priority=50, poll_frequency=15]
```

## Channels

### Hyperion Server:
|Channel | Description | Example  |
|------- | -------- | ---- |
|brightness	 | Sets/stores the current brightness  | hyperion:server:myServer:brightness |
|effect	 | Sets/stores the current effect | hyperion:server:myServer:effect |
|color	 | Sets/stores the current color | hyperion:server:myServer:color |
|clear	 | Clears the priority specified in the Thing config | hyperion:server:myServer:clear |
|clear_all	 | Clears all priorities | hyperion:server:myServer:clear_all |

## Items:
```
Dimmer Brightness 			"Brightness [%s]" 	{ channel="hyperion:server:myServer:brightness"}
String Effect				"Current effect [%s]" 	{ channel="hyperion:server:myServer:effect"}
Color MyColor				"Color"	 { channel="hyperion:server:myServer:color"}
Switch ClearAllSwitch		"Clear all priorities" { channel="hyperion:server:myServer:clear_all"}
Switch ClearSwitch          "Clear priority"  { channel="hyperion:server:myServer:clear"}
```

## Example Sitemap

Using the above things channels and items 
Sitemap:
```
sitemap demo label="Main Menu" {
        Frame  {
               	Slider item=Brightness
				Selection item=Effect mappings=['Knight rider' = 'Knight rider', 'Red mood blobs'='Red mood blobs']
				Switch item=ClearAllSwitch
				Switch item=ClearSwitch
				Colorpicker item=MyColor
        }
}
```