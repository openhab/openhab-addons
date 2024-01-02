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

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ism8.server.DataPointChangedEvent;
import org.openhab.binding.ism8.server.IDataPoint;
import org.openhab.binding.ism8.server.IDataPointChangeListener;
import org.openhab.binding.ism8.server.Server;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
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
    public void handleCommand(ChannelUID channelUID, Command command) {
        this.logger.debug("Ism8: Handle command = {} {}", channelUID.getId(), command);
        Channel channel = getThing().getChannel(channelUID);
        Server svr = this.server;
        if (channel != null && svr != null) {
            if (channel.getConfiguration().containsKey("id")) {
                IDataPoint dataPoint = null;
                try {
                    int id = Integer.parseInt(channel.getConfiguration().get("id").toString());
                    this.logger.debug("Channel '{}' writting into ID '{}'", channel.getUID().getId(), id);
                    this.updateState(channelUID, new QuantityType<>(command.toString()));
                    dataPoint = svr.getDataPoint(id);
                } catch (NumberFormatException e) {
                    this.logger.debug("Updating State of ISM DataPoint '{}' failed. '{}'", channel.getConfiguration(),
                            e.getMessage());
                }

                if (dataPoint != null) {
                    try {
                        svr.sendData(dataPoint.createWriteData(command));
                    } catch (IOException e) {
                        this.logger.debug("Writting to ISM DataPoint '{}' failed. '{}'", dataPoint.getId(),
                                e.getMessage());
                    }
                }
            }
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
    public void initialize() {
        this.config = getConfigAs(Ism8Configuration.class);
        Ism8Configuration cfg = this.config;
        final String uid = this.getThing().getUID().getAsString();
        Server svr = new Server(cfg.getPortNumber(), uid);
        for (Channel channel : getThing().getChannels()) {
            if (channel.getConfiguration().containsKey("id") && channel.getConfiguration().containsKey("type")) {
                try {
                    int id = Integer.parseInt(channel.getConfiguration().get("id").toString());
                    String type = channel.getConfiguration().get("type").toString();
                    String description = channel.getLabel();
                    if (type != null && description != null) {
                        svr.addDataPoint(id, type, description);
                    }
                } catch (NumberFormatException e) {
                    this.logger.warn(
                            "Ism8 initialize: ID couldn't be converted correctly. Check the configuration of channel {}. Cfg={}",
                            channel.getLabel(), channel.getConfiguration());
                }
            } else {
                this.logger.debug("Ism8: ID or type missing - Channel={}  Cfg={}", channel.getLabel(),
                        channel.getConfiguration());
            }
            this.logger.debug("Ism8: Channel={}", channel.getConfiguration().toString());
        }

        this.updateStatus(ThingStatus.UNKNOWN);
        svr.addDataPointChangeListener(this);
        scheduler.execute(svr::start);
        this.server = svr;
    }

    @Override
    public void dataPointChanged(@Nullable DataPointChangedEvent e) {
        if (e != null) {
            IDataPoint dataPoint = e.getDataPoint();
            if (dataPoint != null) {
                this.logger.debug("Ism8: dataPointChanged {}", dataPoint.toString());
                this.updateDataPoint(dataPoint);
            }
        }
    }

    @Override
    public void connectionStatusChanged(ThingStatus status) {
        this.updateStatus(status);
    }

    private void updateDataPoint(IDataPoint dataPoint) {
        this.updateStatus(ThingStatus.ONLINE);
        for (Channel channel : getThing().getChannels()) {
            if (channel.getConfiguration().containsKey("id")) {
                try {
                    int id = Integer.parseInt(channel.getConfiguration().get("id").toString());
                    if (id == dataPoint.getId()) {
                        this.logger.debug("Ism8 updateDataPoint ID:{} {}", dataPoint.getId(), dataPoint.getValueText());
                        Object val = dataPoint.getValueObject();
                        if (val != null) {
                            updateState(channel.getUID(), new QuantityType<>(val.toString()));
                        }
                    }
                } catch (NumberFormatException e) {
                    this.logger.warn(
                            "Ism8 updateDataPoint: ID couldn't be converted correctly. Check the configuration of channel {}. {}",
                            channel.getLabel(), e.getMessage());
                }
            }
        }
    }
}
