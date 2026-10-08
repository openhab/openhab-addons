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
package org.openhab.binding.modbus.migration;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/** User-facing, explicit Modbus poller migration commands. */
@Component(service = ConsoleCommandExtension.class)
@NonNullByDefault
public final class ModbusMigrationConsoleCommandExtension extends AbstractConsoleCommandExtension {
    private final ManagedModbusMigrationAdapter adapter;
    private final ModbusMigrationWorkflow workflow;

    @Activate
    public ModbusMigrationConsoleCommandExtension(@Reference ThingRegistry thingRegistry,
            @Reference ItemChannelLinkRegistry linkRegistry, @Reference ManagedThingProvider thingProvider) {
        super("modbus", "Safe Modbus migration commands.");
        adapter = new ManagedModbusMigrationAdapter(thingRegistry, linkRegistry, thingProvider);
        workflow = new ModbusMigrationWorkflow(new ModbusMigrationPlanner(),
                new ManagedModbusMigrationApplier(thingProvider, linkRegistry));
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length < 2 || !"migrate".equals(args[0])) {
            printUsage(console);
            return;
        }
        try {
            switch (args[1]) {
                case "preview" -> preview(console);
                case "apply" -> apply(args, console);
                case "rollback" -> rollback(args, console);
                default -> printUsage(console);
            }
        } catch (RuntimeException e) {
            console.println("Migration refused: " + e.getMessage());
        }
    }

    private void preview(Console console) {
        ManagedModbusMigrationAdapter.Snapshot snapshot = adapter.snapshot();
        MigrationPreview preview = workflow.preview(snapshot.pollers(), snapshot.data());
        console.println("Preview identity: " + preview.identity());
        console.println(preview.yaml());
        console.println(preview.impactReport());
        snapshot.manualWork().forEach(work -> console.println("MANUAL: " + work.sourceUid() + " — " + work.reason()));
        preview.manualWork().forEach(work -> console.println("MANUAL: " + work.sourceUid() + " — " + work.reason()));
    }

    private void apply(String[] args, Console console) {
        if (args.length != 3) {
            printUsage(console);
            return;
        }
        ManagedModbusMigrationAdapter.Snapshot snapshot = adapter.snapshot();
        workflow.apply(args[2], snapshot.pollers(), snapshot.data());
        console.println("Migration applied; legacy Things and links were not removed.");
    }

    private void rollback(String[] args, Console console) {
        if (args.length != 3) {
            printUsage(console);
            return;
        }
        workflow.rollback(args[2]);
        console.println("Migration targets and replacement links were removed; legacy Things remain untouched.");
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(buildCommandUsage("migrate preview", "Show safe targets, manual work, and identity"),
                buildCommandUsage("migrate apply <identity>", "Create reviewed managed targets and replacement links"),
                buildCommandUsage("migrate rollback <identity>", "Remove only targets and links created by apply"));
    }
}
