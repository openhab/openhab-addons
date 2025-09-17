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
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (GroupInfoDtoGroupUpdate.class.equals(Integer.class)
                        || GroupInfoDtoGroupUpdate.class.equals(Long.class)
                        || GroupInfoDtoGroupUpdate.class.equals(Float.class)
                        || GroupInfoDtoGroupUpdate.class.equals(Double.class)
                        || GroupInfoDtoGroupUpdate.class.equals(Boolean.class)
                        || GroupInfoDtoGroupUpdate.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((GroupInfoDtoGroupUpdate.class.equals(Integer.class)
                                || GroupInfoDtoGroupUpdate.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((GroupInfoDtoGroupUpdate.class.equals(Float.class)
                                || GroupInfoDtoGroupUpdate.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (GroupInfoDtoGroupUpdate.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (GroupInfoDtoGroupUpdate.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
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
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (GroupStateUpdateGroupUpdate.class.equals(Integer.class)
                        || GroupStateUpdateGroupUpdate.class.equals(Long.class)
                        || GroupStateUpdateGroupUpdate.class.equals(Float.class)
                        || GroupStateUpdateGroupUpdate.class.equals(Double.class)
                        || GroupStateUpdateGroupUpdate.class.equals(Boolean.class)
                        || GroupStateUpdateGroupUpdate.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((GroupStateUpdateGroupUpdate.class.equals(Integer.class)
                                || GroupStateUpdateGroupUpdate.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((GroupStateUpdateGroupUpdate.class.equals(Float.class)
                                || GroupStateUpdateGroupUpdate.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (GroupStateUpdateGroupUpdate.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (GroupStateUpdateGroupUpdate.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
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

            // deserialize PlayQueueUpdateGroupUpdate
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (PlayQueueUpdateGroupUpdate.class.equals(Integer.class)
                        || PlayQueueUpdateGroupUpdate.class.equals(Long.class)
                        || PlayQueueUpdateGroupUpdate.class.equals(Float.class)
                        || PlayQueueUpdateGroupUpdate.class.equals(Double.class)
                        || PlayQueueUpdateGroupUpdate.class.equals(Boolean.class)
                        || PlayQueueUpdateGroupUpdate.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((PlayQueueUpdateGroupUpdate.class.equals(Integer.class)
                                || PlayQueueUpdateGroupUpdate.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((PlayQueueUpdateGroupUpdate.class.equals(Float.class)
                                || PlayQueueUpdateGroupUpdate.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (PlayQueueUpdateGroupUpdate.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (PlayQueueUpdateGroupUpdate.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
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

            // deserialize StringGroupUpdate
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (StringGroupUpdate.class.equals(Integer.class) || StringGroupUpdate.class.equals(Long.class)
                        || StringGroupUpdate.class.equals(Float.class) || StringGroupUpdate.class.equals(Double.class)
                        || StringGroupUpdate.class.equals(Boolean.class)
                        || StringGroupUpdate.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((StringGroupUpdate.class.equals(Integer.class)
                                || StringGroupUpdate.class.equals(Long.class)) && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((StringGroupUpdate.class.equals(Float.class)
                                || StringGroupUpdate.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (StringGroupUpdate.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (StringGroupUpdate.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
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
    public static final Map<String, Class<?>> schemas = new HashMap<>();

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

    public GroupUpdate(PlayQueueUpdateGroupUpdate o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public GroupUpdate(StringGroupUpdate o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    static {
        schemas.put("GroupInfoDtoGroupUpdate", GroupInfoDtoGroupUpdate.class);
        schemas.put("GroupStateUpdateGroupUpdate", GroupStateUpdateGroupUpdate.class);
        schemas.put("PlayQueueUpdateGroupUpdate", PlayQueueUpdateGroupUpdate.class);
        schemas.put("StringGroupUpdate", StringGroupUpdate.class);
        JSON.registerDescendants(GroupUpdate.class, Collections.unmodifiableMap(schemas));
        // Initialize and register the discriminator mappings.
        Map<String, Class<?>> mappings = new HashMap<String, Class<?>>();
        mappings.put("GroupDoesNotExist", StringGroupUpdate.class);
        mappings.put("GroupJoined", GroupInfoDtoGroupUpdate.class);
        mappings.put("GroupLeft", StringGroupUpdate.class);
        mappings.put("LibraryAccessDenied", StringGroupUpdate.class);
        mappings.put("NotInGroup", StringGroupUpdate.class);
        mappings.put("PlayQueue", PlayQueueUpdateGroupUpdate.class);
        mappings.put("StateUpdate", GroupStateUpdateGroupUpdate.class);
        mappings.put("UserJoined", StringGroupUpdate.class);
        mappings.put("UserLeft", StringGroupUpdate.class);
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
     * GroupInfoDtoGroupUpdate, GroupStateUpdateGroupUpdate, PlayQueueUpdateGroupUpdate, StringGroupUpdate
     *
     * It could be an instance of the 'oneOf' schemas.
     * The oneOf child schemas may themselves be a composed schema (allOf, anyOf, oneOf).
     */
    @Override
    public void setActualInstance(Object instance) {
        if (JSON.isInstanceOf(GroupInfoDtoGroupUpdate.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(GroupStateUpdateGroupUpdate.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PlayQueueUpdateGroupUpdate.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(StringGroupUpdate.class, instance, new HashSet<Class<?>>())) {
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

        if (getActualInstance() instanceof GroupInfoDtoGroupUpdate) {
            if (getActualInstance() != null) {
                joiner.add(
                        ((GroupInfoDtoGroupUpdate) getActualInstance()).toUrlQueryString(prefix + "one_of_0" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof GroupStateUpdateGroupUpdate) {
            if (getActualInstance() != null) {
                joiner.add(((GroupStateUpdateGroupUpdate) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_1" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof StringGroupUpdate) {
            if (getActualInstance() != null) {
                joiner.add(((StringGroupUpdate) getActualInstance()).toUrlQueryString(prefix + "one_of_2" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof PlayQueueUpdateGroupUpdate) {
            if (getActualInstance() != null) {
                joiner.add(((PlayQueueUpdateGroupUpdate) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_3" + suffix));
            }
            return joiner.toString();
        }
        return null;
    }
}
