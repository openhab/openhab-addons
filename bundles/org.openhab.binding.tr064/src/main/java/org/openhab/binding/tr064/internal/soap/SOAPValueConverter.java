/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.tr064.internal.soap;

import static org.openhab.binding.tr064.internal.util.Util.getSOAPElement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.xml.soap.SOAPMessage;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.tr064.internal.config.Tr064ChannelConfig;
import org.openhab.binding.tr064.internal.dto.additions.Call;
import org.openhab.binding.tr064.internal.dto.additions.Root;
import org.openhab.binding.tr064.internal.util.Util;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link SOAPValueConverter} converts SOAP values and openHAB states
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SOAPValueConverter {
    private final Logger logger = LoggerFactory.getLogger(SOAPValueConverter.class);
    private final HttpClient httpClient;

    public SOAPValueConverter(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * convert an openHAB command to a SOAP value
     *
     * @param command the command to be converted
     * @param dataType the datatype to send
     * @param unit if available, the unit of the converted value
     * @return a string optional containing the converted value
     */
    public Optional<String> getSOAPValueFromCommand(Command command, String dataType, String unit) {
        if (dataType.isEmpty()) {
            // we don't have data to send
            return Optional.of("");
        }
        if (command instanceof QuantityType) {
            QuantityType<?> value = (unit.isEmpty()) ? ((QuantityType<?>) command)
                    : ((QuantityType<?>) command).toUnit(unit);
            if (value == null) {
                logger.warn("Could not convert {} to unit {}", command, unit);
                return Optional.empty();
            }
            switch (dataType) {
                case "ui1":
                case "ui2":
                    return Optional.of(String.valueOf(value.shortValue()));
                case "i4":
                case "ui4":
                    return Optional.of(String.valueOf(value.intValue()));
                default:
            }
        } else if (command instanceof DecimalType) {
            BigDecimal value = ((DecimalType) command).toBigDecimal();
            switch (dataType) {
                case "ui1":
                case "ui2":
                    return Optional.of(String.valueOf(value.shortValue()));
                case "i4":
                case "ui4":
                    return Optional.of(String.valueOf(value.intValue()));
                default:
            }
        } else if (command instanceof StringType) {
            if (dataType.equals("string")) {
                return Optional.of(command.toString());
            }
        } else if (command instanceof OnOffType) {
            if (dataType.equals("boolean")) {
                return Optional.of(OnOffType.ON.equals(command) ? "1" : "0");
            }
        }
        return Optional.empty();
    }

    /**
     * convert the value from a SOAP message to an openHAB value
     *
     * @param soapMessage the inbound SOAP message
     * @param element the element that needs to be extracted
     * @param channelConfig the channel config containing additional information (if null a data-type "string" and
     *            missing unit is assumed)
     * @return an Optional of State containing the converted value
     */
    public Optional<State> getStateFromSOAPValue(SOAPMessage soapMessage, String element,
            @Nullable Tr064ChannelConfig channelConfig) {
        String dataType = channelConfig != null ? channelConfig.getDataType() : "string";
        String unit = channelConfig != null ? channelConfig.getChannelTypeDescription().getItem().getUnit() : "";

        return getSOAPElement(soapMessage, element).map(rawValue -> {
            // map rawValue to State
            switch (dataType) {
                case "boolean":
                    return rawValue.equals("0") ? OnOffType.OFF : OnOffType.ON;
                case "string":
                    return new StringType(rawValue);
                case "ui1":
                case "ui2":
                case "i4":
                case "ui4":
                    if (!unit.isEmpty()) {
                        return new QuantityType<>(rawValue + " " + unit);
                    } else {
                        return new DecimalType(rawValue);
                    }
                default:
                    return null;
            }
        }).map(state -> {
            // check if we need post processing
            if (channelConfig == null
                    || channelConfig.getChannelTypeDescription().getGetAction().getPostProcessor() == null) {
                return state;
            }
            String postProcessor = channelConfig.getChannelTypeDescription().getGetAction().getPostProcessor();
            try {
                Method method = SOAPValueConverter.class.getDeclaredMethod(postProcessor, State.class,
                        Tr064ChannelConfig.class);
                Object o = method.invoke(this, state, channelConfig);
                if (o instanceof State) {
                    return (State) o;
                }
            } catch (NoSuchMethodException | IllegalAccessException e) {
                logger.warn("Postprocessor {} not found, this most likely is a programming error", postProcessor, e);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                logger.info("Postprocessor {} failed: {}", postProcessor,
                        cause != null ? cause.getMessage() : e.getMessage());
            }
            return null;
        }).or(Optional::empty);
    }

    /**
     * post processor to map mac device signal strength to system.signal-strength 0-4
     *
     * @param state with signalStrength
     * @param channelConfig channel config of the mac signal strength
     * @return the mapped system.signal-strength in range 0-4
     */
    @SuppressWarnings("unused")
    private State processMacSignalStrength(State state, Tr064ChannelConfig channelConfig) {
        State mappedSignalStrength = UnDefType.UNDEF;
        DecimalType currentStateValue = state.as(DecimalType.class);

        if (currentStateValue != null) {
            if (currentStateValue.intValue() > 80) {
                mappedSignalStrength = new DecimalType(4);
            } else if (currentStateValue.intValue() > 60) {
                mappedSignalStrength = new DecimalType(3);
            } else if (currentStateValue.intValue() > 40) {
                mappedSignalStrength = new DecimalType(2);
            } else if (currentStateValue.intValue() > 20) {
                mappedSignalStrength = new DecimalType(1);
            } else {
                mappedSignalStrength = new DecimalType(0);
            }
        }

        return mappedSignalStrength;
    }

    /**
     * post processor for decibel values (which are served as deca decibel)
     *
     * @param state the channel value in deca decibel
     * @param channelConfig channel config of the channel
     * @return the state converted to decibel
     */
    @SuppressWarnings("unused")
    private State processDecaDecibel(State state, Tr064ChannelConfig channelConfig) {
        Float value = state.as(DecimalType.class).floatValue() / 10;

        return new QuantityType(value, Units.DECIBEL);
    }

    /**
     * post processor for answering machine new messages channel
     *
     * @param state the message list URL
     * @param channelConfig channel config of the TAM new message channel
     * @return the number of new messages
     * @throws PostProcessingException if the message list could not be retrieved
     */
    @SuppressWarnings("unused")
    private State processTamListURL(State state, Tr064ChannelConfig channelConfig) throws PostProcessingException {
        try {
            ContentResponse response = httpClient.newRequest(state.toString()).timeout(1000, TimeUnit.MILLISECONDS)
                    .send();
            String responseContent = response.getContentAsString();
            int messageCount = responseContent.split("<New>1</New>").length - 1;

            return new DecimalType(messageCount);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new PostProcessingException("Failed to get TAM list from URL " + state.toString(), e);
        }
    }

    /**
     * post processor for missed calls
     *
     * @param state the call list URL
     * @param channelConfig channel config of the missed call channel (contains day number)
     * @return the number of missed calls
     * @throws PostProcessingException if call list could not be retrieved
     */
    @SuppressWarnings("unused")
    private State processMissedCalls(State state, Tr064ChannelConfig channelConfig) throws PostProcessingException {
        return processCallList(state, channelConfig.getParameter(), CallListType.MISSED_COUNT);
    }

    /**
     * post processor for inbound calls
     *
     * @param state the call list URL
     * @param channelConfig channel config of the inbound call channel (contains day number)
     * @return the number of inbound calls
     * @throws PostProcessingException if call list could not be retrieved
     */
    @SuppressWarnings("unused")
    private State processInboundCalls(State state, Tr064ChannelConfig channelConfig) throws PostProcessingException {
        return processCallList(state, channelConfig.getParameter(), CallListType.INBOUND_COUNT);
    }

    /**
     * post processor for rejected calls
     *
     * @param state the call list URL
     * @param channelConfig channel config of the rejected call channel (contains day number)
     * @return the number of rejected calls
     * @throws PostProcessingException if call list could not be retrieved
     */
    @SuppressWarnings("unused")
    private State processRejectedCalls(State state, Tr064ChannelConfig channelConfig) throws PostProcessingException {
        return processCallList(state, channelConfig.getParameter(), CallListType.REJECTED_COUNT);
    }

    /**
     * post processor for outbound calls
     *
     * @param state the call list URL
     * @param channelConfig channel config of the outbound call channel (contains day number)
     * @return the number of outbound calls
     * @throws PostProcessingException if call list could not be retrieved
     */
    @SuppressWarnings("unused")
    private State processOutboundCalls(State state, Tr064ChannelConfig channelConfig) throws PostProcessingException {
        return processCallList(state, channelConfig.getParameter(), CallListType.OUTBOUND_COUNT);
    }

    /**
     * post processor for JSON call list
     *
     * @param state the call list URL
     * @param channelConfig channel config of the call list channel (contains day number)
     * @return caller list in JSON format
     * @throws PostProcessingException if call list could not be retrieved
     */
    @SuppressWarnings("unused")
    private State processCallListJSON(State state, Tr064ChannelConfig channelConfig) throws PostProcessingException {
        return processCallList(state, channelConfig.getParameter(), CallListType.JSON_LIST);
    }

    /**
     * internal helper for call list post processors
     *
     * @param state the call list URL
     * @param days number of days to get
     * @param type type of call (2=missed 1=inbound 4=rejected 3=outbund)
     * @return the quantity of calls of the given type within the given number of days
     * @throws PostProcessingException if the call list could not be retrieved
     */
    private State processCallList(State state, @Nullable String days, CallListType type)
            throws PostProcessingException {
        Root callListRoot = Util.getAndUnmarshalXML(httpClient, state.toString() + "&days=" + days, Root.class);
        if (callListRoot == null) {
            throw new PostProcessingException("Failed to get call list from URL " + state.toString());
        }
        List<Call> calls = callListRoot.getCall();
        switch (type) {
            case INBOUND_COUNT:
            case MISSED_COUNT:
            case OUTBOUND_COUNT:
            case REJECTED_COUNT:
                long callCount = calls.stream().filter(call -> type.typeString().equals(call.getType())).count();
                return new DecimalType(callCount);
            case JSON_LIST:
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssX").serializeNulls().create();
                List<CallListEntry> callListEntries = calls.stream().map(CallListEntry::new)
                        .collect(Collectors.toList());
                return new StringType(gson.toJson(callListEntries));
        }
        return UnDefType.UNDEF;
    }
}
