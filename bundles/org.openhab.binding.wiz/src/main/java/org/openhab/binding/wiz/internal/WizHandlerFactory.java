/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.wiz.internal;

import static org.openhab.binding.wiz.internal.WizBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wiz.internal.handler.WizHandler;
import org.openhab.binding.wiz.internal.handler.WizMediator;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WizHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Sriram Balakrishnan - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.wiz", service = ThingHandlerFactory.class)
public class WizHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(WizHandlerFactory.class);

    private final WizMediator mediator;
    private final WizStateDescriptionProvider stateDescriptionProvider;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public WizHandlerFactory(@Reference WizMediator mediator,
            @Reference WizStateDescriptionProvider stateDescriptionProvider,
            @Reference TimeZoneProvider timeZoneProvider) {
        this.mediator = mediator;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (supportsThingType(thing.getThingTypeUID())) {
            WizHandler handler;

            handler = new WizHandler(thing, mediator, stateDescriptionProvider, timeZoneProvider);

            mediator.registerThingAndWizBulbHandler(thing, handler);
            return handler;
        } else {
            logger.warn("Thing type {} not supported.", thingTypeUID);
        }
        return null;
    }

    @Override
    public void unregisterHandler(final Thing thing) {
        mediator.unregisterWizBulbHandlerByThing(thing);
    }
}
