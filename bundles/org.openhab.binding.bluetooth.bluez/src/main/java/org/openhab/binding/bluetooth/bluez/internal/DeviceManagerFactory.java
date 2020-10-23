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
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.freedesktop.dbus.exceptions.DBusException;
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

    private @Nullable CompletableFuture<DeviceManager> deviceManagerFuture;
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

        var stage1 = this.deviceManagerFuture = callAsync(() -> {
            try {
                // if this is the first call to the library, this call
                // should throw an exception (that we are catching)
                return DeviceManager.getInstance();
                // Experimental - seems reuse does not work
            } catch (IllegalStateException e) {
                // Exception caused by first call to the library
                return DeviceManager.createInstance(false);
            }
        }, scheduler);

        stage1.thenCompose(devManager -> {
            // lambdas can't modify outside variables due to scoping, so instead we use an AtomicInteger.
            AtomicInteger tryCount = new AtomicInteger();
            // We need to set deviceManagerWrapperFuture here since we want to be able to cancel the underlying
            // AsyncCompletableFuture instance
            return this.deviceManagerWrapperFuture = callAsync(() -> {
                int count = tryCount.incrementAndGet();
                try {
                    logger.debug("Registering property handler attempt: {}", count);
                    devManager.registerPropertyHandler(changeHandler);
                    logger.debug("Successfully registered property handler");
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
                logger.warn("Failed to initialize DeviceManager: {}", th.getMessage());
            }
        });
    }

    @Deactivate
    public void dispose() {
        var stage1 = this.deviceManagerFuture;
        if (stage1 != null) {
            if (!stage1.cancel(true)) {
                // a failure to cancel means that the stage completed normally
                stage1.thenAccept(DeviceManager::closeConnection);
            }
        }
        this.deviceManagerFuture = null;

        var stage2 = this.deviceManagerWrapperFuture;
        if (stage2 != null) {
            stage2.cancel(true);
        }
        this.deviceManagerWrapperFuture = null;
    }

    private static <T> CompletableFuture<T> callAsync(Callable<T> callable, ScheduledExecutorService scheduler) {
        return new AsyncCompletableFuture<>(callable, scheduler);
    }

    // this is a utility class that allows use of Callable with CompletableFutures in a way such that the
    // async future is cancellable thru this CompletableFuture instance.
    private static class AsyncCompletableFuture<T> extends CompletableFuture<T> implements Runnable {

        private final Callable<T> callable;
        private final ScheduledExecutorService scheduler;
        private final Object futureLock = new Object();
        private Future<?> future;

        public AsyncCompletableFuture(Callable<T> callable, ScheduledExecutorService scheduler) {
            this.callable = callable;
            this.scheduler = scheduler;
            future = scheduler.submit(this);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            synchronized (futureLock) {
                future.cancel(mayInterruptIfRunning);
            }
            return super.cancel(mayInterruptIfRunning);
        }

        @Override
        public void run() {
            try {
                complete(callable.call());
            } catch (RetryException e) {
                synchronized (futureLock) {
                    if (!future.isCancelled()) {
                        future = scheduler.schedule(this, e.delay, e.unit);
                    }
                }
            } catch (Exception e) {
                completeExceptionally(e);
            }
        }
    }

    // this is a special exception to indicate to a AsyncCompletableFuture that the task needs to be retried.
    private static class RetryException extends Exception {

        private static final long serialVersionUID = 8512275408512109328L;
        private long delay;
        private TimeUnit unit;

        public RetryException(long delay, TimeUnit unit) {
            this.delay = delay;
            this.unit = unit;
        }
    }
}
