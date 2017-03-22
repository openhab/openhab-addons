/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.exception.NoContentException;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageSubType;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVEc;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVOrp;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVPh;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVStatus;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVTemp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MySensors Child definition according to MySensors serial API
 * https://www.mysensors.org/download/serial_api_20
 * 
 * @author Andrea Cioni
 * @author Tim Oberföll
 *
 */
public class MySensorsChildSWaterQuality extends MySensorsChild {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    public MySensorsChildSWaterQuality(int childId) {
        super(childId);
        setPresentationCode(MySensorsMessageSubType.S_WATER_QUALITY);
        try {
            addVariable(new MySensorsVariableVTemp());
            addVariable(new MySensorsVariableVPh());
            addVariable(new MySensorsVariableVOrp());
            addVariable(new MySensorsVariableVEc());
            addVariable(new MySensorsVariableVStatus());
        } catch (NoContentException e) {
            logger.debug("No content to add: {}", e);
        }
    }

}
