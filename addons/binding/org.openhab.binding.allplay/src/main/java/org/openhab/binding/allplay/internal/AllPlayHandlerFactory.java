/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.allplay.internal;

import static org.openhab.binding.allplay.AllPlayBindingConstants.SPEAKER_THING_TYPE;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.allplay.AllPlayBindingConstants;
import org.openhab.binding.allplay.handler.AllPlayHandler;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kaizencode.tchaikovsky.AllPlay;
import de.kaizencode.tchaikovsky.exception.AllPlayException;

/**
 * The {@link AllPlayHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dominic Lerbs - Initial contribution
 */
public class AllPlayHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(AllPlayHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(SPEAKER_THING_TYPE);

    private AllPlay allPlay;
    private CommonSpeakerProperties speakerProperties;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(AllPlayBindingConstants.SPEAKER_THING_TYPE)) {
            logger.debug("Creating AllPlayHandler for thing {}", thing.getUID());
            return new AllPlayHandler(thing, allPlay, speakerProperties);
        }
        return null;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        logger.debug("Activating AllPlayHandlerFactory");
        allPlay = new AllPlay("openHAB2");
        try {
            logger.debug("Connecting to AllPlay");
            allPlay.connect();
        } catch (AllPlayException e) {
            logger.error("Cannot initialize AllPlay", e);
        }
        Dictionary<String, Object> properties = componentContext.getProperties();
        speakerProperties = new CommonSpeakerProperties(properties);
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        logger.debug("Deactivating AllPlayHandlerFactory");
        allPlay.disconnect();
        allPlay = null;
        super.deactivate(componentContext);
    }

}
