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
package org.openhab.binding.knx.internal.rest;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.client.KNXClient;
import org.openhab.binding.knx.internal.client.OutboundSpec;
import org.openhab.binding.knx.internal.config.BridgeConfiguration;
import org.openhab.binding.knx.internal.dpt.DPTUnits;
import org.openhab.binding.knx.internal.dpt.DPTUtil;
import org.openhab.binding.knx.internal.dpt.ValueEncoder;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.rest.dto.GroupTelegramDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupTelegramRequestDTO;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.calimero.GroupAddress;
import io.calimero.KNXException;
import io.calimero.KNXFormatException;
import io.calimero.datapoint.StateDP;

/**
 * Sends one group read or one group write for the page's Group Monitor, through the bridge's own client as a
 * channel does. Nothing goes out unless the bridge's setting {@code groupMonitorSend} is on, the bridge is online,
 * every field of the request is understood and the bridge's limit of telegrams is not used up. A refusal never
 * repeats request text, and the log never holds the value.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class GroupTelegramSender {
    static final int LIMIT = 5;
    static final long WINDOW_MILLIS = 10_000;
    static final String READ = "read";
    static final String WRITE = "write";
    static final String SENT = "sent";
    static final String QUEUED = "queued";
    /** The type a read request is sent with when neither the request nor the project names one. */
    static final String READ_FALLBACK_DPT = "1.001";
    static final int MAX_VALUE_LENGTH = 200;
    /** A number is written with at most this many characters, and its power of ten is within this bound. */
    static final int MAX_NUMBER_LENGTH = 40;
    static final int MAX_EXPONENT = 400;

    private static final Pattern DPT = Pattern.compile("[1-9][0-9]{0,2}\\.[0-9]{3,5}");
    private static final Pattern ADDRESS = Pattern.compile("[0-9]{1,2}/[0-9]/[0-9]{1,3}");

    /** How a value text is read, in this order; the first type the data point type allows and takes wins. */
    private static final List<Map.Entry<Class<? extends Type>, Function<String, Type>>> PARSERS = List.of(
            Map.entry(OnOffType.class, OnOffType::valueOf), Map.entry(UpDownType.class, UpDownType::valueOf),
            Map.entry(OpenClosedType.class, OpenClosedType::valueOf),
            Map.entry(StopMoveType.class, StopMoveType::valueOf),
            Map.entry(IncreaseDecreaseType.class, IncreaseDecreaseType::valueOf),
            Map.entry(HSBType.class, HSBType::valueOf), Map.entry(DecimalType.class, DecimalType::valueOf),
            Map.entry(QuantityType.class, QuantityType::valueOf), Map.entry(PercentType.class, PercentType::valueOf),
            Map.entry(DateTimeType.class, DateTimeType::valueOf), Map.entry(StringType.class, StringType::valueOf));

    private static final Set<Class<? extends Type>> NUMBERS = Set.of(DecimalType.class, QuantityType.class,
            PercentType.class);

    private final Logger logger = LoggerFactory.getLogger(GroupTelegramSender.class);
    private final Clock clock;
    private final Map<ThingUID, Deque<Long>> sendTimes = new ConcurrentHashMap<>();

    GroupTelegramSender(Clock clock) {
        this.clock = clock;
    }

    /** {@return whether the bridge's setting lets the page send} */
    static boolean allowed(KNXBridgeBaseThingHandler handler) {
        return handler.getThing().getConfiguration().as(BridgeConfiguration.class).isGroupMonitorSend();
    }

    /**
     * @param projectDpt the data point type the project gives the address, for a read request without one
     */
    GroupTelegramDTO send(KNXBridgeBaseThingHandler handler, @Nullable GroupTelegramRequestDTO request,
            Function<GroupAddress, @Nullable String> projectDpt) {
        if (!allowed(handler)) {
            throw new KnxRestException(KnxRestError.SEND_NOT_ALLOWED);
        }
        if (request == null) {
            throw KnxRestException.badInput("action", "a telegram request is needed");
        }
        String action = request.action;
        boolean write = WRITE.equals(action);
        if (!write && !READ.equals(action)) {
            throw KnxRestException.badInput("action", "not read or write");
        }
        GroupAddress address = address(request.address);
        String dpt = request.dpt;
        if (dpt != null && dpt.isEmpty()) {
            dpt = null;
        }
        if (dpt != null && !isKnown(dpt) || write && dpt == null) {
            throw new KnxRestException(KnxRestError.UNKNOWN_DATA_TYPE);
        }
        Type value = write && dpt != null ? value(request.value, dpt) : null;
        ThingUID bridge = handler.getThing().getUID();
        KNXClient client = handler.client();
        if (handler.getThing().getStatus() != ThingStatus.ONLINE || !client.isConnected()) {
            throw new KnxRestException(KnxRestError.BRIDGE_OFFLINE);
        }
        if (!take(bridge)) {
            throw new KnxRestException(KnxRestError.TOO_MANY_TELEGRAMS);
        }
        GroupTelegramDTO answer = new GroupTelegramDTO();
        if (value != null && dpt != null) {
            boolean sent;
            try {
                sent = client.writeToKNX(new Spec(dpt, address, value));
            } catch (KNXException | RuntimeException e) {
                logger.info("KNX page: a group write to {} through {} failed ({})", address, bridge,
                        e.getClass().getSimpleName());
                throw new KnxRestException(KnxRestError.SEND_FAILED);
            }
            if (!sent) {
                // the connection went away after the check above
                throw new KnxRestException(KnxRestError.BRIDGE_OFFLINE);
            }
            logger.info("KNX page: group write to {} sent through {}", address, bridge);
            answer.action = WRITE;
            answer.state = SENT;
        } else {
            String readDpt = dpt != null ? dpt : projectDpt.apply(address);
            client.readOnce(
                    new StateDP(address, "", 0, readDpt != null && isKnown(readDpt) ? readDpt : READ_FALLBACK_DPT));
            logger.info("KNX page: group read of {} queued for {}", address, bridge);
            answer.action = READ;
            answer.state = QUEUED;
        }
        return answer;
    }

    private static GroupAddress address(@Nullable String text) {
        if (text != null && ADDRESS.matcher(text).matches()) {
            try {
                GroupAddress address = new GroupAddress(text);
                if (address.getRawAddress() != 0) {
                    return address;
                }
            } catch (KNXFormatException | RuntimeException e) {
                // not an address: answered below
            }
        }
        throw new KnxRestException(KnxRestError.BAD_GROUP_ADDRESS);
    }

    private static boolean isKnown(String dpt) {
        return DPT.matcher(dpt).matches() && !DPTUtil.getAllowedTypes(dpt).isEmpty() && ValueEncoder.isEncodable(dpt);
    }

    /** The value as the first type that the data point type allows, reads the text and takes it in its range. */
    private static Type value(@Nullable String text, String dpt) {
        if (text != null && !text.isBlank() && text.length() <= MAX_VALUE_LENGTH) {
            Set<Class<? extends Type>> allowed = DPTUtil.getAllowedTypes(dpt);
            // every 1.x type is one bit: ON and OFF are its 1 and 0 whatever words the type has
            boolean oneBit = dpt.startsWith("1.");
            String trimmed = text.trim();
            for (Map.Entry<Class<? extends Type>, Function<String, Type>> parser : PARSERS) {
                if (!allowed.contains(parser.getKey()) && !(oneBit && parser.getKey() == OnOffType.class)) {
                    continue;
                }
                // the number parsers take a comma as a thousands mark: "21,5" would go out as 215
                if (NUMBERS.contains(parser.getKey()) && (trimmed.indexOf(',') >= 0 || !isShortNumber(trimmed))) {
                    continue;
                }
                try {
                    Type value = parser.getValue().apply(trimmed);
                    if (!isValueOf(dpt, value)) {
                        continue;
                    }
                    String encoded = ValueEncoder.encode(value, dpt);
                    if (encoded != null && ValueEncoder.accepts(dpt, encoded)) {
                        return value;
                    }
                } catch (RuntimeException e) {
                    // not a text of this type, or a unit the unit library cannot work with: the next type
                }
            }
        }
        throw new KnxRestException(KnxRestError.BAD_VALUE);
    }

    /**
     * Whether the number at the start of the text is short enough to be worked with: the encoder writes a number
     * out digit by digit, so a huge power of ten would cost memory in proportion.
     */
    static boolean isShortNumber(String text) {
        int end = 0;
        int exponentAt = -1;
        for (; end < text.length(); end++) {
            char c = text.charAt(end);
            if (exponentAt < 0 && (c == 'e' || c == 'E') && end > 0) {
                exponentAt = end;
            } else if (!Character.isDigit(c) && c != '.' && c != '+' && c != '-') {
                break;
            }
        }
        if (exponentAt >= 0 && (exponentAt + 1 == end || !isSignOrDigit(text.charAt(exponentAt + 1)))) {
            // an "e" that begins a unit
            end = exponentAt;
            exponentAt = -1;
        }
        if (end > MAX_NUMBER_LENGTH) {
            return false;
        }
        if (exponentAt >= 0) {
            int exponent = 0;
            for (int i = exponentAt + 1; i < end && exponent <= MAX_EXPONENT; i++) {
                int digit = Character.digit(text.charAt(i), 10);
                if (digit >= 0) {
                    exponent = exponent * 10 + digit;
                }
            }
            return exponent <= MAX_EXPONENT;
        }
        return true;
    }

    private static boolean isSignOrDigit(char c) {
        return Character.isDigit(c) || c == '+' || c == '-';
    }

    /**
     * Whether the value is one the data point type has. The encoder maps some numbers to another value before the
     * library sees them, and drops a unit the type has no use for; a channel keeps that behaviour, the page's send
     * does not.
     */
    private static boolean isValueOf(String dpt, Type value) {
        if (value instanceof QuantityType<?> quantity) {
            return isShort(quantity.toBigDecimal()) && DPTUnits.getUnitForDpt(dpt) != null
                    && ValueEncoder.hasUnitOf(quantity, dpt);
        }
        if (value instanceof DecimalType decimal) {
            BigDecimal number = decimal.toBigDecimal();
            return isShort(number) && isNumberOf(dpt, number);
        }
        return true;
    }

    private static boolean isShort(BigDecimal number) {
        long exponent = (long) number.precision() - number.scale() - 1;
        return number.precision() <= MAX_NUMBER_LENGTH && Math.abs(exponent) <= MAX_EXPONENT;
    }

    private static boolean isNumberOf(String dpt, BigDecimal number) {
        boolean sceneAb = "1.022".equals(dpt);
        boolean controlled = dpt.startsWith("2.");
        boolean scene = dpt.startsWith("18.");
        if (!sceneAb && !controlled && !scene) {
            return true;
        }
        int whole;
        try {
            whole = number.intValueExact();
        } catch (ArithmeticException e) {
            return false;
        }
        if (sceneAb) {
            return whole == 0 || whole == 1;
        }
        if (controlled) {
            return whole >= 0 && whole <= 3;
        }
        return whole >= 0 && whole <= 63 || whole >= 128 && whole <= 191;
    }

    /** Counts a telegram of the bridge if fewer than {@link #LIMIT} went out in the last {@link #WINDOW_MILLIS}. */
    private boolean take(ThingUID bridge) {
        Deque<Long> times = Objects.requireNonNull(sendTimes.computeIfAbsent(bridge, uid -> new ArrayDeque<>()));
        long now = clock.millis();
        synchronized (times) {
            while (!times.isEmpty() && now - times.peekFirst() >= WINDOW_MILLIS) {
                times.removeFirst();
            }
            if (times.size() >= LIMIT) {
                return false;
            }
            times.addLast(now);
            return true;
        }
    }

    private record Spec(String dpt, GroupAddress groupAddress, Type value) implements OutboundSpec {
        @Override
        public String getDPT() {
            return dpt;
        }

        @Override
        public GroupAddress getGroupAddress() {
            return groupAddress;
        }

        @Override
        public Type getValue() {
            return value;
        }

        @Override
        public boolean matchesDestination(GroupAddress address) {
            return groupAddress.equals(address);
        }
    }
}
