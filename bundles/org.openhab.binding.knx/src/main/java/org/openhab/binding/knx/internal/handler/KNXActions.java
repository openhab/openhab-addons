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
package org.openhab.binding.knx.internal.handler;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.client.KNXClient;
import org.openhab.binding.knx.internal.client.OutboundSpec;
import org.openhab.binding.knx.internal.dpt.DPTUtil;
import org.openhab.binding.knx.internal.dpt.ValueEncoder;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
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
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Type;
import org.openhab.core.types.TypeParser;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.calimero.GroupAddress;
import io.calimero.KNXException;
import io.calimero.KNXFormatException;
import io.calimero.datapoint.CommandDP;
import io.calimero.dptxlator.DptId;
import io.calimero.dptxlator.TranslatorTypes;

/**
 * Rule actions to write and read any group address through a KNX bridge.
 *
 * @author Stamate Viorel - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = KNXActions.class)
@ThingActionsScope(name = "knx")
@NonNullByDefault
public class KNXActions implements ThingActions {

    // most specific first; DecimalType before PercentType keeps fractions that PercentType would truncate
    private static final List<Class<? extends Type>> PARSE_ORDER = List.of(OnOffType.class, UpDownType.class,
            OpenClosedType.class, StopMoveType.class, IncreaseDecreaseType.class, HSBType.class, DateTimeType.class,
            DecimalType.class, PercentType.class, QuantityType.class, StringType.class);

    private final Logger logger = LoggerFactory.getLogger(KNXActions.class);

    private @Nullable KNXBridgeBaseThingHandler handler;

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof KNXBridgeBaseThingHandler bridgeHandler) {
            this.handler = bridgeHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/actionWriteGroupLabel", description = "@text/actionWriteGroupDesc")
    public @ActionOutput(type = "java.lang.Boolean", label = "@text/actionOutputSentLabel") boolean writeGroup(
            @ActionInput(name = "groupAddress", label = "@text/actionInputGroupAddressLabel", required = true) String groupAddress,
            @ActionInput(name = "dpt", label = "@text/actionInputDptLabel", required = true) String dpt,
            @ActionInput(name = "value", label = "@text/actionInputValueLabel", required = true) String value) {
        KNXClient client = connectedClient();
        GroupAddress ga = parseGroupAddress(groupAddress);
        if (client == null || ga == null) {
            return false;
        }
        Type type = parseValue(dpt, value);
        if (type == null) {
            logger.warn("Value '{}' cannot be sent as DPT '{}' to {}", value, dpt, ga);
            return false;
        }
        try {
            client.writeToKNX(new GroupWrite(ga, dpt, type));
            return true;
        } catch (KNXException e) {
            logger.warn("Writing '{}' to {} failed: {}", value, ga, e.getMessage());
            return false;
        }
    }

    @RuleAction(label = "@text/actionReadGroupLabel", description = "@text/actionReadGroupDesc")
    public @ActionOutput(type = "java.lang.Boolean", label = "@text/actionOutputSentLabel") boolean readGroup(
            @ActionInput(name = "groupAddress", label = "@text/actionInputGroupAddressLabel", required = true) String groupAddress,
            @ActionInput(name = "dpt", label = "@text/actionInputDptLabel", required = true) String dpt) {
        KNXClient client = connectedClient();
        GroupAddress ga = parseGroupAddress(groupAddress);
        if (client == null || ga == null) {
            return false;
        }
        if (DPTUtil.getAllowedTypes(dpt).isEmpty() || !hasTranslator(dpt)) {
            logger.warn("DPT '{}' is not supported by the KNX binding", dpt);
            return false;
        }
        client.readDatapoint(new CommandDP(ga, "rule action", 0, dpt));
        return true;
    }

    public static boolean writeGroup(ThingActions actions, String groupAddress, String dpt, String value) {
        return ((KNXActions) actions).writeGroup(groupAddress, dpt, value);
    }

    public static boolean readGroup(ThingActions actions, String groupAddress, String dpt) {
        return ((KNXActions) actions).readGroup(groupAddress, dpt);
    }

    private @Nullable KNXClient connectedClient() {
        KNXBridgeBaseThingHandler handler = this.handler;
        if (handler == null || !handler.getClient().isConnected()) {
            logger.warn("KNX bridge is not connected, action skipped");
            return null;
        }
        return handler.getClient();
    }

    private @Nullable GroupAddress parseGroupAddress(String groupAddress) {
        try {
            return new GroupAddress(groupAddress);
        } catch (KNXFormatException e) {
            logger.warn("'{}' is not a valid group address", groupAddress);
            return null;
        }
    }

    /**
     * Parses a value given as text into the first openHAB type that the DPT accepts and that encodes to a valid
     * telegram.
     */
    static @Nullable Type parseValue(String dpt, String value) {
        Set<Class<? extends Type>> allowed = DPTUtil.getAllowedTypes(dpt);
        for (Class<? extends Type> typeClass : PARSE_ORDER) {
            if (!allowed.contains(typeClass)) {
                continue;
            }
            Type type = TypeParser.parseType(typeClass.getSimpleName(), value);
            if (type != null && isEncodable(type, dpt)) {
                return type;
            }
        }
        return null;
    }

    // ValueEncoder passes text through unchecked, so let the translator used for sending judge it
    private static boolean isEncodable(Type type, String dpt) {
        String encoded = ValueEncoder.encode(type, dpt);
        if (encoded == null) {
            return false;
        }
        try {
            TranslatorTypes.createTranslator(DptId.from(DPTUtil.NORMALIZED_DPT.getOrDefault(dpt, dpt)), encoded);
            return true;
        } catch (KNXException | RuntimeException e) {
            return false;
        }
    }

    // "9" passes the binding's DPT pattern, but Calimero needs the sub number to decode the answer
    private static boolean hasTranslator(String dpt) {
        try {
            TranslatorTypes.createTranslator(DptId.from(DPTUtil.NORMALIZED_DPT.getOrDefault(dpt, dpt)));
            return true;
        } catch (KNXException | RuntimeException e) {
            return false;
        }
    }

    private record GroupWrite(GroupAddress ga, String dpt, Type value) implements OutboundSpec {
        @Override
        public String getDPT() {
            return dpt;
        }

        @Override
        public GroupAddress getGroupAddress() {
            return ga;
        }

        @Override
        public Type getValue() {
            return value;
        }

        @Override
        public boolean matchesDestination(GroupAddress groupAddress) {
            return ga.equals(groupAddress);
        }
    }
}
