# Siemens RDS Binding

The Siemens RDS binding allows you to connect openHab to Siemens Climtix IC cloud server and integrate your RDS smart thermostats onto the bus.

## Binding Configuration

The binding polls each thermostat that is configured, at the rate as set in the configuration. 

| Property            | Default | Required | Description
|---------------------|---------|----------|---------------------------------------------------------
| userEmailAddress    |         |   Yes    | The registration e-mail address of the RDS thermostats
| userPassword        |         |   Yes    | The registration password of the RDS thermostats
| pollInterval        | 60      |   No     | Refresh interval in seconds

## Item Configuration

The following channels, and their associated data types, are shown below. 

| Property            | Data Type | Description                  
|---------------------|-----------|---------------------------------------------------------------------
| RoomTemperature     | Number    | Room Temperature
| SetTemperature      | Number    | Target room temperature (user can override temporarily)
| RoomHumidity	      | Number    | Room Humidity
| RoomAirQuality      | String    | Room Air Quality (Poor..Good)
| OutsideTemperature  | Number    | Outside temperature
| GreenLeaf           | String    | Energy saving level (Poor..Excellent)
| OccupancyMode	      | String    | Occupancy mode (Absent, Present) (user can override)
| DomesticHotWater    | String    | Hot Water Override (Null/Auto, Off, On) (user can override)

