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
package org.openhab.binding.tuya.internal.console;

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.BINDING_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.THING_TYPE_TUYA_DEVICE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.SchemaReloadException;
import org.openhab.binding.tuya.internal.TuyaSchemaDB;
import org.openhab.binding.tuya.internal.TuyaSchemaService;
import org.openhab.binding.tuya.internal.config.DeviceConfiguration;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.ConsoleCommandCompleter;
import org.openhab.core.io.console.StringsCompleter;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingManager;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link TuyaCommandExtension} provides console commands for inspecting and reloading device schemas.
 *
 * @author Carlo Dischler - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class TuyaCommandExtension extends AbstractConsoleCommandExtension implements ConsoleCommandCompleter {
    private static final String SUBCMD_RELOAD = "reload";
    private static final String SUBCMD_SCHEMA = "schema";
    private static final StringsCompleter SUBCMD_COMPLETER = new StringsCompleter(List.of(SUBCMD_RELOAD, SUBCMD_SCHEMA),
            false);

    private final ThingRegistry thingRegistry;
    private final ThingManager thingManager;
    private final TuyaSchemaService schemaService;

    @Activate
    public TuyaCommandExtension(final @Reference ThingRegistry thingRegistry,
            final @Reference ThingManager thingManager, final @Reference TuyaSchemaService schemaService) {
        super(BINDING_ID, "Interact with the Tuya binding.");
        this.thingRegistry = thingRegistry;
        this.thingManager = thingManager;
        this.schemaService = schemaService;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length != 2) {
            printUsage(console);
            return;
        }

        switch (args[0]) {
            case SUBCMD_RELOAD -> reloadSchema(args[1], console);
            case SUBCMD_SCHEMA -> printSchema(args[1], console);
            default -> printUsage(console);
        }
    }

    @Override
    public List<String> getUsages() {
        return List.of(buildCommandUsage(SUBCMD_SCHEMA + " <productId>", "show the stored schema of a product"),
                buildCommandUsage(SUBCMD_RELOAD + " <thingUID>",
                        "reload the schema of a device from the cloud and re-initialize the things using it"));
    }

    @Override
    public @Nullable ConsoleCommandCompleter getCompleter() {
        return this;
    }

    @Override
    public boolean complete(String[] args, int cursorArgumentIndex, int cursorPosition, List<String> candidates) {
        if (cursorArgumentIndex <= 0) {
            return SUBCMD_COMPLETER.complete(args, cursorArgumentIndex, cursorPosition, candidates);
        }
        if (cursorArgumentIndex == 1) {
            if (SUBCMD_RELOAD.equals(args[0])) {
                return new StringsCompleter(deviceThings().map(thing -> thing.getUID().getAsString()).toList(), true)
                        .complete(args, cursorArgumentIndex, cursorPosition, candidates);
            } else if (SUBCMD_SCHEMA.equals(args[0])) {
                return new StringsCompleter(deviceThings().map(TuyaCommandExtension::productIdOf)
                        .filter(productId -> !productId.isBlank()).distinct().toList(), true)
                        .complete(args, cursorArgumentIndex, cursorPosition, candidates);
            }
        }
        return false;
    }

    private void printSchema(String productId, Console console) {
        Map<String, SchemaDp> schema = TuyaSchemaDB.get(productId);
        if (schema == null) {
            console.println("No schema stored for product '" + productId + "'.");
            return;
        }

        console.println("Schema of product '" + productId + "' ("
                + (TuyaSchemaDB.isBuiltIn(productId) ? "built into the binding" : "retrieved from the cloud") + "):");
        console.println(String.format("%4s  %-32s %-8s %-10s %s", "DP", "Code", "Type", "Access", "Details"));
        schema.values().forEach(schemaDp -> console.println(String.format("%4d  %-32s %-8s %-10s %s", schemaDp.id,
                schemaDp.code, schemaDp.type, schemaDp.readOnly ? "read-only" : "read/write", details(schemaDp))));
    }

    private static String details(SchemaDp schemaDp) {
        List<String> details = new ArrayList<>();
        if (!schemaDp.label.isBlank()) {
            details.add("label=\"" + schemaDp.label + "\"");
        }
        if (!schemaDp.unit.isEmpty()) {
            details.add("unit=" + schemaDp.unit);
        }
        Double min = schemaDp.min;
        if (min != null) {
            details.add("min=" + min);
        }
        Double max = schemaDp.max;
        if (max != null) {
            details.add("max=" + max);
        }
        if (schemaDp.step.compareTo(BigDecimal.ONE) != 0) {
            details.add("step=" + schemaDp.step);
        }
        if (schemaDp.scale != 0) {
            details.add("scale=" + schemaDp.scale);
        }
        List<String> range = schemaDp.range;
        if (range != null && !range.isEmpty()) {
            details.add("range=" + String.join(",", range));
        }
        return String.join(" ", details);
    }

    private void reloadSchema(String thingUidString, Console console) {
        ThingUID thingUID;
        try {
            thingUID = new ThingUID(thingUidString);
        } catch (IllegalArgumentException e) {
            console.println("'" + thingUidString + "' is not a valid thing UID.");
            return;
        }

        Thing thing = thingRegistry.get(thingUID);
        if (thing == null || !THING_TYPE_TUYA_DEVICE.equals(thing.getThingTypeUID())) {
            console.println("'" + thingUID + "' is not a Tuya device thing.");
            return;
        }

        DeviceConfiguration configuration = thing.getConfiguration().as(DeviceConfiguration.class);
        String productId = configuration.productId;

        List<SchemaDp> schemaDps;
        try {
            schemaDps = schemaService.reloadSchema(productId, configuration.deviceId);
        } catch (SchemaReloadException e) {
            console.println("Reloading the schema failed: " + e.getMessage() + ".");
            return;
        }
        console.println("Stored schema with " + schemaDps.size() + " datapoints for product '" + productId + "'.");

        deviceThings().filter(t -> productId.equals(productIdOf(t))).forEach(t -> {
            ThingUID uid = t.getUID();
            if (thingManager.isEnabled(uid)) {
                thingManager.setEnabled(uid, false);
                thingManager.setEnabled(uid, true);
                console.println("Re-initialized thing '" + uid + "'.");
            } else {
                console.println("Thing '" + uid + "' is disabled, the new schema is used when it is enabled.");
            }
        });
    }

    private Stream<Thing> deviceThings() {
        return thingRegistry.getAll().stream().filter(t -> THING_TYPE_TUYA_DEVICE.equals(t.getThingTypeUID()));
    }

    private static String productIdOf(Thing thing) {
        return thing.getConfiguration().as(DeviceConfiguration.class).productId;
    }
}
