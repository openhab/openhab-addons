/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.handler;

import static org.openhab.binding.verisure.VerisureBindingConstants.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.verisure.internal.DeviceStatusListener;
import org.openhab.binding.verisure.internal.VerisureBridgeConfiguration;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VerisureBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author l3rum - Initial contribution
 */
public class VerisureBridgeHandler extends BaseBridgeHandler {

    private static final int REFRESH_DELAY = 10;

    @Override
    protected void updateThing(Thing thing) {
        super.updateThing(thing);
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        stopAutomaticRefresh();
        super.updateConfiguration(configuration);
        initialize();
    }

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private Logger logger = LoggerFactory.getLogger(VerisureBridgeHandler.class);

    private BigDecimal refresh = new BigDecimal(600);
    private String authstring;
    private String pinCode;
    ScheduledFuture<?> refreshJob;
    ScheduledFuture<?> immediateRefreshJob;
    ReentrantLock immediateRefreshJobLock = new ReentrantLock();

    private VerisureSession session = null;

    Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            logger.debug("VerisureBridgeHandler - Thread is up and running!");
            try {
                boolean success = session.refresh();
                if (success) {
                    updateStatus(ThingStatus.ONLINE);
                    updateAlarmState();
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }
    };

    public VerisureBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("Supposed to handle a command {} ", command);
        if (command instanceof RefreshType) {
            scheduleImmediateRefresh();
            updateAlarmState();
        } else if (channelUID.getId().equals(CHANNEL_SETSTATUS)) {
            handleChangeAlarmState(command);
        } else {
            logger.warn("unknown command! {}", command);
        }
        scheduleImmediateRefresh();

    }

    private void handleChangeAlarmState(Command command) {
        if (command.toString().equals("0")) {
            logger.debug("attempting to turn off alarm!");
            session.disarmAlarm();
            ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS);
            updateState(cuid, new StringType("pending"));
        } else if (command.toString().equals("1")) {
            logger.debug("arming at home");
            session.armHomeAlarm();
            ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS);
            updateState(cuid, new StringType("pending"));

        } else if (command.toString().equals("2")) {
            logger.debug("arming away!");
            session.armAwayAlarm();
            ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS);
            updateState(cuid, new StringType("pending"));
        } else {
            logger.debug("unknown command!");
        }
    }

    void scheduleImmediateRefresh() {
        this.immediateRefreshJobLock.lock();
        try {
            // We schedule in 10 sec, to avoid multiple updates
            if (refreshJob != null) {
                logger.debug("Current remaining delay {}", refreshJob.getDelay(TimeUnit.SECONDS));
                if (refreshJob.getDelay(TimeUnit.SECONDS) > REFRESH_DELAY) {
                    if (immediateRefreshJob == null || immediateRefreshJob.getDelay(TimeUnit.SECONDS) <= 0) {
                        if (immediateRefreshJob != null) {
                            logger.debug("Current immediateRefreshJob delay {}",
                                    immediateRefreshJob.getDelay(TimeUnit.SECONDS));
                        }
                        // Note we are using getDelay() instead of isDone() as we want to allow Things to schedule a
                        // refresh if their status is pending. As the status update happens inside the pollingRunnable
                        // execution the isDone() will return false and would not allow the rescheduling of the task.
                        immediateRefreshJob = scheduler.schedule(pollingRunnable, REFRESH_DELAY, TimeUnit.SECONDS);
                    }
                }
            }
        } finally {
            this.immediateRefreshJobLock.unlock();
        }
    }

    public VerisureSession getSession() {
        return session;
    }

    @Override
    public void initialize() {

        logger.debug("Initializing Verisure Binding");
        VerisureBridgeConfiguration config = getConfigAs(VerisureBridgeConfiguration.class);

        this.refresh = config.refresh;
        this.pinCode = config.pin;

        if (config.username == null || config.password == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Must configure username and password");
        } else {
            session = new VerisureSession();
            authstring = "j_username=" + config.username + "&j_password=" + config.password;
            try {
                session.initialize(authstring, pinCode);
                startAutomaticRefresh();
            } catch (Error e) {
                logger.error("Failed", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (Exception e) {
                logger.error("Failed", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }

    }

    private void stopAutomaticRefresh() {
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            logger.debug("Scheduling at fixed rate");
            refreshJob = scheduler.scheduleAtFixedRate(pollingRunnable, REFRESH_DELAY, refresh.intValue(),
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        stopAutomaticRefresh();
        if (session != null) {
            session.dispose();
            session = null;
        }
    }

    public boolean registerObjectStatusListener(DeviceStatusListener deviceStatusListener) {
        return session.registerDeviceStatusListener(deviceStatusListener);
    }

    /**
     *
     */
    private void updateAlarmState() {
        try {
            ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS);
            updateState(cuid, session.getAlarmStatus());

            cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS_NUMERIC);
            updateState(cuid, session.getAlarmStatusNumeric());

            cuid = new ChannelUID(getThing().getUID(), CHANNEL_CHANGERNAME);
            updateState(cuid, session.getAlarmChangerName());

            cuid = new ChannelUID(getThing().getUID(), CHANNEL_TIMESTAMP);
            updateState(cuid, session.getAlarmTimestamp());

            cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS_LOCALIZED);
            updateState(cuid, session.getAlarmObject() == null ? UnDefType.UNDEF
                    : new StringType(session.getAlarmObject().getLabel()));
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#handleRemoval()
     */
    @Override
    public void handleRemoval() {
        dispose();
        super.handleRemoval();
    }

}
