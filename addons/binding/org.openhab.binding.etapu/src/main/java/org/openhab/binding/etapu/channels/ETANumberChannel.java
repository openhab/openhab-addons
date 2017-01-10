package org.openhab.binding.etapu.channels;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ETANumberChannel extends ETAChannel {

    @Override
    public Number getValue() {
        Number result = null;
        try {
            Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(getResponse())));
            NodeList nodeList = d.getElementsByTagName("value");
            if (nodeList.getLength() > 0) {
                NamedNodeMap attrs = nodeList.item(0).getAttributes();

                Double value = Double.parseDouble(attrs.getNamedItem("strValue").getNodeValue());
                Double scaleFactor = Double.parseDouble(attrs.getNamedItem("scaleFactor").getNodeValue());

                result = value * scaleFactor;
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return result;
    }
}
