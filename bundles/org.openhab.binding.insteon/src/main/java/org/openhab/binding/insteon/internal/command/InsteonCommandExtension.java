/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.command;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonBindingConstants;
import org.openhab.binding.insteon.internal.handler.InsteonBridgeHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * The {@link InsteonCommandExtension} is responsible for handling console commands
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class InsteonCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final List<Class<? extends InsteonCommand>> SUBCMD_CLASSES = List.of(ModemCommand.class,
            DeviceCommand.class, SceneCommand.class, ChannelCommand.class, DebugCommand.class);

    private final ThingRegistry thingRegistry;
    private final InsteonLegacyCommandExtension legacyCommandExtension;
    private final Map<String, InsteonCommand> subCommands;

    private @Nullable InsteonBridgeHandler handler;

    @Activate
    public InsteonCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super(InsteonBindingConstants.BINDING_ID, "Interact with the Insteon integration.");
        this.thingRegistry = thingRegistry;

        this.legacyCommandExtension = new InsteonLegacyCommandExtension(thingRegistry);
        this.subCommands = SUBCMD_CLASSES.stream().map(this::instantiateCommand).filter(Objects::nonNull)
                .collect(Collectors.toMap(InsteonCommand::getSubCommand, Function.identity(), (key1, key2) -> key1,
                        LinkedHashMap::new));
    }

    @Override
    public List<String> getUsages() {
        if (legacyCommandExtension.isAvailable()) {
            return legacyCommandExtension.getUsages();
        }
        return subCommands.values().stream().map(cmd -> buildCommandUsage(cmd.getSubCommand(), cmd.getDescription()))
                .toList();
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (legacyCommandExtension.isAvailable()) {
            legacyCommandExtension.execute(args, console);
            return;
        }

        InsteonBridgeHandler handler = getBridgeHandler();
        if (handler == null) {
            console.println("No Insteon bridge configured or enabled.");
            return;
        }

        if (handler.getModem() == null) {
            console.println("Insteon bridge " + handler.getThing().getUID() + " not initialized yet.");
            return;
        }

        if (args.length == 0) {
            printUsage(console);
            return;
        }

        InsteonCommand command = subCommands.get(args[0]);
        if (command != null) {
            command.execute(Arrays.copyOfRange(args, 1, args.length), console);
        } else {
            console.println("Unknown command '" + args[0] + "'");
            printUsage(console);
        }
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        InsteonBridgeHandler handler = getBridgeHandler();
        if (!legacyCommandExtension.isAvailable() && handler != null && handler.getModem() != null) {
            if (cursorArgumentIndex == 0) {
                return new StringsCompleter(subCommands.keySet(), false).complete(args, cursorArgumentIndex,
                        cursorPosition, candidates);
            }

            ConsoleCommandCompleter completer = subCommands.get(args[0]);
            if (completer != null) {
                return completer.complete(Arrays.copyOfRange(args, 1, args.length), cursorArgumentIndex - 1,
                        cursorPosition, candidates);
            }
        }
        return false;
    }

    public @Nullable InsteonBridgeHandler getBridgeHandler() {
        InsteonBridgeHandler handler = this.handler;
        if (handler == null || !handler.getThing().isEnabled()) {
            return getBridgeHandlers().findFirst().orElse(null);
        }
        return handler;
    }

    public Stream<InsteonBridgeHandler> getBridgeHandlers() {
        return thingRegistry.getAll().stream().filter(Thing::isEnabled).map(Thing::getHandler)
                .filter(InsteonBridgeHandler.class::isInstance).map(InsteonBridgeHandler.class::cast);
    }

    public void setBridgeHandler(InsteonBridgeHandler handler) {
        this.handler = handler;
    }

    private @Nullable InsteonCommand instantiateCommand(Class<? extends InsteonCommand> clazz) {
        try {
            return clazz.getDeclaredConstructor(InsteonCommandExtension.class).newInstance(this);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            return null;
        }
    }
}
