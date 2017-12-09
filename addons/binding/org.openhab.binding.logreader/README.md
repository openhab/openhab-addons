# <bindingName> LogReader

Binding that reads and parses openHABs `../openhab.log` and updates various channels. After this for example [Telegram Action](http://docs.openhab.org/addons/actions/telegram/readme.html)
can be used to inform user if errors are found.

You can find more info here: [OpenHAB community: LogReader binding](https://community.openhab.org/t/logreader-binding/36440)

## How to get

Download `.jar` from [here](http://lantee.arkku.net/files/org.openhab.binding.logreader-2.2.0-SNAPSHOT.jar) and drop it in your openhab/addons folder. 
Distributing through IoT Marketplace is planned for easier installation.

## Supported Things

At this time Binding supports only one ThingType: `LogReader`

In PaperUI when setting up the thing there's also listed another ThingType LogTailer.
!Do not use this! For now it is only for testing purposes.

## Thing Configuration

Thing is configurable manually or through PaperUI and there's only one configuration parameter. 

| Parameter        | Type    | Required | Default if omitted | Description                                   |                                                                                                                                        
| -----------------| ------- | -------- | ------------------ |---------------------------------------------- |
| `refreshRate`    | integer |    yes   | `60`               | Time between individual log reads. In seconds.|

Log file path is automatically set using system properties.


## Channels

List of channels

| Channel Type ID  | Item Type    | Description                                       |
| ---------------- | ------------ | ------------------------------------------------- |
| `logRotated`     | `DateTime`   | Last time log rotated                             |
| `lastRead`       | `DateTime`   | Last time when log was read                       |
| `lastLine`       | `DateTime`   | Last log lines time stamp                         |
| `warningLines`   | `Number`     | How many [WARN ] lines was found since last read  |
| `errorLines`     | `Number`     | How many [ERROR ] lines was found since last read |
| `lastWarningLine`| `String`     | Contents of last warning line                     |
| `lastErrorLine`  | `String`     | Contents of last error line                       |

## Examples

### Logreader_example.things

```xtend

logreader:reader:reader1[ refreshRate=60 ]

```

### logreader_example.items

```xtend

DateTime logreaderLogRotated        "Last Log Rotation [%1$tY.%1$tm.%1$te %1$tR]"  <time>  { channel="logreader:reader:reader1:logRotated" } 
DateTime logreaderLastRead          "Last Read [%1$tY.%1$tm.%1$te %1$tR]"          <time>  { channel="logreader:reader:reader1:lastRead" }
DateTime logreaderLastLine          "Last Line [%1$tY.%1$tm.%1$te %1$tR]"          <time>  { channel="logreader:reader:reader1:lastLine" }
Number   logreaderWarnings          "Warning lines [%d]"                           <alarm> { channel="logreader:reader:reader1:warningLines" }
Number   logreaderErrors            "Error lines [%d]"                             <alarm> { channel="logreader:reader:reader1:errorLines" }
String   logreaderLastWarningline   "Last warning line [%s]"                               { channel="logreader:reader:reader1:lastWarningLine" }
String   logreaderLastErrorline     "Last error line [%s]"                                 { channel="logreader:reader:reader1:lastErrorLine" }

```

### logreader_example.sitemap

```xtend

sitemap logreader_example label="Example" {
   
    Frame label="LogReader" {
        Text item=logreaderLogRotated
        Text item=logreaderLastRead
        Text item=logreaderLastLine
        Text item=logreaderErrors
        Text item=logreaderWarnings
        Text item=logreaderLastWarningline
        Text item=logreaderLastErrorline
    }
}

```

### logreader_example.rules

```xtend

rule "LogReader"
    when
        Item logreaderLastLine changed
    then
        if (logreaderErrors.state > 0) {
            sendTelegram("<YourBot>", "LogReader alarm!\n\n" 
                                       + logreaderErrors.state.toString 
                                       + " Errors in log! Heres the last one:\n\n" 
                                       + logreaderLastErrorline.state.toString)
                                       // Lines split for readability
        }
    end


```

## Troubleshooting

### Thing status

Check thing status for errors.

### Verbose logging

Enable DEBUG logging in karaf console to see more presice error messages:

`log:set DEBUG org.openhab.binding.logreader`

See [openHAB2 logging docs](http://docs.openhab.org/administration/logging.html#defining-what-to-log) for more help.


