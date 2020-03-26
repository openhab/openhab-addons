/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.webthings.internal;

import static org.openhab.binding.webthings.internal.WebThingsBindingConstants.*;
import static org.openhab.binding.webthings.internal.WebThingsBindingGlobals.*;
import org.openhab.binding.webthings.internal.handler.*;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link WebThingsHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Sven Schneider - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.webthings", service = ThingHandlerFactory.class)
public class WebThingsHandlerFactory extends BaseThingHandlerFactory {
    private Map<String, Object> serverParams = new HashMap<String, Object>();

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();
    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_CONNECTOR);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_SERVER);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_WEBTHING);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);

        Dictionary<String, Object> properties = componentContext.getProperties();

        serverParams.put("serverUrl", (String) properties.get("serverUrl"));
        serverParams.put("token", (String) properties.get("token"));
        serverParams.put("openhabIp", (String) properties.get("openhabIp"));
        serverParams.put("mozilla", properties.get("mozilla"));
        serverParams.put("system", properties.get("system"));
        serverParams.put("userdataPath", properties.get("userdataPath"));
        serverParams.put("backgroundDiscovery", properties.get("backgroundDiscovery"));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        setParams(serverParams);

        if (THING_TYPE_CONNECTOR.equals(thingTypeUID)) {
            return new WebThingsConnectorHandler(thing);
        } else if(THING_TYPE_SERVER.equals(thingTypeUID)){
            return new WebThingsServerHandler(thing);
        } else if(THING_TYPE_WEBTHING.equals(thingTypeUID)){
            return new WebThingsWebThingHandler(thing);
        }
        

        return null;
    }
}
