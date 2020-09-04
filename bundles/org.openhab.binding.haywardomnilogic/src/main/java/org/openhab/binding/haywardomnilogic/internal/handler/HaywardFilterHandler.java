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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.haywardomnilogic.internal.hayward.HaywardThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Filter Handler
 *
 * @author Matt Myers - Initial Contribution
 */
@NonNullByDefault
public class HaywardFilterHandler extends HaywardThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HaywardFilterHandler.class);

    public HaywardFilterHandler(Thing thing) {
        super(thing);
    }

    public void setFilterProperty(String systemID, String channelID, String data) {
        Channel chan = getThing().getChannel(channelID);
        if (chan != null) {
            Map<String, String> properties = editProperties();
            updateProperties(properties);
            logger.trace("Updated Hayward Filter {} {} to: {}", systemID, channelID, data);
        }
    }

    public void getFilterProperty(String systemID, String channelID, String data) {
        Channel chan = getThing().getChannel(channelID);
        if (chan != null) {
            updateState(chan.getUID(), new DecimalType(data));
            logger.trace("Updated Hayward Filter {} {} to: {}", systemID, channelID, data);
        }
    }
}
