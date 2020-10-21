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
package org.openhab.binding.bluetooth.bluez.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.freedesktop.dbus.exceptions.DBusException;
import org.openhab.core.common.NamedThreadFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hypfvieh.bluetooth.DeviceManager;

/**
 *
 * @author Connor Petty - Initial Contribution
 *
 */
@NonNullByDefault
@Component(service = DeviceManagerFactory.class)
public class DeviceManagerFactory {

    private final Logger logger = LoggerFactory.getLogger(DeviceManagerFactory.class);
    private @Nullable ScheduledExecutorService scheduler;
    private final BlueZPropertiesChangedHandler changeHandler = new BlueZPropertiesChangedHandler();

    private @Nullable DeviceManager deviceManager;
    private @Nullable CompletableFuture<DeviceManagerWrapper> deviceManagerFuture;

    public BlueZPropertiesChangedHandler getPropertiesChangedHandler() {
        return changeHandler;
    }

    public @Nullable DeviceManagerWrapper getDeviceManager() {
        // we can cheat the null checker with casting here
        var future = (CompletableFuture<@Nullable DeviceManagerWrapper>) this.deviceManagerFuture;
        if (future != null) {
            return future.getNow(null);
        }
        return null;
    }

    @Activate
    public void initialize() {
        logger.debug("initializing DeviceManagerFactory");
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1,
                new NamedThreadFactory("bluetooth.bluez-init", true));

        scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduler.setRemoveOnCancelPolicy(true);

        this.scheduler = scheduler;

        this.deviceManagerFuture = callAsync(() -> {
            try {
                // if this is the first call to the library, this call
                // should throw an exception (that we are catching)
                return DeviceManager.getInstance();
                // Experimental - seems reuse does not work
            } catch (IllegalStateException e) {
                // Exception caused by first call to the library
                return DeviceManager.createInstance(false);
            }
        }, scheduler)//
                .thenApply(devManager -> {
                    this.deviceManager = devManager;
                    return devManager;
                }).thenCompose(devManager -> registerPropertyHandler(devManager, scheduler))//
                .thenApply(DeviceManagerWrapper::new)//
                .whenComplete((devManager, th) -> {
                    if (th != null) {
                        logger.warn("Failed to initialize DeviceManager: {}", th.getMessage());
                    }
                });
    }

    @Deactivate
    public void dispose() {
        DeviceManager manager = deviceManager;
        if (manager != null) {
            manager.closeConnection();
        }
        deviceManager = null;

        ExecutorService executor = scheduler;
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private CompletableFuture<DeviceManager> registerPropertyHandler(DeviceManager deviceManager,
            ScheduledExecutorService scheduler) {

        class RegisterHandlerTask implements Runnable {

            private CompletableFuture<DeviceManager> future = new CompletableFuture<>();
            private int retryCount = 0;

            @Override
            public void run() {
                try {
                    logger.debug("Registering property handler attempt: {}", retryCount + 1);
                    deviceManager.registerPropertyHandler(changeHandler);
                    future.complete(deviceManager);
                    logger.debug("Successfully registered property handler");
                } catch (DBusException e) {
                    if (retryCount < 3) {
                        retryCount++;
                        scheduler.schedule(this, 5, TimeUnit.SECONDS);
                    } else {
                        future.completeExceptionally(e);
                    }
                }
            }
        }
        RegisterHandlerTask task = new RegisterHandlerTask();
        task.run();
        return task.future;
    }

    private static <T> CompletableFuture<T> callAsync(Callable<T> callable, ExecutorService executor) {
        return new AsyncCompletableFuture<>(callable, executor);
    }

    // this is a utility class that allows use of Callable with CompletableFutures in a way such that the
    // async future is cancellable thru this CompletableFuture instance.
    private static class AsyncCompletableFuture<T> extends CompletableFuture<T> {

        private Future<?> future;

        public AsyncCompletableFuture(Callable<T> callable, ExecutorService executor) {
            future = executor.submit(() -> {
                try {
                    complete(callable.call());
                } catch (Exception e) {
                    completeExceptionally(e);
                }
            });
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            future.cancel(mayInterruptIfRunning);
            return super.cancel(mayInterruptIfRunning);
        }
    }
}
