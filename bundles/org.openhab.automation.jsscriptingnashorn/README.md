# JavaScript Scripting (Nashorn)

This add-on allows you to use your older ECMAScript 5.1 code on newer Java versions until the code is migrated to ECMAScript 2021+.
It should only be installed for providing backwards compatibility.
When writing new code it is preferred to do this using ECMAScript 2021+ for which support is provided by installing the [JavaScript Scripting](https://www.openhab.org/addons/automation/jsscripting/) add-on.

This add-on uses a standalone [Nashorn Engine](https://github.com/openjdk/nashorn) to run ECMAScript 5.1 code.
The Nashorn Engine was pre-installed in openHAB 2 and openHAB 3 because it was part of Java.
Since Java 15 the Nashorn Engine has been removed from Java.

## Creating JavaScript Scripts

When this add-on is installed, JavaScript script actions will be run by this add-on and allow ECMAScript 5.1 features.

Alternatively, you can create scripts in the `automation/jsr223` configuration directory.
If you create an empty file called `test.nashornjs`, you will see a log line with information similar to:

```text
    ... [INFO ] [.a.m.s.r.i.l.ScriptFileWatcher:150  ] - Loading script 'test.nashornjs'
```

To enable debug logging, use the [console logging]({{base}}/administration/logging.html) commands to enable debug logging for the automation functionality:

```text
log:set DEBUG org.openhab.core.automation
```

For more information on the available APIs in scripts see the [JSR223 Scripting]({{base}}/configuration/jsr223.html) documentation.

## Script Examples

JavaScript scripts provide access to almost all the functionality in an openHAB runtime environment.
As a simple example, the following script logs "Hello, World!".
Note that `console.log` will usually not work since the output has no terminal to display the text.
The openHAB server uses the [SLF4J](https://www.slf4j.org/) library for logging.

```js
var LoggerFactory = Java.type('org.slf4j.LoggerFactory');

LoggerFactory.getLogger("org.openhab.core.automation.examples").info("Hello, World!");
```

Depending on the openHAB logging configuration, you may need to prefix logger names with `org.openhab.core.automation` for them to show up in the log file (or you modify the logging configuration).

The script uses the [LoggerFactory](https://www.slf4j.org/apidocs/org/slf4j/Logger.html) to obtain a named logger and then logs a message like:

```text
    ... [INFO ] [org.openhab.core.automation.examples ] - Hello, World!
```
