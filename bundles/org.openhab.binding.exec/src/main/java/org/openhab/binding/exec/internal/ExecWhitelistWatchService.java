/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.service.AbstractWatchService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.HashSet;
import java.util.Set;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * The {@link ExecWhitelistWatchService} provides a whitelist check for exec commands
 *
 * @author Jan N. Klug - Initial contribution
 */
@Component(service = ExecWhitelistWatchService.class)
@NonNullByDefault
public class ExecWhitelistWatchService extends AbstractWatchService {
    private static final String COMMAND_WHITELIST_PATH = ConfigConstants.getConfigFolder() + File.separator + "misc";
    private static final String COMMAND_WHITELIST_FILE = "exec.whitelist";
    private final Set<String> commandWhitelist = new HashSet<>();

    @Activate
    public ExecWhitelistWatchService() {
        super(COMMAND_WHITELIST_PATH);
    }

    @Override
    protected boolean watchSubDirectories() {
        return false;
    }

    @Override
    protected WatchEvent.Kind<?>[] getWatchEventKinds(@Nullable Path directory) {
        return new WatchEvent.Kind<?>[] { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY };
    }

    @Override
    protected void processWatchEvent(@Nullable WatchEvent<?> event,  WatchEvent.@Nullable Kind<?> kind, @Nullable Path path) {
        if (path.endsWith(COMMAND_WHITELIST_FILE)) {
            commandWhitelist.clear();
            try  {
                Files.lines(path).forEach(commandWhitelist::add);
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
