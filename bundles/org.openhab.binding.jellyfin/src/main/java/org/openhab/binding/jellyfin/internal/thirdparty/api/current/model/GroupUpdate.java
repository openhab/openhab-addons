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

package org.openhab.binding.jellyfin.internal.thirdparty.api.current.model;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openhab.binding.jellyfin.internal.thirdparty.api.JSON;

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
@JsonDeserialize(using = GroupUpdate.GroupUpdateDeserializer.class)
@JsonSerialize(using = GroupUpdate.GroupUpdateSerializer.class)
public class GroupUpdate extends AbstractOpenApiSchema {
    private static final Logger log = Logger.getLogger(GroupUpdate.class.getName());

    public static class GroupUpdateSerializer extends StdSerializer<GroupUpdate> {
        public GroupUpdateSerializer(Class<GroupUpdate> t) {
            super(t);
        }

        public GroupUpdateSerializer() {
            this(null);
        }

        @Override
        public void serialize(GroupUpdate value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonProcessingException {
            jgen.writeObject(value.getActualInstance());
        }
    }

    public static class GroupUpdateDeserializer extends StdDeserializer<GroupUpdate> {
        public GroupUpdateDeserializer() {
            this(GroupUpdate.class);
        }

        public GroupUpdateDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public GroupUpdate deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            JsonNode tree = jp.readValueAsTree();
            Object deserialized = null;
            boolean typeCoercion = ctxt.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS);
            int match = 0;
            JsonToken token = tree.traverse(jp.getCodec()).nextToken();
            // deserialize SyncPlayGroupDoesNotExistUpdate
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (SyncPlayGroupDoesNotExistUpdate.class.equals(Integer.class)
                        || SyncPlayGroupDoesNotExistUpdate.class.equals(Long.class)
                        || SyncPlayGroupDoesNotExistUpdate.class.equals(Float.class)
                        || SyncPlayGroupDoesNotExistUpdate.class.equals(Double.class)
                        || SyncPlayGroupDoesNotExistUpdate.class.equals(Boolean.class)
                        || SyncPlayGroupDoesNotExistUpdate.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((SyncPlayGroupDoesNotExistUpdate.class.equals(Integer.class)
                                || SyncPlayGroupDoesNotExistUpdate.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((SyncPlayGroupDoesNotExistUpdate.class.equals(Float.class)
                                || SyncPlayGroupDoesNotExistUpdate.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (SyncPlayGroupDoesNotExistUpdate.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (SyncPlayGroupDoesNotExistUpdate.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SyncPlayGroupDoesNotExistUpdate.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SyncPlayGroupDoesNotExistUpdate'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SyncPlayGroupDoesNotExistUpdate'", e);
            }

            // deserialize SyncPlayGroupJoinedUpdate
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (SyncPlayGroupJoinedUpdate.class.equals(Integer.class)
                        || SyncPlayGroupJoinedUpdate.class.equals(Long.class)
                        || SyncPlayGroupJoinedUpdate.class.equals(Float.class)
                        || SyncPlayGroupJoinedUpdate.class.equals(Double.class)
                        || SyncPlayGroupJoinedUpdate.class.equals(Boolean.class)
                        || SyncPlayGroupJoinedUpdate.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((SyncPlayGroupJoinedUpdate.class.equals(Integer.class)
                                || SyncPlayGroupJoinedUpdate.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((SyncPlayGroupJoinedUpdate.class.equals(Float.class)
                                || SyncPlayGroupJoinedUpdate.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (SyncPlayGroupJoinedUpdate.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (SyncPlayGroupJoinedUpdate.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SyncPlayGroupJoinedUpdate.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SyncPlayGroupJoinedUpdate'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SyncPlayGroupJoinedUpdate'", e);
            }

            // deserialize SyncPlayGroupLeftUpdate
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (SyncPlayGroupLeftUpdate.class.equals(Integer.class)
                        || SyncPlayGroupLeftUpdate.class.equals(Long.class)
                        || SyncPlayGroupLeftUpdate.class.equals(Float.class)
                        || SyncPlayGroupLeftUpdate.class.equals(Double.class)
                        || SyncPlayGroupLeftUpdate.class.equals(Boolean.class)
                        || SyncPlayGroupLeftUpdate.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((SyncPlayGroupLeftUpdate.class.equals(Integer.class)
                                || SyncPlayGroupLeftUpdate.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((SyncPlayGroupLeftUpdate.class.equals(Float.class)
                                || SyncPlayGroupLeftUpdate.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (SyncPlayGroupLeftUpdate.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (SyncPlayGroupLeftUpdate.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SyncPlayGroupLeftUpdate.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SyncPlayGroupLeftUpdate'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SyncPlayGroupLeftUpdate'", e);
            }

            // deserialize SyncPlayLibraryAccessDeniedUpdate
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (SyncPlayLibraryAccessDeniedUpdate.class.equals(Integer.class)
                        || SyncPlayLibraryAccessDeniedUpdate.class.equals(Long.class)
                        || SyncPlayLibraryAccessDeniedUpdate.class.equals(Float.class)
                        || SyncPlayLibraryAccessDeniedUpdate.class.equals(Double.class)
                        || SyncPlayLibraryAccessDeniedUpdate.class.equals(Boolean.class)
                        || SyncPlayLibraryAccessDeniedUpdate.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((SyncPlayLibraryAccessDeniedUpdate.class.equals(Integer.class)
                                || SyncPlayLibraryAccessDeniedUpdate.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((SyncPlayLibraryAccessDeniedUpdate.class.equals(Float.class)
                                || SyncPlayLibraryAccessDeniedUpdate.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (SyncPlayLibraryAccessDeniedUpdate.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (SyncPlayLibraryAccessDeniedUpdate.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SyncPlayLibraryAccessDeniedUpdate.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SyncPlayLibraryAccessDeniedUpdate'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SyncPlayLibraryAccessDeniedUpdate'", e);
            }

            // deserialize SyncPlayNotInGroupUpdate
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (SyncPlayNotInGroupUpdate.class.equals(Integer.class)
                        || SyncPlayNotInGroupUpdate.class.equals(Long.class)
                        || SyncPlayNotInGroupUpdate.class.equals(Float.class)
                        || SyncPlayNotInGroupUpdate.class.equals(Double.class)
                        || SyncPlayNotInGroupUpdate.class.equals(Boolean.class)
                        || SyncPlayNotInGroupUpdate.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((SyncPlayNotInGroupUpdate.class.equals(Integer.class)
                                || SyncPlayNotInGroupUpdate.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((SyncPlayNotInGroupUpdate.class.equals(Float.class)
                                || SyncPlayNotInGroupUpdate.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (SyncPlayNotInGroupUpdate.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (SyncPlayNotInGroupUpdate.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SyncPlayNotInGroupUpdate.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SyncPlayNotInGroupUpdate'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SyncPlayNotInGroupUpdate'", e);
            }

            // deserialize SyncPlayPlayQueueUpdate
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (SyncPlayPlayQueueUpdate.class.equals(Integer.class)
                        || SyncPlayPlayQueueUpdate.class.equals(Long.class)
                        || SyncPlayPlayQueueUpdate.class.equals(Float.class)
                        || SyncPlayPlayQueueUpdate.class.equals(Double.class)
                        || SyncPlayPlayQueueUpdate.class.equals(Boolean.class)
                        || SyncPlayPlayQueueUpdate.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((SyncPlayPlayQueueUpdate.class.equals(Integer.class)
                                || SyncPlayPlayQueueUpdate.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((SyncPlayPlayQueueUpdate.class.equals(Float.class)
                                || SyncPlayPlayQueueUpdate.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (SyncPlayPlayQueueUpdate.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (SyncPlayPlayQueueUpdate.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SyncPlayPlayQueueUpdate.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SyncPlayPlayQueueUpdate'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SyncPlayPlayQueueUpdate'", e);
            }

            // deserialize SyncPlayStateUpdate
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (SyncPlayStateUpdate.class.equals(Integer.class) || SyncPlayStateUpdate.class.equals(Long.class)
                        || SyncPlayStateUpdate.class.equals(Float.class)
                        || SyncPlayStateUpdate.class.equals(Double.class)
                        || SyncPlayStateUpdate.class.equals(Boolean.class)
                        || SyncPlayStateUpdate.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((SyncPlayStateUpdate.class.equals(Integer.class)
                                || SyncPlayStateUpdate.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((SyncPlayStateUpdate.class.equals(Float.class)
                                || SyncPlayStateUpdate.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (SyncPlayStateUpdate.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (SyncPlayStateUpdate.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SyncPlayStateUpdate.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SyncPlayStateUpdate'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SyncPlayStateUpdate'", e);
            }

            // deserialize SyncPlayUserJoinedUpdate
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (SyncPlayUserJoinedUpdate.class.equals(Integer.class)
                        || SyncPlayUserJoinedUpdate.class.equals(Long.class)
                        || SyncPlayUserJoinedUpdate.class.equals(Float.class)
                        || SyncPlayUserJoinedUpdate.class.equals(Double.class)
                        || SyncPlayUserJoinedUpdate.class.equals(Boolean.class)
                        || SyncPlayUserJoinedUpdate.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((SyncPlayUserJoinedUpdate.class.equals(Integer.class)
                                || SyncPlayUserJoinedUpdate.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((SyncPlayUserJoinedUpdate.class.equals(Float.class)
                                || SyncPlayUserJoinedUpdate.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (SyncPlayUserJoinedUpdate.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (SyncPlayUserJoinedUpdate.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SyncPlayUserJoinedUpdate.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SyncPlayUserJoinedUpdate'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SyncPlayUserJoinedUpdate'", e);
            }

            // deserialize SyncPlayUserLeftUpdate
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (SyncPlayUserLeftUpdate.class.equals(Integer.class)
                        || SyncPlayUserLeftUpdate.class.equals(Long.class)
                        || SyncPlayUserLeftUpdate.class.equals(Float.class)
                        || SyncPlayUserLeftUpdate.class.equals(Double.class)
                        || SyncPlayUserLeftUpdate.class.equals(Boolean.class)
                        || SyncPlayUserLeftUpdate.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((SyncPlayUserLeftUpdate.class.equals(Integer.class)
                                || SyncPlayUserLeftUpdate.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((SyncPlayUserLeftUpdate.class.equals(Float.class)
                                || SyncPlayUserLeftUpdate.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (SyncPlayUserLeftUpdate.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (SyncPlayUserLeftUpdate.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SyncPlayUserLeftUpdate.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SyncPlayUserLeftUpdate'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SyncPlayUserLeftUpdate'", e);
            }

            if (match == 1) {
                GroupUpdate ret = new GroupUpdate();
                ret.setActualInstance(deserialized);
                return ret;
            }
            throw new IOException(String.format(java.util.Locale.ROOT,
                    "Failed deserialization for GroupUpdate: %d classes match result, expected 1", match));
        }

        /**
         * Handle deserialization of the 'null' value.
         */
        @Override
        public GroupUpdate getNullValue(DeserializationContext ctxt) throws JsonMappingException {
            throw new JsonMappingException(ctxt.getParser(), "GroupUpdate cannot be null");
        }
    }

    // store a list of schema names defined in oneOf
    public static final Map<String, Class<?>> schemas = new HashMap<>();

    public GroupUpdate() {
        super("oneOf", Boolean.FALSE);
    }

    public GroupUpdate(SyncPlayGroupDoesNotExistUpdate o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public GroupUpdate(SyncPlayGroupJoinedUpdate o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public GroupUpdate(SyncPlayGroupLeftUpdate o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public GroupUpdate(SyncPlayLibraryAccessDeniedUpdate o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public GroupUpdate(SyncPlayNotInGroupUpdate o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public GroupUpdate(SyncPlayPlayQueueUpdate o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public GroupUpdate(SyncPlayStateUpdate o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public GroupUpdate(SyncPlayUserJoinedUpdate o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public GroupUpdate(SyncPlayUserLeftUpdate o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    static {
        schemas.put("SyncPlayGroupDoesNotExistUpdate", SyncPlayGroupDoesNotExistUpdate.class);
        schemas.put("SyncPlayGroupJoinedUpdate", SyncPlayGroupJoinedUpdate.class);
        schemas.put("SyncPlayGroupLeftUpdate", SyncPlayGroupLeftUpdate.class);
        schemas.put("SyncPlayLibraryAccessDeniedUpdate", SyncPlayLibraryAccessDeniedUpdate.class);
        schemas.put("SyncPlayNotInGroupUpdate", SyncPlayNotInGroupUpdate.class);
        schemas.put("SyncPlayPlayQueueUpdate", SyncPlayPlayQueueUpdate.class);
        schemas.put("SyncPlayStateUpdate", SyncPlayStateUpdate.class);
        schemas.put("SyncPlayUserJoinedUpdate", SyncPlayUserJoinedUpdate.class);
        schemas.put("SyncPlayUserLeftUpdate", SyncPlayUserLeftUpdate.class);
        JSON.registerDescendants(GroupUpdate.class, Collections.unmodifiableMap(schemas));
        // Initialize and register the discriminator mappings.
        Map<String, Class<?>> mappings = new HashMap<String, Class<?>>();
        mappings.put("GroupDoesNotExist", SyncPlayGroupDoesNotExistUpdate.class);
        mappings.put("GroupJoined", SyncPlayGroupJoinedUpdate.class);
        mappings.put("GroupLeft", SyncPlayGroupLeftUpdate.class);
        mappings.put("LibraryAccessDenied", SyncPlayLibraryAccessDeniedUpdate.class);
        mappings.put("NotInGroup", SyncPlayNotInGroupUpdate.class);
        mappings.put("PlayQueue", SyncPlayPlayQueueUpdate.class);
        mappings.put("StateUpdate", SyncPlayStateUpdate.class);
        mappings.put("UserJoined", SyncPlayUserJoinedUpdate.class);
        mappings.put("UserLeft", SyncPlayUserLeftUpdate.class);
        mappings.put("GroupUpdate", GroupUpdate.class);
        JSON.registerDiscriminator(GroupUpdate.class, "Type", mappings);
    }

    @Override
    public Map<String, Class<?>> getSchemas() {
        return GroupUpdate.schemas;
    }

    /**
     * Set the instance that matches the oneOf child schema, check
     * the instance parameter is valid against the oneOf child schemas:
     * SyncPlayGroupDoesNotExistUpdate, SyncPlayGroupJoinedUpdate, SyncPlayGroupLeftUpdate,
     * SyncPlayLibraryAccessDeniedUpdate, SyncPlayNotInGroupUpdate, SyncPlayPlayQueueUpdate, SyncPlayStateUpdate,
     * SyncPlayUserJoinedUpdate, SyncPlayUserLeftUpdate
     *
     * It could be an instance of the 'oneOf' schemas.
     * The oneOf child schemas may themselves be a composed schema (allOf, anyOf, oneOf).
     */
    @Override
    public void setActualInstance(Object instance) {
        if (JSON.isInstanceOf(SyncPlayGroupDoesNotExistUpdate.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SyncPlayGroupJoinedUpdate.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SyncPlayGroupLeftUpdate.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SyncPlayLibraryAccessDeniedUpdate.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SyncPlayNotInGroupUpdate.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SyncPlayPlayQueueUpdate.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SyncPlayStateUpdate.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SyncPlayUserJoinedUpdate.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SyncPlayUserLeftUpdate.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        throw new RuntimeException(
                "Invalid instance type. Must be SyncPlayGroupDoesNotExistUpdate, SyncPlayGroupJoinedUpdate, SyncPlayGroupLeftUpdate, SyncPlayLibraryAccessDeniedUpdate, SyncPlayNotInGroupUpdate, SyncPlayPlayQueueUpdate, SyncPlayStateUpdate, SyncPlayUserJoinedUpdate, SyncPlayUserLeftUpdate");
    }

    /**
     * Get the actual instance, which can be the following:
     * SyncPlayGroupDoesNotExistUpdate, SyncPlayGroupJoinedUpdate, SyncPlayGroupLeftUpdate,
     * SyncPlayLibraryAccessDeniedUpdate, SyncPlayNotInGroupUpdate, SyncPlayPlayQueueUpdate, SyncPlayStateUpdate,
     * SyncPlayUserJoinedUpdate, SyncPlayUserLeftUpdate
     *
     * @return The actual instance (SyncPlayGroupDoesNotExistUpdate, SyncPlayGroupJoinedUpdate, SyncPlayGroupLeftUpdate,
     *         SyncPlayLibraryAccessDeniedUpdate, SyncPlayNotInGroupUpdate, SyncPlayPlayQueueUpdate,
     *         SyncPlayStateUpdate, SyncPlayUserJoinedUpdate, SyncPlayUserLeftUpdate)
     */
    @Override
    public Object getActualInstance() {
        return super.getActualInstance();
    }

    /**
     * Get the actual instance of `SyncPlayGroupDoesNotExistUpdate`. If the actual instance is not
     * `SyncPlayGroupDoesNotExistUpdate`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SyncPlayGroupDoesNotExistUpdate`
     * @throws ClassCastException if the instance is not `SyncPlayGroupDoesNotExistUpdate`
     */
    public SyncPlayGroupDoesNotExistUpdate getSyncPlayGroupDoesNotExistUpdate() throws ClassCastException {
        return (SyncPlayGroupDoesNotExistUpdate) super.getActualInstance();
    }

    /**
     * Get the actual instance of `SyncPlayGroupJoinedUpdate`. If the actual instance is not
     * `SyncPlayGroupJoinedUpdate`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SyncPlayGroupJoinedUpdate`
     * @throws ClassCastException if the instance is not `SyncPlayGroupJoinedUpdate`
     */
    public SyncPlayGroupJoinedUpdate getSyncPlayGroupJoinedUpdate() throws ClassCastException {
        return (SyncPlayGroupJoinedUpdate) super.getActualInstance();
    }

    /**
     * Get the actual instance of `SyncPlayGroupLeftUpdate`. If the actual instance is not `SyncPlayGroupLeftUpdate`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SyncPlayGroupLeftUpdate`
     * @throws ClassCastException if the instance is not `SyncPlayGroupLeftUpdate`
     */
    public SyncPlayGroupLeftUpdate getSyncPlayGroupLeftUpdate() throws ClassCastException {
        return (SyncPlayGroupLeftUpdate) super.getActualInstance();
    }

    /**
     * Get the actual instance of `SyncPlayLibraryAccessDeniedUpdate`. If the actual instance is not
     * `SyncPlayLibraryAccessDeniedUpdate`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SyncPlayLibraryAccessDeniedUpdate`
     * @throws ClassCastException if the instance is not `SyncPlayLibraryAccessDeniedUpdate`
     */
    public SyncPlayLibraryAccessDeniedUpdate getSyncPlayLibraryAccessDeniedUpdate() throws ClassCastException {
        return (SyncPlayLibraryAccessDeniedUpdate) super.getActualInstance();
    }

    /**
     * Get the actual instance of `SyncPlayNotInGroupUpdate`. If the actual instance is not `SyncPlayNotInGroupUpdate`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SyncPlayNotInGroupUpdate`
     * @throws ClassCastException if the instance is not `SyncPlayNotInGroupUpdate`
     */
    public SyncPlayNotInGroupUpdate getSyncPlayNotInGroupUpdate() throws ClassCastException {
        return (SyncPlayNotInGroupUpdate) super.getActualInstance();
    }

    /**
     * Get the actual instance of `SyncPlayPlayQueueUpdate`. If the actual instance is not `SyncPlayPlayQueueUpdate`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SyncPlayPlayQueueUpdate`
     * @throws ClassCastException if the instance is not `SyncPlayPlayQueueUpdate`
     */
    public SyncPlayPlayQueueUpdate getSyncPlayPlayQueueUpdate() throws ClassCastException {
        return (SyncPlayPlayQueueUpdate) super.getActualInstance();
    }

    /**
     * Get the actual instance of `SyncPlayStateUpdate`. If the actual instance is not `SyncPlayStateUpdate`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SyncPlayStateUpdate`
     * @throws ClassCastException if the instance is not `SyncPlayStateUpdate`
     */
    public SyncPlayStateUpdate getSyncPlayStateUpdate() throws ClassCastException {
        return (SyncPlayStateUpdate) super.getActualInstance();
    }

    /**
     * Get the actual instance of `SyncPlayUserJoinedUpdate`. If the actual instance is not `SyncPlayUserJoinedUpdate`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SyncPlayUserJoinedUpdate`
     * @throws ClassCastException if the instance is not `SyncPlayUserJoinedUpdate`
     */
    public SyncPlayUserJoinedUpdate getSyncPlayUserJoinedUpdate() throws ClassCastException {
        return (SyncPlayUserJoinedUpdate) super.getActualInstance();
    }

    /**
     * Get the actual instance of `SyncPlayUserLeftUpdate`. If the actual instance is not `SyncPlayUserLeftUpdate`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SyncPlayUserLeftUpdate`
     * @throws ClassCastException if the instance is not `SyncPlayUserLeftUpdate`
     */
    public SyncPlayUserLeftUpdate getSyncPlayUserLeftUpdate() throws ClassCastException {
        return (SyncPlayUserLeftUpdate) super.getActualInstance();
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

        if (getActualInstance() instanceof SyncPlayGroupDoesNotExistUpdate) {
            if (getActualInstance() != null) {
                joiner.add(((SyncPlayGroupDoesNotExistUpdate) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_0" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof SyncPlayGroupJoinedUpdate) {
            if (getActualInstance() != null) {
                joiner.add(((SyncPlayGroupJoinedUpdate) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_1" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof SyncPlayGroupLeftUpdate) {
            if (getActualInstance() != null) {
                joiner.add(
                        ((SyncPlayGroupLeftUpdate) getActualInstance()).toUrlQueryString(prefix + "one_of_2" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof SyncPlayLibraryAccessDeniedUpdate) {
            if (getActualInstance() != null) {
                joiner.add(((SyncPlayLibraryAccessDeniedUpdate) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_3" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof SyncPlayNotInGroupUpdate) {
            if (getActualInstance() != null) {
                joiner.add(((SyncPlayNotInGroupUpdate) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_4" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof SyncPlayPlayQueueUpdate) {
            if (getActualInstance() != null) {
                joiner.add(
                        ((SyncPlayPlayQueueUpdate) getActualInstance()).toUrlQueryString(prefix + "one_of_5" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof SyncPlayStateUpdate) {
            if (getActualInstance() != null) {
                joiner.add(((SyncPlayStateUpdate) getActualInstance()).toUrlQueryString(prefix + "one_of_6" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof SyncPlayUserJoinedUpdate) {
            if (getActualInstance() != null) {
                joiner.add(((SyncPlayUserJoinedUpdate) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_7" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof SyncPlayUserLeftUpdate) {
            if (getActualInstance() != null) {
                joiner.add(
                        ((SyncPlayUserLeftUpdate) getActualInstance()).toUrlQueryString(prefix + "one_of_8" + suffix));
            }
            return joiner.toString();
        }
        return null;
    }
}
