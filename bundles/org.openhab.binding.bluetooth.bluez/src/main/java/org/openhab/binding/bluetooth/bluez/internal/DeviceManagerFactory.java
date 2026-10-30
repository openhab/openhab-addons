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
package org.openhab.binding.bluetooth.bluez.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.freedesktop.dbus.exceptions.DBusException;
import org.openhab.binding.bluetooth.util.RetryException;
import org.openhab.binding.bluetooth.util.RetryFuture;
import org.openhab.core.common.ThreadPoolManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hypfvieh.bluetooth.DeviceManager;

/**
 * This service handles the lifecycle of the {@link DeviceManager} singleton instance.
 * In addition, this class is responsible for managing the BlueZPropertiesChangedHandler instance
 * used by the binding for listening and dispatching dbus events from the DeviceManager.
 *
 * Creation of the DeviceManagerWrapper is asynchronous and thus attempts to retrieve the
 * DeviceManagerWrapper through 'getDeviceManager' may initially fail.
 *
 * @author Connor Petty - Initial Contribution
 *
 */
@NonNullByDefault
@Component(service = DeviceManagerFactory.class)
public class DeviceManagerFactory {

    private final Logger logger = LoggerFactory.getLogger(DeviceManagerFactory.class);
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("bluetooth");

    private final BlueZPropertiesChangedHandler changeHandler = new BlueZPropertiesChangedHandler();

    private @Nullable CompletableFuture<@Nullable DeviceManager> deviceManagerFuture;
    private @Nullable CompletableFuture<DeviceManagerWrapper> deviceManagerWrapperFuture;

    public BlueZPropertiesChangedHandler getPropertiesChangedHandler() {
        return changeHandler;
    }

    public @Nullable DeviceManagerWrapper getDeviceManager() {
        // we can cheat the null checker with casting here
        var future = (CompletableFuture<@Nullable DeviceManagerWrapper>) deviceManagerWrapperFuture;
        if (future != null) {
            return future.getNow(null);
        }
        return null;
    }

    @Activate
    public void initialize() {
        logger.debug("initializing DeviceManagerFactory");

        var stage1 = this.deviceManagerFuture = RetryFuture.callWithRetry(() -> {
            DeviceManager existing = getConnectedInstance();
            if (existing != null) {
                logger.debug("Reusing connected DeviceManager");
                return existing;
            }
            try {
                // either the first call to the library, or the previous instance was disconnected on dispose
                return DeviceManager.createInstance(false);
            } catch (DBusException | UnsatisfiedLinkError ex) {
                // we might be on a system without DBus, such as macOS or Windows
                logger.debug("Failed to initialize DeviceManager: {}", ex.getMessage());
                return null;
            }
        }, scheduler);

        this.deviceManagerWrapperFuture = stage1.thenCompose(devManager -> {
            // lambdas can't modify outside variables due to scoping, so instead we use an AtomicInteger.
            AtomicInteger tryCount = new AtomicInteger();
            return RetryFuture.callWithRetry(() -> {
                int count = tryCount.incrementAndGet();
                try {
                    logger.debug("Registering property handler attempt: {}", count);
                    if (devManager != null) {
                        devManager.registerPropertyHandler(changeHandler);
                        logger.debug("Successfully registered property handler");
                    }
                    return new DeviceManagerWrapper(devManager);
                } catch (DBusException e) {
                    if (count < 3) {
                        throw new RetryException(5, TimeUnit.SECONDS);
                    } else {
                        throw e;
                    }
                }
            }, scheduler);
        }).whenComplete((devManagerWrapper, th) -> {
            if (th != null) {
                if (th.getCause() instanceof DBusException) {
                    // we might be on a system without DBus, such as macOS or Windows
                    logger.debug("Failed to initialize DeviceManager: {}", th.getMessage());
                } else {
                    logger.warn("Failed to initialize DeviceManager: {}", th.getMessage());
                }
            }
        });
    }

    /**
     * The bluez-dbus library keeps a static {@link DeviceManager} instance. Its D-Bus connection is closed when this
     * factory is deactivated, e.g. when the bundle is restarted. Reusing that instance would make every D-Bus call fail
     * with NotConnected, so it is only reused while its connection is still open.
     */
    private static @Nullable DeviceManager getConnectedInstance() {
        try {
            DeviceManager deviceManager = DeviceManager.getInstance();
            return deviceManager.getDbusConnection().isConnected() ? deviceManager : null;
        } catch (IllegalStateException e) {
            // no instance created yet
            return null;
        }
    }

    @Deactivate
    public void dispose() {
        var stage1 = this.deviceManagerFuture;
        if (stage1 != null) {
            if (!stage1.cancel(true)) {
                // a failure to cancel means that the stage completed normally
                stage1.thenAccept(devManager -> {
                    if (devManager != null) {
                        try {
                            devManager.unRegisterPropertyHandler(changeHandler);
                        } catch (DBusException e) {
                            logger.debug("Failed to unregister property handler: {}", e.getMessage());
                        }
                        devManager.closeConnection();
                    }
                });
            }
        }
        this.deviceManagerFuture = null;

        var stage2 = this.deviceManagerWrapperFuture;
        if (stage2 != null) {
            stage2.cancel(true);
        }
        this.deviceManagerWrapperFuture = null;
    }
}
