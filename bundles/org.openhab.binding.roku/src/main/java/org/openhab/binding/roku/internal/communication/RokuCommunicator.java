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
package org.openhab.binding.roku.internal.communication;

import java.io.StringReader;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.roku.internal.RokuHttpException;
import org.openhab.binding.roku.internal.dto.ActiveApp;
import org.openhab.binding.roku.internal.dto.Apps;
import org.openhab.binding.roku.internal.dto.Apps.App;
import org.openhab.binding.roku.internal.dto.DeviceInfo;
import org.openhab.binding.roku.internal.dto.Player;
import org.openhab.binding.roku.internal.dto.TvChannel;
import org.openhab.binding.roku.internal.dto.TvChannels;
import org.openhab.binding.roku.internal.dto.TvChannels.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods for accessing the HTTP interface of the Roku
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RokuCommunicator {
    private final Logger logger = LoggerFactory.getLogger(RokuCommunicator.class);
    private final HttpClient httpClient;

    private final String urlKeyPress;
    private final String urlLaunchApp;
    private final String urlLaunchTvChannel;
    private final String urlQryDevice;
    private final String urlQryActiveApp;
    private final String urlQryApps;
    private final String urlQryPlayer;
    private final String urlQryActiveTvChannel;
    private final String urlQryTvChannels;

    public RokuCommunicator(HttpClient httpClient, String host, int port) {
        this.httpClient = httpClient;

        final String baseUrl = "http://" + host + ":" + port;
        urlKeyPress = baseUrl + "/keypress/";
        urlLaunchApp = baseUrl + "/launch/";
        urlLaunchTvChannel = baseUrl + "/launch/tvinput.dtv?ch=";
        urlQryDevice = baseUrl + "/query/device-info";
        urlQryActiveApp = baseUrl + "/query/active-app";
        urlQryApps = baseUrl + "/query/apps";
        urlQryPlayer = baseUrl + "/query/media-player";
        urlQryActiveTvChannel = baseUrl + "/query/tv-active-channel";
        urlQryTvChannels = baseUrl + "/query/tv-channels";
    }

    /**
     * Send a keypress command to the Roku
     *
     * @param key The key code to send
     *
     */
    public void keyPress(String key) throws RokuHttpException {
        postCommand(urlKeyPress + key);
    }

    /**
     * Send a launch app command to the Roku
     *
     * @param appId The appId of the app to launch
     *
     */
    public void launchApp(String appId) throws RokuHttpException {
        postCommand(urlLaunchApp + appId);
    }

    /**
     * Send a TV channel change command to the Roku TV
     *
     * @param channelNumber The channel number of the channel to tune into, ie: 2.1
     *
     */
    public void launchTvChannel(String channelNumber) throws RokuHttpException {
        postCommand(urlLaunchTvChannel + channelNumber);
    }

    /**
     * Send a command to get device-info from the Roku and return a DeviceInfo object
     *
     * @return A DeviceInfo object populated with information about the connected Roku
     * @throws RokuHttpException
     */
    public DeviceInfo getDeviceInfo() throws RokuHttpException {
        try {
            JAXBContext ctx = JAXBUtils.JAXBCONTEXT_DEVICE_INFO;
            if (ctx != null) {
                Unmarshaller unmarshaller = ctx.createUnmarshaller();
                if (unmarshaller != null) {
                    XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY
                            .createXMLStreamReader(new StringReader(getCommand(urlQryDevice)));
                    DeviceInfo device = (DeviceInfo) unmarshaller.unmarshal(xsr);
                    if (device != null) {
                        return device;
                    }
                }
            }
            throw new RokuHttpException("No DeviceInfo model in response");
        } catch (JAXBException | XMLStreamException e) {
            throw new RokuHttpException("Exception creating DeviceInfo Unmarshaller: " + e.getLocalizedMessage());
        }
    }

    /**
     * Send a command to get active-app from the Roku and return an ActiveApp object
     *
     * @return An ActiveApp object populated with information about the current running app on the Roku
     * @throws RokuHttpException
     */
    public ActiveApp getActiveApp() throws RokuHttpException {
        try {
            JAXBContext ctx = JAXBUtils.JAXBCONTEXT_ACTIVE_APP;
            if (ctx != null) {
                Unmarshaller unmarshaller = ctx.createUnmarshaller();
                if (unmarshaller != null) {
                    XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY
                            .createXMLStreamReader(new StringReader(getCommand(urlQryActiveApp)));
                    ActiveApp activeApp = (ActiveApp) unmarshaller.unmarshal(xsr);
                    if (activeApp != null) {
                        return activeApp;
                    }
                }
            }
            throw new RokuHttpException("No ActiveApp model in response");
        } catch (JAXBException | XMLStreamException e) {
            throw new RokuHttpException("Exception creating ActiveApp Unmarshaller: " + e.getLocalizedMessage());
        }
    }

    /**
     * Send a command to get the installed app list from the Roku and return a List of App objects
     *
     * @return A List of App objects for all apps currently installed on the Roku
     * @throws RokuHttpException
     */
    public List<App> getAppList() throws RokuHttpException {
        try {
            JAXBContext ctx = JAXBUtils.JAXBCONTEXT_APPS;
            if (ctx != null) {
                Unmarshaller unmarshaller = ctx.createUnmarshaller();
                if (unmarshaller != null) {
                    XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY
                            .createXMLStreamReader(new StringReader(getCommand(urlQryApps)));
                    Apps appList = (Apps) unmarshaller.unmarshal(xsr);
                    if (appList != null) {
                        return appList.getApp();
                    }
                }
            }
            throw new RokuHttpException("No AppList model in response");
        } catch (JAXBException | XMLStreamException e) {
            throw new RokuHttpException("Exception creating AppList Unmarshaller: " + e.getLocalizedMessage());
        }
    }

    /**
     * Send a command to get media-player from the Roku and return a Player object
     *
     * @return A Player object populated with information about the current stream playing on the Roku
     * @throws RokuHttpException
     */
    public Player getPlayerInfo() throws RokuHttpException {
        try {
            JAXBContext ctx = JAXBUtils.JAXBCONTEXT_PLAYER;
            if (ctx != null) {
                Unmarshaller unmarshaller = ctx.createUnmarshaller();
                if (unmarshaller != null) {
                    XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY
                            .createXMLStreamReader(new StringReader(getCommand(urlQryPlayer)));
                    Player playerInfo = (Player) unmarshaller.unmarshal(xsr);
                    if (playerInfo != null) {
                        return playerInfo;
                    }
                }
            }
            throw new RokuHttpException("No Player info model in response");
        } catch (JAXBException | XMLStreamException e) {
            throw new RokuHttpException("Exception creating Player info Unmarshaller: " + e.getLocalizedMessage());
        }
    }

    /**
     * Send a command to get tv-active-channel from the Roku TV and return a TvChannel object
     *
     * @return A TvChannel object populated with information about the current active TV Channel
     * @throws RokuHttpException
     */
    public TvChannel getActiveTvChannel() throws RokuHttpException {
        try {
            JAXBContext ctx = JAXBUtils.JAXBCONTEXT_TVCHANNEL;
            if (ctx != null) {
                Unmarshaller unmarshaller = ctx.createUnmarshaller();
                if (unmarshaller != null) {
                    XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY
                            .createXMLStreamReader(new StringReader(getCommand(urlQryActiveTvChannel)));
                    TvChannel tvChannelInfo = (TvChannel) unmarshaller.unmarshal(xsr);
                    if (tvChannelInfo != null) {
                        return tvChannelInfo;
                    }
                }
            }
            throw new RokuHttpException("No TvChannel info model in response");
        } catch (JAXBException | XMLStreamException e) {
            throw new RokuHttpException("Exception creating TvChannel info Unmarshaller: " + e.getLocalizedMessage());
        }
    }

    /**
     * Send a command to get tv-channels from the Roku TV and return a list of Channel objects
     *
     * @return A List of Channel objects for all TV channels currently available on the Roku TV
     * @throws RokuHttpException
     */
    public List<Channel> getTvChannelList() throws RokuHttpException {
        try {
            JAXBContext ctx = JAXBUtils.JAXBCONTEXT_TVCHANNELS;
            if (ctx != null) {
                Unmarshaller unmarshaller = ctx.createUnmarshaller();
                if (unmarshaller != null) {
                    XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY
                            .createXMLStreamReader(new StringReader(getCommand(urlQryTvChannels)));
                    TvChannels tvChannels = (TvChannels) unmarshaller.unmarshal(xsr);
                    if (tvChannels != null) {
                        return tvChannels.getChannel();
                    }
                }
            }
            throw new RokuHttpException("No TvChannels info model in response");
        } catch (JAXBException | XMLStreamException e) {
            throw new RokuHttpException("Exception creating TvChannel info Unmarshaller: " + e.getLocalizedMessage());
        }
    }

    /**
     * Sends a GET command to the Roku
     *
     * @param url The url to send with the command embedded in the URI
     * @return The response content of the http request
     */
    private String getCommand(String url) {
        try {
            return httpClient.GET(url).getContentAsString();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("Error executing player GET command, URL: {}, {} ", url, e.getMessage());
            return "";
        }
    }

    /**
     * Sends a POST command to the Roku
     *
     * @param url The url to send with the command embedded in the URI
     * @throws RokuHttpException
     */
    private void postCommand(String url) throws RokuHttpException {
        try {
            httpClient.POST(url).method(HttpMethod.POST).send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new RokuHttpException("Error executing player POST command, URL: " + url + e.getMessage());
        }
    }
}
