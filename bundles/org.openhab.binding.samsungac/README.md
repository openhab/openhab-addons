# Samsung Air Conditioner

This binding uses the HTTP interface of the Samsung air conditioner to control and monitor it.

## Supported Things

There is one supported thing type: `ac`

## Thing Configuration

The thing type `ac` has a few configuration parameters:

| Parameter         | Description                                                                          |
|-------------------|--------------------------------------------------------------------------------------|
| token             | Bearer for this specific Samsung air conditioner                              |
| ip                | The IP address for this thing                                                         |
| port              | Unique ID of the measuring station.                                                  |
| refresh           | Refresh interval in seconds. Default value is 60 minutes.                            |
| keystore          | Absolute path to keystore with Private Key for the Webserver in the Digital Inverter |
| keystore_secret   | Password to the encrypted keystore                                                   |

## Channels

The information from the Samsung Digital Inverter:

| Channel ID           | Item Type          | Description                                                                     |
|----------------------|--------------------|---------------------------------------------------------------------------------|
| power                | Switch             | Power On/Off the Inverter                                                       |
| autoclean            | Switch             | Enable/Disable Autoclean                                                        |
| beep                 | Switch             | Enable/Disable Beep on the unit                                                 |
| setpoint_temperature | Number:Temperature | Desired Temperature                                                             |
| temperature          | Number:Temperature | Measured Temperature                                                            |
| outdoor_temperature  | Number:Temperature | Outdoor Unit Temperature                                                    |
| winddirection        | String             | Set Wind Direction Automatic=All, Up and Down=Up_And_Low, Fixed=Fix |
| windspeed            | Number             | Set windspeed Auto=0, Low=1, Medium=2, High=3, Turbo=4                                        |
| operation_mode       | String             | Set operating mode Auto, Heat, Cool, Dry, Wind                                                     |
| comode               | String             | Comfort Settings: Normal=Comode_Off, Quiet=Comode_Quiet, Comfort=Comode_Comfort, Smart=Comode_Smart, 25Step=Comode_25Step                 |
| filtertime           | Number             | Operating hours since last Filter Cleaning                  |
| filteralarmtime      | Number             | Set Thershold for Filter Cleaning INterval                  |
| alarm                | String             | Device Alarm Status                             |
