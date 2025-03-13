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
import java.io.PrintStream;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.felix.service.command.Process;
import org.apache.karaf.shell.api.console.Command;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Parser;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.api.console.SessionFactory;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.jrubyscripting.internal.watch.JRubyScriptFileWatcher;
import org.openhab.core.automation.module.script.ScriptEngineManager;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionRegistry;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.StringsCompleter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JRubyConsoleCommandExtension} class
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@Component(immediate = true)
// @NonNullByDefault cannot be applied to classes that implement Command
// This is implemented directly using the Command interface in order to access Karaf's Session
public class JRubyConsoleCommandExtension implements Command, Completer {
    private final Logger logger = LoggerFactory.getLogger(JRubyConsoleCommandExtension.class);

    private final String DEFAULT_CONSOLE_PATH = "openhab/console/";

    private static final String INFO = "info";
    private static final String CONSOLE = "console";
    private static final String BUNDLE = "bundle";
    private static final String GEM = "gem";
    private static final String UPDATE = "update";
    private static final String PRUNE = "prune";

    private static final List<String> SUB_COMMANDS = List.of(INFO, CONSOLE, BUNDLE, GEM, UPDATE, PRUNE);

    private final ScriptEngineManager scriptEngineManager;
    private final JRubyScriptEngineFactory jRubyScriptEngineFactory;
    private final JRubyScriptFileWatcher scriptFileWatcher;
    private final ConfigDescriptionRegistry configDescriptionRegistry;

    private final SessionFactory sessionFactory;

    private final String scriptType;

    @Activate
    public JRubyConsoleCommandExtension( //
            @Reference ScriptEngineManager scriptEngineManager, //
            @Reference JRubyScriptEngineFactory jRubyScriptEngineFactory, //
            @Reference JRubyScriptFileWatcher scriptFileWatcher, //
            @Reference ConfigDescriptionRegistry configDescriptionRegistry, //
            @Reference SessionFactory sessionFactory) {
        this.scriptEngineManager = scriptEngineManager;
        this.jRubyScriptEngineFactory = jRubyScriptEngineFactory;
        this.scriptFileWatcher = scriptFileWatcher;
        this.scriptType = jRubyScriptEngineFactory.getScriptTypes().getFirst();
        this.configDescriptionRegistry = configDescriptionRegistry;
        this.sessionFactory = sessionFactory;
        sessionFactory.getRegistry().register(this);
    }

    @Deactivate
    protected void deactivate() {
        sessionFactory.getRegistry().unregister(this);
    }

    @Override
    public @Nullable Completer getCompleter(boolean scoped) {
        return this;
    }

    @Override
    public String getDescription() {
        return "JRuby Scripting console utilities.";
    }

    @Override
    public String getName() {
        return "jrubyscripting";
    }

    @Override
    public Parser getParser() {
        return null;
    }

    @Override
    public String getScope() {
        return "openhab";
    }

    @Override
    public Object execute(Session session, List<Object> argList) throws Exception {
        String[] args = argList.stream().map(Object::toString).toArray(String[]::new);
        PrintStream out = Process.Utils.current().out();
        final Console console = new JRubyConsole(getScope(), out);

        if (args.length > 0) {
            String command = args[0];
            switch (command) {
                case INFO:
                    info(console);
                    break;
                case CONSOLE:
                    startConsole(console, session, Arrays.copyOfRange(args, 1, args.length));
                    break;
                case BUNDLE:
                    bundler(console, Arrays.copyOfRange(args, 1, args.length));
                    break;
                case GEM:
                    gem(console, Arrays.copyOfRange(args, 1, args.length));
                    break;
                case UPDATE:
                    updateGems(console);
                    break;
                case PRUNE:
                    if (args.length > 1) {
                        if ("-f".equals(args[1]) || "--force".equals(args[1])) {
                            cleanupOtherGemHomes(console, session, true);
                        } else {
                            console.println("Use -f or --force to skip confirmation.");
                        }
                    } else {
                        cleanupOtherGemHomes(console, session, false);
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
        return null;
    }

    private void info(Console console) {
        final String PRINT_VERSION_NUMBERS = """
                library_version = defined?(OpenHAB::DSL::VERSION) && OpenHAB::DSL::VERSION

                puts "JRuby #{JRUBY_VERSION}"
                puts "JRuby Scripting Library #{library_version || 'is not installed'}"
                puts "GEM_HOME: #{ENV['GEM_HOME']}"
                puts "RUBYLIB: #{ENV['RUBYLIB']}"
                    """;

        executeWithFullJRuby(console, engine -> engine.eval(PRINT_VERSION_NUMBERS));
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

        Map<String, String> config = jRubyScriptEngineFactory.getConfiguration().getConfigurations();
        Map<String, List<ConfigDescriptionParameter>> paramsByGroup = configDescription.getParameters().stream()
                .collect(Collectors.groupingBy(ConfigDescriptionParameter::getGroupName));

        configDescription.getParameterGroups().forEach(group -> {
            List<ConfigDescriptionParameter> parameters = paramsByGroup.get(group.getName());
            if (parameters == null) {
                return;
            }

            String groupLabel = group.getLabel();
            if (groupLabel == null) {
                groupLabel = group.getName();
            }
            console.println(groupLabel);
            parameters.forEach(parameter -> {
                console.println("  " + parameter.getName() + ": " + config.get(parameter.getName()));
            });
            console.println("");
        });
    }

    private Map<String, String> getConsoles() {
        return (Map<String, String>) executeWithPlainJRuby(null, engine -> engine.eval(
                "require '" + DEFAULT_CONSOLE_PATH + "registry'; OpenHAB::Console::REGISTRY.transform_keys(&:to_s)"));
    }

    private void startConsole(Console console, Session session, String[] args) {
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
                    if (consoles == null) {
                        console.println(
                                "The list of console scripts is not available. Please install/update the JRuby helper library gem.");
                    } else {
                        console.println("Available console scripts:");
                        for (Map.Entry<String, String> consoleScript : consoles.entrySet()) {
                            String name = consoleScript.getKey();
                            String description = consoleScript.getValue();
                            if (defaultConsole.equals(DEFAULT_CONSOLE_PATH + name) || defaultConsole.equals(name)) {
                                description = description + " (default)";
                                defaultConsoleInRegistry = true;
                            }
                            console.println("  " + name + " - " + description);
                        }
                    }
                    if (!defaultConsoleInRegistry && defaultConsole != null && !defaultConsole.isBlank()) {
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
            engine.put("$terminal", session.getTerminal());
            engine.put(ScriptEngine.ARGV, argv);
            engine.eval(String.format("require '%s'", consoleScript));
            return null;
        });
    }

    synchronized private void bundler(Console console, String[] args) {
        final String BUNDLER = """
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

        String originalDir = System.setProperty("user.dir", scriptFileWatcher.getWatchPath().toString());
        try {
            executeWithPlainJRuby(console, engine -> {
                engine.put(ScriptEngine.ARGV, args);
                engine.eval(BUNDLER);
                return null;
            });
        } finally {
            if (originalDir == null) {
                System.clearProperty("user.dir");
            } else {
                System.setProperty("user.dir", originalDir);
            }
        }
    }

    synchronized private void gem(Console console, String[] args) {
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

    private void updateGems(Console console) {
        console.println("Updating configured gems: " + jRubyScriptEngineFactory.getConfiguration().getGems());
        executeWithPlainJRuby(console, engine -> {
            jRubyScriptEngineFactory.updateGems(engine);
            return null;
        });
    }

    /*
     * Deletes all other gem homes except the one that is currently in use.
     * This is to prevent the accumulation of old gem homes that are no longer needed.
     * The user is prompted to confirm the deletion when force is false.
     */
    private void cleanupOtherGemHomes(Console console, Session session, boolean force) {
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

                // Prevent readLine() from logging a warning
                // see:
                // https://github.com/apache/karaf/blob/ad427cd12543dc78e095bbaa4608d7ca3d5ea4d8/shell/core/src/main/java/org/apache/karaf/shell/impl/console/ConsoleSessionImpl.java#L549
                // https://github.com/jline/jline3/blob/ee4886bf24f40288a4044f9b4b74917b58103e49/reader/src/main/java/org/jline/reader/LineReaderBuilder.java#L90
                String previousSetting = System.setProperty("org.jline.reader.support.parsedline", "true");
                try {
                    session.readLine("\nPress Enter to delete them or Ctrl+C to cancel.", null);
                    console.println("");
                } catch (RuntimeException e) {
                    // Ctrl+C was pressed
                    // We can't use a more specific exception type without adding org.jline as bundle dependency
                    console.println("Operation cancelled.");
                    return;
                } finally {
                    if (previousSetting != null) {
                        System.setProperty("org.jline.reader.support.parsedline", previousSetting);
                    } else {
                        System.clearProperty("org.jline.reader.support.parsedline");
                    }
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
        ScriptEngine engine = scriptEngineManager.createScriptEngine(scriptType, scriptIdentifier).getScriptEngine();
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

    private List<String> getUsages() {
        return Arrays.asList( //
                buildCommandUsage(INFO, "displays information about JRuby Scripting add-on"), //
                buildCommandUsage(CONSOLE + " [--list|-l|--help|-h] | [script] [options]",
                        "starts an interactive JRuby console"), //
                buildCommandUsage(BUNDLE + " [arguments]", "runs Ruby bundler in the main Script path"), //
                buildCommandUsage(GEM + " [arguments]", "manages JRuby Scripting add-on's RubyGems"), //
                buildCommandUsage(UPDATE, "updates the configured gems"), //
                buildCommandUsage(PRUNE + " [-f|--force]", "cleans up older versions in the .gem directory") //
        );
    }

    private String buildCommandUsage(final String syntax, final String description) {
        return String.format("%s %s - %s", getName(), syntax, description);
    }

    private void printUsage(Console console) {
        for (final String usage : getUsages()) {
            console.printUsage(usage);
        }
    }

    @Override
    public int complete(Session session, CommandLine commandLine, List<String> candidates) {
        String globalCommand = getScope() + ":" + getName();
        String command = getName();
        String[] args = commandLine.getArguments();
        int cursorPosition = commandLine.getArgumentPosition();
        int cursorArgumentIndex = commandLine.getCursorArgumentIndex();

        if (args.length > 1 && !command.equals(args[0]) && !globalCommand.equals(args[0])) {
            return -1;
        }

        StringsCompleter completer = new StringsCompleter();
        SortedSet<String> strings = completer.getStrings();
        if (cursorArgumentIndex == 0) {
            strings.add(command);
            strings.add(globalCommand);
        } else if (cursorArgumentIndex == 1) {
            strings.addAll(SUB_COMMANDS);
        } else if (cursorArgumentIndex == 2) {
            if (CONSOLE.equals(args[1])) {
                Map<String, String> consoles = (Map<String, String>) getConsoles();
                if (consoles != null) {
                    strings.addAll(consoles.keySet());
                }
            }
        }

        if (!strings.isEmpty() && completer.complete(args, cursorArgumentIndex, cursorPosition, candidates)) {
            return commandLine.getBufferPosition() - cursorPosition;
        }

        return -1;
    }

    @FunctionalInterface
    public interface EngineEvalFunction {
        @Nullable
        Object apply(ScriptEngine e) throws ScriptException;
    }

    public class JRubyConsole implements Console {
        private final String scope;
        private final PrintStream out;

        public JRubyConsole(final String scope, PrintStream out) {
            this.scope = scope;
            this.out = out;
        }

        @Override
        public void printf(String format, Object... args) {
            out.printf(format, args);
        }

        @Override
        public void print(final String s) {
            out.print(s);
        }

        @Override
        public void println(final String s) {
            out.println(s);
        }

        @Override
        public void printUsage(final String s) {
            out.println(String.format("Usage: %s:%s", scope, s));
        }
    }
}
