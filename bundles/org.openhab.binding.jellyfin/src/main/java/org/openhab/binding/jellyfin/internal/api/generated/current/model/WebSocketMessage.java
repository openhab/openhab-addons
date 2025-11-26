/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.jellyfin.internal.api.generated.current.model;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openhab.binding.jellyfin.internal.api.generated.JSON;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
@JsonDeserialize(using = WebSocketMessage.WebSocketMessageDeserializer.class)
@JsonSerialize(using = WebSocketMessage.WebSocketMessageSerializer.class)
public class WebSocketMessage extends AbstractOpenApiSchema {
    private static final Logger log = Logger.getLogger(WebSocketMessage.class.getName());

    public static class WebSocketMessageSerializer extends StdSerializer<WebSocketMessage> {
        public WebSocketMessageSerializer(Class<WebSocketMessage> t) {
            super(t);
        }

        public WebSocketMessageSerializer() {
            this(null);
        }

        @Override
        public void serialize(WebSocketMessage value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonProcessingException {
            jgen.writeObject(value.getActualInstance());
        }
    }

    public static class WebSocketMessageDeserializer extends StdDeserializer<WebSocketMessage> {
        public WebSocketMessageDeserializer() {
            this(WebSocketMessage.class);
        }

        public WebSocketMessageDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public WebSocketMessage deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            JsonNode tree = jp.readValueAsTree();
            Object deserialized = null;
            boolean typeCoercion = ctxt.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS);
            int match = 0;
            JsonToken token = tree.traverse(jp.getCodec()).nextToken();
            // deserialize InboundWebSocketMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (InboundWebSocketMessage.class.equals(Integer.class)
                        || InboundWebSocketMessage.class.equals(Long.class)
                        || InboundWebSocketMessage.class.equals(Float.class)
                        || InboundWebSocketMessage.class.equals(Double.class)
                        || InboundWebSocketMessage.class.equals(Boolean.class)
                        || InboundWebSocketMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((InboundWebSocketMessage.class.equals(Integer.class)
                                || InboundWebSocketMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((InboundWebSocketMessage.class.equals(Float.class)
                                || InboundWebSocketMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (InboundWebSocketMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (InboundWebSocketMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(InboundWebSocketMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'InboundWebSocketMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'InboundWebSocketMessage'", e);
            }

            // deserialize OutboundWebSocketMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (OutboundWebSocketMessage.class.equals(Integer.class)
                        || OutboundWebSocketMessage.class.equals(Long.class)
                        || OutboundWebSocketMessage.class.equals(Float.class)
                        || OutboundWebSocketMessage.class.equals(Double.class)
                        || OutboundWebSocketMessage.class.equals(Boolean.class)
                        || OutboundWebSocketMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((OutboundWebSocketMessage.class.equals(Integer.class)
                                || OutboundWebSocketMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((OutboundWebSocketMessage.class.equals(Float.class)
                                || OutboundWebSocketMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (OutboundWebSocketMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (OutboundWebSocketMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(OutboundWebSocketMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'OutboundWebSocketMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'OutboundWebSocketMessage'", e);
            }

            if (match == 1) {
                WebSocketMessage ret = new WebSocketMessage();
                ret.setActualInstance(deserialized);
                return ret;
            }
            throw new IOException(String.format(Locale.ROOT,
                    "Failed deserialization for WebSocketMessage: %d classes match result, expected 1", match));
        }

        /**
         * Handle deserialization of the 'null' value.
         */
        @Override
        public WebSocketMessage getNullValue(DeserializationContext ctxt) throws JsonMappingException {
            throw new JsonMappingException(ctxt.getParser(), "WebSocketMessage cannot be null");
        }
    }

    // store a list of schema names defined in oneOf
    public static final Map<String, Class<?>> schemas = new HashMap<>();

    public WebSocketMessage() {
        super("oneOf", Boolean.FALSE);
    }

    public WebSocketMessage(InboundWebSocketMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public WebSocketMessage(OutboundWebSocketMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    static {
        schemas.put("InboundWebSocketMessage", InboundWebSocketMessage.class);
        schemas.put("OutboundWebSocketMessage", OutboundWebSocketMessage.class);
        JSON.registerDescendants(WebSocketMessage.class, Collections.unmodifiableMap(schemas));
        // Initialize and register the discriminator mappings.
        Map<String, Class<?>> mappings = new HashMap<String, Class<?>>();
        mappings.put("ActivityLogEntry", ActivityLogEntryMessage.class);
        mappings.put("ForceKeepAlive", ForceKeepAliveMessage.class);
        mappings.put("GeneralCommand", GeneralCommandMessage.class);
        mappings.put("KeepAlive", OutboundKeepAliveMessage.class);
        mappings.put("LibraryChanged", LibraryChangedMessage.class);
        mappings.put("PackageInstallationCancelled", PluginInstallationCancelledMessage.class);
        mappings.put("PackageInstallationCompleted", PluginInstallationCompletedMessage.class);
        mappings.put("PackageInstallationFailed", PluginInstallationFailedMessage.class);
        mappings.put("PackageInstalling", PluginInstallingMessage.class);
        mappings.put("PackageUninstalled", PluginUninstalledMessage.class);
        mappings.put("Play", PlayMessage.class);
        mappings.put("Playstate", PlaystateMessage.class);
        mappings.put("RefreshProgress", RefreshProgressMessage.class);
        mappings.put("RestartRequired", RestartRequiredMessage.class);
        mappings.put("ScheduledTaskEnded", ScheduledTaskEndedMessage.class);
        mappings.put("ScheduledTasksInfo", ScheduledTasksInfoMessage.class);
        mappings.put("SeriesTimerCancelled", SeriesTimerCancelledMessage.class);
        mappings.put("SeriesTimerCreated", SeriesTimerCreatedMessage.class);
        mappings.put("ServerRestarting", ServerRestartingMessage.class);
        mappings.put("ServerShuttingDown", ServerShuttingDownMessage.class);
        mappings.put("Sessions", SessionsMessage.class);
        mappings.put("SyncPlayCommand", SyncPlayCommandMessage.class);
        mappings.put("SyncPlayGroupUpdate", SyncPlayGroupUpdateMessage.class);
        mappings.put("TimerCancelled", TimerCancelledMessage.class);
        mappings.put("TimerCreated", TimerCreatedMessage.class);
        mappings.put("UserDataChanged", UserDataChangedMessage.class);
        mappings.put("UserDeleted", UserDeletedMessage.class);
        mappings.put("UserUpdated", UserUpdatedMessage.class);
        mappings.put("InboundWebSocketMessage", InboundWebSocketMessage.class);
        mappings.put("OutboundWebSocketMessage", OutboundWebSocketMessage.class);
        mappings.put("WebSocketMessage", WebSocketMessage.class);
        JSON.registerDiscriminator(WebSocketMessage.class, "MessageType", mappings);
    }

    @Override
    public Map<String, Class<?>> getSchemas() {
        return WebSocketMessage.schemas;
    }

    /**
     * Set the instance that matches the oneOf child schema, check
     * the instance parameter is valid against the oneOf child schemas:
     * InboundWebSocketMessage, OutboundWebSocketMessage
     *
     * It could be an instance of the 'oneOf' schemas.
     * The oneOf child schemas may themselves be a composed schema (allOf, anyOf, oneOf).
     */
    @Override
    public void setActualInstance(Object instance) {
        if (JSON.isInstanceOf(InboundWebSocketMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(OutboundWebSocketMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        throw new RuntimeException("Invalid instance type. Must be InboundWebSocketMessage, OutboundWebSocketMessage");
    }

    /**
     * Get the actual instance, which can be the following:
     * InboundWebSocketMessage, OutboundWebSocketMessage
     *
     * @return The actual instance (InboundWebSocketMessage, OutboundWebSocketMessage)
     */
    @Override
    public Object getActualInstance() {
        return super.getActualInstance();
    }

    /**
     * Get the actual instance of `InboundWebSocketMessage`. If the actual instance is not `InboundWebSocketMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `InboundWebSocketMessage`
     * @throws ClassCastException if the instance is not `InboundWebSocketMessage`
     */
    public InboundWebSocketMessage getInboundWebSocketMessage() throws ClassCastException {
        return (InboundWebSocketMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `OutboundWebSocketMessage`. If the actual instance is not `OutboundWebSocketMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `OutboundWebSocketMessage`
     * @throws ClassCastException if the instance is not `OutboundWebSocketMessage`
     */
    public OutboundWebSocketMessage getOutboundWebSocketMessage() throws ClassCastException {
        return (OutboundWebSocketMessage) super.getActualInstance();
    }

    /**
     * Convert the instance into URL query string.
     *
     * @return URL query string
     */
    public String toUrlQueryString() {
        return toUrlQueryString(null);
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        String suffix = "";
        String containerSuffix = "";
        String containerPrefix = "";
        if (prefix == null) {
            // style=form, explode=true, e.g. /pet?name=cat&type=manx
            prefix = "";
        } else {
            // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
            prefix = prefix + "[";
            suffix = "]";
            containerSuffix = "]";
            containerPrefix = "[";
        }

        StringJoiner joiner = new StringJoiner("&");

        if (getActualInstance() instanceof InboundWebSocketMessage) {
            if (getActualInstance() != null) {
                joiner.add(
                        ((InboundWebSocketMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_0" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof OutboundWebSocketMessage) {
            if (getActualInstance() != null) {
                joiner.add(((OutboundWebSocketMessage) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_1" + suffix));
            }
            return joiner.toString();
        }
        return null;
    }
}
