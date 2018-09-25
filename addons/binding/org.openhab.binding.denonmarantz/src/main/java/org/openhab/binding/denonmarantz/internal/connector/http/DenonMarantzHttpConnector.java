/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.internal.connector.http;

import java.beans.Introspector;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.denonmarantz.internal.DenonMarantzState;
import org.openhab.binding.denonmarantz.internal.config.DenonMarantzConfiguration;
import org.openhab.binding.denonmarantz.internal.connector.DenonMarantzConnector;
import org.openhab.binding.denonmarantz.internal.xml.entities.Deviceinfo;
import org.openhab.binding.denonmarantz.internal.xml.entities.Main;
import org.openhab.binding.denonmarantz.internal.xml.entities.ZoneStatus;
import org.openhab.binding.denonmarantz.internal.xml.entities.ZoneStatusLite;
import org.openhab.binding.denonmarantz.internal.xml.entities.commands.AppCommandRequest;
import org.openhab.binding.denonmarantz.internal.xml.entities.commands.AppCommandResponse;
import org.openhab.binding.denonmarantz.internal.xml.entities.commands.CommandRx;
import org.openhab.binding.denonmarantz.internal.xml.entities.commands.CommandTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class makes the connection to the receiver and manages it.
 * It is also responsible for sending commands to the receiver.
 * *
 *
 * @author Jeroen Idserda - Initial Contribution (1.x Binding)
 * @author Jan-Willem Veldhuis - Refactored for 2.x
 */
public class DenonMarantzHttpConnector extends DenonMarantzConnector {

    private Logger logger = LoggerFactory.getLogger(DenonMarantzHttpConnector.class);

    private static final int REQUEST_TIMEOUT_MS = 5000; // 5 seconds

    // Main URL for the receiver
    private static final String URL_MAIN = "formMainZone_MainZoneXml.xml";

    // Main Zone Status URL
    private static final String URL_ZONE_MAIN = "formMainZone_MainZoneXmlStatus.xml";

    // Secondary zone lite status URL (contains less info)
    private static final String URL_ZONE_SECONDARY_LITE = "formZone%d_Zone%dXmlStatusLite.xml";

    // Device info URL
    private static final String URL_DEVICE_INFO = "Deviceinfo.xml";

    // URL to send app commands to
    private static final String URL_APP_COMMAND = "AppCommand.xml";

    private static final String CONTENT_TYPE_XML = "application/xml";

    private final String cmdUrl;

    private final String statusUrl;

    private final HttpClient httpClient;

    private ScheduledFuture<?> pollingJob;

    public DenonMarantzHttpConnector(DenonMarantzConfiguration config, DenonMarantzState state,
            ScheduledExecutorService scheduler, HttpClient httpClient) {
        this.config = config;
        this.scheduler = scheduler;
        this.state = state;
        this.cmdUrl = String.format("http://%s:%d/goform/formiPhoneAppDirect.xml?", config.getHost(),
                config.getHttpPort());
        this.statusUrl = String.format("http://%s:%d/goform/", config.getHost(), config.getHttpPort());
        this.httpClient = httpClient;
    }

    public DenonMarantzState getState() {
        return state;
    }

    /**
     * Set up the connection to the receiver by starting to poll the HTTP API.
     */
    @Override
    public void connect() {
        refreshState();
        startPolling();
    }

    private void startPolling() {
        if (!isPolling()) {
            logger.debug("HTTP polling started.");
            pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    refreshHttpProperties();
                } catch (RuntimeException e) {
                    /**
                     * We need to catch this RuntimeException, as otherwise the polling stops.
                     * Log as error as it could be a user configuration error.
                     */
                    StringBuilder sb = new StringBuilder();
                    for (StackTraceElement s : e.getStackTrace()) {
                        sb.append(s.toString()).append("\n");
                    }
                    logger.error("Error while polling Http: \"{}\". Stacktrace: \n{}", e.getMessage(), sb.toString());
                }
            }, config.httpPollingInterval, config.httpPollingInterval, TimeUnit.SECONDS);
        }
    }

    private boolean isPolling() {
        return pollingJob != null && !pollingJob.isCancelled();
    }

    private void stopPolling() {
        if (isPolling()) {
            pollingJob.cancel(true);
            logger.debug("HTTP polling stopped.");
        }
    }

    /**
     * Shutdown the http client
     */
    @Override
    public void dispose() {
        logger.debug("disposing connector");

        stopPolling();
    }

    /**
     * Gets the current state of all properties from the receiver, including
     * basic configuration info (like the number of zones)
     *
     * @throws IOException
     */
    private void refreshState() {
        setConfigProperties();
        refreshHttpProperties();
    }

    @Override
    protected void internalSendCommand(String command) {
        logger.debug("Sending command '{}'", command);
        if (StringUtils.isBlank(command)) {
            logger.warn("Trying to send empty command");
            return;
        }

        try {
            String url = cmdUrl + URLEncoder.encode(command, Charset.defaultCharset().displayName());
            logger.trace("Calling url {}", url);

            httpClient.newRequest(url).timeout(5, TimeUnit.SECONDS).send(new Response.CompleteListener() {
                @Override
                public void onComplete(Result result) {
                    if (result.getResponse().getStatus() != 200) {
                        logger.warn("Error {} while sending command", result.getResponse().getReason());
                    }
                }
            });

        } catch (UnsupportedEncodingException e) {
            logger.warn("Error sending command", e);
        }
    }

    private void updateMain() {
        String url = statusUrl + URL_MAIN;
        logger.trace("Refreshing URL: {}", url);

        Main statusMain = getDocument(url, Main.class);
        if (statusMain != null) {
            state.setPower(statusMain.getPower().getValue());
        }
    }

    private void updateMainZone() {
        String url = statusUrl + URL_ZONE_MAIN;
        logger.trace("Refreshing URL: {}", url);

        ZoneStatus mainZone = getDocument(url, ZoneStatus.class);
        if (mainZone != null) {
            state.setInput(mainZone.getInputFuncSelect().getValue());
            state.setMainVolume(mainZone.getMasterVolume().getValue());
            state.setMainZonePower(mainZone.getPower().getValue());
            state.setMute(mainZone.getMute().getValue());

            if (config.inputOptions == null) {
                config.inputOptions = mainZone.getInputFuncList();
            }

            if (mainZone.getSurrMode() == null) {
                logger.debug("Unable to get the SURROUND_MODE. MainZone update may not be correct.");
            } else {
                state.setSurroundProgram(mainZone.getSurrMode().getValue());
            }
        }
    }

    private void updateSecondaryZones() {
        for (int i = 2; i <= config.getZoneCount(); i++) {
            String url = String.format("%s" + URL_ZONE_SECONDARY_LITE, statusUrl, i, i);
            logger.trace("Refreshing URL: {}", url);
            ZoneStatusLite zoneSecondary = getDocument(url, ZoneStatusLite.class);
            if (zoneSecondary != null) {
                switch (i) {
                    // maximum 2 secondary zones are supported
                    case 2:
                        state.setZone2Power(zoneSecondary.getPower().getValue());
                        state.setZone2Volume(zoneSecondary.getMasterVolume().getValue());
                        state.setZone2Mute(zoneSecondary.getMute().getValue());
                        state.setZone2Input(zoneSecondary.getInputFuncSelect().getValue());
                        break;
                    case 3:
                        state.setZone3Power(zoneSecondary.getPower().getValue());
                        state.setZone3Volume(zoneSecondary.getMasterVolume().getValue());
                        state.setZone3Mute(zoneSecondary.getMute().getValue());
                        state.setZone3Input(zoneSecondary.getInputFuncSelect().getValue());
                        break;
                }
            }
        }
    }

    private void updateDisplayInfo() {
        String url = statusUrl + URL_APP_COMMAND;
        logger.trace("Refreshing URL: {}", url);

        AppCommandRequest request = AppCommandRequest.of(CommandTx.CMD_NET_STATUS);
        AppCommandResponse response = postDocument(url, AppCommandResponse.class, request);

        if (response != null) {
            CommandRx titleInfo = response.getCommands().get(0);
            state.setNowPlayingArtist(titleInfo.getText("artist"));
            state.setNowPlayingAlbum(titleInfo.getText("album"));
            state.setNowPlayingTrack(titleInfo.getText("track"));
        }
    }

    private boolean setConfigProperties() {
        String url = statusUrl + URL_DEVICE_INFO;
        logger.debug("Refreshing URL: {}", url);

        Deviceinfo deviceinfo = getDocument(url, Deviceinfo.class);
        if (deviceinfo != null) {
            config.setZoneCount(deviceinfo.getDeviceZones());
        }

        /**
         * The maximum volume is received from the telnet connection in the
         * form of the MVMAX property. It is not always received reliable however,
         * so we're using a default for now.
         */
        config.setMainVolumeMax(DenonMarantzConfiguration.MAX_VOLUME);

        // if deviceinfo is null, something went wrong (and is logged in getDocument catch blocks)
        return (deviceinfo != null);
    }

    private void refreshHttpProperties() {
        logger.trace("Refreshing Denon status");

        updateMain();
        updateMainZone();
        updateSecondaryZones();
        updateDisplayInfo();
    }

    @Nullable
    private <T> T getDocument(String uri, Class<T> response) {
        try {
            String result = HttpUtil.executeUrl("GET", uri, REQUEST_TIMEOUT_MS);
            logger.trace("result of getDocument for uri '{}':\r\n{}", uri, result);

            if (StringUtils.isNotBlank(result)) {
                JAXBContext jc = JAXBContext.newInstance(response);
                XMLInputFactory xif = XMLInputFactory.newInstance();
                XMLStreamReader xsr = xif.createXMLStreamReader(IOUtils.toInputStream(result));
                xsr = new PropertyRenamerDelegate(xsr);

                @SuppressWarnings("unchecked")
                T obj = (T) jc.createUnmarshaller().unmarshal(xsr);

                return obj;
            }
        } catch (UnmarshalException e) {
            logger.debug("Failed to unmarshal xml document: {}", e.getMessage());
        } catch (JAXBException e) {
            logger.debug("Unexpected error occurred during unmarshalling of document: {}", e.getMessage());
        } catch (XMLStreamException e) {
            logger.debug("Communication error: {}", e.getMessage());
        } catch (IOException e) {
            logger.debug("IO error while retrieving document: {}", e);
            state.connectionError("IO error while connecting to AVR: " + e.getMessage());
        }

        return null;
    }

    @Nullable
    private <T, S> T postDocument(String uri, Class<T> response, S request) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(request.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(request, sw);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(sw.toString().getBytes(StandardCharsets.UTF_8));
            String result = HttpUtil.executeUrl("POST", uri, inputStream, CONTENT_TYPE_XML, REQUEST_TIMEOUT_MS);

            if (StringUtils.isNotBlank(result)) {
                JAXBContext jcResponse = JAXBContext.newInstance(response);

                @SuppressWarnings("unchecked")
                T obj = (T) jcResponse.createUnmarshaller().unmarshal(IOUtils.toInputStream(result));

                return obj;
            }
        } catch (JAXBException e) {
            logger.debug("Encoding error in post", e);
        } catch (IOException e) {
            logger.debug("IO error while sending document: {}", e);
            state.connectionError("IO error while sending command to AVR: " + e.getMessage());
        }

        return null;
    }

    private static class PropertyRenamerDelegate extends StreamReaderDelegate {

        public PropertyRenamerDelegate(XMLStreamReader xsr) {
            super(xsr);
        }

        @Override
        public String getAttributeLocalName(int index) {
            return Introspector.decapitalize(super.getAttributeLocalName(index));
        }

        @Override
        public String getLocalName() {
            return Introspector.decapitalize(super.getLocalName());
        }
    }

}
