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
package org.openhab.binding.exec.internal;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.service.AbstractWatchService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ExecWhitelistWatchService} provides a whitelist check for exec commands
 *
 * @author Jan N. Klug - Initial contribution
 */
@Component(service = ExecWhitelistWatchService.class)
@NonNullByDefault
public class ExecWhitelistWatchService extends AbstractWatchService {
    private static final String COMMAND_WHITELIST_PATH = OpenHAB.getConfigFolder() + File.separator + "misc";
    private static final String COMMAND_WHITELIST_FILE = "exec.whitelist";

    private final Logger logger = LoggerFactory.getLogger(ExecWhitelistWatchService.class);
    private final Set<String> commandWhitelist = new HashSet<>();

    @Activate
    public ExecWhitelistWatchService() {
        super(COMMAND_WHITELIST_PATH);
        processWatchEvent(null, null, Paths.get(COMMAND_WHITELIST_PATH, COMMAND_WHITELIST_FILE));
    }

    @Override
    protected boolean watchSubDirectories() {
        return false;
    }

    @Override
    protected Kind<?> @Nullable [] getWatchEventKinds(@Nullable Path directory) {
        return new Kind<?>[] { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY };
    }

    @Override
    protected void processWatchEvent(@Nullable WatchEvent<?> event, @Nullable Kind<?> kind, @Nullable Path path) {
        if (path != null && path.endsWith(COMMAND_WHITELIST_FILE)) {
            commandWhitelist.clear();
            try {
                Files.lines(path).filter(line -> !line.trim().startsWith("#")).forEach(commandWhitelist::add);
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
