/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.enigma2.internal;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.UrlEncoded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@link Enigma2Client} class is responsible for communicating with the Enigma2 device.
 *
 * @see <a href=
 *      "https://github.com/E2OpenPlugins/e2openplugin-OpenWebif/wiki/OpenWebif-API-documentation">OpenWebif-API-documentation</a>
 *
 * @author Guido Dolfen - Initial contribution
 */
@NonNullByDefault
public class Enigma2Client {
    private final Logger logger = LoggerFactory.getLogger(Enigma2Client.class);

    static final String PATH_REMOTE_CONTROL = "/web/remotecontrol?command=";
    static final String PATH_POWER = "/web/powerstate";
    static final String PATH_VOLUME = "/web/vol";
    static final String PATH_SET_VOLUME = "/web/vol?set=set";
    static final String PATH_TOGGLE_MUTE = "/web/vol?set=mute";
    static final String PATH_TOGGLE_POWER = "/web/powerstate?newstate=0";
    static final String PATH_MESSAGE = "/web/message?type=";
    static final String PATH_ALL_SERVICES = "/web/getallservices";
    static final String PATH_ZAP = "/web/zap?sRef=";
    static final String PATH_CHANNEL = "/web/subservices";
    static final String PATH_EPG = "/web/epgservicenow?sRef=";
    static final String PATH_ANSWER = "/web/messageanswer?getanswer=now";
    static final int TYPE_QUESTION = 0;
    static final int TYPE_INFO = 1;
    static final int TYPE_WARNING = 2;
    static final int TYPE_ERROR = 3;
    private final Map<String, String> channels = new ConcurrentHashMap<>();
    private final String host;
    private boolean power;
    private String channel = "";
    private String title = "";
    private String description = "";
    private String answer = "";
    private int volume = 0;
    private boolean mute;
    private boolean online;
    private boolean initialized;
    private boolean asking;
    private LocalDateTime lastAnswerTime = LocalDateTime.of(2020, 1, 1, 0, 0); // Date in the past
    private final Enigma2HttpClient enigma2HttpClient;
    private final DocumentBuilderFactory factory;

    public Enigma2Client(String host, @Nullable String user, @Nullable String password, int requestTimeout) {
        enigma2HttpClient = new Enigma2HttpClient(requestTimeout);
        factory = DocumentBuilderFactory.newInstance();
        // see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
        try {
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
        } catch (ParserConfigurationException e) {
            logger.warn("Failed setting parser features against XXE attacks!", e);
        }
        if (user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
            this.host = "http://" + user + ":" + password + "@" + host;
        } else {
            this.host = "http://" + host;
        }
    }

    public boolean refresh() {
        boolean wasOnline = online;
        refreshPower();
        if (!wasOnline && online) {
            // Only refresh all services if the box changed from offline to online and power is on
            // because it is a performance intensive action.
            refreshAllServices();
        }
        refreshChannel();
        refreshEpg();
        refreshVolume();
        refreshAnswer();
        return online;
    }

    public void refreshPower() {
        Optional<Document> document = transmitWithResult(PATH_POWER);
        if (document.isPresent()) {
            online = true;
            processPowerResult(document.get());
        } else {
            online = false;
            power = false;
        }
        initialized = true;
    }

    public void refreshAllServices() {
        if (power || channels.isEmpty()) {
            transmitWithResult(PATH_ALL_SERVICES).ifPresent(this::processAllServicesResult);
        }
    }

    public void refreshChannel() {
        if (power) {
            transmitWithResult(PATH_CHANNEL).ifPresent(this::processChannelResult);
        }
    }

    public void refreshAnswer() {
        if (asking) {
            transmitWithResult(PATH_ANSWER).ifPresent(this::processAnswerResult);
        }
    }

    public void refreshVolume() {
        if (power) {
            transmitWithResult(PATH_VOLUME).ifPresent(this::processVolumeResult);
        }
    }

    public void refreshEpg() {
        if (power) {
            Optional.ofNullable(channels.get(channel))
                    .flatMap(name -> transmitWithResult(PATH_EPG + UrlEncoded.encodeString(name)))
                    .ifPresent(this::processEpgResult);
        }
    }

    private Optional<Document> transmitWithResult(String path) {
        try {
            Optional<String> xml = transmit(path);
            if (xml.isPresent()) {
                DocumentBuilder builder = factory.newDocumentBuilder();
                return Optional.ofNullable(builder.parse(new InputSource(new StringReader(xml.get()))));
            }
            return Optional.empty();
        } catch (IOException | SAXException | ParserConfigurationException | IllegalArgumentException e) {
            if (online || !initialized) {
                logger.debug("Error on transmit {}{}.", host, path, e);
            }
            return Optional.empty();
        }
    }

    private Optional<String> transmit(String path) {
        String url = host + path;
        try {
            logger.debug("Transmitting {}", url);
            String result = getEnigma2HttpClient().get(url);
            logger.debug("Transmitting result is {}", result);
            return Optional.ofNullable(result);
        } catch (IOException | IllegalArgumentException e) {
            if (online || !initialized) {
                logger.debug("Error on transmit {}.", url, e);
            }
            return Optional.empty();
        }
    }

    public void setMute(boolean mute) {
        refreshVolume();
        if (this.mute != mute) {
            transmitWithResult(PATH_TOGGLE_MUTE).ifPresent(this::processVolumeResult);
        }
    }

    public void setPower(boolean power) {
        refreshPower();
        if (this.power != power) {
            transmitWithResult(PATH_TOGGLE_POWER).ifPresent(this::processPowerResult);
        }
    }

    public void setVolume(int volume) {
        transmitWithResult(PATH_SET_VOLUME + volume).ifPresent(this::processVolumeResult);
    }

    public void setChannel(String name) {
        if (channels.containsKey(name)) {
            String id = channels.get(name);
            transmitWithResult(PATH_ZAP + UrlEncoded.encodeString(id)).ifPresent(document -> channel = name);
        } else {
            logger.warn("Channel {} not found.", name);
        }
    }

    public void sendRcCommand(int key) {
        transmit(PATH_REMOTE_CONTROL + key);
    }

    public void sendError(int timeout, String text) {
        sendMessage(TYPE_ERROR, timeout, text);
    }

    public void sendWarning(int timeout, String text) {
        sendMessage(TYPE_WARNING, timeout, text);
    }

    public void sendInfo(int timeout, String text) {
        sendMessage(TYPE_INFO, timeout, text);
    }

    public void sendQuestion(int timeout, String text) {
        asking = true;
        sendMessage(TYPE_QUESTION, timeout, text);
    }

    private void sendMessage(int type, int timeout, String text) {
        transmit(PATH_MESSAGE + type + "&timeout=" + timeout + "&text=" + UrlEncoded.encodeString(text));
    }

    private void processPowerResult(Document document) {
        power = !getBoolean(document, "e2instandby");
        if (!power) {
            title = "";
            description = "";
            channel = "";
        }
    }

    private void processChannelResult(Document document) {
        channel = getString(document, "e2servicename");
        // Add channel-Reference-ID if not known
        if (!channels.containsKey(channel)) {
            channels.put(channel, getString(document, "e2servicereference"));
        }
    }

    private void processAnswerResult(Document document) {
        if (asking) {
            boolean state = getBoolean(document, "e2state");
            if (state) {
                String[] text = getString(document, "e2statetext").split(" ");
                answer = text[text.length - 1].replace("!", "");
                asking = false;
                lastAnswerTime = LocalDateTime.now();
            }
        }
    }

    private void processVolumeResult(Document document) {
        volume = getInt(document, "e2current");
        mute = getBoolean(document, "e2ismuted");
    }

    private void processEpgResult(Document document) {
        title = getString(document, "e2eventtitle");
        description = getString(document, "e2eventdescription");
    }

    private void processAllServicesResult(Document document) {
        NodeList bouquetList = document.getElementsByTagName("e2bouquet");
        channels.clear();
        for (int i = 0; i < bouquetList.getLength(); i++) {
            Element bouquet = (Element) bouquetList.item(i);
            NodeList serviceList = bouquet.getElementsByTagName("e2service");
            for (int j = 0; j < serviceList.getLength(); j++) {
                Element service = (Element) serviceList.item(j);
                String id = service.getElementsByTagName("e2servicereference").item(0).getTextContent();
                String name = service.getElementsByTagName("e2servicename").item(0).getTextContent();
                channels.put(name, id);
            }
        }
    }

    private String getString(Document document, String elementId) {
        return Optional.ofNullable(document.getElementsByTagName(elementId)).map(nodeList -> nodeList.item(0))
                .map(Node::getTextContent).map(String::trim).orElse("");
    }

    private boolean getBoolean(Document document, String elementId) {
        return Boolean.parseBoolean(getString(document, elementId));
    }

    private int getInt(Document document, String elementId) {
        try {
            return Integer.parseInt(getString(document, elementId));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getVolume() {
        return volume;
    }

    public boolean isMute() {
        return mute;
    }

    public boolean isPower() {
        return power;
    }

    public LocalDateTime getLastAnswerTime() {
        return lastAnswerTime;
    }

    public String getChannel() {
        return channel;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAnswer() {
        return answer;
    }

    public Collection<String> getChannels() {
        return channels.keySet();
    }

    /**
     * Getter for Test-Injection
     *
     * @return HttpGet.
     */
    Enigma2HttpClient getEnigma2HttpClient() {
        return enigma2HttpClient;
    }
}
