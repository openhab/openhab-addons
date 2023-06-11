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
package org.openhab.binding.milight.internal;

import static org.openhab.binding.milight.internal.MilightBindingConstants.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.milight.internal.handler.BridgeV3Handler;
import org.openhab.binding.milight.internal.handler.BridgeV6Handler;
import org.openhab.binding.milight.internal.handler.MilightV2RGBHandler;
import org.openhab.binding.milight.internal.handler.MilightV3RGBWHandler;
import org.openhab.binding.milight.internal.handler.MilightV3WhiteHandler;
import org.openhab.binding.milight.internal.handler.MilightV6RGBCWWWHandler;
import org.openhab.binding.milight.internal.handler.MilightV6RGBIBOXHandler;
import org.openhab.binding.milight.internal.handler.MilightV6RGBWHandler;
import org.openhab.binding.milight.internal.protocol.QueuedSend;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link MilightHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.milight")
public class MilightHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(BRIDGEV3_THING_TYPE, BRIDGEV6_THING_TYPE, RGB_V2_THING_TYPE, RGB_THING_TYPE, WHITE_THING_TYPE,
                    RGB_W_THING_TYPE, RGB_CW_WW_THING_TYPE, RGB_IBOX_THING_TYPE).collect(Collectors.toSet()));

    // The UDP queue for bridge communication is a single instance for all bridges.
    // This is because all bridges use the same radio frequency, and if multiple
    // bridges would send a command at the same time, they would interfere with
    // each other (user report!).
    private @Nullable QueuedSend queuedSend;

    private int bridgeOffset;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        final QueuedSend queuedSend = this.queuedSend;
        if (queuedSend == null) {
            return null;
        }

        if (thingTypeUID.equals(BRIDGEV3_THING_TYPE)) {
            return new BridgeV3Handler((Bridge) thing, bridgeOffset += 100);
        } else if (thingTypeUID.equals(BRIDGEV6_THING_TYPE)) {
            return new BridgeV6Handler((Bridge) thing, bridgeOffset += 100);
        } else if (thing.getThingTypeUID().equals(MilightBindingConstants.RGB_IBOX_THING_TYPE)) {
            return new MilightV6RGBIBOXHandler(thing, queuedSend);
        } else if (thing.getThingTypeUID().equals(MilightBindingConstants.RGB_CW_WW_THING_TYPE)) {
            return new MilightV6RGBCWWWHandler(thing, queuedSend);
        } else if (thing.getThingTypeUID().equals(MilightBindingConstants.RGB_W_THING_TYPE)) {
            return new MilightV6RGBWHandler(thing, queuedSend);
        } else if (thing.getThingTypeUID().equals(MilightBindingConstants.RGB_V2_THING_TYPE)) {
            return new MilightV2RGBHandler(thing, queuedSend);
        } else if (thing.getThingTypeUID().equals(MilightBindingConstants.WHITE_THING_TYPE)) {
            return new MilightV3WhiteHandler(thing, queuedSend);
        } else if (thing.getThingTypeUID().equals(MilightBindingConstants.RGB_THING_TYPE)) {
            return new MilightV3RGBWHandler(thing, queuedSend);
        }

        return null;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        queuedSend = new QueuedSend();
        queuedSend.start();
        bridgeOffset = 0;
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        QueuedSend queuedSend = this.queuedSend;
        if (queuedSend != null) {
            try {
                queuedSend.close();
            } catch (IOException ignore) {
            }
        }
        super.deactivate(componentContext);
    }
}
