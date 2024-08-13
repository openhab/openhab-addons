# CasoKitchen Binding

Gives you control on Caso WineCooler.

## Supported Things

- `winecooler`: Wine cooler

## Discovery

There's no automatic discovery.

## Thing Configuration

You need a [Caso Account](https://www.casoapp.com/Account/Create) to get configuration parameters.
After register you'll get the

- API key
- Device ID 

### `sample` Thing Configuration

| Name            | Type    | Description                                          | Default | 
|-----------------|---------|------------------------------------------------------|---------|
| apiKey          | text    | API obtained from thing configuration                | N/A     |
| deviceId        | text    | Device Id obtained from thing configuration          | N/A     |
| refreshInterval | integer | Interval the device is polled in minutes             | 5       |

## Channels

### Generic

| Channel       | Type     | Read/Write | Description                  |
|---------------|----------|------------|------------------------------|
| light-control | Switch   | RW         | Control lights for all zones |
| hint          | text     | R          | General command description  |
| last-update   | DateTime | R          | Date and Time of last update |

### Top Zone

| Channel          | Type                  | Read/Write | Description                  |
|------------------|-----------------------|------------|------------------------------|
| temperature      | Number:Temperature    | R          | Current Zone Temperature     |
| set-temperature  | Number:Temperature    | R          | Wanted Zone Temperature      |
| light-control    | Switch                | RW         | Control lights for this zone |
| power            | Switch                | R          | Zone Power                   |

### Bottom Zone

| Channel          | Type                  | Read/Write | Description                  |
|------------------|-----------------------|------------|------------------------------|
| temperature      | Number:Temperature    | R          | Current Zone Temperature     |
| set-temperature  | Number:Temperature    | R          | Wanted Zone Temperature      |
| light-control    | Switch                | RW         | Control lights for this zone |
| power            | Switch                | R          | Zone Power                   |

## Full Example

_Provide a full usage example based on textual configuration files._
_*.things, *.items examples are mandatory as textual configuration is well used by many users._
_*.sitemap examples are optional._

### Thing Configuration

```java
Example thing configuration goes here.
```

### Item Configuration

```java
Example item configuration goes here.
```

### Sitemap Configuration

```perl
Optional Sitemap configuration goes here.
Remove this section, if not needed.
```

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
