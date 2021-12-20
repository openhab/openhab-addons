# JRuby Scripting

This add-on provides [JRuby](https://www.jruby.org/) 9.3.2 that can be used as a scripting language within automation rules.

## JRuby Scripting Configuration

After installing this add-on, you will find configuration options in the openHAB portal under _Settings -> Other Services -> JRuby Scripting_.

Alternatively, JRuby configuration parameters may be set by creating a `jruby.cfg` file in `conf/services/`

| Parameter                                             | Default                                 | Description                                                                                                                                                                                                 |
| ----------------------------------------------------- | --------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| org.openhab.automation.jrubyscripting:gem_home        | $OPENHAB_CONF/scripts/lib/ruby/gem_home | Location ruby gems will be installed and loaded, directory will be created if missing and gem installs are specified                                                                                        |
| org.openhab.automation.jrubyscripting:rubylib         | $OPENHAB_CONF/automation/lib/ruby/      | Search path for user libraries. Separate each path with a colon (semicolon in Windows).                                                                                                                     |
| org.openhab.automation.jrubyscripting:local_context   | singlethread                            | The local context holds Ruby runtime, name-value pairs for sharing variables between Java and Ruby. See [this](https://github.com/jruby/jruby/wiki/RedBridge#Context_Instance_Type) for options and details |
| org.openhab.automation.jrubyscripting:local_variables | transient                               | Defines how variables are shared between Ruby and Java. See [this](https://github.com/jruby/jruby/wiki/RedBridge#local-variable-behavior-options) for options and details                                   |
| org.openhab.automation.jrubyscripting:gems            |                                         | Comma separated list of [Ruby Gems](https://rubygems.org/) to install.                                                                                                                                      |

## Ruby Gems

This automation add-on will install user specified gems and make them available on the library search path.
Gem versions may be specified using the standard ruby gem_name=version format.
The version number follows the [pessimistic version constraint](https://guides.rubygems.org/patterns/#pessimistic-version-constraint) syntax.

For example this configuration will install version 4 or higher of the [openHAB JRuby Scripting Library](https://boc-tothefuture.github.io/openhab-jruby/).

```text
org.openhab.automation.jrubyscripting:gems=openhab-scripting=~>4.0
```

## Creating JRuby Scripts

When this add-on is installed, you can select JRuby as a scripting language when creating a script action within the rule editor of the UI.

Alternatively, you can create scripts in the `automation/jsr223/ruby/personal` configuration directory.
If you create an empty file called `test.rb`, you will see a log line with information similar to:

```text
    ... [INFO ] [.a.m.s.r.i.l.ScriptFileWatcher:150  ] - Loading script 'test.rb'
```

To enable debug logging, use the [console logging]({{base}}/administration/logging.html) commands to
enable debug logging for the automation functionality:

```text
log:set DEBUG org.openhab.core.automation
log:set DEBUG org.openhab.automation.jrubyscripting
```

## Imports

All [ScriptExtensions]({{base}}/configuration/jsr223.html#scriptextension-objects-all-jsr223-languages) are available in JRuby with the following exceptions/modifications:

- The File variable, referencing java.io.File is not available as it conflicts with Ruby's File class preventing Ruby from initializing
- Globals scriptExtension, automationManager, ruleRegistry, items, voice, rules, things, events, itemRegistry, ir, actions, se, audio, lifecycleTracker are prepended with a $ (e.g. $automationManager) making them available as a global objects in Ruby.

## Script Examples

JRuby scripts provide access to almost all the functionality in an openHAB runtime environment.
As a simple example, the following script logs "Hello, World!".
Note that `puts` will usually not work since the output has no terminal to display the text.
The openHAB server uses the [SLF4J](https://www.slf4j.org/) library for logging.

```ruby
require 'java'
java_import org.slf4j.LoggerFactory

LoggerFactory.getLogger("org.openhab.core.automation.examples").info("Hello world!")
```

JRuby can [import Java classes](https://github.com/jruby/jruby/wiki/CallingJavaFromJRuby).
Depending on the openHAB logging configuration, you may need to prefix logger names with `org.openhab.core.automation` for them to show up in the log file (or you modify the logging configuration).
