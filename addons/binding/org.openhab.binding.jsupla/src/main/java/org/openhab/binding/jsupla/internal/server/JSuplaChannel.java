/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jsupla.internal.server;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.jsupla.handler.JSuplaCloudBridgeHandler;
import org.openhab.binding.jsupla.handler.SuplaDeviceHandler;
import org.openhab.binding.jsupla.internal.SuplaDeviceRegistry;
import org.openhab.binding.jsupla.internal.discovery.JSuplaDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.grzeslowski.jsupla.protocoljava.api.entities.dcs.PingServer;
import pl.grzeslowski.jsupla.protocoljava.api.entities.dcs.SetActivityTimeout;
import pl.grzeslowski.jsupla.protocoljava.api.entities.ds.DeviceChannelValue;
import pl.grzeslowski.jsupla.protocoljava.api.entities.ds.RegisterDevice;
import pl.grzeslowski.jsupla.protocoljava.api.entities.ds.RegisterDeviceC;
import pl.grzeslowski.jsupla.protocoljava.api.entities.sd.RegisterDeviceResult;
import pl.grzeslowski.jsupla.protocoljava.api.entities.sdc.PingServerResultClient;
import pl.grzeslowski.jsupla.protocoljava.api.entities.sdc.SetActivityTimeoutResult;
import pl.grzeslowski.jsupla.protocoljava.api.types.ToServerEntity;
import pl.grzeslowski.jsupla.server.api.Channel;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.time.Instant.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.DEVICE_TIMEOUT_SEC;
import static pl.grzeslowski.jsupla.protocol.api.ResultCode.SUPLA_RESULTCODE_TRUE;
import static reactor.core.publisher.Flux.just;

/**
 * @author Grzeslowski - Initial contribution
 */
public final class JSuplaChannel {
    private final SuplaDeviceRegistry suplaDeviceRegistry;
    private Logger logger = LoggerFactory.getLogger(JSuplaChannel.class);
    private final JSuplaCloudBridgeHandler jSuplaCloudBridgeHandler;
    private final int serverAccessId;
    private final char[] serverAccessIdPassword;
    private final JSuplaDiscoveryService jSuplaDiscoveryService;
    private final Channel channel;
    private final ScheduledExecutorService scheduledPool;
    private boolean authorized;
    private String guid;
    private AtomicReference<ScheduledFuture<?>> pingSchedule = new AtomicReference<>();
    private AtomicLong lastMessageFromDevice = new AtomicLong();
    private SuplaDeviceHandler suplaDeviceHandler;

    public JSuplaChannel(final JSuplaCloudBridgeHandler jSuplaCloudBridgeHandler,
                         final int serverAccessId,
                         final char[] serverAccessIdPassword,
                         final JSuplaDiscoveryService jSuplaDiscoveryService,
                         final Channel channel,
                         final ScheduledExecutorService scheduledPool,
                         final SuplaDeviceRegistry suplaDeviceRegistry) {
        this.jSuplaCloudBridgeHandler = requireNonNull(jSuplaCloudBridgeHandler);
        this.serverAccessId = serverAccessId;
        this.serverAccessIdPassword = serverAccessIdPassword;
        this.jSuplaDiscoveryService = requireNonNull(jSuplaDiscoveryService);
        this.channel = channel;
        this.scheduledPool = requireNonNull(scheduledPool);
        this.suplaDeviceRegistry = requireNonNull(suplaDeviceRegistry);
    }

    public synchronized void onNext(final ToServerEntity entity) {
        logger.trace("{} -> {}", guid, entity);
        lastMessageFromDevice.set(now().getEpochSecond());
        if (!authorized) {
            if (entity instanceof RegisterDevice) {
                final RegisterDevice registerDevice = (RegisterDevice) entity;
                guid = registerDevice.getGuid();
                logger = LoggerFactory.getLogger(this.getClass().getName() + "." + guid);
                authorize(guid, registerDevice.getLocationId(), registerDevice.getLocationPassword());
                if (authorized) {
                    sendDeviceToDiscoveryInbox(registerDevice);
                    sendRegistrationConfirmation();
                    bindToThingHandler(registerDevice);
                } else {
                    logger.debug("Authorization failed for GUID {}", guid);
                }
            } else {
                logger.debug("Device in channel is not authorized in but is also not sending RegisterClient entity! {}",
                        entity);
            }
        } else if (entity instanceof SetActivityTimeout) {
            setActivityTimeout();
        } else if (entity instanceof PingServer) {
            pingServer((PingServer) entity);
        } else if (entity instanceof DeviceChannelValue) {
            deviceChannelValue((DeviceChannelValue) entity);
        } else {
            logger.debug("Do not handling this command: {}", entity);
        }
    }

    public void onError(final Throwable ex) {
        if (suplaDeviceHandler != null) {
            logger.error("Error occurred in device. ", ex);
            suplaDeviceHandler.updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error occurred in channel pipe. " + ex.getLocalizedMessage());
        }
    }

    public void onComplete() {
        logger.debug("onComplete() {}", toString());
        this.jSuplaCloudBridgeHandler.completedChannel();
        if (suplaDeviceHandler != null) {
            suplaDeviceHandler.updateStatus(OFFLINE, ThingStatusDetail.NONE,
                    "Device went offline");
        }
    }

    private void setActivityTimeout() {
        final SetActivityTimeoutResult data = new SetActivityTimeoutResult(
                DEVICE_TIMEOUT_SEC,
                DEVICE_TIMEOUT_SEC - 2,
                DEVICE_TIMEOUT_SEC + 2);
        channel.write(Flux.just(data))
                .subscribe(date -> logger.trace("setActivityTimeout {} {}", data, date.format(ISO_DATE_TIME)));
        final ScheduledFuture<?> pingSchedule = scheduledPool.scheduleWithFixedDelay(
                this::checkIfDeviceIsUp,
                DEVICE_TIMEOUT_SEC * 2,
                DEVICE_TIMEOUT_SEC,
                TimeUnit.SECONDS);
        this.pingSchedule.set(pingSchedule);
    }

    private void checkIfDeviceIsUp() {
        final long now = now().getEpochSecond();
        if (now - lastMessageFromDevice.get() > DEVICE_TIMEOUT_SEC) {
            logger.debug("Device {} is dead. Need to kill it!", guid);
            channel.close();
            this.pingSchedule.get().cancel(false);
            suplaDeviceHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                    "Device do not response on pings.");
        }
    }

    private void pingServer(final PingServer entity) {
        final PingServerResultClient response = new PingServerResultClient(entity.getTimeval());
        channel.write(just(response))
                .subscribe(date -> logger.trace("pingServer {}s {}ms {}",
                        response.getTimeval().getSeconds(),
                        response.getTimeval().getSeconds(),
                        date.format(ISO_DATE_TIME)));
    }

    private void sendRegistrationConfirmation() {
        final RegisterDeviceResult result = new RegisterDeviceResult(SUPLA_RESULTCODE_TRUE.getValue(), 100, 5, 1);
        channel.write(just(result))
                .subscribe(date -> logger.trace("Send register response at {}", date.format(ISO_DATE_TIME)));
    }

    private void authorize(final String guid, final int accessId, final char[] accessIdPassword) {
        if (serverAccessId != accessId) {
            logger.debug("Wrong accessId for GUID {}; {} != {}", guid, accessId, serverAccessId);
            authorized = false;
            return;
        }
        if (!isGoodPassword(accessIdPassword)) {
            logger.debug("Wrong accessIdPassword for GUID {}", guid);
            authorized = false;
            return;
        }
        logger.debug("Authorizing GUID {}", guid);
        authorized = true;
    }

    private boolean isGoodPassword(final char[] accessIdPassword) {
        if (serverAccessIdPassword.length > accessIdPassword.length) {
            return false;
        }
        for (int i = 0; i < serverAccessIdPassword.length; i++) {
            if (serverAccessIdPassword[i] != accessIdPassword[i]) {
                return false;
            }
        }
        return true;
    }

    private void sendDeviceToDiscoveryInbox(final RegisterDevice registerClient) {
        final String name;
        if (registerClient instanceof RegisterDeviceC) {
            final RegisterDeviceC registerDeviceC = (RegisterDeviceC) registerClient;
            final String serverName = registerDeviceC.getServerName();
            if (isNullOrEmpty(serverName)) {
                name = registerDeviceC.getName();
            } else {
                name = registerDeviceC.getName() + " " + serverName;
            }
        } else {
            name = registerClient.getName();
        }
        jSuplaDiscoveryService.addSuplaDevice(registerClient.getGuid(), name);
    }

    private void bindToThingHandler(final RegisterDevice registerDevice) {
        final Optional<SuplaDeviceHandler> suplaDevice = suplaDeviceRegistry.getSuplaDevice(guid);
        if (suplaDevice.isPresent()) {
            suplaDeviceHandler = suplaDevice.get();
            suplaDeviceHandler.setChannels(registerDevice.getChannels());
            suplaDeviceHandler.setSuplaChannel(channel);
        } else {
            logger.debug("Thing not found. Binding of channels will happen later...");
            scheduledPool.schedule(
                    () -> bindToThingHandler(registerDevice),
                    DEVICE_TIMEOUT_SEC,
                    SECONDS);
        }
    }

    private void deviceChannelValue(final DeviceChannelValue entity) {
        suplaDeviceHandler.updateStatus(entity.getChannelNumber(), entity.getValue());
    }

    @Override
    public String toString() {
        return "JSuplaChannel{" +
                       "authorized=" + authorized +
                       ", guid='" + guid + '\'' +
                       '}';
    }
}
