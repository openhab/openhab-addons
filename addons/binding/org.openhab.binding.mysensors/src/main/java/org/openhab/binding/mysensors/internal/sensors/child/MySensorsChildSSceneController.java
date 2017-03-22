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
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVSceneOff;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVSceneOn;
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
public class MySensorsChildSSceneController extends MySensorsChild {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    public MySensorsChildSSceneController(int childId) {
        super(childId);
        setPresentationCode(MySensorsMessageSubType.S_SCENE_CONTROLLER);
        try {
            addVariable(new MySensorsVariableVSceneOn());
            addVariable(new MySensorsVariableVSceneOff());
        } catch (NoContentException e) {
            logger.debug("No content to add: {}", e);
        }
    }

}
