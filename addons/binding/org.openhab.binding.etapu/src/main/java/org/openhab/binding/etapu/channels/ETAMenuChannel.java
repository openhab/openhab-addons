package org.openhab.binding.etapu.channels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ETAMenuChannel extends ETAChannel {

    private Logger logger = LoggerFactory.getLogger(ETAMenuChannel.class);

    public ETAMenuChannel() {
        setId("menu");
        setUrl("/user/menu/");
    }

    public Set<ETAVarChannel> getETAVarChannels() {
        Set<ETAVarChannel> channels = new HashSet<>();
        if (getResponse().isPresent()) {
            try {
                Document d = DocumentBuilderFactory.newInstance()
                                                   .newDocumentBuilder()
                                                   .parse(new InputSource(new StringReader(getResponse().get())));
                NodeList nodeList = d.getElementsByTagName("object");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    String uri = nodeList.item(i).getAttributes().getNamedItem("uri").getNodeValue();
                    String id = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
                    channels.add(new ETAVarChannel(id, uri));
                    logger.info("Found new channel " + getId() + " at URL " + getUrl());
                }
            } catch (SAXException | IOException | ParserConfigurationException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return channels;
    }

}
