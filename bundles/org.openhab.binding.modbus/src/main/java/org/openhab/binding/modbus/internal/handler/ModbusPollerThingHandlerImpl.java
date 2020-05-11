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
package org.openhab.binding.modbus.internal.handler;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.binding.modbus.internal.AtomicStampedValue;
import org.openhab.binding.modbus.internal.ModbusBindingConstantsInternal;
import org.openhab.binding.modbus.internal.config.ModbusPollerConfiguration;
import org.openhab.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.BasicPollTaskImpl;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.ModbusReadCallback;
import org.openhab.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.PollTask;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ModbusPollerThingHandlerImpl} is responsible for polling Modbus slaves. Errors and data is delegated to
 * child thing handlers inheriting from {@link ModbusReadCallback} -- in practice: {@link ModbusDataThingHandler}.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusPollerThingHandlerImpl extends BaseBridgeHandler implements ModbusPollerThingHandler {

    /**
     * {@link ModbusReadCallback} that delegates all tasks forward.
     *
     * All instances of {@linkplain ReadCallbackDelegator} are considered equal, if they are connected to the same
     * bridge. This makes sense, as the callback delegates
     * to all child things of this bridge.
     *
     * @author Sami Salonen
     *
     */
    private class ReadCallbackDelegator implements ModbusReadCallback {

        private volatile @Nullable AtomicStampedValue<AsyncModbusReadResult> lastResult;

        @Override
        public synchronized void handle(AsyncModbusReadResult result) {
            // Ignore all incoming data and errors if configuration is not correct
            if (hasConfigurationError() || disposed) {
                return;
            }
            if (config.getCacheMillis() >= 0) {
                AtomicStampedValue<AsyncModbusReadResult> localLastResult = this.lastResult;
                if (localLastResult == null) {
                    this.lastResult = new AtomicStampedValue<>(System.currentTimeMillis(), result);
                } else {
                    localLastResult.update(System.currentTimeMillis(), result);
                    this.lastResult = localLastResult;
                }
            }
            logger.debug("Thing {} received response {}", thing.getUID(), result);
            childCallbacks.forEach(handler -> handler.handle(result));
            if (result.hasError()) {
                Exception error = result.getCause();
                assert error != null;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format("Error with read: %s: %s", error.getClass().getName(), error.getMessage()));
            } else {
                resetCommunicationError();
            }
        }

        private void resetCommunicationError() {
            ThingStatusInfo statusInfo = thing.getStatusInfo();
            if (ThingStatus.OFFLINE.equals(statusInfo.getStatus())
                    && ThingStatusDetail.COMMUNICATION_ERROR.equals(statusInfo.getStatusDetail())) {
                updateStatus(ThingStatus.ONLINE);
            }
        }

        /**
         * Update children data if data is fresh enough
         *
         * @param oldestStamp oldest data that is still passed to children
         * @return whether data was updated. Data is not updated when it's too old or there's no data at all.
         */
        @SuppressWarnings("null")
        public boolean updateChildrenWithOldData(long oldestStamp) {
            return Optional.ofNullable(this.lastResult).map(result -> result.copyIfStampAfter(oldestStamp))
                    .map(result -> {
                        logger.debug("Thing {} reusing cached data: {}", thing.getUID(), result.getValue());
                        childCallbacks.forEach(handler -> handler.handle(result.getValue()));
                        return true;
                    }).orElse(false);

        }

        /**
         * Rest data caches
         */
        public void resetCache() {
            lastResult = null;
        }
    }

    /**
     * Immutable {@link ModbusReadRequestBlueprint} to read from endpoint represented by this Poller's bridge
     *
     * @author Sami Salonen
     *
     */
    private static class ModbusPollerReadRequest extends ModbusReadRequestBlueprint {

        private static ModbusReadFunctionCode getFunctionCode(@Nullable String type) {
            if (!ModbusBindingConstantsInternal.READ_FUNCTION_CODES.containsKey(type)) {
                Object[] acceptedTypes = ModbusBindingConstantsInternal.READ_FUNCTION_CODES.keySet().toArray();
                Arrays.sort(acceptedTypes);
                throw new IllegalArgumentException(
                        String.format("No function code found for type='%s'. Was expecting one of: %s", type,
                                StringUtils.join(acceptedTypes, ", ")));
            }
            ModbusReadFunctionCode functionCode = ModbusBindingConstantsInternal.READ_FUNCTION_CODES.get(type);
            return functionCode;
        }

        public ModbusPollerReadRequest(ModbusPollerConfiguration config,
                ModbusEndpointThingHandler slaveEndpointThingHandler) throws EndpointNotInitializedException {
            super(slaveEndpointThingHandler.getSlaveId(), getFunctionCode(config.getType()), config.getStart(),
                    config.getLength(), config.getMaxTries());
        }
    }

    private final Logger logger = LoggerFactory.getLogger(ModbusPollerThingHandlerImpl.class);

    @NonNullByDefault({})
    private ModbusPollerConfiguration config;
    private long cacheMillis;
    private volatile @Nullable PollTask pollTask;
    private Supplier<ModbusManager> managerRef;
    private volatile boolean disposed;
    private volatile List<ModbusReadCallback> childCallbacks = new CopyOnWriteArrayList<>();

    private ReadCallbackDelegator callbackDelegator = new ReadCallbackDelegator();

    public ModbusPollerThingHandlerImpl(Bridge bridge, Supplier<ModbusManager> managerRef) {
        super(bridge);
        this.managerRef = managerRef;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No channels, no commands
    }

    private @Nullable ModbusEndpointThingHandler getEndpointThingHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Bridge is null");
            return null;
        }
        if (bridge.getStatus() != ThingStatus.ONLINE) {
            logger.debug("Bridge is not online");
            return null;
        }

        ThingHandler handler = bridge.getHandler();
        if (handler == null) {
            logger.debug("Bridge handler is null");
            return null;
        }

        if (handler instanceof ModbusEndpointThingHandler) {
            ModbusEndpointThingHandler slaveEndpoint = (ModbusEndpointThingHandler) handler;
            return slaveEndpoint;
        } else {
            logger.debug("Unexpected bridge handler: {}", handler);
            return null;
        }
    }

    @Override
    public synchronized void initialize() {
        if (this.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            // If the bridge was online then first change it to offline.
            // this ensures that children will be notified about the change
            updateStatus(ThingStatus.OFFLINE);
        }
        this.callbackDelegator.resetCache();
        disposed = false;
        logger.trace("Initializing {} from status {}", this.getThing().getUID(), this.getThing().getStatus());
        try {
            config = getConfigAs(ModbusPollerConfiguration.class);
            cacheMillis = this.config.getCacheMillis();
            registerPollTask();
        } catch (EndpointNotInitializedException e) {
            logger.debug("Exception during initialization", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String
                    .format("Exception during initialization: %s (%s)", e.getMessage(), e.getClass().getSimpleName()));
        } finally {
            logger.trace("initialize() of thing {} '{}' finished", thing.getUID(), thing.getLabel());
        }
    }

    @Override
    public synchronized void dispose() {
        logger.debug("dispose()");
        // Mark handler as disposed as soon as possible to halt processing of callbacks
        disposed = true;
        unregisterPollTask();
        this.callbackDelegator.resetCache();
    }

    /**
     * Unregister poll task.
     *
     * No-op in case no poll task is registered, or if the initialization is incomplete.
     */
    public synchronized void unregisterPollTask() {
        logger.trace("unregisterPollTask()");
        if (pollTask == null || config == null) {
            return;
        }
        logger.debug("Unregistering polling from ModbusManager");
        @NonNull
        PollTask task = (@NonNull PollTask) pollTask;
        managerRef.get().unregisterRegularPoll(task);
        pollTask = null;
        updateStatus(ThingStatus.OFFLINE);
    }

    /**
     * Register poll task
     *
     * @throws EndpointNotInitializedException in case the bridge initialization is not complete. This should only
     *             happen in transient conditions, for example, when bridge is initializing.
     */
    @SuppressWarnings("null")
    private synchronized void registerPollTask() throws EndpointNotInitializedException {
        logger.trace("registerPollTask()");
        if (pollTask != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            logger.debug("pollTask should be unregistered before registering a new one!");
            return;
        }

        ModbusEndpointThingHandler slaveEndpointThingHandler = getEndpointThingHandler();
        if (slaveEndpointThingHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, String.format("Bridge '%s' is offline",
                    Optional.ofNullable(getBridge()).map(b -> b.getLabel()).orElse("<null>")));
            logger.debug("No bridge handler available -- aborting init for {}", this);
            return;
        }
        ModbusSlaveEndpoint endpoint = slaveEndpointThingHandler.asSlaveEndpoint();
        if (endpoint == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, String.format(
                    "Bridge '%s' not completely initialized", Optional.ofNullable(getBridge()).map(b -> b.getLabel())));
            logger.debug("Bridge not initialized fully (no endpoint) -- aborting init for {}", this);
            return;
        }

        ModbusReadRequestBlueprint request = new ModbusPollerReadRequest(config, slaveEndpointThingHandler);
        @NonNull
        PollTask task = new BasicPollTaskImpl(endpoint, request, callbackDelegator);
        pollTask = task;

        if (config.getRefresh() <= 0L) {
            logger.debug("Not registering polling with ModbusManager since refresh disabled");
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Not polling");
        } else {
            logger.debug("Registering polling with ModbusManager");
            managerRef.get().registerRegularPoll(task, config.getRefresh(), 0);
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private boolean hasConfigurationError() {
        ThingStatusInfo statusInfo = getThing().getStatusInfo();
        return statusInfo.getStatus() == ThingStatus.OFFLINE
                && statusInfo.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR;
    }

    @Override
    public synchronized void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged for {}. Reseting handler", this.getThing().getUID());
        this.dispose();
        this.initialize();
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof ModbusReadCallback) {
            this.childCallbacks.add((ModbusReadCallback) childHandler);
        }
    }

    @SuppressWarnings("unlikely-arg-type")
    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof ModbusReadCallback) {
            this.childCallbacks.remove(childHandler);
        }
    }

    @Override
    public Supplier<ModbusManager> getManagerRef() {
        return managerRef;
    }

    @Override
    public @Nullable PollTask getPollTask() {
        return pollTask;
    }

    /**
     * Refresh the data
     *
     * If data or error was just recently received (i.e. cache is fresh), return the cached response.
     */
    @Override
    public void refresh() {
        PollTask pollTask = this.pollTask;
        if (pollTask == null) {
            return;
        }

        long oldDataThreshold = System.currentTimeMillis() - cacheMillis;
        boolean cacheWasRecentEnoughForUpdate = cacheMillis > 0
                && this.callbackDelegator.updateChildrenWithOldData(oldDataThreshold);
        if (cacheWasRecentEnoughForUpdate) {
            logger.debug(
                    "Poller {} received refresh() and cache was recent enough (age at most {} ms). Reusing old response",
                    getThing().getUID(), cacheMillis);
        } else {
            // cache expired, poll new data
            logger.debug("Poller {} received refresh() but the cache is not applicable. Polling new data",
                    getThing().getUID());
            managerRef.get().submitOneTimePoll(pollTask);
        }
    }
}
