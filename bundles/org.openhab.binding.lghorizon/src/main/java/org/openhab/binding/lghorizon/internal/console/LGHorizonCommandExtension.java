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
package org.openhab.binding.lghorizon.internal.console;

import static org.openhab.binding.lghorizon.internal.LGHorizonBindingConstants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lghorizon.internal.LGHorizonContentAnonymizer;
import org.openhab.binding.lghorizon.internal.api.dto.CustomerDto.DeviceDto;
import org.openhab.binding.lghorizon.internal.api.dto.CustomerDto.DeviceDto.SettingsDto;
import org.openhab.binding.lghorizon.internal.api.dto.CustomerDto.ProfileDto;
import org.openhab.binding.lghorizon.internal.handler.LGHorizonAccountHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Console commands to inspect configured LG Horizon accounts/boxes and to capture anonymized REST/MQTT
 * content for later analysis without needing physical access to the box or provider in question.
 * <p>
 * Accounts and boxes are selected by their own identifiers ({@code customerId}, {@code deviceId}).
 * <p>
 * Two distinct, deliberately decoupled capture commands:
 * <ul>
 * <li>{@code fingerprint} - a quick, mostly-instant REST-only dump (customer/entitlements/channels/service
 * config), plus a passive wait for each box's next (or already-cached) {@code .../status} message</li>
 * <li>{@code capture} - an active, duration-based live-traffic recording for one specific box: every MQTT
 * status/uiStatus message, every REST call the binding makes in response (event/VOD/recording detail
 * lookups, the intent image URL lookup), and metadata (never the actual bytes) for every image fetch.
 * Requires the device to have an actual box thing already configured.</li>
 * </ul>
 *
 * @author Mark - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class LGHorizonCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String FINGERPRINT_ROOT_PATH = System.getProperty("user.home") + File.separator + BINDING_ID;

    private static final int CAPTURE_TIMEOUT_SECONDS = 10;
    private static final int MAX_LIVE_CAPTURE_DURATION_SECONDS = 300;

    private static final String ACCOUNTS = "accounts";
    private static final String PROFILES = "profiles";
    private static final String BOXES = "boxes";
    private static final String FINGERPRINT = "fingerprint";
    private static final String CAPTURE = "capture";
    private static final StringsCompleter CMD_COMPLETER = new StringsCompleter(
            List.of(ACCOUNTS, PROFILES, BOXES, FINGERPRINT, CAPTURE), false);

    private static final DateTimeFormatter CAPTURE_ENTRY_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final ThingRegistry thingRegistry;

    @Activate
    public LGHorizonCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super("lghorizon", "Interact with the Liberty Global Horizon binding");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length < 1) {
            console.println("Invalid number of arguments");
            printUsage(console);
            return;
        }

        List<LGHorizonAccountHandler> accountHandlers = allAccountHandlers();
        if (accountHandlers.isEmpty()) {
            console.println("No Liberty Global Horizon accounts configured");
            return;
        }

        String subCommand = args[0];

        if (CAPTURE.equalsIgnoreCase(subCommand)) {
            executeCapture(args, console, accountHandlers);
            return;
        }

        if (!(ACCOUNTS.equalsIgnoreCase(subCommand) || PROFILES.equalsIgnoreCase(subCommand)
                || BOXES.equalsIgnoreCase(subCommand) || FINGERPRINT.equalsIgnoreCase(subCommand))) {
            console.println("Unsupported command '" + subCommand + "'");
            printUsage(console);
            return;
        }
        if (args.length > 2) {
            console.println("Invalid number of arguments");
            printUsage(console);
            return;
        }

        if (ACCOUNTS.equalsIgnoreCase(subCommand)) {
            accounts(console, accountHandlers);
            return;
        }

        List<LGHorizonAccountHandler> handlers;
        if (args.length > 1) {
            String customerId = args[1];
            handlers = accountHandlers.stream().filter(h -> customerId.equalsIgnoreCase(h.getCustomerId()))
                    .collect(Collectors.toList());
            if (handlers.isEmpty()) {
                console.println("No Liberty Global Horizon account with customer id '" + args[1] + "'");
                printUsage(console);
                return;
            }
        } else {
            handlers = accountHandlers;
        }

        if (PROFILES.equalsIgnoreCase(subCommand)) {
            profiles(console, handlers);
        } else if (BOXES.equalsIgnoreCase(subCommand)) {
            boxes(console, handlers);
        } else {
            fingerprint(console, handlers);
        }
    }

    private void executeCapture(String[] args, Console console, List<LGHorizonAccountHandler> accountHandlers) {
        if (args.length != 2) {
            console.println("Usage: " + CAPTURE + " <durationSeconds>");
            return;
        }
        Integer duration = tryParseInt(args[1]);
        if (duration == null || duration < 1 || duration > MAX_LIVE_CAPTURE_DURATION_SECONDS) {
            console.println("Capture duration must be a whole number of seconds between 1 and "
                    + MAX_LIVE_CAPTURE_DURATION_SECONDS);
            return;
        }
        capture(console, accountHandlers, duration);
    }

    private static @Nullable Integer tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<LGHorizonAccountHandler> allAccountHandlers() {
        return thingRegistry.stream().filter(t -> THING_TYPE_ACCOUNT.equals(t.getThingTypeUID())).map(Thing::getHandler)
                .filter(LGHorizonAccountHandler.class::isInstance).map(LGHorizonAccountHandler.class::cast)
                .collect(Collectors.toList());
    }

    /** Label used in section headers: the customer id when known, falling back to the thing UID otherwise. */
    private String accountLabel(LGHorizonAccountHandler handler) {
        String customerId = handler.getCustomerId();
        return customerId != null ? customerId : handler.getThing().getUID().toString();
    }

    private void accounts(Console console, List<LGHorizonAccountHandler> handlers) {
        for (LGHorizonAccountHandler handler : handlers) {
            Thing thing = handler.getThing();
            String customerId = handler.getCustomerId();
            Object provider = thing.getConfiguration().get(CONFIG_PROVIDER);
            console.println(String.format("%-20s: %-12s provider=%-20s thingUID=%s",
                    customerId != null ? customerId : "(not yet known)", thing.getStatus(), provider, thing.getUID()));
        }
    }

    private List<Thing> boxThingsFor(LGHorizonAccountHandler handler) {
        ThingUID bridgeUid = handler.getThing().getUID();
        return thingRegistry.stream()
                .filter(t -> THING_TYPE_BOX.equals(t.getThingTypeUID()) && bridgeUid.equals(t.getBridgeUID()))
                .collect(Collectors.toList());
    }

    private void profiles(Console console, List<LGHorizonAccountHandler> handlers) {
        boolean multipleAccount = handlers.size() > 1;
        for (LGHorizonAccountHandler handler : handlers) {
            if (multipleAccount) {
                console.println("### Account " + accountLabel(handler));
            }

            List<ProfileDto> profiles = handler.getProfiles();
            if (profiles.isEmpty()) {
                console.println("No profiles found");
            }
            for (ProfileDto profile : profiles) {
                String profileId = profile.profileId;
                String name = profile.name;
                name = name != null ? name : "";
                console.println(String.format("%-30s: %s", profileId, name));
            }
        }
    }

    private void boxes(Console console, List<LGHorizonAccountHandler> handlers) {
        boolean multipleAccount = handlers.size() > 1;
        for (LGHorizonAccountHandler handler : handlers) {
            if (multipleAccount) {
                console.println("### Account " + accountLabel(handler));
            }
            Map<String, ThingUID> thingUidByDeviceId = new HashMap<>();
            for (Thing box : boxThingsFor(handler)) {
                Object deviceIdObj = box.getConfiguration().get(CONFIG_DEVICE_ID);
                if (deviceIdObj != null) {
                    thingUidByDeviceId.put(deviceIdObj.toString(), box.getUID());
                }
            }

            List<DeviceDto> boxes = handler.getAssignedDevices();
            if (boxes.isEmpty()) {
                console.println("No boxes found");
            }
            for (DeviceDto box : boxes) {
                String deviceId = box.deviceId;
                SettingsDto settings = box.settings;
                String friendlyName = settings != null ? settings.deviceFriendlyName : null;
                friendlyName = friendlyName != null ? friendlyName : "";
                ThingUID thingUid = deviceId != null ? thingUidByDeviceId.get(deviceId) : null;
                String thingInfo = thingUid != null ? thingUid.toString() : "(no thing)";
                console.println(String.format("%-30s: %-30s %s", deviceId, friendlyName, thingInfo));
            }
            if (multipleAccount) {
                console.println("### End account");
            }
        }
    }

    /**
     * A quick, mostly-instant REST-only snapshot (customer/entitlements/channels/service config), plus a
     * passive wait for each box's next (or already-cached) {@code .../status} message.
     */
    private void fingerprint(Console console, List<LGHorizonAccountHandler> handlers) {
        String basePath = FINGERPRINT_ROOT_PATH + File.separator
                + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String path = nextPath(basePath, null);

        console.println("Generating fingerprint, please be patient...");
        console.println("# Start fingerprint");
        int accountNdx = 0;
        boolean multipleAccount = handlers.size() > 1;
        for (LGHorizonAccountHandler handler : handlers) {
            accountNdx++;
            Thing accountThing = handler.getThing();
            if (multipleAccount) {
                console.println("### Account " + accountLabel(handler));
            }
            if (!ThingStatus.ONLINE.equals(accountThing.getStatus())) {
                console.println("LG Horizon account not online, cannot create fingerprint");
                if (multipleAccount) {
                    console.println("### End account");
                }
                continue;
            }
            String accountPath = path + File.separator + "Account-" + accountNdx;

            console.println("###### REST snapshot");
            Map<String, String> restSnapshot = handler.fetchDiagnosticRestSnapshot();
            if (restSnapshot.isEmpty()) {
                console.println("Could not retrieve any REST responses");
            }
            restSnapshot.forEach((name, json) -> printAndSave(console, accountPath, name, json));

            List<Thing> boxThings = boxThingsFor(handler);
            if (boxThings.isEmpty()) {
                console.println("No box things found");
            }
            for (Thing box : boxThings) {
                Object deviceIdObj = box.getConfiguration().get(CONFIG_DEVICE_ID);
                String deviceId = deviceIdObj == null ? "" : deviceIdObj.toString();
                console.println("###### Box " + box.getUID() + " - " + box.getLabel());
                captureAndSave(console, accountPath,
                        "box-status_" + LGHorizonContentAnonymizer.anonymizeDeviceId(deviceId),
                        handler.captureNextStatus(deviceId));
            }
            if (multipleAccount) {
                console.println("### End account " + accountLabel(handler));
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

    /**
     * Actively records live traffic across every configured account for a fixed window into a single, sequentially
     * appended log file. Every MQTT message, REST call, and image fetch across every account is captured,
     * unconditionally.
     */
    private void capture(Console console, List<LGHorizonAccountHandler> handlers, int durationSeconds) {
        String path = nextPath(FINGERPRINT_ROOT_PATH + File.separator + "capture-"
                + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE), "log");

        console.println("Capturing all live traffic across " + handlers.size() + " account(s) for " + durationSeconds
                + "s - interact with any box now (change channels, rewind, launch an app, "
                + "start a recording, ...)");
        console.println("Writing to: " + path);

        AtomicInteger sequence = new AtomicInteger();
        ExecutorService writer = Executors.newSingleThreadExecutor();
        List<BiConsumer<String, JsonObject>> mqttListeners = new ArrayList<>();

        for (LGHorizonAccountHandler handler : handlers) {
            BiConsumer<String, JsonObject> mqttListener = (topic, payload) -> {
                int index = sequence.incrementAndGet();
                String timestamp = LocalDateTime.now().format(CAPTURE_ENTRY_TIME_FORMAT);
                JsonObject wrapper = new JsonObject();
                wrapper.addProperty("topic", topic);
                wrapper.add("payload", payload);
                submitCaptureEntry(writer, console, path, index, timestamp, "MQTT", topic, wrapper.toString());
            };
            mqttListeners.add(mqttListener);
            handler.startLiveCapture(mqttListener);

            handler.startRestCapture((url, responseBody) -> {
                int index = sequence.incrementAndGet();
                String timestamp = LocalDateTime.now().format(CAPTURE_ENTRY_TIME_FORMAT);
                JsonObject wrapper = new JsonObject();
                wrapper.addProperty("url", url);
                try {
                    wrapper.add("response", JsonParser.parseString(responseBody));
                } catch (JsonSyntaxException e) {
                    wrapper.addProperty("response", responseBody);
                }
                submitCaptureEntry(writer, console, path, index, timestamp, "REST", url, responseBody);
            });
            handler.startImageCapture(description -> {
                int index = sequence.incrementAndGet();
                String timestamp = LocalDateTime.now().format(CAPTURE_ENTRY_TIME_FORMAT);
                submitCaptureEntry(writer, console, path, index, timestamp, "IMAGE", description, null);
            });
        }

        try {
            Thread.sleep(durationSeconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            for (int i = 0; i < handlers.size(); i++) {
                LGHorizonAccountHandler handler = handlers.get(i);
                handler.stopLiveCapture(mqttListeners.get(i));
                handler.stopRestCapture();
                handler.stopImageCapture();
            }
        }

        writer.shutdown();
        try {
            writer.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        console.println("Live capture complete (" + sequence.get() + " events captured)");
        console.println("Capture written to: " + path);
    }

    private void submitCaptureEntry(ExecutorService writer, Console console, String path, int index, String timestamp,
            String type, String rawLabel, @Nullable String rawBody) {
        writer.execute(() -> {
            String label = String.valueOf(LGHorizonContentAnonymizer.anonymizeTopic(rawLabel));
            String body = rawBody == null ? null : prettyJson(anonymizeOrEmpty(rawBody));

            StringBuilder entry = new StringBuilder();
            entry.append(String.format("--- #%03d %s %-5s %s ---%n", index, timestamp, type, label));
            if (body != null) {
                entry.append(body).append(System.lineSeparator());
            }
            entry.append(System.lineSeparator());
            String text = entry.toString();

            console.println(text);
            try {
                File file = new File(path);
                Objects.requireNonNull(file.getParentFile()).mkdirs();
                Files.writeString(Paths.get(path), text, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                console.println("Exception writing to capture file: " + e.getMessage());
            }
        });
    }

    private String anonymizeOrEmpty(String content) {
        String anonymized = LGHorizonContentAnonymizer.anonymizeMessage(content);
        return anonymized != null ? anonymized : "";
    }

    private void captureAndSave(Console console, String accountPath, String filename,
            CompletableFuture<JsonObject> future) {
        try {
            JsonObject captured = future.get(CAPTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            printAndSave(console, accountPath, filename, captured.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            console.println("Could not capture a message for " + filename + " within " + CAPTURE_TIMEOUT_SECONDS
                    + "s (box may be offline, or does not send this message type)");
        }
    }

    private void printAndSave(Console console, String path, String filename, String content) {
        String anonymized = LGHorizonContentAnonymizer.anonymizeMessage(content);
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
            // Keep the unformatted content if there is a syntax exception
            return json;
        }
    }

    private void writeJsonToFile(String pathString, String filename, String json) throws IOException {
        try {
            JsonElement element = JsonParser.parseString(json);
            if (element.isJsonNull() || (element.isJsonArray() && ((JsonArray) element).isEmpty())) {
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
        }
    }

    private void deleteDirectory(String path) throws IOException {
        Files.walk(Paths.get(path)).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(buildCommandUsage(ACCOUNTS, "list all configured Liberty Global Horizon accounts"),
                buildCommandUsage(PROFILES, "list all profiles on all accounts"),
                buildCommandUsage(PROFILES + " <customerId>", "list all profiles on one account"),
                buildCommandUsage(BOXES, "list all boxes on all accounts, with their box thing UID if configured"),
                buildCommandUsage(BOXES + " <customerId>", "list all boxes on one account"),
                buildCommandUsage(FINGERPRINT,
                        "quick REST-only snapshot (+ each box's last known status) for all accounts"),
                buildCommandUsage(FINGERPRINT + " <customerId>", "quick REST-only snapshot for one account"),
                buildCommandUsage(CAPTURE + " <durationSeconds>",
                        "actively record live MQTT/REST traffic across every configured account over a fixed duration"));
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        if (cursorArgumentIndex <= 0) {
            return CMD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
        }
        if (CAPTURE.equalsIgnoreCase(args[0])) {
            return false;
        }
        List<LGHorizonAccountHandler> allHandlers = allAccountHandlers();
        if (cursorArgumentIndex == 1) {
            List<String> customerIds = allHandlers.stream().map(LGHorizonAccountHandler::getCustomerId)
                    .filter(Objects::nonNull).map(Objects::requireNonNull).toList();
            return new StringsCompleter(customerIds, false).complete(args, cursorArgumentIndex, cursorPosition,
                    candidates);
        }
        return false;
    }
}
