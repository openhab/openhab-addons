# Exec Transformation Service

Transforms an input string with an external program.

Executes an external program and returns the output as a string.
In the given command line the placeholder `%s` is substituted with the input value.

The provided command line is split on spaces before it is passed to the shell.
Using single quotes (`'`) splitting can be avoided (e.g. `'%s'` would prevent splitting on spaces within the input value).
The surrounding single quotes are removed.

The external program must either be in the executable search path of the server process, or an absolute path has to be used.

For security reasons all commands need to be whitelisted.
Allowed commands need to be added to the `misc/exec.whitelist` file in the configuration directory.
Every command needs to be on a separate line.

Example:

```shell
/bin/date -v1d -v+1m -v-1d -v-%s
numfmt --to=iec-i --suffix=B --padding=7 %s

```

## Examples

### General Setup

#### Item

This will replace the visible label in the UI with the transformation you apply with the command <TransformProgram>.
  
```java
String yourItem "Some info  [EXEC(/absolute/path/to/your/<TransformProgram> %s):%s]"
```

#### Rule

```java
rule "Your Rule Name"
when
    Item YourTriggeringItem changed
then
    var formatted = transform("EXEC","/absolute/path/to/your/<TransformProgram>", YourTriggeringItem.state.toString)
    yourFormattedItem.sendCommand(formatted.toString) 
end
```

### Example with a program

Substitute the `/absolute/path/to/your/<TransformProgram>` with

```shell
/bin/date -v1d -v+1m -v-1d -v-%s
```

When the input argument for `%s` is `fri` the execution returns a string with the last weekday of the month, formated as readable text.

```shell
Fri 31 Mar 2017 13:58:47 IST`
```

Or replace it with

```shell
numfmt --to=iec-i --suffix=B --padding=7 %s
```

When the input argument for `%s` is 1234567 it will return the bytes formatted in a better readable form

```shell
1.2MiB
```

### Usage as a Profile

The functionality of this `TransformationService` can be used in a `Profile` on an `ItemChannelLink` too.
To do so, it can be configured in the `.items` file as follows:

```java
String <itemName> { channel="<channelUID>"[profile="transform:EXEC", function="<shellcommand>", sourceFormat="<valueFormat>"]}
```

The shell command to be executed has to be set in the `function` parameter.
The parameter `sourceFormat` is optional and can be used to format the input value **before** the transformation, i.e. `%.3f`.
If omitted the default is `%s`, so the input value will be put into the transformation without any format changes.

Please note: This profile is a one-way transformation, i.e. only values from a device towards the item are changed, the other direction is left untouched.

## Further Reading

* [Manual](http://man7.org/linux/man-pages/man1/date.1.html) and [tutorial](https://linode.com/docs/tools-reference/tools/use-the-date-command-in-linux/) for date.
* [Manual](http://man7.org/linux/man-pages/man1/numfmt.1.html) and [tutorial](https://www.pixelbeat.org/docs/numfmt.html) for numfmt.
