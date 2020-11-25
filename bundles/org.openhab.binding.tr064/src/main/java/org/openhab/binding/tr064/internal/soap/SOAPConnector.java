/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.xml.soap.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.tr064.internal.Tr064CommunicationException;
import org.openhab.binding.tr064.internal.config.Tr064ChannelConfig;
import org.openhab.binding.tr064.internal.dto.config.ActionType;
import org.openhab.binding.tr064.internal.dto.config.ChannelTypeDescription;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDServiceType;
import org.openhab.binding.tr064.internal.dto.scpd.service.SCPDActionType;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SOAPConnector} provides communication with a remote SOAP device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SOAPConnector {
    private static final int SOAP_TIMEOUT = 2000; // in ms
    private final Logger logger = LoggerFactory.getLogger(SOAPConnector.class);
    private final HttpClient httpClient;
    private final String endpointBaseURL;
    private final SOAPValueConverter soapValueConverter;

    public SOAPConnector(HttpClient httpClient, String endpointBaseURL) {
        this.httpClient = httpClient;
        this.endpointBaseURL = endpointBaseURL;
        this.soapValueConverter = new SOAPValueConverter(httpClient);
    }

    /**
     * prepare a SOAP request for an action request to a service
     *
     * @param service the service
     * @param soapAction the action to send
     * @param arguments arguments to send along with the request
     * @return a jetty Request containing the full SOAP message
     * @throws IOException if a problem while writing the SOAP message to the Request occurs
     * @throws SOAPException if a problem with creating the SOAP message occurs
     */
    private Request prepareSOAPRequest(SCPDServiceType service, String soapAction, Map<String, String> arguments)
            throws IOException, SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.setEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/");

        // SOAP body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement(soapAction, "u", service.getServiceType());
        arguments.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(argument -> {
            try {
                soapBodyElem.addChildElement(argument.getKey()).setTextContent(argument.getValue());
            } catch (SOAPException e) {
                logger.warn("Could not add {}:{} to SOAP Request: {}", argument.getKey(), argument.getValue(),
                        e.getMessage());
            }
        });

        // SOAP headers
        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", service.getServiceType() + "#" + soapAction);
        soapMessage.saveChanges();

        // create Request and add headers and content
        Request request = httpClient.newRequest(endpointBaseURL + service.getControlURL()).method(HttpMethod.POST);
        ((Iterator<MimeHeader>) soapMessage.getMimeHeaders().getAllHeaders())
                .forEachRemaining(header -> request.header(header.getName(), header.getValue()));
        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            soapMessage.writeTo(os);
            byte[] content = os.toByteArray();
            request.content(new BytesContentProvider(content));
        }

        return request;
    }

    /**
     * execute a SOAP request
     *
     * @param service the service to send the action to
     * @param soapAction the action itself
     * @param arguments arguments to send along with the request
     * @return the SOAPMessage answer from the remote host
     * @throws Tr064CommunicationException if an error occurs during the request
     */
    public synchronized SOAPMessage doSOAPRequest(SCPDServiceType service, String soapAction,
            Map<String, String> arguments) throws Tr064CommunicationException {
        try {
            Request request = prepareSOAPRequest(service, soapAction, arguments).timeout(SOAP_TIMEOUT,
                    TimeUnit.MILLISECONDS);
            if (logger.isTraceEnabled()) {
                request.getContent().forEach(buffer -> logger.trace("Request: {}", new String(buffer.array())));
            }

            ContentResponse response = request.send();
            if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                // retry once if authentication expired
                logger.trace("Re-Auth needed.");
                httpClient.getAuthenticationStore().clearAuthenticationResults();
                request = prepareSOAPRequest(service, soapAction, arguments).timeout(SOAP_TIMEOUT,
                        TimeUnit.MILLISECONDS);
                response = request.send();
            }
            try (final ByteArrayInputStream is = new ByteArrayInputStream(response.getContent())) {
                logger.trace("Received response: {}", response.getContentAsString());

                SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
                if (soapMessage.getSOAPBody().hasFault()) {
                    String soapError = getSOAPElement(soapMessage, "errorCode").orElse("unknown");
                    String soapReason = getSOAPElement(soapMessage, "errorDescription").orElse("unknown");
                    String error = String.format("HTTP-Response-Code %d (%s), SOAP-Fault: %s (%s)",
                            response.getStatus(), response.getReason(), soapError, soapReason);
                    throw new Tr064CommunicationException(error);
                }
                return soapMessage;
            }
        } catch (IOException | SOAPException | InterruptedException | TimeoutException | ExecutionException e) {
            throw new Tr064CommunicationException(e);
        }
    }

    /**
     * send a command to the remote device
     *
     * @param channelConfig the channel config containing all information
     * @param command the command to send
     */
    public void sendChannelCommandToDevice(Tr064ChannelConfig channelConfig, Command command) {
        soapValueConverter.getSOAPValueFromCommand(command, channelConfig.getDataType(),
                channelConfig.getChannelTypeDescription().getItem().getUnit()).ifPresentOrElse(value -> {
                    final ChannelTypeDescription channelTypeDescription = channelConfig.getChannelTypeDescription();
                    final SCPDServiceType service = channelConfig.getService();
                    logger.debug("Sending {} as {} to {}/{}", command, value, service.getServiceId(),
                            channelTypeDescription.getSetAction().getName());
                    try {
                        Map<String, String> arguments = new HashMap<>();
                        if (channelTypeDescription.getSetAction().getArgument() != null) {
                            arguments.put(channelTypeDescription.getSetAction().getArgument(), value);
                        }
                        String parameter = channelConfig.getParameter();
                        if (parameter != null) {
                            arguments.put(
                                    channelConfig.getChannelTypeDescription().getGetAction().getParameter().getName(),
                                    parameter);
                        }
                        doSOAPRequest(service, channelTypeDescription.getSetAction().getName(), arguments);
                    } catch (Tr064CommunicationException e) {
                        logger.warn("Could not send command {}: {}", command, e.getMessage());
                    }
                }, () -> logger.warn("Could not convert {} to SOAP value", command));
    }

    /**
     * get a value from the remote device - updates state cache for all possible channels
     *
     * @param channelConfig the channel config containing all information
     * @param channelConfigMap map of all channels in the device
     * @param stateCache the ExpiringCacheMap for states of the device
     * @return the value for the requested channel
     */
    public State getChannelStateFromDevice(final Tr064ChannelConfig channelConfig,
            Map<ChannelUID, Tr064ChannelConfig> channelConfigMap, ExpiringCacheMap<ChannelUID, State> stateCache) {
        try {
            final SCPDActionType getAction = channelConfig.getGetAction();
            if (getAction == null) {
                // channel has no get action, return a default
                switch (channelConfig.getDataType()) {
                    case "boolean":
                        return OnOffType.OFF;
                    case "string":
                        return StringType.EMPTY;
                    default:
                        return UnDefType.UNDEF;
                }
            }

            // get value(s) from remote device
            Map<String, String> arguments = new HashMap<>();
            String parameter = channelConfig.getParameter();
            ActionType action = channelConfig.getChannelTypeDescription().getGetAction();
            if (parameter != null && !action.getParameter().isInternalOnly()) {
                arguments.put(action.getParameter().getName(), parameter);
            }
            SOAPMessage soapResponse = doSOAPRequest(channelConfig.getService(), getAction.getName(), arguments);

            String argumentName = channelConfig.getChannelTypeDescription().getGetAction().getArgument();
            // find all other channels with the same action that are already in cache, so we can update them
            Map<ChannelUID, Tr064ChannelConfig> channelsInRequest = channelConfigMap.entrySet().stream()
                    .filter(map -> getAction.equals(map.getValue().getGetAction())
                            && stateCache.containsKey(map.getKey())
                            && !argumentName
                                    .equals(map.getValue().getChannelTypeDescription().getGetAction().getArgument()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            channelsInRequest
                    .forEach(
                            (channelUID,
                                    channelConfig1) -> soapValueConverter
                                            .getStateFromSOAPValue(soapResponse,
                                                    channelConfig1.getChannelTypeDescription().getGetAction()
                                                            .getArgument(),
                                                    channelConfig1)
                                            .ifPresent(state -> stateCache.putValue(channelUID, state)));

            return soapValueConverter.getStateFromSOAPValue(soapResponse, argumentName, channelConfig)
                    .orElseThrow(() -> new Tr064CommunicationException("failed to transform '"
                            + channelConfig.getChannelTypeDescription().getGetAction().getArgument() + "'"));
        } catch (Tr064CommunicationException e) {
            logger.info("Failed to get {}: {}", channelConfig, e.getMessage());
            return UnDefType.UNDEF;
        }
    }
}
