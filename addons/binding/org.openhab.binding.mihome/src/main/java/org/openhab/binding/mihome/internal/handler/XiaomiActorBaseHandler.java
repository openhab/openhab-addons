/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mihome.internal.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for controllable devices
 *
 * @author Dieter Schmidt - Initial contribution
 */
public abstract class XiaomiActorBaseHandler extends XiaomiDeviceBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(XiaomiActorBaseHandler.class);

    public XiaomiActorBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    void execute(ChannelUID channelUID, Command command) {
        logger.debug("The binding does not support this message yet, contact authors if you want it to");
    }
}
