/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ipcamera.internal.onvif;

import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.*;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.Helper;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateOption;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * The {@link OnvifConnection} This is a basic Netty implementation for connecting and communicating to ONVIF cameras.
 *
 * @author Matthew Skinner - Initial contribution
 * @author Kai Kreuzer - Improve handling for certain cameras
 */

@NonNullByDefault
public class OnvifConnection {
    public enum RequestType {
        AbsoluteMove,
        AddPTZConfiguration,
        ContinuousMoveLeft,
        ContinuousMoveRight,
        ContinuousMoveUp,
        ContinuousMoveDown,
        Stop,
        ContinuousMoveIn,
        ContinuousMoveOut,
        CreatePullPointSubscription,
        GetCapabilities,
        GetDeviceInformation,
        GetProfiles,
        GetServiceCapabilities,
        GetSnapshotUri,
        GetStreamUri,
        GetSystemDateAndTime,
        Subscribe,
        Unsubscribe,
        PullMessages,
        GetEventProperties,
        RelativeMoveLeft,
        RelativeMoveRight,
        RelativeMoveUp,
        RelativeMoveDown,
        RelativeMoveIn,
        RelativeMoveOut,
        Renew,
        GetConfigurations,
        GetConfigurationOptions,
        GetConfiguration,
        SetConfiguration,
        GetNodes,
        GetStatus,
        GotoPreset,
        GetPresets
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(2);
    private @Nullable Bootstrap bootstrap;
    private EventLoopGroup mainEventLoopGroup = new NioEventLoopGroup(2);
    private ReentrantLock connecting = new ReentrantLock();
    private String ipAddress = "";
    private String user = "";
    private String password = "";
    private int onvifPort = 80;
    public String deviceXAddr = "http://" + ipAddress + "/onvif/device_service";
    private String eventXAddr = "http://" + ipAddress + "/onvif/device_service";
    private String mediaXAddr = "http://" + ipAddress + "/onvif/device_service";
    @SuppressWarnings("unused")
    private String imagingXAddr = "http://" + ipAddress + "/onvif/device_service";
    private String ptzXAddr = "http://" + ipAddress + "/onvif/ptz_service";
    public String subscriptionXAddr = "";
    public String subscriptionId = "";
    private boolean isConnected = false;
    private int mediaProfileIndex = 0;
    private String rtspUri = "";
    private IpCameraHandler ipCameraHandler;
    // Use/skip events even if camera support them. API cameras skip, as their own methods give better results.
    private boolean usingEvents = false;
    private int onvifEventServiceType = 0; // 0 = auto detect, 1 = disabled, 2 = PullMessages, 3 = WSBaseSubscription
    public AtomicInteger pullMessageRequests = new AtomicInteger();
    private long createSubscriptionTimestamp;

    // These hold the cameras PTZ position in the range that the camera uses, ie
    // mine is -1 to +1
    private Float panRangeMin = -1.0f;
    private Float panRangeMax = 1.0f;
    private Float tiltRangeMin = -1.0f;
    private Float tiltRangeMax = 1.0f;
    private Float zoomMin = 0.0f;
    private Float zoomMax = 1.0f;
    // These hold the PTZ values for updating openHABs controls in 0-100 range
    private Float currentPanPercentage = 0.0f;
    private Float currentTiltPercentage = 0.0f;
    private Float currentZoomPercentage = 0.0f;
    private Float currentPanCamValue = 0.0f;
    private Float currentTiltCamValue = 0.0f;
    private Float currentZoomCamValue = 0.0f;
    private String ptzNodeToken = "000";
    private String ptzConfigToken = "000";
    private int presetTokenIndex = 0;
    private List<String> presetTokens = new LinkedList<>();
    private List<String> presetNames = new LinkedList<>();
    private List<String> mediaProfileTokens = new LinkedList<>();
    private boolean ptzDevice = true;

    public OnvifConnection(IpCameraHandler ipCameraHandler, String ipAddress, String user, String password) {
        this.ipCameraHandler = ipCameraHandler;
        if (!ipAddress.isEmpty()) {
            this.user = user;
            this.password = password;
            getIPandPortFromUrl(ipAddress);
        }
    }

    private String getXml(RequestType requestType) {
        try {
            switch (requestType) {
                case AbsoluteMove:
                    return "<AbsoluteMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex) + "</ProfileToken><Position><PanTilt x=\""
                            + currentPanCamValue + "\" y=\"" + currentTiltCamValue
                            + "\" space=\"http://www.onvif.org/ver10/tptz/PanTiltSpaces/PositionGenericSpace\">\n"
                            + "</PanTilt>\n" + "<Zoom x=\"" + currentZoomCamValue
                            + "\" space=\"http://www.onvif.org/ver10/tptz/ZoomSpaces/PositionGenericSpace\">\n"
                            + "</Zoom>\n" + "</Position>\n"
                            + "<Speed><PanTilt x=\"0.1\" y=\"0.1\" space=\"http://www.onvif.org/ver10/tptz/PanTiltSpaces/GenericSpeedSpace\"></PanTilt><Zoom x=\"1.0\" space=\"http://www.onvif.org/ver10/tptz/ZoomSpaces/ZoomGenericSpeedSpace\"></Zoom>\n"
                            + "</Speed></AbsoluteMove>";
                case AddPTZConfiguration: // not tested to work yet
                    return "<AddPTZConfiguration xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex) + "</ProfileToken><ConfigurationToken>"
                            + ptzConfigToken + "</ConfigurationToken></AddPTZConfiguration>";
                case ContinuousMoveLeft:
                    return "<ContinuousMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex)
                            + "</ProfileToken><Velocity><PanTilt x=\"-0.5\" y=\"0\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Velocity></ContinuousMove>";
                case ContinuousMoveRight:
                    return "<ContinuousMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex)
                            + "</ProfileToken><Velocity><PanTilt x=\"0.5\" y=\"0\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Velocity></ContinuousMove>";
                case ContinuousMoveUp:
                    return "<ContinuousMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex)
                            + "</ProfileToken><Velocity><PanTilt x=\"0\" y=\"-0.5\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Velocity></ContinuousMove>";
                case ContinuousMoveDown:
                    return "<ContinuousMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex)
                            + "</ProfileToken><Velocity><PanTilt x=\"0\" y=\"0.5\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Velocity></ContinuousMove>";
                case Stop:
                    return "<Stop xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex)
                            + "</ProfileToken><PanTilt>true</PanTilt><Zoom>true</Zoom></Stop>";
                case ContinuousMoveIn:
                    return "<ContinuousMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex)
                            + "</ProfileToken><Velocity><Zoom x=\"0.5\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Velocity></ContinuousMove>";
                case ContinuousMoveOut:
                    return "<ContinuousMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex)
                            + "</ProfileToken><Velocity><Zoom x=\"-0.5\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Velocity></ContinuousMove>";
                case CreatePullPointSubscription:
                    return "<CreatePullPointSubscription xmlns=\"http://www.onvif.org/ver10/events/wsdl\"><InitialTerminationTime>PT600S</InitialTerminationTime></CreatePullPointSubscription>";
                case GetCapabilities:
                    return "<GetCapabilities xmlns=\"http://www.onvif.org/ver10/device/wsdl\"><Category>All</Category></GetCapabilities>";

                case GetDeviceInformation:
                    return "<GetDeviceInformation xmlns=\"http://www.onvif.org/ver10/device/wsdl\"/>";
                case GetProfiles:
                    return "<GetProfiles xmlns=\"http://www.onvif.org/ver10/media/wsdl\"/>";
                case GetServiceCapabilities:
                    return "<GetServiceCapabilities xmlns=\"http://www.onvif.org/ver10/events/wsdl\"></GetServiceCapabilities>";
                case GetSnapshotUri:
                    return "<GetSnapshotUri xmlns=\"http://www.onvif.org/ver10/media/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex) + "</ProfileToken></GetSnapshotUri>";
                case GetStreamUri:
                    return "<GetStreamUri xmlns=\"http://www.onvif.org/ver10/media/wsdl\"><StreamSetup><Stream xmlns=\"http://www.onvif.org/ver10/schema\">RTP-Unicast</Stream><Transport xmlns=\"http://www.onvif.org/ver10/schema\"><Protocol>RTSP</Protocol></Transport></StreamSetup><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex) + "</ProfileToken></GetStreamUri>";
                case GetSystemDateAndTime:
                    return "<GetSystemDateAndTime xmlns=\"http://www.onvif.org/ver10/device/wsdl\"/>";
                case Subscribe:
                    return "<Subscribe xmlns=\"http://docs.oasis-open.org/wsn/b-2\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"><ConsumerReference><wsa:Address>http://"
                            + ipCameraHandler.hostIp + ":" + SERVLET_PORT + "/ipcamera/"
                            + ipCameraHandler.getThing().getUID().getId()
                            + "/OnvifEvent</wsa:Address></ConsumerReference><InitialTerminationTime>PT600S</InitialTerminationTime></Subscribe>";
                case Unsubscribe:
                    return "<Unsubscribe xmlns=\"http://docs.oasis-open.org/wsn/b-2\"></Unsubscribe>";
                case PullMessages:
                    return "<PullMessages xmlns=\"http://www.onvif.org/ver10/events/wsdl\"><Timeout>PT8S</Timeout><MessageLimit>10</MessageLimit></PullMessages>";
                case GetEventProperties:
                    return "<GetEventProperties xmlns=\"http://www.onvif.org/ver10/events/wsdl\"/>";
                case RelativeMoveLeft:
                    return "<RelativeMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex)
                            + "</ProfileToken><Translation><PanTilt x=\"0.05000000\" y=\"0\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Translation></RelativeMove>";
                case RelativeMoveRight:
                    return "<RelativeMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex)
                            + "</ProfileToken><Translation><PanTilt x=\"-0.05000000\" y=\"0\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Translation></RelativeMove>";
                case RelativeMoveUp:
                    return "<RelativeMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex)
                            + "</ProfileToken><Translation><PanTilt x=\"0\" y=\"0.100000000\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Translation></RelativeMove>";
                case RelativeMoveDown:
                    return "<RelativeMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex)
                            + "</ProfileToken><Translation><PanTilt x=\"0\" y=\"-0.100000000\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Translation></RelativeMove>";
                case RelativeMoveIn:
                    return "<RelativeMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex)
                            + "</ProfileToken><Translation><Zoom x=\"0.0240506344\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Translation></RelativeMove>";
                case RelativeMoveOut:
                    return "<RelativeMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex)
                            + "</ProfileToken><Translation><Zoom x=\"-0.0240506344\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Translation></RelativeMove>";
                case Renew:
                    return "<Renew xmlns=\"http://docs.oasis-open.org/wsn/b-2\"><TerminationTime>PT600S</TerminationTime></Renew>";
                case GetConfigurations:
                    return "<GetConfigurations xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"></GetConfigurations>";
                case GetConfigurationOptions:
                    return "<GetConfigurationOptions xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ConfigurationToken>"
                            + ptzConfigToken + "</ConfigurationToken></GetConfigurationOptions>";
                case GetConfiguration:
                    return "<GetConfiguration xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><PTZConfigurationToken>"
                            + ptzConfigToken + "</PTZConfigurationToken></GetConfiguration>";
                case SetConfiguration:// not tested to work yet
                    return "<SetConfiguration xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><PTZConfiguration><NodeToken>"
                            + ptzNodeToken
                            + "</NodeToken><DefaultAbsolutePantTiltPositionSpace>AbsolutePanTiltPositionSpace</DefaultAbsolutePantTiltPositionSpace><DefaultAbsoluteZoomPositionSpace>AbsoluteZoomPositionSpace</DefaultAbsoluteZoomPositionSpace></PTZConfiguration></SetConfiguration>";
                case GetNodes:
                    return "<GetNodes xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"></GetNodes>";
                case GetStatus:
                    return "<GetStatus xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex) + "</ProfileToken></GetStatus>";
                case GotoPreset:
                    return "<GotoPreset xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex) + "</ProfileToken><PresetToken>"
                            + presetTokens.get(presetTokenIndex) + "</PresetToken></GotoPreset>";
                case GetPresets:
                    return "<GetPresets xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                            + mediaProfileTokens.get(mediaProfileIndex) + "</ProfileToken></GetPresets>";
            }
        } catch (IndexOutOfBoundsException e) {
            if (!isConnected) {
                logger.debug("IndexOutOfBoundsException occurred, camera is not connected via ONVIF: {}",
                        e.getMessage());
            } else {
                logger.debug("IndexOutOfBoundsException occurred, {}", e.getMessage());
            }
        }
        return "notfound";
    }

    public void processReply(RequestType requestType, String message) {
        logger.trace("ONVIF {} reply is: {}", requestType, message);
        switch (requestType) {
            case CreatePullPointSubscription:
                setSubscriptionXAddr(message);
                if (!subscriptionXAddr.isEmpty()) {
                    sendOnvifRequest(RequestType.PullMessages, subscriptionXAddr);
                }
                break;
            case Subscribe:
                setSubscriptionXAddr(message);
                break;
            case GetCapabilities:
                parseXAddr(message);
                setOnvifEventServiceType(message.contains("WSPullPointSupport>true"),
                        message.contains("WSSubscriptionPolicySupport>true"));
                if (!getEventsSupported() && ipCameraHandler.cameraConfig.getOnvifEventServiceType() != 1) {
                    // If the camera does not report event capabilities here we also check with GetServiceCapabilities.
                    sendOnvifRequest(RequestType.GetServiceCapabilities, mediaXAddr);
                } else {
                    sendOnvifRequest(RequestType.GetProfiles, mediaXAddr);
                }
                break;
            case GetDeviceInformation:
                break;
            case GetProfiles:
                setIsConnected(true);
                parseProfiles(message);
                sendOnvifRequest(RequestType.GetSnapshotUri, mediaXAddr);
                sendOnvifRequest(RequestType.GetStreamUri, mediaXAddr);
                if (ptzDevice) {
                    sendPTZRequest(RequestType.GetNodes);
                }
                if (usingEvents) {// stops API cameras from getting sent ONVIF events.
                    createSubscription();
                }
                break;
            case GetServiceCapabilities:
                setOnvifEventServiceType(message.contains("WSPullPointSupport=\"true\""),
                        message.contains("WSSubscriptionPolicySupport=\"true\""));
                sendOnvifRequest(RequestType.GetProfiles, mediaXAddr);
                break;
            case GetSnapshotUri:
                String url = Helper.fetchXML(message, ":MediaUri", ":Uri");
                if (!url.isBlank()) {
                    logger.debug("GetSnapshotUri: {}", url);
                    if (ipCameraHandler.snapshotUri.isEmpty()
                            && !"ffmpeg".equals(ipCameraHandler.cameraConfig.getSnapshotUrl())) {
                        ipCameraHandler.snapshotUri = ipCameraHandler.getCorrectUrlFormat(url);
                        if (ipCameraHandler.getPortFromShortenedUrl(url) != ipCameraHandler.cameraConfig.getPort()) {
                            logger.warn(
                                    "ONVIF is reporting the snapshot does not match the things configured port of:{}",
                                    ipCameraHandler.cameraConfig.getPort());
                        }
                    }
                }
                break;
            case GetStreamUri:
                String xml = StringUtils.unEscapeXml(Helper.fetchXML(message, ":MediaUri", ":Uri>"));
                if (xml != null) {
                    rtspUri = xml;
                    logger.debug("GetStreamUri: {}", rtspUri);
                    if (ipCameraHandler.rtspUri.isEmpty()) {// only use if not hard coded in initialize()
                        ipCameraHandler.rtspUri = rtspUri;
                    }
                }
                break;
            case GetSystemDateAndTime:
                setIsConnected(true);// Instar profile T only cameras need this
                parseDateAndTime(message);
                break;
            case PullMessages:
                try {
                    eventReceived(message);
                } catch (Exception e) {
                    logger.error("Error processing PullMessages error:\n{}\nmessage: {}", e.toString(), message);
                }
                if (!subscriptionXAddr.isEmpty()) {
                    sendOnvifRequest(RequestType.PullMessages, subscriptionXAddr);
                }
                break;
            case GetEventProperties:
                break;
            case Renew:
                break;
            case GetConfiguration:
                sendPTZRequest(RequestType.GetPresets);
                ptzConfigToken = Helper.fetchXML(message, "PTZConfiguration", "token=\"");
                logger.debug("ptzConfigToken={}", ptzConfigToken);
                sendPTZRequest(RequestType.GetConfigurationOptions);
                break;
            case GetNodes:
                sendPTZRequest(RequestType.GetStatus);
                ptzNodeToken = Helper.fetchXML(message, "", "token=\"");
                logger.debug("ptzNodeToken={}", ptzNodeToken);
                sendPTZRequest(RequestType.GetConfigurations);
                break;
            case GetStatus:
                processPTZLocation(message);
                break;
            case GetPresets:
                parsePresets(message);
                break;
            default:
                break;
        }
    }

    private void setOnvifEventServiceType(boolean cameraSupportsPullPointSupport,
            boolean cameraSupportsSubscriptionPolicySupport) {
        // 0 = auto detect, 1 = disabled, 2 = PullMessages, 3 = WSBaseSubscription
        if (cameraSupportsPullPointSupport && ipCameraHandler.cameraConfig.getOnvifEventServiceType() == 0) {
            onvifEventServiceType = 2;
        } else if (cameraSupportsSubscriptionPolicySupport
                && ipCameraHandler.cameraConfig.getOnvifEventServiceType() == 0) {
            onvifEventServiceType = 3;
        } else if (ipCameraHandler.cameraConfig.getOnvifEventServiceType() == 0) {
            logger.warn(
                    "Camera at {} could not auto detect the ONVIF event method the camera supports, try setting the configuration away from auto to remove this message by forcing an option.",
                    ipAddress);
        } else {
            onvifEventServiceType = ipCameraHandler.cameraConfig.getOnvifEventServiceType();
        }
    }

    public void processBadRequest(RequestType requestType) {
        logger.trace("ONVIF {} processing bad request for camera {}.", requestType, ipAddress);
        switch (requestType) {
            case CreatePullPointSubscription:
                subscriptionXAddr = "";
                logger.debug("Camera {} returned bad request on CreatePullPointSubscription. Trying again later.",
                        ipAddress);
                break;
            case Subscribe:
                subscriptionXAddr = "";
                logger.debug("Camera {} returned bad request on WSBaseSubscription. Trying again later.", ipAddress);
                break;
            case GetServiceCapabilities:
                logger.debug(
                        "Camera {} returned bad request on GetServiceCapabilities. Cannot auto detect supported event types.",
                        ipAddress);
                sendOnvifRequest(RequestType.GetProfiles, mediaXAddr);
                break;
            case PullMessages:
                logger.debug("PullMessages returned bad request for camera {}, re-creating subscription now",
                        ipAddress);
                createSubscription();
                break;
            case Renew:
                logger.debug("Renew subscription returned bad request for camera {}, re-creating subscription now",
                        ipAddress);
                createSubscription();
                break;
            default:
                break;
        }
    }

    void setSubscriptionXAddr(String message) {
        subscriptionXAddr = Helper.fetchXML(message, "SubscriptionReference>", "Address>");
        int start = message.indexOf("<dom0:SubscriptionId");
        int end = message.indexOf("</dom0:SubscriptionId>");
        if (start > -1 && end > start) {
            subscriptionId = message.substring(start, end + 22);
        }
        logger.debug("subscriptionXAddr={} subscriptionId={}", subscriptionXAddr, subscriptionId);
    }

    public void createSubscription() {
        if (!getEventsSupported()) {
            logger.debug("ONVIF events are disabled or not supported for camera at {}", ipAddress);
            return;
        }

        // Only send new subscription every 5 seconds if the camera is offline or there are already too much
        // subscriptions.
        if (createSubscriptionTimestamp == 0) {
            createSubscriptionTimestamp = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - createSubscriptionTimestamp < 5000) {
            // Subscription sent less than 5 seconds ago.
            return;
        }

        // Prefer PullPoint events over WSBaseSubscription because there is no way to check if a WSBaseSubscription is
        // already registered on the camera.
        if (onvifEventServiceType == 2) {
            sendOnvifRequest(RequestType.CreatePullPointSubscription, eventXAddr);
        } else if (onvifEventServiceType == 3) {
            sendOnvifRequest(RequestType.Subscribe, eventXAddr);
        }
    }

    /**
     * This method should be executed regularly to renew the event subscription and to check if a new subscription is
     * needed.
     */
    public void checkAndRenewEventSubscription() {
        if (getEventsSupported() && onvifEventServiceType == 2) {
            if (subscriptionXAddr.isEmpty()) {
                // The camera claims to have event support, but no subscription was created yet. Try to create a new
                // subscription.
                createSubscription();
            } else if (pullMessageRequests.intValue() == 0) {
                // If we get events via PullMessages, check if a PullMessages request is running. Netty's
                // IdleStateHandler
                // will know if any request fails after a set time expires.
                sendOnvifRequest(RequestType.Renew, subscriptionXAddr);
                logger.debug("The alarm stream was not running for camera {}, re-starting it now", ipAddress);
                sendOnvifRequest(RequestType.PullMessages, subscriptionXAddr);
            } else {
                sendOnvifRequest(RequestType.Renew, subscriptionXAddr);
            }
        }
    }

    /**
     * The {@link removeIPandPortFromUrl} Will throw away all text before the cameras IP, also removes the IP and the
     * PORT
     * leaving just the URL.
     *
     * @author Matthew Skinner - Initial contribution
     */
    String removeIPandPortFromUrl(String url) {
        int index = url.indexOf("//");
        if (index != -1) {// now remove the :port
            index = url.indexOf("/", index + 2);
        }
        if (index == -1) {
            logger.debug("We hit an issue parsing url: {}", url);
            return "";
        }
        return url.substring(index);
    }

    String extractIPportFromUrl(String url) {
        int startIndex = url.indexOf("//") + 2;
        int endIndex = url.indexOf("/", startIndex);// skip past any :port to the slash /
        if (startIndex != -1 && endIndex != -1) {
            return url.substring(startIndex, endIndex);
        }
        logger.debug("We hit an issue extracting IP:PORT from url: {}", url);
        return "";
    }

    int extractPortFromUrl(String url) {
        int startIndex = url.indexOf("//") + 2;// skip past http://
        startIndex = url.indexOf(":", startIndex);
        if (startIndex == -1) {// no port defined so use port 80
            return 80;
        }
        int endIndex = url.indexOf("/", startIndex);// skip past any :port to the slash /
        if (endIndex == -1) {
            return 80;
        }
        return Integer.parseInt(url.substring(startIndex + 1, endIndex));
    }

    void parseXAddr(String message) {
        // Normally I would search '<tt:XAddr>' instead but Foscam needed this work around.
        String temp = Helper.fetchXML(message, "<tt:Device", "tt:XAddr");
        if (!temp.isEmpty()) {
            deviceXAddr = temp;
            logger.debug("deviceXAddr: {}", deviceXAddr);
        }
        temp = Helper.fetchXML(message, "<tt:Events", "tt:XAddr");
        if (!temp.isEmpty()) {
            eventXAddr = temp;
            logger.debug("eventsXAddr: {}", eventXAddr);
        }
        temp = Helper.fetchXML(message, "<tt:Media", "tt:XAddr");
        if (!temp.isEmpty()) {
            mediaXAddr = temp;
            logger.debug("mediaXAddr: {}", mediaXAddr);
        }

        ptzXAddr = Helper.fetchXML(message, "<tt:PTZ", "tt:XAddr");
        if (ptzXAddr.isEmpty()) {
            ptzDevice = false;
            logger.debug("Camera has no ONVIF PTZ support.");
            List<org.openhab.core.thing.Channel> removeChannels = new ArrayList<>();
            org.openhab.core.thing.Channel channel = ipCameraHandler.getThing().getChannel(CHANNEL_PAN);
            if (channel != null) {
                removeChannels.add(channel);
            }
            channel = ipCameraHandler.getThing().getChannel(CHANNEL_TILT);
            if (channel != null) {
                removeChannels.add(channel);
            }
            channel = ipCameraHandler.getThing().getChannel(CHANNEL_ZOOM);
            if (channel != null) {
                removeChannels.add(channel);
            }
            ipCameraHandler.removeChannels(removeChannels);
        } else {
            logger.debug("ptzXAddr: {}", ptzXAddr);
        }
    }

    private void parseDateAndTime(String message) {
        Date openHABTime = new Date();
        String minute = Helper.fetchXML(message, "UTCDateTime", "Minute>");
        String hour = Helper.fetchXML(message, "UTCDateTime", "Hour>");
        String second = Helper.fetchXML(message, "UTCDateTime", "Second>");
        String day = Helper.fetchXML(message, "UTCDateTime", "Day>");
        String month = Helper.fetchXML(message, "UTCDateTime", "Month>");
        String year = Helper.fetchXML(message, "UTCDateTime", "Year>");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d'T'H:m:s");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            String time = year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":" + second;
            Date cameraUTC = dateFormat.parse(time);
            long timeOffset = cameraUTC.getTime() - openHABTime.getTime();
            logger.debug("Camera  UTC dateTime is: {} openHAB time is {} time is offset by {}ms",
                    dateFormat.format(cameraUTC.getTime()), dateFormat.format(openHABTime.getTime()), timeOffset);
            if (timeOffset > 5000 || timeOffset < -5000) {
                logger.warn(
                        "ONVIF time in camera does not match openHAB's time, this can cause authentication issues as ONVIF requires the time to be close to each other");
            }
        } catch (ParseException e) {
            logger.debug("Cameras time and date could not be parsed");
        }
    }

    private String getUTCdateTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }

    String createNonce() {
        Random nonce = new SecureRandom();
        return "" + nonce.nextInt();
    }

    String encodeBase64(String raw) {
        return Base64.getEncoder().encodeToString(raw.getBytes());
    }

    String createDigest(String nOnce, String dateTime) {
        String beforeEncryption = nOnce + dateTime + password;
        MessageDigest msgDigest;
        byte[] encryptedRaw = null;
        try {
            msgDigest = MessageDigest.getInstance("SHA-1");
            msgDigest.reset();
            msgDigest.update(beforeEncryption.getBytes(StandardCharsets.UTF_8));
            encryptedRaw = msgDigest.digest();
        } catch (NoSuchAlgorithmException e) {
        }
        return Base64.getEncoder().encodeToString(encryptedRaw);
    }

    public void sendOnvifRequest(RequestType requestType, String xAddr) {
        logger.trace("Sending ONVIF request: {} to {}", requestType, xAddr);
        int port = extractPortFromUrl(xAddr);
        String security = "";
        String extraEnvelope = "";
        String headerTo = "";
        String getXmlCache = getXml(requestType);
        if (requestType.equals(RequestType.CreatePullPointSubscription) || requestType.equals(RequestType.PullMessages)
                || requestType.equals(RequestType.Renew) || requestType.equals(RequestType.Unsubscribe)) {
            headerTo = "<a:To s:mustUnderstand=\"1\">" + xAddr + "</a:To>";
            extraEnvelope = " xmlns:a=\"http://www.w3.org/2005/08/addressing\"";
        }
        String headers;
        if (!password.isEmpty() && !requestType.equals(RequestType.GetSystemDateAndTime)) {
            String nonce = createNonce();
            String dateTime = getUTCdateTime();
            String digest = createDigest(nonce, dateTime);
            security = "<Security s:mustUnderstand=\"1\" xmlns=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\"><UsernameToken><Username>"
                    + user
                    + "</Username><Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">"
                    + digest
                    + "</Password><Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">"
                    + encodeBase64(nonce)
                    + "</Nonce><Created xmlns=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">"
                    + dateTime + "</Created></UsernameToken></Security>";

            if (requestType.equals(RequestType.PullMessages) || requestType.equals(RequestType.Renew)) {
                headers = "<s:Header>" + security + headerTo + subscriptionId + "</s:Header>";
            } else {
                headers = "<s:Header>" + security + headerTo + "</s:Header>";
            }
        } else {// GetSystemDateAndTime must not be password protected as per spec.
            headers = "";
        }
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod("POST"),
                removeIPandPortFromUrl(xAddr));
        String actionString = Helper.fetchXML(getXmlCache, requestType.toString(), "xmlns=\"");
        request.headers().add("Content-Type",
                "application/soap+xml; charset=utf-8; action=\"" + actionString + "/" + requestType + "\"");
        request.headers().add("Charset", "utf-8");
        // Tapo brand have different ports for the event xAddr to the other xAddr, can't use 1 port for all ONVIF calls.
        request.headers().set("Host", ipAddress + ":" + port);
        request.headers().set("Connection", HttpHeaderValues.CLOSE);
        request.headers().set("Accept-Encoding", "gzip, deflate");
        String fullXml = "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\"" + extraEnvelope + ">"
                + headers
                + "<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
                + getXmlCache + "</s:Body></s:Envelope>";
        request.headers().add("SOAPAction", "\"" + actionString + "/" + requestType + "\"");
        ByteBuf bbuf = Unpooled.copiedBuffer(fullXml, StandardCharsets.UTF_8);
        request.headers().set("Content-Length", bbuf.readableBytes());
        request.content().clear().writeBytes(bbuf);

        Bootstrap localBootstap = bootstrap;
        if (localBootstap == null) {
            mainEventLoopGroup = new NioEventLoopGroup(2);
            localBootstap = new Bootstrap();
            localBootstap.group(mainEventLoopGroup);
            localBootstap.channel(NioSocketChannel.class);
            localBootstap.option(ChannelOption.SO_KEEPALIVE, true);
            localBootstap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
            localBootstap.option(ChannelOption.SO_SNDBUF, 1024 * 8);
            localBootstap.option(ChannelOption.SO_RCVBUF, 1024 * 1024);
            localBootstap.option(ChannelOption.TCP_NODELAY, true);
            localBootstap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast("idleStateHandler", new IdleStateHandler(0, 0, 18));
                    socketChannel.pipeline().addLast("HttpClientCodec", new HttpClientCodec());
                    socketChannel.pipeline().addLast(ONVIF_CODEC, new OnvifCodec(getHandle()));
                }
            });
            bootstrap = localBootstap;
        }
        if (!mainEventLoopGroup.isShuttingDown()) {
            // Tapo brand have different ports for the event xAddr to the other xAddr, can't use 1 port for all calls.
            localBootstap.connect(new InetSocketAddress(ipAddress, port)).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(@Nullable ChannelFuture future) {
                    if (future == null) {
                        return;
                    }
                    if (future.isDone() && future.isSuccess()) {
                        Channel ch = future.channel();
                        OnvifCodec onvifCodec = (OnvifCodec) ch.pipeline().get(ONVIF_CODEC);
                        onvifCodec.setRequestType(requestType);
                        ch.writeAndFlush(request);
                    } else { // an error occurred
                        if (future.isDone() && !future.isCancelled()) {
                            Throwable cause = future.cause();
                            String msg = cause.getMessage();
                            logger.debug("Connect failed - cause is: {}", cause.getMessage());
                            if (cause instanceof ConnectTimeoutException) {
                                usingEvents = false;// Prevent Unsubscribe from being sent
                                ipCameraHandler.cameraCommunicationError(
                                        "Camera timed out when trying to connect to the ONVIF port:" + port);
                            } else if ((cause instanceof ConnectException) && msg != null
                                    && msg.contains("Connection refused")) {
                                usingEvents = false;// Prevent Unsubscribe from being sent
                                ipCameraHandler.cameraCommunicationError(
                                        "Camera refused to connect when using ONVIF to port:" + port);
                            }
                        } else {
                            ipCameraHandler.cameraCommunicationError("Camera failed to connect due to being cancelled");
                        }
                    }
                }
            });
        } else {
            logger.debug("ONVIF message not sent as connection is shutting down");
        }
    }

    OnvifConnection getHandle() {
        return this;
    }

    void getIPandPortFromUrl(String url) {
        int beginIndex = url.indexOf(":");
        int endIndex = url.indexOf("/", beginIndex);
        if (beginIndex >= 0 && endIndex == -1) {// 192.168.1.1:8080
            ipAddress = url.substring(0, beginIndex);
            onvifPort = Integer.parseInt(url.substring(beginIndex + 1));
        } else if (beginIndex >= 0 && endIndex > beginIndex) {// 192.168.1.1:8080/foo/bar
            ipAddress = url.substring(0, beginIndex);
            onvifPort = Integer.parseInt(url.substring(beginIndex + 1, endIndex));
        } else {// 192.168.1.1
            ipAddress = url;
            deviceXAddr = "http://" + ipAddress + "/onvif/device_service";
            logger.debug("No ONVIF Port found when parsing: {}", url);
            return;
        }
        deviceXAddr = "http://" + ipAddress + ":" + onvifPort + "/onvif/device_service";
    }

    public void gotoPreset(int index) {
        if (ptzDevice) {
            if (index > 0) {// 0 is reserved for HOME as cameras seem to start at preset 1.
                if (presetTokens.isEmpty()) {
                    logger.warn("Camera did not report any ONVIF preset locations, updating preset tokens now.");
                    sendPTZRequest(RequestType.GetPresets);
                } else {
                    presetTokenIndex = index - 1;
                    sendPTZRequest(RequestType.GotoPreset);
                }
            }
        }
    }

    public void eventReceived(String eventMessage) {
        Document xmlDocument;
        try {
            xmlDocument = Helper.loadXMLFromString(eventMessage);
        } catch (Exception e) {
            logger.error("Error parsing ONVIF xml.", e);
            return;
        }
        NodeList notificationMessages = xmlDocument.getElementsByTagName("wsnt:NotificationMessage");
        for (int i = 0; i < notificationMessages.getLength(); i++) {
            Element notificationMessageElement = (Element) notificationMessages.item(i);

            Element topicElement = (Element) notificationMessageElement.getElementsByTagName("wsnt:Topic").item(0);
            String topic = topicElement.getFirstChild().getNodeValue().replace("tns1:", "");

            Element sourceElement = (Element) notificationMessageElement.getElementsByTagName("tt:Source").item(0);

            Element dataElement = (Element) notificationMessageElement.getElementsByTagName("tt:Data").item(0);

            if (dataElement == null) {
                // Events without data element are not relevant.
                continue;
            }

            Element dataItemElement = (Element) dataElement.getElementsByTagName("tt:SimpleItem").item(0);

            String dataName = dataItemElement.getAttributes().getNamedItem("Name").getNodeValue();
            String dataValue = dataItemElement.getAttributes().getNamedItem("Value").getNodeValue();

            logger.debug("ONVIF Event Topic: {}, Data name: {}, Data value: {}", topic, dataName, dataValue);
            switch (topic) {
                case "RuleEngine/CellMotionDetector/Motion":
                    if ("true".equals(dataValue)) {
                        ipCameraHandler.motionDetected(CHANNEL_CELL_MOTION_ALARM);
                    } else if ("false".equals(dataValue)) {
                        ipCameraHandler.noMotionDetected(CHANNEL_CELL_MOTION_ALARM);
                    }
                    break;
                case "RuleEngine/PackageDetector/PackageDetected":
                    if ("true".equals(dataValue)) {
                        ipCameraHandler.motionDetected(CHANNEL_ITEM_LEFT);
                    } else if ("false".equals(dataValue)) {
                        ipCameraHandler.noMotionDetected(CHANNEL_ITEM_LEFT);
                    }
                    break;
                case "VideoAnalytics/Motion":
                    if ("Trigger".equals(dataValue)) {
                        ipCameraHandler.motionDetected(CHANNEL_MOTION_ALARM);
                    } else if ("Normal".equals(dataValue)) {
                        ipCameraHandler.noMotionDetected(CHANNEL_MOTION_ALARM);
                    }
                    break;
                case "RuleEngine/tnsaxis:VMD3/vmd3_video_1":
                case "RuleEngine/MotionRegionDetector/Motion":
                case "VideoSource/MotionAlarm":
                    if ("true".equals(dataValue) || "1".equals(dataValue)) {
                        ipCameraHandler.motionDetected(CHANNEL_MOTION_ALARM);
                    } else if ("false".equals(dataValue) || "0".equals(dataValue)) {
                        ipCameraHandler.noMotionDetected(CHANNEL_MOTION_ALARM);
                    }
                    break;
                case "AudioAnalytics/Audio/DetectedSound":
                    if ("true".equals(dataValue)) {
                        ipCameraHandler.audioDetected();
                    } else if ("false".equals(dataValue)) {
                        ipCameraHandler.noAudioDetected();
                    }
                    break;
                case "RuleEngine/AreaDetection/AreaDetected":
                case "RuleEngine/FieldDetector/ObjectsInside":
                    if ("true".equals(dataValue)) {
                        ipCameraHandler.motionDetected(CHANNEL_FIELD_DETECTION_ALARM);
                    } else if ("false".equals(dataValue)) {
                        ipCameraHandler.noMotionDetected(CHANNEL_FIELD_DETECTION_ALARM);
                    }
                    break;
                case "RuleEngine/LineCrossDetector/LineCross":
                case "RuleEngine/LineDetector/Crossed":
                    if ("ObjectId".equals(dataName)) {
                        ipCameraHandler.motionDetected(CHANNEL_LINE_CROSSING_ALARM);
                    } else {
                        ipCameraHandler.noMotionDetected(CHANNEL_LINE_CROSSING_ALARM);
                    }
                    break;
                case "RuleEngine/TamperDetector/Tamper":
                    if ("true".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_TAMPER_ALARM, OnOffType.ON);
                    } else if ("false".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_TAMPER_ALARM, OnOffType.OFF);
                    }
                    break;
                case "Device/tnsaxis:HardwareFailure/StorageFailure":
                case "Device/HardwareFailure/StorageFailure":
                    if ("true".equals(dataValue) || "1".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_STORAGE_ALARM, OnOffType.ON);
                    } else if ("false".equals(dataValue) || "0".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_STORAGE_ALARM, OnOffType.OFF);
                    }
                    break;
                case "VideoSource/ImageTooDark/AnalyticsService":
                case "VideoSource/ImageTooDark/ImagingService":
                case "VideoSource/ImageTooDark/RecordingService":
                    if ("true".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_TOO_DARK_ALARM, OnOffType.ON);
                    } else if ("false".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_TOO_DARK_ALARM, OnOffType.OFF);
                    }
                    break;
                case "VideoSource/GlobalSceneChange/AnalyticsService":
                case "VideoSource/GlobalSceneChange/ImagingService":
                case "VideoSource/GlobalSceneChange/RecordingService":
                    if ("true".equals(dataValue) || "1".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_SCENE_CHANGE_ALARM, OnOffType.ON);
                    } else if ("false".equals(dataValue) || "0".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_SCENE_CHANGE_ALARM, OnOffType.OFF);
                    }
                    break;
                case "VideoSource/ImageTooBright/AnalyticsService":
                case "VideoSource/ImageTooBright/ImagingService":
                case "VideoSource/ImageTooBright/RecordingService":
                    if ("true".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_TOO_BRIGHT_ALARM, OnOffType.ON);
                    } else if ("false".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_TOO_BRIGHT_ALARM, OnOffType.OFF);
                    }
                    break;
                case "VideoSource/ImageTooBlurry/AnalyticsService":
                case "VideoSource/ImageTooBlurry/ImagingService":
                case "VideoSource/ImageTooBlurry/RecordingService":
                    if ("true".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_TOO_BLURRY_ALARM, OnOffType.ON);
                    } else if ("false".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_TOO_BLURRY_ALARM, OnOffType.OFF);
                    }
                    break;
                case "RuleEngine/MyRuleDetector/Visitor":
                    if ("true".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_DOORBELL, OnOffType.ON);
                    } else if ("false".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_DOORBELL, OnOffType.OFF);
                    }
                    break;
                case "RuleEngine/Analytics/VehicleDetection":
                case "RuleEngine/VehicleDetector/Vehicle":
                case "VideoAnalytics/VehicleDetection/Vehicle":
                case "RuleEngine/MyRuleDetector/VehicleDetect":
                    if ("true".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_CAR_ALARM, OnOffType.ON);
                    } else if ("false".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_CAR_ALARM, OnOffType.OFF);
                    }
                    break;
                case "RuleEngine/PetDetector/Pet":
                case "RuleEngine/MyRuleDetector/DogCatDetect":
                    if ("true".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_ANIMAL_ALARM, OnOffType.ON);
                    } else if ("false".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_ANIMAL_ALARM, OnOffType.OFF);
                    }
                    break;
                case "RuleEngine/FaceDetector/FaceDetected":
                case "RuleEngine/MyRuleDetector/FaceDetect":
                    if ("true".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_FACE_DETECTED, OnOffType.ON);
                    } else if ("false".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_FACE_DETECTED, OnOffType.OFF);
                    }
                    break;
                case "VideoAnalytics/PersonDetection/Person":
                case "RuleEngine/PeopleDetector/People":
                case "RuleEngine/SmartMotionHumanDetection/HumanDetection":
                case "RuleEngine/HumanDetection/HumanDetected":
                case "RuleEngine/MyRuleDetector/PeopleDetect":
                    if ("true".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_HUMAN_ALARM, OnOffType.ON);
                    } else if ("false".equals(dataValue)) {
                        ipCameraHandler.changeAlarmState(CHANNEL_HUMAN_ALARM, OnOffType.OFF);
                    }
                    break;
                default:
                    logger.debug("Please report this camera has an un-implemented ONVIF event. Topic: {}", topic);
            }
        }
    }

    public boolean supportsPTZ() {
        return ptzDevice;
    }

    public void getStatus() {
        if (ptzDevice) {
            sendPTZRequest(RequestType.GetStatus);
        }
    }

    public Float getAbsolutePan() {
        return currentPanPercentage;
    }

    public Float getAbsoluteTilt() {
        return currentTiltPercentage;
    }

    public Float getAbsoluteZoom() {
        return currentZoomPercentage;
    }

    public void setAbsolutePan(Float panValue) {// Value is 0-100% of cameras range
        if (ptzDevice) {
            currentPanPercentage = panValue;
            currentPanCamValue = ((((panRangeMin - panRangeMax) * -1) / 100) * panValue + panRangeMin);
        }
    }

    public void setAbsoluteTilt(Float tiltValue) {// Value is 0-100% of cameras range
        if (ptzDevice) {
            currentTiltPercentage = tiltValue;
            currentTiltCamValue = ((((panRangeMin - panRangeMax) * -1) / 100) * tiltValue + tiltRangeMin);
        }
    }

    public void setAbsoluteZoom(Float zoomValue) {// Value is 0-100% of cameras range
        if (ptzDevice) {
            currentZoomPercentage = zoomValue;
            currentZoomCamValue = ((((zoomMin - zoomMax) * -1) / 100) * zoomValue + zoomMin);
        }
    }

    public void absoluteMove() { // Camera wont move until PTZ values are set, then call this.
        if (ptzDevice) {
            sendPTZRequest(RequestType.AbsoluteMove);
        }
    }

    public void setSelectedMediaProfile(int mediaProfileIndex) {
        this.mediaProfileIndex = mediaProfileIndex;
    }

    List<String> listOfResults(String message, String heading, String key) {
        List<String> results = new LinkedList<>();
        String temp = "";
        for (int startLookingFromIndex = 0; startLookingFromIndex != -1;) {
            startLookingFromIndex = message.indexOf(heading, startLookingFromIndex);
            if (startLookingFromIndex >= 0) {
                temp = Helper.fetchXML(message.substring(startLookingFromIndex), heading, key);
                if (!temp.isEmpty()) {
                    logger.trace("String was found: {}", temp);
                    results.add(temp);
                } else {
                    return results;// key string must not exist so stop looking.
                }
                startLookingFromIndex += temp.length();
            }
        }
        return results;
    }

    void parsePresets(String message) {
        List<StateOption> presets = new ArrayList<>();
        int counter = 1;// Presets start at 1 not 0. HOME may be added to index 0.
        presetTokens = listOfResults(message, "<tptz:Preset", "token=\"");
        presetNames = listOfResults(message, "<tptz:Preset", "<tt:Name>");
        if (presetTokens.size() != presetNames.size()) {
            logger.warn("Camera did not report the same number of Tokens and Names for PTZ presets");
            return;
        }
        for (String value : presetNames) {
            presets.add(new StateOption(Integer.toString(counter++), value));
        }
        ipCameraHandler.stateDescriptionProvider
                .setStateOptions(new ChannelUID(ipCameraHandler.getThing().getUID(), CHANNEL_GOTO_PRESET), presets);
    }

    void parseProfiles(String message) {
        mediaProfileTokens = listOfResults(message, "<trt:Profiles", "token=\"");
        if (mediaProfileIndex >= mediaProfileTokens.size()) {
            logger.warn(
                    "You have set the media profile to {} when the camera reported {} profiles. Falling back to mainstream 0.",
                    mediaProfileIndex, mediaProfileTokens.size());
            mediaProfileIndex = 0;
        }
    }

    void processPTZLocation(String result) {
        logger.debug("Processing new PTZ location now");

        int beginIndex = result.indexOf("x=\"");
        int endIndex = result.indexOf("\"", (beginIndex + 3));
        if (beginIndex >= 0 && endIndex >= 0) {
            currentPanCamValue = Float.parseFloat(result.substring(beginIndex + 3, endIndex));
            currentPanPercentage = (((panRangeMin - currentPanCamValue) * -1) / ((panRangeMin - panRangeMax) * -1))
                    * 100;
            logger.debug("Pan is updating to: {} and the cam value is {}", Math.round(currentPanPercentage),
                    currentPanCamValue);
        } else {
            logger.warn(
                    "Binding could not determin the cameras current PTZ location. Not all cameras respond to GetStatus requests.");
            return;
        }

        beginIndex = result.indexOf("y=\"");
        endIndex = result.indexOf("\"", (beginIndex + 3));
        if (beginIndex >= 0 && endIndex >= 0) {
            currentTiltCamValue = Float.parseFloat(result.substring(beginIndex + 3, endIndex));
            currentTiltPercentage = (((tiltRangeMin - currentTiltCamValue) * -1) / ((tiltRangeMin - tiltRangeMax) * -1))
                    * 100;
            logger.debug("Tilt is updating to: {} and the cam value is {}", Math.round(currentTiltPercentage),
                    currentTiltCamValue);
        } else {
            return;
        }

        beginIndex = result.lastIndexOf("x=\"");
        endIndex = result.indexOf("\"", (beginIndex + 3));
        if (beginIndex >= 0 && endIndex >= 0) {
            currentZoomCamValue = Float.parseFloat(result.substring(beginIndex + 3, endIndex));
            currentZoomPercentage = (((zoomMin - currentZoomCamValue) * -1) / ((zoomMin - zoomMax) * -1)) * 100;
            logger.debug("Zoom is updating to: {} and the cam value is {}", Math.round(currentZoomPercentage),
                    currentZoomCamValue);
        } else {
            return;
        }
    }

    public void sendPTZRequest(RequestType requestType) {
        if (!isConnected) {
            logger.debug("ONVIF was not connected when a PTZ request was made, connecting now");
            connect(usingEvents);
        }
        sendOnvifRequest(requestType, ptzXAddr);
    }

    public void sendEventRequest(RequestType requestType) {
        sendOnvifRequest(requestType, eventXAddr);
    }

    public void connect(boolean useEvents) {
        connecting.lock();
        try {
            if (!isConnected) {
                logger.debug("Connecting {} to ONVIF", ipAddress);
                threadPool = Executors.newScheduledThreadPool(2);
                sendOnvifRequest(RequestType.GetSystemDateAndTime, deviceXAddr);
                usingEvents = useEvents;
                sendOnvifRequest(RequestType.GetCapabilities, deviceXAddr);
            }
        } finally {
            connecting.unlock();
        }
    }

    public boolean isConnected() {
        connecting.lock();
        try {
            return isConnected;
        } finally {
            connecting.unlock();
        }
    }

    public boolean getEventsSupported() {
        return onvifEventServiceType > 1;// 0 = auto detect, 1 = disabled, 2 = PullMessages, 3 = WSBaseSubscription
    }

    public void setIsConnected(boolean isConnected) {
        connecting.lock();
        try {
            this.isConnected = isConnected;
        } finally {
            connecting.unlock();
        }
    }

    private void cleanup() {
        if (!isConnected && !mainEventLoopGroup.isShuttingDown()) {
            try {
                mainEventLoopGroup.shutdownGracefully();
                mainEventLoopGroup.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("ONVIF was not cleanly shutdown, due to being interrupted");
            } finally {
                logger.debug("Eventloop is shutdown: {}", mainEventLoopGroup.isShutdown());
                bootstrap = null;
                threadPool.shutdown();
            }
        }
    }

    public void disconnect() {
        connecting.lock();// Lock out multiple disconnect()/connect() attempts as we try to send Unsubscribe.
        try {
            if (bootstrap != null) {
                if (isConnected && usingEvents && !mainEventLoopGroup.isShuttingDown()
                        && !subscriptionXAddr.isEmpty()) {
                    // Only makes sense to send if connected
                    // Some cameras may continue to send events even when they can't reach a server.
                    sendOnvifRequest(RequestType.Unsubscribe, subscriptionXAddr);
                }
                // give time for the Unsubscribe request to be sent, shutdownGracefully will try to send it first.
                threadPool.schedule(this::cleanup, 50, TimeUnit.MILLISECONDS);
            } else {
                cleanup();
            }

            isConnected = false;// isConnected is not thread safe, connecting.lock() used as fix.
        } finally {
            connecting.unlock();
        }
    }
}
