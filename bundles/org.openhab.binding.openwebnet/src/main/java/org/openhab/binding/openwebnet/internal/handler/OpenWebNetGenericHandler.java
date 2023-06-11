/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.openwebnet.internal.handler;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereLightAutom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetGenericHandler} is responsible for handling Generic OpenWebNet
 * devices. It does not too much, but it is needed to avoid handler errors and to tell the user
 * that some device has been found by the gateway but it was not recognised.
 * It extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
public class OpenWebNetGenericHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetGenericHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.GENERIC_SUPPORTED_THING_TYPES;

    public OpenWebNetGenericHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        // do nothing
        logger.warn("Generic: there are no channels");
    }

    @Override
    protected void refreshDevice(boolean refreshAll) {
        // do nothing
        logger.warn("Generic: nothing to refresh");
    }

    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {
        // do nothing
        logger.warn("Generic: there are no channels");
    }

    @Override
    protected String ownIdPrefix() {
        return "G";
    }

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        return new WhereLightAutom(wStr);
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        super.handleMessage(msg);
        // do nothing
        logger.warn("Generic: handleMessage() nothing to do!");
    }
}
