/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.netatmo.config.AbstractNetatmoThingConfiguration;

/**
 * {@link AbstractNetatmoWelcomeThingHandler} is the abstract class that handles
 * common behaviors of welcome camera devices
 *
 * @author Ing. Peter Weiss
 */
abstract class AbstractNetatmoWelcomeThingHandler<X extends AbstractNetatmoThingConfiguration>
        extends AbstractNetatmoThingHandler<X> {

    AbstractNetatmoWelcomeThingHandler(Thing thing, Class<X> configurationClass) {
        super(thing, configurationClass);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateChannels(configuration.getId());
        }
    }

}
