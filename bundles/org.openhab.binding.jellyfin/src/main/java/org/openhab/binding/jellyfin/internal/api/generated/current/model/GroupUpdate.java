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
            // deserialize GroupInfoDtoGroupUpdate
            try {
                boolean attemptParsing = true;
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(GroupInfoDtoGroupUpdate.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'GroupInfoDtoGroupUpdate'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'GroupInfoDtoGroupUpdate'", e);
            }

            // deserialize GroupStateUpdateGroupUpdate
            try {
                boolean attemptParsing = true;
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(GroupStateUpdateGroupUpdate.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'GroupStateUpdateGroupUpdate'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'GroupStateUpdateGroupUpdate'", e);
            }

            // deserialize StringGroupUpdate
            try {
                boolean attemptParsing = true;
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(StringGroupUpdate.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'StringGroupUpdate'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'StringGroupUpdate'", e);
            }

            // deserialize PlayQueueUpdateGroupUpdate
            try {
                boolean attemptParsing = true;
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(PlayQueueUpdateGroupUpdate.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'PlayQueueUpdateGroupUpdate'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'PlayQueueUpdateGroupUpdate'", e);
            }

            if (match == 1) {
                GroupUpdate ret = new GroupUpdate();
                ret.setActualInstance(deserialized);
                return ret;
            }
            throw new IOException(String
                    .format("Failed deserialization for GroupUpdate: %d classes match result, expected 1", match));
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
    public static final Map<String, GenericType<?>> schemas = new HashMap<>();

    public GroupUpdate() {
        super("oneOf", Boolean.FALSE);
    }

    public GroupUpdate(GroupInfoDtoGroupUpdate o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public GroupUpdate(GroupStateUpdateGroupUpdate o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public GroupUpdate(StringGroupUpdate o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public GroupUpdate(PlayQueueUpdateGroupUpdate o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    static {
        schemas.put("GroupInfoDtoGroupUpdate", new GenericType<GroupInfoDtoGroupUpdate>() {
        });
        schemas.put("GroupStateUpdateGroupUpdate", new GenericType<GroupStateUpdateGroupUpdate>() {
        });
        schemas.put("PlayQueueUpdateGroupUpdate", new GenericType<PlayQueueUpdateGroupUpdate>() {
        });
        schemas.put("StringGroupUpdate", new GenericType<StringGroupUpdate>() {
        });
        JSON.registerDescendants(GroupUpdate.class, Collections.unmodifiableMap(schemas));
        // Initialize and register the discriminator mappings.
        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("GroupDoesNotExist", StringGroupUpdate.class);
        mappings.put("GroupJoined", GroupInfoDtoGroupUpdate.class);
        mappings.put("GroupLeft", StringGroupUpdate.class);
        mappings.put("LibraryAccessDenied", StringGroupUpdate.class);
        mappings.put("NotInGroup", StringGroupUpdate.class);
        mappings.put("PlayQueue", PlayQueueUpdateGroupUpdate.class);
        mappings.put("StateUpdate", GroupStateUpdateGroupUpdate.class);
        mappings.put("UserJoined", StringGroupUpdate.class);
        mappings.put("UserLeft", StringGroupUpdate.class);
        mappings.put("GroupInfoDtoGroupUpdate", GroupInfoDtoGroupUpdate.class);
        mappings.put("GroupStateUpdateGroupUpdate", GroupStateUpdateGroupUpdate.class);
        mappings.put("PlayQueueUpdateGroupUpdate", PlayQueueUpdateGroupUpdate.class);
        mappings.put("StringGroupUpdate", StringGroupUpdate.class);
        mappings.put("GroupUpdate", GroupUpdate.class);
        JSON.registerDiscriminator(GroupUpdate.class, "Type", mappings);
    }

    @Override
    public Map<String, GenericType<?>> getSchemas() {
        return GroupUpdate.schemas;
    }

    /**
     * Set the instance that matches the oneOf child schema, check
     * the instance parameter is valid against the oneOf child schemas:
     * GroupInfoDtoGroupUpdate, GroupStateUpdateGroupUpdate, PlayQueueUpdateGroupUpdate, StringGroupUpdate
     *
     * It could be an instance of the 'oneOf' schemas.
     * The oneOf child schemas may themselves be a composed schema (allOf, anyOf, oneOf).
     */
    @Override
    public void setActualInstance(Object instance) {
        if (JSON.isInstanceOf(GroupInfoDtoGroupUpdate.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(GroupStateUpdateGroupUpdate.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(StringGroupUpdate.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PlayQueueUpdateGroupUpdate.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        throw new RuntimeException(
                "Invalid instance type. Must be GroupInfoDtoGroupUpdate, GroupStateUpdateGroupUpdate, PlayQueueUpdateGroupUpdate, StringGroupUpdate");
    }

    /**
     * Get the actual instance, which can be the following:
     * GroupInfoDtoGroupUpdate, GroupStateUpdateGroupUpdate, PlayQueueUpdateGroupUpdate, StringGroupUpdate
     *
     * @return The actual instance (GroupInfoDtoGroupUpdate, GroupStateUpdateGroupUpdate, PlayQueueUpdateGroupUpdate,
     *         StringGroupUpdate)
     */
    @Override
    public Object getActualInstance() {
        return super.getActualInstance();
    }

    /**
     * Get the actual instance of `GroupInfoDtoGroupUpdate`. If the actual instance is not `GroupInfoDtoGroupUpdate`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `GroupInfoDtoGroupUpdate`
     * @throws ClassCastException if the instance is not `GroupInfoDtoGroupUpdate`
     */
    public GroupInfoDtoGroupUpdate getGroupInfoDtoGroupUpdate() throws ClassCastException {
        return (GroupInfoDtoGroupUpdate) super.getActualInstance();
    }

    /**
     * Get the actual instance of `GroupStateUpdateGroupUpdate`. If the actual instance is not
     * `GroupStateUpdateGroupUpdate`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `GroupStateUpdateGroupUpdate`
     * @throws ClassCastException if the instance is not `GroupStateUpdateGroupUpdate`
     */
    public GroupStateUpdateGroupUpdate getGroupStateUpdateGroupUpdate() throws ClassCastException {
        return (GroupStateUpdateGroupUpdate) super.getActualInstance();
    }

    /**
     * Get the actual instance of `StringGroupUpdate`. If the actual instance is not `StringGroupUpdate`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `StringGroupUpdate`
     * @throws ClassCastException if the instance is not `StringGroupUpdate`
     */
    public StringGroupUpdate getStringGroupUpdate() throws ClassCastException {
        return (StringGroupUpdate) super.getActualInstance();
    }

    /**
     * Get the actual instance of `PlayQueueUpdateGroupUpdate`. If the actual instance is not
     * `PlayQueueUpdateGroupUpdate`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `PlayQueueUpdateGroupUpdate`
     * @throws ClassCastException if the instance is not `PlayQueueUpdateGroupUpdate`
     */
    public PlayQueueUpdateGroupUpdate getPlayQueueUpdateGroupUpdate() throws ClassCastException {
        return (PlayQueueUpdateGroupUpdate) super.getActualInstance();
    }
}
