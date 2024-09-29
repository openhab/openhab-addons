/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tr064.internal.util;

import static org.openhab.binding.tr064.internal.Tr064BindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tr064.internal.ChannelConfigException;
import org.openhab.binding.tr064.internal.Tr064RootHandler;
import org.openhab.binding.tr064.internal.config.Tr064BaseThingConfiguration;
import org.openhab.binding.tr064.internal.config.Tr064ChannelConfig;
import org.openhab.binding.tr064.internal.config.Tr064RootConfiguration;
import org.openhab.binding.tr064.internal.config.Tr064SubConfiguration;
import org.openhab.binding.tr064.internal.dto.config.ActionType;
import org.openhab.binding.tr064.internal.dto.config.ChannelTypeDescription;
import org.openhab.binding.tr064.internal.dto.config.ChannelTypeDescriptions;
import org.openhab.binding.tr064.internal.dto.config.ParameterType;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDServiceType;
import org.openhab.binding.tr064.internal.dto.scpd.service.SCPDActionType;
import org.openhab.binding.tr064.internal.dto.scpd.service.SCPDArgumentType;
import org.openhab.binding.tr064.internal.dto.scpd.service.SCPDDirection;
import org.openhab.binding.tr064.internal.dto.scpd.service.SCPDScpdType;
import org.openhab.binding.tr064.internal.dto.scpd.service.SCPDStateVariableType;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

/**
 * The {@link Util} is a set of helper functions
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);
    // cache XML content for 5s
    private static final ExpiringCacheMap<String, Object> XML_OBJECT_CACHE = new ExpiringCacheMap<>(
            Duration.ofMillis(3000));

    /**
     * read the channel config from the resource file (static initialization)
     *
     * @return a list of all available channel configurations
     */
    public static List<ChannelTypeDescription> readXMLChannelConfig() {
        try {
            InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("channels.xml");
            JAXBContext context = JAXBContext.newInstance(ChannelTypeDescriptions.class);
            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource(resource));
            Unmarshaller um = context.createUnmarshaller();
            JAXBElement<ChannelTypeDescriptions> root = um.unmarshal(xsr, ChannelTypeDescriptions.class);
            return root.getValue().getChannel();
        } catch (JAXBException | XMLStreamException e) {
            LOGGER.warn("Failed to read channel definitions", e);
            return List.of();
        }
    }

    /**
     * Extract an argument from an SCPD action definition
     *
     * @param scpdAction the action object
     * @param argumentName the argument's name
     * @param direction the direction (in or out)
     * @return the requested argument object
     * @throws ChannelConfigException if not found
     */
    private static SCPDArgumentType getArgument(SCPDActionType scpdAction, String argumentName, SCPDDirection direction)
            throws ChannelConfigException {
        return scpdAction.getArgumentList().stream()
                .filter(argument -> argument.getName().equals(argumentName) && argument.getDirection() == direction)
                .findFirst()
                .orElseThrow(() -> new ChannelConfigException(
                        (direction == SCPDDirection.IN ? "Set-Argument '" : "Get-Argument '") + argumentName
                                + "' not found"));
    }

    /**
     * Extract the related state variable from the service root for a given argument
     *
     * @param serviceRoot the service root object
     * @param scpdArgument the argument object
     * @return the related state variable object for this argument
     * @throws ChannelConfigException if not found
     */
    private static SCPDStateVariableType getStateVariable(SCPDScpdType serviceRoot, SCPDArgumentType scpdArgument)
            throws ChannelConfigException {
        return serviceRoot.getServiceStateTable().stream()
                .filter(stateVariable -> stateVariable.getName().equals(scpdArgument.getRelatedStateVariable()))
                .findFirst().orElseThrow(() -> new ChannelConfigException(
                        "StateVariable '" + scpdArgument.getRelatedStateVariable() + "' not found"));
    }

    /**
     * Extract an action from the service root
     *
     * @param serviceRoot the service root object
     * @param actionName the action name
     * @param actionType "Get-Action" or "Set-Action" (for exception string only)
     * @return the requested action object
     * @throws ChannelConfigException if not found
     */
    private static SCPDActionType getAction(SCPDScpdType serviceRoot, String actionName, String actionType)
            throws ChannelConfigException {
        return serviceRoot.getActionList().stream().filter(action -> actionName.equals(action.getName())).findFirst()
                .orElseThrow(() -> new ChannelConfigException(actionType + " '" + actionName + "' not found"));
    }

    /**
     * check and add available channels on a thing
     *
     * @param thing the Thing
     * @param thingBuilder the ThingBuilder (needs to be passed as editThing is only available in the handler)
     * @param scpdUtil the SCPDUtil instance for this thing
     * @param deviceId the device id for this thing
     * @param deviceType the (SCPD) device-type for this thing
     * @param channels a (mutable) channel list for storing all channels
     */
    public static void checkAvailableChannels(Thing thing, ThingHandlerCallback callback, ThingBuilder thingBuilder,
            SCPDUtil scpdUtil, String deviceId, String deviceType, Map<ChannelUID, Tr064ChannelConfig> channels) {
        Tr064BaseThingConfiguration thingConfig = Tr064RootHandler.SUPPORTED_THING_TYPES
                .contains(thing.getThingTypeUID()) ? thing.getConfiguration().as(Tr064RootConfiguration.class)
                        : thing.getConfiguration().as(Tr064SubConfiguration.class);
        channels.clear();
        CHANNEL_TYPES.stream().filter(channel -> deviceType.equals(channel.getService().getDeviceType()))
                .forEach(channelTypeDescription -> {
                    String channelId = channelTypeDescription.getName();
                    String serviceId = channelTypeDescription.getService().getServiceId();
                    Set<String> parameters = new HashSet<>();
                    try {
                        SCPDServiceType deviceService = scpdUtil.getDevice(deviceId)
                                .flatMap(device -> device.getServiceList().stream()
                                        .filter(service -> service.getServiceId().equals(serviceId)).findFirst())
                                .orElseThrow(() -> new ChannelConfigException("Service '" + serviceId + "' not found"));
                        SCPDScpdType serviceRoot = scpdUtil.getService(deviceService.getServiceId())
                                .orElseThrow(() -> new ChannelConfigException(
                                        "Service definition for '" + serviceId + "' not found"));
                        Tr064ChannelConfig channelConfig = new Tr064ChannelConfig(channelTypeDescription,
                                deviceService);

                        // get
                        boolean fixedValue = false;
                        ActionType getAction = channelTypeDescription.getGetAction();
                        if (getAction != null) {
                            String actionName = getAction.getName();
                            String argumentName = getAction.getArgument();
                            SCPDActionType scpdAction = getAction(serviceRoot, actionName, "Get-Action");
                            SCPDArgumentType scpdArgument = getArgument(scpdAction, argumentName, SCPDDirection.OUT);
                            SCPDStateVariableType relatedStateVariable = getStateVariable(serviceRoot, scpdArgument);
                            parameters.addAll(
                                    getAndCheckParameters(channelId, getAction, scpdAction, serviceRoot, thingConfig));
                            if (getAction.getParameter() != null && getAction.getParameter().getFixedValue() != null) {
                                fixedValue = true;
                            }
                            channelConfig.setGetAction(scpdAction);
                            channelConfig.setDataType(relatedStateVariable.getDataType());
                        }

                        // check set action
                        ActionType setAction = channelTypeDescription.getSetAction();
                        if (setAction != null) {
                            String actionName = setAction.getName();
                            String argumentName = setAction.getArgument();

                            SCPDActionType scpdAction = getAction(serviceRoot, actionName, "Set-Action");
                            if (argumentName != null) {
                                SCPDArgumentType scpdArgument = getArgument(scpdAction, argumentName, SCPDDirection.IN);
                                SCPDStateVariableType relatedStateVariable = getStateVariable(serviceRoot,
                                        scpdArgument);
                                if (channelConfig.getDataType().isEmpty()) {
                                    channelConfig.setDataType(relatedStateVariable.getDataType());
                                } else if (!channelConfig.getDataType().equals(relatedStateVariable.getDataType())) {
                                    throw new ChannelConfigException("dataType of set and get action are different");
                                }
                            }
                        }

                        // everything is available, create the channel
                        String channelType = Objects.requireNonNullElse(channelTypeDescription.getTypeId(), "");
                        ChannelTypeUID channelTypeUID = channelType.isBlank()
                                ? new ChannelTypeUID(BINDING_ID, channelTypeDescription.getName())
                                : new ChannelTypeUID(channelType);
                        if (parameters.isEmpty() || fixedValue) {
                            // we have no parameters, so create a single channel
                            ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
                            Channel channel = callback.createChannelBuilder(channelUID, channelTypeUID).build();
                            thingBuilder.withChannel(channel);
                            Tr064ChannelConfig channelConfig1 = new Tr064ChannelConfig(channelConfig);
                            if (fixedValue) {
                                channelConfig1.setParameter(parameters.iterator().next());
                            }
                            channels.put(channelUID, channelConfig1);
                        } else {
                            // create a channel for each parameter
                            parameters.forEach(parameter -> {
                                // remove comment: split parameter at '#', discard everything after that and remove
                                // trailing spaces
                                String rawParameter = parameter.split("#")[0].trim();
                                String normalizedParameter = UIDUtils.encode(rawParameter);
                                ChannelUID channelUID = new ChannelUID(thing.getUID(),
                                        channelId + "_" + normalizedParameter);
                                Channel channel = callback.createChannelBuilder(channelUID, channelTypeUID)
                                        .withLabel(channelTypeDescription.getLabel() + " " + parameter).build();
                                thingBuilder.withChannel(channel);
                                Tr064ChannelConfig channelConfig1 = new Tr064ChannelConfig(channelConfig);
                                channelConfig1.setParameter(rawParameter);
                                channels.put(channelUID, channelConfig1);
                            });
                        }
                    } catch (ChannelConfigException e) {
                        LOGGER.debug("Channel {} not available: {}", channelId, e.getMessage());
                    }
                });
    }

    private static Set<String> getAndCheckParameters(String channelId, ActionType action, SCPDActionType scpdAction,
            SCPDScpdType serviceRoot, Tr064BaseThingConfiguration thingConfig) throws ChannelConfigException {
        ParameterType parameter = action.getParameter();
        if (parameter == null) {
            return Set.of();
        }
        if (parameter.getFixedValue() != null) {
            return Set.of(parameter.getFixedValue());
        }
        // process list of thing parameters
        try {
            Set<String> parameters = new HashSet<>();

            // get parameters by reflection from thing config
            Field paramField = thingConfig.getClass().getField(parameter.getThingParameter());
            Object rawFieldValue = paramField.get(thingConfig);
            if ((rawFieldValue instanceof List<?> list)) {
                list.forEach(obj -> {
                    if (obj instanceof String string) {
                        parameters.add(string);
                    }
                });
            }

            // validate parameter against pattern
            String parameterPattern = parameter.getPattern();
            if (parameterPattern != null) {
                parameters.removeIf(param -> {
                    if (param.isBlank()) {
                        LOGGER.debug("Removing empty parameter while processing '{}'.", channelId);
                        return true;
                    } else if (!param.matches(parameterPattern)) {
                        LOGGER.warn("Removing '{}' while processing '{}', does not match pattern '{}', check config.",
                                param, channelId, parameterPattern);
                        return true;
                    } else {
                        return false;
                    }
                });
            }

            // validate parameter against SCPD (if not internal only)
            if (!parameter.isInternalOnly()) {
                SCPDArgumentType scpdArgument = getArgument(scpdAction, parameter.getName(), SCPDDirection.IN);
                SCPDStateVariableType relatedStateVariable = getStateVariable(serviceRoot, scpdArgument);
                if (relatedStateVariable.getAllowedValueRange() != null) {
                    int paramMin = relatedStateVariable.getAllowedValueRange().getMinimum();
                    int paramMax = relatedStateVariable.getAllowedValueRange().getMaximum();
                    int paramStep = relatedStateVariable.getAllowedValueRange().getStep();
                    Set<String> allowedValues = Stream.iterate(paramMin, i -> i <= paramMax, i -> i + paramStep)
                            .map(String::valueOf).collect(Collectors.toSet());
                    parameters.retainAll(allowedValues);
                }
            }

            // check we have at least one valid parameter left
            if (parameters.isEmpty()) {
                throw new IllegalArgumentException();
            }
            return parameters;
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            throw new ChannelConfigException("Could not get required parameter for channel '" + channelId
                    + "' from thing config (missing, empty or invalid)");
        }
    }

    public static Optional<String> getSOAPElement(SOAPMessage soapMessage, String elementName) {
        try {
            NodeList nodeList = soapMessage.getSOAPBody().getElementsByTagName(elementName);
            if (nodeList != null && nodeList.getLength() > 0) {
                return Optional.of(nodeList.item(0).getTextContent());
            }
        } catch (SOAPException e) {
            // if an error occurs, returning an empty Optional is fine
        }
        return Optional.empty();
    }

    /**
     * generic unmarshaller
     *
     * @param uri the uri of the XML file
     * @param clazz the class describing the XML file
     * @param timeout timeout in s
     * @return unmarshalling result
     */
    @SuppressWarnings("unchecked")
    public static <T> @Nullable T getAndUnmarshalXML(HttpClient httpClient, String uri, Class<T> clazz, int timeout) {
        try {
            T returnValue = (T) XML_OBJECT_CACHE.putIfAbsentAndGet(uri, () -> {
                try {
                    LOGGER.trace("Refreshing cache for '{}'", uri);
                    ContentResponse contentResponse = httpClient.newRequest(uri).timeout(timeout, TimeUnit.SECONDS)
                            .method(HttpMethod.GET).send();
                    byte[] response = contentResponse.getContent();
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("XML = {}", new String(response));
                    }
                    InputStream xml = new ByteArrayInputStream(response);

                    JAXBContext context = JAXBContext.newInstance(clazz);
                    XMLInputFactory xif = XMLInputFactory.newFactory();
                    xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
                    xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
                    XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource(xml));
                    Unmarshaller um = context.createUnmarshaller();
                    T newValue = um.unmarshal(xsr, clazz).getValue();
                    LOGGER.trace("Storing in cache {}", newValue);
                    return newValue;
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    LOGGER.debug("HTTP Failed to GET uri '{}': {}", uri, e.getMessage());
                    throw new IllegalArgumentException();
                } catch (JAXBException | XMLStreamException e) {
                    LOGGER.debug("Unmarshalling failed: {}", e.getMessage());
                    throw new IllegalArgumentException();
                }
            });
            LOGGER.trace("Returning from cache: {}", returnValue);
            return returnValue;
        } catch (IllegalArgumentException e) {
            // already logged
        }
        return null;
    }
}
