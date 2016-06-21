# FileRegexParser Binding

The FileRegexParser binding provides thing to read new lines from a file, parse them with a given regular expression and provide channels for each matching group.

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

The things requires the file name (fileName including full path) and the regular expression to be applied to each line (regEx).
Optional the type (str, num) for each matching group can be defined with the matchingGroupTypes parameter (comma separated list).
It matchingGroupTypes is used, it has to define the type for all of the groups defined in the regEx.
For further information on how to define the regular expressions please see: https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html

## Channels

+ groupCount type Number: The number of matching groups found in the regEx
+ matchingGroupX type String or Number: For each matching group a channel with the name "matchingGroupX" (where X is the number of the matching group in the regEx) is provided. If matchingGroupTypes is not defined, all channels are of type String.

## Full Example

This example will get the load avarage of the linux system every minute and update the related items.

Crontab:
```
* * * * * openhab cat /proc/loadavg >> /tmp/openhab/loadavg.txt
```

Things:

```
 fileregexparser:filetoparse:loadAvg [fileName="/tmp/openhab/loadavg.txt", regEx="^(\\d{0,2}\\.\\d{2})\\s(\\d{0,2}\\.\\d{2})\\s(\\d{0,2}\\.\\d{2})\\s(\\d*/\\d*)\\s(\\d*)$", matchingGroupTypes="num,num,num,str,num"]

```

Items:

```
Number sysNoMatchingGroups    "Number of matching groups [%d]" (gSys) {channel="fileregexparser:filetoparse:loadAvg:groupCount"}
Number sysNoMatchingGroups    "Number of matching groups [%d]" (gSys) {channel="fileregexparser:filetoparse:loadAvg:groupCount"}
Number sysLoadAvg1  "LoadAvg 1min [%s]"  (gSys) { channel="fileregexparser:filetoparse:loadAvg:matchingGroup1" }
Number sysLoadAvg5  "LoadAvg 5min [%s]"  (gSys) { channel="fileregexparser:filetoparse:loadAvg:matchingGroup2" }
Number sysLoadAvg15  "LoadAvg 15min [%s]"  (gSys) { channel="fileregexparser:filetoparse:loadAvg:matchingGroup3" }
String sysThreads  "Threads [%s]"  (gSys) { channel="fileregexparser:filetoparse:loadAvg:matchingGroup4" }
Number sysLastPid  "Last PID [%d]"  (gSys) { channel="fileregexparser:filetoparse:loadAvg:matchingGroup5" }
```
