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
package org.openhab.binding.boschshc.internal.console;

import static org.openhab.binding.boschshc.internal.discovery.ThingDiscoveryService.DEVICEMODEL_TO_THINGTYPE_MAP;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.PublicInformation;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Console command to list Bosch SHC devices and openhab support.
 * <p>
 * Uses the SHC API to get all SHC devices and SHC services and tries to lookup
 * openHAB devices and implemented service classes. Prints each name and
 * looked-up implementation on console.
 *
 * @author Gerd Zanker - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class BoschShcCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {

    static final String SHOW_BINDINGINFO = "showBindingInfo";
    static final String SHOW_DEVICES = "showDevices";
    static final String SHOW_SERVICES = "showServices";

    static final String GET_BRIDGEINFO = "bridgeInfo";
    static final String GET_DEVICES = "deviceInfo";
    private static final StringsCompleter SUBCMD_COMPLETER = new StringsCompleter(
            List.of(SHOW_BINDINGINFO, SHOW_DEVICES, SHOW_SERVICES, GET_BRIDGEINFO, GET_DEVICES), false);

    private final ThingRegistry thingRegistry;

    @Activate
    public BoschShcCommandExtension(final @Reference ThingRegistry thingRegistry) {
        super(BoschSHCBindingConstants.BINDING_ID, "Interact with the Bosch Smart Home Controller.");
        this.thingRegistry = thingRegistry;
    }

    /**
     * Returns all implemented services of this Bosch SHC binding.
     * <p>
     * This list shall contain all available services and needs to be extended when
     * a new service is added. A unit tests checks if this list matches with the
     * existing subfolders in
     * <code>src/main/java/org/openhab/binding/boschshc/internal/services</code>.
     */
    List<String> getAllBoschShcServices() {
        return List.of("airqualitylevel", "alarm", "batterylevel", "binaryswitch", "bypass", "cameranotification",
                "childlock", "childprotection", "communicationquality", "hsbcoloractuator", "humiditylevel",
                "illuminance", "impulseswitch", "intrusion", "keypad", "latestmotion", "multilevelswitch", "powermeter",
                "powerswitch", "privacymode", "roomclimatecontrol", "shuttercontact", "shuttercontrol", "silentmode",
                "smokedetectorcheck", "temperaturelevel", "userstate", "valvetappet", "vibrationsensor",
                "waterleakagesensor", "waterleakagesensorcheck", "waterleakagesensortilt");
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 0) {
            printUsage(console);
            return;
        }
        try {
            if (GET_BRIDGEINFO.equals(args[0])) {
                console.print(buildBridgeInfo());
                return;
            }
            if (GET_DEVICES.equals(args[0])) {
                console.print(buildDeviceInfo());
                return;
            }
            if (SHOW_BINDINGINFO.equals(args[0])) {
                console.print(buildBindingInfo());
                return;
            }
            if (SHOW_DEVICES.equals(args[0])) {
                console.print(buildSupportedDeviceStatus());
                return;
            }
            if (SHOW_SERVICES.equals(args[0])) {
                console.print(buildSupportedServiceStatus());
                return;
            }
        } catch (BoschSHCException | ExecutionException | TimeoutException e) {
            console.print(String.format("Error %1s%n", e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // unsupported command, print usage
        printUsage(console);
    }

    private List<BridgeHandler> getBridgeHandlers() {
        List<BridgeHandler> bridges = new ArrayList<>();
        for (Thing thing : thingRegistry.getAll()) {
            ThingHandler thingHandler = thing.getHandler();
            if (thingHandler instanceof BridgeHandler bridgeHandler) {
                bridges.add(bridgeHandler);
            }
        }
        return bridges;
    }

    String buildBridgeInfo() throws BoschSHCException, InterruptedException, ExecutionException, TimeoutException {
        List<BridgeHandler> bridges = getBridgeHandlers();
        StringBuilder builder = new StringBuilder();
        for (BridgeHandler bridgeHandler : bridges) {
            builder.append(String.format("Bridge: %1s%n", bridgeHandler.getThing().getLabel()));
            builder.append(String.format("  access possible: %1s%n", bridgeHandler.checkBridgeAccess()));

            PublicInformation publicInformation = bridgeHandler.getPublicInformation();
            builder.append(String.format("  SHC Generation: %1s%n", publicInformation.shcGeneration));
            builder.append(String.format("  IP Address: %1s%n", publicInformation.shcIpAddress));
            builder.append(String.format("  API Versions: %1s%n", publicInformation.apiVersions));
            builder.append(String.format("  Software Version: %1s%n",
                    publicInformation.softwareUpdateState.swInstalledVersion));
            builder.append(String.format("  Version Update State: %1s%n",
                    publicInformation.softwareUpdateState.swUpdateState));
            builder.append(String.format("  Available Version: %1s%n",
                    publicInformation.softwareUpdateState.swUpdateAvailableVersion));
            builder.append(String.format("%n"));
        }
        return builder.toString();
    }

    String buildDeviceInfo() throws InterruptedException {
        StringBuilder builder = new StringBuilder();
        for (Thing thing : thingRegistry.getAll()) {
            ThingHandler thingHandler = thing.getHandler();
            if (thingHandler instanceof BridgeHandler bridgeHandler) {
                builder.append(String.format("thing: %1s%n", thing.getLabel()));
                builder.append(String.format("  thingHandler: %1s%n", thingHandler.getClass().getName()));
                builder.append(String.format("bridge access possible: %1s%n", bridgeHandler.checkBridgeAccess()));

                List<Device> devices = bridgeHandler.getDevices();
                builder.append(String.format("devices (%1d): %n", devices.size()));
                for (Device device : devices) {
                    builder.append(buildDeviceInfo(device));
                    builder.append(String.format("%n"));
                }
            }
        }
        return builder.toString();
    }

    private String buildDeviceInfo(Device device) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("  deviceID: %1s%n", device.id));
        builder.append(String.format("      type: %1s -> ", device.deviceModel));

        ThingTypeUID thingTypeUID = DEVICEMODEL_TO_THINGTYPE_MAP.get(device.deviceModel);
        if (thingTypeUID != null) {
            builder.append(thingTypeUID.getId());
        } else {
            builder.append("!UNSUPPORTED!");
        }

        builder.append(String.format("%n"));
        builder.append(buildDeviceServices(device.deviceServiceIds));
        return builder.toString();
    }

    private String buildDeviceServices(List<String> deviceServiceIds) {
        StringBuilder builder = new StringBuilder();
        List<String> existingServices = getAllBoschShcServices();
        for (String serviceName : deviceServiceIds) {
            builder.append(String.format("            service: %1s -> ", serviceName));

            if (existingServices.stream().anyMatch(s -> s.equals(serviceName.toLowerCase()))) {
                for (String existingService : existingServices) {
                    if (existingService.equals(serviceName.toLowerCase())) {
                        builder.append(existingService);
                    }
                }
            } else {
                builder.append("!UNSUPPORTED!");
            }
            builder.append(String.format("%n"));
        }
        return builder.toString();
    }

    String buildBindingInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Bosch SHC Binding%n"));
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        if (bundle != null) {
            builder.append(String.format("  SymbolicName %1s%n", bundle.getSymbolicName()));
            builder.append(String.format("  Version %1s%n", bundle.getVersion()));
        }
        return builder.toString();
    }

    String buildSupportedDeviceStatus() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Supported Devices (%1d):%n", DEVICEMODEL_TO_THINGTYPE_MAP.size()));
        for (Map.Entry<String, ThingTypeUID> entry : DEVICEMODEL_TO_THINGTYPE_MAP.entrySet()) {
            builder.append(
                    String.format(" - %1s = %1s%n", entry.getKey(), DEVICEMODEL_TO_THINGTYPE_MAP.get(entry.getKey())));
        }
        return builder.toString();
    }

    String buildSupportedServiceStatus() {
        StringBuilder builder = new StringBuilder();
        List<String> supportedServices = getAllBoschShcServices();
        builder.append(String.format("Supported Services (%1d):%n", supportedServices.size()));
        for (String service : supportedServices) {
            builder.append(String.format(" - %1s%n", service));
        }
        return builder.toString();
    }

    @Override
    public List<String> getUsages() {
        return List.of(buildCommandUsage(SHOW_BINDINGINFO, "list detailed information about this binding"),
                buildCommandUsage(SHOW_DEVICES, "list all devices supported by this binding"),
                buildCommandUsage(SHOW_SERVICES, "list all services supported by this binding"),
                buildCommandUsage(GET_DEVICES, "get all Bosch SHC devices"),
                buildCommandUsage(GET_BRIDGEINFO, "get detailed information from Bosch SHC"));
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        return cursorArgumentIndex <= 0
                && SUBCMD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
    }
}
