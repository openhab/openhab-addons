# Log Reader Binding

This binding reads and analyzes log files. Search patterns are fully configurable, therefore different kind of log files should be possible to monitor by this binding.
When certain log events is recognized, openHAB rules can be used to send notification about the event e.g by email for further analysis.

## Supported Things

This binding supports one ThingType: `reader`.
A reader supports 3 separate channels; One for errors, one for warnings and one custom channel for other purposes.

## Thing Configuration

The `reader` Thing has the following configuration parameters: 

| Parameter                     | Type    | Required | Default if omitted               | Description                                                                             |
| ------------------------------| ------- | -------- | -------------------------------- |-----------------------------------------------------------------------------------------|
| `filePath`                    | String  |   yes    | `${OPENHAB_LOGDIR}/openhab.log`  | Path to log file. ${OPENHAB_LOGDIR} is automatically replaced by the correct directory. |
| `refreshRate`                 | integer |   no     | `1000`                           | Time in milliseconds between individual log reads.                                      |
| `errorPatterns`               | String  |   no     | `ERROR+`                         | Search patterns separated by \| character for warning events.                            |
| `errorBlacklistingPatterns`   | String  |   no     |                                  | Search patterns for blacklisting unwanted error events separated by \| character.       |
| `warningPatterns`             | String  |   no     | `WARN+`                          | Search patterns separated by \| character for error events.                              |
| `warningBlacklistingPatterns` | String  |   no     |                                  | Search patterns for blacklisting unwanted warning events separated by \| character.     |
| `customPatterns`              | String  |   no     |                                  | Search patterns separated by \| character for custom events.                             |
| `customBlacklistingPatterns`  | String  |   no     |                                  | Search patterns for blacklisting unwanted custom events separated by \| character.      |

Search patterns follows Java regular expression syntax. See https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html.

## Channels

List of channels

| Channel Type ID    | Item Type    | Description                                                    |
| ------------------ | ------------ | -------------------------------------------------------------- |
| `lastErrorEvent`   | `String`     | Displays contents of last [ERROR] event                        |
| `lastWarningEvent` | `String`     | Displays contents of last [WARN] event                         |
| `lastCustomEvent`  | `String`     | Displays contents of last custom event                         |
| `errorEvents`      | `Number`     | Displays number of [ERROR] lines matched to search pattern     |
| `warningEvents`    | `Number`     | Displays number of [WARN] lines matched to search pattern      |
| `customEvents`     | `Number`     | Displays number of custom lines matched to search pattern      |
| `logRotated`       | `DateTime`   | Last time when log rotated recognized                          |
| `newErrorEvent`    | -            | Trigger channel for last [ERROR] line                          |
| `newWarningEvent`  | -            | Trigger channel for last [ERROR] line                          |
| `newCustomEvent`   | -            | Trigger channel for last [ERROR] line                          |

## Examples

### example.things

```xtend

logreader:reader:openhablog[ refreshRate=1000, errorPatterns="ERROR+", errorBlacklistingPatterns="annoying error which should ignored|Another annoying error which should ignored" ]

```

### example.items

```xtend

String   logreaderLastError         "Last error [%s]"                                      { channel="logreader:reader:openhablog:lastErrorEvent" }
String   logreaderLastWarning       "Last warning [%s]"                                    { channel="logreader:reader:openhablog:lastWarningEvent" }
Number   logreaderErrors            "Error events matched [%d]"                            { channel="logreader:reader:openhablog:errorEvents" }
Number   logreaderWarnings          "Warning events matched [%d]"                          { channel="logreader:reader:openhablog:warningEvents" }
DateTime logreaderLogRotated        "Last Log Rotation [%1$tY.%1$tm.%1$te %1$tR]"          { channel="logreader:reader:openhablog:logRotated" } 

```

### example.sitemap

```xtend

sitemap logreader_example label="Example" {
    Frame label="LogReader" {
        Text item=logreaderLastError
        Text item=logreaderLastWarning
        Text item=logreaderErrors
        Text item=logreaderWarnings
        Text item=logreaderLogRotated
    }
}

```

### example.rules

```xtend
rule "LogReader"
    when
        Channel "logreader:reader:openhablog:newErrorEvent" triggered
    then
        // do something
    end
```

Be careful when sending e.g. email notifications.
You could easily send thousand of *spam* emails in short period if e.g. one binding is in error loop.

### Thing status

Check thing status for errors.

### Verbose logging

Enable DEBUG logging in karaf console to see more precise error messages:

`log:set DEBUG org.openhab.binding.logreader`

See [openHAB2 logging docs](https://www.openhab.org/docs/administration/logging.html#defining-what-to-log) for more help.


