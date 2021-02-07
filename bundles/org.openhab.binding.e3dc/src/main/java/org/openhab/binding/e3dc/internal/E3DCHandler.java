/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Björn Brings - Initial contribution
 */
@NonNullByDefault
public class E3DCHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(E3DCHandler.class);

    private @Nullable E3DCConfiguration config;
    private @Nullable ScheduledFuture<?> readDataJob;
    private @Nullable E3DCConnector e3dcconnect;

    public E3DCHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        this.logger.debug("handleCommand channel:{}  command:{}", channelUID, command);
        if (command instanceof RefreshType) {
            return;
        }

        if (E3DCBindingConstants.CHANNEL_WeatherRegulatedCharge.equals(channelUID.getId())) {
            boolean value = (command == OnOffType.ON);
            e3dcconnect.setWeatherRegulatedChargeEnable(value);
        }

        if (E3DCBindingConstants.CHANNEL_PowerSave.equals(channelUID.getId())) {
            boolean value = (command == OnOffType.ON);
            e3dcconnect.setPowerSaveEnable(value);
        }

        if (E3DCBindingConstants.CHANNEL_PowerLimitsUsed.equals(channelUID.getId())) {
            boolean value = (command == OnOffType.ON);
            e3dcconnect.setPowerLimitsUsed(value);
        }

        if (E3DCBindingConstants.CHANNEL_MaxCharge.equals(channelUID.getId())) {
            int value = convertCommandToIntValue(command, 100, 3000);
            e3dcconnect.setMaxChargePower(value);
        }

        if (E3DCBindingConstants.CHANNEL_MaxDischarge.equals(channelUID.getId())) {
            int value = convertCommandToIntValue(command, 100, 3000);
            e3dcconnect.setMaxDischargePower(value);
        }

        if (E3DCBindingConstants.CHANNEL_DischargeStart.equals(channelUID.getId())) {
            int value = convertCommandToIntValue(command, 0, 500);
            e3dcconnect.setDischargeStartPower(value);
        }
    }

    public int convertCommandToIntValue(Command command, int min, int max) {
        this.logger.debug("convertCommandToIntValue  command: {}", command);

        int value;
        double fValue;

        if (command instanceof DecimalType) {
            fValue = ((DecimalType) command).floatValue();
            logger.trace("convertCommandToIntValue DecimalType fValue: {}", Double.valueOf(fValue));
        } else if (command instanceof QuantityType) {
            fValue = ((QuantityType<?>) command).doubleValue();
            logger.trace("convertCommandToIntValue QuantityType fValue: {}", Double.valueOf(fValue));
        } else {
            throw new NumberFormatException("Command type '" + command + "' not supported");
        }

        value = (int) fValue;
        value = Math.min(max, value);
        value = Math.max(min, value);

        return value;
    }

    @Override
    public void initialize() {
        config = getConfigAs(E3DCConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            e3dcconnect = new E3DCConnector(this, config);
            updateStatus(ThingStatus.ONLINE);
            scheduleReadDataJob();
        });
    }

    private void scheduleReadDataJob() {
        int readDataInterval = config.getUpdateinterval();
        // Ensure that the request is finished
        if (readDataInterval < 5) {
            readDataInterval = 5;
        }

        logger.debug("Data table request interval {} seconds", readDataInterval);

        readDataJob = scheduler.scheduleWithFixedDelay(() -> {
            e3dcconnect.requestE3DCData();
        }, 0, readDataInterval, TimeUnit.SECONDS);
    }

    private void cancelReadDataJob() {
        if (readDataJob != null) {
            if (!readDataJob.isDone()) {
                readDataJob.cancel(true);
                logger.debug("Scheduled data table requests cancelled");
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing thing {}", getThing().getUID());
        cancelReadDataJob();
        e3dcconnect.close();
        logger.debug("Thing {} disposed", getThing().getUID());
    }

    @Override
    protected void updateState(String strChannelName, State dt) {
        super.updateState(strChannelName, dt);
    }

    @Override
    protected void updateStatus(ThingStatus ts, ThingStatusDetail statusDetail, @Nullable String reason) {
        super.updateStatus(ts, statusDetail, reason);
    }

    @Override
    protected void updateStatus(ThingStatus ts, ThingStatusDetail statusDetail) {
        super.updateStatus(ts, statusDetail);
    }

    @Override
    protected void updateStatus(ThingStatus ts) {
        super.updateStatus(ts);
    }
}
