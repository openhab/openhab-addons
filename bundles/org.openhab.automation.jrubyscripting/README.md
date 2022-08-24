# JRuby Scripting

This add-on provides [JRuby](https://www.jruby.org/) scripting language for automation rules.

## JRuby Scripting Configuration

After installing this add-on, you will find configuration options in the openHAB portal under _Settings -> Other Services -> JRuby Scripting_.

Alternatively, JRuby configuration parameters may be set by creating a `jruby.cfg` file in `conf/services/`

| Parameter                                             | Default                                 | Description                                                                                                                                                                                                 |
| ----------------------------------------------------- | --------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| org.openhab.automation.jrubyscripting:gem_home        | $OPENHAB_CONF/scripts/lib/ruby/gem_home | Location ruby gems will be installed and loaded, directory will be created if missing and gem installs are specified                                                                                        |
| org.openhab.automation.jrubyscripting:rubylib         | $OPENHAB_CONF/automation/lib/ruby/      | Search path for user libraries. Separate each path with a colon (semicolon in Windows).                                                                                                                     |
| org.openhab.automation.jrubyscripting:local_context   | singlethread                            | The local context holds Ruby runtime, name-value pairs for sharing variables between Java and Ruby. See [this](https://github.com/jruby/jruby/wiki/RedBridge#Context_Instance_Type) for options and details |
| org.openhab.automation.jrubyscripting:local_variables | transient                               | Defines how variables are shared between Ruby and Java. See [this](https://github.com/jruby/jruby/wiki/RedBridge#local-variable-behavior-options) for options and details                                   |
| org.openhab.automation.jrubyscripting:gems            |                                         | A comma separated list of [Ruby Gems](https://rubygems.org/) to install.                                                                                                                                    |
| org.openhab.automation.jrubyscripting:require         |                                         | A comma separated list of script names to be required by the JRuby Scripting Engine at the beginning of user scripts.                                                                                       |
| org.openhab.automation.jrubyscripting:check_update    | true                                    | Check RubyGems for updates to the above gems when OpenHAB starts or JRuby settings are changed. Otherwise it will try to fulfil the requirements with locally installed gems, and you can manage them yourself with an external Ruby by setting the same GEM_HOME. |

## Ruby Gems

This automation add-on will install user specified gems and make them available on the library search path.
Gem versions may be specified using the standard ruby gem_name=version format.
The version number follows the [pessimistic version constraint](https://guides.rubygems.org/patterns/#pessimistic-version-constraint) syntax.

For example this configuration will install the latest version of the [openHAB JRuby Scripting Library](https://boc-tothefuture.github.io/openhab-jruby/), and instruct the scripting engine to automatically insert `require 'openhab'` at the start of the script. 

```text
org.openhab.automation.jrubyscripting:gems=openhab-scripting
org.openhab.automation.jrubyscripting:require=openhab
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

- The `File` variable, referencing `java.io.File` is not available as it conflicts with Ruby's File class preventing Ruby from initializing
- Globals `scriptExtension`, `automationManager`, `ruleRegistry`, `items`, `voice`, `rules`, `things`, `events`, `itemRegistry`, `ir`, `actions`, `se`, `audio`, `lifecycleTracker` are prepended with a `$` (e.g. `$automationManager`) making them available as global objects in Ruby.

## Script Examples

JRuby scripts provide access to almost all the functionality in an openHAB runtime environment.
As a simple example, the following script logs "Hello, World!".
Note that `puts` will usually not work since the output has no terminal to display the text.
The openHAB server uses the [SLF4J](https://www.slf4j.org/) library for logging.

```ruby
require 'java'
java_import org.slf4j.LoggerFactory

LoggerFactory.getLogger("org.openhab.automation.examples").info("Hello world!")
```

JRuby can [import Java classes](https://github.com/jruby/jruby/wiki/CallingJavaFromJRuby).
Depending on the openHAB logging configuration, you may need to prefix logger names with `org.openhab.automation` for them to show up in the log file (or you modify the logging configuration).

**Note**: Installing the [JRuby Scripting Library](https://boc-tothefuture.github.io/openhab-jruby/installation/) will provide enhanced capabilities with simpler rule syntax.

## Transformations

This add-on also provides the necessary infrastructure to use Ruby for writing [transformations](https://www.openhab.org/docs/configuration/transformations.html).
Once the addon is installed, you can create a Ruby file in the `$OPENHAB_CONF/transform` directory, with the extension `.script`.
It's important that the extension is `.script` so that the core `SCRIPT` transform service will recognize it.
When referencing the file, you need to specify the `SCRIPT` transform, with `rb` as the script type: `SCRIPT(rb:mytransform.script):%s`.
You can also specify additional variables to be set in the script using a URI-like query syntax: `SCRIPT(rb:mytransform.script?a=1b=c):%s` in order to share a single script with slightly different parameters for different items.

**Note**: Due to an [issue](https://github.com/jruby/jruby/issues/5876) in the current version of JRuby, you will need to begin your script with `input ||= nil` (and `a ||= nil` etc. for additional query variables) so that JRuby will recognize the variables as variables--rather than method calls--when it's parsing the script.
Otherwise you will get errors like `(NameError) undefined local variable or method 'input' for main:Object`.

### Example Transformation

#### compass.script

```ruby
input ||= nil
DIRECTIONS = %w[N NE E SE S SW W NW N].freeze

if input.nil? || input == "NULL" || input == "UNDEF"
  "-"
else
  cardinal = DIRECTIONS[(input.to_f / 45).round]
  "#{cardinal} (#{input.to_i}°)"
end
```

#### weather.items
```Xtend
Number:Angle Exterior_WindDirection "Wind Direction [SCRIPT(rb:compass.script):%s]" <wind>
```

Given a state of `82 °`, this will produce a formatted state of `E (82°)`.
