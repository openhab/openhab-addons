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
package org.openhab.binding.onewire.internal.device;

import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.DigitalIoConfig;
import org.openhab.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.openhab.binding.onewire.internal.owserver.OwserverDeviceParameter;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractDigitalOwDevice} class defines an abstract digital I/O device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractDigitalOwDevice extends AbstractOwDevice {
    private final Logger logger = LoggerFactory.getLogger(AbstractDigitalOwDevice.class);

    protected @NonNullByDefault({}) OwserverDeviceParameter fullInParam;
    protected @NonNullByDefault({}) OwserverDeviceParameter fullOutParam;

    protected final List<DigitalIoConfig> ioConfig = new ArrayList<>();

    public AbstractDigitalOwDevice(SensorId sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
    }

    @Override
    public void configureChannels() throws OwException {
        Thing thing = callback.getThing();
        OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider = callback
                .getDynamicStateDescriptionProvider();

        for (Integer i = 0; i < ioConfig.size(); i++) {
            String channelId = ioConfig.get(i).getChannelId();
            Channel channel = thing.getChannel(channelId);

            if (channel != null) {
                Configuration channelConfig = channel.getConfiguration();

                try {
                    if (channelConfig.get(CONFIG_DIGITAL_MODE) != null) {
                        ioConfig.get(i).setIoMode((String) channelConfig.get(CONFIG_DIGITAL_MODE));
                    }
                    if (channelConfig.get(CONFIG_DIGITAL_LOGIC) != null) {
                        ioConfig.get(i).setIoLogic((String) channelConfig.get(CONFIG_DIGITAL_LOGIC));
                    }
                } catch (IllegalArgumentException e) {
                    throw new OwException(channelId + " has invalid configuration");
                }

                if (dynamicStateDescriptionProvider != null) {
                    StateDescription stateDescription = StateDescriptionFragmentBuilder.create()
                            .withReadOnly(ioConfig.get(i).isInput()).build().toStateDescription();
                    if (stateDescription != null) {
                        dynamicStateDescriptionProvider.setDescription(ioConfig.get(i).getChannelUID(),
                                stateDescription);
                    } else {
                        logger.warn("Failed to create state description in thing {}", thing.getUID());
                    }
                } else {
                    logger.debug(
                            "state description may be inaccurate, state description provider not available in thing {}",
                            thing.getUID());
                }

                logger.debug("configured {} channel {}: {}", thing.getUID(), i, ioConfig.get(i));
            } else {
                throw new OwException(channelId + " not found");
            }
        }

        isConfigured = true;
    }

    /**
     * refreshes this sensor - note that the update interval check is not performed as its and i/o device
     */
    @Override
    public void refresh(OwserverBridgeHandler bridgeHandler, Boolean forcedRefresh) throws OwException {
        logger.trace("refresh of sensor {} started", sensorId);
        if (isConfigured) {
            State state;

            BitSet statesSensed = bridgeHandler.readBitSet(sensorId, fullInParam);
            BitSet statesPIO = bridgeHandler.readBitSet(sensorId, fullOutParam);

            for (int i = 0; i < ioConfig.size(); i++) {
                if (ioConfig.get(i).isInput()) {
                    state = ioConfig.get(i).convertState(statesSensed.get(i));
                    logger.trace("{} IN{}: raw {}, final {}", sensorId, i, statesSensed, state);
                } else {
                    state = ioConfig.get(i).convertState(statesPIO.get(i));
                    logger.trace("{} OUT{}: raw {}, final {}", sensorId, i, statesPIO, state);
                }
                callback.postUpdate(ioConfig.get(i).getChannelId(), state);
            }
        }
    }

    /**
     * get the number of channels
     *
     * @return number of channels
     */
    public int getChannelCount() {
        return ioConfig.size();
    }

    public boolean writeChannel(OwserverBridgeHandler bridgeHandler, Integer ioChannel, Command command) {
        if (ioChannel < getChannelCount()) {
            try {
                if (ioConfig.get(ioChannel).isOutput()) {
                    bridgeHandler.writeDecimalType(sensorId, ioConfig.get(ioChannel).getParameter(),
                            ioConfig.get(ioChannel).convertState((OnOffType) command));
                    return true;
                } else {
                    return false;
                }
            } catch (OwException e) {
                logger.info("could not write {} to {}: {}", command, ioChannel, e.getMessage());
                return false;
            }
        } else {
            throw new IllegalArgumentException("channel number out of range");
        }
    }
}
