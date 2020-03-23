# Jython ScriptEngineFactory

This addon provides a Jython ScriptEngineFactory for use with scripted automation and eliminates the need to download the Jython jar file and create EXTRA_JAVA_OPTS entries for bootclasspath, python.home and python.path.
The path to the bundle will be set for python.home and will also be appended to any existing python.path.
Without this addon, Jython would need to be manually installed as documented here...

[https://openhab-scripters.github.io/openhab-helper-libraries/Getting%20Started/Installation.html](https://openhab-scripters.github.io/openhab-helper-libraries/Getting%20Started/Installation.html)
