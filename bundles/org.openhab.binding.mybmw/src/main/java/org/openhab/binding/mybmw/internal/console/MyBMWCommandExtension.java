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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
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

/**
 * The {@link MyBMWCommandExtension} is responsible for handling console commands
 *
 * @author Mark Herwege - Initial contribution
 */

@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class MyBMWCommandExtension extends AbstractConsoleCommandExtension {

    private static final String ACCOUNTS = "accounts";
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

        if (ACCOUNTS.equals(args[0])) {
            if (args.length == 1) {
                bridgeHandlers.forEach(b -> console.printf("%s - %s%n", b.getThing().getUID().getId(),
                        b.getThing().getConfiguration().get("userName")));
            } else {
                console.println("No extra argument allowed after 'accounts'");
                printUsage(console);
            }
        } else if (FINGERPRINT.equals(args[0])) {
            List<MyBMWBridgeHandler> handlers;
            if (args.length > 1) {
                Optional<MyBMWBridgeHandler> bridgeOptional = bridgeHandlers.stream()
                        .filter(b -> b.getThing().getUID().getId().equals(args[1])).findAny();
                if (bridgeOptional.isEmpty()) {
                    console.println("'" + args[1] + "' is not a valid id for a myBMW account bridge");
                    printUsage(console);
                    return;
                }
                handlers = List.of(bridgeOptional.get());
            } else {
                handlers = bridgeHandlers;
            }

            console.println("# Start fingerprint");
            int accountNdx = 0;
            for (MyBMWBridgeHandler handler : handlers) {
                accountNdx++;
                console.println("### Start account " + String.valueOf(accountNdx));
                if (!ThingStatus.ONLINE.equals(handler.getThing().getStatus())) {
                    console.println("MyBMW bridge for account not online, cannot create fingerprint");
                } else {
                    handler.getProxy().ifPresentOrElse(prox -> {
                        // get list of vehicles
                        List<@NonNull VehicleBase> vehicles = null;
                        try {
                            vehicles = prox.requestVehiclesBase();

                            for (String brand : BimmerConstants.REQUESTED_BRANDS) {
                                console.println("###### Vehicles base for brand " + brand);
                                console.println(ResponseContentAnonymizer
                                        .anonymizeResponseContent(prox.requestVehiclesBaseJson(brand)));
                            }

                            if (args.length == 3) {
                                Optional<VehicleBase> vehicleOptional = vehicles.stream()
                                        .filter(v -> v.getVin().toLowerCase().equals(args[2].toLowerCase())).findAny();
                                if (vehicleOptional.isEmpty()) {
                                    console.println(
                                            "'" + args[2] + "' is not a valid vin on the account bridge with id '"
                                                    + handler.getThing().getUID().getId() + "'");
                                    printUsage(console);
                                    return;
                                }
                                vehicles = List.of(vehicleOptional.get());
                            }

                            int vehicleNdx = 0;
                            for (VehicleBase vehicleBase : vehicles) {
                                vehicleNdx++;
                                console.println("###### Start vehicle " + String.valueOf(vehicleNdx));
                                // get state
                                console.println("######## Vehicle state");
                                console.println(
                                        ResponseContentAnonymizer.anonymizeResponseContent(prox.requestVehicleStateJson(
                                                vehicleBase.getVin(), vehicleBase.getAttributes().getBrand())));

                                // get charge statistics -> only successful for electric vehicles
                                console.println("######### Vehicle charging statistics");
                                console.println(ResponseContentAnonymizer
                                        .anonymizeResponseContent(prox.requestChargeStatisticsJson(vehicleBase.getVin(),
                                                vehicleBase.getAttributes().getBrand())));

                                console.println("######### Vehicle charging sessions");
                                console.println(ResponseContentAnonymizer
                                        .anonymizeResponseContent(prox.requestChargeSessionsJson(vehicleBase.getVin(),
                                                vehicleBase.getAttributes().getBrand())));
                                console.println("###### End vehicle " + String.valueOf(vehicleNdx));
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
            console.println("# End fingerprint");
        } else {
            console.println("Unsupported command '" + args[0] + "'");
            printUsage(console);
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] { buildCommandUsage(ACCOUNTS, "list bridgeIds for accounts"),
                buildCommandUsage(FINGERPRINT, "generate fingerprint for all vehicles on all account bridges"),
                buildCommandUsage(FINGERPRINT + " <bridgeId>", "generate fingerprint for vehicles on account bridge"),
                buildCommandUsage(FINGERPRINT + " <bridgeId> <vin>",
                        "generate fingerprint for vehicle with vin on account bridge") });
    }
}
