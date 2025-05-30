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
package org.openhab.binding.emotiva.internal.protocol;

import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags.UNKNOWN_TAG;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.emotiva.internal.dto.AbstractJAXBElementDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaAckDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaBarNotifyDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaBarNotifyWrapper;
import org.openhab.binding.emotiva.internal.dto.EmotivaCommandDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaControlDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaMenuNotifyDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaNotifyDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaNotifyWrapper;
import org.openhab.binding.emotiva.internal.dto.EmotivaPingDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaPropertyDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaSubscriptionRequest;
import org.openhab.binding.emotiva.internal.dto.EmotivaSubscriptionResponse;
import org.openhab.binding.emotiva.internal.dto.EmotivaTransponderDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaUnsubscribeDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaUpdateRequest;
import org.openhab.binding.emotiva.internal.dto.EmotivaUpdateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Helper class for marshalling and unmarshalling Emotiva message types.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class EmotivaXmlUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmotivaXmlUtils.class);
    Marshaller marshaller;

    JAXBContext context;

    public EmotivaXmlUtils() throws JAXBException {
        context = JAXBContext.newInstance(EmotivaAckDTO.class, EmotivaBarNotifyWrapper.class, EmotivaBarNotifyDTO.class,
                EmotivaCommandDTO.class, EmotivaControlDTO.class, EmotivaMenuNotifyDTO.class,
                EmotivaNotifyWrapper.class, EmotivaPingDTO.class, EmotivaPropertyDTO.class,
                EmotivaSubscriptionRequest.class, EmotivaSubscriptionResponse.class, EmotivaTransponderDTO.class,
                EmotivaUnsubscribeDTO.class, EmotivaUpdateRequest.class, EmotivaUpdateResponse.class);
        marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }

    public String marshallEmotivaDTO(Object objectInstanceType) {
        try {
            var out = new StringWriter();
            marshaller.marshal(objectInstanceType, out);
            return out.toString();
        } catch (JAXBException e) {
            LOGGER.debug("Could not marshall class of type {}", objectInstanceType.getClass().getName(), e);
        }
        return "";
    }

    public String marshallJAXBElementObjects(AbstractJAXBElementDTO jaxbElementDTO) {
        try {
            var out = new StringWriter();

            List<JAXBElement<String>> commandsAsJAXBElement = new ArrayList<>();

            if (jaxbElementDTO.getCommands() != null) {
                for (EmotivaCommandDTO command : jaxbElementDTO.getCommands()) {
                    if (command.getName() != null) {
                        var sb = new StringBuilder();
                        if (command.getValue() != null) {
                            sb.append(" value=\"").append(command.getValue()).append("\"");
                        }
                        if (command.getStatus() != null) {
                            sb.append(" status=\"").append(command.getStatus()).append("\"");
                        }
                        if (command.getVisible() != null) {
                            sb.append(" visible=\"").append(command.getVisible()).append("\"");
                        }
                        if (command.getAck() != null) {
                            sb.append(" ack=\"").append(command.getAck()).append("\"");
                        }
                        var name = new QName("%s%s".formatted(command.getName().trim(), sb));
                        commandsAsJAXBElement.add(jaxbElementDTO.createJAXBElement(name));
                    }
                }
            }

            // Replace commands with modified JaxbElements for Emotiva compatible marshalling
            jaxbElementDTO.setJaxbElements(commandsAsJAXBElement);
            jaxbElementDTO.setCommands(Collections.emptyList());

            marshaller.marshal(jaxbElementDTO, out);

            // Remove JAXB added xsi and xmlns data, not needed
            return out.toString().replaceAll("xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"",
                    "");
        } catch (JAXBException e) {
            LOGGER.debug("Could not marshall class of type {}", jaxbElementDTO.getClass().getName(), e);
        }
        return "";
    }

    public Object unmarshallToEmotivaDTO(String xmlAsString) throws JAXBException {
        Object object;
        Unmarshaller unmarshaller = context.createUnmarshaller();

        if (xmlAsString.isEmpty()) {
            throw new JAXBException("Could not unmarshall value, xml value is null or empty");
        }

        var xmlAsStringReader = new StringReader(xmlAsString);
        var xmlAsStringStream = new StreamSource(xmlAsStringReader);
        object = unmarshaller.unmarshal(xmlAsStringStream);
        return object;
    }

    public List<EmotivaCommandDTO> unmarshallXmlObjectsToControlCommands(List<Object> objects) {
        List<EmotivaCommandDTO> commands = new ArrayList<>();
        for (Object object : objects) {
            try {
                var xmlElement = (Element) object;

                try {
                    EmotivaCommandDTO commandDTO = getEmotivaCommandDTO(xmlElement);
                    commands.add(commandDTO);
                } catch (IllegalArgumentException e) {
                    LOGGER.debug("Notify tag {} is unknown or not defined, skipping.", xmlElement.getTagName(), e);
                }
            } catch (ClassCastException e) {
                LOGGER.debug("Could not cast object to Element, object is of type {}", object.getClass());
            }
        }
        return commands;
    }

    public List<EmotivaNotifyDTO> unmarshallToNotification(List<Object> objects) {
        List<EmotivaNotifyDTO> commands = new ArrayList<>();
        for (Object object : objects) {
            try {
                var xmlElement = (Element) object;

                try {
                    EmotivaNotifyDTO tagDTO = getEmotivaNotifyTags(xmlElement);
                    commands.add(tagDTO);
                } catch (IllegalArgumentException e) {
                    LOGGER.debug("Notify tag {} is unknown or not defined, skipping.", xmlElement.getTagName(), e);
                }
            } catch (ClassCastException e) {
                LOGGER.debug("Could not cast object to Element, object is of type {}", object.getClass());
            }
        }
        return commands;
    }

    public List<EmotivaBarNotifyDTO> unmarshallToBarNotify(List<Object> objects) {
        List<EmotivaBarNotifyDTO> commands = new ArrayList<>();
        for (Object object : objects) {
            try {
                var xmlElement = (Element) object;

                try {
                    EmotivaBarNotifyDTO tagDTO = getEmotivaBarNotify(xmlElement);
                    commands.add(tagDTO);
                } catch (IllegalArgumentException e) {
                    LOGGER.debug("Bar notify type {} is unknown or not defined, skipping.", xmlElement.getTagName(), e);
                }
            } catch (ClassCastException e) {
                LOGGER.debug("Could not cast object to Element, object is of type {}", object.getClass());
            }
        }
        return commands;
    }

    public List<EmotivaCommandDTO> unmarshallToCommands(String elementAsString) {
        List<EmotivaCommandDTO> commands = new ArrayList<>();
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = builderFactory.newDocumentBuilder();

            String[] lines = elementAsString.split("\n");
            for (String line : lines) {
                if (line != null && line.trim().startsWith("<") && line.trim().endsWith("/>")) {
                    Document doc = db.parse(new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8)));
                    doc.getDocumentElement();
                    EmotivaCommandDTO commandDTO = getEmotivaCommandDTO(doc.getDocumentElement());
                    commands.add(commandDTO);
                }
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            LOGGER.debug("Error unmarshall elements to commands", e);
        }
        return commands;
    }

    private static EmotivaCommandDTO getEmotivaCommandDTO(Element xmlElement) {
        EmotivaControlCommands commandType;
        try {
            String tagName = xmlElement.getTagName();
            if (tagName == null || tagName.isBlank()) {
                LOGGER.debug("Could not create EmotivaCommand, tag name was '{}'", tagName);
                commandType = EmotivaControlCommands.none;
            } else {
                commandType = EmotivaControlCommands.valueOf(tagName.trim());
            }
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Could not create EmotivaCommand, unknown command {}", xmlElement.getTagName());
            commandType = EmotivaControlCommands.none;
        }
        var commandDTO = new EmotivaCommandDTO(commandType);
        if (xmlElement.hasAttribute("status")) {
            commandDTO.setStatus(xmlElement.getAttribute("status"));
        }
        if (xmlElement.hasAttribute("value")) {
            commandDTO.setValue(xmlElement.getAttribute("value"));
        }
        if (xmlElement.hasAttribute("visible")) {
            commandDTO.setVisible(xmlElement.getAttribute("visible"));
        }
        return commandDTO;
    }

    private static EmotivaBarNotifyDTO getEmotivaBarNotify(Element xmlElement) {
        var barNotify = new EmotivaBarNotifyDTO(xmlElement.getTagName());
        if (xmlElement.hasAttribute("type")) {
            barNotify.setType(xmlElement.getAttribute("type"));
        }
        if (xmlElement.hasAttribute("text")) {
            barNotify.setText(xmlElement.getAttribute("text"));
        }
        if (xmlElement.hasAttribute("units")) {
            barNotify.setUnits(xmlElement.getAttribute("units"));
        }
        if (xmlElement.hasAttribute("value")) {
            barNotify.setValue(xmlElement.getAttribute("value"));
        }
        if (xmlElement.hasAttribute("min")) {
            barNotify.setMin(xmlElement.getAttribute("min"));
        }
        if (xmlElement.hasAttribute("max")) {
            barNotify.setMax(xmlElement.getAttribute("max"));
        }
        return barNotify;
    }

    private static EmotivaNotifyDTO getEmotivaNotifyTags(Element xmlElement) {
        String notifyTagName;
        try {
            String tagName = xmlElement.getTagName();
            if (tagName == null || tagName.isBlank()) {
                LOGGER.debug("Could not create EmotivaNotify, subscription tag name was '{}'", tagName);
                notifyTagName = UNKNOWN_TAG;
            } else {
                notifyTagName = EmotivaSubscriptionTags.valueOf(tagName.trim()).name();
            }
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Could not create EmotivaNotify, unknown subscription tag {}", xmlElement.getTagName());
            notifyTagName = UNKNOWN_TAG;
        }
        var commandDTO = new EmotivaNotifyDTO(notifyTagName);
        if (xmlElement.hasAttribute("status")) {
            commandDTO.setStatus(xmlElement.getAttribute("status"));
        }
        if (xmlElement.hasAttribute("value")) {
            commandDTO.setValue(xmlElement.getAttribute("value"));
        }
        if (xmlElement.hasAttribute("visible")) {
            commandDTO.setVisible(xmlElement.getAttribute("visible"));
        }
        if (xmlElement.hasAttribute("ack")) {
            commandDTO.setAck(xmlElement.getAttribute("ack"));
        }
        return commandDTO;
    }
}
