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
package org.openhab.automation.pythonscripting.internal.console;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.pythonscripting.internal.PythonScriptEngine;
import org.openhab.automation.pythonscripting.internal.PythonScriptEngineConfiguration;
import org.openhab.automation.pythonscripting.internal.PythonScriptEngineFactory;
import org.openhab.automation.pythonscripting.internal.console.handler.InfoCmd;
import org.openhab.automation.pythonscripting.internal.console.handler.TypingCmd;
import org.openhab.automation.pythonscripting.internal.console.handler.UpdateCmd;
import org.openhab.core.automation.module.script.ScriptEngineContainer;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptEngineManager;
import org.openhab.core.config.core.ConfigDescriptionRegistry;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link PythonConsoleCommandExtension} class
 *
 * @author Holger Hees - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class PythonConsoleCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {
    private static final String INFO = "info";
    private static final String CONSOLE = "console";
    private static final String TYPING = "typing";
    private static final String PIP = "pip";
    private static final String PIP_INSTALL = "install";
    private static final String PIP_UNINSTALL = "uninstall";
    private static final String PIP_SHOW = "show";
    private static final String PIP_LIST = "list";
    private static final String UPDATE = "update";
    private static final String UPDATE_LIST = "list";
    private static final String UPDATE_CHECK = "check";
    private static final String UPDATE_INSTALL = "install";

    private static final List<String> COMMANDS = List.of(INFO, CONSOLE, UPDATE, TYPING);
    private static final List<String> UPDATE_COMMANDS = List.of(UPDATE_LIST, UPDATE_CHECK, UPDATE_INSTALL);
    private static final List<String> PIP_COMMANDS = List.of(PIP_INSTALL, PIP_UNINSTALL, PIP_SHOW, PIP_LIST);

    private final ScriptEngineManager scriptEngineManager;
    private final PythonScriptEngineFactory pythonScriptEngineFactory;
    private final ConfigDescriptionRegistry configDescriptionRegistry;
    private final PythonScriptEngineConfiguration pythonScriptEngineConfiguration;

    private final String scriptType;

    @Activate
    public PythonConsoleCommandExtension( //
            @Reference ScriptEngineManager scriptEngineManager, //
            @Reference PythonScriptEngineFactory pythonScriptEngineFactory, //
            @Reference ConfigDescriptionRegistry configDescriptionRegistry) {
        super("pythonscripting", "Python Scripting console utilities.");
        this.scriptEngineManager = scriptEngineManager;
        this.pythonScriptEngineFactory = pythonScriptEngineFactory;
        this.pythonScriptEngineConfiguration = pythonScriptEngineFactory.getConfiguration();
        this.scriptType = PythonScriptEngineFactory.SCRIPT_TYPE;
        this.configDescriptionRegistry = configDescriptionRegistry;
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public List<String> getUsages() {
        ArrayList<String> usages = new ArrayList<String>();
        usages.add(buildCommandUsage(INFO, "displays information about Python Scripting add-on"));
        usages.add(buildCommandUsage(CONSOLE, "starts an interactive python console"));
        usages.add(getUpdateUsage());
        if (pythonScriptEngineConfiguration.isVEnvEnabled()) {
            usages.add(getPipUsage());
        }
        usages.add(buildCommandUsage(TYPING, "create type hint stub files"));
        return usages;
    }

    public String getUpdateUsage() {
        return buildCommandUsage(UPDATE + " <" + String.join("|", UPDATE_COMMANDS) + ">", "update helper lib module");
    }

    public String getPipUsage() {
        return buildCommandUsage(PIP + " <" + String.join("|", PIP_COMMANDS) + "> [optional pip specific arguments]",
                "manages python modules");
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        StringsCompleter completer = new StringsCompleter();
        SortedSet<String> strings = completer.getStrings();
        if (cursorArgumentIndex == 0) {
            strings.addAll(COMMANDS);
            if (pythonScriptEngineConfiguration.isVEnvEnabled()) {
                strings.add(PIP);
            }
        } else if (cursorArgumentIndex == 1) {
            if (PIP.equals(args[0])) {
                strings.addAll(PIP_COMMANDS);
            } else if (UPDATE.equals(args[0])) {
                strings.addAll(UPDATE_COMMANDS);
            }
        }

        return strings.isEmpty() ? false : completer.complete(args, cursorArgumentIndex, cursorPosition, candidates);
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String command = args[0];
            switch (command) {
                case "--help":
                case "-h":
                    printUsage(console);
                    break;
                case INFO:
                    info(console);
                    break;
                case CONSOLE:
                    startConsole(console, Arrays.copyOfRange(args, 1, args.length));
                    break;
                case UPDATE:
                    executeUpdate(console, Arrays.copyOfRange(args, 1, args.length));
                    break;
                case TYPING:
                    executeTyping(console);
                    break;
                case PIP:
                    if (pythonScriptEngineConfiguration.isVEnvEnabled()) {
                        executePip(console, Arrays.copyOfRange(args, 1, args.length));
                        break;
                    }
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
        new InfoCmd(pythonScriptEngineConfiguration, console).show(configDescriptionRegistry);
    }

    private void startConsole(Console console, String[] args) {
        final String startInteractiveSessionCode = """
                import readline # optional, will allow Up/Down/History in the console
                import code

                vars = globals().copy()
                vars.update(locals())
                shell = code.InteractiveConsole(vars)
                try:
                    shell.interact()
                except SystemExit:
                    pass
                """;

        executePython(console, engine -> engine.eval(startInteractiveSessionCode), true);
    }

    private void executeUpdate(Console console, String[] args) {
        if (args.length == 0) {
            console.println("Missing update action");
            console.printUsage(getUpdateUsage());
        } else if (UPDATE_COMMANDS.indexOf(args[0]) == -1) {
            console.println("Unknown update action '" + args[0] + "'");
            console.printUsage(getUpdateUsage());
        } else {
            UpdateCmd cmd = new UpdateCmd(pythonScriptEngineConfiguration, console);
            switch (args[0]) {
                case UPDATE_LIST:
                    cmd.updateList();
                    break;
                case UPDATE_CHECK:
                    cmd.updateCheck();
                    break;
                case UPDATE_INSTALL:
                    if (args.length <= 1) {
                        console.println("Missing release name");
                        console.printUsage("pythonscripting update install <\"latest\"|version>");
                    } else {
                        cmd.updateInstall(args[1]);
                    }
                    break;
            }
        }
    }

    private void executeTyping(Console console) {
        try {
            if (!confirmAction(console, "You are about creating python type hint stub files in '"
                    + PythonScriptEngineConfiguration.PYTHON_TYPINGS_PATH + "'.")) {
                return;
            }
            new TypingCmd(new TypingCmd.Logger(console)).build();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void executePip(Console console, String[] args) {
        if (args.length == 0) {
            console.println("Missing pip action");
            console.printUsage(getPipUsage());
        } else if (PIP_COMMANDS.indexOf(args[0]) == -1) {
            console.println("Unknown pip action '" + args[1] + "'");
            console.printUsage(getPipUsage());
        } else {
            ArrayList<String> params = new ArrayList<String>(Arrays.asList(args));

            if (PIP_UNINSTALL.equals(args[0]) && args.length >= 2) {
                if (!confirmAction(console, "You are uninstalling python modules.")) {
                    return;
                }
                params.add(1, "-y");
            }

            final String pipCode = """
                    import subprocess
                    import sys

                    command_list = [sys.executable, "-m", "pip"] + PARAMS
                    with subprocess.Popen(command_list, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True) as proc:
                        for line in proc.stdout:
                            print(line.rstrip())
                    """;

            executePython(console, engine -> {
                engine.getContext().setAttribute("PARAMS", params, ScriptContext.ENGINE_SCOPE);
                return engine.eval(pipCode);
            }, false);
        }
    }

    private void printLoadingMessage(Console console, boolean show) {
        String loadingMessage = "Loading Python script engine...";
        if (show) {
            console.print(loadingMessage);
        } else {
            // Clear the loading message
            console.print("\r" + " ".repeat(loadingMessage.length()) + "\r");
        }
    }

    /*
     * Create Python engine.
     *
     * withFullContext = true => means a full openHAB-managed script engine with scoped variables
     * including any injected required modules.
     */
    private @Nullable Object executePython(Console console, EngineEvalFunction process, boolean withFullContext) {
        String scriptIdentifier = "python-console-" + UUID.randomUUID().toString();
        ScriptEngine engine = null;

        try {
            printLoadingMessage(console, true);

            if (withFullContext) {
                ScriptEngineContainer container = scriptEngineManager.createScriptEngine(scriptType, scriptIdentifier);
                if (container != null) {
                    engine = container.getScriptEngine();
                }
            } else {
                engine = pythonScriptEngineFactory.createScriptEngine(scriptType);
                if (engine != null) {
                    engine.getContext().setAttribute(ScriptEngineFactory.CONTEXT_KEY_ENGINE_IDENTIFIER,
                            scriptIdentifier, ScriptContext.ENGINE_SCOPE);
                }
            }

            if (engine == null) {
                console.println("Error: Unable to create python script engine.");
                return null;
            }

            printLoadingMessage(console, false);

            engine.getContext().setAttribute(PythonScriptEngine.CONTEXT_KEY_ENGINE_LOGGER_INPUT,
                    createInputStream(console), ScriptContext.ENGINE_SCOPE);
            engine.getContext().setAttribute(PythonScriptEngine.CONTEXT_KEY_ENGINE_LOGGER_OUTPUT, System.out,
                    ScriptContext.ENGINE_SCOPE);

            return process.apply(engine);
        } catch (ScriptException e) {
            console.println("Error: " + e.getMessage());
            return null;
        } finally {
            if (withFullContext) {
                scriptEngineManager.removeEngine(scriptIdentifier);
            } else {
                if (engine instanceof AutoCloseable closeable) {
                    try {
                        closeable.close();
                    } catch (Exception e) {
                        console.println("Error while closing script engine. " + e.getMessage());
                    }
                }
            }
        }
    }

    private boolean confirmAction(Console console, String msg) {
        try {
            console.readLine("\n" + msg + "\n\nPress Enter to confirm or Ctrl+C to cancel.", null);
            console.println("");
            return true;
        } catch (IOException e) {
            console.println("Error: " + e.getMessage());
            return false;
        } catch (RuntimeException e) {
            console.println("Operation cancelled.");
            return false;
        }
    }

    private InputStream createInputStream(Console console) {
        return new InputStream() {
            byte @Nullable [] buffer = null;
            int pos = 0;

            @SuppressWarnings("null")
            @Override
            public int read() throws IOException {
                if (pos < 0) {
                    pos = 0;
                    return -1;
                } else if (buffer == null) {
                    assert pos == 0;
                    try {
                        String line = console.readLine("", null);
                        buffer = line.getBytes(StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        return -1;
                    }
                }
                if (pos == buffer.length) {
                    buffer = null;
                    pos = -1;
                    return '\n';
                } else {
                    return buffer[pos++];
                }
            }
        };
    }

    @FunctionalInterface
    public interface EngineEvalFunction {
        @Nullable
        Object apply(ScriptEngine e) throws ScriptException;
    }
}
