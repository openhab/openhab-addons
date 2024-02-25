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
package org.openhab.binding.ism8.internal;

import static org.openhab.binding.ism8.internal.Ism8BindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ism8.internal.util.Ism8DomainMap;
import org.openhab.binding.ism8.server.DataPointChangedEvent;
import org.openhab.binding.ism8.server.IDataPoint;
import org.openhab.binding.ism8.server.IDataPointChangeListener;
import org.openhab.binding.ism8.server.Server;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Ism8Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public class Ism8Handler extends BaseThingHandler implements IDataPointChangeListener {
    private final Logger logger = LoggerFactory.getLogger(Ism8Handler.class);

    private @Nullable Ism8Configuration config;
    private @Nullable Server server;

    public Ism8Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(Ism8Configuration.class);
        Ism8Configuration cfg = this.config;
        final String uid = this.getThing().getUID().getAsString();
        Server svr = new Server(cfg.getPortNumber(), uid);
        this.server = svr;
        for (Channel channel : getThing().getChannels()) {
            Configuration channelConfig = channel.getConfiguration();
            if (registerDataPointToServer(channelConfig, channel.getLabel())) {
                logger.debug("Ism8: Channel={} registered datapoint", channelConfig.toString());
            } else {
                logger.warn("Ism8: Channel={} failed to register datapoint", channelConfig.toString());
            }
        }

        this.updateStatus(ThingStatus.UNKNOWN);
        svr.addDataPointChangeListener(this);
        scheduler.execute(svr::start);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Ism8: Handle command = {} {}", channelUID.getId(), command);
        Channel channel = getThing().getChannel(channelUID);
        if (channel == null) {
            return;
        }
        IDataPoint dataPoint = getDataPoint(channel);
        if (dataPoint == null) {
            return;
        }
        if (command == RefreshType.REFRESH) {
            updateChannel(dataPoint);
        } else {
            setDataPoint(dataPoint, command);
        }
    }

    @Override
    public void dispose() {
        Server svr = this.server;
        if (svr != null) {
            svr.stopServerThread();
        }
    }

    @Override
    public void dataPointChanged(@Nullable DataPointChangedEvent e) {
        if (e != null) {
            IDataPoint dataPoint = e.getDataPoint();
            if (dataPoint != null) {
                logger.debug("Ism8: dataPointChanged {}", dataPoint.toString());
                this.updateChannel(dataPoint);
            }
        }
    }

    @Override
    public void connectionStatusChanged(ThingStatus status) {
        this.updateStatus(status);
    }

    private boolean registerDataPointToServer(Configuration config, @Nullable String description) {
        Server svr = this.server;
        if (config.containsKey(CHANNEL_CONFIG_ID) && config.containsKey(CHANNEL_CONFIG_TYPE)) {
            try {
                int id = Integer.parseInt(config.get(CHANNEL_CONFIG_ID).toString());
                String type = config.get(CHANNEL_CONFIG_TYPE).toString();
                if (svr != null && type != null && description != null) {
                    svr.addDataPoint(id, type, description);
                    return true;
                }
            } catch (NumberFormatException e) {
                logger.warn("Ism8: ID couldn't be converted correctly. Check the configuration of channel {}. Cfg={}",
                        description, config);
            }
        } else {
            logger.debug("Ism8: ID or type missing - Channel={}  Cfg={}", description, config);
        }
        return false;
    }

    private void setDataPoint(IDataPoint dataPoint, Command command) {
        Server svr = this.server;
        if (svr != null) {
            try {
                svr.sendData(Ism8DomainMap.toISM8WriteData(dataPoint, command));
            } catch (IOException e) {
                logger.debug("Writting to Ism8 DataPoint '{}' failed. '{}'", dataPoint.getId(), e.getMessage());
            }
        }
    }

    private @Nullable IDataPoint getDataPoint(Channel channel) {
        IDataPoint dataPoint = null;
        Configuration config = channel.getConfiguration();
        Server svr = this.server;
        if (svr == null) {
            return dataPoint;
        }
        if (config.containsKey(CHANNEL_CONFIG_ID) && config.containsKey(CHANNEL_CONFIG_TYPE)) {
            try {
                int id = Integer.parseInt(config.get(CHANNEL_CONFIG_ID).toString());
                dataPoint = svr.getDataPoint(id);
            } catch (NumberFormatException e) {
                logger.debug("Retrieving Ism8 DataPoint '{}' failed. '{}'", channel.getConfiguration(), e.getMessage());
            }
        } else {
            logger.debug("Ism8: ID or type missing - Channel={}  Cfg={}", channel.getLabel(),
                    channel.getConfiguration());
        }
        return dataPoint;
    }

    private boolean updateChannel(Channel channel, IDataPoint dataPoint) {
        try {
            int id = Integer.parseInt(channel.getConfiguration().get(CHANNEL_CONFIG_ID).toString());
            if (id == dataPoint.getId()) {
                if (dataPoint.getValueObject() != null) {
                    logger.debug("Ism8: updating channel {} with datapoint: {}", channel.getUID().getAsString(),
                            dataPoint.getId());
                    updateState(channel.getUID(), Ism8DomainMap.toOpenHABState(dataPoint));
                    return true;
                }
            } else {
                logger.debug("Ism8 channel: {} and DataPoint do not have a matching Id: {} vs {}", channel.getUID(), id,
                        dataPoint.getId());
            }
        } catch (NumberFormatException e) {
            logger.warn(
                    "Ism8 updateChannel: ID couldn't be converted correctly. Check the configuration of channel {}. {}",
                    channel.getLabel(), e.getMessage());
        }
        return false;
    }

    private void updateChannel(IDataPoint dataPoint) {
        this.updateStatus(ThingStatus.ONLINE);
        for (Channel channel : getThing().getChannels()) {
            if (channel.getConfiguration().containsKey(CHANNEL_CONFIG_ID)) {
                if (updateChannel(channel, dataPoint)) {
                    break;
                }
            }
        }
        logger.debug("Ism8: no channel was found for DataPoint id: {}", dataPoint.getId());
    }
}
