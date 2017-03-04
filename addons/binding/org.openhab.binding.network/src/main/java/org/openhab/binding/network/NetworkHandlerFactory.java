/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network;

import static org.openhab.binding.network.NetworkBindingConstants.*;

import java.util.Dictionary;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.network.handler.NetworkHandlerBuilder;
import org.osgi.service.component.ComponentContext;

/**
 * The handler factory retrieves the binding configuration and is responsible for creating
 * PING_DEVICE and SERVICE_DEVICE handlers.
 *
 * @author David Graeff
 * @author Marc Mettke
 */
public class NetworkHandlerFactory extends BaseThingHandlerFactory {
    private boolean allowSystemPings;
    private boolean allowDHCPlisten;
    private int cacheDeviceStateTimeInMS;
    private String arpPingToolPath;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Returns the parameter as a string or the
     * <code>defaultValue</code> if the parameter is <code>null</code>.
     *
     * @param propValue the property value or <code>null</code>
     * @param defaultValue the default string value
     */
    public static String toString(Object propValue, String defaultValue) {
        return (propValue != null) ? propValue.toString() : defaultValue;
    }

    /**
     * Returns the boolean value of the parameter or the
     * <code>defaultValue</code> if the parameter is <code>null</code>.
     * If the parameter is not a <code>Boolean</code> it is converted
     * by calling <code>Boolean.valueOf</code> on the string value of the
     * object.
     *
     * @param propValue the property value or <code>null</code>
     * @param defaultValue the default boolean value
     */
    public static boolean toBoolean(Object propValue, boolean defaultValue) {
        if (propValue instanceof Boolean) {
            return (Boolean) propValue;
        } else if (propValue != null) {
            return Boolean.valueOf(String.valueOf(propValue));
        }
        return defaultValue;
    }

    /**
     * Returns the parameter as an integer or the
     * <code>defaultValue</code> if the parameter is <code>null</code> or if
     * the parameter is not an <code>Integer</code> and cannot be converted to
     * an <code>Integer</code> from the parameter's string value.
     *
     * @param propValue the property value or <code>null</code>
     * @param defaultValue the default integer value
     */
    public static int toInteger(Object propValue, int defaultValue) {
        if (propValue instanceof Integer) {
            return (Integer) propValue;
        } else if (propValue != null) {
            try {
                return Integer.valueOf(String.valueOf(propValue));
            } catch (NumberFormatException nfe) {
                // don't care, fall through to default value
            }
        }
        return defaultValue;
    }

    // The activate component call is used to access the bindings configuration
    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        allowSystemPings = toBoolean(properties.get("allow_system_pings"), true);
        allowDHCPlisten = toBoolean(properties.get("allow_dhcp_listen"), true);
        cacheDeviceStateTimeInMS = toInteger(properties.get("cache_device_state"), 2);
        arpPingToolPath = toString(properties.get("arp_ping_tool_path"), "arping");
    };

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(PING_DEVICE) || thingTypeUID.equals(BACKWARDS_COMPATIBLE_DEVICE)) {
            return NetworkHandlerBuilder.createPingDevice(thing).allowSystemPings(allowSystemPings)
                    .allowDHCPListen(allowDHCPlisten).cacheTimeInMS(cacheDeviceStateTimeInMS)
                    .arpPingToolPath(arpPingToolPath).build();
        } else if (thingTypeUID.equals(SERVICE_DEVICE)) {
            return NetworkHandlerBuilder.createServiceDevice(thing).allowSystemPings(allowSystemPings)
                    .allowDHCPListen(allowDHCPlisten).cacheTimeInMS(cacheDeviceStateTimeInMS)
                    .arpPingToolPath(arpPingToolPath).build();
        }
        return null;
    }
}
