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
package org.openhab.binding.nikohomecontrol.internal.console;

import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.BINDING_ID;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlBridgeHandler;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlBridgeHandler2;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlDiscover;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NikoHomeControlCommunication2;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link NikoHomeControlCommandExtension} is responsible for handling console commands
 *
 * @author Mark Herwege - Initial contribution
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class NikoHomeControlCommandExtension extends AbstractConsoleCommandExtension
        implements ConsoleCommandCompleter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String CONTROLLERS = "controllers";
    private static final String SYSTEMINFO = "systeminfo";
    private static final String DEVICELIST = "devicelist";
    private static final String DUMP = "dump";

    private static final String ROOT_PATH = System.getProperty("user.home") + File.separator + BINDING_ID
            + File.separator + DEVICELIST;

    private static final StringsCompleter CMD_COMPLETER = new StringsCompleter(
            List.of(CONTROLLERS, SYSTEMINFO, DEVICELIST), false);
    private static final StringsCompleter DUMP_COMPLETER = new StringsCompleter(List.of(DUMP), false);

    private final ThingRegistry thingRegistry;
    private final NetworkAddressService networkAddressService;

    private List<NikoHomeControlBridgeHandler> bridgeHandlers = List.of();

    @Activate
    public NikoHomeControlCommandExtension(final @Reference ThingRegistry thingRegistry,
            final @Reference NetworkAddressService networkAddressService) {
        super("nikohomecontrol", "Interact with the Niko Home Control binding");
        this.thingRegistry = thingRegistry;
        this.networkAddressService = networkAddressService;
    }

    @Override
    public void execute(String[] args, Console console) {
        if ((args.length < 1) || (args.length > 3)) {
            console.println("Invalid number of arguments");
            printUsage(console);
            return;
        }

        bridgeHandlers = thingRegistry.getAll().stream()
                .filter(t -> t.getHandler() instanceof NikoHomeControlBridgeHandler)
                .map(b -> ((NikoHomeControlBridgeHandler) b.getHandler())).toList();
        Map<String, String> bridgeNhcVersion = bridgeHandlers.stream().collect(Collectors
                .toMap(b -> b.getControllerId(), b -> b instanceof NikoHomeControlBridgeHandler2 ? "II" : "I"));

        NikoHomeControlBridgeHandler bridgeHandler = null;
        if (args.length > 1) {
            Optional<NikoHomeControlBridgeHandler> bridgeOptional = bridgeHandlers.stream()
                    .filter(b -> b.getControllerId().toLowerCase().equals(args[1].toLowerCase())).findAny();
            if (bridgeOptional.isEmpty()) {
                console.println("'" + args[1] + "' is not a valid controller ID");
                printUsage(console);
                return;
            }
            bridgeHandler = bridgeOptional.get();
        }

        switch (args[0].toLowerCase()) {
            case CONTROLLERS:
                if (args.length > 1) {
                    console.println("No extra argument allowed after 'controllers'");
                    printUsage(console);
                    return;
                } else {
                    Map<String, String> bridgeIds = bridgeHandlers.stream().collect(Collectors
                            .toMap(b -> b.getThing().getUID().toString(), b -> b.getControllerId().toLowerCase()));
                    List<String> controllerIds = List.of();
                    Map<String, String> controllerNhcVersion = Map.of();
                    try {
                        String broadcastAddr = networkAddressService.getConfiguredBroadcastAddress();
                        if (broadcastAddr == null) {
                            console.println(
                                    "Controller discovery not possible, no broadcast address found, result only contains bridges");
                        } else {
                            NikoHomeControlDiscover nhcDiscover;
                            nhcDiscover = new NikoHomeControlDiscover(broadcastAddr);
                            controllerIds = nhcDiscover.getNhcBridgeIds().stream().map(String::toLowerCase)
                                    .filter(id -> !bridgeIds.containsValue(id)).toList();
                            controllerNhcVersion = controllerIds.stream().collect(
                                    Collectors.toMap(Function.identity(), id -> nhcDiscover.isNhcII(id) ? "II" : "I"));
                        }
                    } catch (IOException e) {
                        console.println(
                                "Controller discovery not possible, network error, result only contains bridges");
                    }
                    Map<String, String> nhcVersion = Map.copyOf(controllerNhcVersion);
                    console.printf("%-14s %-12s %s%n", "Controller-ID", "NHC-Version", "Bridge-ID");
                    console.printf("%-14s %-12s %s%n", "-------------", "-----------", "---------");
                    bridgeIds.forEach(
                            (bridge, id) -> console.printf("%-14s %-12s %s%n", id, bridgeNhcVersion.get(id), bridge));
                    controllerIds.forEach(id -> console.printf("%-14s %-12s %s%n", id, nhcVersion.get(id), " "));
                }
                break;
            case SYSTEMINFO:
                if (args.length < 2) {
                    console.println("No controller ID provided");
                    printUsage(console);
                    return;
                }
                if (bridgeHandler != null) {
                    if (!ThingStatus.ONLINE.equals(bridgeHandler.getThing().getStatus())) {
                        console.println("Niko Home Control bridge not online, system info may be out of date");
                    }
                    console.println("Property                     Value");
                    console.println("--------                     -----");
                    Map<String, String> properties = bridgeHandler.getThing().getProperties();
                    for (String key : properties.keySet()) {
                        console.printf("%-28.28s %s%n", key, properties.get(key));
                    }
                }
                break;
            case DEVICELIST:
                if (args.length < 2) {
                    console.println("No controller ID provided");
                    printUsage(console);
                    return;
                }
                if (!"II".equals(bridgeNhcVersion.get(args[1]))) {
                    console.println("'" + args[1] + "' is not a Niko Home Control II bridge");
                    printUsage(console);
                    return;
                }
                if (bridgeHandler != null) {
                    if (!ThingStatus.ONLINE.equals(bridgeHandler.getThing().getStatus())) {
                        console.println("Niko Home Control bridge not online, device list may be out of date");
                    }
                    NikoHomeControlCommunication2 nhcComm = (NikoHomeControlCommunication2) bridgeHandler
                            .getCommunication();
                    if (nhcComm != null) {
                        String devices = prettyJson(nhcComm.getRawDevicesListResponse());
                        if (args.length == 2) {
                            console.println(devices);
                        } else if (DUMP.equals(args[2])) {
                            String filename = ROOT_PATH + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                                    + ".json";
                            String systeminfo = GSON.toJson(bridgeHandler.getThing().getProperties());
                            writeJsonToFile(filename, systeminfo, console);
                            writeJsonToFile(filename, devices, console);
                            console.printf("System info and device list dumped to file '%s'%n", filename);
                        } else {
                            console.println("Command argument '" + args[2] + "' not recognized");
                            printUsage(console);
                            return;
                        }
                    }
                }
                break;
            default:
                console.println("Command argument '" + args[0] + "' not recognized");
                printUsage(console);
        }
    }

    private String prettyJson(String json) {
        try {
            return GSON.toJson(JsonParser.parseString(json));
        } catch (JsonSyntaxException e) {
            // Keep the unformatted json if there is a syntax exception
            return json;
        }
    }

    private void writeJsonToFile(String filename, String json, Console console) {
        try {
            JsonElement element = JsonParser.parseString(json);
            if (element.isJsonNull() || (element.isJsonArray() && ((JsonArray) element).size() == 0)) {
                console.println("Empty device list, nothing to dump");
                return;
            }
        } catch (JsonSyntaxException e) {
            // Just continue and write the file with non-valid json anyway
        }

        // ensure full path exists
        File file = new File(filename);
        File parentFile = file.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }

        final byte[] contents = (json + "\n").getBytes(StandardCharsets.UTF_8);
        try {
            Files.write(file.toPath(), contents, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            console.println("I/O error writing device list to file");
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] { buildCommandUsage(CONTROLLERS, "list all Niko Home Control Controllers"),
                buildCommandUsage(SYSTEMINFO + " <controller ID>",
                        "show system info for Controller with controller ID"),
                buildCommandUsage(DEVICELIST + " <controller ID>",
                        "create device list of installation on Controller with controller ID"),
                buildCommandUsage(DEVICELIST + " <controller ID> " + DUMP,
                        "dump device list of installation with controller ID to file") });
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        if (cursorArgumentIndex <= 0) {
            return CMD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
        } else if (cursorArgumentIndex == 1) {
            return new StringsCompleter(bridgeHandlers.stream().filter(b -> b instanceof NikoHomeControlBridgeHandler2)
                    .map(b -> b.getControllerId()).toList(), false)
                    .complete(args, cursorArgumentIndex, cursorPosition, candidates);
        } else if (cursorArgumentIndex == 2) {
            return DUMP_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
        }
        return false;
    }
}
