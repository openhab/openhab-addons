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
package org.openhab.binding.onkyo.internal.handler;

import static org.jupnp.model.XMLUtil.appendNewElement;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jupnp.transport.impl.PooledXmlProcessor;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility class for building metadata XML for UPnP "SetAVTransportURI" action.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public class URIMetaDataProcessor extends PooledXmlProcessor {
    private static final String XML_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";
    private static final String DIDL_NAMESPACE_URI = "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/";
    private static final String UPNP_NAMESPACE_URI = "urn:schemas-upnp-org:metadata-1-0/upnp/";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Generates metadata XML for given audio stream.
     *
     * @param url media stream URL
     * @param audioStream audio format specification
     * @return generated XML document as {@code String}
     */
    public String generate(String url, AudioStream audioStream) {
        if (audioStream != null) {
            try {
                Document document = this.newDocument();
                Element rootElement = document.createElementNS(DIDL_NAMESPACE_URI, "DIDL-Lite");
                document.appendChild(rootElement);
                rootElement.setAttributeNS(XML_NAMESPACE_URI, "xmlns:upnp", UPNP_NAMESPACE_URI);

                Element itemElement = appendNewElement(document, rootElement, "item");
                setAttributeIfNotNull(itemElement, "id", audioStream.getId());

                appendNewElement(document, itemElement, "upnp:class", "object.item.audioItem", UPNP_NAMESPACE_URI);
                Element resourceElement = appendNewElement(document, itemElement, "res", url);
                setFormatAttributes(resourceElement, audioStream.getFormat());

                return documentToString(document);
            } catch (Exception e) {
                logger.warn("Unable to build metadata for {}: {}", url, e.getMessage());
            }
        }

        return "";
    }

    private void setFormatAttributes(Element resourceElement, AudioFormat format) {
        setAttributeIfNotNull(resourceElement, "nrAudioChannels", format.getChannels());
        setAttributeIfNotNull(resourceElement, "sampleFrequency", format.getFrequency());
        setAttributeIfNotNull(resourceElement, "bitrate", format.getBitRate());
        setAttributeIfNotNull(resourceElement, "protocolInfo", getProtocolInfo(format));
    }

    private String getProtocolInfo(AudioFormat format) {
        String[] protocolInfo = { "http-get", "*", getFormatMimeType(format), "*" };
        return String.join(":", protocolInfo);
    }

    private String getFormatMimeType(AudioFormat format) {
        if (AudioFormat.MP3.isCompatible(format)) {
            return "audio/mpeg";
        } else if (AudioFormat.WAV.isCompatible(format)) {
            return "audio/wav";
        } else if (AudioFormat.OGG.isCompatible(format)) {
            return "audio/ogg";
        } else if (AudioFormat.AAC.isCompatible(format)) {
            return "audio/aac";
        } else if (AudioFormat.PCM_SIGNED.isCompatible(format)) {
            return "audio/pcm";
        }
        throw new IllegalArgumentException("Invalid audio format given: " + format);
    }

    private String documentToString(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter out = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(document), new StreamResult(out));
        return out.toString();
    }

    private static void setAttributeIfNotNull(Element element, String name, Object value) {
        if (value != null) {
            element.setAttribute(name, value.toString());
        }
    }
}
