# FileRegexParser Binding

The FileRegexParser binding reads files and applies a regular expression. For each regular expression capturing group, a channel is created dynamically. The states of the channels are always updated to the last line (matching) of the file.

Example usage:
- write system load to a file
- use FileRegexParser to monitor the system load in OpenHAB2 
(see full example)

## Supported Things

This binding supports one thing: filetoparse

## Discovery

Discovery is not applicable.

## Binding Configuration

No binding configuration required.

## Thing Configuration

The things require the file name (fileName including the full path) and the regular expression to be applied to each line (regEx).
Optionally the type (str, num) for each capturing group can be defined with the capturingGroupTypes parameter (comma separated list).
If capturingGroupTypes is used, it has to define the types for all of the groups defined in the regEx.
For further information on how to define the regular expressions please see: https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html

## Channels

+ groupCount type Number: The number of capturing groups found in the regEx
+ capturingGroupX type String or Number: For each capturing group a channel with the name "capturingGroupX" (where X is the number of the capturing group in the regEx) is provided. If capturingGroupTypes is not defined, all channels are of type String.

## Full Example

This example will get the load average of the linux system every minute and update the related items.

Crontab:
```
* * * * * openhab cat /proc/loadavg >> /tmp/openhab/loadavg.txt
```

Things:

```
 fileregexparser:filetoparse:loadAvg [fileName="/tmp/openhab/loadavg.txt", regEx="^(\\d{0,2}\\.\\d{2})\\s(\\d{0,2}\\.\\d{2})\\s(\\d{0,2}\\.\\d{2})\\s(\\d*/\\d*)\\s(\\d*)$", capturingGroupTypes="num,num,num,str,num"]

```

Items:

```
Number sysNoCapturingGroups    "Number of capturing groups [%d]" (gSys) {channel="fileregexparser:filetoparse:loadAvg:groupCount"}
Number sysNoCapturingGroups    "Number of capturing groups [%d]" (gSys) {channel="fileregexparser:filetoparse:loadAvg:groupCount"}
Number sysLoadAvg1  "LoadAvg 1min [%s]"  (gSys) { channel="fileregexparser:filetoparse:loadAvg:capturingGroup1" }
Number sysLoadAvg5  "LoadAvg 5min [%s]"  (gSys) { channel="fileregexparser:filetoparse:loadAvg:capturingGroup2" }
Number sysLoadAvg15  "LoadAvg 15min [%s]"  (gSys) { channel="fileregexparser:filetoparse:loadAvg:capturingGroup3" }
String sysThreads  "Threads [%s]"  (gSys) { channel="fileregexparser:filetoparse:loadAvg:capturingGroup4" }
Number sysLastPid  "Last PID [%d]"  (gSys) { channel="fileregexparser:filetoparse:loadAvg:capturingGroup5" }
```
