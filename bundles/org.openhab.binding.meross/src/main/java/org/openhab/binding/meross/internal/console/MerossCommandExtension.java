/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.meross.internal.console;

import static org.openhab.binding.meross.internal.MerossBindingConstants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.meross.internal.ContentAnonymizer;
import org.openhab.binding.meross.internal.api.MerossManager;
import org.openhab.binding.meross.internal.api.MerossMqttConnector;
import org.openhab.binding.meross.internal.dto.Device;
import org.openhab.binding.meross.internal.handler.MerossBridgeHandler;
import org.openhab.binding.meross.internal.handler.MerossDeviceHandler;
import org.openhab.binding.meross.internal.handler.MerossDeviceHandlerCallback;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.transport.mqtt.MqttException;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandler;
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
 * The {@link MerossCommandExtension} is responsible for handling console commands
 *
 * @author Mark Herwege - Initial contribution
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class MerossCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String FINGERPRINT_ROOT_PATH = System.getProperty("user.home") + File.separator + BINDING_ID;

    private static final String DEVICES = "devices";
    private static final String FINGERPRINT = "fingerprint";
    private static final StringsCompleter CMD_COMPLETER = new StringsCompleter(List.of(FINGERPRINT), false);

    private final ThingRegistry thingRegistry;
    private final HttpClient httpClient;

    @Activate
    public MerossCommandExtension(final @Reference ThingRegistry thingRegistry,
            final @Reference HttpClientFactory httpClientFactory) {
        super("meross", "Interact with the Meross binding");
        this.thingRegistry = thingRegistry;
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public void execute(String[] args, Console console) {
        if ((args.length < 1) || (args.length > 3)) {
            console.println("Invalid number of arguments");
            printUsage(console);
            return;
        }

        List<MerossBridgeHandler> bridgeHandlers = thingRegistry.stream()
                .filter(t -> THING_TYPE_GATEWAY.equals(t.getThingTypeUID()))
                .map(b -> ((MerossBridgeHandler) b.getHandler())).filter(Objects::nonNull).collect(Collectors.toList());
        if (bridgeHandlers.isEmpty()) {
            console.println("No Meross gateways configured");
            return;
        }

        if (!(DEVICES.equalsIgnoreCase(args[0]) || FINGERPRINT.equalsIgnoreCase(args[0]))) {
            console.println("Unsupported command '" + args[0] + "'");
            printUsage(console);
            return;
        }

        List<MerossBridgeHandler> handlers;
        if (args.length > 1) {
            handlers = bridgeHandlers.stream()
                    .filter(b -> args[1].equalsIgnoreCase(b.getThing().getConfiguration().get("userEmail").toString()))
                    .filter(Objects::nonNull).collect(Collectors.toList());
            if (handlers.isEmpty()) {
                console.println("No Meross bridge for user with email '" + args[1] + "'");
                printUsage(console);
                return;
            }
        } else {
            handlers = bridgeHandlers;
        }

        String deviceUuid = null;
        if (args.length > 2 && FINGERPRINT.equalsIgnoreCase(args[0])) {
            // Check if argument is a device uuid, if not try device name
            deviceUuid = handlers.get(0).getDevices().stream().map(Device::uuid).filter(uuid -> uuid.equals(args[2]))
                    .findFirst().orElse(null);
            deviceUuid = deviceUuid != null ? deviceUuid : handlers.get(0).getDevUUIDByDevName(args[2]);
        }

        if (DEVICES.equalsIgnoreCase(args[0])) {
            devices(console, handlers);
        } else if (FINGERPRINT.equalsIgnoreCase(args[0])) {
            fingerprint(console, handlers, deviceUuid);
        }
    }

    private void devices(Console console, List<MerossBridgeHandler> handlers) {
        boolean multipleAccount = handlers.size() > 1;
        for (MerossBridgeHandler handler : handlers) {
            if (multipleAccount) {
                console.println("### Account " + handler.getThing().getConfiguration().get("userEmail").toString());
            }

            Map<String, MerossDeviceHandler> deviceHandlers = thingRegistry.stream()
                    .filter(t -> DEVICE_THING_TYPES_UIDS.contains(t.getThingTypeUID()))
                    .map(d -> ((MerossDeviceHandler) d.getHandler())).filter(Objects::nonNull).collect(Collectors
                            .toMap(h -> h.getThing().getConfiguration().get("uuid").toString(), Function.identity()));

            List<Device> devices = handler.getDevices();
            if (devices.isEmpty()) {
                console.print("No devices found");
            }
            devices.forEach(device -> {
                console.print(String.format("%-25s: %s", device.devName(), device.uuid()));

                MerossDeviceHandler deviceHandler = deviceHandlers.get(device.uuid());
                if (deviceHandler != null) {
                    // If the device is already configured we also add the IP address to the output
                    String ipAddress = deviceHandler.getIpAddress();
                    if (ipAddress != null) {
                        console.print(String.format(" - %s", ipAddress));
                    }
                }

                console.println("");
            });
            if (multipleAccount) {
                console.println("### End account");
            }
        }
    }

    private void fingerprint(Console console, List<MerossBridgeHandler> handlers, @Nullable String deviceUuid) {
        String basePath = FINGERPRINT_ROOT_PATH + File.separator
                + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String path = nextPath(basePath, null);

        Map<String, MerossDeviceHandler> deviceHandlers = thingRegistry.stream()
                .filter(t -> DEVICE_THING_TYPES_UIDS.contains(t.getThingTypeUID()))
                .map(d -> ((MerossDeviceHandler) d.getHandler())).filter(Objects::nonNull).collect(Collectors
                        .toMap(h -> h.getThing().getConfiguration().get("uuid").toString(), Function.identity()));

        console.println("Generating fingerprint, please be patient...");
        console.println("# Start fingerprint");
        int accountNdx = 0;
        boolean multipleAccount = handlers.size() > 1;
        for (MerossBridgeHandler handler : handlers) {
            accountNdx++;
            if (multipleAccount) {
                console.println("### Account " + handler.getThing().getConfiguration().get("userEmail").toString());
            }
            MerossMqttConnector mqttConnector = handler.getMerossMqttConnector();
            if (!ThingStatus.ONLINE.equals(handler.getThing().getStatus()) || mqttConnector == null) {
                console.println("Meross bridge for account not online, cannot create fingerprint");
            } else {
                String accountPath = path + File.separator + "Account-" + accountNdx;
                List<Device> devices = handler.getDevices();
                if (devices.isEmpty()) {
                    console.print("No devices found");
                }
                devices.forEach(device -> {
                    if (deviceUuid == null || deviceUuid.isEmpty() || device.uuid().equals(deviceUuid)) {
                        DeviceHandlerCallback callback = new DeviceHandlerCallback();
                        MerossDeviceHandler deviceHandler = deviceHandlers.get(device.uuid());
                        if (deviceHandler != null) {
                            // If the device is already configured as a thing, we may have the IP address and can avoid
                            // a mqtt broker call
                            String ipAddress = deviceHandler.getIpAddress();
                            if (ipAddress != null) {
                                callback.setIpAddress(ipAddress);
                            }
                        }
                        MerossManager manager = new MerossManager(httpClient, mqttConnector, device.uuid(), callback);
                        try {
                            String deviceSpecs = manager.getDeviceSpecsCommand();
                            if (deviceSpecs.isEmpty()) {
                                console.println("###### Device " + device.deviceType() + ": " + device.uuid() + " - "
                                        + device.devName());
                                console.println("Empty device specifications");
                            } else {
                                console.println("###### Device " + device.deviceType() + ": " + device.uuid() + " - "
                                        + device.devName());
                                printAndSave(console, accountPath, device.deviceType() + "_" + device.devName(),
                                        deviceSpecs);
                            }
                        } catch (InterruptedException | ExecutionException | TimeoutException | MqttException e) {
                            console.println("###### Device " + device.deviceType() + ": " + device.uuid() + " - "
                                    + device.devName());
                            console.println("Could not retrieve device specifications");
                        }
                    }
                });
            }
            if (multipleAccount) {
                console.println("### End account " + handler.getThing().getConfiguration().get("userEmail").toString());
            }
        }

        try {
            String zipfile = nextPath(basePath, "zip");
            zipDirectory(Paths.get(path), Paths.get(zipfile));
            deleteDirectory(path);
            console.println("### Fingerprint has been written to zipfile: " + zipfile);
        } catch (IOException e) {
            console.println("Exception zipping fingerprint: " + e.getMessage());
            console.println("### Fingerprint has been written to files in directory: " + path);
        }

        console.println("# End fingerprint");
    }

    private void printAndSave(Console console, String path, String filename, String content) {
        String anonymized = ContentAnonymizer.anonymizeMessage(content);
        anonymized = anonymized != null ? anonymized : "";
        String json = prettyJson(anonymized);
        console.println(json);
        try {
            writeJsonToFile(path, filename, json);
        } catch (IOException e) {
            console.println("Exception writing to file: " + e.getMessage());
        }
    }

    private String nextPath(String pathString, @Nullable String extension) {
        String path = pathString + ((extension != null) ? ("." + extension) : "");
        int pathNdx = 1;
        while (Files.exists(Paths.get(path))) {
            path = pathString + "_" + pathNdx + ((extension != null) ? ("." + extension) : "");
            pathNdx++;
        }
        return path;
    }

    private String prettyJson(String json) {
        try {
            return GSON.toJson(JsonParser.parseString(json));
        } catch (JsonSyntaxException e) {
            // Keep the unformatted json if there is a syntax exception
            return json;
        }
    }

    private void writeJsonToFile(String pathString, String filename, String json) throws IOException {
        try {
            JsonElement element = JsonParser.parseString(json);
            if (element.isJsonNull() || (element.isJsonArray() && ((JsonArray) element).size() == 0)) {
                // Don't write a file if empty
                return;
            }
        } catch (JsonSyntaxException e) {
            // Just continue and write the file with non-valid json anyway
        }

        String path = nextPath(pathString + File.separator + filename, "json");

        // ensure full path exists
        File file = new File(path);
        Objects.requireNonNull(file.getParentFile()).mkdirs();

        final byte[] contents = json.getBytes(StandardCharsets.UTF_8);
        Files.write(file.toPath(), contents);
    }

    // Stackoverflow:
    // https://stackoverflow.com/questions/57997257/how-can-i-zip-a-complete-directory-with-all-subfolders-in-java
    private void zipDirectory(Path sourceDirectoryPath, Path zipPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
                ZipOutputStream zos = new ZipOutputStream(fos)) {
            Files.walkFileTree(sourceDirectoryPath, new SimpleFileVisitor<@Nullable Path>() {
                @Override
                public FileVisitResult visitFile(@Nullable Path file, @Nullable BasicFileAttributes attrs)
                        throws IOException {
                    zos.putNextEntry(new ZipEntry(sourceDirectoryPath.relativize(file).toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw e;
        }
    }

    private void deleteDirectory(String path) throws IOException {
        Files.walk(Paths.get(path)).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] { buildCommandUsage(DEVICES, "list all devices"),
                buildCommandUsage(DEVICES + " <userEmail>", "list all devices on account"),
                buildCommandUsage(FINGERPRINT, "generate fingerprint for all devices on all accounts"),
                buildCommandUsage(FINGERPRINT + " <userEmail>", "generate fingerprint for devices on account"),
                buildCommandUsage(FINGERPRINT + " <userEmail> <device>",
                        "generate fingerprint for a specific device with name or uuid on account") });
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
            return new StringsCompleter(
                    thingRegistry.stream().filter(t -> THING_TYPE_GATEWAY.equals(t.getThingTypeUID()))
                            .map(t -> t.getConfiguration().get("userEmail").toString()).toList(),
                    false).complete(args, cursorArgumentIndex, cursorPosition, candidates);
        } else if (FINGERPRINT.equalsIgnoreCase(args[0]) && cursorArgumentIndex == 2) {
            ThingHandler handler = thingRegistry.stream()
                    .filter(t -> THING_TYPE_GATEWAY.equals(t.getThingTypeUID())
                            && t.getConfiguration().get("userEmail").toString().equals(args[1]))
                    .map(t -> t.getHandler()).findFirst().orElse(null);
            if (handler != null) {
                return new StringsCompleter(((MerossBridgeHandler) handler).getDevices().stream()
                        .flatMap(device -> Stream.<String> of(device.devName(), device.uuid()))
                        .collect(Collectors.toSet()), false)
                        .complete(args, cursorArgumentIndex, cursorPosition, candidates);
            }
        }
        return false;
    }

    /**
     * This callback class allows the {@link MerossManager} to get and set the ipAddress retrieved in the communication.
     * This makes it possible to use local http calls once set.
     */
    private class DeviceHandlerCallback implements MerossDeviceHandlerCallback {

        private @Nullable String ipAddress;

        @Override
        public @Nullable String getIpAddress() {
            return ipAddress;
        }

        @Override
        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }
    }
}
