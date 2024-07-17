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
package org.openhab.binding.mercedesme.internal.actions;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.mercedesme.internal.handler.VehicleHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * {@link VehicleActions} which can be sent to a vehicle
 *
 * @author Bernd Weymann - Initial contribution
 */
@ThingActionsScope(name = "mercedesme")
@NonNullByDefault
public class VehicleActions implements ThingActions {
    private Optional<VehicleHandler> thingHandler = Optional.empty();
    private String[] argumentKey = new String[] { "city", "street", "postcode" };

    @RuleAction(label = "@text/actionPoiLabel", description = "@text/actionPoiDescription")
    /**
     * Send Point of Interest (POI) to your vehicle.
     * This POI is shown in your vehicle messages and can be instantly used to start a navigation route to this point.
     * A "catchy" title plus latitude / longitude are mandatory.
     * Parameters args is optional. If you use it respect the following order
     * 1) City
     * 2) Street
     * 3) Postal Code
     * If you miss any of them provide an empty String
     *
     * @param title - the title will be shown in your vehicle message inbox
     * @param latitude - latitude of POI location
     * @param longitude - longitude of POI location
     * @param args - optional but respect order city, street, postal code
     */
    public void sendPoi(
            @ActionInput(name = "title", label = "@text/poiTitle", description = "@text/poiTitleDescription") String title,
            @ActionInput(name = "latitude", label = "@text/latitudeLabel", description = "@text/latitudeDescription") double latitude,
            @ActionInput(name = "longitude", label = "@text/longitudeLabel", description = "@text/longitudeDescription") double longitude,
            String... args) {
        if (thingHandler.isPresent()) {
            JSONObject poi = new JSONObject();
            poi.put("routeTitle", title);
            poi.put("routeType", "singlePOI");
            JSONArray waypoints = new JSONArray();
            JSONObject waypoint = new JSONObject();
            waypoint.put("title", title);
            waypoint.put("latitude", latitude);
            waypoint.put("longitude", longitude);
            for (int i = 0; i < args.length; i++) {
                waypoint.put(argumentKey[i], args[i]);
            }
            waypoints.put(waypoint);
            poi.put("waypoints", waypoints);
            thingHandler.get().sendPoi(poi);
        }
    }

    public static void sendPoi(ThingActions actions, String title, double lat, double lon, String... args) {
        ((VehicleActions) actions).sendPoi(title, lat, lon, args);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        thingHandler = Optional.of((VehicleHandler) handler);
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        if (thingHandler.isPresent()) {
            return thingHandler.get();
        }
        return null;
    }
}
