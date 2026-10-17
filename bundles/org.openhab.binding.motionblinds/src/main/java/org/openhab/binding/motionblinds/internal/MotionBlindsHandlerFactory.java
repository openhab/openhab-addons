/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.motionblinds.internal;

import static org.openhab.binding.motionblinds.internal.MotionBlindsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MotionBlindsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.motionblinds", service = ThingHandlerFactory.class)
public class MotionBlindsHandlerFactory extends BaseThingHandlerFactory {

    private final MotionBlindsCommunicationManager communicationManager;

    @Activate
    public MotionBlindsHandlerFactory(final @Reference MotionBlindsCommunicationManager communicationManager) {
        this.communicationManager = communicationManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_WIFI_MOTOR.equals(thing.getThingTypeUID())) {
            return new MotionBlindsHandler(thing, communicationManager);
        }
        return null;
    }
}
