# Jython Scripting

This add-on provides [Jython](https://www.jython.org/) 2.7 that can be used as a scripting language within automation rules and which eliminates the need to download Jython and create `EXTRA_JAVA_OPTS` entries for `bootclasspath`, `python.home` and `python.path`.

The `python.home` system property is set to the path of the add-on.

The `python.path` system property is set to `$OPENHAB_CONF/automation/jython/lib`, but any existing `python.path` will be appended to it.

## Creating Jython Scripts

When this add-on is installed, you can select Jython as a scripting language when creating a script action within the rule editor of the UI.

Alternatively, you can create scripts in the `automation/jython` configuration directory.
If you create an empty file called `test.py`, you will see a log line with information similar to:

```text
    ... [INFO ] [.a.m.s.r.i.l.ScriptFileWatcher:150  ] - Loading script 'test.py'
```

To enable debug logging, use the [console logging]({{base}}/administration/logging.html) commands to
enable debug logging for the automation functionality:

```text
log:set DEBUG org.openhab.core.automation
```

## Script Examples

Jython scripts provide access to almost all the functionality in an openHAB runtime environment.
As a simple example, the following script logs "Hello, World!".
Note that `print` will usually not work since the output has no terminal to display the text.
The openHAB server uses the [SLF4J](https://www.slf4j.org/) library for logging.

```python
from org.slf4j import LoggerFactory

LoggerFactory.getLogger("org.openhab.core.automation.examples").info("Hello, World!")
```

Jython can [import Java classes](https://jython.readthedocs.io/en/latest/ModulesPackages/).
Depending on the openHAB logging configuration, you may need to prefix logger names with `org.openhab.core.automation` for them to show up in the log file (or you modify the logging configuration).

::: tip Note
Be careful with using wildcards when importing Java packages (e.g., `import org.slf4j.*`).
This will work in some cases, but it might not work in some situations.
It is best to use explicit imports with Java packages.
For more details, see the Jython documentation on
[Java package scanning](https://jython.readthedocs.io/en/latest/ModulesPackages/#java-package-scanning).
:::

The script uses the [LoggerFactory](https://www.slf4j.org/apidocs/org/slf4j/Logger.html)
to obtain a named logger and then logs a message like:

```text
    ... [INFO ] [.openhab.core.automation.examples:-2   ] - Hello, World!
```
