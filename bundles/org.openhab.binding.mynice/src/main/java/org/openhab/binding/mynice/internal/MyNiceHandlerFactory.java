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
package org.openhab.binding.mynice.internal;

import static org.openhab.binding.mynice.internal.MyNiceBindingConstants.*;

import java.util.Objects;
import java.util.Set;

import javax.net.ssl.SSLSocketFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mynice.internal.handler.GateHandler;
import org.openhab.binding.mynice.internal.handler.It4WifiHandler;
import org.openhab.binding.mynice.internal.ssl.BcJsseSocketFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * The {@link MyNiceHandlerFactory} is responsible for creating thing handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.mynice", service = ThingHandlerFactory.class)
public class MyNiceHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BRIDGE_TYPE_IT4WIFI, THING_TYPE_SWING,
            THING_TYPE_SLIDING);
    private @Nullable SSLSocketFactory socketFactory;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BRIDGE_TYPE_IT4WIFI.equals(thingTypeUID)) {
            return new It4WifiHandler((Bridge) thing, getSocketFactory());
        } else if (THING_TYPE_SWING.equals(thingTypeUID)) {
            return new GateHandler(thing);
        } else if (THING_TYPE_SLIDING.equals(thingTypeUID)) {
            return new GateHandler(thing);
        }

        return null;
    }

    private SSLSocketFactory getSocketFactory() {
        if (socketFactory == null) {
            socketFactory = BcJsseSocketFactory.get();
        }
        return Objects.requireNonNull(socketFactory);
    }

    @Deactivate
    public void dispose() {
        BcJsseSocketFactory.dispose();
    }
}
