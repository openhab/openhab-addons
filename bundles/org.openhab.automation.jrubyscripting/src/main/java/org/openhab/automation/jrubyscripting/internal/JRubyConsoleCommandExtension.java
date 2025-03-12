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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;
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

    private static final String INFO = "info";
    private static final String CONSOLE = "console";
    private static final String GEM = "gem";
    private static final String UPDATE = "update";
    private static final String PRUNE = "prune";

    private static final List<String> SUB_COMMANDS = List.of(INFO, CONSOLE, GEM, UPDATE, PRUNE);

    private final ScriptEngineManager scriptEngineManager;
    private final JRubyScriptEngineFactory jRubyScriptEngineFactory;
    private final JRubyScriptFileWatcher scriptFileWatcher;

    private final SessionFactory sessionFactory;

    private final String scriptType;

    @Activate
    public JRubyConsoleCommandExtension(@Reference ScriptEngineManager scriptEngineManager,
            @Reference JRubyScriptEngineFactory jRubyScriptEngineFactory,
            @Reference JRubyScriptFileWatcher scriptFileWatcher, @Reference SessionFactory sessionFactory) {
        this.scriptEngineManager = scriptEngineManager;
        this.jRubyScriptEngineFactory = jRubyScriptEngineFactory;
        this.scriptFileWatcher = scriptFileWatcher;
        this.scriptType = jRubyScriptEngineFactory.getScriptTypes().getFirst();
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
        jRubyScriptEngineFactory.getConfiguration().getConfigurations().forEach((key, value) -> {
            console.println(key + ": " + value);
        });
    }

    private void startConsole(Console console, Session session, String[] args) {
        String script = jRubyScriptEngineFactory.getConfiguration().getConsole();
        if (args.length > 0 && !args[0].startsWith("-")) {
            script = args[0];
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        if (script == null || script.isBlank()) {
            console.println(
                    "No console script configured. Please set the 'console' property in the add-on configuration, "
                            + "or specify one as an argument to 'jrubyscripting console <scriptname>'.");
            return;
        }

        final String consoleScript = script.contains("/") ? script : "openhab/console/" + script;
        final String[] argv = args;

        logger.debug("Starting JRuby console with script: {}", consoleScript);

        executeWithFullJRuby(console, engine -> {
            engine.put("$terminal", session.getTerminal());
            engine.put(ScriptEngine.ARGV, argv);
            engine.eval(String.format("require '%s'", consoleScript));
            return null;
        });
    }

    private void gem(Console console, String[] args) {
        final String GEM = """
                ENV['PATH'] ||= '' # gem command requires PATH to be set
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
    private @Nullable Object executeWithPlainJRuby(Console console, EngineEvalFunction process) {
        ScriptEngine engine = jRubyScriptEngineFactory.createScriptEngine(scriptType);
        try {
            return process.apply(engine);
        } catch (ScriptException e) {
            console.println("Error: " + e.getMessage());
            return null;
        }
    }

    private List<String> getUsages() {
        return Arrays.asList( //
                buildCommandUsage(INFO, "displays information about JRuby Scripting add-on"), //
                buildCommandUsage(CONSOLE + " [script] [options]", "starts an interactive JRuby console"), //
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
