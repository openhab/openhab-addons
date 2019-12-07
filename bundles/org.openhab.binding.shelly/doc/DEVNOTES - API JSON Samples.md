# Shelly Binding (org.openhab.binding.shelly)

This openHAB 2 Binding implements control for the Shelly series of devices.
This includes sending commands to the devices as well as reading the device status and sensor data.

Author: Markus Michels (markus7017)
Check  https://community.openhab.org/t/shelly-binding/ for more information, questions and contributing ideas. Any comments are welcome!

Also check section **Additional Information** at the end of the document.
This includes some general comments, information how to debug and request new features.

---

### Alpha/Beta versions

The binding is work in progress. You have to expect bugs etc. and each version might be incompatible to the existing thing definition, which means no backware compatibility.

Channel definitions are subject to change with any alpha or beta release. Please make sure to **delete all Shelly things before updating*** the binding and clean out the JSON DB:

- **remove all shelly entries from paperui**
- stop oh2 service
- openhab-cli clear-cache
- copy jar into addons (set correct permission)
- start oh2 service
- **re-discover things**
- the channel/item linkage should be restored automatically

If you hit a problem make sure to post a TRACE log (or send PM) so I could look into the details.

### Instalation

As described above the binding will be installed by copying the jar into the addons folder of your OH installation.
Once a stable state is reached the binding may become part of the openHAB 2.5 distribution, but this will take some time.
The binding was developed an tested on OH version 2.4 and 2.5. 
Please post an info if you also verified compatibility to version 2.3.
However, this release is not officially supported.

# Additional Notes

## General

* You should use firmware version 1.5.2 or never.
It might be that the binding is working with older versions, but this was never tested.
List of Firmware Versions for the different devices could be found here: https://api.shelly.cloud/files/firmware


* If you gave multiple network interfaces you should check openHAB's default setting.

Open PaperUI and go to Configuration:System-:Network Settings and verify the selected interface. 
If the Shelly devices are not on the same network you could try to add them manually.
However, devices in different networks have not been tested yet (please post a comment in the community thread if you are successful).

## Reporting a problem/bug

If you encounter a problem you could put the device into DEBUG or TRACE mode

- open OH console (execute "openhab-cli console")
- set the debug level ("log:set DEBUG org.openhab.binding.shelly")
- issue command or wait until problem occurs
- post an extract of openhab.log to the community thread (or send the author a PM - make sure the log extract has enough information, some more lines are fine)

## Feature Request

Any comment or feature request is welcome. Post the idea to the community thread, all of us will benefit.

## Other devices

The thing definition of the following devices is primarily.
If you have one of those devices send a PM to marks7017 and we could work on the implementation/testing.

- thing-type: shellysmoke
- thing-type: shellysense
- thing-type: shellyplug

## Supporting new devices

You could help to integrate and support new devices. In general the following information is a good start

- open a browser and issue the following urls
- http://&lt;device ip&gt;/settings
- http://&lt;device ip&gt;/status

once basic discovery is implemented the Coap Discription could be discovered

- enable CoIoT events within the thing configuration
- open the thing properties ([Show Properties])
- and copy&amp;paste the coapDescr property

post this information in the community thread or send a PM to the author.
Depending on the device type and complexity of the integration you should be prepared to run test cycles with snapshort binds of the binding incl. back and forth communication with the author. 

# REST API JSON Sample Data

## Shelly1

### Shelly1: /status/relay/0

```
{
	"wifi_sta":{"connected":true,"ssid":"iot-wlan","ip":"10.0.0.1","rssi":-70},
	"cloud":{"enabled":false, "connected":false},
	"mqtt":{"connected":true},
	"time":"15:36","serial":1,"has_update":false,"mac":"XXXXXXXXXXXX",
	"relays" :[
		{"ison":false,"has_timer":false}
	],
	"meters":[
		{"power":0.00,"is_valid":"true"}
	],
	"update":{"status":"idle","has_update":false,"new_version":"20190711-084053/v1.5.0-hotfix4@3b4f7414","old_version":"20190711-084053/v1.5.0-hotfix4@3b4f7414"},
	"ram_total":51104,"ram_free":40356,"fs_size":233681,"fs_free":175951,"uptime":3455
}
```

### COAP Device Description Shelly1

```
{
    "blk":[
        {"I":0,"D":"Relay0"}
    ],
    "sen":[
        {"I":112,"T":"Switch","R":"0/1","L":0}
    ],
    "act":[
        {"I":211,"D":"Switch","L":0,"P":[
            {"I":2011,"D":"ToState","R":"0/1"}
            ]
        }
    ]
}
```


## Shelly 1PM

### Shelly 1PM - /settings

```
{
    "device": {
        "type": "SHSW-PM",
        "mac": "XXXXXXXX",
        "hostname": "shelly1pm-XXXXXX",
        "num_outputs": 1,
        "num_meters": 1
    },
    "wifi_ap": {
        "enabled": false,
        "ssid": "shelly1pm-XXXXXX",
        "key": ""
    },
    "wifi_sta": {
        "enabled": true,
        "ssid": "iot-wlan",
        "ipv4_method": "dhcp",
        "ip": null,
        "gw": null,
        "mask": null,
        "dns": null
    },
    "wifi_sta1": {
        "enabled": false,
        "ssid": null,
        "ipv4_method": "dhcp",
        "ip": null,
        "gw": null,
        "mask": null,
        "dns": null
    },
    "mqtt": {
        "enable": false,
        "server": "10.0.0.2:1883",
        "user": "",
        "id": "shelly1pm-XXXXXX",
        "reconnect_timeout_max": 60.0,
        "reconnect_timeout_min": 2.0,
        "clean_session": true,
        "keep_alive": 60,
        "max_qos": 0,
        "retain": false,
        "update_period": 30
    },
    "sntp": {
        "server": "time.google.com"
    },
    "login": {
        "enabled": false,
        "unprotected": false,
        "username": "admin",
        "password": "admin"
    },
    "pin_code": "RPSR$p",
    "name": "",
    "fw": "20191127-095910/v1.5.6@0d769d69",
    "build_info": {
        "build_id": "20191127-095910/v1.5.6@0d769d69",
        "build_timestamp": "2019-11-27T09:59:10Z",
        "build_version": "1.0"
    },
    "cloud": {
        "enabled": true,
        "connected": true
    },
    "timezone": "Europe/Berlin",
    "lat": 50.252491,
    "lng": 5.779092,
    "tzautodetect": false,
    "time": "12:41",
    "hwinfo": {
        "hw_revision": "prod-190329",
        "batch_id": 1
    },
    "max_power": 3500,
    "mode": "relay",
    "relays": [
        {
            "name": null,
            "ison": false,
            "has_timer": false,
            "default_state": "off",
            "btn_type": "edge",
            "btn_reverse": 0,
            "auto_on": 0.0,
            "auto_off": 0.0,
            "btn_on_url": null,
            "btn_off_url": null,
            "out_on_url": "http://10.0.0.20:8080/shelly/event/shelly1pm-e5bed0/relay/0?type=out_on",
            "out_off_url": "http://10.0.0.2:8080/shelly/event/shelly1pm-e5bed0/relay/0?type=out_off",
            "longpush_url": null,
            "shortpush_url": null,
            "schedule": false,
            "schedule_rules": [],
            "max_power": 3500
        }
    ],
    "meters": [
        {
            "power": 0.0,
            "is_valid": true,
            "timestamp": 1575117676,
            "counters": [
                0.0,
                0.0,
                0.0
            ],
            "total": 2262
        }
    ]
}
```

### Shelly 1PM /status

```
{
    "wifi_sta": {
        "connected": true,
        "ssid": "iot-wlan",
        "ip": "10.0.0.2",
        "rssi": -49
    },
    "cloud": {
        "enabled": true,
        "connected": true
    },
    "mqtt": {
        "connected": false
    },
    "time": "12:41",
    "serial": 23,
    "has_update": false,
    "mac": "XXXXXXXXXXXX",
    "relays": [
        {
            "ison": false,
            "has_timer": false,
            "overpower": false
        }
    ],
    "meters": [
        {
            "power": 0.0,
            "is_valid": true,
            "timestamp": 1575117691,
            "counters": [
                0.0,
                0.0,
                0.0
            ],
            "total": 2262
        }
    ],
    "inputs": [
        {
            "input": 0
        }
    ],
    "ext_temperature": {},
    "temperature": 45.51,
    "overtemperature": false,
    "tmp": {
        "tC": 45.51,
        "tF": 113.93,
        "is_valid": "true"
    },
    "update": {
        "status": "idle",
        "has_update": false,
        "new_version": "20191127-095910/v1.5.6@0d769d69",
        "old_version": "20191127-095910/v1.5.6@0d769d69"
    },
    "ram_total": 50704,
    "ram_free": 35428,
    "fs_size": 233681,
    "fs_free": 172437,
    "uptime": 237010
}
```

## Shelly2
 
### /settings

```
{
	"device" : {
	   "type":"SHSW-21","mac":"XXXXXXXXXXXX","hostname":"shellyswitch-XXXXXX",
	   "num_outputs":2,"num_meters":1,"num_rollers":1
	 },
	"wifi_ap":{"enabled":false,"ssid":"shellyswitch-XXXXXX","key":"},
	"wifi_sta":{"enabled":true,"ssid":"iot-wlan","ipv4_method":"static","ip":"10.0.0.10","gw":"10.0.0.1","mask":"255.0.0.0","dns":"10.0.0.1"},
	"wifi_sta1":{"enabled":false,"ssid":null,"ipv4_method":"dhcp","ip":null,"gw":null,"mask":null,"dns":null},
	"mqtt": {"enable":true,"server":"10.0.0.7:1883","user":"admin","reconnect_timeout_max":60.000000,"reconnect_timeout_min":2.000000,
		   "clean_session":true,"keep_alive":60,"will_topic":"shellies/shellyswitch-XXXXXX/online","will_message":"false", max_qos":0,"retain":true,"update_period":30},
	"sntp": {"server":"time.google.com"},
	"login":{"enabled":true,"unprotected":false,"username":"xxx","password":"xxx"},"pin_code":"",
	"coiot_execute_enable":true,"name":"",
	"fw":"20190531-075812/v1.5.0-hotfix2@022ec015","build_info":{"build_id":"20190531-075812/v1.5.0-hotfix2@022ec015","build_timestamp":"2019-05-31T07:58:12Z","build_version":"1.0"},
	"cloud":{"enabled":false,"connected":false},
	"timezone":"Europe/Berlin","lat":45.864700,"lng":7.625460,"tzautodetect":true,"time":"23:02",
	"hwinfo":{"hw_revision":"prod-2018-10c", "batch_id":5},
	"mode":"relay","max_power":1840,
	"relays":[
		{
			"name":null,"ison":false,"has_timer":false,"overpower":falsem "default_state":"last","btn_type":"edge", "btn_reverse":0,
			"auto_on":0.00,"auto_off":0.00,"btn_on_url":null,"btn_off_url":null,"out_on_url":null,"out_off_url":null,"schedule":false,
			"schedule_rules":[]
		},
		{
			"name":null,"ison":false,"has_timer":false,"overpower":false,"default_state":"off","btn_type":"edge","btn_reverse":0,
			"auto_on":0.00,"auto_off":0.00,
			"btn_on_url":null,"btn_off_url":null,"out_on_url":null,"out_off_url":null,
			"schedule":false,"schedule_rules":[]}
		],
		"rollers":[
			{
				"maxtime":20.00,"maxtime_open":20.00,"maxtime_close":20.00,"default_state":"stop","swap":false,"swap_inputs":false,"input_mode":"openclose",
				"button_type":"toggle","btn_reverse":0,
				"state":"stop",
				"power":0.00,
				"is_valid":true,"safety_switch":false,
				"schedule":false,"schedule_rules":[],
				"obstacle_mode":"disabled","obstacle_action":"stop","obstacle_power":200,"obstacle_delay":1,"safety_mode":"while_opening","safety_action":"stop",
				"safety_allowed_on_trigger":"none","off_power":2,
				"positioning":true
			}
		],
		"meters":[
			{"power":0.00,"is_valid":true,"timestamp":1562713329,"counters":[0.000, 0.000, 0.000],"total":19111}
		]
	}
```

### Shelly 2 /settings/relay/0

```
{
	"name":null, "ison":false,"has_timer":false,
	"overpower":false,"default_state":"last",
	"btn_type":"edge","btn_reverse":0,
	"auto_on":0.00,"auto_off":0.00,
	"btn_on_url":null,
	"btn_off_url":null,
	"out_on_url":null,
	"out_off_url":null,
	"schedule":false,
	"schedule_rules":[]
}
```

### Shelly 2: /status/relay/0

```
{
	"wifi_sta":{"connected":true,"ssid":"iot-wlan","ip":"10.0.0.1","rssi":-70},
	"cloud":{"enabled":false, "connected":false},
	"mqtt":{"connected":true},
	"time":"15:36","serial":1,"has_update":false,"mac":"XXXXXXXXXXXX","serial":1,
	"has_update":true,"mac":"XXXXXXXXXXXX",
	"relays":[
		{"ison":false,"has_timer":false,"overpower":false,"is_valid":true},
		{"ison":false,"has_timer":false,"overpower":false,"is_valid":true}
	],
	"rollers":[
		{"state":"stop","power":0.00,"is_valid":true,"safety_switch":false,"stop_reason":"normal","last_direction":"open","current_pos":101,"calibrating":false,"positioning":true}
	],
	"meters":[
		{"power":0.00,"is_valid":true,"timestamp":1563292829,"counters":[0.000, 0.000, 0.000],"total":35473}
	],
	"update":{"status":"pending","has_update":true,"new_version":"20190711-084105/v1.5.0-hotfix4@3b4f7414","old_version":"20190531-075812/v1.5.0-hotfix2@022ec015"},
	"ram_total":50264,"ram_free":37548,
	"fs_size":233681,
	"fs_free":155620,
	"uptime":1352018
}
```


### Shelly2: /status/device

```
{
	"enabled":true, "ssid":"iot-wlan", "ipv4_method":"static","ip":"10.0.0.10","gw":"10.0.0.1","mask":"255.0.0.0","dns":"10.0.0.1"

{
	"wifi_sta":{"connected":true,"ssid":"iot-wlan","ip":"10.0.0.1","rssi":-69},
	"cloud":{"enabled":false,"connected":false},
	"mqtt":{"connected":true},
	"time":"00:17","serial":1,"has_update":false,"mac":"XXXXXXXXXXXX",
	"relays":[
		{
			"ison":false,
			"has_timer":false,
			"overpower":false,
			"is_valid":true
		},
			{
			"ison":false,
			"has_timer":false,
			"overpower":false,
			"is_valid":true
		}
	],
	"rollers":[
		{
			"state":"stop",
			"power":0.00,
			"is_valid":true,
			"safety_switch":false,
			"stop_reason":"normal",
			"last_direction":"stop",
			"current_pos":101,
			"calibrating":false,
			"positioning":true
		}
	],
	"meters":[
			{
				"power":0.00,
				"is_valid":true,
				"timestamp":1562717876,
				"counters":[0.000, 0.000, 0.000],
				"total":19111
			}
	],
	"update":{
		"status":"idle",
		"has_update":false,
		"new_version":"20190531-075812/v1.5.0-hotfix2@022ec015",
		"old_version":"20190531-075812/v1.5.0-hotfix2@022ec015"
	},
	"ram_total":50264,"ram_free":37884,"fs_size":233681,"fs_free":156373,"uptime":777069
}
```

## Shelly 2.5

### Shelly 2.5-Relay: /settings

```
{
	"device": {
		"type": "SHSW-25",
		"mac": "XXXXXXXXXXXX",
		"hostname": "shellyswitch25-XXXXXX",
		"num_outputs": 2,
		"num_meters": 2,
		"num_rollers": 1
	},
	"wifi_ap": {
		"enabled": false,
		"ssid": "shellyswitch25-XXXXXX",
		"key": ""
	},
	"wifi_sta": {
		"enabled": true,
		"ssid": "iot-wlan",
		"ipv4_method": "dhcp",
		"ip": null,
		"gw": null,
		"mask": null,
		"dns": null
	},
	"wifi_sta1": {
		"enabled": false,
		"ssid": null,
		"ipv4_method": "dhcp",
		"ip": null,
		"gw": null,
		"mask": null,
		"dns": null
	},
	"mqtt": {
		"enable": false,
		"server": "10.0.0.2:1883",
		"user": "",
		"id": "shellyswitch25-XXXXXX",
		"reconnect_timeout_max": 60.0,
		"reconnect_timeout_min": 2.0,
		"clean_session": true,
		"keep_alive": 60,
		"max_qos": 0,
		"retain": false,
		"update_period": 30
	},
	"sntp": {
		"server": "time.google.com"
	},
	"login": {
		"enabled": false,
		"unprotected": false,
		"username": "admin",
		"password": "admin"
	},
	"pin_code": "Y}e!BK",
	"name": "",
	"fw": "20191127-095444/v1.5.6@0d769d69",
	"build_info": {
		"build_id": "20191127-095444/v1.5.6@0d769d69",
		"build_timestamp": "2019-11-27T09:54:44Z",
		"build_version": "1.0"
	},
	"cloud": {
		"enabled": true,
		"connected": true
	},
	"timezone": "Europe/Berlin",
	"lat": 50.252491,
	"lng": 5.779092,
	"tzautodetect": false,
	"time": "12:42",
	"hwinfo": {
		"hw_revision": "prod-2019-03",
		"batch_id": 1
	},
	"mode": "relay",
	"max_power": 1840,
	"relays": [
		{
			"name": null,
			"ison": false,
			"has_timer": false,
			"overpower": false,
			"default_state": "off",
			"btn_type": "edge",
			"btn_reverse": 0,
			"auto_on": 0.0,
			"auto_off": 0.0,
			"max_power": 0,
			"btn_on_url": null,
			"btn_off_url": null,
			"out_on_url": "http://10.0.0.2:8080/shelly/event/shellyswitch25-0043e0/relay/0?type=out_on",
			"out_off_url": "http://10.0.0.2:8080/shelly/event/shellyswitch25-0043e0/relay/0?type=out_off",
			"longpush_url": null,
			"shortpush_url": null,
			"schedule": false,
			"schedule_rules": []
		},
		{
			"name": null,
			"ison": false,
			"has_timer": false,
			"overpower": false,
			"default_state": "off",
			"btn_type": "edge",
			"btn_reverse": 0,
			"auto_on": 0.0,
			"auto_off": 0.0,
			"max_power": 0,
			"btn_on_url": null,
			"btn_off_url": null,
			"out_on_url": "http://10.0.0.2:8080/shelly/event/shellyswitch25-0043e0/relay/1?type=out_on",
			"out_off_url": "http://10.0.0.2:8080/shelly/event/shellyswitch25-0043e0/relay/1?type=out_off",
			"longpush_url": null,
			"shortpush_url": null,
			"schedule": false,
			"schedule_rules": []
		}
	],
	"rollers": [
		{
			"maxtime": 20.0,
			"maxtime_open": 20.0,
			"maxtime_close": 20.0,
			"default_state": "stop",
			"swap": false,
			"swap_inputs": false,
			"input_mode": "openclose",
			"button_type": "toggle",
			"btn_reverse": 0,
			"state": "stop",
			"power": 0.0,
			"is_valid": true,
			"safety_switch": false,
			"roller_open_url": null,
			"roller_close_url": null,
			"roller_stop_url": null,
			"schedule": false,
			"schedule_rules": [],
			"obstacle_mode": "disabled",
			"obstacle_action": "stop",
			"obstacle_power": 200,
			"obstacle_delay": 1,
			"safety_mode": "while_opening",
			"safety_action": "stop",
			"safety_allowed_on_trigger": "none",
			"off_power": 2,
			"positioning": true
		}
	],
	"meters": [
		{
			"power": 0.0,
			"is_valid": true,
			"timestamp": 1575117725,
			"counters": [
				0.0,
				0.0,
				0.0
			],
			"total": 47
		},
		{
			"power": 0.0,
			"is_valid": true,
			"timestamp": 1575117725,
			"counters": [
				0.0,
				0.0,
				0.0
			],
			"total": 10215
		}
	]
}
```

### Shelly 2.5 Relay Mode: /status

```
{
	"wifi_sta": {
		"connected": true,
		"ssid": "iot_wlan",
		"ip": "10.0.0.2",
		"rssi": -48
	},
	"cloud": {
		"enabled": true,
		"connected": true
	},
	"mqtt": {
		"connected": false
	},
	"time": "12:42",
	"serial": 83,
	"has_update": false,
	"mac": "XXXXXXXXXXXX",
	"relays": [
		{
			"ison": false,
			"has_timer": false,
			"overpower": false,
			"overtemperature": false,
			"is_valid": true
		},
		{
			"ison": false,
			"has_timer": false,
			"overpower": false,
			"overtemperature": false,
			"is_valid": true
		}
	],
	"rollers": [
		{
			"state": "stop",
			"power": 0.0,
			"is_valid": true,
			"safety_switch": false,
			"overtemperature": false,
			"stop_reason": "normal",
			"last_direction": "stop",
			"current_pos": 101,
			"calibrating": false,
			"positioning": true
		}
	],
	"meters": [
		{
			"power": 0.0,
			"is_valid": true,
			"timestamp": 1575117739,
			"counters": [
				0.0,
				0.0,
				0.0
			],
			"total": 47
		},
		{
			"power": 0.0,
			"is_valid": true,
			"timestamp": 1575117739,
			"counters": [
				0.0,
				0.0,
				0.0
			],
			"total": 10215
		}
	],
	"inputs": [
		{
			"input": 0
		},
		{
			"input": 0
		}
	],
	"temperature": 61.76,
	"overtemperature": false,
	"tmp": {
		"tC": 61.76,
		"tF": 143.16,
		"is_valid": "true"
	},
	"update": {
		"status": "idle",
		"has_update": false,
		"new_version": "20191127-095444/v1.5.6@0d769d69",
		"old_version": "20191127-095444/v1.5.6@0d769d69"
	},
	"ram_total": 49568,
	"ram_free": 34720,
	"fs_size": 233681,
	"fs_free": 156624,
	"voltage": 237.72,
	"uptime": 237037
}
```

### Shelly 2.5 Generic Coap Status 

```
{
    "G":[
        [0,112,0],
        [0,122,0],
        [0,111,0.000000]
        ]
}
```

### Shelly 2.5 Coap Description


```
{"blk":[
    {"I":0,"D":"Relay0"},{"I":1,"D":"Relay1"},
    {"I":2,"D":"Device"}],"sen":[
        {"I":112,"T":"S","D":"State","R":"0/1","L":0},
        {"I":122,"T":"S","D":"State","R":"0/1","L":1},
        {"I":111,"T":"W","D":"Power","R":"0/2300","L":0},
        {"I":121,"T":"W","D":"Power","R":"0/2300","L":1},
        {"I":113,"T":"S","D":"Position","R":"0/100","L":2}
    ],
    "act":[
        {"I":211,"D":"Switch","L":0,"P":[{"I":2011,"D":"ToState","R":"0/1"}]},
        {"I":221,"D":"Switch","L":1,"P":[{"I":2021,"D":"ToState","R":"0/1"}]}
    ]
}
```


```
{
    "blk":[
        {"I":0,"D":"Relay0"},
        {"I":1,"D":"Relay1"},
        {"I":2,"D":"Device"}
        ],
    "sen":[
        {"I":112,"T":"S","D":"State","R":"0/1","L":0},
        {"I":122,"T":"S","D":"State","R":"0/1","L":1},
        {"I":111,"T":"W","D":"Power","R":"0/3680","L":2},
        {"I":113,"T":"S","D":"Position","R":"0/100","L":2}
        ],
    "act":[
        {"I":211,"D":"Switch","L":0,"P":[
            {"I":2011,"D":"ToState","R":"0/1"}
            ]
        },
        {"I":221,"D":"Switch","L":1,"P":[
            {"I":2021,"D":"ToState","R":"0/1"}
            ]
        }]
}
```

### SHelly 2.5  Roller Mode: /settings

```
{
	"device": {
		"type": "SHSW-25",
		"mac": "XXXXXXXXXXXX",
		"hostname": "shellyswitch25-XXXXXX",
		"num_outputs": 2,
		"num_meters": 2,
		"num_rollers": 1
	},
	"wifi_ap": {
		"enabled": false,
		"ssid": "shellyswitch25-XXXXXX",
		"key": ""
	},
	"wifi_sta": {
		"enabled": true,
		"ssid": "iot-wlan",
		"ipv4_method": "dhcp",
		"ip": null,
		"gw": null,
		"mask": null,
		"dns": null
	},
	"wifi_sta1": {
		"enabled": false,
		"ssid": null,
		"ipv4_method": "dhcp",
		"ip": null,
		"gw": null,
		"mask": null,
		"dns": null
	},
	"mqtt": {
		"enable": false,
		"server": "10.0.0.2:1883",
		"user": "",
		"id": "shellyswitch25-XXXXXX",
		"reconnect_timeout_max": 60.0,
		"reconnect_timeout_min": 2.0,
		"clean_session": true,
		"keep_alive": 60,
		"max_qos": 0,
		"retain": false,
		"update_period": 30
	},
	"sntp": {
		"server": "time.google.com"
	},
	"login": {
		"enabled": false,
		"unprotected": false,
		"username": "admin",
		"password": "admin"
	},
	"pin_code": "!pw5e5",
	"name": "",
	"fw": "20191127-095444/v1.5.6@0d769d69",
	"build_info": {
		"build_id": "20191127-095444/v1.5.6@0d769d69",
		"build_timestamp": "2019-11-27T09:54:44Z",
		"build_version": "1.0"
	},
	"cloud": {
		"enabled": true,
		"connected": true
	},
	"timezone": "Europe/Berlin",
	"lat": 50.252491,
	"lng": 5.779092,
	"tzautodetect": false,
	"time": "12:42",
	"hwinfo": {
		"hw_revision": "prod-2019-03",
		"batch_id": 1
	},
	"mode": "roller",
	"max_power": 1840,
	"relays": [
		{
			"name": null,
			"ison": false,
			"has_timer": false,
			"overpower": false,
			"default_state": "off",
			"btn_type": "toggle",
			"btn_reverse": 0,
			"auto_on": 0.0,
			"auto_off": 0.0,
			"max_power": 0,
			"btn_on_url": null,
			"btn_off_url": null,
			"out_on_url": null,
			"out_off_url": null,
			"longpush_url": null,
			"shortpush_url": null,
			"schedule": false,
			"schedule_rules": []
		},
		{
			"name": null,
			"ison": false,
			"has_timer": false,
			"overpower": false,
			"default_state": "off",
			"btn_type": "toggle",
			"btn_reverse": 0,
			"auto_on": 0.0,
			"auto_off": 0.0,
			"max_power": 0,
			"btn_on_url": null,
			"btn_off_url": null,
			"out_on_url": null,
			"out_off_url": null,
			"longpush_url": null,
			"shortpush_url": null,
			"schedule": false,
			"schedule_rules": []
		}
	],
	"rollers": [
		{
			"maxtime": 20.0,
			"maxtime_open": 23.0,
			"maxtime_close": 22.0,
			"default_state": "stop",
			"swap": false,
			"swap_inputs": false,
			"input_mode": "openclose",
			"button_type": "momentary",
			"btn_reverse": 0,
			"state": "stop",
			"power": 0.0,
			"is_valid": true,
			"safety_switch": false,
			"roller_open_url": "http://10.0.0.2:8080/shelly/event/shellyswitch25-XXXXXX/roller/0?type=roller_open",
			"roller_close_url": "http://10.0.0.2:8080/shelly/event/shellyswitch25-XXXXXX/roller/0?type=roller_close",
			"roller_stop_url": "http://10.0.0.2:8080/shelly/event/shellyswitch25-XXXXXX/roller/0?type=roller_stop",
			"schedule": false,
			"schedule_rules": [
				"0700-0123456-10%",
				"0000bss-0123456-open"
			],
			"obstacle_mode": "disabled",
			"obstacle_action": "stop",
			"obstacle_power": 200,
			"obstacle_delay": 1,
			"safety_mode": "while_opening",
			"safety_action": "stop",
			"safety_allowed_on_trigger": "none",
			"off_power": 2,
			"positioning": true
		}
	],
	"meters": [
		{
			"power": 0.0,
			"is_valid": true,
			"timestamp": 1575117752,
			"counters": [
				0.0,
				0.0,
				0.0
			],
			"total": 44
		},
		{
			"power": 0.0,
			"is_valid": true,
			"timestamp": 1575117752,
			"counters": [
				0.0,
				0.0,
				0.0
			],
			"total": 44
		}
	]
}
```

### Shelly 2.5 Roller Mode: /status

```
{
	"wifi_sta": {
		"connected": true,
		"ssid": "iot_wlan",
		"ip": "10.0.0.1",
		"rssi": -54
	},
	"cloud": {
		"enabled": true,
		"connected": true
	},
	"mqtt": {
		"connected": false
	},
	"time": "12:42",
	"serial": 16,
	"has_update": false,
	"mac": "XXXXXXXXXXXX",
	"relays": [
		{
			"ison": false,
			"has_timer": false,
			"overpower": false,
			"overtemperature": false,
			"is_valid": true
		},
		{
			"ison": false,
			"has_timer": false,
			"overpower": false,
			"overtemperature": false,
			"is_valid": true
		}
	],
	"rollers": [
		{
			"state": "stop",
			"power": 0.0,
			"is_valid": true,
			"safety_switch": false,
			"overtemperature": false,
			"stop_reason": "normal",
			"last_direction": "open",
			"current_pos": 100,
			"calibrating": false,
			"positioning": true
		}
	],
	"meters": [
		{
			"power": 0.0,
			"is_valid": true,
			"timestamp": 1575117766,
			"counters": [
				0.0,
				0.0,
				0.0
			],
			"total": 44
		},
		{
			"power": 0.0,
			"is_valid": true,
			"timestamp": 1575117766,
			"counters": [
				0.0,
				0.0,
				0.0
			],
			"total": 44
		}
	],
	"inputs": [
		{
			"input": 0
		},
		{
			"input": 0
		}
	],
	"temperature": 63.62,
	"overtemperature": false,
	"tmp": {
		"tC": 63.62,
		"tF": 146.52,
		"is_valid": "true"
	},
	"update": {
		"status": "idle",
		"has_update": false,
		"new_version": "20191127-095444/v1.5.6@0d769d69",
		"old_version": "20191127-095444/v1.5.6@0d769d69"
	},
	"ram_total": 49568,
	"ram_free": 34712,
	"fs_size": 233681,
	"fs_free": 156122,
	"voltage": 231.83,
	"uptime": 237263
}
```



## Shelly Plug-S

###Shelly Plug-S: /settings

```
{
	"device": {
		"type": "SHPLG-S",
		"mac": "XXXXXXXXXXXX",
		"hostname": "shellyplug-s-XXXX",
		"num_outputs": 1,
		"num_meters": 1
	},
	"wifi_ap": {
		"enabled": false,
		"ssid": "shellyplug-s-XXXX",
		"key": ""
	},
	"wifi_sta": {
		"enabled": true,
		"ssid": "iot-wlan",
		"ipv4_method": "dhcp",
		"ip": null,
		"gw": null,
		"mask": null,
		"dns": null
	},
	"wifi_sta1": {
		"enabled": false,
		"ssid": null,
		"ipv4_method": "dhcp",
		"ip": null,
		"gw": null,
		"mask": null,
		"dns": null
	},
	"mqtt": {
		"enable": false,
		"server": "10.0.0.2:1883",
		"user": "",
		"id": "shellyplug-s-XXXXXX",
		"reconnect_timeout_max": 60.0,
		"reconnect_timeout_min": 2.0,
		"clean_session": true,
		"keep_alive": 60,
		"max_qos": 0,
		"retain": false,
		"update_period": 30
	},
	"sntp": {
		"server": "time.google.com"
	},
	"login": {
		"enabled": false,
		"unprotected": false,
		"username": "admin",
		"password": "admin"
	},
	"pin_code": "adIL-{",
	"name": "",
	"fw": "20191127-095857/v1.5.6@0d769d69",
	"build_info": {
		"build_id": "20191127-095857/v1.5.6@0d769d69",
		"build_timestamp": "2019-11-27T09:58:57Z",
		"build_version": "1.0"
	},
	"cloud": {
		"enabled": true,
		"connected": true
	},
	"timezone": "Europe/Berlin",
	"lat": 50.252491,
	"lng": 5.779092,
	"tzautodetect": false,
	"time": "12:43",
	"hwinfo": {
		"hw_revision": "prod-190516",
		"batch_id": 1
	},
	"max_power": 2500,
	"led_status_disable": true,
	"led_power_disable": false,
	"relays": [
		{
			"ison": false,
			"has_timer": false,
			"overpower": false,
			"default_state": "off",
			"auto_on": 0.0,
			"auto_off": 0.0,
			"btn_on_url": null,
			"out_on_url": "http://10.0.0.1:8080/shelly/event/shellyplug-s-041b29/relay/0?type=out_on",
			"out_off_url": "http://10.0.0.1:8080/shelly/event/shellyplug-s-041b29/relay/0?type=out_off",
			"schedule": true,
			"schedule_rules": [
				"0700-0123456-off"
			],
			"max_power": 2500
		}
	],
	"meters": [
		{
			"power": 0.0,
			"is_valid": true,
			"timestamp": 1575117787,
			"counters": [
				0.0,
				0.0,
				0.0
			],
			"total": 0
		}
	]
}```

### Shelly Plug-S: /status

{
	"wifi_sta": {
		"connected": true,
		"ssid": "iot_wlan",
		"ip": "10.0.0.2",
		"rssi": -52
	},
	"cloud": {
		"enabled": true,
		"connected": true
	},
	"mqtt": {
		"connected": false
	},
	"time": "12:43",
	"serial": 1,
	"has_update": false,
	"mac": "XXXXXXXXXXXX",
	"relays": [
		{
			"ison": false,
			"has_timer": false,
			"overpower": false
		}
	],
	"meters": [
		{
			"power": 0.0,
			"is_valid": true,
			"timestamp": 1575117798,
			"counters": [
				0.0,
				0.0,
				0.0
			],
			"total": 0
		}
	],
	"temperature": 30.71,
	"overtemperature": false,
	"tmp": {
		"tC": 30.71,
		"tF": 87.28,
		"is_valid": "true"
	},
	"update": {
		"status": "idle",
		"has_update": false,
		"new_version": "20191127-095857/v1.5.6@0d769d69",
		"old_version": "20191127-095857/v1.5.6@0d769d69"
	},
	"ram_total": 50784,
	"ram_free": 37312,
	"fs_size": 233681,
	"fs_free": 173441,
	"uptime": 237171
}
```

### ### Shelly Plug-S Coap Description

```
{
    “blk”:[
        {“I”:0,“D”:“Relay0”}
    ],
    “sen”:[
        {“I”:111,“T”:“W”,“R”:“0/2500”,“L”:0},
        {“I”:112,“T”:“Switch”,“R”:“0/1”,“L”:0},
        {“I”:113,“T”:“tC”,“R”:"-40/300",“L”:0},
        {“I”:114,“T”:“tF”,“R”:"-40/300",“L”:0},
        {“I”:115,“T”:“Overtemp”,“R”:“0/1”,“L”:0}
     ],
     “act”:[{“I”:211,“D”:“Switch”,“L”:0,“P”:[{“I”:2011,“D”:“ToState”,“R”:“0/1”}]}]
 }
 
 ```

## Shelly EM

### Shelly EM /settings

```
{
	"device":{
		"type":"SHEM","mac":"XXXXXXXXXXXX","hostname":"shellyem-XXXXXX",
		"num_outputs":1, "num_meters": 0, "num_emeters":2
	 },
	 "wifi_ap":{"enabled":false,"ssid":"shellyem-XXXXXX","key":""},"wifi_sta":{"enabled":true,"ssid":"iot_wlan","ipv4_method":"dhcp","ip":null,"gw":null,"mask":null,"dns":null},"wifi_sta1":{"enabled":false,"ssid":null,"ipv4_method":"dhcp","ip":null,"gw":null,"mask":null,"dns":null},
	 "mqtt": {"enable":false,"server":"10.0.0.1:1883","user":"","reconnect_timeout_max":60.000000,"reconnect_timeout_min":2.000000,"clean_session":true,"keep_alive":60,"will_topic":"shellies/shellyem-XXXXXX/online","will_message":"false","max_qos":0,"retain":false,"update_period":30},
	 "sntp": {"server":"time.google.com"},"login":{"enabled":false,"unprotected":false,"username":"admin","password":"admin"},"pin_code":"BZ1Xg+",
	 "coiot_execute_enable":false,"name":"","fw":"20190821-095337/v1.5.2@4148d2b7","build_info":{"build_id":"20190821-095337/v1.5.2@4148d2b7","build_timestamp":"2019-08-21T09:53:37Z","build_version":"1.0"},
	 "cloud":{"enabled":true,"connected":true},"timezone":"Europe/Berlin","lat":45.775398,"lng":9.181760,"tzautodetect":true,"time":"22:50","hwinfo":{"hw_revision":"prod-2019-06", "batch_id":0},"max_power":0,
	 "relays":[{"name":null,"ison":true,"has_timer":false,"overpower":false,"default_state":"last","auto_on":0.00,"auto_off":0.00,"max_power":0,
	 "out_on_url":"http://10.0.10.254:8080/shelly/event/shellyem-b9f355/relay/0?type=out_on",
	 "out_off_url":"http://10.0.10.254:8080/shelly/event/shellyem-b9f355/relay/0?type=out_off","schedule":false,
	 "schedule_rules":[]}],
	 "emeters":[{"ctraf_type":120},{"ctraf_type":120}]
}
```

### Shelly EM /status

```
{
	"wifi_sta":{"connected":true,"ssid":"iot-wlan","ip":"172.16.12.26","rssi":-54},"cloud":{"enabled":true,"connected":true},
	"mqtt":{"connected":false},"time":"22:56","serial":13416,
	"has_update":false,"mac":"XXXXXXXXXXXX",
	"relays":[{"ison":true,"has_timer":false,"overpower":false,"is_valid":true}],
	"emeters":[
		{"power":98.55,"reactive":-159.32,"voltage":239.38,"is_valid":true,"total":9188.8,"total_returned":16477.0},
		{"power":0.00,"reactive":0.00,"voltage":239.38,"is_valid":true,"total":0.0,"total_returned":0.0}
	 ],
	 "update":{"status":"idle","has_update":false,"new_version":"20190821-095337/v1.5.2@4148d2b7","old_version":"20190821-095337/v1.5.2@4148d2b7"},
	 "ram_total":49960,"ram_free":33784,"fs_size":233681,"fs_free":169425,"uptime":174189
}
```

### Shelly Dimmer /settings

```
{
	"device":{
		"type":"SHDM-1","mac":"XXXXXXXXXXXX","hostname":"shellydimmer-XXXXXX",
		"num_outputs":1,"num_meters":1},
	 "wifi_ap":{"enabled":false,"ssid":"shellydimmer-XXXXXX","key":""},
	 "wifi_sta":{"enabled":true,"ssid":"iot-wlan","ipv4_method":"dhcp","ip":null,"gw":null,"mask":null,"dns":null},
	 "wifi_sta1":{"enabled":false,"ssid":null,"ipv4_method":"dhcp","ip":null,"gw":null,"mask":null,"dns":null},
	 "mqtt": {"enable":false,"server":"10.0.0.1:1883","user":"","id":"shellydimmer-4200A0","reconnect_timeout_max":60.000000,"reconnect_timeout_min":2.000000,"clean_session":true,"keep_alive":60,"will_topic":"shellies/shellydimmer-4200A0/online","will_message":"false","max_qos":0,"retain":false,"update_period":30},
	 "sntp": {"server":"time.google.com"},"login":{"enabled":false,"unprotected":false,"username":"XXXXXX","password":"XXXXXX"},"pin_code":"lFTBFP","coiot_execute_enable":false,"name":"",
	 "fw":"20191018-133016/master@a8aed1ac","build_info":{"build_id":"20191018-133016/master@a8aed1ac","build_timestamp":"2019-10-18T13:30:16Z","build_version":"1.0"},
	 "cloud":{"enabled":true,"connected":true},"timezone":"Europe/Berlin","lat":45.252491,"lng":7.779092,"tzautodetect":false,"time":"06:57",
	 "hwinfo":{"hw_revision":"dev-prototype","batch_id":0},"mode":"white","pulse_mode":2,
	 "calibrated":false,"transition":2000,"fade_rate":1,
	 "lights":[
		{
			"name":"","ison":false,"default_state":"off","auto_on":0.00,"auto_off":0.00,
			"btn1_on_url":"","btn1_off_url":"","btn2_on_url":"","btn2_off_url":"","out_on_url":"","out_off_url":"",
			"schedule":false,"schedule_rules":[],
			"btn_type":"edge","swap_inputs":0
		 }
	 ],
	 "night_mode":{"enabled":0, "start_time":"21:00", "end_time":"00:00", "brightness":10}
}
```


### Shelly Dimmer /status

```
{
	"wifi_sta":{"connected":true,"ssid":"iot-wlan","ip":"10.0.0.195","rssi":-75},
	"cloud":{"enabled":true,"connected":true},
	"mqtt":{"connected":false},
	"time":"13:35","serial":9496,"has_update":false,"mac":"XXXXXXXXXXXX",
	"lights":[
		{"ison":true,"mode":"white","brightness":100}
	],
	"meters":[{"power":22.83, "is_valid":true, "timestamp":1568986518,"counters":[8.305, 3.153, 8.892],"total":782}],
	"inputs":[
		{"input":0},
		{"input":0}
	],
	"tmp":{"tC":56.73,"tF":134.11, "is_valid":"true"},
	"overtemperature":false,
	"loaderror":false,
	"overload":false
	"update":{"status":"unknown","has_update":false,"new_version":"","old_version":"20190913-140549/master@d040e20e"},
	"ram_total":49888,"ram_free":38540,"fs_size":233681,"fs_free":149596,"uptime":561474
}
```


## Shelly Bulb

Firmware Version 1.5.2

### Shelly Bulb /settings

```
{
	"device":{"type":"SHBLB-1","mac":"XXXXXXXXXXXX","hostname":"shellybulb-XXXXXX","num_outputs":1},
	"wifi_ap":{"enabled":false,"ssid":"shellybulb-XXXXXX","key":""},
	"wifi_sta":{"enabled":true,"ssid":iot-wlan","ipv4_method":"static","ip":"10.0.0.10,"gw":"10.0.0.1","mask":"255.0.0.0","dns":"10.0.0.1"},
	"wifi_sta1":{"enabled":false,"ssid":null,"ipv4_method":"dhcp","ip":null,"gw":null,"mask":null,"dns":null},
	"mqtt": {"enable":false,"server":"10.0.0.10:1883","user":"","reconnect_timeout_max":60.000000,"reconnect_timeout_min":2.000000,"clean_session":true,"keep_alive";60,"will_topic":"shellies/shellybulb-XXXXXX/online","will_message":"false","max_qos":0,"retain":false,"update_period":30},
	"sntp": {"server":"time.google.com"},
	"login":{"enabled":false,"unprotected":false,"username":"admin","password":"admin"},
	"pin_code":"aXJ9eE","coiot_execute_enable":true,"name":"",
	"fw":"20190821-094813/v1.5.2@4148d2b7","build_info":{"build_id":"20190821-094813/v1.5.2@4148d2b7","build_timestamp":"2019-08-21T09:48:13Z","build_version":"1.0"},
	"cloud":{"enabled":true,"connected":true},"timezone":"Europe/Berlin","lat":51.394344,"lng":8.571319,"tzautodetect":false,"time":"10:40",
	"hwinfo": {"hw_revision":"prod-1.3","batch_id":1},	   
	 "mode":"color",
	 "lights":[
		 {"ison":true,"red":255,"green":182,"blue":27,"white":0,"gain":100,"temp":3648,"brightness":86,"effect":0,"default_state":"on","auto_on":0.00,"auto_off":0.00,"power":0.00,"schedule":false,"schedule_rules":[]}
		 ]
}
```


### Shelly Bulb /status


```
{
	"wifi_sta":{"connected":true,"ssid":"iot-wlank","ip":"10.0.0.10","rssi":-74},
	"cloud":{"enabled":true,"connected":true},"mqtt":{"connected":false},"time":"10:41","serial":133,"has_update":false,"mac":"XXXXXXXXXXXX",
	"lights":[
		{"ison":true,"mode":"color","red":255,"green":182,"blue":27,"white":0,"gain":100,"temp":3648,"brightness":86,"effect":0}
		],
	"meters":[{"power":0.00,"is_valid":"true"}],
	"update":{"status":"idle","has_update":false,"new_version":"20190821-094813/v1.5.2@4148d2b7","old_version":"20190821-094813/v1.5.2@4148d2b7"},
	"ram_total":51032,"ram_free":37420,"fs_size":233681,"fs_free":171182,"uptime":3125051
}
```

### Shelly Bulb Coap Description - Color Mode

```
{   
    "blk":[{"I":1,"D":"RGBW"}],
    "sen":[
        {"I":111,"T":"Red","R":"0/255","L":0},
        {"I":121,"T":"Green","R":"0/255","L":0},
        {"I":131,"T":"Blue","R":"0/255","L":0},
        {"I":141,"T":"White","R":"0/255","L":0},
        {"I":151,"T":"Gain","R":"0/100","L":0},
        {"I":161,"T":"Temp","R":"3000/6500","L":0},
        {"I":171,"T":"Brightness","R":"0/100","L":0},
        {"I":181,"T":"VSwitch","R":"0/1","L":0}
     ],
     "act":[
        {"I":211,"D":"RGBW","L":0,"P":[{"I":2011,"T":"Red","R":"0/255"},
        {"I":2021,"T":"Green","R":"0/255"},{"I":2031,"T":"Blue","R":"0/255"},
        {"I":2041,"T":"White","R":"0/255"},{"I":2051,"T":"Gain","R":"0/100"},
        {"I":2061,"T":"Temp","R":"3000/6500"},
        {"I":2071,"T":"Brightness","R":"0/100"},
        {"I":2081,"T":"VSwitch","R":"0/1"}]}
     ]
}
```

### Shelly Bulb Coap Description - White Mode

```
{
    "blk":[{"I":1,"D":"RGBW"}],
    "sen":[
        {"I":111,"T":"Red","R":"0/255","L":0},
        {"I":121,"T":"Green","R":"0/255","L":0},
        {"I":131,"T":"Blue","R":"0/255","L":0},
        {"I":141,"T":"White","R":"0/255","L":0},
        {"I":151,"T":"Gain","R":"0/100","L":0},
        {"I":161,"T":"Temp","R":"3000/6500","L":0},
        {"I":171,"T":"Brightness","R":"0/100","L":0},
        {"I":181,"T":"VSwitch","R":"0/1","L":0}
    ],
    "act":[...]
 }
 ```
 

## Shelly RGBW2

### Shelly RGW2 /settiings in color mode

```
{
	"device":{
		"type":"SHRGBW2",
		"mac":"XXXXXXXXXXXX",
		"hostname":"shellyrgbw2-XXXXXX",
		"num_outputs":4
	},
	"wifi_ap":{"enabled":false, "ssid":"shellyrgbw2-XXXXXX", "key":""},
	"wifi_sta":{"enabled":true,"ssid":"iot-wlan","ipv4_method":"dhcp","ip":null,"gw":null,"mask":null,"dns":null},
	"wifi_sta1":{"enabled":false,"ssid":null,"ipv4_method":"dhcp","ip":null,"gw":null,"mask":null,"dns":null},
	"mqtt": {"enable":false,"server":"10.0.0.3:1883","user":"","reconnect_timeout_max":60.000000,"reconnect_timeout_min":2.000000,"clean_session":true,"keep_alive":60,"will_topic":"","will_message":"","max_qos":0,"retain":false,"update_period":30},
	"sntp": {"server":"time.google.com"},"
	login":{"enabled":false,"unprotected":false,"xxx":"xxx","password":"xxx"},
	"pin_code":"PIpf!A",
	"coiot_execute_enable":false,"name":"",
	"fw":"20190711-084448/v1.5.0-hotfix4@3b4f7414",
	"build_info":{"build_id":"20190711-084448/v1.5.0-hotfix4@3b4f7414","build_timestamp":"2019-07-11T08:44:48Z","build_version":"1.0"},
	"cloud":{"enabled":true,"connected":true},
	"timezone":"Europe/Berlin","lat":45.252491,"lng":7.779092,"tzautodetect":false,"time":"07:13",
	"hwinfo": {"hw_revision":"prod-190410b","batch_id":1},
	"mode":"color","
	dcpower":1,
	lights":[
		{
		"ison":false,
		"red":255,"green":0,"blue":0,"white":255,
		"gain":29,
		"effect":0,
		"default_state":"off",
		"auto_on":0.00,"auto_off":0.00,
		"schedule":false,
		"btn_type":"detached",
		"btn_reverse":0,"schedule_rules":[]
		}
	]
}
```

### RGW2 /status color mode

```
{
	"wifi_sta":{"connected":true,"ssid":"iot-wlan","ip":"10.0.0.100","rssi":-69},
	"cloud":{"enabled":true,"connected":true},
	"mqtt":{"connected":false},"time":"07:12",
	"serial":112,
	"has_update":false,"mac":"XXXXXXXXXXXX",
	"mode":"color",
	"input":0,
	"lights":[
		{"ison":false,"mode":"color", "red":255,"green":0,"blue":0,"white":255, "gain":29,"effect":0, "power":0.00,"overpower":false}
	],
	"meters":[
		{"power":0.00,"is_valid":true}
	],
	"update":{"status":"idle","has_update":false,"new_version":"20190711-084448/v1.5.0-hotfix4@3b4f7414","old_version":"20190711-084448/v1.5.0-hotfix4@3b4f7414"},
	"ram_total":50448,"ram_free":35824,"fs_size":233681,"fs_free":162648,"uptime":30380
}
```

### RGBW2 /settings in white mode

```
{
	"device":{"type":"SHRGBW2","mac":"XXXXXXXXXXXX","hostname":"shellyrgbw2-XXXXXX","num_outputs":4},
	"wifi_ap":{"enabled":false,"ssid":"shellyrgbw2-XXXXXX","key":""},
	"wifi_sta":{"enabled":true,"ssid":"iot_wlan","ipv4_method":"dhcp","ip":null,"gw":null,"mask":null,"dns":null},
	"wifi_sta1":{"enabled":false,"ssid":null,"ipv4_method":"dhcp","ip":null,"gw":null,"mask":null,"dns":null},
	"mqtt": {"enable":false,"server":"10.0.0.1:1883","user":"","reconnect_timeout_max":60.000000,"reconnect_timeout_min":2.000000,"clean_session":true,"keep_alive":60,"will_topic":"","will_message":"","max_qos":0,"retain":false,"update_period":30},
	"sntp": {"server":"time.google.com"},"
	login":{"enabled":false,"unprotected":false,"username":"xxx","password":"xxx"},
	"pin_code":"PIpf!A",
	"coiot_execute_enable":false,
	"name":"",
	"fw":"20190711-084448/v1.5.0-hotfix4@3b4f7414",
	"build_info":{"build_id":"20190711-084448/v1.5.0-hotfix4@3b4f7414","build_timestamp":"2019-07-11T08:44:48Z","build_version":"1.0"},
	"cloud":{"enabled":true,"connected":true},
	"timezone":"Europe/Berlin","lat":45.252491,"lng":7.779092,"tzautodetect":false,"time":"20:33",
	"hwinfo": {"hw_revision":"prod-190410b","batch_id":1},
	"mode":"white",
	"dcpower":1,
	"lights":[
		{"ison":true,"brightness":50,"default_state":"on","auto_on":0.00,"auto_off":0.00,"schedule":false,"btn_type":"detached","btn_reverse":0,"schedule_rules":[]},
		{"ison":false,"brightness":50,"default_state":"on","auto_on":0.00,"auto_off":0.00,"schedule":false,"schedule_rules":[]},
		{"ison":false,"brightness":50,"default_state":"on","auto_on":0.00,"auto_off":0.00,"schedule":false,"schedule_rules":[]},
		{"ison":false,"brightness":50,"default_state":"on","auto_on":0.00,"auto_off":0.00,"schedule":false,"schedule_rules":[]}
	]
}
```

### RGBW2 set/statustings in white mode

```
{
	"wifi_sta":{"connected":true,"ssid":"iot_wlan","ip":"10.0.0.100","rssi":-36},
	"cloud":{"enabled":true,"connected":true},
	"mqtt":{"connected":false},"time":"20:33","serial":42,
	"has_update":false,"mac":"XXXXXXXXXXXX","mode":"white","input":0,
	"lights":[
		{"ison":true,"mode":"white","brightness":50,"power":1.20,"overpower":false},
		{"ison":false,"mode":"white","brightness":50,"power":0.00,"overpower":false},
		{"ison":false,"mode":"white","brightness":50,"power":0.00,"overpower":false},
		{"ison":false,"mode":"white","brightness":50,"power":0.00,"overpower":false}
	],
	"meters":[
		{"power":1.20,"is_valid":true},
		{"power":0.00,"is_valid":true},
		{"power":0.00,"is_valid":true},
		{"power":0.00,"is_valid":true}
	],
	"update":{"status":"idle","has_update":false,"new_version":"20190711-084448/v1.5.0-hotfix4@3b4f7414","old_version":"20190711-084448/v1.5.0-hotfix4@3b4f7414"},
	"ram_total":50448,"ram_free":35360,"fs_size":233681,"fs_free":162648,
	"uptime":7201
}
```

## Shelly Sense

### Shelly Sense: /settings

```
{"device":{"type":"SHSEN-1","mac":"XXXXXXXXXXXX","hostname":"shellysense-XXXXXX"},"wifi_ap":{"enabled":false,"ssid":"shellysense-XXXXXX","key":""},"wifi_sta":{"enabled":true,"ssid":"iot-wlan","ipv4_method":"static","ip":"10.0.0.2","gw":"10.0.0.1","mask":"255.255.255.0","dns":"10.0.0.1"},"wifi_sta1":{"enabled":false,"ssid":null,"ipv4_method":"dhcp","ip":null,"gw":null,"mask":null,"dns":null},"mqtt": {"enable":false,"server":"10.0.0.10:1883","user":"","reconnect_timeout_max":60.000000,"reconnect_timeout_min":2.000000,"clean_session":true,"keep_alive":60,"will_topic":"shellies/shellysense-XXXXXXX/online","will_message":"false","max_qos":0,"retain":false,"update_period":30},"sntp": {"server":"time.google.com"},"login":{"enabled":false,"unprotected":false,"username":"admin","password":"admin"},"pin_code":"bS^Mtq","coiot_execute_enable":true,"name":"","fw":"20190821-095017/v1.5.2@4148d2b7","build_info":{"build_id":"20190821-095017/v1.5.2@4148d2b7","build_timestamp":"2019-08-21T09:50:17Z","build_version":"1.0"},"cloud":{"enabled":true,"connected":true},"timezone":"Europe/Berlin","lat":51.394344,"lng":8.571319,"tzautodetect":false,"time":"19:32","light_sensor":"NOA1305","schedule":false,"schedule_rules":[],"sensors":{"motion_duration":20,"motion_led":false,"temperature_unit":"C"}}
```

### Shelly Sense /status

```
{"wifi_sta":{"connected":true,"ssid":"iot-wlan","ip":"10.0.0.2","rssi":-69},"cloud":{"enabled":true,"connected":true},"mqtt":{"connected":false},"time":"19:32","serial":4768,"has_update":false,"mac":"XXXXXXXXXXXX","motion":false,"charger":true,"tmp":{"value":24.249284,"is_valid":true,"units":"C"},"hum":{"value":44.250477,"is_valid":true},"lux":{"value":12.987013,"is_valid":true},"bat":{"value":40},"update":{"status":"idle","has_update":false,"new_version":"20190821-095017/v1.5.2@4148d2b7","old_version":"20190821-095017/v1.5.2@4148d2b7"},"ram_total":51112,"ram_free":26848,"fs_size":83081,"fs_free":26857,"uptime":1823498}
```

### Shelly Sense /ir/list

```
[
	["1_231_pwr","tv(231) - Power"],["1_231_chdwn","tv(231) - Channel Down"],["1_231_chup","tv(231) - Channel Up"],["1_231_voldwn","tv(231) - Volume Down"],
	["1_231_volup","tv(231) - Volume Up"],["1_231_mute","tv(231) - Mute"],["1_231_menu","tv(231) - Menu"],["1_231_inp","tv(231) - Input"],["1_231_info","tv(231) - Info"],
	["1_231_left","tv(231) - Left"],["1_231_up","tv(231) - Up"],["1_231_right","tv(231) - Right"],["1_231_ok","tv(231) - OK"],["1_231_down","tv(231) - Down"],
	["1_231_back","tv(231) - Back"],["6_546_pwr","receiver(546) - Power"],["6_546_voldwn","receiver(546) - Volume Down"],["6_546_volup","receiver(546) - Volume Up"],
	["6_546_mute","receiver(546) - Mute"],["6_546_menu","receiver(546) - Menu"],["6_546_info","receiver(546) - Info"],["6_546_left","receiver(546) - Left"],
	["6_546_up","receiver(546) - Up"],["6_546_right","receiver(546) - Right"],["6_546_ok","receiver(546) - OK"],["6_546_down","receiver(546) - Down"],["6_546_back","receiver(546) - Back"]
]
```

###  Shelly Sense Coap Description

```
{
	"blk":[{"I":1, "D":"sensors"}],
	"sen":[
		{"I":11, "D":"motion", "T":"S", "R":"0/1", "L":1},
		{"I":22, "D":"charger", "T":"S", "R":"0/1", "L":1},
		{"I":33, "D":"temperature", "T":"T", "R":"-40/125", "L":1},
		{"I":44, "D":"humidity", "T":"H", "R":"0/100", "L":1},
		{"I":66, "D":"lux", "T":"L", "R":"0/1", "L":1},
		{"I":77, "D":"battery", "T":"H", "R":"0/100", "L":1}
	 ]
}
```

## Shelly Flood

### Shelly Flood /settings

```
{
	“device”:{“type”:“SHWT-1”,“mac”:“XXXXXXXX”,“hostname”:“shellyflood-XXXXXX”,“sleep_mode”:true},
	“wifi_ap”:{“enabled”:false,“ssid”:“shellyflood-XXXXXX”,“key”:""},
	“wifi_sta”:{“enabled”:true,“ssid”:“iot-wlan”,“ipv4_method”:“dhcp”,“ip”:null,“gw”:null,“mask”:null,“dns”:null},
	“wifi_sta1”:{“enabled”:false,“ssid”:null,“ipv4_method”:“dhcp”,“ip”:null,“gw”:null,“mask”:null,“dns”:null},
	“mqtt”: {“enable”:true,“server”:“XXXXXX”,“user”:“XXXXXX”,“reconnect_timeout_max”:60.000000,“reconnect_timeout_min”:2.000000,“clean_session”:true,“keep_alive”:60,“will_topic”:“shellies/shellyflood-XXXXXX/online”,“will_message”:“false”,“max_qos”:0,“retain”:false,“update_period”:30},
	“sntp”: {“server”:“time.google.com”},login”:{“enabled”:true,“unprotected”:false,“username”:“XXXXXXX”,“password”:“XXXXXX”},“pin_code”:"",“coiot_execute_enable”:false,
	“name”:"",“fw”:“20190821-095233/v1.5.2@4148d2b7”,“build_info”:{“build_id”:“20190821-095233/v1.5.2@4148d2b7”,“build_timestamp”:“2019-08-21T09:52:33Z”,“build_version”:“1.0”},
	“cloud”:{“enabled”:false,“connected”:false},“timezone”:“Europe/Berlin”,“lat”:50.110901,“lng”:8.682130,“tzautodetect”:true,“time”:“11:09”,
	“sensors”:{
		“temperature_threshold”:1.0,
		“temperature_unit”:“C”
	},
	“sleep_mode”:{“period”:24,“unit”:“h”},
	“report_url”:null, 
	“rain_sensor”:false
}
```

### Shelly Flood /status

```
{
	“wifi_sta”:{“connected”:true,“ssid”:“XXXXX”,“ip”:"XXXXXXXX“,“rssi”:-88},
	“cloud”:{“enabled”:false,“connected”:false},“mqtt”:{“connected”:true},
	“time”:“11:07”,“serial”:1,“has_update”:false,“mac”:“XXXXXXXXXXXX”,
	“is_valid”:true,
	“flood”:false,
	“tmp”:{“value”:21.62,“units”:“C”,“tC”:21.62,“tF”:70.93, “is_valid”:true},
	“bat”:{“value”:95,“voltage”:2.92},“act_reasons”:[“button”], 
	“rain_sensor”:false,
	“update”:{“status”:“idle”,“has_update”:false,“new_version”:“20190821-095233/v1.5.2@4148d2b7”,“old_version”:“20190821-095233/v1.5.2@4148d2b7”},
	“ram_total”:50592,“ram_free”:39632,“fs_size”:233681,“fs_free”:154365,“uptime”:22
}
```



# COAP API JSON




### Decoded Device Description

```
	id=0: Relay0
	id=1: Relay1
	id=2: Device
 Adding 5 sensor definitions
	 id 112: State, Type=S, Range=0/1, Links=0
	 id 122: State, Type=S, Range=0/1, Links=1
	 id 111: Power, Type=W, Range=0/2300, Links=0
	 id 121: Power, Type=W, Range=0/2300, Links=1
	 id 113: Position, Type=S, Range=0/100, Links=2
   Device has 2 actors
	 id=211: Switch, Links=0
		 P[2011]: ToState, Range=0/1
	 id=221: Switch, Links=1
		 P[2021]: ToState, Range=0/1
```
		 
		