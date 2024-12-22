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
package org.openhab.binding.freeboxos.internal.api.rest;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.BINDING_ID;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.annotations.SerializedName;

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

    private static class EndpointStateResponse extends Response<EndpointState> {
    }

    private static class HomeNodesResponse extends Response<HomeNode> {
    }

    private enum AccessType {
        R,
        W,
        RW,
        UNKNOWN
    }

    private enum DisplayType {
        TEXT,
        ICON,
        BUTTON,
        SLIDER,
        TOGGLE,
        COLOR,
        WARNING,
        UNKNOWN
    }

    private static record EndpointValue<T> (T value) {
    }

    private static record EndpointUi(AccessType access, DisplayType display, String iconUrl, @Nullable String unit) {
    }

    private enum ValueType {
        BOOL,
        INT,
        FLOAT,
        VOID,
        STRING,
        UNKNOWN
    }

    public static record EndpointState(@Nullable String value, ValueType valueType, long refresh) {
        public boolean asBoolean() {
            String local = value;
            return local != null ? Boolean.valueOf(local) : false;
        }

        public int asInt() {
            String local = value;
            return local != null ? Integer.valueOf(local) : Integer.MIN_VALUE;
        }

        public @Nullable String value() {
            return value;
        }
    }

    public enum EpType {
        SIGNAL,
        SLOT,
        UNKNOWN;

        public String asConfId() {
            return name().toLowerCase();
        }
    }

    public static record LogEntry(Long timestamp, int value) {
    }

    public static record Endpoint(int id, String name, String label, EpType epType, Visibility visibility, int refresh,
            ValueType valueType, EndpointUi ui, @Nullable String category, Object value,
            @Nullable List<LogEntry> history) {

        private static final Comparator<LogEntry> HISTORY_COMPARATOR = Comparator.comparing(LogEntry::timestamp);

        private enum Visibility {
            INTERNAL,
            NORMAL,
            DASHBOARD,
            UNKNOWN
        }

        public Optional<LogEntry> getLastChange() {
            return history != null ? history.stream().max(HISTORY_COMPARATOR) : Optional.empty();
        }
    }

    private enum Status {
        UNREACHABLE,
        DISABLED,
        ACTIVE,
        UNPAIRED,
        UNKNOWN
    }

    public enum Category {
        BASIC_SHUTTER,
        SHUTTER,
        ALARM,
        KFB,
        CAMERA,
        PIR,
        UNKNOWN;

        public final ThingTypeUID thingTypeUID;

        Category() {
            thingTypeUID = new ThingTypeUID(BINDING_ID, name().toLowerCase().replace('_', '-'));
        }
    }

    public static record NodeType(@SerializedName("abstract") boolean _abstract, List<Endpoint> endpoints,
            boolean generic, String icon, String inherit, String label, String name, boolean physical) {
    }

    public static record HomeNode(int id, @Nullable String name, @Nullable String label, Category category,
            Status status, List<Endpoint> showEndpoints, Map<String, String> props, NodeType type) {

        private static final Comparator<Endpoint> ENPOINT_COMPARATOR = Comparator.comparing(Endpoint::refresh);

        public Optional<Endpoint> getMinRefresh() {
            return showEndpoints.stream().filter(ep -> EpType.SIGNAL.equals(ep.epType) && ep.refresh() != 0)
                    .min(ENPOINT_COMPARATOR);
        }

        public Optional<Endpoint> getEndpoint(int slotId) {
            return showEndpoints.stream().filter(ep -> ep.id == slotId).findAny();
        }
    }

    public HomeManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.HOME, session.getUriBuilder().path(PATH));
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

    public <T> boolean putCommand(int nodeId, int stateSignalId, T value) throws FreeboxException {
        put(new EndpointValue<@NonNull T>(value), ENDPOINTS_PATH, String.valueOf(nodeId),
                String.valueOf(stateSignalId));
        return true;
    }
}
