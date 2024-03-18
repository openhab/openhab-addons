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
package org.openhab.binding.solax.internal.handlers;

import java.io.IOException;
import java.time.ZoneId;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solax.internal.SolaxBindingConstants;
import org.openhab.binding.solax.internal.SolaxConfiguration;
import org.openhab.binding.solax.internal.connectivity.SolaxConnector;
import org.openhab.binding.solax.internal.exceptions.SolaxUpdateException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolaxCloudHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractSolaxHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AbstractSolaxHandler.class);

    private static final int INITIAL_SCHEDULE_DELAY_SECONDS = 0;

    private @NonNullByDefault({}) SolaxConnector connector;

    private @Nullable ScheduledFuture<?> schedule;

    private final ReentrantLock retrieveDataCallLock = new ReentrantLock();

    protected final TranslationProvider i18nProvider;

    protected final ZoneId timeZone;

    public AbstractSolaxHandler(Thing thing, TranslationProvider i18nProvider, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.i18nProvider = i18nProvider;
        this.timeZone = timeZoneProvider.getTimeZone();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        SolaxConfiguration config = getConfigAs(SolaxConfiguration.class);
        connector = createConnector(config);
        int refreshInterval = config.refreshInterval;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        logger.debug("Scheduling regular interval retrieval every {} {}", refreshInterval, timeUnit);
        schedule = scheduler.scheduleWithFixedDelay(this::retrieveData, INITIAL_SCHEDULE_DELAY_SECONDS, refreshInterval,
                timeUnit);
    }

    private void retrieveData() {
        if (retrieveDataCallLock.tryLock()) {
            try {
                String rawJsonData = connector.retrieveData();
                logger.debug("Raw data retrieved = {}", rawJsonData);

                if (rawJsonData != null && !rawJsonData.isEmpty()) {
                    updateFromData(rawJsonData);
                    if (getThing().getStatus() != ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            SolaxBindingConstants.I18N_KEY_OFFLINE_COMMUNICATION_ERROR_JSON_CANNOT_BE_RETRIEVED);
                }
            } catch (IOException e) {
                logger.debug("Exception received while attempting to retrieve data via HTTP", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (SolaxUpdateException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } finally {
                retrieveDataCallLock.unlock();
            }
        } else {
            logger.debug("Unable to retrieve data because a request is already in progress.");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::retrieveData);
        } else {
            logger.debug("Binding {} only supports refresh command", SolaxBindingConstants.BINDING_ID);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        cancelSchedule();
    }

    protected void cancelSchedule() {
        ScheduledFuture<?> schedule = this.schedule;
        if (schedule != null) {
            logger.debug("Cancelling schedule {}", schedule);
            schedule.cancel(true);
            this.schedule = null;
        }
    }

    protected abstract SolaxConnector createConnector(SolaxConfiguration config);

    protected abstract void updateFromData(String rawJsonData) throws SolaxUpdateException;
}
