# Groovy Scripting

This add-on provides support for [Groovy](https://groovy-lang.org/) 4.0.23 that can be used as a scripting language within automation rules and which eliminates the need to manually install Groovy.

## Creating Groovy Scripts

When this add-on is installed, you can select Groovy as a scripting language when creating a script action within the rule editor of the UI.

Alternatively, you can create scripts in the `automation/jsr223` configuration directory.
If you create an empty file called `test.groovy`, you will see a log line with information similar to:

```text
    ... [INFO ] [.a.m.s.r.i.l.ScriptFileWatcher:150  ] - Loading script 'test.groovy'
```

To enable debug logging, use the [console logging]({{base}}/administration/logging.html) commands to enable debug logging for the automation functionality:

```shell
log:set DEBUG org.openhab.core.automation
```

For more information on the available APIs in scripts see the [JSR223 Scripting]({{base}}/configuration/jsr223.html) documentation.

## Code reuse

One can place *.groovy files with Groovy classes under `automation/groovy` configuration directory.
Those classes can be imported in JSR-223 scripts or the UI rules action with the usual Groovy `import` statement.

To apply shared code changes, one has to restart the `openHAB Core :: Bundles :: Automation` bundle on the Console or an openHAB instance altogether.

## Script Examples

Groovy scripts provide access to almost all the functionality in an openHAB runtime environment.
As a simple example, the following script logs "Hello, World!".
Note that `System.out.println` will usually not work since the output has no terminal to display the text.
The openHAB server uses the [SLF4J](https://www.slf4j.org/) library for logging.

```groovy
import org.slf4j.LoggerFactory

LoggerFactory.getLogger("org.openhab.core.automation.examples").info("Hello, World!")
```

Depending on the openHAB logging configuration, you may need to prefix logger names with `org.openhab.core.automation` for them to show up in the log file (or you modify the logging configuration).

The script uses the [LoggerFactory](https://www.slf4j.org/apidocs/org/slf4j/Logger.html) to obtain a named logger and then logs a message like:

```text
    ... [INFO ] [.openhab.core.automation.examples:-2   ] - Hello, World!
```
