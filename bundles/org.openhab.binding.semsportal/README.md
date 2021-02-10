# SEMSPortal Binding

This binding can help you include statistics of your SEMS / GoodWe solar panel insallation into openHAB. 
It is a readonly connection that maps collected parameters to openHAB channels. 
It provides current, day, month and total yields, as well as some income statistics if you have configured these in the SEMS portal. 
It requires a power station that is connected through the internet to the SEMS portal.

## Supported Things

There is only one thing type supported in this binding, which is the powerStation. 
You need to provide the identifying UUID of the solar system in your account yourself. 
If you have multiple powerstations that report to the same account, you can configure the same account for each powerSation and only differ the unique id of the station in each Thing.

## Discovery

At this moment there is no discovery possible. 
Configuration of the powerStation unique id is done by hand in the Thing configuration page.

## Thing Configuration

The configuration of the thing is pretty straight forward. 
You need to have your power station set up in the SEMS portal, and you need to have an account that is allowed to view the power station data. 
You should log in at least once in the portal with this account to activate it. 

The thing needs the username and password to connect and retreive the data. 
It also needs the unique id of the power station. 
It can be found after you log in to the portal, on the URL. 
Use the value you find in the URL at the x-es (including the -s) in this example: https://www.semsportal.com/powerstation/powerstatussnmin/xxxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx


## Channels


| channel       | type             | description                                                                                                |
| ------------- | ---------------- | ---------------------------------------------------------------------------------------------------------- |
| onlineState   | Switch           | If the powerStation is currently on and reporting to the portal                                            |
| lastUpdate    | DateTime         | Last time the powerStation sent information to the portal                                                  |
| currentOutput | Number:Power     | The current output of the powerStation in Watt                                                             |
| todayTotal    | Number:Energy    | Todays total generation of the station in kWh                                                              |
| monthTotal    | Number:Energy    | This month's total generation of the station in kWh                                                        |
| overallTotal  | Number:Energy    | The total generation of the station since installation, in kWh                                             |
| todayIncome   | Number           | Todays income as reported by the portal, if you have configured the power rates of your energy provider    |
| totalIncome   | Number           | The total income as reported by the portal, if you have configured the power rates of your energy provider |

## Parameters

| Parameter   | Required? | Description                                                                                                |
| ----------- |:---------:| ---------------------------------------------------------------------------------------------------------- |
| username    | X         | Account name (emailaddress) at the SEMS portal. Account must have been used at least once to log in.       |
| password    | X         | Password of the SEMS portal                                                                                |
| station     | X         | UUID of the station. See Thing Configuration which value to use here.                                      |
| update      |           | Number of minutes between two updates. Between 1 and 60 minutes, defaults to 5 minutes                     |

## Credits

This binding has been created using the information provided by RogerG007 in this forum topic: https://community.openhab.org/t/connecting-goodwe-solar-panel-inverter-to-openhab/85480
