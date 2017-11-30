/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.te923.handler;

import java.io.File;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.te923.Te923BindingConstants;
import org.openhab.binding.te923.Te923Channel;
import org.openhab.binding.te923.internal.ExecUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Te923Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gabriel Klein - Initial contribution
 */
@NonNullByDefault
public class Te923Handler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(Te923Handler.class);

    // don't "spam" the Te923 command. Max one call per 3 secs
    private static final long MIN_UPDATE_TIME = 3000;

    // refresh frequency
    private long refresh = 60;

    private String cmdte923con = Te923BindingConstants.DEFAULT_CMD;

    @Nullable
    private ScheduledFuture<?> refreshJob;

    public Te923Handler(Thing thing) {
        super(thing);
    }

    private double[] te923Data = new double[22];
    private long lastTE923Update = 0;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.debug("Received channel: {}, command: {}", channelUID, command);

        // No real commands

    }

    @Override
    public void initialize() {

        // updateStatus(ThingStatus.INITIALIZING);

        Configuration config = getThing().getConfiguration();

        try {
            refresh = Long.parseLong("" + config.get("refresh"));
        } catch (Exception e) {
            logger.debug("Cannot set refresh parameter.", e);
        }

        if (refresh < 5) {
            logger.debug("Cannot set refresh parameter to a value lower than 5");
            refresh = 5;
        }

        try {
            Object o = config.get("cmdte923con");
            if (o == null) {
                o = "";
            }
            cmdte923con = "" + o;
        } catch (Exception e) {
            logger.debug("Cannot set cmdte923con parameter.", e);
        }

        // Check that cmdte923 exists
        {
            if (cmdte923con == null || cmdte923con.length() == 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Location of te923con is not configured. More information on http://te923.fukz.org/");
                return;
            }

            double d[] = getTe923Data();
            if (d == null) {
                return;
            }

        }

        startAutomaticRefresh();

    }

    /**
     * Get Te923 Data. Null if no data.
     * This command update the status of this component.
     *
     * @return
     */
    private double @Nullable [] getTe923Data() {

        // avoid spamming the system.
        if (System.currentTimeMillis() - lastTE923Update < MIN_UPDATE_TIME) {
            return te923Data;
        }

        File f = new File(cmdte923con);
        if (!f.exists()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "File te923con does not exist: " + cmdte923con + ". More information on http://te923.fukz.org/");
            return null;
        }

        if (!f.canExecute()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "File te923con cannot be executed: " + cmdte923con);
            return null;
        }

        // TE923 weather station cannot be found, sorry.
        String r = ExecUtil.exec(cmdte923con);
        if (r.contains("cannot be found")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "USB device te923con cannot be found: [Error: " + r
                            + "]. Has the openhab user access to libusb: 'addgroup openhab plugdev'");
            return null;
        }

        String ss[] = r.split(":");

        int size = ss.length;
        if (r == null || size != 22) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unexpected answer from cmd command line: " + r + " - Size is " + size + " and should be 22");
            return null;
        }

        double d[] = new double[22];
        for (int i = 0; i < 22; i++) {
            try {
                d[i] = Double.parseDouble(ss[i]);
            } catch (Throwable t) {
                d[i] = Double.NaN;
            }
        }

        te923Data = d;
        lastTE923Update = System.currentTimeMillis();

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }

        return d;
    }

    /**
     * Start the job that refresh data automatically
     */
    private void startAutomaticRefresh() {
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {

            double d[] = getTe923Data();
            if (d == null) {
                return;
            }

            try {
                // boolean success = updateWeatherData();
                for (Te923Channel c : Te923Channel.values()) {
                    if (!Double.isNaN(d[c.getPositionInResponse()])) {
                        updateState(new ChannelUID(getThing().getUID(), c.getMappingName()),
                                new DecimalType(d[c.getPositionInResponse()]));
                    } else {
                        // updateState(new ChannelUID(getThing().getUID(), c.getMappingName()), null);
                        // Configuration s = getThing().getChannel(c.getMappingName()).getConfiguration();
                        // logger.info("xxxxxxxxxxxxxxx" + s);
                    }
                }
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }, 0, refresh, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        refreshJob = null;
    }

}
