package org.openhab.binding.etapu.channels;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

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
 */
public abstract class ETAChannel {


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

    public Optional<String> getResponse() {
        return Optional.ofNullable(response);
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


}
