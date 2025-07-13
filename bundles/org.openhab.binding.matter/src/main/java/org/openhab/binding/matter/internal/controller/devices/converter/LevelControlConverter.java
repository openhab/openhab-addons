/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.openhab.binding.matter.internal.MatterBindingConstants.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.LevelControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OnOffCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.binding.matter.internal.util.ValueUtils;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;

/**
 * A converter for translating {@link LevelControlCluster} events and attributes to openHAB channels and back again.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class LevelControlConverter extends GenericConverter<LevelControlCluster> {
    private static final int UPDATE_DELAY_MS = 200;
    private @Nullable ScheduledFuture<?> levelFuture = null;
    private ScheduledExecutorService updateScheduler = Executors.newSingleThreadScheduledExecutor();

    private PercentType lastLevel = new PercentType(0);
    private OnOffType lastOnOff = OnOffType.OFF;
    private Integer onTransitionTime = 0;
    private Integer offTransitionTime = 0;
    private Integer defaultMoveRate = 0;

    public LevelControlConverter(LevelControlCluster cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);

        if (cluster.onTransitionTime != null) {
            onTransitionTime = cluster.onTransitionTime;
        } else if (cluster.onOffTransitionTime != null) {
            onTransitionTime = cluster.onOffTransitionTime;
        }

        if (cluster.offTransitionTime != null) {
            offTransitionTime = cluster.offTransitionTime;
        } else if (cluster.onOffTransitionTime != null) {
            offTransitionTime = cluster.onOffTransitionTime;
        }

        if (cluster.defaultMoveRate != null) {
            defaultMoveRate = cluster.defaultMoveRate;
        }
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Channel channel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_LEVEL_LEVEL), CoreItemFactory.DIMMER)
                .withType(CHANNEL_LEVEL_LEVEL).build();
        return Collections.singletonMap(channel, null);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType onOffType) {
            // lighting types always have a OnOff cluster
            if (initializingCluster.featureMap.lighting) {
                ClusterCommand onOffCommand = onOffType == OnOffType.ON ? OnOffCluster.on() : OnOffCluster.off();
                handler.sendClusterCommand(endpointNumber, OnOffCluster.CLUSTER_NAME, onOffCommand);
            } else {
                ClusterCommand levelCommand = LevelControlCluster.moveToLevelWithOnOff(
                        onOffType == OnOffType.OFF ? 0 : 100,
                        onOffType == OnOffType.OFF ? offTransitionTime : onTransitionTime, initializingCluster.options,
                        initializingCluster.options);
                handler.sendClusterCommand(endpointNumber, LevelControlCluster.CLUSTER_NAME, levelCommand);
            }
        } else if (command instanceof PercentType percentType) {
            // Min Level for lighting devices is 1, so send OFF if we get a 0
            if (percentType.equals(PercentType.ZERO) && initializingCluster.featureMap.lighting) {
                handler.sendClusterCommand(endpointNumber, OnOffCluster.CLUSTER_NAME, OnOffCluster.off());
            } else {
                ClusterCommand levelCommand = LevelControlCluster.moveToLevelWithOnOff(
                        ValueUtils.percentToLevel(percentType), defaultMoveRate, initializingCluster.options,
                        initializingCluster.options);
                handler.sendClusterCommand(endpointNumber, LevelControlCluster.CLUSTER_NAME, levelCommand);
            }
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL:
                clearUpdateTimer();
                Integer numberValue = message.value instanceof Number number ? number.intValue() : 0;
                lastLevel = ValueUtils.levelToPercent(numberValue);
                logger.debug("currentLevel {}", lastLevel);
                if (lastOnOff == OnOffType.ON) {
                    updateState(CHANNEL_ID_LEVEL_LEVEL, lastLevel);
                }
                break;
            case OnOffCluster.ATTRIBUTE_ON_OFF:
                logger.debug("onOff {}", message.value);
                clearUpdateTimer();
                if (message.value instanceof Boolean booleanValue) {
                    lastOnOff = OnOffType.from(booleanValue);
                    if (booleanValue) {
                        // most ON commands are followed by a currentLevel update, but not always
                        // We wil wait just a bit before sending the last known level to avoid multiple updates
                        levelFuture = updateScheduler.schedule(() -> {
                            updateState(CHANNEL_ID_LEVEL_LEVEL, lastLevel);
                        }, UPDATE_DELAY_MS, TimeUnit.MILLISECONDS);
                    } else {
                        updateState(CHANNEL_ID_LEVEL_LEVEL, OnOffType.OFF);
                    }

                }
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        // default to on when not used as part of the lighting type
        initState(true);
    }

    public void initState(boolean onOff) {
        lastOnOff = OnOffType.from(onOff);
        lastLevel = ValueUtils.levelToPercent(initializingCluster.currentLevel);
        updateState(CHANNEL_ID_LEVEL_LEVEL, onOff ? lastLevel : OnOffType.OFF);
    }

    private void clearUpdateTimer() {
        ScheduledFuture<?> levelFuture = this.levelFuture;
        if (levelFuture != null) {
            levelFuture.cancel(true);
            this.levelFuture = null;
        }
    }
}
