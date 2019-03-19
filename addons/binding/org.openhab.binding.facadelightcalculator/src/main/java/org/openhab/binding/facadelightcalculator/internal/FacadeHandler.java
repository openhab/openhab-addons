/**
 * Copyright (c) 2014,2019 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.facadelightcalculator.internal;

import static org.openhab.binding.facadelightcalculator.internal.FacadeLightCalculatorBindingConstants.*;

import javax.measure.quantity.Angle;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.facadelightcalculator.internal.config.FacadeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FacadeHandler} is responsible for updating calculated facade
 * illumination data.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FacadeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FacadeHandler.class);

    private static final State RIGHT = new StringType("RIGHT");
    private static final State LEFT = new StringType("LEFT");
    private static final State OUT_RIGHT = new StringType("OUT_RIGHT");
    private static final State OUT_LEFT = new StringType("OUT_LEFT");
    private static final State FRONT = new StringType("FRONT");

    private State previousInWindow = UnDefType.UNDEF;
    private State previousRelative = UnDefType.UNDEF;

    int shift;
    int shiftedOrientation;
    int shiftedLeft;
    int shiftedRight;
    int frontMargin;

    public FacadeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing thing {}", getThing().getUID());
        FacadeConfiguration config = getConfigAs(FacadeConfiguration.class);
        shift = getShift(config.getOrientation(), config.getNegativeOffset(), config.getPositiveOffset());
        shiftedOrientation = config.getOrientation() + shift;
        shiftedLeft = shiftedOrientation - config.getNegativeOffset();
        shiftedRight = shiftedOrientation + config.getPositiveOffset();
        frontMargin = config.getMargin();
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (SUN_AZIMUTH.equals(channelUID.getId()) && command instanceof QuantityType) {
            logger.debug("Sun azimuth changed, updating facade illumination");
            @SuppressWarnings("unchecked")
            float sunAzimut = ((QuantityType<Angle>) command).floatValue() + shift;
            float bearing = getBearing(sunAzimut);

            State sunInFacade = bearing != 0 ? OnOffType.ON : OnOffType.OFF;
            State relativePosition = getRelativePosition(sunAzimut);

            getThing().getChannels().forEach(channel -> {
                switch (channel.getUID().getId()) {
                    case FACADE_FACING:
                        updateState(channel.getUID(), sunInFacade);
                        break;
                    case FACADE_BEARING:
                        updateState(channel.getUID(), new QuantityType<>(bearing, SmartHomeUnits.PERCENT));
                        break;
                    case FACADE_SIDE:
                        updateState(channel.getUID(), relativePosition);
                        break;
                    case EVENT_FACADE:
                        String event = (previousInWindow == OnOffType.ON && sunInFacade == OnOffType.OFF)
                                ? EVENT_LEAVE_FACADE
                                : (previousInWindow == OnOffType.OFF
                                        && sunInFacade == OnOffType.ON)
                                                ? EVENT_ENTER_FACADE
                                                : (relativePosition == FRONT && previousRelative != FRONT
                                                        && previousRelative != UnDefType.UNDEF) ? EVENT_FRONT_FACADE
                                                                : null;
                        if (event != null) {
                            triggerChannel(channel.getUID(), event);
                        }
                }
            });
            previousInWindow = sunInFacade;
            previousRelative = relativePosition;
        }
    }

    private float getBearing(float sunAzimut) {
        return (sunAzimut >= shiftedOrientation && sunAzimut <= shiftedRight)
                ? 100 * (shiftedRight - sunAzimut) / (shiftedRight - shiftedOrientation)
                : (sunAzimut < shiftedOrientation && sunAzimut >= shiftedLeft)
                        ? 100 * (sunAzimut - shiftedLeft) / (shiftedOrientation - shiftedLeft)
                        : 0;
    }

    private int getShift(int orientation, int nOffset, int pOffset) {
        int shift = (orientation - nOffset) < 0 ? (orientation - nOffset) * -1
                : (orientation + pOffset) > 360 ? 360 - (orientation + pOffset) : 0;
        return shift;
    }

    private State getRelativePosition(float sunAzimut) {
        return sunAzimut < shiftedLeft ? OUT_LEFT
                : sunAzimut < (shiftedOrientation - frontMargin) ? LEFT
                        : sunAzimut > shiftedRight ? OUT_RIGHT
                                : sunAzimut > (shiftedOrientation + frontMargin) ? RIGHT : FRONT;
    }
}
