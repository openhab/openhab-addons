/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.souliss.internal;

import static org.openhab.binding.souliss.internal.SoulissBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.internal.handler.SoulissGatewayHandler;
import org.openhab.binding.souliss.internal.handler.SoulissT11Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT12Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT13Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT14Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT16Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT18Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT19Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT1AHandler;
import org.openhab.binding.souliss.internal.handler.SoulissT22Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT31Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT41Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT42Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT51Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT52Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT53Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT54Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT55Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT56Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT57Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT61Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT62Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT63Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT64Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT65Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT66Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT67Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT68Handler;
import org.openhab.binding.souliss.internal.handler.SoulissTopicsHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link SoulissHandlerFactory} is responsible for creating things and thingGeneric
 * handlers. It fire when a new thingGeneric is added.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
@Component(configurationPid = "binding.souliss", service = ThingHandlerFactory.class)
public class SoulissHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        var thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(GATEWAY_THING_TYPE)) {
            return new SoulissGatewayHandler((Bridge) thing);
        } else if (thingTypeUID.equals(T11_THING_TYPE)) {
            return new SoulissT11Handler(thing);
        } else if (thingTypeUID.equals(T12_THING_TYPE)) {
            return new SoulissT12Handler(thing);
        } else if (thingTypeUID.equals(T13_THING_TYPE)) {
            return new SoulissT13Handler(thing);
        } else if (thingTypeUID.equals(T14_THING_TYPE)) {
            return new SoulissT14Handler(thing);
        } else if (thingTypeUID.equals(T16_THING_TYPE)) {
            return new SoulissT16Handler(thing);
        } else if (thingTypeUID.equals(T18_THING_TYPE)) {
            return new SoulissT18Handler(thing);
        } else if (thingTypeUID.equals(T19_THING_TYPE)) {
            return new SoulissT19Handler(thing);
        } else if (thingTypeUID.equals(T1A_THING_TYPE)) {
            return new SoulissT1AHandler(thing);
        } else if (thingTypeUID.equals(T21_THING_TYPE) || (thingTypeUID.equals(T22_THING_TYPE))) {
            return new SoulissT22Handler(thing);
        } else if (thingTypeUID.equals(T31_THING_TYPE)) {
            return new SoulissT31Handler(thing);
        } else if (thingTypeUID.equals(T41_THING_TYPE)) {
            return new SoulissT41Handler(thing);
        } else if (thingTypeUID.equals(T42_THING_TYPE)) {
            return new SoulissT42Handler(thing);
        } else if (thingTypeUID.equals(T51_THING_TYPE)) {
            return new SoulissT51Handler(thing);
        } else if (thingTypeUID.equals(T52_THING_TYPE)) {
            return new SoulissT52Handler(thing);
        } else if (thingTypeUID.equals(T53_THING_TYPE)) {
            return new SoulissT53Handler(thing);
        } else if (thingTypeUID.equals(T54_THING_TYPE)) {
            return new SoulissT54Handler(thing);
        } else if (thingTypeUID.equals(T55_THING_TYPE)) {
            return new SoulissT55Handler(thing);
        } else if (thingTypeUID.equals(T56_THING_TYPE)) {
            return new SoulissT56Handler(thing);
        } else if (thingTypeUID.equals(T57_THING_TYPE)) {
            return new SoulissT57Handler(thing);
        } else if (thingTypeUID.equals(T61_THING_TYPE)) {
            return new SoulissT61Handler(thing);
        } else if (thingTypeUID.equals(T62_THING_TYPE)) {
            return new SoulissT62Handler(thing);
        } else if (thingTypeUID.equals(T63_THING_TYPE)) {
            return new SoulissT63Handler(thing);
        } else if (thingTypeUID.equals(T64_THING_TYPE)) {
            return new SoulissT64Handler(thing);
        } else if (thingTypeUID.equals(T65_THING_TYPE)) {
            return new SoulissT65Handler(thing);
        } else if (thingTypeUID.equals(T66_THING_TYPE)) {
            return new SoulissT66Handler(thing);
        } else if (thingTypeUID.equals(T67_THING_TYPE)) {
            return new SoulissT67Handler(thing);
        } else if (thingTypeUID.equals(T68_THING_TYPE)) {
            return new SoulissT68Handler(thing);
        } else if (thingTypeUID.equals(TOPICS_THING_TYPE)) {
            return new SoulissTopicsHandler(thing);
        }

        return null;
    }
}
