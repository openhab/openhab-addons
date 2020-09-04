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
package org.openhab.binding.haywardomnilogic.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.haywardomnilogic.internal.hayward.HaywardThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Sensor Handler
 *
 * @author Matt Myers - Initial Contribution
 */
@NonNullByDefault
public class HaywardSensorHandler extends HaywardThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HaywardSensorHandler.class);

    public HaywardSensorHandler(Thing thing) {
        super(thing);
    }
}
