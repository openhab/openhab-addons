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
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
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

    /**
     * Called from {@link AVMFritzBaseBridgeHandler)} to update the thing status because updateStatus is protected.
     *
     * @param status       Thing status
     * @param statusDetail Thing status detail
     * @param description  Thing status description
     */
    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        updateStatus(status, statusDetail, description);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        logger.debug("Handle command '{}' for channel {}", command, channelId);
        FritzAhaWebInterface fritzBox = getWebInterface();
        if (fritzBox == null) {
            logger.debug("Cannot handle command '{}' because connection is missing", command);
            return;
        }
        String ain = getIdentifier();
        if (ain == null) {
            logger.debug("Cannot handle command '{}' because AIN is missing", command);
            return;
        }
        switch (channelId) {
            case CHANNEL_MODE:
            case CHANNEL_LOCKED:
            case CHANNEL_DEVICE_LOCKED:
            case CHANNEL_TEMPERATURE:
            case CHANNEL_ENERGY:
            case CHANNEL_POWER:
            case CHANNEL_VOLTAGE:
            case CHANNEL_ACTUALTEMP:
            case CHANNEL_ECOTEMP:
            case CHANNEL_COMFORTTEMP:
            case CHANNEL_NEXTCHANGE:
            case CHANNEL_NEXTTEMP:
            case CHANNEL_BATTERY:
            case CHANNEL_BATTERY_LOW:
                logger.debug("Channel {} is a read-only channel and cannot handle command '{}'", channelId, command);
                break;
            case CHANNEL_OUTLET:
                if (command instanceof OnOffType) {
                    state.getSwitch().setState(OnOffType.ON.equals(command) ? SwitchModel.ON : SwitchModel.OFF);
                    fritzBox.setSwitch(ain, OnOffType.ON.equals(command));
                } else {
                    logger.warn("Received unknown command '{}' for channel {}", command, CHANNEL_OUTLET);
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
                    switch (command.toString()) {
                        case MODE_ON:
                            state.getHkr().setTsoll(HeatingModel.TEMP_FRITZ_ON);
                            fritzBox.setSetTemp(ain, HeatingModel.TEMP_FRITZ_ON);
                            updateState(CHANNEL_SETTEMP,
                                    new QuantityType<>(HeatingModel.toCelsius(HeatingModel.TEMP_FRITZ_ON), CELSIUS));
                            break;
                        case MODE_OFF:
                            state.getHkr().setTsoll(HeatingModel.TEMP_FRITZ_OFF);
                            fritzBox.setSetTemp(ain, HeatingModel.TEMP_FRITZ_OFF);
                            updateState(CHANNEL_SETTEMP,
                                    new QuantityType<>(HeatingModel.toCelsius(HeatingModel.TEMP_FRITZ_OFF), CELSIUS));
                            break;
                        case MODE_COMFORT:
                            BigDecimal comfortTemperature = state.getHkr().getKomfort();
                            state.getHkr().setTsoll(comfortTemperature);
                            fritzBox.setSetTemp(ain, comfortTemperature);
                            updateState(CHANNEL_SETTEMP,
                                    new QuantityType<>(HeatingModel.toCelsius(comfortTemperature), CELSIUS));
                            break;
                        case MODE_ECO:
                            BigDecimal ecoTemperature = state.getHkr().getAbsenk();
                            state.getHkr().setTsoll(ecoTemperature);
                            fritzBox.setSetTemp(ain, ecoTemperature);
                            updateState(CHANNEL_SETTEMP,
                                    new QuantityType<>(HeatingModel.toCelsius(ecoTemperature), CELSIUS));
                            break;
                        case MODE_BOOST:
                            state.getHkr().setTsoll(HeatingModel.TEMP_FRITZ_MAX);
                            fritzBox.setSetTemp(ain, HeatingModel.TEMP_FRITZ_MAX);
                            updateState(CHANNEL_SETTEMP,
                                    new QuantityType<>(HeatingModel.toCelsius(HeatingModel.TEMP_FRITZ_MAX), CELSIUS));
                            break;
                        case MODE_UNKNOWN:
                        case MODE_WINDOW_OPEN:
                            logger.debug("Command '{}' is a read-only command for channel {}.", command, channelId);
                            break;
                        default:
                            logger.warn("Received unknown command '{}' for channel {}", command, CHANNEL_RADIATOR_MODE);
                            break;
                    }
                }
                break;
            default:
                logger.debug("Received unknown channel {}", channelId);
                break;
        }
    }

    /**
     * Creates new channels for the thing.
     *
     * @param channelId ID of the channel to be created.
     */
    public void createChannel(String channelId) {
        ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
            ChannelTypeUID channelTypeUID = CHANNEL_BATTERY.equals(channelId)
                    ? new ChannelTypeUID("system:battery-level")
                    : new ChannelTypeUID(BINDING_ID, channelId);
            Channel channel = callback.createChannelBuilder(channelUID, channelTypeUID).build();
            updateThing(editThing().withoutChannel(channelUID).withChannel(channel).build());
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
    public String getIdentifier() {
        Object ain = getThing().getConfiguration().get(CONFIG_AIN);
        return ain != null ? ain.toString() : null;
    }

    @Nullable
    public AVMFritzBaseModel getState() {
        return state;
    }

    public void setState(AVMFritzBaseModel state) {
        this.state = state;
    }
}
