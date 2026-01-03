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
@JsonDeserialize(using = OutboundWebSocketMessage.OutboundWebSocketMessageDeserializer.class)
@JsonSerialize(using = OutboundWebSocketMessage.OutboundWebSocketMessageSerializer.class)
public class OutboundWebSocketMessage extends AbstractOpenApiSchema {
    private static final Logger log = Logger.getLogger(OutboundWebSocketMessage.class.getName());

    public static class OutboundWebSocketMessageSerializer extends StdSerializer<OutboundWebSocketMessage> {
        public OutboundWebSocketMessageSerializer(Class<OutboundWebSocketMessage> t) {
            super(t);
        }

        public OutboundWebSocketMessageSerializer() {
            this(null);
        }

        @Override
        public void serialize(OutboundWebSocketMessage value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonProcessingException {
            jgen.writeObject(value.getActualInstance());
        }
    }

    public static class OutboundWebSocketMessageDeserializer extends StdDeserializer<OutboundWebSocketMessage> {
        public OutboundWebSocketMessageDeserializer() {
            this(OutboundWebSocketMessage.class);
        }

        public OutboundWebSocketMessageDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public OutboundWebSocketMessage deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            JsonNode tree = jp.readValueAsTree();
            Object deserialized = null;
            boolean typeCoercion = ctxt.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS);
            int match = 0;
            JsonToken token = tree.traverse(jp.getCodec()).nextToken();
            // deserialize ActivityLogEntryMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (ActivityLogEntryMessage.class.equals(Integer.class)
                        || ActivityLogEntryMessage.class.equals(Long.class)
                        || ActivityLogEntryMessage.class.equals(Float.class)
                        || ActivityLogEntryMessage.class.equals(Double.class)
                        || ActivityLogEntryMessage.class.equals(Boolean.class)
                        || ActivityLogEntryMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((ActivityLogEntryMessage.class.equals(Integer.class)
                                || ActivityLogEntryMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((ActivityLogEntryMessage.class.equals(Float.class)
                                || ActivityLogEntryMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (ActivityLogEntryMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (ActivityLogEntryMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(ActivityLogEntryMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'ActivityLogEntryMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'ActivityLogEntryMessage'", e);
            }

            // deserialize ForceKeepAliveMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (ForceKeepAliveMessage.class.equals(Integer.class) || ForceKeepAliveMessage.class.equals(Long.class)
                        || ForceKeepAliveMessage.class.equals(Float.class)
                        || ForceKeepAliveMessage.class.equals(Double.class)
                        || ForceKeepAliveMessage.class.equals(Boolean.class)
                        || ForceKeepAliveMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((ForceKeepAliveMessage.class.equals(Integer.class)
                                || ForceKeepAliveMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((ForceKeepAliveMessage.class.equals(Float.class)
                                || ForceKeepAliveMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (ForceKeepAliveMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (ForceKeepAliveMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(ForceKeepAliveMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'ForceKeepAliveMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'ForceKeepAliveMessage'", e);
            }

            // deserialize GeneralCommandMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (GeneralCommandMessage.class.equals(Integer.class) || GeneralCommandMessage.class.equals(Long.class)
                        || GeneralCommandMessage.class.equals(Float.class)
                        || GeneralCommandMessage.class.equals(Double.class)
                        || GeneralCommandMessage.class.equals(Boolean.class)
                        || GeneralCommandMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((GeneralCommandMessage.class.equals(Integer.class)
                                || GeneralCommandMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((GeneralCommandMessage.class.equals(Float.class)
                                || GeneralCommandMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (GeneralCommandMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (GeneralCommandMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(GeneralCommandMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'GeneralCommandMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'GeneralCommandMessage'", e);
            }

            // deserialize LibraryChangedMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (LibraryChangedMessage.class.equals(Integer.class) || LibraryChangedMessage.class.equals(Long.class)
                        || LibraryChangedMessage.class.equals(Float.class)
                        || LibraryChangedMessage.class.equals(Double.class)
                        || LibraryChangedMessage.class.equals(Boolean.class)
                        || LibraryChangedMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((LibraryChangedMessage.class.equals(Integer.class)
                                || LibraryChangedMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((LibraryChangedMessage.class.equals(Float.class)
                                || LibraryChangedMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (LibraryChangedMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (LibraryChangedMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(LibraryChangedMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'LibraryChangedMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'LibraryChangedMessage'", e);
            }

            // deserialize OutboundKeepAliveMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (OutboundKeepAliveMessage.class.equals(Integer.class)
                        || OutboundKeepAliveMessage.class.equals(Long.class)
                        || OutboundKeepAliveMessage.class.equals(Float.class)
                        || OutboundKeepAliveMessage.class.equals(Double.class)
                        || OutboundKeepAliveMessage.class.equals(Boolean.class)
                        || OutboundKeepAliveMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((OutboundKeepAliveMessage.class.equals(Integer.class)
                                || OutboundKeepAliveMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((OutboundKeepAliveMessage.class.equals(Float.class)
                                || OutboundKeepAliveMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (OutboundKeepAliveMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (OutboundKeepAliveMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(OutboundKeepAliveMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'OutboundKeepAliveMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'OutboundKeepAliveMessage'", e);
            }

            // deserialize PlayMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (PlayMessage.class.equals(Integer.class) || PlayMessage.class.equals(Long.class)
                        || PlayMessage.class.equals(Float.class) || PlayMessage.class.equals(Double.class)
                        || PlayMessage.class.equals(Boolean.class) || PlayMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((PlayMessage.class.equals(Integer.class)
                                || PlayMessage.class.equals(Long.class)) && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((PlayMessage.class.equals(Float.class)
                                || PlayMessage.class.equals(Double.class)) && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (PlayMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (PlayMessage.class.equals(String.class) && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(PlayMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'PlayMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'PlayMessage'", e);
            }

            // deserialize PlaystateMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (PlaystateMessage.class.equals(Integer.class) || PlaystateMessage.class.equals(Long.class)
                        || PlaystateMessage.class.equals(Float.class) || PlaystateMessage.class.equals(Double.class)
                        || PlaystateMessage.class.equals(Boolean.class)
                        || PlaystateMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((PlaystateMessage.class.equals(Integer.class)
                                || PlaystateMessage.class.equals(Long.class)) && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((PlaystateMessage.class.equals(Float.class)
                                || PlaystateMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (PlaystateMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (PlaystateMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(PlaystateMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'PlaystateMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'PlaystateMessage'", e);
            }

            // deserialize PluginInstallationCancelledMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (PluginInstallationCancelledMessage.class.equals(Integer.class)
                        || PluginInstallationCancelledMessage.class.equals(Long.class)
                        || PluginInstallationCancelledMessage.class.equals(Float.class)
                        || PluginInstallationCancelledMessage.class.equals(Double.class)
                        || PluginInstallationCancelledMessage.class.equals(Boolean.class)
                        || PluginInstallationCancelledMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((PluginInstallationCancelledMessage.class.equals(Integer.class)
                                || PluginInstallationCancelledMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((PluginInstallationCancelledMessage.class.equals(Float.class)
                                || PluginInstallationCancelledMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (PluginInstallationCancelledMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (PluginInstallationCancelledMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(PluginInstallationCancelledMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'PluginInstallationCancelledMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'PluginInstallationCancelledMessage'", e);
            }

            // deserialize PluginInstallationCompletedMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (PluginInstallationCompletedMessage.class.equals(Integer.class)
                        || PluginInstallationCompletedMessage.class.equals(Long.class)
                        || PluginInstallationCompletedMessage.class.equals(Float.class)
                        || PluginInstallationCompletedMessage.class.equals(Double.class)
                        || PluginInstallationCompletedMessage.class.equals(Boolean.class)
                        || PluginInstallationCompletedMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((PluginInstallationCompletedMessage.class.equals(Integer.class)
                                || PluginInstallationCompletedMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((PluginInstallationCompletedMessage.class.equals(Float.class)
                                || PluginInstallationCompletedMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (PluginInstallationCompletedMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (PluginInstallationCompletedMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(PluginInstallationCompletedMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'PluginInstallationCompletedMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'PluginInstallationCompletedMessage'", e);
            }

            // deserialize PluginInstallationFailedMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (PluginInstallationFailedMessage.class.equals(Integer.class)
                        || PluginInstallationFailedMessage.class.equals(Long.class)
                        || PluginInstallationFailedMessage.class.equals(Float.class)
                        || PluginInstallationFailedMessage.class.equals(Double.class)
                        || PluginInstallationFailedMessage.class.equals(Boolean.class)
                        || PluginInstallationFailedMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((PluginInstallationFailedMessage.class.equals(Integer.class)
                                || PluginInstallationFailedMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((PluginInstallationFailedMessage.class.equals(Float.class)
                                || PluginInstallationFailedMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (PluginInstallationFailedMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (PluginInstallationFailedMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(PluginInstallationFailedMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'PluginInstallationFailedMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'PluginInstallationFailedMessage'", e);
            }

            // deserialize PluginInstallingMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (PluginInstallingMessage.class.equals(Integer.class)
                        || PluginInstallingMessage.class.equals(Long.class)
                        || PluginInstallingMessage.class.equals(Float.class)
                        || PluginInstallingMessage.class.equals(Double.class)
                        || PluginInstallingMessage.class.equals(Boolean.class)
                        || PluginInstallingMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((PluginInstallingMessage.class.equals(Integer.class)
                                || PluginInstallingMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((PluginInstallingMessage.class.equals(Float.class)
                                || PluginInstallingMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (PluginInstallingMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (PluginInstallingMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(PluginInstallingMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'PluginInstallingMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'PluginInstallingMessage'", e);
            }

            // deserialize PluginUninstalledMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (PluginUninstalledMessage.class.equals(Integer.class)
                        || PluginUninstalledMessage.class.equals(Long.class)
                        || PluginUninstalledMessage.class.equals(Float.class)
                        || PluginUninstalledMessage.class.equals(Double.class)
                        || PluginUninstalledMessage.class.equals(Boolean.class)
                        || PluginUninstalledMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((PluginUninstalledMessage.class.equals(Integer.class)
                                || PluginUninstalledMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((PluginUninstalledMessage.class.equals(Float.class)
                                || PluginUninstalledMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (PluginUninstalledMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (PluginUninstalledMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(PluginUninstalledMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'PluginUninstalledMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'PluginUninstalledMessage'", e);
            }

            // deserialize RefreshProgressMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (RefreshProgressMessage.class.equals(Integer.class)
                        || RefreshProgressMessage.class.equals(Long.class)
                        || RefreshProgressMessage.class.equals(Float.class)
                        || RefreshProgressMessage.class.equals(Double.class)
                        || RefreshProgressMessage.class.equals(Boolean.class)
                        || RefreshProgressMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((RefreshProgressMessage.class.equals(Integer.class)
                                || RefreshProgressMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((RefreshProgressMessage.class.equals(Float.class)
                                || RefreshProgressMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (RefreshProgressMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (RefreshProgressMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(RefreshProgressMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'RefreshProgressMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'RefreshProgressMessage'", e);
            }

            // deserialize RestartRequiredMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (RestartRequiredMessage.class.equals(Integer.class)
                        || RestartRequiredMessage.class.equals(Long.class)
                        || RestartRequiredMessage.class.equals(Float.class)
                        || RestartRequiredMessage.class.equals(Double.class)
                        || RestartRequiredMessage.class.equals(Boolean.class)
                        || RestartRequiredMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((RestartRequiredMessage.class.equals(Integer.class)
                                || RestartRequiredMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((RestartRequiredMessage.class.equals(Float.class)
                                || RestartRequiredMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (RestartRequiredMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (RestartRequiredMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(RestartRequiredMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'RestartRequiredMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'RestartRequiredMessage'", e);
            }

            // deserialize ScheduledTaskEndedMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (ScheduledTaskEndedMessage.class.equals(Integer.class)
                        || ScheduledTaskEndedMessage.class.equals(Long.class)
                        || ScheduledTaskEndedMessage.class.equals(Float.class)
                        || ScheduledTaskEndedMessage.class.equals(Double.class)
                        || ScheduledTaskEndedMessage.class.equals(Boolean.class)
                        || ScheduledTaskEndedMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((ScheduledTaskEndedMessage.class.equals(Integer.class)
                                || ScheduledTaskEndedMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((ScheduledTaskEndedMessage.class.equals(Float.class)
                                || ScheduledTaskEndedMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (ScheduledTaskEndedMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (ScheduledTaskEndedMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(ScheduledTaskEndedMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'ScheduledTaskEndedMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'ScheduledTaskEndedMessage'", e);
            }

            // deserialize ScheduledTasksInfoMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (ScheduledTasksInfoMessage.class.equals(Integer.class)
                        || ScheduledTasksInfoMessage.class.equals(Long.class)
                        || ScheduledTasksInfoMessage.class.equals(Float.class)
                        || ScheduledTasksInfoMessage.class.equals(Double.class)
                        || ScheduledTasksInfoMessage.class.equals(Boolean.class)
                        || ScheduledTasksInfoMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((ScheduledTasksInfoMessage.class.equals(Integer.class)
                                || ScheduledTasksInfoMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((ScheduledTasksInfoMessage.class.equals(Float.class)
                                || ScheduledTasksInfoMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (ScheduledTasksInfoMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (ScheduledTasksInfoMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(ScheduledTasksInfoMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'ScheduledTasksInfoMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'ScheduledTasksInfoMessage'", e);
            }

            // deserialize SeriesTimerCancelledMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (SeriesTimerCancelledMessage.class.equals(Integer.class)
                        || SeriesTimerCancelledMessage.class.equals(Long.class)
                        || SeriesTimerCancelledMessage.class.equals(Float.class)
                        || SeriesTimerCancelledMessage.class.equals(Double.class)
                        || SeriesTimerCancelledMessage.class.equals(Boolean.class)
                        || SeriesTimerCancelledMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((SeriesTimerCancelledMessage.class.equals(Integer.class)
                                || SeriesTimerCancelledMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((SeriesTimerCancelledMessage.class.equals(Float.class)
                                || SeriesTimerCancelledMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (SeriesTimerCancelledMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (SeriesTimerCancelledMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SeriesTimerCancelledMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SeriesTimerCancelledMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SeriesTimerCancelledMessage'", e);
            }

            // deserialize SeriesTimerCreatedMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (SeriesTimerCreatedMessage.class.equals(Integer.class)
                        || SeriesTimerCreatedMessage.class.equals(Long.class)
                        || SeriesTimerCreatedMessage.class.equals(Float.class)
                        || SeriesTimerCreatedMessage.class.equals(Double.class)
                        || SeriesTimerCreatedMessage.class.equals(Boolean.class)
                        || SeriesTimerCreatedMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((SeriesTimerCreatedMessage.class.equals(Integer.class)
                                || SeriesTimerCreatedMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((SeriesTimerCreatedMessage.class.equals(Float.class)
                                || SeriesTimerCreatedMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (SeriesTimerCreatedMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (SeriesTimerCreatedMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SeriesTimerCreatedMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SeriesTimerCreatedMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SeriesTimerCreatedMessage'", e);
            }

            // deserialize ServerRestartingMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (ServerRestartingMessage.class.equals(Integer.class)
                        || ServerRestartingMessage.class.equals(Long.class)
                        || ServerRestartingMessage.class.equals(Float.class)
                        || ServerRestartingMessage.class.equals(Double.class)
                        || ServerRestartingMessage.class.equals(Boolean.class)
                        || ServerRestartingMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((ServerRestartingMessage.class.equals(Integer.class)
                                || ServerRestartingMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((ServerRestartingMessage.class.equals(Float.class)
                                || ServerRestartingMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (ServerRestartingMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (ServerRestartingMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(ServerRestartingMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'ServerRestartingMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'ServerRestartingMessage'", e);
            }

            // deserialize ServerShuttingDownMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (ServerShuttingDownMessage.class.equals(Integer.class)
                        || ServerShuttingDownMessage.class.equals(Long.class)
                        || ServerShuttingDownMessage.class.equals(Float.class)
                        || ServerShuttingDownMessage.class.equals(Double.class)
                        || ServerShuttingDownMessage.class.equals(Boolean.class)
                        || ServerShuttingDownMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((ServerShuttingDownMessage.class.equals(Integer.class)
                                || ServerShuttingDownMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((ServerShuttingDownMessage.class.equals(Float.class)
                                || ServerShuttingDownMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (ServerShuttingDownMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (ServerShuttingDownMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(ServerShuttingDownMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'ServerShuttingDownMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'ServerShuttingDownMessage'", e);
            }

            // deserialize SessionsMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (SessionsMessage.class.equals(Integer.class) || SessionsMessage.class.equals(Long.class)
                        || SessionsMessage.class.equals(Float.class) || SessionsMessage.class.equals(Double.class)
                        || SessionsMessage.class.equals(Boolean.class) || SessionsMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((SessionsMessage.class.equals(Integer.class)
                                || SessionsMessage.class.equals(Long.class)) && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((SessionsMessage.class.equals(Float.class)
                                || SessionsMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (SessionsMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (SessionsMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SessionsMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SessionsMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SessionsMessage'", e);
            }

            // deserialize SyncPlayCommandMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (SyncPlayCommandMessage.class.equals(Integer.class)
                        || SyncPlayCommandMessage.class.equals(Long.class)
                        || SyncPlayCommandMessage.class.equals(Float.class)
                        || SyncPlayCommandMessage.class.equals(Double.class)
                        || SyncPlayCommandMessage.class.equals(Boolean.class)
                        || SyncPlayCommandMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((SyncPlayCommandMessage.class.equals(Integer.class)
                                || SyncPlayCommandMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((SyncPlayCommandMessage.class.equals(Float.class)
                                || SyncPlayCommandMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (SyncPlayCommandMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (SyncPlayCommandMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SyncPlayCommandMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SyncPlayCommandMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SyncPlayCommandMessage'", e);
            }

            // deserialize SyncPlayGroupUpdateMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (SyncPlayGroupUpdateMessage.class.equals(Integer.class)
                        || SyncPlayGroupUpdateMessage.class.equals(Long.class)
                        || SyncPlayGroupUpdateMessage.class.equals(Float.class)
                        || SyncPlayGroupUpdateMessage.class.equals(Double.class)
                        || SyncPlayGroupUpdateMessage.class.equals(Boolean.class)
                        || SyncPlayGroupUpdateMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((SyncPlayGroupUpdateMessage.class.equals(Integer.class)
                                || SyncPlayGroupUpdateMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((SyncPlayGroupUpdateMessage.class.equals(Float.class)
                                || SyncPlayGroupUpdateMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (SyncPlayGroupUpdateMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (SyncPlayGroupUpdateMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SyncPlayGroupUpdateMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SyncPlayGroupUpdateMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SyncPlayGroupUpdateMessage'", e);
            }

            // deserialize TimerCancelledMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (TimerCancelledMessage.class.equals(Integer.class) || TimerCancelledMessage.class.equals(Long.class)
                        || TimerCancelledMessage.class.equals(Float.class)
                        || TimerCancelledMessage.class.equals(Double.class)
                        || TimerCancelledMessage.class.equals(Boolean.class)
                        || TimerCancelledMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((TimerCancelledMessage.class.equals(Integer.class)
                                || TimerCancelledMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((TimerCancelledMessage.class.equals(Float.class)
                                || TimerCancelledMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (TimerCancelledMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (TimerCancelledMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(TimerCancelledMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'TimerCancelledMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'TimerCancelledMessage'", e);
            }

            // deserialize TimerCreatedMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (TimerCreatedMessage.class.equals(Integer.class) || TimerCreatedMessage.class.equals(Long.class)
                        || TimerCreatedMessage.class.equals(Float.class)
                        || TimerCreatedMessage.class.equals(Double.class)
                        || TimerCreatedMessage.class.equals(Boolean.class)
                        || TimerCreatedMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((TimerCreatedMessage.class.equals(Integer.class)
                                || TimerCreatedMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((TimerCreatedMessage.class.equals(Float.class)
                                || TimerCreatedMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (TimerCreatedMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (TimerCreatedMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(TimerCreatedMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'TimerCreatedMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'TimerCreatedMessage'", e);
            }

            // deserialize UserDataChangedMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (UserDataChangedMessage.class.equals(Integer.class)
                        || UserDataChangedMessage.class.equals(Long.class)
                        || UserDataChangedMessage.class.equals(Float.class)
                        || UserDataChangedMessage.class.equals(Double.class)
                        || UserDataChangedMessage.class.equals(Boolean.class)
                        || UserDataChangedMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((UserDataChangedMessage.class.equals(Integer.class)
                                || UserDataChangedMessage.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((UserDataChangedMessage.class.equals(Float.class)
                                || UserDataChangedMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (UserDataChangedMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (UserDataChangedMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(UserDataChangedMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'UserDataChangedMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'UserDataChangedMessage'", e);
            }

            // deserialize UserDeletedMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (UserDeletedMessage.class.equals(Integer.class) || UserDeletedMessage.class.equals(Long.class)
                        || UserDeletedMessage.class.equals(Float.class) || UserDeletedMessage.class.equals(Double.class)
                        || UserDeletedMessage.class.equals(Boolean.class)
                        || UserDeletedMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((UserDeletedMessage.class.equals(Integer.class)
                                || UserDeletedMessage.class.equals(Long.class)) && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((UserDeletedMessage.class.equals(Float.class)
                                || UserDeletedMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (UserDeletedMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (UserDeletedMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(UserDeletedMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'UserDeletedMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'UserDeletedMessage'", e);
            }

            // deserialize UserUpdatedMessage
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (UserUpdatedMessage.class.equals(Integer.class) || UserUpdatedMessage.class.equals(Long.class)
                        || UserUpdatedMessage.class.equals(Float.class) || UserUpdatedMessage.class.equals(Double.class)
                        || UserUpdatedMessage.class.equals(Boolean.class)
                        || UserUpdatedMessage.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((UserUpdatedMessage.class.equals(Integer.class)
                                || UserUpdatedMessage.class.equals(Long.class)) && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((UserUpdatedMessage.class.equals(Float.class)
                                || UserUpdatedMessage.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (UserUpdatedMessage.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (UserUpdatedMessage.class.equals(String.class)
                                && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(UserUpdatedMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'UserUpdatedMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'UserUpdatedMessage'", e);
            }

            if (match == 1) {
                OutboundWebSocketMessage ret = new OutboundWebSocketMessage();
                ret.setActualInstance(deserialized);
                return ret;
            }
            throw new IOException(String.format(java.util.Locale.ROOT,
                    "Failed deserialization for OutboundWebSocketMessage: %d classes match result, expected 1", match));
        }

        /**
         * Handle deserialization of the 'null' value.
         */
        @Override
        public OutboundWebSocketMessage getNullValue(DeserializationContext ctxt) throws JsonMappingException {
            throw new JsonMappingException(ctxt.getParser(), "OutboundWebSocketMessage cannot be null");
        }
    }

    // store a list of schema names defined in oneOf
    public static final Map<String, Class<?>> schemas = new HashMap<>();

    public OutboundWebSocketMessage() {
        super("oneOf", Boolean.FALSE);
    }

    public OutboundWebSocketMessage(ActivityLogEntryMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(ForceKeepAliveMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(GeneralCommandMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(LibraryChangedMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(OutboundKeepAliveMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(PlayMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(PlaystateMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(PluginInstallationCancelledMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(PluginInstallationCompletedMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(PluginInstallationFailedMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(PluginInstallingMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(PluginUninstalledMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(RefreshProgressMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(RestartRequiredMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(ScheduledTaskEndedMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(ScheduledTasksInfoMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(SeriesTimerCancelledMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(SeriesTimerCreatedMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(ServerRestartingMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(ServerShuttingDownMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(SessionsMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(SyncPlayCommandMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(SyncPlayGroupUpdateMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(TimerCancelledMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(TimerCreatedMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(UserDataChangedMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(UserDeletedMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public OutboundWebSocketMessage(UserUpdatedMessage o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    static {
        schemas.put("ActivityLogEntryMessage", ActivityLogEntryMessage.class);
        schemas.put("ForceKeepAliveMessage", ForceKeepAliveMessage.class);
        schemas.put("GeneralCommandMessage", GeneralCommandMessage.class);
        schemas.put("LibraryChangedMessage", LibraryChangedMessage.class);
        schemas.put("OutboundKeepAliveMessage", OutboundKeepAliveMessage.class);
        schemas.put("PlayMessage", PlayMessage.class);
        schemas.put("PlaystateMessage", PlaystateMessage.class);
        schemas.put("PluginInstallationCancelledMessage", PluginInstallationCancelledMessage.class);
        schemas.put("PluginInstallationCompletedMessage", PluginInstallationCompletedMessage.class);
        schemas.put("PluginInstallationFailedMessage", PluginInstallationFailedMessage.class);
        schemas.put("PluginInstallingMessage", PluginInstallingMessage.class);
        schemas.put("PluginUninstalledMessage", PluginUninstalledMessage.class);
        schemas.put("RefreshProgressMessage", RefreshProgressMessage.class);
        schemas.put("RestartRequiredMessage", RestartRequiredMessage.class);
        schemas.put("ScheduledTaskEndedMessage", ScheduledTaskEndedMessage.class);
        schemas.put("ScheduledTasksInfoMessage", ScheduledTasksInfoMessage.class);
        schemas.put("SeriesTimerCancelledMessage", SeriesTimerCancelledMessage.class);
        schemas.put("SeriesTimerCreatedMessage", SeriesTimerCreatedMessage.class);
        schemas.put("ServerRestartingMessage", ServerRestartingMessage.class);
        schemas.put("ServerShuttingDownMessage", ServerShuttingDownMessage.class);
        schemas.put("SessionsMessage", SessionsMessage.class);
        schemas.put("SyncPlayCommandMessage", SyncPlayCommandMessage.class);
        schemas.put("SyncPlayGroupUpdateMessage", SyncPlayGroupUpdateMessage.class);
        schemas.put("TimerCancelledMessage", TimerCancelledMessage.class);
        schemas.put("TimerCreatedMessage", TimerCreatedMessage.class);
        schemas.put("UserDataChangedMessage", UserDataChangedMessage.class);
        schemas.put("UserDeletedMessage", UserDeletedMessage.class);
        schemas.put("UserUpdatedMessage", UserUpdatedMessage.class);
        JSON.registerDescendants(OutboundWebSocketMessage.class, Collections.unmodifiableMap(schemas));
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
        mappings.put("OutboundWebSocketMessage", OutboundWebSocketMessage.class);
        JSON.registerDiscriminator(OutboundWebSocketMessage.class, "MessageType", mappings);
    }

    @Override
    public Map<String, Class<?>> getSchemas() {
        return OutboundWebSocketMessage.schemas;
    }

    /**
     * Set the instance that matches the oneOf child schema, check
     * the instance parameter is valid against the oneOf child schemas:
     * ActivityLogEntryMessage, ForceKeepAliveMessage, GeneralCommandMessage, LibraryChangedMessage,
     * OutboundKeepAliveMessage, PlayMessage, PlaystateMessage, PluginInstallationCancelledMessage,
     * PluginInstallationCompletedMessage, PluginInstallationFailedMessage, PluginInstallingMessage,
     * PluginUninstalledMessage, RefreshProgressMessage, RestartRequiredMessage, ScheduledTaskEndedMessage,
     * ScheduledTasksInfoMessage, SeriesTimerCancelledMessage, SeriesTimerCreatedMessage, ServerRestartingMessage,
     * ServerShuttingDownMessage, SessionsMessage, SyncPlayCommandMessage, SyncPlayGroupUpdateMessage,
     * TimerCancelledMessage, TimerCreatedMessage, UserDataChangedMessage, UserDeletedMessage, UserUpdatedMessage
     *
     * It could be an instance of the 'oneOf' schemas.
     * The oneOf child schemas may themselves be a composed schema (allOf, anyOf, oneOf).
     */
    @Override
    public void setActualInstance(Object instance) {
        if (JSON.isInstanceOf(ActivityLogEntryMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(ForceKeepAliveMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(GeneralCommandMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(LibraryChangedMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(OutboundKeepAliveMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PlayMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PlaystateMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PluginInstallationCancelledMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PluginInstallationCompletedMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PluginInstallationFailedMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PluginInstallingMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PluginUninstalledMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(RefreshProgressMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(RestartRequiredMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(ScheduledTaskEndedMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(ScheduledTasksInfoMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SeriesTimerCancelledMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SeriesTimerCreatedMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(ServerRestartingMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(ServerShuttingDownMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SessionsMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SyncPlayCommandMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SyncPlayGroupUpdateMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(TimerCancelledMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(TimerCreatedMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(UserDataChangedMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(UserDeletedMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(UserUpdatedMessage.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        throw new RuntimeException(
                "Invalid instance type. Must be ActivityLogEntryMessage, ForceKeepAliveMessage, GeneralCommandMessage, LibraryChangedMessage, OutboundKeepAliveMessage, PlayMessage, PlaystateMessage, PluginInstallationCancelledMessage, PluginInstallationCompletedMessage, PluginInstallationFailedMessage, PluginInstallingMessage, PluginUninstalledMessage, RefreshProgressMessage, RestartRequiredMessage, ScheduledTaskEndedMessage, ScheduledTasksInfoMessage, SeriesTimerCancelledMessage, SeriesTimerCreatedMessage, ServerRestartingMessage, ServerShuttingDownMessage, SessionsMessage, SyncPlayCommandMessage, SyncPlayGroupUpdateMessage, TimerCancelledMessage, TimerCreatedMessage, UserDataChangedMessage, UserDeletedMessage, UserUpdatedMessage");
    }

    /**
     * Get the actual instance, which can be the following:
     * ActivityLogEntryMessage, ForceKeepAliveMessage, GeneralCommandMessage, LibraryChangedMessage,
     * OutboundKeepAliveMessage, PlayMessage, PlaystateMessage, PluginInstallationCancelledMessage,
     * PluginInstallationCompletedMessage, PluginInstallationFailedMessage, PluginInstallingMessage,
     * PluginUninstalledMessage, RefreshProgressMessage, RestartRequiredMessage, ScheduledTaskEndedMessage,
     * ScheduledTasksInfoMessage, SeriesTimerCancelledMessage, SeriesTimerCreatedMessage, ServerRestartingMessage,
     * ServerShuttingDownMessage, SessionsMessage, SyncPlayCommandMessage, SyncPlayGroupUpdateMessage,
     * TimerCancelledMessage, TimerCreatedMessage, UserDataChangedMessage, UserDeletedMessage, UserUpdatedMessage
     *
     * @return The actual instance (ActivityLogEntryMessage, ForceKeepAliveMessage, GeneralCommandMessage,
     *         LibraryChangedMessage, OutboundKeepAliveMessage, PlayMessage, PlaystateMessage,
     *         PluginInstallationCancelledMessage, PluginInstallationCompletedMessage, PluginInstallationFailedMessage,
     *         PluginInstallingMessage, PluginUninstalledMessage, RefreshProgressMessage, RestartRequiredMessage,
     *         ScheduledTaskEndedMessage, ScheduledTasksInfoMessage, SeriesTimerCancelledMessage,
     *         SeriesTimerCreatedMessage, ServerRestartingMessage, ServerShuttingDownMessage, SessionsMessage,
     *         SyncPlayCommandMessage, SyncPlayGroupUpdateMessage, TimerCancelledMessage, TimerCreatedMessage,
     *         UserDataChangedMessage, UserDeletedMessage, UserUpdatedMessage)
     */
    @Override
    public Object getActualInstance() {
        return super.getActualInstance();
    }

    /**
     * Get the actual instance of `ActivityLogEntryMessage`. If the actual instance is not `ActivityLogEntryMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `ActivityLogEntryMessage`
     * @throws ClassCastException if the instance is not `ActivityLogEntryMessage`
     */
    public ActivityLogEntryMessage getActivityLogEntryMessage() throws ClassCastException {
        return (ActivityLogEntryMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `ForceKeepAliveMessage`. If the actual instance is not `ForceKeepAliveMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `ForceKeepAliveMessage`
     * @throws ClassCastException if the instance is not `ForceKeepAliveMessage`
     */
    public ForceKeepAliveMessage getForceKeepAliveMessage() throws ClassCastException {
        return (ForceKeepAliveMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `GeneralCommandMessage`. If the actual instance is not `GeneralCommandMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `GeneralCommandMessage`
     * @throws ClassCastException if the instance is not `GeneralCommandMessage`
     */
    public GeneralCommandMessage getGeneralCommandMessage() throws ClassCastException {
        return (GeneralCommandMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `LibraryChangedMessage`. If the actual instance is not `LibraryChangedMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `LibraryChangedMessage`
     * @throws ClassCastException if the instance is not `LibraryChangedMessage`
     */
    public LibraryChangedMessage getLibraryChangedMessage() throws ClassCastException {
        return (LibraryChangedMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `OutboundKeepAliveMessage`. If the actual instance is not `OutboundKeepAliveMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `OutboundKeepAliveMessage`
     * @throws ClassCastException if the instance is not `OutboundKeepAliveMessage`
     */
    public OutboundKeepAliveMessage getOutboundKeepAliveMessage() throws ClassCastException {
        return (OutboundKeepAliveMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `PlayMessage`. If the actual instance is not `PlayMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `PlayMessage`
     * @throws ClassCastException if the instance is not `PlayMessage`
     */
    public PlayMessage getPlayMessage() throws ClassCastException {
        return (PlayMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `PlaystateMessage`. If the actual instance is not `PlaystateMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `PlaystateMessage`
     * @throws ClassCastException if the instance is not `PlaystateMessage`
     */
    public PlaystateMessage getPlaystateMessage() throws ClassCastException {
        return (PlaystateMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `PluginInstallationCancelledMessage`. If the actual instance is not
     * `PluginInstallationCancelledMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `PluginInstallationCancelledMessage`
     * @throws ClassCastException if the instance is not `PluginInstallationCancelledMessage`
     */
    public PluginInstallationCancelledMessage getPluginInstallationCancelledMessage() throws ClassCastException {
        return (PluginInstallationCancelledMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `PluginInstallationCompletedMessage`. If the actual instance is not
     * `PluginInstallationCompletedMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `PluginInstallationCompletedMessage`
     * @throws ClassCastException if the instance is not `PluginInstallationCompletedMessage`
     */
    public PluginInstallationCompletedMessage getPluginInstallationCompletedMessage() throws ClassCastException {
        return (PluginInstallationCompletedMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `PluginInstallationFailedMessage`. If the actual instance is not
     * `PluginInstallationFailedMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `PluginInstallationFailedMessage`
     * @throws ClassCastException if the instance is not `PluginInstallationFailedMessage`
     */
    public PluginInstallationFailedMessage getPluginInstallationFailedMessage() throws ClassCastException {
        return (PluginInstallationFailedMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `PluginInstallingMessage`. If the actual instance is not `PluginInstallingMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `PluginInstallingMessage`
     * @throws ClassCastException if the instance is not `PluginInstallingMessage`
     */
    public PluginInstallingMessage getPluginInstallingMessage() throws ClassCastException {
        return (PluginInstallingMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `PluginUninstalledMessage`. If the actual instance is not `PluginUninstalledMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `PluginUninstalledMessage`
     * @throws ClassCastException if the instance is not `PluginUninstalledMessage`
     */
    public PluginUninstalledMessage getPluginUninstalledMessage() throws ClassCastException {
        return (PluginUninstalledMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `RefreshProgressMessage`. If the actual instance is not `RefreshProgressMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `RefreshProgressMessage`
     * @throws ClassCastException if the instance is not `RefreshProgressMessage`
     */
    public RefreshProgressMessage getRefreshProgressMessage() throws ClassCastException {
        return (RefreshProgressMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `RestartRequiredMessage`. If the actual instance is not `RestartRequiredMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `RestartRequiredMessage`
     * @throws ClassCastException if the instance is not `RestartRequiredMessage`
     */
    public RestartRequiredMessage getRestartRequiredMessage() throws ClassCastException {
        return (RestartRequiredMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `ScheduledTaskEndedMessage`. If the actual instance is not
     * `ScheduledTaskEndedMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `ScheduledTaskEndedMessage`
     * @throws ClassCastException if the instance is not `ScheduledTaskEndedMessage`
     */
    public ScheduledTaskEndedMessage getScheduledTaskEndedMessage() throws ClassCastException {
        return (ScheduledTaskEndedMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `ScheduledTasksInfoMessage`. If the actual instance is not
     * `ScheduledTasksInfoMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `ScheduledTasksInfoMessage`
     * @throws ClassCastException if the instance is not `ScheduledTasksInfoMessage`
     */
    public ScheduledTasksInfoMessage getScheduledTasksInfoMessage() throws ClassCastException {
        return (ScheduledTasksInfoMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `SeriesTimerCancelledMessage`. If the actual instance is not
     * `SeriesTimerCancelledMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SeriesTimerCancelledMessage`
     * @throws ClassCastException if the instance is not `SeriesTimerCancelledMessage`
     */
    public SeriesTimerCancelledMessage getSeriesTimerCancelledMessage() throws ClassCastException {
        return (SeriesTimerCancelledMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `SeriesTimerCreatedMessage`. If the actual instance is not
     * `SeriesTimerCreatedMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SeriesTimerCreatedMessage`
     * @throws ClassCastException if the instance is not `SeriesTimerCreatedMessage`
     */
    public SeriesTimerCreatedMessage getSeriesTimerCreatedMessage() throws ClassCastException {
        return (SeriesTimerCreatedMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `ServerRestartingMessage`. If the actual instance is not `ServerRestartingMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `ServerRestartingMessage`
     * @throws ClassCastException if the instance is not `ServerRestartingMessage`
     */
    public ServerRestartingMessage getServerRestartingMessage() throws ClassCastException {
        return (ServerRestartingMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `ServerShuttingDownMessage`. If the actual instance is not
     * `ServerShuttingDownMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `ServerShuttingDownMessage`
     * @throws ClassCastException if the instance is not `ServerShuttingDownMessage`
     */
    public ServerShuttingDownMessage getServerShuttingDownMessage() throws ClassCastException {
        return (ServerShuttingDownMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `SessionsMessage`. If the actual instance is not `SessionsMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SessionsMessage`
     * @throws ClassCastException if the instance is not `SessionsMessage`
     */
    public SessionsMessage getSessionsMessage() throws ClassCastException {
        return (SessionsMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `SyncPlayCommandMessage`. If the actual instance is not `SyncPlayCommandMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SyncPlayCommandMessage`
     * @throws ClassCastException if the instance is not `SyncPlayCommandMessage`
     */
    public SyncPlayCommandMessage getSyncPlayCommandMessage() throws ClassCastException {
        return (SyncPlayCommandMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `SyncPlayGroupUpdateMessage`. If the actual instance is not
     * `SyncPlayGroupUpdateMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SyncPlayGroupUpdateMessage`
     * @throws ClassCastException if the instance is not `SyncPlayGroupUpdateMessage`
     */
    public SyncPlayGroupUpdateMessage getSyncPlayGroupUpdateMessage() throws ClassCastException {
        return (SyncPlayGroupUpdateMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `TimerCancelledMessage`. If the actual instance is not `TimerCancelledMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `TimerCancelledMessage`
     * @throws ClassCastException if the instance is not `TimerCancelledMessage`
     */
    public TimerCancelledMessage getTimerCancelledMessage() throws ClassCastException {
        return (TimerCancelledMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `TimerCreatedMessage`. If the actual instance is not `TimerCreatedMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `TimerCreatedMessage`
     * @throws ClassCastException if the instance is not `TimerCreatedMessage`
     */
    public TimerCreatedMessage getTimerCreatedMessage() throws ClassCastException {
        return (TimerCreatedMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `UserDataChangedMessage`. If the actual instance is not `UserDataChangedMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `UserDataChangedMessage`
     * @throws ClassCastException if the instance is not `UserDataChangedMessage`
     */
    public UserDataChangedMessage getUserDataChangedMessage() throws ClassCastException {
        return (UserDataChangedMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `UserDeletedMessage`. If the actual instance is not `UserDeletedMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `UserDeletedMessage`
     * @throws ClassCastException if the instance is not `UserDeletedMessage`
     */
    public UserDeletedMessage getUserDeletedMessage() throws ClassCastException {
        return (UserDeletedMessage) super.getActualInstance();
    }

    /**
     * Get the actual instance of `UserUpdatedMessage`. If the actual instance is not `UserUpdatedMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `UserUpdatedMessage`
     * @throws ClassCastException if the instance is not `UserUpdatedMessage`
     */
    public UserUpdatedMessage getUserUpdatedMessage() throws ClassCastException {
        return (UserUpdatedMessage) super.getActualInstance();
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

        if (getActualInstance() instanceof ActivityLogEntryMessage) {
            if (getActualInstance() != null) {
                joiner.add(
                        ((ActivityLogEntryMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_0" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof ForceKeepAliveMessage) {
            if (getActualInstance() != null) {
                joiner.add(
                        ((ForceKeepAliveMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_1" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof GeneralCommandMessage) {
            if (getActualInstance() != null) {
                joiner.add(
                        ((GeneralCommandMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_2" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof LibraryChangedMessage) {
            if (getActualInstance() != null) {
                joiner.add(
                        ((LibraryChangedMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_3" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof OutboundKeepAliveMessage) {
            if (getActualInstance() != null) {
                joiner.add(((OutboundKeepAliveMessage) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_4" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof PlayMessage) {
            if (getActualInstance() != null) {
                joiner.add(((PlayMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_5" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof PlaystateMessage) {
            if (getActualInstance() != null) {
                joiner.add(((PlaystateMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_6" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof PluginInstallationCancelledMessage) {
            if (getActualInstance() != null) {
                joiner.add(((PluginInstallationCancelledMessage) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_7" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof PluginInstallationCompletedMessage) {
            if (getActualInstance() != null) {
                joiner.add(((PluginInstallationCompletedMessage) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_8" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof PluginInstallationFailedMessage) {
            if (getActualInstance() != null) {
                joiner.add(((PluginInstallationFailedMessage) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_9" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof PluginInstallingMessage) {
            if (getActualInstance() != null) {
                joiner.add(((PluginInstallingMessage) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_10" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof PluginUninstalledMessage) {
            if (getActualInstance() != null) {
                joiner.add(((PluginUninstalledMessage) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_11" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof RefreshProgressMessage) {
            if (getActualInstance() != null) {
                joiner.add(
                        ((RefreshProgressMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_12" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof RestartRequiredMessage) {
            if (getActualInstance() != null) {
                joiner.add(
                        ((RestartRequiredMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_13" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof ScheduledTaskEndedMessage) {
            if (getActualInstance() != null) {
                joiner.add(((ScheduledTaskEndedMessage) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_14" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof ScheduledTasksInfoMessage) {
            if (getActualInstance() != null) {
                joiner.add(((ScheduledTasksInfoMessage) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_15" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof SeriesTimerCancelledMessage) {
            if (getActualInstance() != null) {
                joiner.add(((SeriesTimerCancelledMessage) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_16" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof SeriesTimerCreatedMessage) {
            if (getActualInstance() != null) {
                joiner.add(((SeriesTimerCreatedMessage) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_17" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof ServerRestartingMessage) {
            if (getActualInstance() != null) {
                joiner.add(((ServerRestartingMessage) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_18" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof ServerShuttingDownMessage) {
            if (getActualInstance() != null) {
                joiner.add(((ServerShuttingDownMessage) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_19" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof SessionsMessage) {
            if (getActualInstance() != null) {
                joiner.add(((SessionsMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_20" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof SyncPlayCommandMessage) {
            if (getActualInstance() != null) {
                joiner.add(
                        ((SyncPlayCommandMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_21" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof TimerCancelledMessage) {
            if (getActualInstance() != null) {
                joiner.add(
                        ((TimerCancelledMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_22" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof TimerCreatedMessage) {
            if (getActualInstance() != null) {
                joiner.add(((TimerCreatedMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_23" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof UserDataChangedMessage) {
            if (getActualInstance() != null) {
                joiner.add(
                        ((UserDataChangedMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_24" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof UserDeletedMessage) {
            if (getActualInstance() != null) {
                joiner.add(((UserDeletedMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_25" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof UserUpdatedMessage) {
            if (getActualInstance() != null) {
                joiner.add(((UserUpdatedMessage) getActualInstance()).toUrlQueryString(prefix + "one_of_26" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof SyncPlayGroupUpdateMessage) {
            if (getActualInstance() != null) {
                joiner.add(((SyncPlayGroupUpdateMessage) getActualInstance())
                        .toUrlQueryString(prefix + "one_of_27" + suffix));
            }
            return joiner.toString();
        }
        return null;
    }
}
