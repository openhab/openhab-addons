/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.dto.network.NetworkException;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleBase;
import org.openhab.binding.mybmw.internal.handler.MyBMWBridgeHandler;
import org.openhab.binding.mybmw.internal.handler.backend.ResponseContentAnonymizer;
import org.openhab.binding.mybmw.internal.utils.BimmerConstants;
import org.openhab.core.io.console.Console;
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

/**
 * The {@link MyBMWCommandExtension} is responsible for handling console commands
 *
 * @author Mark Herwege - Initial contribution
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class MyBMWCommandExtension extends AbstractConsoleCommandExtension {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String FINGERPRINT_ROOT_PATH = System.getProperty("user.home") + File.separator + BINDING_ID;
    private static final String FINGERPRINT = "fingerprint";

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

        List<MyBMWBridgeHandler> bridgeHandlers = thingRegistry.getAll().stream()
                .filter(t -> t.getHandler() instanceof MyBMWBridgeHandler)
                .map(b -> ((MyBMWBridgeHandler) b.getHandler())).collect(Collectors.toList());
        if (bridgeHandlers.isEmpty()) {
            console.println("No account bridges configured");
            return;
        }

        if (!FINGERPRINT.equals(args[0])) {
            console.println("Unsupported command '" + args[0] + "'");
            printUsage(console);
            return;
        }

        List<MyBMWBridgeHandler> handlers;
        if (args.length > 1) {
            handlers = bridgeHandlers.stream()
                    .filter(b -> args[1].equals(b.getThing().getConfiguration().get("userName")))
                    .collect(Collectors.toList());
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
            console.println("### Account '" + handler.getThing().getConfiguration().get("userName") + "'");
            if (!ThingStatus.ONLINE.equals(handler.getThing().getStatus())) {
                console.println("MyBMW bridge for account not online, cannot create fingerprint");
            } else {
                accountNdx++;
                String accountPath = path + File.separator + "Account-" + String.valueOf(accountNdx);
                handler.getProxy().ifPresentOrElse(prox -> {
                    // get list of vehicles
                    List<@NonNull VehicleBase> vehicles = null;
                    try {
                        vehicles = prox.requestVehiclesBase();

                        for (String brand : BimmerConstants.REQUESTED_BRANDS) {
                            console.println("###### Vehicles base for brand " + brand);
                            writeJsonToFile(accountPath, "VehicleBase_" + brand, ResponseContentAnonymizer
                                    .anonymizeResponseContent(prox.requestVehiclesBaseJson(brand)));
                        }

                        if (args.length == 3) {
                            Optional<VehicleBase> vehicleOptional = vehicles.stream()
                                    .filter(v -> v.getVin().toLowerCase().equals(args[2].toLowerCase())).findAny();
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
                            console.println("###### Vehicle '" + vehicleBase.getVin() + "'");
                            // get state
                            console.println("######## Vehicle state");
                            writeJsonToFile(vinPath, "VehicleState",
                                    ResponseContentAnonymizer.anonymizeResponseContent(prox.requestVehicleStateJson(
                                            vehicleBase.getVin(), vehicleBase.getAttributes().getBrand())));

                            // get charge statistics -> only successful for electric vehicles
                            console.println("######### Vehicle charging statistics");
                            writeJsonToFile(vinPath, "VehicleChargingStatistics",
                                    ResponseContentAnonymizer.anonymizeResponseContent(prox.requestChargeStatisticsJson(
                                            vehicleBase.getVin(), vehicleBase.getAttributes().getBrand())));

                            console.println("######### Vehicle charging sessions");
                            writeJsonToFile(vinPath, "VehicleChargingSessions",
                                    ResponseContentAnonymizer.anonymizeResponseContent(prox.requestChargeSessionsJson(
                                            vehicleBase.getVin(), vehicleBase.getAttributes().getBrand())));
                        }
                    } catch (NetworkException e) {
                        console.println("Fingerprint failed, network exception: " + e.getReason());
                    } catch (IOException e) {
                        console.println("Fingerprint failed, could not write to file");
                    }
                }, () -> {
                    console.println("MyBMW bridge with id '" + handler.getThing().getUID().getId()
                            + "', communication not started, cannot retrieve fingerprint");
                });
            }
        }

        try {
            String zipfile = nextPath(basePath, "zip");
            zipDirectory(Paths.get(path), Paths.get(zipfile));
            deleteDirectory(path);
            console.println("### Fingerprint has been written to zipfile: " + zipfile);
        } catch (IOException e) {
            console.println("### Exception zipping fingerprint");
            console.println("### Fingerprint has been written to files in directory: " + path);
        }

        console.println("# End fingerprint");
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

    private void writeJsonToFile(String pathString, String filename, String json) throws IOException {
        JsonElement element = JsonParser.parseString(json);
        if (element.isJsonNull() || (element.isJsonArray() && ((JsonArray) element).isEmpty())) {
            // Don't write a file if empty
            return;
        }

        String path = nextPath(pathString + File.separator + filename, "json");

        // ensure full path exists
        File file = new File(path);
        file.getParentFile().mkdirs();

        final byte[] contents = GSON.toJson(element).getBytes(StandardCharsets.UTF_8);
        Files.write(file.toPath(), contents);
    }

    // Stackoverflow:
    // https://stackoverflow.com/questions/57997257/how-can-i-zip-a-complete-directory-with-all-subfolders-in-java
    private void zipDirectory(Path sourceDirectoryPath, Path zipPath) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()));
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
        zos.close();
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
}
