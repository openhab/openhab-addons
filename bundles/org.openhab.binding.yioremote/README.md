# YIOremote Binding

This binding will control a YIO Dock/Remote combination. YIO Remote/Dock is a smart home solution that includes an IP based remote. More information can be found at [yio-remote](https://www.yio-remote.com/) or in the forums at [yio-remote](https://community.yio-remote.com/). 

This binding has been designed to compliment the YIO websocket Transport Protocol.

Since this binding allows actual you to trigger IR send/receive actions on YIO Dock, this allows you to use the YIO Dock as an IR solution to openHAB and even learn new IR codes from your remotes. In other words, if the IR code is known then openHAB can use the YIO Dock to control that Device regardless if there is an openHAB binding for it or not.

## Supported Things

* Thing: YIO Dock.

The following are the configurations available to each of the bridges/things:

### YIO Dock

| Name                 | Type    | Required | Default | Description                                                                                                    |
|----------------------|---------|----------|---------|----------------------------------------------------------------------------------------------------------------|
| yiodockhostip        | string  | Yes      | (None)  | IP Address or host name of the YIO Dock                                                                        |
| yiodockaccesstoken   | string  | Yes      | 0       | The authentication token for the access currently 0
                                                      
## Channels

### YIO Dock

The YIO Dock has the following channels:

| Channel 		     		| Input/Output 	| Item Type    | Description                                                                                																			|
|---------------------------|---------------|--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| yiodockreceiverswitch     | Input         | Switch       | The switch to enable diable the IR reciving diode/function                                 																			|
| yiodocksendircode		    | Input         | String       | The IR Code Format(3;0x20DF40BF;32;0) which will send by the YIO Dock                     																				|
| yiodockstatus			    | Output        | String       | The status of the YIO Dock. If the reciever is on than the recognized IR code will be displayed otherwise the IR send status is displayed of the last IR code send.	|


## Full Example

.things

```
yioremote:yioremote:livingroom [ yiodockhostip="192.168.178.21",yiodockaccesstoken="0"  ]
```

.items

```
String yiodocksendircode		"IR CODE [%s]" 			{channel="yioremote:yioremote:livingroom:input#yiodocksendircode"}
Switch yiodockreceiverswitch	"IR recieving switch"	{channel="yioremote:yioremote:livingroom:input#yiodockreceiverswitch"}
String yiodockstatus			"YIO Dock status[%s]" 	{channel="yioremote:yioremote:livingroom:output#yiodockstatus"}
```

.sitemap

```
sitemap Basic label="YIO Dock" {
        Text item=yiodocksendircode
        Switch item=yiodockreceiverswitch
        Text item=yiodockstatus
}
```

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
