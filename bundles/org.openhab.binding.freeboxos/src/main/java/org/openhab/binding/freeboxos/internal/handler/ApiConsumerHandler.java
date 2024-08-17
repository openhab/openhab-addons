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
package org.openhab.binding.freeboxos.internal.handler;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.PermissionException;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.Source;
import org.openhab.binding.freeboxos.internal.api.rest.MediaReceiverManager;
import org.openhab.binding.freeboxos.internal.api.rest.MediaReceiverManager.MediaType;
import org.openhab.binding.freeboxos.internal.api.rest.MediaReceiverManager.Receiver;
import org.openhab.binding.freeboxos.internal.api.rest.RestManager;
import org.openhab.binding.freeboxos.internal.config.ApiConsumerConfiguration;
import org.openhab.binding.freeboxos.internal.config.ClientConfiguration;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import inet.ipaddr.IPAddress;

/**
 * The {@link ApiConsumerHandler} is a base abstract class for all devices made available by the FreeboxOs bridge
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class ApiConsumerHandler extends BaseThingHandler implements ApiConsumerIntf {
    private final Logger logger = LoggerFactory.getLogger(ApiConsumerHandler.class);
    private final Map<String, ScheduledFuture<?>> jobs = new HashMap<>();

    private @Nullable ServiceRegistration<?> reg;
    protected boolean statusDrivenByBridge = true;

    ApiConsumerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        FreeboxOsHandler bridgeHandler = checkBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }
        initializeOnceBridgeOnline(bridgeHandler);
    }

    private void initializeOnceBridgeOnline(FreeboxOsHandler bridgeHandler) {
        Map<String, String> properties = editProperties();
        try {
            initializeProperties(properties);
            updateProperties(properties);
        } catch (FreeboxException e) {
            logger.warn("Error getting thing {} properties: {}", thing.getUID(), e.getMessage());
        }

        startRefreshJob();
    }

    protected void configureMediaSink() {
        try {
            String upnpName = editProperties().getOrDefault(Source.UPNP.name(), "");
            Receiver receiver = getManager(MediaReceiverManager.class).getReceiver(upnpName);
            if (receiver != null) {
                Map<String, String> properties = editProperties();
                receiver.capabilities().entrySet()
                        .forEach(entry -> properties.put(entry.getKey().name(), entry.getValue().toString()));
                updateProperties(properties);

                startAudioSink(receiver);
            } else {
                stopAudioSink();
            }
        } catch (FreeboxException e) {
            logger.warn("Unable to retrieve Media Receivers: {}", e.getMessage());
        }
    }

    private void startAudioSink(Receiver receiver) {
        FreeboxOsHandler bridgeHandler = checkBridgeHandler();
        // Only video and photo is supported by the API so use VIDEO capability for audio
        if (reg == null && bridgeHandler != null && Boolean.TRUE.equals(receiver.capabilities().get(MediaType.VIDEO))) {
            ApiConsumerConfiguration config = getConfig().as(ApiConsumerConfiguration.class);
            String callbackURL = bridgeHandler.getCallbackURL();
            if (!config.password.isEmpty() || !receiver.passwordProtected()) {
                reg = bridgeHandler.getBundleContext()
                        .registerService(
                                AudioSink.class.getName(), new AirMediaSink(this, bridgeHandler.getAudioHTTPServer(),
                                        callbackURL, receiver.name(), config.password, config.acceptAllMp3),
                                new Hashtable<>());
                logger.debug("Audio sink registered for {}.", receiver.name());
            } else {
                logger.warn("A password needs to be configured to enable Air Media capability.");
            }
        }
    }

    private void stopAudioSink() {
        ServiceRegistration<?> localReg = reg;
        if (localReg != null) {
            localReg.unregister();
            logger.debug("Audio sink unregistered");
            reg = null;
        }
    }

    public <T extends RestManager> T getManager(Class<T> clazz) throws FreeboxException {
        FreeboxOsHandler handler = checkBridgeHandler();
        if (handler != null) {
            return handler.getManager(clazz);
        }
        throw new FreeboxException("Bridge handler not yet defined");
    }

    abstract void initializeProperties(Map<String, String> properties) throws FreeboxException;

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        FreeboxOsHandler bridgeHandler = checkBridgeHandler();
        if (bridgeHandler != null) {
            initializeOnceBridgeOnline(bridgeHandler);
        } else {
            stopJobs();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }
        try {
            if (checkBridgeHandler() != null) {
                if (command instanceof RefreshType) {
                    internalForcePoll();
                } else if (!internalHandleCommand(channelUID.getIdWithoutGroup(), command)) {
                    logger.debug("Unexpected command {} on channel {}", command, channelUID.getId());
                }
            }
        } catch (PermissionException e) {
            logger.warn("Missing permission {} for handling command {} on channel {}: {}", e.getPermission(), command,
                    channelUID.getId(), e.getMessage());
        } catch (FreeboxException e) {
            logger.warn("Error handling command {} on channel {}: {}", command, channelUID.getId(), e.getMessage());
        }
    }

    private @Nullable FreeboxOsHandler checkBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof FreeboxOsHandler fbOsHandler) {
                if (bridge.getStatus() == ThingStatus.ONLINE) {
                    if (statusDrivenByBridge) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                    return fbOsHandler;
                }
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
        return null;
    }

    @Override
    public void dispose() {
        stopJobs();
        stopAudioSink();
        super.dispose();
    }

    private void startRefreshJob() {
        removeJob("GlobalJob");

        int refreshInterval = getConfigAs(ApiConsumerConfiguration.class).refreshInterval;
        logger.debug("Scheduling state update every {} seconds for thing {}...", refreshInterval, getThing().getUID());

        ThingStatusDetail detail = thing.getStatusInfo().getStatusDetail();
        if (ThingStatusDetail.DUTY_CYCLE.equals(detail)) {
            try {
                internalForcePoll();
            } catch (FreeboxException ignore) {
                // An exception is normal if the box is rebooting then let's try again later...
                addJob("Initialize", this::initialize, 10, TimeUnit.SECONDS);
                return;
            }
        }

        addJob("GlobalJob", () -> {
            try {
                internalPoll();
            } catch (FreeboxException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }, 0, refreshInterval, TimeUnit.SECONDS);
    }

    private void removeJob(String name) {
        ScheduledFuture<?> existing = jobs.get(name);
        if (existing != null && !existing.isCancelled()) {
            existing.cancel(true);
        }
    }

    @Override
    public void addJob(String name, Runnable command, long initialDelay, long delay, TimeUnit unit) {
        removeJob(name);
        jobs.put(name, scheduler.scheduleWithFixedDelay(command, initialDelay, delay, unit));
    }

    @Override
    public void addJob(String name, Runnable command, long delay, TimeUnit unit) {
        removeJob(name);
        jobs.put(name, scheduler.schedule(command, delay, unit));
    }

    @Override
    public void stopJobs() {
        jobs.keySet().forEach(name -> removeJob(name));
        jobs.clear();
    }

    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        return false;
    }

    protected abstract void internalPoll() throws FreeboxException;

    protected void internalForcePoll() throws FreeboxException {
        internalPoll();
    }

    private void updateIfActive(String group, String channelId, State state) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, state);
        }
    }

    protected void updateIfActive(String channelId, State state) {
        ChannelUID id = new ChannelUID(getThing().getUID(), channelId);
        if (isLinked(id)) {
            updateState(id, state);
        }
    }

    protected void updateChannelDateTimeState(String channelId, @Nullable ZonedDateTime timestamp) {
        updateIfActive(channelId, timestamp == null ? UnDefType.NULL : new DateTimeType(timestamp));
    }

    protected void updateChannelDateTimeState(String group, String channelId, @Nullable ZonedDateTime timestamp) {
        updateIfActive(group, channelId, timestamp == null ? UnDefType.NULL : new DateTimeType(timestamp));
    }

    protected void updateChannelOnOff(String group, String channelId, boolean value) {
        updateIfActive(group, channelId, OnOffType.from(value));
    }

    protected void updateChannelOnOff(String channelId, boolean value) {
        updateIfActive(channelId, OnOffType.from(value));
    }

    protected void updateChannelString(String group, String channelId, @Nullable String value) {
        updateIfActive(group, channelId, value != null ? new StringType(value) : UnDefType.NULL);
    }

    protected void updateChannelString(String group, String channelId, @Nullable IPAddress value) {
        updateIfActive(group, channelId, value != null ? new StringType(value.toCanonicalString()) : UnDefType.NULL);
    }

    protected void updateChannelString(String channelId, @Nullable String value) {
        updateIfActive(channelId, value != null ? new StringType(value) : UnDefType.NULL);
    }

    protected void updateChannelString(String channelId, Enum<?> value) {
        updateIfActive(channelId, new StringType(value.name()));
    }

    protected void updateChannelString(String group, String channelId, Enum<?> value) {
        updateIfActive(group, channelId, new StringType(value.name()));
    }

    protected void updateChannelQuantity(String group, String channelId, double d, Unit<?> unit) {
        updateChannelQuantity(group, channelId, new QuantityType<>(d, unit));
    }

    protected void updateChannelQuantity(String channelId, @Nullable QuantityType<?> quantity) {
        updateIfActive(channelId, quantity != null ? quantity : UnDefType.NULL);
    }

    protected void updateChannelQuantity(String group, String channelId, @Nullable QuantityType<?> quantity) {
        updateIfActive(group, channelId, quantity != null ? quantity : UnDefType.NULL);
    }

    protected void updateChannelDecimal(String group, String channelId, @Nullable Integer value) {
        updateIfActive(group, channelId, value != null ? new DecimalType(value) : UnDefType.NULL);
    }

    protected void updateChannelQuantity(String group, String channelId, QuantityType<?> qtty, Unit<?> unit) {
        updateChannelQuantity(group, channelId, qtty.toUnit(unit));
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public Map<String, String> editProperties() {
        return super.editProperties();
    }

    @Override
    public void updateProperties(@Nullable Map<String, String> properties) {
        super.updateProperties(properties);
    }

    @Override
    public boolean anyChannelLinked(String groupId, Set<String> channelSet) {
        return channelSet.stream().map(id -> new ChannelUID(getThing().getUID(), groupId, id))
                .anyMatch(uid -> isLinked(uid));
    }

    @Override
    public Configuration getConfig() {
        return super.getConfig();
    }

    @Override
    public int getClientId() {
        return ((BigDecimal) getConfig().get(ClientConfiguration.ID)).intValue();
    }
}
