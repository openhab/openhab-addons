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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState.ValueType;
import org.openhab.binding.freeboxos.internal.api.rest.LoginManager.Session.Permission;

import com.google.gson.JsonElement;

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

    private static record EndpointUi(String access) {
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

        public @Nullable Boolean asBoolean() {
            final JsonElement theValue = value;
            if (theValue != null && theValue.isJsonPrimitive() && theValue.getAsJsonPrimitive().isBoolean()) {
                return theValue.getAsBoolean();
            }
            return null;
        }

        public @Nullable Integer asInt() {
            final JsonElement theValue = value;
            if (theValue != null && theValue.isJsonPrimitive() && theValue.getAsJsonPrimitive().isNumber()) {
                return theValue.getAsInt();
            }
            return null;
        }
    }

    public static enum EpType {
        SIGNAL,
        SLOT,
        UNKNOWN;
    }

    public static record Endpoint(int id, @Nullable String name, @Nullable String label, EpType epType,
            Visibility visibility, int refresh, ValueType valueType, EndpointUi ui) {
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

    public static record HomeNode(int id, @Nullable String name, @Nullable String label, @Nullable String category,
            Status status, List<Endpoint> showEndpoints) {
    }

    public HomeManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.HOME, session.getUriBuilder().path(PATH));
    }

    public List<HomeNode> getHomeNodes() throws FreeboxException {
        return get(HomeNodesResponse.class, NODES_PATH);
    }

    public <T> @Nullable EndpointState getEndpointsState(int nodeId, int stateSignalId) throws FreeboxException {
        return getSingle(EndpointStateResponse.class, ENDPOINTS_PATH, String.valueOf(nodeId),
                String.valueOf(stateSignalId));
    }

    public <T> void putCommand(int nodeId, int stateSignalId, T value) throws FreeboxException {
        put(GenericResponse.class, new EndpointValue<T>(value), ENDPOINTS_PATH, String.valueOf(nodeId),
                String.valueOf(stateSignalId));
    }
}
