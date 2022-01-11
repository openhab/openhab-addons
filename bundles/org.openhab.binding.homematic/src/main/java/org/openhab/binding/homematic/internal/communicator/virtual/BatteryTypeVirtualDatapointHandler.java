/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.VIRTUAL_DATAPOINT_NAME_BATTERY_TYPE;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmValueType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A virtual String datapoint which adds the battery type to a battery powered device.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class BatteryTypeVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    private final Logger logger = LoggerFactory.getLogger(BatteryTypeVirtualDatapointHandler.class);

    private static final Properties batteries = new Properties();

    public BatteryTypeVirtualDatapointHandler() {
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        try (InputStream stream = bundle.getResource("homematic/batteries.properties").openStream()) {
            batteries.load(stream);
        } catch (IllegalStateException | IOException e) {
            logger.warn("The resource homematic/batteries.properties could not be loaded! Battery types not available",
                    e);
        }
    }

    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_BATTERY_TYPE;
    }

    @Override
    public void initialize(HmDevice device) {
        String batteryType = batteries.getProperty(device.getType());
        if (batteryType != null) {
            addDatapoint(device, 0, getName(), HmValueType.STRING, batteryType, true);
        }
    }
}
