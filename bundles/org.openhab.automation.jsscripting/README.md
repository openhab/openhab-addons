# JavaScript Scripting

This add-on provides support for JavaScript (ECMAScript 2021+) that can be used as a scripting language within automation rules.

## Creating JavaScript Scripts

When this add-on is installed, JavaScript script actions will be run by this add-on and allow ECMAScript 2021+ features.

Alternatively, you can create scripts in the `automation/jsr223` configuration directory.
If you create an empty file called `test.js`, you will see a log line with information similar to:

```text
    ... [INFO ] [.a.m.s.r.i.l.ScriptFileWatcher:150  ] - Loading script 'test.js'
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
const LoggerFactory = Java.type('org.slf4j.LoggerFactory');

LoggerFactory.getLogger("org.openhab.core.automation.examples").info("Hello world!");
```

Depending on the openHAB logging configuration, you may need to prefix logger names with `org.openhab.core.automation` for them to show up in the log file (or you modify the logging configuration).

The script uses the [LoggerFactory](https://www.slf4j.org/apidocs/org/slf4j/Logger.html) to obtain a named logger and then logs a message like:

```text
    ... [INFO ] [.openhab.core.automation.examples:-2   ] - Hello world!
```
