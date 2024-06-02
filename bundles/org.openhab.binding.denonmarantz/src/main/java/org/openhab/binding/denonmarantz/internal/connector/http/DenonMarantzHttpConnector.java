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
package org.openhab.binding.denonmarantz.internal.connector.http;

import java.beans.Introspector;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.denonmarantz.internal.DenonMarantzState;
import org.openhab.binding.denonmarantz.internal.config.DenonMarantzConfiguration;
import org.openhab.binding.denonmarantz.internal.connector.DenonMarantzConnector;
import org.openhab.binding.denonmarantz.internal.exception.HttpCommunicationException;
import org.openhab.binding.denonmarantz.internal.xml.dto.Deviceinfo;
import org.openhab.binding.denonmarantz.internal.xml.dto.Main;
import org.openhab.binding.denonmarantz.internal.xml.dto.ZoneStatus;
import org.openhab.binding.denonmarantz.internal.xml.dto.ZoneStatusLite;
import org.openhab.binding.denonmarantz.internal.xml.dto.commands.AppCommandRequest;
import org.openhab.binding.denonmarantz.internal.xml.dto.commands.AppCommandResponse;
import org.openhab.binding.denonmarantz.internal.xml.dto.commands.CommandRx;
import org.openhab.binding.denonmarantz.internal.xml.dto.commands.CommandTx;
import org.openhab.binding.denonmarantz.internal.xml.dto.types.StringType;
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
@NonNullByDefault
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

    private @Nullable ScheduledFuture<?> pollingJob;

    private boolean legacyApiSupported = true;

    public DenonMarantzHttpConnector(DenonMarantzConfiguration config, DenonMarantzState state,
            ScheduledExecutorService scheduler, HttpClient httpClient) {
        super(config, scheduler, state);
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
        if (!isPolling()) {
            logger.debug("HTTP polling started.");
            try {
                setConfigProperties();
            } catch (TimeoutException | ExecutionException | HttpCommunicationException e) {
                logger.debug("IO error while retrieving document:", e);
                state.connectionError("IO error while connecting to AVR: " + e.getMessage());
                return;
            } catch (InterruptedException e) {
                logger.debug("Interrupted while retrieving document: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }

            pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    refreshHttpProperties();
                } catch (TimeoutException | ExecutionException e) {
                    logger.debug("IO error while retrieving document", e);
                    state.connectionError("IO error while connecting to AVR: " + e.getMessage());
                    stopPolling();
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
                } catch (InterruptedException e) {
                    logger.debug("Interrupted while polling: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }, 0, config.httpPollingInterval, TimeUnit.SECONDS);
        }
    }

    private boolean isPolling() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        return pollingJob != null && !pollingJob.isCancelled();
    }

    private void stopPolling() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
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

    @Override
    protected void internalSendCommand(String command) {
        logger.debug("Sending command '{}'", command);
        if (command.isBlank()) {
            logger.warn("Trying to send empty command");
            return;
        }

        String url = cmdUrl + URLEncoder.encode(command, Charset.defaultCharset());
        logger.trace("Calling url {}", url);

        httpClient.newRequest(url).timeout(5, TimeUnit.SECONDS).send(new Response.CompleteListener() {
            @Override
            public void onComplete(@Nullable Result result) {
                if (result != null && result.getResponse().getStatus() != 200) {
                    logger.warn("Error {} while sending command", result.getResponse().getReason());
                }
            }
        });
    }

    private void updateMain() throws TimeoutException, ExecutionException, InterruptedException {
        String url = statusUrl + URL_MAIN;
        logger.trace("Refreshing URL: {}", url);

        try {
            Main statusMain = getDocument(url, Main.class);
            if (statusMain != null) {
                state.setPower(statusMain.getPower().getValue());
            }
        } catch (HttpCommunicationException e) {
            if (e.getHttpStatus() == HttpStatus.FORBIDDEN_403) {
                legacyApiSupported = false;
                logger.debug("Legacy API not supported, will attempt app command method");
            } else {
                logger.debug("Failed to update main by legacy API: {}", e.getMessage());
            }
        }
    }

    private void updateMainZone() throws TimeoutException, ExecutionException, InterruptedException {
        String url = statusUrl + URL_ZONE_MAIN;
        logger.trace("Refreshing URL: {}", url);

        try {
            ZoneStatus mainZone = getDocument(url, ZoneStatus.class);
            if (mainZone != null) {
                state.setInput(mainZone.getInputFuncSelect().getValue());
                state.setMainVolume(mainZone.getMasterVolume().getValue());
                state.setMainZonePower(mainZone.getPower().getValue());
                state.setMute(mainZone.getMute().getValue());

                if (config.inputOptions == null) {
                    config.inputOptions = mainZone.getInputFuncList();
                }

                StringType surroundMode = mainZone.getSurrMode();
                if (surroundMode == null) {
                    logger.debug("Unable to get the SURROUND_MODE. MainZone update may not be correct.");
                } else {
                    state.setSurroundProgram(surroundMode.getValue());
                }
            }
        } catch (HttpCommunicationException e) {
            if (e.getHttpStatus() == HttpStatus.FORBIDDEN_403) {
                legacyApiSupported = false;
                logger.debug("Legacy API not supported, will attempt app command method");
            } else {
                logger.debug("Failed to update main zone by legacy API: {}", e.getMessage());
            }
        }
    }

    private void updateMainZoneByAppCommand() throws TimeoutException, ExecutionException, InterruptedException {
        String url = statusUrl + URL_APP_COMMAND;
        logger.trace("Refreshing URL: {}", url);

        AppCommandRequest request = AppCommandRequest.of(CommandTx.CMD_ALL_POWER).add(CommandTx.CMD_VOLUME_LEVEL)
                .add(CommandTx.CMD_MUTE_STATUS).add(CommandTx.CMD_SOURCE_STATUS).add(CommandTx.CMD_SURROUND_STATUS);

        try {
            AppCommandResponse response = postDocument(url, AppCommandResponse.class, request);

            if (response != null) {
                for (CommandRx rx : response.getCommands()) {
                    String inputSource = rx.getSource();
                    if (inputSource != null) {
                        state.setInput(inputSource);
                    }
                    Boolean power = rx.getZone1();
                    if (power != null) {
                        state.setMainZonePower(power.booleanValue());
                    }
                    BigDecimal volume = rx.getVolume();
                    if (volume != null) {
                        state.setMainVolume(volume);
                    }
                    Boolean mute = rx.getMute();
                    if (mute != null) {
                        state.setMute(mute.booleanValue());
                    }
                    String surroundMode = rx.getSurround();
                    if (surroundMode != null) {
                        state.setSurroundProgram(surroundMode);
                    }
                }
            }
        } catch (HttpCommunicationException e) {
            logger.debug("Failed to update main zone by app command: {}", e.getMessage());
        }
    }

    private void updateSecondaryZones() throws TimeoutException, ExecutionException, InterruptedException {
        for (int i = 2; i <= config.getZoneCount(); i++) {
            String url = String.format("%s" + URL_ZONE_SECONDARY_LITE, statusUrl, i, i);
            logger.trace("Refreshing URL: {}", url);
            try {
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
                        case 4:
                            state.setZone4Power(zoneSecondary.getPower().getValue());
                            state.setZone4Volume(zoneSecondary.getMasterVolume().getValue());
                            state.setZone4Mute(zoneSecondary.getMute().getValue());
                            state.setZone4Input(zoneSecondary.getInputFuncSelect().getValue());
                            break;
                    }
                }
            } catch (HttpCommunicationException e) {
                logger.debug("Failed to update zone {}: {}", i, e.getMessage());
            }
        }
    }

    private void updateDisplayInfo() throws TimeoutException, ExecutionException, InterruptedException {
        String url = statusUrl + URL_APP_COMMAND;
        logger.trace("Refreshing URL: {}", url);

        AppCommandRequest request = AppCommandRequest.of(CommandTx.CMD_NET_STATUS);
        try {
            AppCommandResponse response = postDocument(url, AppCommandResponse.class, request);

            if (response == null) {
                return;
            }
            CommandRx titleInfo = response.getCommands().get(0);
            String artist = titleInfo.getText("artist");
            if (artist != null) {
                state.setNowPlayingArtist(artist);
            }
            String album = titleInfo.getText("album");
            if (album != null) {
                state.setNowPlayingAlbum(album);
            }
            String track = titleInfo.getText("track");
            if (track != null) {
                state.setNowPlayingTrack(track);
            }
        } catch (HttpCommunicationException e) {
            logger.debug("Failed to update display info: {}", e.getMessage());
        }
    }

    private boolean setConfigProperties()
            throws TimeoutException, ExecutionException, InterruptedException, HttpCommunicationException {
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

    private void refreshHttpProperties() throws TimeoutException, ExecutionException, InterruptedException {
        logger.trace("Refreshing Denon status");

        if (legacyApiSupported) {
            updateMain();
            updateMainZone();
        }

        if (!legacyApiSupported) {
            updateMainZoneByAppCommand();
        }

        updateSecondaryZones();
        updateDisplayInfo();
    }

    @Nullable
    private <T> T getDocument(String uri, Class<T> response)
            throws TimeoutException, ExecutionException, InterruptedException, HttpCommunicationException {
        try {
            Request request = httpClient.newRequest(uri).timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .method(HttpMethod.GET);

            ContentResponse contentResponse = request.send();

            String result = contentResponse.getContentAsString();
            int status = contentResponse.getStatus();

            logger.trace("result of getDocument for uri '{}' (status code {}):\r\n{}", uri, status, result);

            if (!HttpStatus.isSuccess(status)) {
                throw new HttpCommunicationException("Error retrieving document for uri '" + uri + "'", status);
            }

            if (result != null && !result.isBlank()) {
                JAXBContext jc = JAXBContext.newInstance(response);
                XMLInputFactory xif = XMLInputFactory.newInstance();
                xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
                xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
                XMLStreamReader xsr = xif
                        .createXMLStreamReader(new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8)));
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
        }

        return null;
    }

    @Nullable
    private <T, S> T postDocument(String uri, Class<T> response, S request)
            throws TimeoutException, ExecutionException, InterruptedException, HttpCommunicationException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(request.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(request, sw);

            Request httpRequest = httpClient.newRequest(uri).timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .content(new StringContentProvider(sw.toString(), StandardCharsets.UTF_8), CONTENT_TYPE_XML)
                    .method(HttpMethod.POST);

            ContentResponse contentResponse = httpRequest.send();

            String result = contentResponse.getContentAsString();
            int status = contentResponse.getStatus();

            logger.trace("result of postDocument for uri '{}' (status code {}):\r\n{}", uri, status, result);

            if (!HttpStatus.isSuccess(status)) {
                throw new HttpCommunicationException("Error retrieving document for uri '" + uri + "'", status);
            }

            if (result != null && !result.isBlank()) {
                JAXBContext jcResponse = JAXBContext.newInstance(response);

                @SuppressWarnings("unchecked")
                T obj = (T) jcResponse.createUnmarshaller()
                        .unmarshal(new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8)));

                return obj;
            }
        } catch (JAXBException e) {
            logger.debug("Encoding error in post", e);
        }

        return null;
    }

    private static class PropertyRenamerDelegate extends StreamReaderDelegate {

        public PropertyRenamerDelegate(XMLStreamReader xsr) {
            super(xsr);
        }

        @Override
        public String getAttributeLocalName(int index) {
            return Introspector.decapitalize(super.getAttributeLocalName(index)).intern();
        }

        @Override
        public String getLocalName() {
            return Introspector.decapitalize(super.getLocalName()).intern();
        }
    }
}
