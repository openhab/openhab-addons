/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.handler;

import static org.eclipse.smarthome.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.binding.avmfritz.BindingConstants.*;

import java.math.BigDecimal;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.avmfritz.internal.ahamodel.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.ahamodel.HeatingModel;
import org.openhab.binding.avmfritz.internal.ahamodel.SwitchModel;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract handler for a FRITZ! thing. Handles commands, which are sent to one of the channels.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet
 *         DECT
 * @author Christoph Weitkamp - Added support for groups
 */
@NonNullByDefault
public abstract class AVMFritzBaseThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzBaseThingHandler.class);

    /**
     * keeps track of the current state for handling of increase/decrease
     */
    @Nullable
    private AVMFritzBaseModel state;

    /**
     * Constructor
     *
     * @param thing Thing object representing a FRITZ! device
     */
    public AVMFritzBaseThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for {}", getClass().getName());
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        logger.debug("Handle command '{}' for channel {}", command, channelId);
        FritzAhaWebInterface fritzBox = getWebInterface();
        if (fritzBox == null) {
            return;
        }
        if (getThing().getConfiguration().get(THING_AIN) == null) {
            logger.debug("Cannot handle command '{}' because AIN is missing", command);
            return;
        }
        String ain = getThing().getConfiguration().get(THING_AIN).toString();
        switch (channelId) {
            case CHANNEL_MODE:
            case CHANNEL_LOCKED:
            case CHANNEL_DEVICE_LOCKED:
            case CHANNEL_TEMP:
            case CHANNEL_ENERGY:
            case CHANNEL_POWER:
            case CHANNEL_ACTUALTEMP:
            case CHANNEL_ECOTEMP:
            case CHANNEL_COMFORTTEMP:
            case CHANNEL_NEXTCHANGE:
            case CHANNEL_NEXTTEMP:
            case CHANNEL_BATTERY:
                logger.debug("Channel {} is a read-only channel and cannot handle command '{}'", channelId, command);
                break;
            case CHANNEL_SWITCH:
                if (command instanceof OnOffType) {
                    state.getSwitch().setState(OnOffType.ON.equals(command) ? SwitchModel.ON : SwitchModel.OFF);
                    fritzBox.setSwitch(ain, OnOffType.ON.equals(command));
                } else {
                    logger.warn("Received unknown command '{}' for channel {}", command, CHANNEL_SWITCH);
                }
                break;
            case CHANNEL_SETTEMP:
                if (command instanceof DecimalType) {
                    BigDecimal temperature = HeatingModel.normalizeCelsius(((DecimalType) command).toBigDecimal());
                    state.getHkr().setTsoll(temperature);
                    fritzBox.setSetTemp(ain, HeatingModel.fromCelsius(temperature));
                    updateState(CHANNEL_RADIATOR_MODE, new StringType(state.getHkr().getRadiatorMode()));
                } else if (command instanceof QuantityType) {
                    BigDecimal temperature = HeatingModel
                            .normalizeCelsius(((QuantityType<Temperature>) command).toUnit(CELSIUS).toBigDecimal());
                    state.getHkr().setTsoll(temperature);
                    fritzBox.setSetTemp(ain, HeatingModel.fromCelsius(temperature));
                    updateState(CHANNEL_RADIATOR_MODE, new StringType(state.getHkr().getRadiatorMode()));
                } else if (command instanceof IncreaseDecreaseType) {
                    BigDecimal temperature = state.getHkr().getTsoll();
                    if (IncreaseDecreaseType.INCREASE.equals(command)) {
                        temperature.add(BigDecimal.ONE);
                    } else {
                        temperature.subtract(BigDecimal.ONE);
                    }
                    state.getHkr().setTsoll(temperature);
                    fritzBox.setSetTemp(ain, temperature);
                    updateState(CHANNEL_RADIATOR_MODE, new StringType(state.getHkr().getRadiatorMode()));
                } else if (command instanceof OnOffType) {
                    BigDecimal temperature = OnOffType.ON.equals(command) ? HeatingModel.TEMP_FRITZ_ON
                            : HeatingModel.TEMP_FRITZ_OFF;
                    state.getHkr().setTsoll(temperature);
                    fritzBox.setSetTemp(ain, temperature);
                    updateState(CHANNEL_RADIATOR_MODE, new StringType(state.getHkr().getRadiatorMode()));
                } else {
                    logger.warn("Received unknown command '{}' for channel {}", command, CHANNEL_SETTEMP);
                }
                break;
            case CHANNEL_RADIATOR_MODE:
                if (command instanceof StringType) {
                    String commandString = command.toString();
                    if (MODE_ON.equals(commandString)) {
                        state.getHkr().setTsoll(HeatingModel.TEMP_FRITZ_ON);
                        fritzBox.setSetTemp(ain, HeatingModel.TEMP_FRITZ_ON);
                        updateState(CHANNEL_SETTEMP,
                                new DecimalType(HeatingModel.toCelsius(HeatingModel.TEMP_FRITZ_ON)));
                    } else if (MODE_OFF.equals(commandString)) {
                        state.getHkr().setTsoll(HeatingModel.TEMP_FRITZ_OFF);
                        fritzBox.setSetTemp(ain, HeatingModel.TEMP_FRITZ_OFF);
                        updateState(CHANNEL_SETTEMP,
                                new DecimalType(HeatingModel.toCelsius(HeatingModel.TEMP_FRITZ_OFF)));
                    } else if (MODE_COMFORT.equals(commandString)) {
                        BigDecimal comfortTemperature = state.getHkr().getKomfort();
                        state.getHkr().setTsoll(comfortTemperature);
                        fritzBox.setSetTemp(ain, comfortTemperature);
                        updateState(CHANNEL_SETTEMP, new DecimalType(HeatingModel.toCelsius(comfortTemperature)));
                    } else if (MODE_ECO.equals(commandString)) {
                        BigDecimal ecoTemperature = state.getHkr().getAbsenk();
                        state.getHkr().setTsoll(ecoTemperature);
                        fritzBox.setSetTemp(ain, ecoTemperature);
                        updateState(CHANNEL_SETTEMP, new DecimalType(HeatingModel.toCelsius(ecoTemperature)));
                    } else if (MODE_BOOST.equals(commandString)) {
                        state.getHkr().setTsoll(HeatingModel.TEMP_FRITZ_MAX);
                        fritzBox.setSetTemp(ain, HeatingModel.TEMP_FRITZ_MAX);
                        updateState(CHANNEL_SETTEMP,
                                new DecimalType(HeatingModel.toCelsius(HeatingModel.TEMP_FRITZ_MAX)));
                    } else {
                        logger.warn("Received unknown command '{}' for channel {}", command, CHANNEL_RADIATOR_MODE);
                    }
                }
                break;
            default:
                logger.debug("Received unknown channel {}", channelId);
                break;
        }
    }

    /**
     * Provides the web interface object.
     *
     * @return The web interface object
     */
    @Nullable
    protected FritzAhaWebInterface getWebInterface() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null && handler instanceof BoxHandler) {
                return ((BoxHandler) handler).getWebInterface();
            }
        }
        return null;
    }

    @Nullable
    public AVMFritzBaseModel getState() {
        return state;
    }

    public void setState(AVMFritzBaseModel state) {
        this.state = state;
    }
}
