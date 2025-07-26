/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.automation.jrubyscripting.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;
import java.util.stream.Stream;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.jrubyscripting.internal.watch.JRubyScriptFileWatcher;
import org.openhab.core.automation.module.script.ScriptEngineContainer;
import org.openhab.core.automation.module.script.ScriptEngineManager;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionRegistry;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JRubyConsoleCommandExtension} class
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class JRubyConsoleCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {
    private final Logger logger = LoggerFactory.getLogger(JRubyConsoleCommandExtension.class);

    private static final String DEFAULT_CONSOLE_PATH = "openhab/console/";
    private static final String OPENHAB_SCRIPTING_GEM = "gem \"openhab-scripting\", \"~> 5.0\"";

    private static final String INFO = "info";
    private static final String CONSOLE = "console";
    private static final String BUNDLE = "bundle";
    private static final String GEM = "gem";
    private static final String PRUNE = "prune";

    private static final List<String> SUB_COMMANDS = List.of(INFO, CONSOLE, BUNDLE, GEM, PRUNE);

    private final ScriptEngineManager scriptEngineManager;
    private final JRubyScriptEngineFactory jRubyScriptEngineFactory;
    private final JRubyScriptFileWatcher scriptFileWatcher;
    private final ConfigDescriptionRegistry configDescriptionRegistry;

    private final String scriptType;

    @Activate
    public JRubyConsoleCommandExtension( //
            @Reference ScriptEngineManager scriptEngineManager, //
            @Reference JRubyScriptEngineFactory jRubyScriptEngineFactory, //
            @Reference JRubyScriptFileWatcher scriptFileWatcher, //
            @Reference ConfigDescriptionRegistry configDescriptionRegistry) {
        super("jrubyscripting", "JRuby Scripting console utilities.");
        this.scriptEngineManager = scriptEngineManager;
        this.jRubyScriptEngineFactory = jRubyScriptEngineFactory;
        this.scriptFileWatcher = scriptFileWatcher;
        this.scriptType = jRubyScriptEngineFactory.getScriptTypes().getFirst();
        this.configDescriptionRegistry = configDescriptionRegistry;
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList( //
                buildCommandUsage(INFO, "displays information about JRuby Scripting add-on"), //
                buildCommandUsage(CONSOLE + " [--list|-l|--help|-h] | [script] [options]",
                        "starts an interactive JRuby console"), //
                buildCommandUsage(BUNDLE + " [arguments]", "runs Ruby bundler against your Gemfile"), //
                buildCommandUsage(GEM + " [arguments]", "manages JRuby Scripting add-on's RubyGems"), //
                buildCommandUsage(PRUNE + " [-f|--force]", "cleans up older versions in the .gem directory") //
        );
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        StringsCompleter completer = new StringsCompleter();
        SortedSet<String> strings = completer.getStrings();
        if (cursorArgumentIndex == 0) {
            strings.addAll(SUB_COMMANDS);
        } else if (cursorArgumentIndex == 1) {
            if (CONSOLE.equals(args[0])) {
                Map<String, String> consoles = (Map<String, String>) getConsoles();
                if (consoles != null) {
                    strings.addAll(consoles.keySet());
                }
            }
        }

        if (!strings.isEmpty()) {
            return completer.complete(args, cursorArgumentIndex, cursorPosition, candidates);
        }

        return false;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String command = args[0];
            switch (command) {
                case INFO:
                    info(console);
                    break;
                case CONSOLE:
                    startConsole(console, Arrays.copyOfRange(args, 1, args.length));
                    break;
                case BUNDLE:
                    bundler(console, Arrays.copyOfRange(args, 1, args.length));
                    break;
                case GEM:
                    gem(console, Arrays.copyOfRange(args, 1, args.length));
                    break;
                case PRUNE:
                    if (args.length > 1) {
                        if ("-f".equals(args[1]) || "--force".equals(args[1])) {
                            cleanupOtherGemHomes(console, true);
                        } else {
                            console.println("Use -f or --force to skip confirmation.");
                        }
                    } else {
                        cleanupOtherGemHomes(console, false);
                    }
                    break;
                case "--help":
                case "-h":
                    printUsage(console);
                    break;
                default:
                    console.println("Unknown command '" + command + "'");
                    printUsage(console);
                    break;
            }
        } else {
            printUsage(console);
        }
    }

    private void info(Console console) {
        File gemfile = jRubyScriptEngineFactory.getConfiguration().getGemfile();
        final String printVersionNumbers = """
                library_version = defined?(OpenHAB::DSL::VERSION) && OpenHAB::DSL::VERSION

                puts "JRuby #{JRUBY_VERSION}"
                puts "JRuby Scripting Library #{library_version || 'is not installed'}"
                puts "ENV['GEM_HOME']: #{ENV['GEM_HOME']}"
                puts "ENV['RUBYLIB']: #{ENV['RUBYLIB']}"
                    """ + (!gemfile.exists() ? "" : """
                puts "ENV['BUNDLE_GEMFILE']: #{ENV['BUNDLE_GEMFILE']}"
                    """);

        executeWithFullJRuby(console, engine -> engine.eval(printVersionNumbers));
        console.println("Script path: " + scriptFileWatcher.getWatchPath());
        console.println("");
        console.println("JRuby Scripting Add-on Configuration:");
        console.println("=====================================");

        ConfigDescription configDescription = configDescriptionRegistry
                .getConfigDescription(URI.create(JRubyScriptEngineFactory.CONFIG_DESCRIPTION_URI));

        if (configDescription == null) {
            console.println("No configuration found for JRuby Scripting add-on. This is probably a bug.");
            return;
        }

        List<ConfigDescriptionParameter> parameters = configDescription.getParameters();
        Map<String, String> config = jRubyScriptEngineFactory.getConfiguration().getConfigurations();
        // The JRubyScripting Add-on configuration doesn't have group-less parameters,
        // but in case they exist in the future, print them out
        configDescription.getParameters().forEach(parameter -> {
            if (parameter.getGroupName() == null) {
                console.println(parameter.getName() + ": " + config.get(parameter.getName()));
            }
        });
        configDescription.getParameterGroups().forEach(group -> {
            String groupLabel = group.getLabel();
            if (groupLabel == null) {
                groupLabel = group.getName();
            }
            console.println(groupLabel);
            parameters.forEach(parameter -> {
                if (!group.getName().equals(parameter.getGroupName())) {
                    return;
                }
                console.print("  " + parameter.getName() + ": ");
                String value = config.get(parameter.getName());
                if (value == null) {
                    console.println("not set");
                } else if (value.contains("\n")) {
                    console.println("  (multiline)");
                    console.println("    " + value.replace("\n", "\n    "));
                } else {
                    console.println(value);
                }
            });
            console.println("");
        });
    }

    @SuppressWarnings("unchecked")
    private @Nullable Map<String, String> getConsoles() {
        return (Map<String, String>) executeWithPlainJRuby(null, engine -> engine.eval(
                "require '" + DEFAULT_CONSOLE_PATH + "registry'; OpenHAB::Console::REGISTRY.transform_keys(&:to_s)"));
    }

    private void startConsole(Console console, String[] args) {
        final String defaultConsole = jRubyScriptEngineFactory.getConfiguration().getConsole();
        String script = defaultConsole;

        if (args.length > 0) {
            switch (args[0]) {
                case "--help":
                case "-h":
                    console.printUsage("jrubyscripting console [--list|-l|--help|-h] | [script] [options]");
                    console.println("");
                    console.println("  --list, -l: list available console scripts");
                    console.println("  --help, -h: show this help");
                    console.println("  script: name of the console script to run");
                    console.println("  options: arguments to pass to the console script");
                    console.println("");
                    console.printf("  If no script is specified, the default console script '%s' is used.\n",
                            defaultConsole);
                    console.println("  The default console script can be configured in the add-on configuration.");
                    console.println("");
                    return;
                case "--list":
                case "-l":
                    boolean defaultConsoleInRegistry = false;
                    Map<String, String> consoles = (Map<String, String>) getConsoles();
                    if (consoles.isEmpty()) {
                        console.println(
                                "The list of console scripts is not available. Please install/update the JRuby helper library gem.");
                    } else {
                        console.println("Available console scripts:");
                        for (Map.Entry<String, String> consoleScript : consoles.entrySet()) {
                            String name = consoleScript.getKey();
                            String description = consoleScript.getValue();
                            if ((DEFAULT_CONSOLE_PATH + name).equals(defaultConsole) || name.equals(defaultConsole)) {
                                description = description + " (default)";
                                defaultConsoleInRegistry = true;
                            }
                            console.println("  " + name + " - " + description);
                        }
                    }
                    if (!defaultConsoleInRegistry && !defaultConsole.isBlank()) {
                        console.println("Default console script: '" + defaultConsole + "'");
                    }
                    return;
                default:
                    if (!args[0].startsWith("-")) {
                        script = args[0];
                        args = Arrays.copyOfRange(args, 1, args.length);
                    }
            }
        }

        if (script == null || script.isBlank()) {
            console.println(
                    "No console script configured. Please set the 'console' property in the add-on configuration, "
                            + "or specify one as an argument to 'jrubyscripting console <scriptname>'.");
            return;
        }

        final String consoleScript = script.contains("/") ? script : DEFAULT_CONSOLE_PATH + script;
        final String[] argv = args;

        logger.debug("Starting JRuby console with script: {}", consoleScript);

        executeWithFullJRuby(console, engine -> {
            // Resolve console.getSession().getTerminal() in Ruby to avoid having to add
            // org.apache.karaf.shell.core as a dependency in pom.xml
            engine.put("$console", console);
            engine.put(ScriptEngine.ARGV, argv);
            engine.eval(String.format("require '%s'", consoleScript));

            return null;
        });
    }

    private synchronized void bundler(Console console, String[] args) {
        final File gemfile = jRubyScriptEngineFactory.getConfiguration().getGemfile();
        boolean bundleInit = args.length >= 1 && "init".equals(args[0]);

        if (bundleInit && gemfile.exists()) {
            console.printf("Gemfile '%s' already exists.\n", gemfile.toString());
            return;
        } else if (!bundleInit && !gemfile.exists()) {
            console.printf("""
                    No Gemfile found. Please ensure the 'bundle_gemfile' setting is correct and the Gemfile exists.

                    To create a new Gemfile '%s', run the command:
                      jrubyscripting bundle init

                    This will create a Gemfile that includes the openhab helper library in the current directory.
                    """, gemfile.toString());
            return;
        }

        final String bundler = """
                require "jruby"
                JRuby.runtime.instance_config.update_native_env_enabled = false

                require "bundler"
                require "bundler/friendly_errors"

                Bundler.with_friendly_errors do
                  require "bundler/cli"

                  # Allow any command to use --help flag to show help for that command
                  help_flags = %w[--help -h]
                  help_flag_used = ARGV.any? { |a| help_flags.include? a }
                  args = help_flag_used ? Bundler::CLI.reformatted_help_args(ARGV) : ARGV

                  Bundler::CLI.start(args, debug: true)
                end
                """;

        // We need to set user.dir because bundle init creates the Gemfile there
        // and ignores BUNDLE_GEMFILE environment variable
        String gemfileDir = gemfile.getParent();
        if (gemfileDir == null) {
            console.println("Error: Unable to determine Gemfile directory.");
            console.println("Please check the 'bundle_gemfile' setting in the add-on configuration.");
            console.println("Current setting: " + gemfile.toString());
            return;
        }
        String originalDir = System.setProperty("user.dir", gemfileDir);
        try {
            Object result = executeWithPlainJRuby(console, engine -> {
                engine.put(ScriptEngine.ARGV, args);
                return engine.eval(bundler);
            });
            logger.debug("Bundler result: {}", result);
            // A null result indicates a successful creation of Gemfile.
            // Otherwise, if a Gemfile already exists, it will return `1`
            if (bundleInit && result == null) {
                try {
                    // bundler init always creates a file called "Gemfile".
                    // if our setting points to any file other than "Gemfile",
                    // we need to rename the new Gemfile to it
                    Path newGemfile = Path.of(gemfileDir, "Gemfile");
                    Path gemfilePath = gemfile.toPath();
                    if (!newGemfile.equals(gemfilePath)) {
                        Files.move(newGemfile, gemfilePath);
                        console.printf("Renamed %s to %s\n", newGemfile.toString(), gemfilePath.toString());
                    }
                    if (gemfile.exists()) {
                        insertHelperLibraryGem(console, gemfile.toPath());
                    } else {
                        console.println("Gemfile creation failed.");
                    }
                } catch (IOException e) {
                    console.println("Error: " + e.getMessage());
                    return;
                }
            }
        } finally {
            if (originalDir == null) {
                System.clearProperty("user.dir");
            } else {
                System.setProperty("user.dir", originalDir);
            }
        }
    }

    private void insertHelperLibraryGem(Console console, Path gemfilePath) throws IOException {
        List<String> originalGemfile = Files.readAllLines(gemfilePath);
        // The Gemfile generated by bundler init contains `# gem "rails"` -> remove it
        // and add the openHAB scripting gem
        List<String> outputGemfile = Stream.concat( //
                originalGemfile.stream().filter(line -> !line.trim().startsWith("# gem ")), //
                Stream.of(OPENHAB_SCRIPTING_GEM) //
        ).toList();
        Files.write(gemfilePath, outputGemfile);
    }

    private synchronized void gem(Console console, String[] args) {
        final String GEM = """
                require "rubygems/gem_runner"
                Gem::GemRunner.new.run ARGV
                    """;

        executeWithPlainJRuby(console, engine -> {
            engine.put(ScriptEngine.ARGV, args);
            engine.eval(GEM);
            return null;
        });
    }

    /*
     * Deletes all other gem homes except the one that is currently in use.
     * This is to prevent the accumulation of old gem homes that are no longer needed.
     * The user is prompted to confirm the deletion when force is false.
     */
    private void cleanupOtherGemHomes(Console console, boolean force) {
        Path gemHomeBase = Path.of(jRubyScriptEngineFactory.getConfiguration().getGemHomeBase());
        Path specificGemHome = Path.of(jRubyScriptEngineFactory.getConfiguration().getSpecificGemHome());

        if (gemHomeBase.equals(specificGemHome)) {
            console.println("Pruning is not necessary because the gem home directory is not versioned.");
            return;
        }

        // Cowardly refuse to prune the gem home if it is not in a standard path
        // This is to prevent accidental deletion of the entire filesystem
        // or other important directories
        if (!gemHomeBase.endsWith(".gem")) {
            console.println("The gem home directory is not located in a standard path. Please prune it manually.");
            console.println("  " + gemHomeBase);
            return;
        }

        try (Stream<Path> paths = Files.walk(gemHomeBase)) {
            Iterator<File> filesToDelete = paths.filter(p -> !p.equals(gemHomeBase) && !p.startsWith(specificGemHome)) //
                    .sorted(Comparator.reverseOrder()) //
                    .map(Path::toFile).iterator();

            if (!filesToDelete.hasNext()) {
                console.println("No files or directories to delete from " + gemHomeBase);
                return;
            }

            if (!force) {
                console.printf("Some files and directories exist in '%s' outside of your configured gem home '%s'.\n",
                        gemHomeBase, specificGemHome);
                console.println("They may have been left over from previous installations or updates.\n");

                Files.list(gemHomeBase) //
                        .filter(p -> !p.equals(specificGemHome)) //
                        .sorted() //
                        .map(Path::toFile) //
                        .forEach(file -> console.println("  " + file + (file.isDirectory() ? "/" : "")));

                try {
                    console.readLine("\nPress Enter to delete them or Ctrl+C to cancel.", null);
                    console.println("");
                } catch (RuntimeException e) {
                    // Ctrl+C was pressed
                    // We can't use a more specific exception type without adding org.jline as bundle dependency
                    console.println("Operation cancelled.");
                    return;
                }
            }

            while (filesToDelete.hasNext()) {
                File file = filesToDelete.next();
                console.println("Deleting: " + file);
                file.delete();
                logger.info("Deleted: {}", file);
            }
        } catch (IOException e) {
            console.println("Error: " + e.getMessage());
            return;
        }
    }

    private void printLoadingMessage(Console console, boolean show) {
        String loadingMessage = "Loading JRuby script engine...";
        if (show) {
            console.print(loadingMessage);
        } else {
            // Clear the loading message
            console.print("\r" + " ".repeat(loadingMessage.length()) + "\r");
        }
    }

    /*
     * Create a full openHAB-managed JRuby engine with openHAB scoped variables
     * including any injected required gems.
     * 
     * This will run the script with the helper library if configured.
     */
    private @Nullable Object executeWithFullJRuby(Console console, EngineEvalFunction process) {
        final String scriptIdentifier = "jruby-console-" + UUID.randomUUID().toString();

        printLoadingMessage(console, true);
        ScriptEngineContainer container = scriptEngineManager.createScriptEngine(scriptType, scriptIdentifier);
        if (container == null) {
            console.println("Error: Unable to create JRuby script engine.");
            return null;
        }
        ScriptEngine engine = container.getScriptEngine();
        try {
            printLoadingMessage(console, false);
            return process.apply(engine);
        } catch (ScriptException e) {
            console.println("Error: " + e.getMessage());
            return null;
        } finally {
            scriptEngineManager.removeEngine(scriptIdentifier);
        }
    }

    /*
     * Create a plain JRuby script engine without loading the helper library.
     */
    private @Nullable Object executeWithPlainJRuby(@Nullable Console console, EngineEvalFunction process) {
        ScriptEngine engine = jRubyScriptEngineFactory.createScriptEngine(scriptType);
        try {
            if (engine == null) {
                throw new ScriptException("Unable to create JRuby script engine.");
            }
            return process.apply(engine);
        } catch (ScriptException e) {
            if (console != null) {
                console.println("Error: " + e.getMessage());
            } else {
                logger.warn("Error: {}", e.getMessage());
            }
            return null;
        }
    }

    @FunctionalInterface
    public interface EngineEvalFunction {
        @Nullable
        Object apply(ScriptEngine e) throws ScriptException;
    }
}
