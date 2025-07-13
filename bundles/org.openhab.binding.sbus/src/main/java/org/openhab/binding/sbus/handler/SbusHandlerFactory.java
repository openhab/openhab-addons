/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sbus.handler;

import static org.openhab.binding.sbus.BindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SbusHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.sbus")
public class SbusHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SbusHandlerFactory.class);
    private @Nullable SbusService sbusService;
    private @Nullable TranslationProvider translationProvider;
    private @Nullable LocaleProvider localeProvider;

    @Reference
    public void setSbusService(final SbusService service) {
        this.sbusService = service;
    }

    @Reference
    public void setTranslationProvider(TranslationProvider translationProvider) {
        this.translationProvider = translationProvider;
    }

    @Reference
    public void setLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_UDP_BRIDGE, THING_TYPE_SWITCH,
            THING_TYPE_TEMPERATURE, THING_TYPE_RGBW, THING_TYPE_CONTACT);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        final TranslationProvider tp = translationProvider;
        final LocaleProvider lp = localeProvider;
        if (tp == null || lp == null) {
            logger.error("Required services not available");
            return null;
        }

        if (thingTypeUID.equals(THING_TYPE_UDP_BRIDGE)) {
            logger.debug("Creating Sbus UDP bridge handler for thing {}", thing.getUID());
            return new SbusBridgeHandler((Bridge) thing, sbusService, tp, lp);
        }

        if (thingTypeUID.equals(THING_TYPE_SWITCH)) {
            logger.debug("Creating Sbus switch handler for thing {}", thing.getUID());
            return new SbusSwitchHandler(thing, tp, lp);
        } else if (thingTypeUID.equals(THING_TYPE_TEMPERATURE)) {
            logger.debug("Creating Sbus temperature handler for thing {}", thing.getUID());
            return new SbusTemperatureHandler(thing, tp, lp);
        } else if (thingTypeUID.equals(THING_TYPE_RGBW)) {
            logger.debug("Creating Sbus RGBW handler for thing {}", thing.getUID());
            return new SbusRgbwHandler(thing, tp, lp);
        } else if (thingTypeUID.equals(THING_TYPE_CONTACT)) {
            logger.debug("Creating Sbus contact handler for thing {}", thing.getUID());
            return new SbusContactHandler(thing, tp, lp);
        }

        logger.debug("Unknown thing type: {}", thingTypeUID);
        return null;
    }
}
