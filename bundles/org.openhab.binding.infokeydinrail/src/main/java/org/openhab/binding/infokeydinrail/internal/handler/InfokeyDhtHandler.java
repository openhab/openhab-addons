/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.infokeydinrail.internal.handler;

import static org.openhab.binding.infokeydinrail.internal.InfokeyBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.infokeydinrail.internal.DhtModuleRunnable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InfokeyDhtHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * This GPIO provider implements the DHT 11 / 22 / AM2302 as native device.
 * </p>
 *
 * <p>
 * The DHT 11 / 22 / AM2302 is connected via Custom Rpi Python Server and get results
 * </p>
 *
 * @author Themistoklis Anastasopoulos - Initial contribution
 */
// @NonNullByDefault
public class InfokeyDhtHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ScheduledFuture<?> pollingJob;
    private String serverIP = "127.0.0.1";
    private Integer pollingInterval = 1000;
    private Boolean initializedPolling = false;

    public InfokeyDhtHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub
    }

    @Override
    public void initialize() {
        try {
            checkConfiguration();
            this.getThing();
            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalArgumentException | SecurityException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "An exception occurred while adding pin. Check pin configuration. Exception: " + e.getMessage());
        }
    }

    protected void checkConfiguration() {
        Configuration configuration = getConfig();
        serverIP = configuration.get(DHT_SERVER_IP).toString();
        pollingInterval = Integer.parseInt((configuration.get(DHT_POLLING_INTERVAL)).toString());
        logger.debug("initializing infokey dht provider for server IP {} and polling interval {} ms", serverIP,
                pollingInterval);
    }

    private void initializeScheduler(ChannelUID channel) {
        logger.debug("initializing scheduler for channel {}", channel.getAsString());

        try {

            Runnable runnable = new DhtModuleRunnable(this, this.thing, serverIP);
            pollingJob = scheduler.scheduleAtFixedRate(runnable, 0, pollingInterval, TimeUnit.MILLISECONDS);

        } catch (Exception ex) {
            logger.debug("Ops!", ex);
        }
    }

    public void updateValue(ChannelUID channelUID, String value) {
        // logger.debug("4. updateValue {} / {}", channelUID.toString(), value);
        updateState(channelUID, new StringType(value));
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        synchronized (this) {
            logger.debug("channel linked {}", channelUID.getAsString());

            String channelGroup = channelUID.getGroupId();

            if (channelGroup != null && channelGroup.equals(CHANNEL_GROUP_DHT_INPUT)) {

                logger.debug("channelLinked for channelGroup {} and initialized {} ", channelGroup, initializedPolling);

                if (!initializedPolling) {
                    initializedPolling = true;
                    initializeScheduler(channelUID);
                }

            }
            super.channelLinked(channelUID);
        }
    }

    public Configuration getConfigFile() {
        return getConfig();
    }
}
