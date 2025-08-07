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

            // deserialize SyncPlayGroupUpdateCommandMessage
            try {
                boolean attemptParsing = true;
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(SyncPlayGroupUpdateCommandMessage.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'SyncPlayGroupUpdateCommandMessage'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'SyncPlayGroupUpdateCommandMessage'", e);
            }

            // deserialize TimerCancelledMessage
            try {
                boolean attemptParsing = true;
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
            throw new IOException(String.format(
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
    public static final Map<String, GenericType<?>> schemas = new HashMap<>();

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

    public OutboundWebSocketMessage(SyncPlayGroupUpdateCommandMessage o) {
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
        schemas.put("ActivityLogEntryMessage", new GenericType<ActivityLogEntryMessage>() {
        });
        schemas.put("ForceKeepAliveMessage", new GenericType<ForceKeepAliveMessage>() {
        });
        schemas.put("GeneralCommandMessage", new GenericType<GeneralCommandMessage>() {
        });
        schemas.put("LibraryChangedMessage", new GenericType<LibraryChangedMessage>() {
        });
        schemas.put("OutboundKeepAliveMessage", new GenericType<OutboundKeepAliveMessage>() {
        });
        schemas.put("PlayMessage", new GenericType<PlayMessage>() {
        });
        schemas.put("PlaystateMessage", new GenericType<PlaystateMessage>() {
        });
        schemas.put("PluginInstallationCancelledMessage", new GenericType<PluginInstallationCancelledMessage>() {
        });
        schemas.put("PluginInstallationCompletedMessage", new GenericType<PluginInstallationCompletedMessage>() {
        });
        schemas.put("PluginInstallationFailedMessage", new GenericType<PluginInstallationFailedMessage>() {
        });
        schemas.put("PluginInstallingMessage", new GenericType<PluginInstallingMessage>() {
        });
        schemas.put("PluginUninstalledMessage", new GenericType<PluginUninstalledMessage>() {
        });
        schemas.put("RefreshProgressMessage", new GenericType<RefreshProgressMessage>() {
        });
        schemas.put("RestartRequiredMessage", new GenericType<RestartRequiredMessage>() {
        });
        schemas.put("ScheduledTaskEndedMessage", new GenericType<ScheduledTaskEndedMessage>() {
        });
        schemas.put("ScheduledTasksInfoMessage", new GenericType<ScheduledTasksInfoMessage>() {
        });
        schemas.put("SeriesTimerCancelledMessage", new GenericType<SeriesTimerCancelledMessage>() {
        });
        schemas.put("SeriesTimerCreatedMessage", new GenericType<SeriesTimerCreatedMessage>() {
        });
        schemas.put("ServerRestartingMessage", new GenericType<ServerRestartingMessage>() {
        });
        schemas.put("ServerShuttingDownMessage", new GenericType<ServerShuttingDownMessage>() {
        });
        schemas.put("SessionsMessage", new GenericType<SessionsMessage>() {
        });
        schemas.put("SyncPlayCommandMessage", new GenericType<SyncPlayCommandMessage>() {
        });
        schemas.put("SyncPlayGroupUpdateCommandMessage", new GenericType<SyncPlayGroupUpdateCommandMessage>() {
        });
        schemas.put("TimerCancelledMessage", new GenericType<TimerCancelledMessage>() {
        });
        schemas.put("TimerCreatedMessage", new GenericType<TimerCreatedMessage>() {
        });
        schemas.put("UserDataChangedMessage", new GenericType<UserDataChangedMessage>() {
        });
        schemas.put("UserDeletedMessage", new GenericType<UserDeletedMessage>() {
        });
        schemas.put("UserUpdatedMessage", new GenericType<UserUpdatedMessage>() {
        });
        JSON.registerDescendants(OutboundWebSocketMessage.class, Collections.unmodifiableMap(schemas));
        // Initialize and register the discriminator mappings.
        Map<String, Class<?>> mappings = new HashMap<>();
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
        mappings.put("SyncPlayGroupUpdate", SyncPlayGroupUpdateCommandMessage.class);
        mappings.put("TimerCancelled", TimerCancelledMessage.class);
        mappings.put("TimerCreated", TimerCreatedMessage.class);
        mappings.put("UserDataChanged", UserDataChangedMessage.class);
        mappings.put("UserDeleted", UserDeletedMessage.class);
        mappings.put("UserUpdated", UserUpdatedMessage.class);
        mappings.put("ActivityLogEntryMessage", ActivityLogEntryMessage.class);
        mappings.put("ForceKeepAliveMessage", ForceKeepAliveMessage.class);
        mappings.put("GeneralCommandMessage", GeneralCommandMessage.class);
        mappings.put("LibraryChangedMessage", LibraryChangedMessage.class);
        mappings.put("OutboundKeepAliveMessage", OutboundKeepAliveMessage.class);
        mappings.put("PlayMessage", PlayMessage.class);
        mappings.put("PlaystateMessage", PlaystateMessage.class);
        mappings.put("PluginInstallationCancelledMessage", PluginInstallationCancelledMessage.class);
        mappings.put("PluginInstallationCompletedMessage", PluginInstallationCompletedMessage.class);
        mappings.put("PluginInstallationFailedMessage", PluginInstallationFailedMessage.class);
        mappings.put("PluginInstallingMessage", PluginInstallingMessage.class);
        mappings.put("PluginUninstalledMessage", PluginUninstalledMessage.class);
        mappings.put("RefreshProgressMessage", RefreshProgressMessage.class);
        mappings.put("RestartRequiredMessage", RestartRequiredMessage.class);
        mappings.put("ScheduledTaskEndedMessage", ScheduledTaskEndedMessage.class);
        mappings.put("ScheduledTasksInfoMessage", ScheduledTasksInfoMessage.class);
        mappings.put("SeriesTimerCancelledMessage", SeriesTimerCancelledMessage.class);
        mappings.put("SeriesTimerCreatedMessage", SeriesTimerCreatedMessage.class);
        mappings.put("ServerRestartingMessage", ServerRestartingMessage.class);
        mappings.put("ServerShuttingDownMessage", ServerShuttingDownMessage.class);
        mappings.put("SessionsMessage", SessionsMessage.class);
        mappings.put("SyncPlayCommandMessage", SyncPlayCommandMessage.class);
        mappings.put("SyncPlayGroupUpdateCommandMessage", SyncPlayGroupUpdateCommandMessage.class);
        mappings.put("TimerCancelledMessage", TimerCancelledMessage.class);
        mappings.put("TimerCreatedMessage", TimerCreatedMessage.class);
        mappings.put("UserDataChangedMessage", UserDataChangedMessage.class);
        mappings.put("UserDeletedMessage", UserDeletedMessage.class);
        mappings.put("UserUpdatedMessage", UserUpdatedMessage.class);
        mappings.put("OutboundWebSocketMessage", OutboundWebSocketMessage.class);
        JSON.registerDiscriminator(OutboundWebSocketMessage.class, "MessageType", mappings);
    }

    @Override
    public Map<String, GenericType<?>> getSchemas() {
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
     * ServerShuttingDownMessage, SessionsMessage, SyncPlayCommandMessage, SyncPlayGroupUpdateCommandMessage,
     * TimerCancelledMessage, TimerCreatedMessage, UserDataChangedMessage, UserDeletedMessage, UserUpdatedMessage
     *
     * It could be an instance of the 'oneOf' schemas.
     * The oneOf child schemas may themselves be a composed schema (allOf, anyOf, oneOf).
     */
    @Override
    public void setActualInstance(Object instance) {
        if (JSON.isInstanceOf(ActivityLogEntryMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(ForceKeepAliveMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(GeneralCommandMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(LibraryChangedMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(OutboundKeepAliveMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PlayMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PlaystateMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PluginInstallationCancelledMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PluginInstallationCompletedMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PluginInstallationFailedMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PluginInstallingMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(PluginUninstalledMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(RefreshProgressMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(RestartRequiredMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(ScheduledTaskEndedMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(ScheduledTasksInfoMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SeriesTimerCancelledMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SeriesTimerCreatedMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(ServerRestartingMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(ServerShuttingDownMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SessionsMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SyncPlayCommandMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(SyncPlayGroupUpdateCommandMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(TimerCancelledMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(TimerCreatedMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(UserDataChangedMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(UserDeletedMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(UserUpdatedMessage.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        throw new RuntimeException(
                "Invalid instance type. Must be ActivityLogEntryMessage, ForceKeepAliveMessage, GeneralCommandMessage, LibraryChangedMessage, OutboundKeepAliveMessage, PlayMessage, PlaystateMessage, PluginInstallationCancelledMessage, PluginInstallationCompletedMessage, PluginInstallationFailedMessage, PluginInstallingMessage, PluginUninstalledMessage, RefreshProgressMessage, RestartRequiredMessage, ScheduledTaskEndedMessage, ScheduledTasksInfoMessage, SeriesTimerCancelledMessage, SeriesTimerCreatedMessage, ServerRestartingMessage, ServerShuttingDownMessage, SessionsMessage, SyncPlayCommandMessage, SyncPlayGroupUpdateCommandMessage, TimerCancelledMessage, TimerCreatedMessage, UserDataChangedMessage, UserDeletedMessage, UserUpdatedMessage");
    }

    /**
     * Get the actual instance, which can be the following:
     * ActivityLogEntryMessage, ForceKeepAliveMessage, GeneralCommandMessage, LibraryChangedMessage,
     * OutboundKeepAliveMessage, PlayMessage, PlaystateMessage, PluginInstallationCancelledMessage,
     * PluginInstallationCompletedMessage, PluginInstallationFailedMessage, PluginInstallingMessage,
     * PluginUninstalledMessage, RefreshProgressMessage, RestartRequiredMessage, ScheduledTaskEndedMessage,
     * ScheduledTasksInfoMessage, SeriesTimerCancelledMessage, SeriesTimerCreatedMessage, ServerRestartingMessage,
     * ServerShuttingDownMessage, SessionsMessage, SyncPlayCommandMessage, SyncPlayGroupUpdateCommandMessage,
     * TimerCancelledMessage, TimerCreatedMessage, UserDataChangedMessage, UserDeletedMessage, UserUpdatedMessage
     *
     * @return The actual instance (ActivityLogEntryMessage, ForceKeepAliveMessage, GeneralCommandMessage,
     *         LibraryChangedMessage, OutboundKeepAliveMessage, PlayMessage, PlaystateMessage,
     *         PluginInstallationCancelledMessage, PluginInstallationCompletedMessage, PluginInstallationFailedMessage,
     *         PluginInstallingMessage, PluginUninstalledMessage, RefreshProgressMessage, RestartRequiredMessage,
     *         ScheduledTaskEndedMessage, ScheduledTasksInfoMessage, SeriesTimerCancelledMessage,
     *         SeriesTimerCreatedMessage, ServerRestartingMessage, ServerShuttingDownMessage, SessionsMessage,
     *         SyncPlayCommandMessage, SyncPlayGroupUpdateCommandMessage, TimerCancelledMessage, TimerCreatedMessage,
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
     * Get the actual instance of `SyncPlayGroupUpdateCommandMessage`. If the actual instance is not
     * `SyncPlayGroupUpdateCommandMessage`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SyncPlayGroupUpdateCommandMessage`
     * @throws ClassCastException if the instance is not `SyncPlayGroupUpdateCommandMessage`
     */
    public SyncPlayGroupUpdateCommandMessage getSyncPlayGroupUpdateCommandMessage() throws ClassCastException {
        return (SyncPlayGroupUpdateCommandMessage) super.getActualInstance();
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
}
