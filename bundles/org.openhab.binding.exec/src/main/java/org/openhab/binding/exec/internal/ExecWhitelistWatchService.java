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
package org.openhab.binding.exec.internal;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.service.WatchService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ExecWhitelistWatchService} provides a whitelist check for exec commands
 *
 * @author Jan N. Klug - Initial contribution
 */
@Component(service = ExecWhitelistWatchService.class)
@NonNullByDefault
public class ExecWhitelistWatchService implements WatchService.WatchEventListener {
    private static final Path COMMAND_WHITELIST_FILE = Path.of("misc", "exec.whitelist");

    private final Logger logger = LoggerFactory.getLogger(ExecWhitelistWatchService.class);
    private final Set<String> commandWhitelist = new HashSet<>();
    private final WatchService watchService;
    private final Path watchFile;

    @Activate
    public ExecWhitelistWatchService(
            final @Reference(target = WatchService.CONFIG_WATCHER_FILTER) WatchService watchService) {
        this.watchService = watchService;
        this.watchFile = watchService.getWatchPath().resolve(COMMAND_WHITELIST_FILE);
        watchService.registerListener(this, COMMAND_WHITELIST_FILE, false);

        // read initial content
        processWatchEvent(WatchService.Kind.CREATE, COMMAND_WHITELIST_FILE);
    }

    @Deactivate
    public void deactivate() {
        watchService.unregisterListener(this);
    }

    @Override
    public void processWatchEvent(WatchService.Kind kind, Path path) {
        commandWhitelist.clear();
        if (kind != WatchService.Kind.DELETE) {
            try (Stream<String> lines = Files.lines(watchFile)) {
                lines.filter(line -> !line.trim().startsWith("#")).forEach(commandWhitelist::add);
                logger.debug("Updated command whitelist: {}", commandWhitelist);
            } catch (IOException e) {
                logger.warn("Cannot read whitelist file, exec binding commands won't be processed: {}", e.getMessage());
            }
        }
    }

    /**
     * Check if a command is whitelisted
     *
     * @param command the command to check alias
     * @return true if whitelisted, false if not
     */
    public boolean isWhitelisted(String command) {
        return commandWhitelist.contains(command);
    }
}
