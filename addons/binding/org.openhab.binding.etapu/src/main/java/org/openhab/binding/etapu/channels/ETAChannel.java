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

/**
 * Class that represents a channel of an ETA thing.
 *
 * @author mh
 *
 */
public class ETAChannel {

    private Logger logger = LoggerFactory.getLogger(ETAChannel.class);

    private String id;

    private String url;

    private String response;

    /**
     * Sets the url of the rest interface that is called when channel is refreshed
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Sets the response of the rest interface that has been called when a channel is refreshed
     *
     * @param r
     */
    public void setResponse(String r) {
        this.response = r;
    }

    public String getResponse() {
        return response;
    }

    /**
     * Sets the id of the channel
     *
     * @return
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Parses the response string of the REST interface and returns the value as String
     * 
     * @return
     */
    public String getValue() {
        String result = "";
        try {
            if (response != null) {
                Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(new InputSource(new StringReader(getResponse())));
                NodeList nodeList = d.getElementsByTagName("value");
                if (nodeList.getLength() > 0) {
                    result = nodeList.item(0).getAttributes().getNamedItem("strValue").getNodeValue();
                }
            } else {
                logger.warn("Channel " + id + " did not receive a result.");
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return result;
    }

}
