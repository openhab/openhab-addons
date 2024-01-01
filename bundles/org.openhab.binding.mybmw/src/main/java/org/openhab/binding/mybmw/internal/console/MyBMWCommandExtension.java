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
package org.openhab.binding.mybmw.internal.console;

import static org.openhab.binding.mybmw.internal.MyBMWConstants.BINDING_ID;
import static org.openhab.binding.mybmw.internal.MyBMWConstants.THING_TYPE_CONNECTED_DRIVE_ACCOUNT;

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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleBase;
import org.openhab.binding.mybmw.internal.handler.MyBMWBridgeHandler;
import org.openhab.binding.mybmw.internal.handler.backend.NetworkException;
import org.openhab.binding.mybmw.internal.handler.backend.ResponseContentAnonymizer;
import org.openhab.binding.mybmw.internal.utils.BimmerConstants;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
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
 * The {@link MyBMWCommandExtension} is responsible for handling console commands
 *
 * @author Mark Herwege - Initial contribution
 * @author Martin Grassl - improved exception handling
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class MyBMWCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String FINGERPRINT_ROOT_PATH = System.getProperty("user.home") + File.separator + BINDING_ID;

    private static final String FINGERPRINT = "fingerprint";
    private static final StringsCompleter CMD_COMPLETER = new StringsCompleter(List.of(FINGERPRINT), false);

    private final ThingRegistry thingRegistry;

    @Activate
    public MyBMWCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super("mybmw", "Interact with the MyBMW binding");
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if ((args.length < 1) || (args.length > 3)) {
            console.println("Invalid number of arguments");
            printUsage(console);
            return;
        }

        List<MyBMWBridgeHandler> bridgeHandlers = thingRegistry.stream()
                .filter(t -> THING_TYPE_CONNECTED_DRIVE_ACCOUNT.equals(t.getThingTypeUID()))
                .map(b -> ((MyBMWBridgeHandler) b.getHandler())).filter(Objects::nonNull).collect(Collectors.toList());
        if (bridgeHandlers.isEmpty()) {
            console.println("No account bridges configured");
            return;
        }

        if (!FINGERPRINT.equalsIgnoreCase(args[0])) {
            console.println("Unsupported command '" + args[0] + "'");
            printUsage(console);
            return;
        }

        List<MyBMWBridgeHandler> handlers;
        if (args.length > 1) {
            handlers = bridgeHandlers.stream()
                    .filter(b -> args[1].equalsIgnoreCase(b.getThing().getConfiguration().get("userName").toString()))
                    .filter(Objects::nonNull).collect(Collectors.toList());
            if (handlers.isEmpty()) {
                console.println("No myBMW account bridge for user '" + args[1] + "'");
                printUsage(console);
                return;
            }
        } else {
            handlers = bridgeHandlers;
        }

        String basePath = FINGERPRINT_ROOT_PATH + File.separator
                + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String path = nextPath(basePath, null);

        console.println("# Start fingerprint");
        int accountNdx = 0;
        for (MyBMWBridgeHandler handler : handlers) {
            accountNdx++;
            console.println("### Account " + String.valueOf(accountNdx));
            if (!ThingStatus.ONLINE.equals(handler.getThing().getStatus())) {
                console.println("MyBMW bridge for account not online, cannot create fingerprint");
            } else {
                String accountPath = path + File.separator + "Account-" + String.valueOf(accountNdx);
                handler.getMyBmwProxy().ifPresentOrElse(prox -> {
                    // get list of vehicles
                    List<@NonNull VehicleBase> vehicles = null;
                    try {
                        vehicles = prox.requestVehiclesBase();

                        for (String brand : BimmerConstants.REQUESTED_BRANDS) {
                            console.println("###### Vehicles base for brand " + brand);
                            printAndSave(console, accountPath, "VehicleBase_" + brand,
                                    prox.requestVehiclesBaseJson(brand));
                        }

                        if (args.length == 3) {
                            Optional<VehicleBase> vehicleOptional = vehicles.stream()
                                    .filter(v -> v.getVin().equalsIgnoreCase(args[2])).findAny();
                            if (vehicleOptional.isEmpty()) {
                                console.println("'" + args[2] + "' is not a valid vin on the account bridge with id '"
                                        + handler.getThing().getUID().getId() + "'");
                                printUsage(console);
                                return;
                            }
                            vehicles = List.of(vehicleOptional.get());
                        }

                        int vinNdx = 0;
                        for (VehicleBase vehicleBase : vehicles) {
                            vinNdx++;
                            String vinPath = accountPath + File.separator + "Vin-" + String.valueOf(vinNdx);
                            console.println("###### Vehicle " + String.valueOf(vinNdx));

                            // get state
                            console.println("######## Vehicle state");
                            printAndSave(console, vinPath, "VehicleState", prox.requestVehicleStateJson(
                                    vehicleBase.getVin(), vehicleBase.getAttributes().getBrand()));

                            // get charge statistics -> only successful for electric vehicles
                            console.println("######### Vehicle charging statistics");
                            printAndSave(console, vinPath, "VehicleChargingStatistics",
                                    prox.requestChargeStatisticsJson(vehicleBase.getVin(),
                                            vehicleBase.getAttributes().getBrand()));

                            // get charge sessions -> only successful for electric vehicles
                            console.println("######### Vehicle charging sessions");
                            printAndSave(console, vinPath, "VehicleChargingSessions", prox.requestChargeSessionsJson(
                                    vehicleBase.getVin(), vehicleBase.getAttributes().getBrand()));

                            console.println("###### End vehicle " + String.valueOf(vinNdx));
                        }
                    } catch (NetworkException e) {
                        console.println("Fingerprint failed, network exception: " + e.getReason());
                    }
                }, () -> {
                    console.println("MyBMW bridge with id '" + handler.getThing().getUID().getId()
                            + "', communication not started, cannot retrieve fingerprint");
                });
            }
            console.println("### End account " + String.valueOf(accountNdx));
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

    private void printAndSave(Console console, String path, String filename, String content) throws NetworkException {
        String json = prettyJson(ResponseContentAnonymizer.anonymizeResponseContent(content));
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
            path = pathString + "_" + String.valueOf(pathNdx) + ((extension != null) ? ("." + extension) : "");
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
        file.getParentFile().mkdirs();

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
        return Arrays.asList(
                new String[] { buildCommandUsage(FINGERPRINT, "generate fingerprint for all vehicles on all accounts"),
                        buildCommandUsage(FINGERPRINT + " <userName>", "generate fingerprint for vehicles on account"),
                        buildCommandUsage(FINGERPRINT + " <userName> <vin>",
                                "generate fingerprint for vehicle with vin on account") });
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        try {
            if (cursorArgumentIndex <= 0) {
                return CMD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
            } else if (cursorArgumentIndex == 1) {
                return new StringsCompleter(
                        thingRegistry.stream()
                                .filter(t -> THING_TYPE_CONNECTED_DRIVE_ACCOUNT.equals(t.getThingTypeUID()))
                                .map(t -> t.getConfiguration().get("userName").toString()).collect(Collectors.toList()),
                        false).complete(args, cursorArgumentIndex, cursorPosition, candidates);
            } else if (cursorArgumentIndex == 2) {
                MyBMWBridgeHandler handler = (MyBMWBridgeHandler) thingRegistry.stream()
                        .filter(t -> THING_TYPE_CONNECTED_DRIVE_ACCOUNT.equals(t.getThingTypeUID())
                                && args[1].equals(t.getConfiguration().get("userName")))
                        .map(t -> t.getHandler()).findAny().get();
                List<VehicleBase> vehicles = handler.getMyBmwProxy().get().requestVehiclesBase();
                return new StringsCompleter(
                        vehicles.stream().map(v -> v.getVin()).filter(Objects::nonNull).collect(Collectors.toList()),
                        false).complete(args, cursorArgumentIndex, cursorPosition, candidates);
            }
        } catch (NoSuchElementException | NetworkException e) {
            return false;
        }
        return false;
    }
}
