/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.linuxinput.internal;

import static org.openhab.binding.linuxinput.internal.LinuxInputBindingConstants.THING_TYPE_DEVICE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linuxinput.internal.evdev4j.EvdevDevice;
import org.openhab.binding.linuxinput.internal.evdev4j.LastErrorException;
import org.openhab.binding.linuxinput.internal.evdev4j.jnr.EvdevLibrary;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for LinuxInputHandlers based on the /dev/input directory.
 *
 * @author Thomas Weißschuh - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.linuxinput")
@NonNullByDefault
public class LinuxInputDiscoveryService extends AbstractDiscoveryService {

    private static final Duration REFRESH_INTERVAL = Duration.ofSeconds(50);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final Duration EVENT_TIMEOUT = Duration.ofSeconds(60);
    private static final Path DEVICE_DIRECTORY = FileSystems.getDefault().getPath("/dev/input");

    private final Logger logger = LoggerFactory.getLogger(LinuxInputDiscoveryService.class);
    private @NonNullByDefault({}) Future<?> discoveryJob;

    public LinuxInputDiscoveryService() {
        super(Set.of(THING_TYPE_DEVICE), (int) TIMEOUT.getSeconds(), true);
    }

    @Override
    protected void startScan() {
        performScan(false);
    }

    private void performScan(boolean applyTtl) {
        logger.debug("Scanning directory {} for devices", DEVICE_DIRECTORY);
        removeOlderResults(getTimestampOfLastScan());
        File directory = DEVICE_DIRECTORY.toFile();
        Duration ttl = null;
        if (applyTtl) {
            ttl = REFRESH_INTERVAL.multipliedBy(2);
        }
        if (directory == null) {
            logger.warn("Could not open device directory {}", DEVICE_DIRECTORY);
            return;
        }
        File[] devices = directory.listFiles();
        if (devices == null) {
            logger.warn("Could not enumerate {}, it is not a directory", directory);
            return;
        }
        for (File file : devices) {
            handleFile(file, ttl);
        }
    }

    private void handleFile(File file, @Nullable Duration ttl) {
        logger.trace("Discovering file {}", file);
        if (file.isDirectory()) {
            logger.trace("{} is not a file, ignoring", file);
            return;
        }
        if (!file.canRead()) {
            logger.debug("{} is not readable, ignoring", file);
            return;
        }
        DiscoveryResultBuilder result = DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_DEVICE, file.getName()))
                .withProperty("path", file.getAbsolutePath()).withRepresentationProperty("path");
        if (ttl != null) {
            result = result.withTTL(ttl.getSeconds());
        }

        boolean shouldDiscover = enrichDevice(result, file);
        if (shouldDiscover) {
            DiscoveryResult thing = result.build();
            logger.debug("Discovered: {}", thing);
            thingDiscovered(thing);
        } else {
            logger.debug("{} is not a keyboard, ignoring", file);
        }
    }

    private boolean enrichDevice(DiscoveryResultBuilder builder, File inputDevice) {
        String label = inputDevice.getName();
        try {
            try (EvdevDevice device = new EvdevDevice(inputDevice.getAbsolutePath())) {
                String labelFromDevice = device.getName();
                boolean isKeyboard = device.has(EvdevLibrary.Type.KEY);
                if (labelFromDevice != null) {
                    label = String.format("%s (%s)", labelFromDevice, inputDevice.getName());
                }
                return isKeyboard;
            } finally {
                builder.withLabel(label);
            }
        } catch (IOException | LastErrorException e) {
            logger.debug("Could not open device {}", inputDevice, e);
            return false;
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting background discovery");
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            WatchService watchService = null;
            try {
                watchService = makeWatcher();
            } catch (IOException e) {
                logger.debug("Could not start event based watcher, falling back to polling", e);
            }
            if (watchService != null) {
                WatchService watcher = watchService;
                FutureTask<?> job = new FutureTask<>(() -> {
                    waitForNewDevices(watcher);
                    return null;
                });
                Thread t = Utils.backgroundThread(job, "discovery", null);
                t.start();
                discoveryJob = job;
            } else {
                discoveryJob = scheduler.scheduleWithFixedDelay(() -> performScan(true), 0,
                        REFRESH_INTERVAL.getSeconds(), TimeUnit.SECONDS);
            }
        }
    }

    private WatchService makeWatcher() throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        // FIXME also trigger on inotify "ATTRIB" events when WatchService supports this.
        // Triggering on ENTRY_MODIFY will trigger multiple times on each keypress for *any* input device.
        DEVICE_DIRECTORY.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE);
        return watchService;
    }

    private void waitForNewDevices(WatchService watchService) {
        while (!Thread.currentThread().isInterrupted()) {
            boolean gotEvent = waitAndDrainAll(watchService);
            logger.debug("Input devices changed: {}. Triggering rescan: {}", gotEvent, gotEvent);

            if (gotEvent) {
                performScan(false);
            }
        }
        logger.debug("Discovery stopped");
    }

    private static boolean waitAndDrainAll(WatchService watchService) {
        WatchKey event;
        try {
            event = watchService.poll(EVENT_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        if (event == null) {
            return false;
        }
        do {
            event.pollEvents();
            event.reset();
            event = watchService.poll();
        } while (event != null);

        return true;
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping background discovery");
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            discoveryJob = null;
        }
    }
}
