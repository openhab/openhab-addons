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
package org.openhab.binding.qolsysiq.internal.handler;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qolsysiq.internal.QolsysIQBindingConstants;
import org.openhab.binding.qolsysiq.internal.client.QolsysIQClientListener;
import org.openhab.binding.qolsysiq.internal.client.QolsysiqClient;
import org.openhab.binding.qolsysiq.internal.client.dto.action.Action;
import org.openhab.binding.qolsysiq.internal.client.dto.action.InfoAction;
import org.openhab.binding.qolsysiq.internal.client.dto.action.InfoActionType;
import org.openhab.binding.qolsysiq.internal.client.dto.event.AlarmEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.event.ArmingEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.event.ErrorEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.event.SecureArmInfoEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.event.SummaryInfoEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.event.ZoneActiveEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.event.ZoneAddEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.event.ZoneUpdateEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.model.Partition;
import org.openhab.binding.qolsysiq.internal.config.QolsysIQPanelConfiguration;
import org.openhab.binding.qolsysiq.internal.discovery.QolsysIQChildDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QolsysIQPanelHandler} connects to a security panel and routes messages to child partitions.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class QolsysIQPanelHandler extends BaseBridgeHandler
        implements QolsysIQClientListener, QolsysIQChildDiscoveryHandler {
    private final Logger logger = LoggerFactory.getLogger(QolsysIQPanelHandler.class);
    private static final int QUICK_RETRY_SECONDS = 1;
    private static final int LONG_RETRY_SECONDS = 30;
    private static final int HEARTBEAT_SECONDS = 30;
    private @Nullable QolsysiqClient apiClient;
    private @Nullable ScheduledFuture<?> retryFuture;
    private @Nullable QolsysIQChildDiscoveryService discoveryService;
    private List<Partition> partitions = Collections.synchronizedList(new LinkedList<Partition>());
    private String key = "";

    public QolsysIQPanelHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand {}", command);
        if (command instanceof RefreshType) {
            refresh();
        }
    }

    @Override
    public void initialize() {
        logger.debug("initialize");
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            connect();
        });
    }

    @Override
    public void dispose() {
        stopRetryFuture();
        disconnect();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(QolsysIQChildDiscoveryService.class);
    }

    @Override
    public void setDiscoveryService(QolsysIQChildDiscoveryService service) {
        this.discoveryService = service;
    }

    @Override
    public void startDiscovery() {
        refresh();
    }

    @Override
    public void disconnected(Exception reason) {
        logger.debug("disconnected", reason);
        setOfflineAndReconnect(reason, QUICK_RETRY_SECONDS);
    }

    @Override
    public void alarmEvent(AlarmEvent event) {
        logger.debug("AlarmEvent {}", event.partitionId);
        QolsysIQPartitionHandler handler = partitionHandler(event.partitionId);
        if (handler != null) {
            handler.alarmEvent(event);
        }
    }

    @Override
    public void armingEvent(ArmingEvent event) {
        logger.debug("ArmingEvent {}", event.partitionId);
        QolsysIQPartitionHandler handler = partitionHandler(event.partitionId);
        if (handler != null) {
            handler.armingEvent(event);
        }
    }

    @Override
    public void errorEvent(ErrorEvent event) {
        logger.debug("ErrorEvent {}", event.partitionId);
        QolsysIQPartitionHandler handler = partitionHandler(event.partitionId);
        if (handler != null) {
            handler.errorEvent(event);
        }
    }

    @Override
    public void summaryInfoEvent(SummaryInfoEvent event) {
        logger.debug("SummaryInfoEvent");
        synchronized (partitions) {
            partitions.clear();
            partitions.addAll(event.partitionList);
        }
        updatePartitions();
        discoverChildDevices();
    }

    @Override
    public void secureArmInfoEvent(SecureArmInfoEvent event) {
        logger.debug("ArmingEvent {}", event.value);
        QolsysIQPartitionHandler handler = partitionHandler(event.partitionId);
        if (handler != null) {
            handler.secureArmInfoEvent(event);
        }
    }

    @Override
    public void zoneActiveEvent(ZoneActiveEvent event) {
        logger.debug("ZoneActiveEvent {} {}", event.zone.zoneId, event.zone.status);
        partitions.forEach(p -> {
            if (p.zoneList.stream().filter(z -> z.zoneId.equals(event.zone.zoneId)).findAny().isPresent()) {
                QolsysIQPartitionHandler handler = partitionHandler(p.partitionId);
                if (handler != null) {
                    handler.zoneActiveEvent(event);
                }
            }
        });
    }

    @Override
    public void zoneUpdateEvent(ZoneUpdateEvent event) {
        logger.debug("ZoneUpdateEvent {}", event.zone.name);
        partitions.forEach(p -> {
            if (p.zoneList.stream().filter(z -> z.zoneId.equals(event.zone.zoneId)).findAny().isPresent()) {
                QolsysIQPartitionHandler handler = partitionHandler(p.partitionId);
                if (handler != null) {
                    handler.zoneUpdateEvent(event);
                }
            }
        });
    }

    @Override
    public void zoneAddEvent(ZoneAddEvent event) {
        logger.debug("ZoneAddEvent {}", event.zone.name);
        partitions.forEach(p -> {
            if (p.zoneList.stream().filter(z -> z.zoneId.equals(event.zone.zoneId)).findAny().isPresent()) {
                QolsysIQPartitionHandler handler = partitionHandler(p.partitionId);
                if (handler != null) {
                    handler.zoneAddEvent(event);
                }
            }
        });
    }

    /**
     * Sends the action to the panel. This will replace the token of the action passed in with the one configured here
     *
     * @param action
     */
    protected void sendAction(Action action) {
        action.token = key;
        QolsysiqClient client = this.apiClient;
        if (client != null) {
            try {
                client.sendAction(action);
            } catch (IOException e) {
                logger.debug("Could not send action", e);
                setOfflineAndReconnect(e, QUICK_RETRY_SECONDS);
            }
        }
    }

    protected synchronized void refresh() {
        sendAction(new InfoAction(InfoActionType.SUMMARY));
    }

    /**
     * Connect the client
     */
    private synchronized void connect() {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            logger.debug("connect: Bridge is already connected");
            return;
        }
        QolsysIQPanelConfiguration config = getConfigAs(QolsysIQPanelConfiguration.class);
        key = config.key;

        try {
            QolsysiqClient apiClient = new QolsysiqClient(config.hostname, config.port, HEARTBEAT_SECONDS, scheduler,
                    "OH-binding-" + getThing().getUID().getAsString());
            apiClient.connect();
            apiClient.addListener(this);
            this.apiClient = apiClient;
            refresh();
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            logger.debug("Could not connect");
            setOfflineAndReconnect(e, LONG_RETRY_SECONDS);
        }
    }

    /**
     * Disconnects the client and removes listeners
     */
    private void disconnect() {
        logger.debug("disconnect");
        QolsysiqClient apiClient = this.apiClient;
        if (apiClient != null) {
            apiClient.removeListener(this);
            apiClient.disconnect();
            this.apiClient = null;
        }
    }

    private void startRetryFuture(int seconds) {
        stopRetryFuture();
        logger.debug("startRetryFuture");
        this.retryFuture = scheduler.schedule(this::connect, seconds, TimeUnit.SECONDS);
    }

    private void stopRetryFuture() {
        logger.debug("stopRetryFuture");
        ScheduledFuture<?> retryFuture = this.retryFuture;
        if (retryFuture != null) {
            retryFuture.cancel(true);
            this.retryFuture = null;
        }
    }

    private void setOfflineAndReconnect(Exception reason, int seconds) {
        logger.debug("setOfflineAndReconnect");
        disconnect();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason.getMessage());
        startRetryFuture(seconds);
    }

    private void updatePartitions() {
        synchronized (partitions) {
            partitions.forEach(p -> {
                QolsysIQPartitionHandler handler = partitionHandler(p.partitionId);
                if (handler != null) {
                    handler.updatePartition(p);
                }
            });
        }
    }

    private void discoverChildDevices() {
        synchronized (partitions) {
            QolsysIQChildDiscoveryService discoveryService = this.discoveryService;
            if (discoveryService != null) {
                partitions.forEach(p -> {
                    ThingUID bridgeUID = getThing().getUID();
                    ThingUID thingUID = new ThingUID(QolsysIQBindingConstants.THING_TYPE_PARTITION, bridgeUID,
                            String.valueOf(p.partitionId));
                    discoveryService.discoverQolsysIQChildThing(thingUID, bridgeUID, p.partitionId,
                            "Qolsys IQ Partition: " + p.name);
                });
            }
        }
    }

    private @Nullable QolsysIQPartitionHandler partitionHandler(int partitionId) {
        for (Thing thing : getThing().getThings()) {
            ThingHandler handler = thing.getHandler();
            if (handler instanceof QolsysIQPartitionHandler) {
                if (((QolsysIQPartitionHandler) handler).getPartitionId() == partitionId) {
                    return (QolsysIQPartitionHandler) handler;
                }
            }
        }
        return null;
    }
}
