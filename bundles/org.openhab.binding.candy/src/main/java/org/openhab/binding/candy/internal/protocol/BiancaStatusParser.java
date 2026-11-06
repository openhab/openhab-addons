/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.candy.internal.protocol;

import static org.openhab.binding.candy.internal.CandyBindingConstants.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.Strictness;

/**
 * Maps Bianca status fields using explicit wire constants instead of the app's UI enum ordinals.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public final class BiancaStatusParser {
    private final Gson gson = new GsonBuilder().setStrictness(Strictness.STRICT).create();

    public BiancaSnapshot parse(String json) throws CandyProtocolException {
        if (json.length() > CandyCodec.MAX_WIRE_BYTES / 2) {
            throw new CandyProtocolException("@text/protocol-error-size");
        }
        JsonElement root;
        try {
            root = gson.fromJson(json, JsonElement.class);
        } catch (JsonParseException e) {
            throw new CandyProtocolException("@text/protocol-error-json");
        }
        if (root == null || !root.isJsonObject()) {
            throw new CandyProtocolException("@text/protocol-error-wrapper");
        }
        JsonElement wrapper = root.getAsJsonObject().get("statusLavatrice");
        if (wrapper == null || !wrapper.isJsonObject()) {
            throw new CandyProtocolException("@text/protocol-error-wrapper");
        }
        JsonObject status = wrapper.getAsJsonObject();
        Map<String, State> states = new HashMap<>();
        LIVE_CHANNELS.forEach(channel -> states.put(channel, UnDefType.UNDEF));
        Integer mode = integer(status, "MachMd");
        Integer phase = integer(status, "PrPh");
        if (mode != null) {
            states.put(CHANNEL_MACHINE_MODE_CODE, new DecimalType(mode));
            Optional<MachineMode> machineMode = MachineMode.fromCode(mode);
            states.put(CHANNEL_MACHINE_STATE,
                    machineMode.isPresent() ? new StringType(machineMode.get().name()) : UnDefType.UNDEF);
        }
        if (phase != null) {
            states.put(CHANNEL_PROGRAM_PHASE_CODE, new DecimalType(phase));
            Optional<ProgramPhase> programPhase = ProgramPhase.fromCode(phase);
            states.put(CHANNEL_PROGRAM_PHASE,
                    programPhase.isPresent() ? new StringType(programPhase.get().name()) : UnDefType.UNDEF);
        }
        states.put(CHANNEL_PROGRAM_NUMBER, mapped(identifier(status, "PrNm")));
        states.put(CHANNEL_PROGRAM_CODE, mapped(identifier(status, "PrCode")));
        states.put(CHANNEL_ERROR_CODE, mapped(identifier(status, "Err")));

        Integer remaining = integer(status, "RemTime");
        if (remaining != null && remaining >= 0 && remaining != 60000) {
            states.put(CHANNEL_REMAINING_TIME, new QuantityType<>(remaining, Units.SECOND));
        }
        Integer temperature = integer(status, "Temp");
        if (temperature != null && temperature >= 0 && temperature < 255) {
            states.put(CHANNEL_TARGET_TEMPERATURE, new QuantityType<>(temperature, SIUnits.CELSIUS));
        }
        Integer spin = integer(status, "SpinSp");
        if (spin != null && spin >= 0 && spin != 255) {
            int rpm = spin < 100 ? spin * 100 : spin;
            states.put(CHANNEL_TARGET_SPIN_SPEED, new QuantityType<>(rpm, Units.RPM));
        }
        String remote = primitive(status, "WiFiStatus");
        if ("1".equals(remote) || "true".equalsIgnoreCase(remote)) {
            states.put(CHANNEL_REMOTE_CONTROL_ENABLED, OnOffType.ON);
        } else if ("0".equals(remote) || "false".equalsIgnoreCase(remote)) {
            states.put(CHANNEL_REMOTE_CONTROL_ENABLED, OnOffType.OFF);
        }
        if (states.values().stream().allMatch(state -> state == UnDefType.UNDEF)) {
            throw new CandyProtocolException("@text/protocol-error-fields");
        }
        return new BiancaSnapshot(states);
    }

    private State mapped(@Nullable String value) {
        return value == null ? UnDefType.UNDEF : new StringType(value);
    }

    private @Nullable Integer integer(JsonObject object, String field) {
        String value = primitive(object, field);
        if (value != null && value.matches("-?[0-9]+")) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private @Nullable String identifier(JsonObject object, String field) {
        String value = primitive(object, field);
        // Identifiers are scalar protocol codes, not arbitrary payload text or account information.
        return value != null && value.matches("[A-Za-z0-9_-]{1,64}") && !"true".equalsIgnoreCase(value)
                && !"false".equalsIgnoreCase(value) ? value : null;
    }

    private @Nullable String primitive(JsonObject object, String field) {
        JsonElement value = object.get(field);
        if (value instanceof JsonPrimitive primitive) {
            return primitive.getAsString();
        }
        return null;
    }

    private enum MachineMode {
        NO_STATE(0),
        SELECTING(1),
        RUNNING(2),
        PAUSED(3),
        DELAY_SELECTION(4),
        DELAYED(5),
        ERROR(6),
        FINISHED(7);

        private final int code;

        MachineMode(int code) {
            this.code = code;
        }

        private static Optional<MachineMode> fromCode(int code) {
            return Arrays.stream(values()).filter(mode -> mode.code == code).findFirst();
        }
    }

    private enum ProgramPhase {
        NONE(0),
        PREWASH(1),
        WASH(2),
        RINSE(3),
        LAST_RINSE(4),
        FINISHED(5),
        DRYING(6),
        ERROR(7),
        GOOD_NIGHT_PAUSE(9);

        private final int code;

        ProgramPhase(int code) {
            this.code = code;
        }

        private static Optional<ProgramPhase> fromCode(int code) {
            return Arrays.stream(values()).filter(phase -> phase.code == code).findFirst();
        }
    }
}
