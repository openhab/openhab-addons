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
package org.openhab.binding.freeboxos.internal.api.rest;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.BINDING_ID;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState.ValueType;
import org.openhab.binding.freeboxos.internal.api.rest.LoginManager.Session.Permission;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * The {@link HomeManager} is the Java class used to handle api requests related to home
 *
 * @author ben12 - Initial contribution
 */
@NonNullByDefault
public class HomeManager extends RestManager {
    private static final String PATH = "home";
    private static final String NODES_PATH = "nodes";
    private static final String ENDPOINTS_PATH = "endpoints";

    public static class EndpointStateResponse extends Response<EndpointState> {
    }

    public static class HomeNodesResponse extends Response<HomeNode> {
    }

    public static enum AccessType {
        R,
        W,
        RW,
        UNKNOWN;
    }

    public static enum DisplayType {
        TEXT,
        ICON,
        BUTTON,
        SLIDER,
        TOGGLE,
        COLOR,
        WARNING,
        UNKNOWN;
    }

    public static record EndpointUi(AccessType access, DisplayType display, String iconUrl, @Nullable String unit) {

        public @Nullable String unit() {
            return unit;
        }
    }

    public static record EndpointValue<T> (T value) {
    }

    public static record EndpointState(@Nullable JsonElement value, ValueType valueType, long refresh) {
        public static enum ValueType {
            BOOL,
            INT,
            FLOAT,
            VOID,
            STRING,
            UNKNOWN;
        }

        private @Nullable JsonPrimitive getAsPrimitive() {
            final JsonElement theValue = value;
            return theValue != null && theValue.isJsonPrimitive() ? theValue.getAsJsonPrimitive() : null;
        }

        public @Nullable Boolean asBoolean() {
            final JsonPrimitive theValue = getAsPrimitive();
            return theValue != null && theValue.isBoolean() ? theValue.getAsBoolean() : null;
        }

        public @Nullable Integer asInt() {
            final JsonPrimitive theValue = getAsPrimitive();
            return theValue != null && theValue.isNumber() ? theValue.getAsInt() : null;
        }

        public @Nullable String asString() {
            final JsonPrimitive theValue = getAsPrimitive();
            return theValue != null && theValue.isString() ? theValue.getAsString() : null;
        }
    }

    public static enum EpType {
        SIGNAL,
        SLOT,
        UNKNOWN;
    }

    public static record Endpoint(int id, String name, String label, EpType epType, Visibility visibility, int refresh,
            ValueType valueType, EndpointUi ui, @Nullable String category, Object value) {
        private static enum Visibility {
            INTERNAL,
            NORMAL,
            DASHBOARD,
            UNKNOWN;
        }
    }

    public static enum Status {
        UNREACHABLE,
        DISABLED,
        ACTIVE,
        UNPAIRED,
        UNKNOWN;
    }

    public static enum Category {
        BASIC_SHUTTER,
        SHUTTER,
        ALARM,
        KFB,
        CAMERA,
        UNKNOWN;

        private final ThingTypeUID thingTypeUID;

        Category() {
            thingTypeUID = new ThingTypeUID(BINDING_ID, name().toLowerCase());
        }

        public ThingTypeUID getThingTypeUID() {
            return thingTypeUID;
        }
    }

    public static record HomeNode(int id, @Nullable String name, @Nullable String label, Category category,
            Status status, List<Endpoint> showEndpoints) {
    }

    public HomeManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.HOME, session.getUriBuilder().path(PATH));
    }

    public List<HomeNode> getHomeNodes() throws FreeboxException {
        return get(HomeNodesResponse.class, NODES_PATH);
    }

    public HomeNode getHomeNode(int nodeId) throws FreeboxException {
        return getSingle(HomeNodesResponse.class, NODES_PATH, Integer.toString(nodeId));
    }

    public <T> @Nullable EndpointState getEndpointsState(int nodeId, int stateSignalId) throws FreeboxException {
        return getSingle(EndpointStateResponse.class, ENDPOINTS_PATH, String.valueOf(nodeId),
                String.valueOf(stateSignalId));
    }

    public <T> void putCommand(int nodeId, int stateSignalId, T value) throws FreeboxException {
        put(new EndpointValue<T>(value), ENDPOINTS_PATH, String.valueOf(nodeId), String.valueOf(stateSignalId));
    }
}
