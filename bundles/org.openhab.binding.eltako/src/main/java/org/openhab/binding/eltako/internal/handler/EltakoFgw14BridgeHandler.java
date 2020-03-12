/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.eltako.internal.handler;

import static org.openhab.binding.eltako.internal.misc.EltakoBindingConstants.THING_TYPE_FGW14;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;

/**
 * The {@link EltakoFgw14BridgeHandler} is responsible for handling connection to serial interface
 *
 * @author Martin Wenske - Initial contribution
 */
public class EltakoFgw14BridgeHandler extends EltakoGenericBridgeHandler {

    /*
     * Logger instance to create log entries
     */
    // private Logger logger = LoggerFactory.getLogger(EltakoFgw14BridgeHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_FGW14));

    /**
     * Initializer method
     */
    public EltakoFgw14BridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge, serialPortManager);
    }

}