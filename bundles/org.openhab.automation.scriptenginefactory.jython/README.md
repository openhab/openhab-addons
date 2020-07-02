# Jython 2.7.2 ScriptEngineFactory

This addon provides a Jython ScriptEngineFactory for use with scripted automation and eliminates the need to download Jython and create `EXTRA_JAVA_OPTS` entries for `bootclasspath`, `python.home` and `python.path`.
The `python.home` System property will be set to the path of the add-on.
The `python.path` System property will be set to `$OPENHAB_CONF/automation/lib/python`, but any existing `python.path` will be appended to it. 
As an alternative to this add-on, the manual installation of Jython for use with openHAB is documented here...

[https://openhab-scripters.github.io/openhab-helper-libraries/)
