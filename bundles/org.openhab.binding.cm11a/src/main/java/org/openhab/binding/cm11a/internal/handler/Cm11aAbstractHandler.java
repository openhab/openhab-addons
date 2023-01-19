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
package org.openhab.binding.cm11a.internal.handler;

import java.io.IOException;

import org.openhab.binding.cm11a.internal.InvalidAddressException;
import org.openhab.binding.cm11a.internal.X10Interface;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an abstract base class for the "Thing" handlers (i.e. Cm11aApplianceHandler and Cm11aLampHandler).
 * It is not used by the Bridge handler (Cm11aHandler)
 *
 * @author Bob Raker - Initial contribution
 *
 */
public abstract class Cm11aAbstractHandler extends BaseThingHandler {

    /**
     * The House and Unit codes set on the module, i.e. A1, J14
     */
    protected String houseUnitCode;

    /**
     * The X10 function
     */
    protected int x10Function;

    /**
     * The channel ID
     */
    protected ChannelUID channelUID;

    /**
     * The current State of the device
     */
    protected State currentState;

    /**
     * Number of CM11a dim increments for dimable devices
     */
    static final int X10_DIM_INCREMENTS = 22;
    static final String HOUSE_UNIT_CODE = "houseUnitCode";
    static final String NO_BRIDGE_ERROR = "No bridge found using house unit code ";

    private final Logger logger = LoggerFactory.getLogger(Cm11aAbstractHandler.class);

    /**
     * The construction
     *
     * @param thing The "Thing" to be handled
     */
    public Cm11aAbstractHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Configuration config = thing.getConfiguration();

        houseUnitCode = (String) config.get(HOUSE_UNIT_CODE);
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.warn("{}", NO_BRIDGE_ERROR + houseUnitCode);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, NO_BRIDGE_ERROR + houseUnitCode);
            return;
        }

        if (ThingStatus.ONLINE.equals(bridge.getStatus())) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void dispose() {
        houseUnitCode = null;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("CM11A status changed to {}.", bridgeStatusInfo.getStatus());

        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            logger.debug("CM11A is not online. Bridge status: {}", bridgeStatusInfo.getStatus());
            return;
        }

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            if (houseUnitCode.length() > 0) {
                // The config must be present and was set during initialization
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            }
        }
    }

    /**
     * Will be called by the X10Interface when it is ready for this X10 device to use the X10 bus.
     * Child classes should override this method with the specific process necessary to update the
     * hardware with the latest data.
     *
     * <p>
     * Warning: This will be called in a different thread. It must be thread safe.
     * </p>
     *
     * <p>
     * Retries in the event of interface problems will be handled by the X10Interface. If a comms
     * problem occurs and the method throws an exception, this device will be rescheduled again later.
     * </p>
     */
    public abstract void updateHardware(X10Interface x10Interface) throws IOException, InvalidAddressException;

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    /**
     * Subtract the specified number of X10 "dims" from the current state
     *
     * @param dims The number of dims to remove
     * @return The updated current state
     */
    public State addDimsToCurrentState(int dims) {
        if (!(currentState instanceof PercentType)) {
            if (currentState instanceof OnOffType && currentState == OnOffType.ON) {
                currentState = PercentType.HUNDRED;
            } else {
                currentState = PercentType.ZERO;
            }
        }
        // The current state is stored in a PercentType object and therefore needs to be converted to an incremental
        // percent
        int curPercent = ((PercentType) currentState).intValue();
        // dims is a number between 0 and 22 which represents the full range of cm11a
        int dimsPercent = (dims * 100) / X10_DIM_INCREMENTS;
        int newPercent = curPercent - dimsPercent;
        newPercent = Math.max(newPercent, 0);
        newPercent = Math.min(newPercent, 100);
        currentState = new PercentType(newPercent);
        return currentState;
    }

    /**
     * Add the specified number of X10 "dims" from the current state
     *
     * @param dims The number of dims to remove
     * @return The updated current state
     */
    public State addBrightsToCurrentState(int dims) {
        return addDimsToCurrentState(-dims);
    }
}
