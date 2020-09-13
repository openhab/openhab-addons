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
package org.openhab.binding.tr064.internal.util;

import static org.openhab.binding.tr064.internal.Tr064BindingConstants.*;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tr064.internal.ChannelConfigException;
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
import org.openhab.binding.tr064.internal.dto.scpd.service.SCPDScpdType;
import org.openhab.binding.tr064.internal.dto.scpd.service.SCPDStateVariableType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
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

    public static List<ChannelTypeDescription> readXMLChannelConfig() {
        try {
            InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("channels.xml");
            JAXBContext context = JAXBContext.newInstance(ChannelTypeDescriptions.class);
            Unmarshaller um = context.createUnmarshaller();
            JAXBElement<ChannelTypeDescriptions> root = um.unmarshal(new StreamSource(resource),
                    ChannelTypeDescriptions.class);
            return root.getValue().getChannel();
        } catch (JAXBException e) {
            LOGGER.warn("Failed to read channel definitions", e);
            return Collections.emptyList();
        }
    }

    public static ThingBuilder checkAvailableChannels(Thing thing, ThingBuilder thingBuilder, SCPDUtil scpdUtil,
            String deviceId, String deviceType, Map<ChannelUID, Tr064ChannelConfig> channels) {
        Tr064BaseThingConfiguration thingConfig = thing.getThingTypeUID().equals(THING_TYPE_FRITZBOX)
                ? thing.getConfiguration().as(Tr064RootConfiguration.class)
                : thing.getConfiguration().as(Tr064SubConfiguration.class);
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
                        ActionType getAction = channelTypeDescription.getGetAction();
                        if (getAction != null) {
                            String actionName = getAction.getName();
                            String argumentName = getAction.getArgument();
                            SCPDActionType scpdAction = serviceRoot.getActionList().stream()
                                    .filter(action -> actionName.equals(action.getName())).findFirst()
                                    .orElseThrow(() -> new ChannelConfigException(
                                            "Get-Action '" + actionName + "' not found"));
                            SCPDArgumentType scpdArgument = scpdAction.getArgumentList().stream()
                                    .filter(argument -> argument.getName().equals(argumentName)
                                            && argument.getDirection().equals("out"))
                                    .findFirst().orElseThrow(() -> new ChannelConfigException(
                                            "Get-Argument '" + argumentName + "' not found"));
                            SCPDStateVariableType relatedStateVariable = serviceRoot.getServiceStateTable().stream()
                                    .filter(stateVariable -> stateVariable.getName()
                                            .equals(scpdArgument.getRelatedStateVariable()))
                                    .findFirst().orElseThrow(() -> new ChannelConfigException("StateVariable '"
                                            + scpdArgument.getRelatedStateVariable() + "' not found"));
                            parameters.addAll(
                                    getAndCheckParameters(channelId, getAction, scpdAction, serviceRoot, thingConfig));

                            channelConfig.setGetAction(scpdAction);
                            channelConfig.setDataType(relatedStateVariable.getDataType());
                        }

                        // check set action
                        ActionType setAction = channelTypeDescription.getSetAction();
                        if (setAction != null) {
                            String actionName = setAction.getName();
                            String argumentName = setAction.getArgument();

                            SCPDActionType scpdAction = serviceRoot.getActionList().stream()
                                    .filter(action -> action.getName().equals(actionName)).findFirst()
                                    .orElseThrow(() -> new ChannelConfigException(
                                            "Set-Action '" + actionName + "' not found"));
                            if (argumentName != null) {
                                SCPDArgumentType scpdArgument = scpdAction.getArgumentList().stream()
                                        .filter(argument -> argument.getName().equals(argumentName)
                                                && argument.getDirection().equals("in"))
                                        .findFirst().orElseThrow(() -> new ChannelConfigException(
                                                "Set-Argument '" + argumentName + "' not found"));
                                SCPDStateVariableType relatedStateVariable = serviceRoot.getServiceStateTable().stream()
                                        .filter(stateVariable -> stateVariable.getName()
                                                .equals(scpdArgument.getRelatedStateVariable()))
                                        .findFirst().orElseThrow(() -> new ChannelConfigException("StateVariable '"
                                                + scpdArgument.getRelatedStateVariable() + "' not found"));
                                if (channelConfig.getDataType().isEmpty()) {
                                    channelConfig.setDataType(relatedStateVariable.getDataType());
                                } else if (!channelConfig.getDataType().equals(relatedStateVariable.getDataType())) {
                                    throw new ChannelConfigException("dataType of set and get action are different");
                                }
                            }
                        }

                        // everything is available, create the channel
                        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID,
                                channelTypeDescription.getName());
                        if (parameters.isEmpty()) {
                            // we have no parameters, so create a single channel
                            ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
                            ChannelBuilder channelBuilder = ChannelBuilder
                                    .create(channelUID, channelTypeDescription.getItem().getType())
                                    .withType(channelTypeUID);
                            thingBuilder.withChannel(channelBuilder.build());
                            channels.put(channelUID, channelConfig);
                        } else {
                            // create a channel for each parameter
                            parameters.forEach(parameter -> {
                                String normalizedParameter = UIDUtils.encode(parameter);
                                ChannelUID channelUID = new ChannelUID(thing.getUID(),
                                        channelId + "_" + normalizedParameter);
                                ChannelBuilder channelBuilder = ChannelBuilder
                                        .create(channelUID, channelTypeDescription.getItem().getType())
                                        .withType(channelTypeUID)
                                        .withLabel(channelTypeDescription.getLabel() + " " + parameter);
                                thingBuilder.withChannel(channelBuilder.build());
                                Tr064ChannelConfig channelConfig1 = new Tr064ChannelConfig(channelConfig);
                                channelConfig1.setParameter(parameter);
                                channels.put(channelUID, channelConfig1);
                            });
                        }
                    } catch (ChannelConfigException e) {
                        LOGGER.debug("Channel {} not available: {}", channelId, e.getMessage());
                    }
                });
        return thingBuilder;
    }

    private static Set<String> getAndCheckParameters(String channelId, ActionType action, SCPDActionType scpdAction,
            SCPDScpdType serviceRoot, Tr064BaseThingConfiguration thingConfig) throws ChannelConfigException {
        ParameterType parameter = action.getParameter();
        if (parameter == null) {
            return Collections.emptySet();
        }
        try {
            Set<String> parameters = new HashSet<>();

            // get parameters by reflection from thing config
            Field paramField = thingConfig.getClass().getField(parameter.getThingParameter());
            Object rawFieldValue = paramField.get(thingConfig);
            if ((rawFieldValue instanceof List<?>)) {
                ((List<?>) rawFieldValue).forEach(obj -> {
                    if (obj instanceof String) {
                        parameters.add((String) obj);
                    }
                });
            }

            // validate parameter against pattern
            String parameterPattern = parameter.getPattern();
            if (parameterPattern != null) {
                parameters.removeIf(param -> !param.matches(parameterPattern));
            }

            // validate parameter against SCPD (if not internal only)
            if (!parameter.isInternalOnly()) {
                SCPDArgumentType scpdArgument = scpdAction.getArgumentList().stream()
                        .filter(argument -> argument.getName().equals(parameter.getName())
                                && argument.getDirection().equals("in"))
                        .findFirst().orElseThrow(() -> new ChannelConfigException(
                                "Get-Parameter '" + parameter.getName() + "' not found"));
                SCPDStateVariableType relatedStateVariable = serviceRoot.getServiceStateTable().stream()
                        .filter(stateVariable -> stateVariable.getName().equals(scpdArgument.getRelatedStateVariable()))
                        .findFirst().orElseThrow(() -> new ChannelConfigException(
                                "StateVariable '" + scpdArgument.getRelatedStateVariable() + "' not found"));
                if (relatedStateVariable.getAllowedValueRange() != null) {
                    int paramMin = relatedStateVariable.getAllowedValueRange().getMinimum();
                    int paramMax = relatedStateVariable.getAllowedValueRange().getMaximum();
                    int paramStep = relatedStateVariable.getAllowedValueRange().getStep();
                    Set<String> allowedValues = Stream.iterate(paramMin, i -> i <= paramMax, i -> i + paramStep)
                            .map(String::valueOf).collect(Collectors.toSet());
                    parameters.removeIf(param -> !allowedValues.contains(param));
                }
            }

            // check we have at least one valid parameter left
            if (parameters.isEmpty()) {
                throw new IllegalArgumentException();
            }
            return parameters;
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            throw new ChannelConfigException("Could not get required parameter '" + channelId
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
        }
        return Optional.empty();
    }
}
