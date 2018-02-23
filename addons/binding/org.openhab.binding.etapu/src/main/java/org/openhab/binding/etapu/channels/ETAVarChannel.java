package org.openhab.binding.etapu.channels;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ETAVarChannel extends ETAChannel {
    private Logger logger = LoggerFactory.getLogger(ETAVarChannel.class);

    private static final String prefix = "/user/var";

    public ETAVarChannel(String id, String uri) {
        super();
        setId(id);
        setUrl(uri);
    }

    @Override
    public void setUrl(String url) {
        super.setUrl(prefix + url);
    }

    /**
     * Parses the response string of the REST interface and returns the value as String
     *
     * @return
     */
    public String getValue() {
        String result = "";
        try {
            if (getResponse().isPresent()) {
                Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(new InputSource(new StringReader(getResponse().get())));
                NodeList nodeList = d.getElementsByTagName("value");
                if (nodeList.getLength() > 0) {
                    result = nodeList.item(0).getAttributes().getNamedItem("strValue").getNodeValue();
                }
            } else {
                logger.warn("Channel " + getId() + " did not receive a result.");
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

}
