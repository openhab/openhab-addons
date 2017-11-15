/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.enocean.handler;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polito.elite.enocean.enj.communication.EnJConnection;
import it.polito.elite.enocean.enj.communication.EnJDeviceListener;
import it.polito.elite.enocean.enj.eep.EEPAttributeChangeListener;
import it.polito.elite.enocean.enj.eep.eep26.attributes.EEP26RockerSwitch2RockerAction;
import it.polito.elite.enocean.enj.eep.eep26.attributes.EEP26RockerSwitch2RockerButtonCount;
import it.polito.elite.enocean.enj.link.EnJLink;
import it.polito.elite.enocean.enj.model.EnOceanDevice;

/**
 * The {@link EnOceanBridgeHandler} is the bridge representation of the serial communication device used to communicate
 * with the EnOcean devices. It manages the interaction with the EnOcean serial library.
 *
 * @author Jan Kemmler - Initial contribution
 */
// @NonNullByDefault
public class EnOceanBridgeHandler extends BaseBridgeHandler implements EnJDeviceListener {
    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(EnOceanRockerSwitchHandler.class);

    @Nullable
    private EnJLink linkLayer = null;
    @Nullable
    private EnJConnection connection;

    public EnOceanBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {

        Configuration config = this.getConfig();
        String serialPort = (String) config.get("serialPort");
        try {
            if (null != serialPort) {
                // create the lowest link layer
                linkLayer = new EnJLink(serialPort);

                // create the connection layer
                connection = new EnJConnection(linkLayer, null, this);
                if (null != linkLayer) {
                    // connect the link
                    linkLayer.connect();
                }
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            this.logger.warn("The given port does not exist or no device is plugged in. {}", e);
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (null != connection) {
            String enoceanAddress = (String) childThing.getConfiguration().get("enoceanAddress");

            connection.addNewDevice(enoceanAddress, "F6-02-02");
            EnOceanDevice device = connection
                    .getDevice(EnOceanDevice.byteAddressToUID(EnOceanDevice.parseAddress(enoceanAddress)));
            if (null != device) {
                device.getEEP().addEEP26AttributeListener(0, EEP26RockerSwitch2RockerAction.NAME,
                        (EEPAttributeChangeListener) childHandler);
                device.getEEP().addEEP26AttributeListener(0, EEP26RockerSwitch2RockerButtonCount.NAME,
                        (EEPAttributeChangeListener) childHandler);

                // childHandler.updateStatus();
            } else {
                logger.warn("Unable to register device. {}", enoceanAddress);
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (null != connection) {
            String enoceanAddress = (String) childThing.getConfiguration().get("enoceanAddress");

            EnOceanDevice device = connection
                    .getDevice(EnOceanDevice.byteAddressToUID(EnOceanDevice.parseAddress(enoceanAddress)));
            if (null != device) {
                device.getEEP().removeEEP26AttributeListener(0, EEP26RockerSwitch2RockerAction.NAME,
                        (EEPAttributeChangeListener) childHandler);
                device.getEEP().removeEEP26AttributeListener(0, EEP26RockerSwitch2RockerButtonCount.NAME,
                        (EEPAttributeChangeListener) childHandler);
            } else {
                logger.warn("Unable to unregister device. {}", enoceanAddress);
            }
        }
    }

    @Override
    public void addedEnOceanDevice(@Nullable EnOceanDevice addedDevice) {
        return;

    }

    @Override
    public void modifiedEnOceanDevice(@Nullable EnOceanDevice changedDevice) {

    }

    @Override
    public void removedEnOceanDevice(@Nullable EnOceanDevice changedDevice) {

    }

}
