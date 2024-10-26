# openHAB Java223 Scripting

Write openHAB scripts in Java as a JSR223 language.

Features :

- full JSR 223 support (use in files, in GUI, transformations, inline rule action, etc...)
- auto-injection of openHAB variable/preset for simplicity
- use all OSGi services from the openHAB runtime
- library support for sharing code (.jar and .java)
- rule annotations are available in the helper library for easy rule creation
- helper library files: auto generation for items, things, and actions, with strong typing and ease of use
- no boilerplate code for simple scripts: you can do a one-liner script (declaring a class and a method is optional).
- optional reuse of instances script to share values between execution occurrences
- designed to be easily used with your favorite IDE

It makes heavy use of Eric Obermühlner's Java JSR 223 ScriptEngine [java-scriptengine](https://github.com/eobermuhlner/java-scriptengine),
and is partially based on work from other openHAB contributors who created their own JSR 223 Java automation bundle (many thanks to them).

# What can you do?

All JSR223 openHAB related things, and surely a bit more, thanks to your scripts sharing the same JVM as openHAB.
If you just want to see how to use it, see the [Examples](#examples) section.

# How it works

You should first take a look at the [official documentation about openHAB JSR223 support](https://www.openhab.org/docs/configuration/jsr223.html).
That said, keep reading for useful insider information.

## Script location: where can I use Java223?

### First location option: embedded in the application

As a full-featured JSR223 automation bundle, you can use Java223 scripts everywhere in openHAB where JSR223 scripts are allowed.
Including, but not limited to:

- Creating `Scripts` or `Transformation` in the so-called GUI sections
- Inside a `Rule`, as an inline script action in the `Then` or the `Only If` section
- When linking a channel to an item, as a transformation `Profile` of type `Script Java`

Scripts defined like this or as part of transformations/profiles are **compiled once and can be executed many times**.

### Second location option: File script

A JSR223 script **file** is a script located in your configuration directory, under the `automation/jsr223` subdirectory.

At startup, or each time a file is created (or modified) in this directory, openHAB will handle it to the relevant JSR223 scripting language for **immediate** execution (using the extension as a discriminating value to choose the JSR223 implementation).
So in our case, every `.java` files will be handled by the Java223 automation bundle.

Note that openHAB asks to compile and execute a script in `automation/jsr223` only once: at creation or modification.
**Such a script is never re-executed.**

As a script can create and register rules during its one-time execution (by accessing and using the openHAB automation manager), **this 'file mode' is then especially useful for defining rules**.
And, when a script that created rules is deleted, the linked rules are also deleted, thanks to the way openHAB registers a rule (same for modification, as the associated rules are deleted and recreated).
See the [rules](#rules) section for more information on how to create a rule.

## Execution

A Java223 script does not need any dependency to be compiled and executed. It can just be a plain, simple Java class like this one:

```java
public class SimpleClass {
    public void main() {
        int sum = 2 + 2;
    }
}
```

(In fact, it can even be a simpler one-liner, see the [no boilerplate section](#noboilerplate))

When openHAB presents a Java script to the Java223 automation bundle, it searches for methods with name `main`, `eval`, `run`, `exec`, or any methods annotated with `@RunScript`, and then runs them (from here we will refer to those as the "runnable methods").
Files in `automation/jsr223` can perform their logic in the constructor and are not required to have a runnable method.
Returning a value is supported but optional. That's all you need for a basic script.

A note about the context: each script has its own context, its own ClassLoader.
It means that scripts are perfectly separated and cannot interact with, or even see, each other.
But there are dedicated features for this ([shared cache](#sharedcache) for sharing values, [library](#library) for sharing code).

If you are curious about another part of the inner working, take a look at the advanced [topic about instantiation](#instantiationinnerworking).

## openHAB variables injection

Of course, a script needs to communicate with openHAB to be useful.
We will call 'openHAB inputs' those objects, values, references, that openHAB can expose to your script.
For example, a reference to the item registry will allow a script to interact with items by checking their state or giving them command.

With this Java223 bundle, it is done by the way of automatic injection.
It means that you don't need to do anything special.
You only have to declare a variable in your script at a specific location, and the bundle will take care of injecting the corresponding 'openHAB input' value in it.
There are three input injection possibilities:

- as a field in your script (see [example](#fieldinjection))
- as a method parameter in your runnable methods (see [example](#parameterinjection))
- as a method parameter in the constructor of your script. (see [example](#constructorinjection))

The variable name is used to find the correct value to inject, so take care of your spelling (full reference in [official documentation about openHAB JSR223 support](https://www.openhab.org/docs/configuration/jsr223.html#scriptextension-objects-all-jsr223-languages) ), or inherit the [Java223Script helper class](#java223script) to directly have the right variable names.

If you want to have more control on the injection process, see the [advanced topic about injection](#injectioncontrol).

<a id="osgiinjection"></a>

### Inject (or get) OSGi services

You can also inject any OSGi services available in the openHAB runtime.
You only have to declare a variable with the type of the service you want, and the bundle will provide the implementation.

For instance, these two services are injected by default in the `Java223Script` helper class and so are directly available if you inherit it:

| Variable         | Description                                                                                                                    | Purpose                           |
|------------------|--------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|
| ruleManager      | [`org.openhab.core.automation.RuleManager`](https://www.openhab.org/javadoc/latest/org/openhab/core/automation/rulemanager)    | Run or disable/enable other rules |
| thingManager     | [`org.openhab.core.thing.ThingManager`](https://www.openhab.org/javadoc/latest/org/openhab/core/thing/thingmanager)            | Enable/Disable thing              |

You can also programmatically get any OSGi service available in the openHAB runtime by using the `getService` method of the `Java223Script` helper class.

See [example](#osgiinjectionexample)

<a id="rules"></a>

## Defining Rules

As a JSR223 openHAB language, you can define rules with the openHAB JSR223 DSL.
All necessary classes and instances (SimpleRule, TriggerBuilder, automationManager instance, etc.) are, of course, exposed natively with this bundle.
You can see an example of how to use it [here](https://www.openhab.org/docs/configuration/jsr223.html#example-rules-for-a-first-impression) (examples are written with other languages, but concepts and objects for Java223 are the same), or below in the [full-fledged example without helper library](#example-without-helper-library).

**However**, keep in mind that there is a much more convenient way to do this.
You can jump to the relevant section [here](#helperrules).
But the following sections also expose some prerequisites if you want to have a better comprehension before jumping in.

<a id="library"></a>

## Libraries

With libraries, you can:

- use external code distributed by third parties.
- or define your own reusable code, shared between your scripts.

So, a library is located under the `automation\lib\java` subdirectory, and is either:

- a .java file,
- a .jar archive containing several already compiled classes:
  - a third party jar
  - or a custom one containing your own classes

The Java223 bundle will monitor this directory and automatically adds everything inside to the compilation unit of your script (although, it's not applied retrospectively).
The script still has its dedicated ClassLoader, but inside this ClassLoader, all your library classes are also available.

Be careful: it also means that other scripts have their **own** library classes inside their **own** ClassLoader.
And so **you cannot share value between scripts this way**, even by using a static property inside a library class (use the [shared cache](#sharedcache) for this)

<a id="libraryinjection"></a>

### Auto-injection of libraries

Your personal library probably also needs to communicate with openHAB.
You can, of course, pass openHAB input references as a parameter to your library methods, or by a setter.
For example, if we imagine a library `MyLibrary` that need access to the items and things registries:

```java
...
    ItemRegistry ir; // <- auto-injected in your script
    ThingRegistry things; // <- auto-injected in your script
    public void main() {
        var myUsefulParameter1 = ...
        var myUsefulParameter2 = ...
        MyLibrary myLib = new MyLibrary();
        myLib.doSomethingInteresting(ir, things, myUsefulParameter1, myUsefulParameter2); // <- then pass 'ir' and 'things' for your library to use
    }
...
```

**BUT**, as you can see, it can be cumbersome and unnecessarily hard to read.
This Java223 bundle provides a much simpler way to do this: letting it instantiate your library and auto-inject all openHAB inputs values into them.
It works on fields or method/constructor parameters. See [example](#libraryautoinjection).
It can even work recursively: a lib can reference another lib, itself referencing some openHAB inputs, and all this will work out of the box.

Getting back to our example: As the library instantiation and injection with the items and things registries are taken care of, the same code can then become:

```java
    public void main(MyLibrary myLib) { // <- myLib will be instantiated by the bundle, and auto-injected with the openHAB input variables declared in it. (You can also use other injection methods)
        var myUsefulParameter1 = ...
        var myUsefulParameter2 = ...
        myLib.doSomethingInteresting(myUsefulParameter1, myUsefulParameter2); // <-- myLib already have been instantiated and injected with registries reference, and so we do not need to pass them here
    }
```

Tip: The Java223 automation bundle recognizes a library by its type, so you don't have to worry about respecting a naming convention for the variable.
Feel free to use anything.


## Helper library

The helper library is totally optional, but you should seriously consider using it, as it will make your code experience much more streamlined.
It consists of two parts: dynamically .java generated files, and a .jar file with some already compiled classes.

If you do not want to use the helper library, you can completely disable it by setting the `enableHelper` setting to `false`.
Nothing (code, lib) will be generated/copied in the lib directory.

### Java dynamically generated classes

The Java223 bundle generates some ready-to-use library classes in the `automation\lib\java` directory.
These classes are dynamic and contain detailed information about your openHAB setup.

You will get several Java files in the package `helper.generated` :

- Items.java: contains all your item names as static String and label as their Javadoc. Also contains methods to directly get the Item, cast to the right Class. (see [example](#itemsandthings))
- Things.java: contains all your Thing UID as static String, with label as their Javadoc. Also contains methods to directly get the Thing. (see [example](#itemsandthings))
- Actions.java: contains strongly typed, ready-to-use methods to get the actions available on your things. (see [example](#actions)) <a id="java223script"></a>. Actions.java is your entry point to get real Actions implementation available in subpackages.
- Java223Script.java: this abstract class is very handy. In fact, you may consider inheriting it in all your scripts. It already contains all openHAB inputs variables, as well as some other useful shortcuts.
- EnumStrings.java: this class is just a simple store for direct String representations of various enums used by openHAB. It is especially useful for Rule annotations configuration which requires static strings.

As these files are no more, no less, standard Java223 library files, you can use them as candidates for auto-injection in your script.
Be careful though, do not use the variable names `items`, `things`, or `actions`, as they are already reserved as openHAB input values for the ItemRegistry, ThingsRegistry, and ScriptThingActions respectively.
As a reference or example, in the Java223Script helper abstract class, the following variables are used for them:  `_items`, `_things`, `_actions`.

**Tip: all your scripts, *including libraries*, can extend the `Java223Script` class.
This way they will automatically obtain easy access to all openHAB inputs, to some shortcuts, etc.**

<a id="helperrules"></a>

### helper-lib.jar and rules

The Java223 bundle also copies in your `automation\lib\java` a pre-compiled jar with a set of library files inside.
This jar is also no more, no less, a standard library .jar file, and is an example of how powerful the openHAB JSR223 feature is.
It contains all you need to define Rules with the help of simple-to-use annotations.
The entry point is the `RuleAnnotationParser` class.
The `parse` method automatically scans your script, searching for annotated methods defining rules, and then creates and registers them.

Tip: But you don't have to care about the inner working and parsing.
The best way to use this functionality is to extend the `Java223Script`, as it already contains a call to the `parse` method in a `@RunScript` annotated method.

When combined with all the aforementioned helpers, see how easy it is to define a rule:

```java
import helper.generated.EnumStrings;
import helper.generated.Items;
import helper.generated.Java223Script;
import helper.rules.annotations.ItemStateUpdateTrigger;
import helper.rules.annotations.Rule;

public class MyRule extends Java223Script { // extending Java223Script make parsing rules automatic

    @Rule
    @ItemStateUpdateTrigger(itemName = Items.my_detector_item, state = EnumStrings.OnOffType.ON)
    public void myRule() {
        _items.my_bulb_item().send(ON);
    }
}
```

This rule above is triggered by an 'ON' state update of an item linked to a detector, and then lights a bulb:
**Here really shines the JSR223 for Java: no random 'magic' strings, full auto-completion from your IDE, strongly typed code and no misspelling mistake possible.**

You can also use automatic injection **in your rule method parameter**.
It is especially useful for having strongly typed parameter.
Take a look at this rule, triggered by two different detectors:

```java
import helper.generated.EnumStrings;
import helper.generated.Items;
import helper.generated.Java223Script;
import helper.rules.annotations.ItemStateUpdateTrigger;
import helper.rules.annotations.Rule;
import helper.rules.eventinfo.ItemStateUpdate;

public class MyRule extends Java223Script {

    @Rule(name = "detecting.people", description = "Detecting people and light")
    @ItemStateUpdateTrigger(itemName = Items.my_detector_item, state = EnumStrings.OnOffType.ON)
    @ItemStateUpdateTrigger(itemName = Items.my_otherdetector_item, state = EnumStrings.OnOffType.ON)
    public void myRule(ItemStateUpdate inputs) { // <-- HERE, strongly typed parameter
        _items.my_bulb_item().send(ON);
        logger.info("Movement detected at " + inputs.getItemName()); // inputs.getItemName() gives the triggering detector name
    }
}
```

Here, `ItemStateChange` is a class defined in the helper-lib.jar.
It is a plain POJO, you could have written it yourself.
As it is a Java223 library class like any others, it leverages the autoinjection feature: its fields are automatically injected with the corresponding parameter given by openHAB.
So, by using the right event object for your trigger, such as `ItemStateChange` in this example, you don't have to check the documentation to search for how the event parameter you need is named, and you won't miss the parameter because you misspelled it.
You should find in the package `helper.rules.eventinfo`, the other event objects matching the triggers of your rules.

Here are all functionalities of the helper-lib:

- Many different `@Trigger` classes. Check the `helper.rules.annotation` package for a list.
- You can add (multiple) `@Condition` to a Rule. It exposes a pre-condition for the rule to execute. Check the `helper.rules.annotation` package.
- `@Trigger`, `@Conditions`, `@Rule` have many parameters. Some parameters add functionality; others can overwrite default behavior (for example, instead of using the method name for the label of a rule, you can override it with a custom label). Javadoc is included in the jar.
- Pre-made event objects that you can use as a parameter in a rule are defined in the package `helper.rules.eventinfo`.
- If you want all the triggering event input parameters in a map for a rule, you can use the parameter `Map<String, ?> bindings` as a rule method parameter.
- You can set the `@Rule` annotation on a method, but also on many types of field containing code to execute, such as Function, Runnable... Take a look at the class `Java223Rule` for an exhaustive list of what is supported. You can even switch the value of the field containing code at runtime, thus making the code your rule execute even more dynamic.
- A `@Debounce` annotation can be used to debounce between rule execution. Several types are available, look at the Javadoc for more information.

## Sharing values

<a id="sharedcache"></a>

### Share value between scripts

To share value between different scripts, you can use the shared standard openHAB cache available in the `cache` preset. Auto-inject it with :

```java
    protected @InjectBinding(preset = "cache") ValueCache sharedCache;
```

This cache is accessible the same way a `Map<String, Object>` is.

Tip: it is automatically available to scripts inheriting the Java223Script helper class.

### Share value between executions

#### Share values in Rules

When you define rules in a `.java` script in `automation/jsr223`, openHAB asks the Java223 bundle to compile and run the script immediately (and by nature, only once).
The script is instantiated, and its methods are executed.
As part of this, rules are parsed and registered in the Rule Manager.
But, even if the script _creating them_ is executed only once, the rules inside define _actions_, and those actions will run many times: each time the corresponding trigger is fired.
These actions have, by nature, some context around them (as they are methods or fields, they belong to an instance).
And as such, their code can access fields of the script instance (which is the instance used to register them).
This means that you can share states between triggered executions of rules by using fields of the script used to create the rules.

#### Share in Scripts

On the opposite, when you define a `java` script (for example in the GUI), the script is compiled and waits for further (possibly many) executions.
Each new execution is taken care of by the Java223 bundle, which has then to choose between two behaviors:

- instantiate the script with a `new` operator each time openHAB asks to run it
- or reuse the same instance and then re-execute the relevant action (method, field) on it. In this case, you can share data or states between executions

To enable the second behavior, take a look at the [topic about instantiation](#instantiationinnerworking).

<a id="noboilerplate"></a>

## No boilerplate code

Sometimes, your 'real' (useful) code is very short, and you don't need complex logic, custom auto-injection, etc.
In this case, you can omit the 'boilerplate' code and directly write your 'useful' code.

Under the hood, the Java223 bundle will 'wrap' your code inside a class with nearly the same content as `Java223Script` (you can inspect `Java223Script` in the helper lib: a bunch of standard imports such as item state types, injection utility methods, the generated -if enabled- helper classes, and a main method wrapping your code).

For example, this one-line script is perfectly valid:

```java
    _items.myitem().send(ON); // let there be light
```

It will produce the following wrapper script:

```java
import org.openhab.core.library.items.*;
import org.openhab.core.library.types.*;
import static org.openhab.core.library.types.HSBType.*;
import static org.openhab.core.library.types.IncreaseDecreaseType.*;
[other imports...]

public class WrappedJavaScript extends Java223Script {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected @InjectBinding Map<String, Object> bindings;
    protected @InjectBinding ScriptExtensionManagerWrapper scriptExtension;
    protected @InjectBinding ScriptExtensionManagerWrapper se;
    protected @InjectBinding AudioManager audio;
    protected @InjectBinding ItemRegistry ir;
    protected @InjectBinding ItemRegistry itemRegistry;
    protected @InjectBinding LifecycleTracker lifecycleTracker;
    protected @InjectBinding Map<String, State> items;
    protected @InjectBinding RuleRegistry rules;
    protected @InjectBinding ScriptBusEvent events;
    protected @InjectBinding ScriptThingActions actions;
    protected @InjectBinding ThingRegistry things;
    protected @InjectBinding VoiceManager voice;
    protected static final IncreaseDecreaseType DECREASE = IncreaseDecreaseType.DECREASE;
    protected static final IncreaseDecreaseType INCREASE = IncreaseDecreaseType.INCREASE;
    protected static final NextPreviousType NEXT = NextPreviousType.NEXT;
    protected static final NextPreviousType PREVIOUS = NextPreviousType.PREVIOUS;
    protected static final OnOffType OFF = OnOffType.OFF;
    protected static final OnOffType ON = OnOffType.ON;
    protected static final OpenClosedType CLOSED = OpenClosedType.CLOSED;
    protected static final OpenClosedType OPEN = OpenClosedType.OPEN;
    protected static final PlayPauseType PAUSE = PlayPauseType.PAUSE;
    protected static final PlayPauseType PLAY = PlayPauseType.PLAY;
    protected static final RefreshType REFRESH = RefreshType.REFRESH;
    protected static final RewindFastforwardType FASTFORWARD = RewindFastforwardType.FASTFORWARD;
    protected static final RewindFastforwardType REWIND = RewindFastforwardType.REWIND;
    protected static final StopMoveType MOVE = StopMoveType.MOVE;
    protected static final StopMoveType STOP = StopMoveType.STOP;
    protected static final UnDefType NULL = UnDefType.NULL;
    protected static final UnDefType UNDEF = UnDefType.UNDEF;
    protected static final UpDownType DOWN = UpDownType.DOWN;
    protected static final UpDownType UP = UpDownType.UP;

    public Object main() {
        _items.myitem().send(ON); // let there be light
        return null;
    }

    /**
     * Use this method to manually inject binding value in an object of your choice.
     */
    public void injectBindings(Object objectToInjectInto) {
        BindingInjector.injectBindingsInto(this.getClass().getClassLoader(), bindings, objectToInjectInto);
    }

    /**
     * Use this method to instantiate one of your libraries with all binding values injected if you can't use auto-injection.
     * This can be especially useful for one-liner script because they can't declare a library as a class member or method parameter.
     * @param clazz
     * @return The instantiated class
     */
    public <T> T createAndInjectBindings(Class<T> clazz) {
        return BindingInjector.getOrInstantiateObject(this.getClass().getClassLoader(), bindings, clazz);
    }
}
```

If you enable the helper-lib, you will also have access to the following fields:

```java
    protected @InjectBinding helper.generated.Items _items;
    protected @InjectBinding helper.generated.Actions _actions;
    protected @InjectBinding helper.generated.Things _things;
```

This 'wrapping' will take place if nowhere in your code a trimmed line starts with `public class`.

If you need to import some class, you can also do it. The import statements (lines starting with `import `) will be parsed and moved at the beginning of the resulting wrapper script, before the wrapping class and method.

You can return a value.
A line returning the value MUST begin with `return `, in order for the wrapping method to NOT insert a `return null` statement at the end.
This is useful for Transformation.

**Note that**, because your code is wrapped, the following functionalities are not available:

- definition of methods (your code is already inside one)
- customize the auto-injection (because class field members or method parameters happen outside the area your code is inserted).
- You have to rely on what is already injected by the wrapped script.
- instance reuse, while still happening if set to true, is nearly useless (as you cannot declare fields in the wrapper class, you so cannot use them to share data)

## Transformation

You can use a Java223 script in transformations.
A transformation is a piece of code with an input and an output.
To do so, you only have to respect this contract:

- you use the openHAB input value named 'input'. It is declared as a `String`.
- your runnable method must return a value

Example of transformation appending the word "Hello" to the input, using the "no boilerplate" functionality:

```java
return "Hello " + input;
```

# Use your IDE

## convenience-dependencies.jar

A jar file, purely for convenience, is exported, at startup, from the openHAB runtime and added to the lib directory.
This jar is EXCLUDED from entering the compilation unit of your script; its sole purpose is for you to use inside an IDE.
It contains most of the openHAB classes you probably need to write and compile scripts and rules.
By using this jar in your project, you probably won't have to set up advanced dependency management tools such as Maven or Gradle.

You can ask the Java223 bundle to add to this exported jar some classes by using the following Java223 configuration properties:

- `additionalBundles`: Additional package name exposed by bundles running in openHAB. Use ',' as a separator.
- `additionalClasses`: Additional individual classes. Use ',' as a separator.

## Configure your project

To use an IDE and write code with autocompletion, Javadoc, and all other syntax sugar, you only have to add to your project :

- **As a source directory**: the root directory of your scripts, under `automation/jsr223` (probably `automation/jsr223/java`, but you can use what you want)
- **As a source directory**: the root directory reserved as the Java223 library location `automation/lib/java`
- **As a library**: `automation/lib/java/helper-lib.jar`
- **As a library**: `automation/lib/java/convenience-dependencies.jar`

Tip: to access a remote openHAB installation scripts folder, you can copy, use Webdav, a Samba share, a SFTP virtual file system or sync feature (available on your OS or included in your IDE), or any other mean you can think about.

<a id="examples"></a>

# Configuration parameters

| Parameter Name                | Type    | Default | Label                          | Description                                                                                                                                                                                                 |
|-------------------------------|---------|---------|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `allowInstanceReuse`          | boolean | false   | Allow Script Instance Reuse    | Reuse an instance if found in the cache. Allow sharing data between subsequent executions.                                                                                                                  |
| `enableHelper`                | boolean | true    | Enable helper generation       | Enable code generation, and copying helper and convenience libraries                                                                                                                                        |
| `additionalBundles`           | text    | -       | Additional Bundles             | Additional bundles inserted in convenience-dependencies.jar, concatenated by ",".                                                                                                                           |
| `additionalClasses`           | text    | -       | Additional Classes             | Additional classes inserted in convenience-dependencies.jar, concatenated by ",".                                                                                                                           |
| `stabilityGenerationWaitTime` | integer | 10000   | Stability Generation Wait Time | Delay (in ms) before writing generated classes. Each new generation triggering event further delays the generation. Useful to prevent multiple code generations when many Things activate at the same time. |
| `startupGuardTime`            | integer | 60000   | Startup Guard Time             | Delay (in ms) before overwriting previously generated classes, at startup. Useful to not replace files from previous openHAB run with incomplete generation from a not fully loaded system.                 |

# Advanced topics

<a id="instantiationinnerworking"></a>

### Advanced topic: inner working, and about instantiation

To execute a script, the Java223 automation bundle (in conjunction with openHAB) has to instantiate the script class.
The inner working is like this:

- it receives a .java script
- the bundle compiles it (if not already done). openHAB will then store the compilation unit for further (and fastest) reuse.
- then, when execution is needed and asked by openHAB:
    - The bundle needs an instance:
        - The engine will choose a constructor and instantiate the script with the `new` operator.
          Auto-injection of openHAB values may occur in constructor parameters.
        - **OR**
        - The engine will reuse an existing instance
    - the engine will then execute the relevant script methods (`main`, etc., and any `@RunScript` annotated methods) on the instance. Inherited methods/fields are also included. Auto-injection will occur on fields and method parameters.

What is the right method for your use case?
As you can imagine, instance reuse is handy if you have costly operations to run once, for example, when dealing with network connections or with an embedded SQLite database.
You can also use this to share value or state between successive executions of your script.
BUT be careful, as stated above, injection on **constructor parameters** will only happen when the instance is first created.
So **do not choose** instance reuse if you want to use injection of circumstantial value to happen (such as 'input' or 'event') on constructor parameters.
If you don't choose instance reuse, you won't have any side effect, but you lose the possibility of sharing data and the performance boost gained (which is only meaningful if your constructor is heavy).

To choose between these two instantiation possibilities, you can set the default behavior with the global option `allowInstanceReuse`.
If set to false (default), the engine will re-instantiate the script each time it is asked by openHAB to run.
If set to true, the engine behavior will be to reuse the script instance between executions.

You can also overwrite this default behavior for individual scripts by using the `@ReuseScriptInstance` annotation on the class level.

Take note that the reuse functionality depends on the script compilation cache, managed by openHAB.
So instance reuse is not possible if something triggers a recompilation of your script (for example, a dependency change will always trigger a recompilation).

<a id="injectioncontrol"></a>

### Advanced topic: injection control

You can control the injection further (i.e. overriding default behavior, or directly injecting something from a preset) with the @InjectBinding annotation. See [example](#injectbinding).

| InjectBinding parameter | Description                                                                                                                                                                                                                                                                                                                                                  |
|-------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| disable                 | If true, will prevent injection. Useful if you don't want an useless parsing of your class field. Also useful when using field typed to classes from external third party jar not made for this.                                                                                                                                                             |
| named                   | Use this to specify the key used to get the value. If not specified, the field name will be used, or the type definition for libraries or OSGi services. You can also use a special 'path traversal' syntaxic sugar for getting field inside field. Example 'event.itemName' will get the event object, and use Java reflection to get its field 'itemName'. |
| preset                  | Use this field to inject value from a specific preset. Some presets may be difficult to work with. You can use tricks nonetheless (see [example](#presetadvanceduse)).                                                                                                                                                                                       |
| mandatory               | If set to true (which is the default when adding @InjectBinding) and the variable is not found, then the injection will fail, you will see an error log, and the script won't execute. You should use when you want to "fail fast".                                                                                                                          |
| recursive               | For library only. If set to true (default), the library injected will be parsed and its fields also injected. And so on, etc.                                                                                                                                                                                                                                |

Take note that this annotation is **NOT** mandatory for injection to happen. Injection **WILL** happen if **ONE** of the following conditions is respected:

- The variable name is a valid openHAB input
- The variable type is a library class (see [library](#libraryinjection) for more information)
- The variable type is an OSGi service running in the openHAB runtime (see [osgi injection](#osgiinjection))
- The variable name doesn't point to a valid openHAB input, BUT the combination of the 'named' (optional) and 'preset' parameter of the @InjectBinding annotation points to a valid openHAB input

A note about the 'mandatory' parameter: if you read carefully, you already did understand that a declaration like this:

```java
String input;
```

Is totally equivalent to this:

```java
@InjectBinding(mandatory = false) input;
```

And adding a plain, empty, `@InjectBinding` annotation with no parameter is equivalent to adding a mandatory aspect to the injection.

```java
@InjectBinding input;
```

### Advanced topic: Concurrency

Regarding concurrency, openHAB has several peculiarities about the way rules are executed.
If you think your code is at risk when being executed multiple times at the same moment, then you should read the main openHAB documentation about rules carefully.

Also, for rules defined inside a `.java` script in `automation/jsr223`, remember that the 'Action' part of each rule (i.e., the code doing something) are only lambda-like pieces of code running on a Java instance.
As openHAB does not manage the instance, there is no protection against concurrent executions of different rules.
The openHAB policy preventing a rule from running twice at the same time applies to one rule, so several rules defined on the same script file will share state and can access the script instance concurrently.
If different rules -triggered at the same time- access the same instance fields, you may have to synchronize read/write from/to your data.
If this is a concern for you, and if you are new to Java, look online for some good tutorials about concurrency, thread safety, and locks.



# Examples

## Lightning a bulb

```java
import helper.generated.Java223Script;

public class BasicExample extends Java223Script {
    public void main() {
        _items.myitem().send(ON); // let there be light
    }
}
```

Note the use of the `_items` field, inherited from the `Java223Script` class.
It is an instance of the generated class containing all the items defined in your system.

<a id="simplerule"></a>


## No boilerplate code

A one-liner also works:

```java
    _items.myitem().send(ON); // let there be light
```


## Create a simple rule

This rule is triggered only by an 'ON' state update of an item linked to a detector, and then lights a bulb.

```java
import helper.generated.EnumStrings;
import helper.generated.Items;
import helper.generated.Java223Script;

public class MyRule extends Java223Script {

    @Rule
    @ItemStateUpdateTrigger(itemName = Items.my_detector_item, state = EnumStrings.OnOffType.ON)
    public void myRule() {
        _items.my_bulb_item().send(OnOffType.ON);
    }
}
```

<a id="example-without-helper-library"></a>
## Full-fledged example without helper library

This example shows how to use JSR223 from openHAB without using the code generated by Java223.
It installs a rule waiting for item `r` to change, and sends a message over the MQTT broker `b`.
Put in openhab/automation/jsr223/M.java:

```java
import java.util.*;
import org.openhab.automation.java223.common.InjectBinding;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.module.script.defaultscope.ScriptThingActions;
import org.openhab.core.automation.module.script.rulesupport.shared.ScriptedAutomationManager;
import org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleRule;
import org.openhab.core.automation.util.TriggerBuilder;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.binding.ThingActions;
import org.slf4j.LoggerFactory;

public class M {
    org.slf4j.Logger logger = LoggerFactory.getLogger("logger1");
    @InjectBinding(preset = "RuleSupport") ScriptedAutomationManager automationManager;
    ItemRegistry ir;
    ScriptThingActions actions;

    public Object main() {
        automationManager.addRule(new SimpleRule() {
            {
                name = "abc"; //If not set, openHAB will assign random name
                description = "When description is not set, openHAB will assign empty string as description";
                uid = "1-2-3-uid"; //If not set, openHAB will assign random UID
                triggers = List.of(TriggerBuilder.create().withId("trig1").withTypeUID("core.ItemStateChangeTrigger").withConfiguration(new Configuration(Map.of("itemName", "r"))).build());
            }

            @Override public Object execute(Action module, Map<String, ?> inputs) {
                logger.warn("execute got inputs: " + inputs.toString());
                // Without M.this actions refers to the protected field with the same name in class SimpleRule
                ThingActions mqtt_broker = M.this.actions.get("mqtt", "mqtt:broker:b");
                try {
                    java.lang.reflect.Method mqtt_broker_publish = mqtt_broker.getClass().getMethod("publishMQTT", String.class, String.class);
                    mqtt_broker_publish.invoke(mqtt_broker, "topic1/a", "value123");
                } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {}
                return null;
            }
        });

        try {
            logger.warn("r’s value from ItemRegistry is " + ir.getItem("r").getStateAs(DecimalType.class));
        } catch (org.openhab.core.items.ItemNotFoundException e) {
            logger.debug("Item not found " + e.toString());
        }
        logger.warn("Loading of M.java completed");
        return null;
    }
}
```

## UI Based Rules in full-text format ('code' tab)

When creating a rule over MainUI and only the action is written in Java, the inputs from the trigger are available over the `bindings` variable:

```yaml
triggers:
  - id: "1"
    configuration:
      itemName: r
    type: core.ItemStateChangeTrigger
actions:
  - inputs: {}
    id: "2"
    configuration:
      type: java
      script: >-
        public class anything {
            org.openhab.core.thing.ThingRegistry things; // from the "default" preset
            public Object main(java.util.Map<String, Object> bindings) { // use bindings to get access to all variables
                org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("Bindings");
                logger.error("Is things the same as bindings('things')? " + Boolean.valueOf(things == bindings.get("things"))); // true
                logger.error("lastStateUpdate as reported by the trigger: " + bindings.get("lastStateUpdate") + " is of " + bindings.get("lastStateUpdate").getClass());
                for (var e : bindings.entrySet()) // print all useful and not so useful injected context
                    logger.error(e.getKey() + " -> " + e.getValue());
                return null;
            }
        }
    type: script.ScriptAction
```

## Create a rule with several triggers and options

This time, the rule is triggered by an 'ON' state update on one of two possible detectors.
The method parameter is a strongly typed library element (`ItemStateChange`) and as such, its fields are auto-injected with the right value from the input.
Thanks to this, it is straightforward to get the input parameters without risking using a wrong parameter name.
For example, we get here the name of the item triggering the detection, for a detailed log.
Additionally, instead of the default (the method name used for the label of the rule), it has a description, and a dedicated name for the label, and both will be shown on the openHAB GUI.

```java
import org.openhab.core.library.types.OnOffType;

import helper.generated.EnumStrings;
import helper.generated.Items;
import helper.generated.Java223Script;
import helper.rules.annotations.ItemStateUpdateTrigger;
import helper.rules.annotations.Rule;
import helper.rules.eventinfo.ItemStateUpdate;

public class MyRule extends Java223Script {

    @Rule(name = "detecting.people", description = "Detecting people and light")
    @ItemStateUpdateTrigger(itemName = Items.my_detector_item, state = EnumStrings.OnOffType.ON)
    @ItemStateUpdateTrigger(itemName = Items.my_otherdetector_item, state = EnumStrings.OnOffType.ON)
    public void myRule(ItemStateUpdate inputs) { // here, a strongly typed parameter
        _items.my_bulb_item().send(ON);
        logger.info("Movement detected at {}", inputs.getItemName());
    }
}
```

## Example of different openHAB input variables injection types

<a id="fieldinjection"></a>

If you don't want to extend the `Java223Script` class, then you will have to take care of formatting your script for injection of openHAB input value.

### Field input injection

```java
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;

public class FieldInjectionExample {
    ItemRegistry itemRegistry; // <-- the injection will happen here, 'itemRegistry' is a valid openHAB input name
    public void main() {
        ((SwitchItem)itemRegistry).get("myitem").send(OnOffType.ON);
    }
}
```

<a id="parameterinjection"></a>

### Method parameter input injection

```java
import org.openhab.core.items.ItemRegistry;
import static org.openhab.core.library.types.OnOffType.ON;

public class MethodInjectionExample {
    public void main(ItemRegistry itemRegistry) {  // <-- the injection will happen here, 'itemRegistry' is a valid openHAB input name
        try {
            ((org.openhab.core.library.items.SwitchItem)itemRegistry.getItem("myitem")).send(ON);
        } catch (org.openhab.core.items.ItemNotFoundException e) {}
    }
}
```

<a id="constructorinjection"></a>

### Constructor parameter input injection

```java
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;

public class ConstructorInjectionExample {
    // The injection WON'T happen here because the variable name is not the name of an available openHAB input.
    // Alternatively, by using @InjectBinding(disable=true) you can prevent the Java223 bundle from even trying to inject an openHAB value.
    ItemRegistry myItemRegistry;

    public ConstructorInjectionExample(ItemRegistry itemRegistry) { // <-- the injection will happen here, 'itemRegistry' is a valid openHAB input name
       this.myItemRegistry = itemRegistry;
    }

    public void main() {
        ((SwitchItem)myItemRegistry.get("myitem")).send(OnOffType.ON);
    }
}
```

<a id="osgiinjectionexample"></a>

## Injection of OSGi services

You can inject any OSGi services into your scripts.
Useful examples are the RuleManager and the ThingManager, and they are already available if you extends the `Java223Script` class.

### Run another rule or script with the OSGi service RuleManager

You may want to run another rule or a script.
First, inject the ruleManager in your script, then use it with the `runNow` method and the `UID` of the rule.

```java
import java.util.Map;
import org.openhab.core.automation.RuleManager;

public class RunAnotherRule {
    public void main(RuleManager ruleManager) { // <-- Injection by the constructor. RuleManager is an OSGi service and so is available as a candidate for injection
        // simple execution :
        ruleManager.runNow("myruleid");
        // execution with parameters in a key / value map :
        // set the boolean parameter to true if you want to check conditions before execution (in case of a full rule)
        ruleManager.runNow("myparameterizedruleid", false, Map.of("key", "value"));
    }
}
```

### Disable a thing with the OSGi service ThingManager

As with the RuleManager, you first inject it (or use the inherited field from Java223Script), and then you can disable a thing with the `ThingManager` service.

```java
public class DisableThing extends helper.generated.Java223Script { //  <-- Java223Script already has a thingManager field
    public void main() {
        thingManager.setEnabled(_things.network_pingdevice_mything().getUID(), false);
    }
}
```

### Use the OSGi ServiceGetter

If you don't want to use injection, you can get an OSGi service programmatically by using the `getService` method in the `helper.generated.Java223Script` class.

```java
    ThingManager tm = getService(ThingManager.class);
```

Or you can get a special (non-openHAB JSR223 standard) service by querying the binding for the key `serviceGetter`, and then use it to get any OSGi service.

```java
import org.openhab.automation.java223.common.ServiceGetter;

ServiceGetter sGetter = ((ServiceGetter) bindings.get('serviceGetter'));
ThingManager tm = sGetter.getService(ThingManager.class);
```

<a id="injectbinding"></a>

## Advanced injection control

Control automatic injection behavior by using the `@InjectBinding` annotation.
You can use it on field or on method/constructor parameter.

```java
import org.openhab.core.automation.module.script.rulesupport.shared.ScriptedAutomationManager;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;
import helper.rules.annotations.InjectBinding;

public class InjectBindingExample {

    // inject something from a preset :
    protected @InjectBinding(preset = "RuleSupport") ScriptedAutomationManager automationManager;
    // disable injection even if the field name should trigger it :
    protected @InjectBinding(disable = true) ItemRegistry itemRegistry;
    // name your variable as you wish by using the 'named' parameter :
    protected @InjectBinding(named = "itemRegistry") ItemRegistry otherVariableName;
    // make it mandatory (the script will not run if the value cannot be found).
    protected @InjectBinding(mandatory = true) ThingRegistry things;

    public void main(ItemRegistry itemRegistry) {
        myItemRegistry.get("myitem");
    }
}
```

<a id="presetadvanceduse"></a>

## Preset advanced use and tricks

Sometimes presets can collide with each other. Or they can provide unavailable class implementations. 
For instance, `itemRegistry` is offered in two different forms, by both the `default` and `provider` presets. You can use those tricks:

- As the key is `itemRegistry` in both cases, you should take special care of your naming to avoid collision (use the `named` parameter)
- Class `ProviderItemRegistryDelegate` is the implementation provided by the preset `provider`. It provides an additional method to add an Item, `addPermanent`, but some dependency of this class isn't available at runtime (internal class). So your script can't compile if you use it directly. You can use the public interface and use Java reflection tricks to access the otherwise unavailable methods.

This example is of no particular real use, but it shows the possibilities.

```java
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.SwitchItem;
// import  org.openhab.core.automation.module.script.providersupport.shared.ProviderItemRegistryDelegate; <-- won't work because it refers to internal classes

public class PresetAdvancedUse {

    @InjectBinding ItemRegistry itemRegistry; // <-- from the default preset
    @InjectBinding(named="itemRegistry", preset="provider") ItemRegistry itemRegistryFromProvider; // <-- from the provider preset. We can only use the interface for the type declaration, not the ProviderItemRegistryDelegate type. 
    
    public void main() throws NoSuchMethodException, SecurityException, IllegalAccessException {
        // use Java reflection to access the unavailable method 'addPermanent(Item item)':
        itemRegistryFromProvider.getClass().getMethod("addPermanent", Item.class).invoke(itemRegistryFromProvider, new SwitchItem("SillyExample"));
    }
}
```


<a id="libraryautoinjection"></a>

## Library use and auto-injection

Inside the `automation/lib/java` directory, let's define a library that will be available to all scripts.

```java
import org.openhab.core.items.ItemRegistry;

public class MyGreatLibrary {
    ItemRegistry itemRegistry; // will be auto-injected if instantiation is taken care of by the bundle

    public void myUsefullLibraryMethod(String itemName) {
        itemRegistry.get(itemName);
        //something usefull...
    }
}
```

Here is how to use it, with auto-injection of your script (in automation/jsr223/).
For example, with field injection :

```java
public class MyScript {

    MyGreatLibrary mylib; // will be auto-instantiated and then auto-injected with all necessary openHAB input value

    public void exec() {
        mylib.myUsefullLibraryMethod("myitemName");
    }
}
```

<a id="itemsandthings"></a>

Tip: remember that all classes, including libraries, can extend Java223Script

## Items and Things helper libraries

By extending the Java223Script class (optional, you can inject them the way you want), the variables _items and _things are directly accessible and expose the helper-generated classes.

```java
import helper.generated.Java223Script;

public class ItemsAndThingAccessExample extends Java223Script { // <-- take the Java223Script class as a base class
                                                                // to access _items and _things more easily

    public void exec() {
        _items.myLightItem().send(ON); // <-- light on !
        logger.info(_things.zwave_device_2ecfa3a2_node68().getStatus().toString()); // <-- get thing info
    }
}
```

<a id="actions"></a>

## Actions helper libraries

With the auto generated `Actions` class (here referenced by the `_actions` variable), you can call a method to get strongly typed actions (and auto-completion) linked to your Thing.

```java
import helper.generated.Java223Script;
import helper.generated.Things;

public class ActionExample extends Java223Script { // <-- take the Java223Script class as a base class
                                                   // to access _actions more easily

    public void exec() {
        _actions.getSmsmodem_SMSModemActions(Things.mySMSthing).sendSMS("+3312345678", "Hello world");
    }
}
```
