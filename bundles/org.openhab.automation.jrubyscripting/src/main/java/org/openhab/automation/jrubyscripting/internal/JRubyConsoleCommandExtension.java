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
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;
import java.util.stream.Stream;

import javax.script.ScriptContext;
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
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.scheduler.Scheduler;
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
    private static final String SHELL = "shell";
    private static final String GEM = "gem";
    private static final String UPDATE = "update";
    private static final String PRUNE = "prune";
    private static final String UNLOAD = "unload";

    private static final List<String> SUB_COMMANDS = List.of(INFO, SHELL, GEM, UPDATE, PRUNE, UNLOAD);
    private static final List<String> GEM_OPTIONS = List.of("bootstrap", "build", "bump", "cert", "check", "cleanup",
            "contents", "dependency", "environment", "fetch", "gemspec", "generate_index", "help", "info", "install",
            "list", "lock", "mirror", "open", "outdated", "owner", "pristine", "push", "rdoc", "release", "search",
            "server", "signin", "signout", "sources", "specification", "stale", "tag", "uninstall", "unpack", "update",
            "which", "yank");

    private final Scheduler scheduler;
    private final ScriptEngineManager manager;
    private final JRubyScriptEngineFactory jRubyScriptEngineFactory;
    private final JRubyScriptFileWatcher scriptFileWatcher;

    private final SessionFactory sessionFactory;

    private final String scriptType;
    private Optional<IdentifierAndEngine> scriptEngine = Optional.empty();
    private @Nullable ScheduledCompletableFuture<?> engineUnloader = null;
    private boolean scoped = true;

    @Activate
    public JRubyConsoleCommandExtension(@Reference ScriptEngineManager manager, @Reference Scheduler scheduler,
            @Reference JRubyScriptEngineFactory jRubyScriptEngineFactory,
            @Reference JRubyScriptFileWatcher scriptFileWatcher, @Reference SessionFactory sessionFactory) {
        this.scheduler = scheduler;
        this.manager = manager;
        this.jRubyScriptEngineFactory = jRubyScriptEngineFactory;
        this.scriptFileWatcher = scriptFileWatcher;
        this.scriptType = jRubyScriptEngineFactory.getScriptTypes().getFirst();
        this.sessionFactory = sessionFactory;
        sessionFactory.getRegistry().register(this);
    }

    @Deactivate
    protected void deactivate() {
        sessionFactory.getRegistry().unregister(this);
        unloadEngine();
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
        final Console console = new JRubyConsole(getScope(), out, session);

        if (args.length > 0) {
            String command = args[0];
            switch (command) {
                case INFO:
                    info(console);
                    break;
                case SHELL:
                    shell(console);
                    break;
                case GEM:
                    gem(console, Arrays.stream(args).skip(1).toList());
                    break;
                case UPDATE:
                    updateGems(console);
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
                case UNLOAD:
                    unloadEngine();
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
                library_version = Module.const_defined?(:OpenHAB) && OpenHAB::DSL::VERSION

                puts "JRuby #{JRUBY_VERSION}"
                puts "JRuby Scripting Library #{library_version || 'is not installed'}"
                puts "GEM_HOME: #{ENV['GEM_HOME']}"
                puts "RUBYLIB: #{ENV['RUBYLIB']}"
                    """;

        executeWithJRuby(console, engine -> engine.eval(PRINT_VERSION_NUMBERS));
        console.println("Script path: " + scriptFileWatcher.getWatchPath());
        console.println("");
        console.println("JRuby Scripting Add-on Configuration:");
        console.println("=====================================");
        jRubyScriptEngineFactory.getConfiguration().getConfigurations().forEach((key, value) -> {
            console.println(key + ": " + value);
        });
    }

    private void shell(Console console) {
        // We're doing the REPL inside Ruby to preserve local variables between loops
        final String RUBY_REPL = """
                    # frozen_string_literal: true
                    class Console
                      # Create constants instead of java_import to avoid polluting the global namespace
                      LineReader ||= org.jline.reader.LineReader
                      Bracket ||= org.jline.reader.impl.DefaultParser::Bracket

                      # use ||= to avoid warnings when the shell is re-entered
                      ESC ||= "\\e[" # Double backslashes because it's inside a Java string
                      BOLD ||= ESC + "1m"
                      RESET ||= ESC + "0m"
                      RED ||= ESC + "31m"
                      GREEN ||= ESC + "32m"
                      YELLOW ||= ESC + "33m"
                      BLUE ||= ESC + "34m"
                      # MAGENTA ||= ESC + "35m"
                      CYAN ||= ESC + "36m"

                      ERROR ||= RED
                      STRING ||= BOLD + YELLOW
                      NUMBER ||= BOLD + BLUE
                      OBJECT ||= BOLD + GREEN
                      SIMPLE_CLASS ||= BOLD + CYAN
                      PROMPT ||= BOLD + "JRuby> " + RESET

                      def initialize
                        parser = org.jline.reader.impl.DefaultParser.new
                                    .eof_on_unclosed_bracket(Bracket::CURLY, Bracket::ROUND, Bracket::SQUARE)
                                    .eof_on_unclosed_quote(true)
                                    .eof_on_escaped_new_line(true)

                        completer =
                          org.jline.reader.Completer.impl do |method_name, reader, line, candidates|
                            sources = OpenHAB::DSL.items.map(&:name) +
                                      OpenHAB::DSL.methods(false) +
                                      TOPLEVEL_BINDING.local_variables +
                                      Object.constants

                            candidates.add_all(sources.map { |c| org.jline.reader.Candidate.new(c.to_s) })
                          end

                        @reader = org.jline.reader.LineReaderBuilder.builder
                                     .terminal($console.session.terminal)
                                     .app_name("jrubyscripting")
                                     .parser(parser)
                                     .completer(completer)
                                     .variable(LineReader::SECONDARY_PROMPT_PATTERN, "%M%P > ")
                                     .variable(LineReader::INDENTATION, 2)
                                     .build
                      end

                      def read_line = @reader.read_line(PROMPT)

                      def print_result(result)
                        puts "=> " +
                          case result
                          when nil, true, false then SIMPLE_CLASS + result.inspect + RESET
                          when String then '"' + STRING + result.dump[1..-2] + RESET + '"'
                          when Numeric, Array, Hash then NUMBER + result.to_s + RESET
                          else OBJECT + result.to_java.inspect + RESET
                          end
                      end

                      def print_error(error)
                        puts ERROR + "Error: #{error.message}" + RESET
                      end
                    end

                    console = Console.new

                    puts "Welcome to JRuby REPL. Press Ctrl+D to exit, Alt+Enter (or Esc,Enter) to insert a new line."

                    loop do
                      begin
                        input = console.read_line
                        next if input.strip.empty?
                      rescue org.jline.reader.UserInterruptException # Ctrl+C is pressed
                        next
                      rescue org.jline.reader.EndOfFileException # Ctrl+D is pressed
                        break
                      end

                      begin
                        # Use TOPLEVEL_BINDING to isolate and keep the local variables between loops
                        result = TOPLEVEL_BINDING.eval(input)
                        console.print_result(result)
                      rescue Exception => e
                        console.print_error(e)
                      end
                    end
                """;

        ScriptEngine engine = getEngine(console);
        engine.getContext().setAttribute("$console", console, ScriptContext.ENGINE_SCOPE);
        try {
            engine.eval(RUBY_REPL);
        } catch (ScriptException e) {
            console.println("Error: " + e.getMessage());
        }
    }

    private void gem(Console console, List<String> args) {
        final String GEM = """
                ENV['PATH'] ||= '' # gem command requires PATH to be set
                require "rubygems/gem_runner"
                Gem::GemRunner.new.run args.to_a
                    """;

        executeWithJRuby(console, engine -> {
            engine.getContext().setAttribute("args", args, ScriptContext.ENGINE_SCOPE);
            engine.eval(GEM);
            return null;
        });
    }

    private void updateGems(Console console) {
        executeWithJRuby(console, engine -> {
            jRubyScriptEngineFactory.updateGems(engine);
            return null;
        });
    }

    /*
     * Deletes all other gem homes except the one that is currently in use.
     * This is to prevent the accumulation of old gem homes that are no longer needed.
     * The user is prompted to confirm the deletion when force is false.
     */
    private void cleanupOtherGemHomes(Console console, boolean force) {
        // Only our console contains the session required for readLine() to work
        if (console instanceof JRubyConsole jrubyConsole) {
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
                Iterator<File> filesToDelete = paths
                        .filter(p -> !p.equals(gemHomeBase) && !p.startsWith(specificGemHome)) //
                        .sorted(Comparator.reverseOrder()) //
                        .map(Path::toFile).iterator();

                if (!filesToDelete.hasNext()) {
                    console.println("No files or directories to delete from " + gemHomeBase);
                    return;
                }

                if (!force) {
                    console.printf(
                            "Some files and directories exist in '%s' outside of your configured gem home '%s'.\n",
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
                        jrubyConsole.getSession().readLine("\nPress Enter to delete them or Ctrl+C to cancel.", null);
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
    }

    private synchronized ScriptEngine getEngine(Console console) {
        ScriptEngine engine;

        ScheduledCompletableFuture<?> unloader = this.engineUnloader;
        if (unloader != null) {
            unloader.cancel(false);
        }
        if (this.scriptEngine.isEmpty()) {
            String loadingMessage = "Creating JRuby console script engine...";
            console.print(loadingMessage);
            final String scriptIdentifier = "jruby-console-" + UUID.randomUUID().toString();
            engine = manager.createScriptEngine(scriptType, scriptIdentifier).getScriptEngine();
            console.print("\r" + " ".repeat(loadingMessage.length()) + "\r");
            this.scriptEngine = Optional.of(new IdentifierAndEngine(scriptIdentifier, engine));
        } else {
            engine = this.scriptEngine.get().engine();
        }
        this.engineUnloader = scheduler.schedule(() -> unloadEngine(), Instant.now().plusSeconds(900));

        return engine;
    }

    private synchronized void unloadEngine() {
        ScheduledCompletableFuture<?> unloader = this.engineUnloader;
        if (unloader != null) {
            unloader.cancel(false);
            engineUnloader = null;
        }
        scriptEngine.ifPresent(se -> manager.removeEngine(se.scriptIdentifier()));
        scriptEngine = Optional.empty();
    }

    private @Nullable Object executeWithJRuby(Console console, EngineEvalFunction process) {
        try {
            return process.apply(getEngine(console));
        } catch (ScriptException e) {
            console.println("Error: " + e.getMessage());
            return null;
        }
    }

    private List<String> getUsages() {
        return Arrays.asList( //
                buildCommandUsage(INFO, "displays information about JRuby Scripting add-on"), //
                buildCommandUsage(SHELL, "starts an interactive JRuby shell"), //
                buildCommandUsage(GEM, "manages JRuby Scripting add-on's RubyGems"), //
                buildCommandUsage(UPDATE, "updates the configured gems"), //
                buildCommandUsage(PRUNE + " [-f|--force]", "cleans up older versions in the .gem directory"), //
                buildCommandUsage(UNLOAD, "unloads the console's script engine") //
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
        } else if (cursorArgumentIndex == 2 && args.length >= 2 && args[1].equals(GEM)) {
            strings.addAll(GEM_OPTIONS);
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

    public record IdentifierAndEngine(String scriptIdentifier, ScriptEngine engine) {
    }

    public class JRubyConsole implements Console {
        private final Session session;
        private final String scope;
        private final PrintStream out;

        public JRubyConsole(final String scope, PrintStream out, Session session) {
            this.scope = scope;
            this.out = out;
            this.session = session;
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

        public Session getSession() {
            return session;
        }
    }
}
