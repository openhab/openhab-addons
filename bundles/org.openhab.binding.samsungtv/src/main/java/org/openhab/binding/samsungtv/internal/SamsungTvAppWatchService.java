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
package org.openhab.binding.samsungtv.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.samsungtv.internal.protocol.RemoteControllerWebSocket;
import org.openhab.core.OpenHAB;
import org.openhab.core.service.WatchService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SamsungTvAppWatchService} provides a list of apps for >2020 Samsung TV's
 * File should be in json format
 *
 * @author Nick Waterton - Initial contribution
 * @author Nick Waterton - Refactored to new WatchService
 */
@Component(service = SamsungTvAppWatchService.class)
@NonNullByDefault
public class SamsungTvAppWatchService implements WatchService.WatchEventListener {
    private static final String APPS_PATH = OpenHAB.getConfigFolder() + File.separator + "services";
    private static final String APPS_FILE = "samsungtv.cfg";

    private final Logger logger = LoggerFactory.getLogger(SamsungTvAppWatchService.class);
    private final RemoteControllerWebSocket remoteControllerWebSocket;
    private String host = "";
    private boolean started = false;
    int count = 0;

    public SamsungTvAppWatchService(String host, RemoteControllerWebSocket remoteControllerWebSocket) {
        this.host = host;
        this.remoteControllerWebSocket = remoteControllerWebSocket;
    }

    public void start() {
        File file = new File(APPS_PATH, APPS_FILE);
        if (file.exists() && !getStarted()) {
            logger.info("{}: Starting Apps File monitoring service", host);
            started = true;
            readFileApps();
        } else if (count++ == 0) {
            logger.warn("{}: cannot start Apps File monitoring service, file {} does not exist", host, file.toString());
            remoteControllerWebSocket.addKnownAppIds();
        }
    }

    public boolean getStarted() {
        return started;
    }

    /**
     * Check file path for existance
     *
     */
    public boolean checkFileDir() {
        File file = new File(APPS_PATH, APPS_FILE);
        return file.exists();
    }

    public void readFileApps() {
        processWatchEvent(WatchService.Kind.MODIFY, Paths.get(APPS_PATH, APPS_FILE));
    }

    public boolean watchSubDirectories() {
        return false;
    }

    @Override
    public void processWatchEvent(WatchService.Kind kind, Path path) {
        if (path.endsWith(APPS_FILE) && kind != WatchService.Kind.DELETE) {
            logger.debug("{}: Updating Apps list from FILE {}", host, path);
            try {
                @SuppressWarnings("null")
                List<String> allLines = Files.lines(path).filter(line -> !line.trim().startsWith("#"))
                        .collect(Collectors.toList());
                logger.debug("{}: Updated Apps list, {} apps in list", host, allLines.size());
                remoteControllerWebSocket.updateAppList(allLines);
            } catch (IOException e) {
                logger.debug("{}: Cannot read apps file: {}", host, e.getMessage());
            }
        }
    }
}
