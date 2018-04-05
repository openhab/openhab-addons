/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.handler;

import static org.openhab.binding.avmfritz.BindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.avmfritz.BindingConstants;
import org.openhab.binding.avmfritz.internal.ahamodel.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.ahamodel.SwitchModel;
import org.openhab.binding.avmfritz.internal.config.AVMFritzConfiguration;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a FRITZ!Powerline 546E device. Handles polling of values from AHA devices and commands, which are sent to
 * one of the channels.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for groups
 */
@NonNullByDefault
public class Powerline546EHandler extends AVMFritzBaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(Powerline546EHandler.class);

    /**
     * keeps track of the current state for handling of increase/decrease
     */
    @Nullable
    private AVMFritzBaseModel state;

    /**
     * Constructor
     *
     * @param bridge Bridge object representing a FRITZ!Powerline 546E
     */
    public Powerline546EHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge, httpClient);
    }

    @Override
    public void addDeviceList(AVMFritzBaseModel device) {
        logger.debug("set device model: {}", device);
        ThingUID thingUID = getThingUID(device);
        if (getThing().getUID().equals(thingUID)) {
            logger.debug("update self {} with device model: {}", thingUID, device);
            setState(device);
            updateThingFromDevice(getThing(), device);
        } else {
            super.addDeviceList(device);
        }
    }

    /**
     * Updates things from device model.
     *
     * @param thing Thing to be updated.
     * @param device Device model with new data.
     */
    @Override
    protected void updateThingFromDevice(Thing thing, AVMFritzBaseModel device) {
        // save AIN to config for FRITZ!Powerline 546E stand-alone
        if (thing.getConfiguration().get(THING_AIN) == null) {
            thing.getConfiguration().put(THING_AIN, device.getIdentifier());
        }
        super.updateThingFromDevice(thing, device);
    }

    /**
     * Builds a {@link ThingUID} from a device model. The UID is build from the
     * {@link BindingConstants#BINDING_ID} and value of
     * {@link AVMFritzBaseModel#getProductName()} in which all characters NOT matching
     * the regex [^a-zA-Z0-9_] are replaced by "_".
     *
     * @param device Discovered device model
     * @return ThingUID without illegal characters.
     */
    @Override
    @Nullable
    public ThingUID getThingUID(AVMFritzBaseModel device) {
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, getThingTypeId(device).concat("_Solo"));
        String ipAddress = getConfigAs(AVMFritzConfiguration.class).getIpAddress();

        if (PL546E_STANDALONE_THING_TYPE.equals(thingTypeUID)) {
            String thingName = "fritz.powerline".equals(ipAddress) ? ipAddress : ipAddress.replaceAll(INVALID_PATTERN, "_");
            return new ThingUID(thingTypeUID, thingName);
        } else {
            return super.getThingUID(device);
        }
    }

    /**
     * Handle the commands for switchable outlets.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        logger.debug("Handle command '{}' for channel {}", command, channelId);
        FritzAhaWebInterface fritzBox = getWebInterface();
        if (fritzBox == null) {
            logger.debug("Cannot handle command '{}' because connection is missing", command);
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
            case CHANNEL_ENERGY:
            case CHANNEL_POWER:
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
            default:
                super.handleCommand(channelUID, command);
                break;
        }
    }

    @Nullable
    public AVMFritzBaseModel getState() {
        return state;
    }

    public void setState(AVMFritzBaseModel state) {
        this.state = state;
    }
}
