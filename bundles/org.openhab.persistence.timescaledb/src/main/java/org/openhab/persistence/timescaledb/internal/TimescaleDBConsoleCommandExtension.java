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
package org.openhab.persistence.timescaledb.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Karaf console commands for the TimescaleDB persistence service.
 *
 * <pre>
 * openhab:timescaledb downsample   - run the downsampling job immediately
 * </pre>
 *
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault
@Component(service = ConsoleCommandExtension.class)
public class TimescaleDBConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private static final String CMD_DOWNSAMPLE = "downsample";

    private final TimescaleDBPersistenceService persistenceService;

    @Activate
    public TimescaleDBConsoleCommandExtension(@Reference TimescaleDBPersistenceService persistenceService) {
        super("timescaledb", "TimescaleDB persistence commands.");
        this.persistenceService = persistenceService;
    }

    @Override
    public List<String> getUsages() {
        return List.of(buildCommandUsage(CMD_DOWNSAMPLE, "run the downsampling/retention job immediately"));
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 1 && CMD_DOWNSAMPLE.equals(args[0])) {
            console.println("Starting downsampling job...");
            boolean ran = persistenceService.runDownsampleNow();
            if (ran) {
                console.println("Downsampling job finished.");
            } else {
                console.println("TimescaleDB persistence service is not active — cannot run job.");
            }
        } else {
            printUsage(console);
        }
    }
}
