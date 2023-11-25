# SAICiSMART Binding

OpenHAB binding to the SAIC-API used by MG cars (MG4, MG5 EV, MG ZSV...)

It enables iSMART users to get battery status and other data from their cars. 
They can also pre-heat their cars by turning ON the AC.

Based on the work done here: https://github.com/SAIC-iSmart-API

## Supported Things

European iSMART accounts and vehicles.

- `account`: An iSMART Account
- `vehicle`: An iSMART MG Car

## Discovery

Vehicle discovery is implemented. 
Once an account has been configured it can be scanned for vehicles.

## Thing Configuration

### `account` iSMART Account Configuration

| Name     | Type    | Description                 | Default | Required | Advanced |
|----------|---------|-----------------------------|---------|----------|----------|
| username | text    | iSMART username             | N/A     | yes      | no       |
| password | text    | iSMART password             | N/A     | yes      | no       |
| pin      | text    | Security code setting (PIN) | 123456  | yes      | no       |

### `vehicle` An iSMART MG Car

| Name          | Type | Description                          | Default | Required | Advanced |
|---------------|------|--------------------------------------|---------|----------|----------|
| vin           | text | Vehicle identification number (VIN)  | N/A     | yes      | no       |
| abrpUserToken | text | User token for A Better Routeplanner | N/A     | no       | no       |


## Channels

| Channel                    | Type                     | Read/Write | Description                                         | Advanced |
|----------------------------|--------------------------|------------|-----------------------------------------------------|----------|
| odometer                   | Number:Length            | R          | Total distance driven                               | no       |
| range-electric             | Number:Length            | R          | Electric range                                      | no       |
| soc                        | Number                   | R          | State of the battery in %                           | no       |
| power                      | Number:Power             | R          | Power usage                                         | no       |
| charging                   | Switch                   | R          | Charging                                            | no       |
| engine                     | Switch                   | R          | Engine state                                        | no       |
| speed                      | Number:Speed             | R          | Vehicle speed                                       | no       |
| location                   | Location                 | R          | The actual position of the vehicle                  | no       |
| heading                    | Number:Angle             | R          | The compass heading of the car, (0-360 degrees)     | no       |
| auxiliary-battery-voltage  | Number:ElectricPotential | R          | Auxiliary battery voltage                           | no       |
| tyre-pressure-front-left   | Number:Pressure          | R          | Pressure front left                                 | no       |
| tyre-pressure-front-right  | Number:Pressure          | R          | Pressure front right                                | no       |
| tyre-pressure-rear-left    | Number:Pressure          | R          | Pressure rear left                                  | no       |
| tyre-pressure-rear-right   | Number:Pressure          | R          | Pressure rear right                                 | no       |
| interior-temperature       | Number:Temperature       | R          | Interior temperature                                | no       |
| exterior-temperature       | Number:Temperature       | R          | Exterior temperature                                | no       |
| door-driver                | Contact                  | R          | Driver door open state                              | no       |
| door-passenger             | Contact                  | R          | Passenger door open state                           | no       |
| door-rear-left             | Contact                  | R          | Rear left door open state                           | no       |
| door-rear-right            | Contact                  | R          | Rear right door open state                          | no       |
| window-driver              | Contact                  | R          | Driver window open state                            | no       |
| window-passenger           | Contact                  | R          | Passenger window open state                         | no       |
| window-rear-left           | Contact                  | R          | Rear left window open state                         | no       |
| window-rear-right          | Contact                  | R          | Rear right window open state                        | no       |
| window-sun-roof            | Contact                  | R          | Sun roof open state                                 | no       |
| last-activity              | DateTime                 | R          | Last time the engine was on or the car was charging | no       |
| last-position-update       | DateTime                 | R          | Last time the Position data was updated             | no       |
| last-charge-state-update   | DateTime                 | R          | Last time the Charge State data was updated         | no       |
| remote-ac-status           | Number                   | R          | Status of the A/C                                   | no       |
| switch-ac                  | Switch                   | R/W        | Control the A/C remotely                            | no       |
| force-refresh              | Switch                   | R/W        | Force an immediate refresh of the car data          | yes      |
| last-alarm-message-date    | DateTime                 | R          | Last time an alarm message was sent                 | no       |
| last-alarm-message-content | String                   | R          | Vehicle message                                     | no       |


## Limitations

The advanced channel "force refresh" if used regularly will drain the 12v car battery and you will be unable to start it!

Only European iSMART accounts and vehicles are supported. API host configuration and testing for other markets is required.
