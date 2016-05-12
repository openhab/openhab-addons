/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.VIRTUAL_DATAPOINT_NAME_BATTERY_TYPE;

import java.io.InputStream;
import java.util.Properties;

import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A virtual String datapoint which adds the battery type to a battery powered device.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class BatteryTypeVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    private static final Logger logger = LoggerFactory.getLogger(BatteryTypeVirtualDatapointHandler.class);

    private static final Properties batteries = new Properties();

    public BatteryTypeVirtualDatapointHandler() {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("homematic/batteries.properties");
            batteries.load(is);
        } catch (Exception e) {
            logger.warn("Battery property file not found, battery type not available");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(HmDevice device) {
        String batteryType = batteries.getProperty(device.getType());
        if (batteryType != null) {
            addDatapoint(device, 0, VIRTUAL_DATAPOINT_NAME_BATTERY_TYPE, HmValueType.STRING, batteryType, true);
        }
    }

}
