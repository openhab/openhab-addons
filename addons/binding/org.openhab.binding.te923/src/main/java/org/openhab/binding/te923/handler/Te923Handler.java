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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.te923.Te923Channel;
import org.openhab.binding.te923.internal.ExecUtil;
import org.openhab.binding.te923.internal.conf.Te923Configuration;
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

    // Number of parameters returned by the te923con command.
    private static final int COUNT_PARAMS_TE_CMD = 22;

    @Nullable
    private Te923Configuration te923Configuration = null;

    @Nullable
    private ScheduledFuture<?> refreshJob;

    public Te923Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {

        te923Configuration = getConfigAs(Te923Configuration.class);

        // Can we get data from the te923con command?
        // If we cannot get the data, set the status to offline and don't start the automatic refresh
        double d[] = getTe923Data();
        if (d == null) {
            return;
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

        @SuppressWarnings("null")
        String cmdte923con = te923Configuration == null ? "" : te923Configuration.cmdte923con;

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
        if (size != COUNT_PARAMS_TE_CMD) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unexpected answer from cmd command line: " + r + " - Size is " + size + " and should be "
                            + COUNT_PARAMS_TE_CMD);
            return null;
        }

        double d[] = new double[COUNT_PARAMS_TE_CMD];
        for (int i = 0; i < COUNT_PARAMS_TE_CMD; i++) {
            try {
                d[i] = Double.parseDouble(ss[i]);
            } catch (NumberFormatException t) {
                d[i] = Double.NaN;
            }
        }

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }

        return d;
    }

    /**
     * Start the job that refresh data automatically
     */
    private void startAutomaticRefresh() {

        @SuppressWarnings("null")
        long refresh = te923Configuration == null ? 60 : te923Configuration.refresh;

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {

            double d[] = getTe923Data();
            if (d == null) {
                return;
            }

            try {
                for (Te923Channel c : Te923Channel.values()) {
                    if (!Double.isNaN(d[c.getPositionInResponse()])) {
                        updateState(new ChannelUID(getThing().getUID(), c.getMappingName()),
                                new DecimalType(d[c.getPositionInResponse()]));
                    }
                }
            } catch (RuntimeException e) {
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

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // NOP
    }

}
