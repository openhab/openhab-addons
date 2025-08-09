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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.GenericType;

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

@JsonDeserialize(using = InboundWebSocketMessage.InboundWebSocketMessageDeserializer.class)
@JsonSerialize(using = InboundWebSocketMessage.InboundWebSocketMessageSerializer.class)
public class InboundWebSocketMessage extends AbstractOpenApiSchema {
    private static final Logger log = Logger.getLogger(InboundWebSocketMessage.class.getName());

    public static class InboundWebSocketMessageSerializer extends StdSerializer<InboundWebSocketMessage> {
        public InboundWebSocketMessageSerializer(Class<InboundWebSocketMessage> t) {
            super(t);
        }

        public InboundWebSocketMessageSerializer() {
            this(null);
        }

        @Override
        public void serialize(InboundWebSocketMessage value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonProcessingException {
            jgen.writeObject(value.getActualInstance());
        }
    }

    public static class InboundWebSocketMessageDeserializer extends StdDeserializer<InboundWebSocketMessage> {
        public InboundWebSocketMessageDeserializer() {
            this(InboundWebSocketMessage.class);
        }

        public InboundWebSocketMessageDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public InboundWebSocketMessage deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            JsonNode tree = jp.readValueAsTree();
            Object deserialized = null;
            boolean typeCoercion = ctxt.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS);
            int match = 0;
            JsonToken token = tree.traverse(jp.getCodec()).nextToken();
            // deserialize ActivityLogEntryStartMessage
            try {
                boolean attemptParsing = true;
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(ActivityLogEntryStartMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'ActivityLogEntryStartMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'ActivityLogEntryStartMessage'", e);
            }

            // deserialize ActivityLogEntryStopMessage
            try {
                boolean attemptParsing = true;
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(ActivityLogEntryStopMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'ActivityLogEntryStopMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'ActivityLogEntryStopMessage'", e);
            }

            // deserialize InboundKeepAliveMessage
            try {
                boolean attemptParsing = true;
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(InboundKeepAliveMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'InboundKeepAliveMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'InboundKeepAliveMessage'", e);
            }

            // deserialize ScheduledTasksInfoStartMessage
            try {
                boolean attemptParsing = true;
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(ScheduledTasksInfoStartMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'ScheduledTasksInfoStartMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'ScheduledTasksInfoStartMessage'", e);
            }

            // deserialize ScheduledTasksInfoStopMessage
            try {
                boolean attemptParsing = true;
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(ScheduledTasksInfoStopMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'ScheduledTasksInfoStopMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'ScheduledTasksInfoStopMessage'", e);
            }

            // deserialize SessionsStartMessage
            try {
                boolean attemptParsing = true;
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SessionsStartMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SessionsStartMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SessionsStartMessage'", e);
            }

            // deserialize SessionsStopMessage
            try {
                boolean attemptParsing = true;
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SessionsStopMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SessionsStopMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SessionsStopMessage'", e);
            }

            if (match == 1) {
                InboundWebSocketMessage ret = new InboundWebSocketMessage();
                ret.setActualInstance(deserialized);
                return ret;
            }
            throw new IOException(String.format(
                    "Failed deserialization for InboundWebSocketMessage: %d classes match result, expected 1", match));
        }

        /**
         * Handle deserialization of the 'null' value.
         */
        @Override
        public InboundWebSocketMessage getNullValue(DeserializationContext ctxt) throws JsonMappingException {
            throw new JsonMappingException(ctxt.getParser(), "InboundWebSocketMessage cannot be null");
        }
    }

    // store a list of schema names defined in oneOf
    public static final Map<String, GenericType<?>> schemas = new HashMap<>();

    public InboundWebSocketMessage() {
        super("oneOf", Boolean.FALSE);
    }

    public InboundWebSocketMessage(ActivityLogEntryStartMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public InboundWebSocketMessage(ActivityLogEntryStopMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public InboundWebSocketMessage(InboundKeepAliveMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public InboundWebSocketMessage(ScheduledTasksInfoStartMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public InboundWebSocketMessage(ScheduledTasksInfoStopMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public InboundWebSocketMessage(SessionsStartMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public InboundWebSocketMessage(SessionsStopMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    static {
        schemas.put("ActivityLogEntryStartMessage", new GenericType<ActivityLogEntryStartMessage>() {
        });
        schemas.put("ActivityLogEntryStopMessage", new GenericType<ActivityLogEntryStopMessage>() {
        });
        schemas.put("InboundKeepAliveMessage", new GenericType<InboundKeepAliveMessage>() {
        });
        schemas.put("ScheduledTasksInfoStartMessage", new GenericType<ScheduledTasksInfoStartMessage>() {
        });
        schemas.put("ScheduledTasksInfoStopMessage", new GenericType<ScheduledTasksInfoStopMessage>() {
        });
        schemas.put("SessionsStartMessage", new GenericType<SessionsStartMessage>() {
        });
        schemas.put("SessionsStopMessage", new GenericType<SessionsStopMessage>() {
        });
        JSON.registerDescendants(InboundWebSocketMessage.class, Collections.unmodifiableMap(schemas));
        // Initialize and register the discriminator mappings.
        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("ActivityLogEntryStart", ActivityLogEntryStartMessage.class);
        mappings.put("ActivityLogEntryStop", ActivityLogEntryStopMessage.class);
        mappings.put("KeepAlive", InboundKeepAliveMessage.class);
        mappings.put("ScheduledTasksInfoStart", ScheduledTasksInfoStartMessage.class);
        mappings.put("ScheduledTasksInfoStop", ScheduledTasksInfoStopMessage.class);
        mappings.put("SessionsStart", SessionsStartMessage.class);
        mappings.put("SessionsStop", SessionsStopMessage.class);
        mappings.put("ActivityLogEntryStartMessage", ActivityLogEntryStartMessage.class);
        mappings.put("ActivityLogEntryStopMessage", ActivityLogEntryStopMessage.class);
        mappings.put("InboundKeepAliveMessage", InboundKeepAliveMessage.class);
        mappings.put("ScheduledTasksInfoStartMessage", ScheduledTasksInfoStartMessage.class);
        mappings.put("ScheduledTasksInfoStopMessage", ScheduledTasksInfoStopMessage.class);
        mappings.put("SessionsStartMessage", SessionsStartMessage.class);
        mappings.put("SessionsStopMessage", SessionsStopMessage.class);
        mappings.put("InboundWebSocketMessage", InboundWebSocketMessage.class);
        JSON.registerDiscriminator(InboundWebSocketMessage.class, "MessageType", mappings);
    }

    @Override
    public Map<String, GenericType<?>> getSchemas() {
        return InboundWebSocketMessage.schemas;
    }

    /**
     * Set the instance that matches the oneOf child schema, check
     * the instance parameter is valid against the oneOf child schemas:
     * ActivityLogEntryStartMessage, ActivityLogEntryStopMessage, InboundKeepAliveMessage,
     * ScheduledTasksInfoStartMessage, ScheduledTasksInfoStopMessage, SessionsStartMessage, SessionsStopMessage
     *
     * It could be an instance of the 'oneOf' schemas.
     * The oneOf child schemas may themselves be a composed schema (allOf, anyOf, oneOf).
     */
    @Override
    public void setActualInstance(Object instance) {
        if (JSON.isInstanceOf(ActivityLogEntryStartMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(ActivityLogEntryStopMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(InboundKeepAliveMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(ScheduledTasksInfoStartMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(ScheduledTasksInfoStopMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SessionsStartMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SessionsStopMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        throw new RuntimeException(
                "Invalid instance type. Must be ActivityLogEntryStartMessage, ActivityLogEntryStopMessage, InboundKeepAliveMessage, ScheduledTasksInfoStartMessage, ScheduledTasksInfoStopMessage, SessionsStartMessage, SessionsStopMessage");
    }

    /**
     * Get the actual instance, which can be the following:
     * ActivityLogEntryStartMessage, ActivityLogEntryStopMessage, InboundKeepAliveMessage,
     * ScheduledTasksInfoStartMessage, ScheduledTasksInfoStopMessage, SessionsStartMessage, SessionsStopMessage
     *
     * @return The actual instance (ActivityLogEntryStartMessage, ActivityLogEntryStopMessage, InboundKeepAliveMessage,
     *         ScheduledTasksInfoStartMessage, ScheduledTasksInfoStopMessage, SessionsStartMessage, SessionsStopMessage)
     */
    @Override
    public Object getActualInstance() {
        return super.getActualInstance();
    }

    /**
     * Get the actual instance of `ActivityLogEntryStartMessage`. If the actual instance is not
     * `ActivityLogEntryStartMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `ActivityLogEntryStartMessage`
     * @throws ClassCastException if the instance is not `ActivityLogEntryStartMessage`
     */
    public ActivityLogEntryStartMessage getActivityLogEntryStartMessage() throws ClassCastException {
        return (ActivityLogEntryStartMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `ActivityLogEntryStopMessage`. If the actual instance is not
     * `ActivityLogEntryStopMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `ActivityLogEntryStopMessage`
     * @throws ClassCastException if the instance is not `ActivityLogEntryStopMessage`
     */
    public ActivityLogEntryStopMessage getActivityLogEntryStopMessage() throws ClassCastException {
        return (ActivityLogEntryStopMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `InboundKeepAliveMessage`. If the actual instance is not `InboundKeepAliveMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `InboundKeepAliveMessage`
     * @throws ClassCastException if the instance is not `InboundKeepAliveMessage`
     */
    public InboundKeepAliveMessage getInboundKeepAliveMessage() throws ClassCastException {
        return (InboundKeepAliveMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `ScheduledTasksInfoStartMessage`. If the actual instance is not
     * `ScheduledTasksInfoStartMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `ScheduledTasksInfoStartMessage`
     * @throws ClassCastException if the instance is not `ScheduledTasksInfoStartMessage`
     */
    public ScheduledTasksInfoStartMessage getScheduledTasksInfoStartMessage() throws ClassCastException {
        return (ScheduledTasksInfoStartMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `ScheduledTasksInfoStopMessage`. If the actual instance is not
     * `ScheduledTasksInfoStopMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `ScheduledTasksInfoStopMessage`
     * @throws ClassCastException if the instance is not `ScheduledTasksInfoStopMessage`
     */
    public ScheduledTasksInfoStopMessage getScheduledTasksInfoStopMessage() throws ClassCastException {
        return (ScheduledTasksInfoStopMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `SessionsStartMessage`. If the actual instance is not `SessionsStartMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SessionsStartMessage`
     * @throws ClassCastException if the instance is not `SessionsStartMessage`
     */
    public SessionsStartMessage getSessionsStartMessage() throws ClassCastException {
        return (SessionsStartMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `SessionsStopMessage`. If the actual instance is not `SessionsStopMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SessionsStopMessage`
     * @throws ClassCastException if the instance is not `SessionsStopMessage`
     */
    public SessionsStopMessage getSessionsStopMessage() throws ClassCastException {
        return (SessionsStopMessage) super.getActualInstance();
    }
}
