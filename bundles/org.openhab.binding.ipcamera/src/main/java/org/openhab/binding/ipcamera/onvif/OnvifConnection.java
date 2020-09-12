/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.ipcamera.onvif;

import static org.openhab.binding.ipcamera.IpCameraBindingConstants.*;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.handler.IpCameraHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * The {@link OnvifConnection} This is a basic Netty implementation for connecting and communicating to ONVIF cameras.
 *
 *
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class OnvifConnection {
    @Nullable
    private Bootstrap bootstrap;
    private EventLoopGroup mainEventLoopGroup = new NioEventLoopGroup();
    private String ipAddress = "";
    private String user = "";
    private String password = "";
    private int onvifPort = 80;
    private String deviceXAddr = "/onvif/device_service";
    private String eventXAddr = "/onvif/device_service";
    private String mediaXAddr = "/onvif/device_service";
    @SuppressWarnings("unused")
    private String imagingXAddr = "/onvif/device_service";
    private String ptzXAddr = "/onvif/ptz_service";
    private String subscriptionXAddr = "/onvif/device_service";
    private boolean isConnected = false;
    private int mediaProfileIndex = 0;
    private String snapshotUri = "";
    private String rtspUri = "";
    private IpCameraHandler ipCameraHandler;
    private boolean usingEvents = false;

    // These hold the cameras PTZ position in the range that the camera uses, ie
    // mine is -1 to +1
    private Float panRangeMin = -1.0f;
    private Float panRangeMax = 1.0f;
    private Float tiltRangeMin = -1.0f;
    private Float tiltRangeMax = 1.0f;
    private Float zoomMin = 0.0f;
    private Float zoomMax = 1.0f;
    // These hold the PTZ values for updating Openhabs controls in 0-100 range
    private Float currentPanPercentage = 0.0f;
    private Float currentTiltPercentage = 0.0f;
    private Float currentZoomPercentage = 0.0f;
    private Float currentPanCamValue = 0.0f;
    private Float currentTiltCamValue = 0.0f;
    private Float currentZoomCamValue = 0.0f;
    private String ptzNodeToken = "000";
    private String ptzConfigToken = "000";
    private int presetTokenIndex = 0;
    private LinkedList<String> presetTokens = new LinkedList<>();
    private LinkedList<String> mediaProfileTokens = new LinkedList<>();
    private boolean ptzDevice = true;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public OnvifConnection(IpCameraHandler ipCameraHandler, String ipAddress, String user, String password) {
        this.ipCameraHandler = ipCameraHandler;
        if (!ipAddress.isEmpty()) {
            this.user = user;
            this.password = password;
            getIPandPortFromUrl(ipAddress);
        }
    }

    String getXml(String requestType) {
        switch (requestType) {
            case "AbsoluteMove":
                return "<AbsoluteMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex) + "</ProfileToken><Position><PanTilt x=\""
                        + currentPanCamValue + "\" y=\"" + currentTiltCamValue
                        + "\" space=\"http://www.onvif.org/ver10/tptz/PanTiltSpaces/PositionGenericSpace\">\n"
                        + "</PanTilt>\n" + "<Zoom x=\"" + currentZoomCamValue
                        + "\" space=\"http://www.onvif.org/ver10/tptz/ZoomSpaces/PositionGenericSpace\">\n"
                        + "</Zoom>\n" + "</Position>\n"
                        + "<Speed><PanTilt x=\"0.1\" y=\"0.1\" space=\"http://www.onvif.org/ver10/tptz/PanTiltSpaces/GenericSpeedSpace\"></PanTilt><Zoom x=\"1.0\" space=\"http://www.onvif.org/ver10/tptz/ZoomSpaces/ZoomGenericSpeedSpace\"></Zoom>\n"
                        + "</Speed></AbsoluteMove>";
            case "AddPTZConfiguration": // not tested to work yet
                return "<AddPTZConfiguration xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex) + "</ProfileToken><ConfigurationToken>"
                        + ptzConfigToken + "</ConfigurationToken></AddPTZConfiguration>";
            case "ContinuousMoveLeft":
                return "<ContinuousMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex)
                        + "</ProfileToken><Velocity><PanTilt x=\"-0.5\" y=\"0\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Velocity></ContinuousMove>";
            case "ContinuousMoveRight":
                return "<ContinuousMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex)
                        + "</ProfileToken><Velocity><PanTilt x=\"0.5\" y=\"0\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Velocity></ContinuousMove>";
            case "ContinuousMoveUp":
                return "<ContinuousMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex)
                        + "</ProfileToken><Velocity><PanTilt x=\"0\" y=\"-0.5\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Velocity></ContinuousMove>";
            case "ContinuousMoveDown":
                return "<ContinuousMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex)
                        + "</ProfileToken><Velocity><PanTilt x=\"0\" y=\"0.5\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Velocity></ContinuousMove>";
            case "Stop":
                return "<Stop xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex)
                        + "</ProfileToken><PanTilt>true</PanTilt><Zoom>true</Zoom></Stop>";
            case "ContinuousMoveIn":
                return "<ContinuousMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex)
                        + "</ProfileToken><Velocity><Zoom x=\"0.5\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Velocity></ContinuousMove>";
            case "ContinuousMoveOut":
                return "<ContinuousMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex)
                        + "</ProfileToken><Velocity><Zoom x=\"-0.5\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Velocity></ContinuousMove>";
            case "CreatePullPointSubscription":
                return "<CreatePullPointSubscription xmlns=\"http://www.onvif.org/ver10/events/wsdl\"><InitialTerminationTime>PT600S</InitialTerminationTime></CreatePullPointSubscription>";
            case "GetCapabilities":
                return "<GetCapabilities xmlns=\"http://www.onvif.org/ver10/device/wsdl\"><Category>All</Category></GetCapabilities>";

            case "GetDeviceInformation":
                return "<GetDeviceInformation xmlns=\"http://www.onvif.org/ver10/device/wsdl\"/>";
            case "GetProfiles":
                return "<GetProfiles xmlns=\"http://www.onvif.org/ver10/media/wsdl\"/>";
            case "GetServiceCapabilities":
                return "<GetServiceCapabilities xmlns=\"http://docs.oasis-open.org/wsn/b-2/\"></GetServiceCapabilities>";
            case "GetSnapshotUri":
                return "<GetSnapshotUri xmlns=\"http://www.onvif.org/ver10/media/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex) + "</ProfileToken></GetSnapshotUri>";
            case "GetStreamUri":
                return "<GetStreamUri xmlns=\"http://www.onvif.org/ver10/media/wsdl\"><StreamSetup><Stream xmlns=\"http://www.onvif.org/ver10/schema\">RTP-Unicast</Stream><Transport xmlns=\"http://www.onvif.org/ver10/schema\"><Protocol>RTSP</Protocol></Transport></StreamSetup><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex) + "</ProfileToken></GetStreamUri>";
            case "GetSystemDateAndTime":
                return "<GetSystemDateAndTime xmlns=\"http://www.onvif.org/ver10/device/wsdl\"/>";
            case "Subscribe":
                return "<Subscribe xmlns=\"http://docs.oasis-open.org/wsn/b-2/\"><ConsumerReference><Address>http://"
                        + ipCameraHandler.hostIp + ":" + ipCameraHandler.serverPort
                        + "/OnvifEvent</Address></ConsumerReference></Subscribe>";
            case "Unsubscribe":
                return "<Unsubscribe xmlns=\"http://docs.oasis-open.org/wsn/b-2/\"></Unsubscribe>";
            case "PullMessages":
                return "<PullMessages xmlns=\"http://www.onvif.org/ver10/events/wsdl\"><Timeout>PT8S</Timeout><MessageLimit>1</MessageLimit></PullMessages>";
            case "GetEventProperties":
                return "<GetEventProperties xmlns=\"http://www.onvif.org/ver10/events/wsdl\"/>";
            case "RelativeMoveLeft":
                return "<RelativeMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex)
                        + "</ProfileToken><Translation><PanTilt x=\"0.05000000\" y=\"0\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Translation></RelativeMove>";
            case "RelativeMoveRight":
                return "<RelativeMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex)
                        + "</ProfileToken><Translation><PanTilt x=\"-0.05000000\" y=\"0\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Translation></RelativeMove>";
            case "RelativeMoveUp":
                return "<RelativeMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex)
                        + "</ProfileToken><Translation><PanTilt x=\"0\" y=\"0.100000000\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Translation></RelativeMove>";
            case "RelativeMoveDown":
                return "<RelativeMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex)
                        + "</ProfileToken><Translation><PanTilt x=\"0\" y=\"-0.100000000\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Translation></RelativeMove>";
            case "RelativeMoveIn":
                return "<RelativeMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex)
                        + "</ProfileToken><Translation><Zoom x=\"0.0240506344\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Translation></RelativeMove>";
            case "RelativeMoveOut":
                return "<RelativeMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex)
                        + "</ProfileToken><Translation><Zoom x=\"-0.0240506344\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Translation></RelativeMove>";
            case "Renew":
                return "<Renew xmlns=\"http://docs.oasis-open.org/wsn/b-2\"><TerminationTime>PT1M</TerminationTime></Renew>";
            case "GetConfigurations":
                return "<GetConfigurations xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"></GetConfigurations>";
            case "GetConfigurationOptions":
                return "<GetConfigurationOptions xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ConfigurationToken>"
                        + ptzConfigToken + "</ConfigurationToken></GetConfigurationOptions>";
            case "GetConfiguration":
                return "<GetConfiguration xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><PTZConfigurationToken>"
                        + ptzConfigToken + "</PTZConfigurationToken></GetConfiguration>";
            case "SetConfiguration":// not tested to work yet
                return "<SetConfiguration xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><PTZConfiguration><NodeToken>"
                        + ptzNodeToken
                        + "</NodeToken><DefaultAbsolutePantTiltPositionSpace>AbsolutePanTiltPositionSpace</DefaultAbsolutePantTiltPositionSpace><DefaultAbsoluteZoomPositionSpace>AbsoluteZoomPositionSpace</DefaultAbsoluteZoomPositionSpace></PTZConfiguration></SetConfiguration>";
            case "GetNodes":
                return "<GetNodes xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"></GetNodes>";
            case "GetStatus":
                return "<GetStatus xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex) + "</ProfileToken></GetStatus>";
            case "GotoPreset":
                return "<GotoPreset xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex) + "</ProfileToken><PresetToken>"
                        + presetTokens.get(presetTokenIndex)
                        + "</PresetToken><Speed><PanTilt x=\"0.0\" y=\"0.0\" space=\"\"></PanTilt><Zoom x=\"0.0\" space=\"\"></Zoom></Speed></GotoPreset>";
            case "GetPresets":
                return "<GetPresets xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>"
                        + mediaProfileTokens.get(mediaProfileIndex) + "</ProfileToken></GetPresets>";
        }
        return "notfound";
    }

    public void processReply(String message) {
        logger.trace("Onvif reply is:{}", message);
        if (message.contains("PullMessagesResponse")) {
            eventRecieved(message);
        } else if (message.contains("RenewResponse")) {
            sendOnvifRequest(requestBuilder("PullMessages", subscriptionXAddr));
        } else if (message.contains("GetSystemDateAndTimeResponse")) {// 1st to be sent.
            isConnected = true;
            sendOnvifRequest(requestBuilder("GetCapabilities", deviceXAddr));
            parseDateAndTime(message);
            logger.debug("Openhabs UTC dateTime is:{}", getUTCdateTime());
        } else if (message.contains("GetCapabilitiesResponse")) {// 2nd to be sent.
            parseXAddr(message);
            sendOnvifRequest(requestBuilder("GetProfiles", mediaXAddr));
        } else if (message.contains("GetProfilesResponse")) {// 3rd to be sent.
            parseProfiles(message);
            sendOnvifRequest(requestBuilder("GetSnapshotUri", mediaXAddr));
            sendOnvifRequest(requestBuilder("GetStreamUri", mediaXAddr));
            if (ptzDevice) {
                sendPTZRequest("GetNodes");
            }
            if (usingEvents) {// stops API cameras from getting sent ONVIF events.
                sendOnvifRequest(requestBuilder("GetEventProperties", eventXAddr));
                sendOnvifRequest(requestBuilder("GetServiceCapabilities", eventXAddr));
            }
        } else if (message.contains("GetServiceCapabilitiesResponse")) {
            if (message.contains("WSSubscriptionPolicySupport=\"true\"")) {
                sendOnvifRequest(requestBuilder("Subscribe", eventXAddr));
            }
            // sendOnvifRequest(requestBuilder("Subscribe", eventXAddr));
        } else if (message.contains("GetEventPropertiesResponse")) {
            sendOnvifRequest(requestBuilder("CreatePullPointSubscription", eventXAddr));
        } else if (message.contains("SubscribeResponse")) {
            logger.info("Onvif Subscribe appears to be working for Alarms/Events.");
        } else if (message.contains("CreatePullPointSubscriptionResponse")) {
            subscriptionXAddr = removeIPfromUrl(fetchXML(message, "SubscriptionReference>", "Address>"));
            logger.debug("subscriptionXAddr={}", subscriptionXAddr);
            sendOnvifRequest(requestBuilder("PullMessages", subscriptionXAddr));
        } else if (message.contains("GetStatusResponse")) {
            processPTZLocation(message);
        } else if (message.contains("GetPresetsResponse")) {
            presetTokens = listOfResults(message, "<tptz:Preset", "token=\"");
        } else if (message.contains("GetConfigurationsResponse")) {
            sendPTZRequest("GetPresets");
            ptzConfigToken = fetchXML(message, "PTZConfiguration", "token=\"");
            logger.debug("ptzConfigToken={}", ptzConfigToken);
            sendPTZRequest("GetConfigurationOptions");
        } else if (message.contains("GetNodesResponse")) {
            sendPTZRequest("GetStatus");
            ptzNodeToken = fetchXML(message, "", "token=\"");
            logger.debug("ptzNodeToken={}", ptzNodeToken);
            sendPTZRequest("GetConfigurations");
        } else if (message.contains("GetDeviceInformationResponse")) {
            logger.debug("GetDeviceInformationResponse recieved");
        } else if (message.contains("GetSnapshotUriResponse")) {
            snapshotUri = removeIPfromUrl(fetchXML(message, ":MediaUri", ":Uri"));
            logger.debug("GetSnapshotUri:{}", snapshotUri);
            if (ipCameraHandler.snapshotUri.isEmpty()) {
                ipCameraHandler.snapshotUri = snapshotUri;
            }
        } else if (message.contains("GetStreamUriResponse")) {
            rtspUri = fetchXML(message, ":MediaUri", ":Uri>");
            logger.debug("GetStreamUri:{}", rtspUri);
            if (ipCameraHandler.rtspUri.isEmpty()) {
                ipCameraHandler.rtspUri = rtspUri;
            }
        } else {
            logger.trace("Unhandled Onvif reply is:{}", message);
        }
    }

    HttpRequest requestBuilder(String requestType, String xAddr) {
        logger.trace("Sending ONVIF request:{}", requestType);
        String security = "";
        String extraEnvelope = " xmlns:a=\"http://www.w3.org/2005/08/addressing\"";
        String headerTo = "";
        if (requestType.equals("CreatePullPointSubscription") || requestType.equals("PullMessages")
                || requestType.equals("Renew") || requestType.equals("Unsubscribe")) {
            headerTo = "<a:To s:mustUnderstand=\"1\">http://" + ipAddress + xAddr + "</a:To>";
        }
        if (!password.isEmpty()) {
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
        }
        String headers = "<s:Header>" + security + headerTo + "</s:Header>";

        if (requestType.equals("GetSystemDateAndTime")) {
            extraEnvelope = "";
            headers = "";
        }

        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod("POST"), xAddr);
        request.headers().add("Content-Type", "application/soap+xml");
        request.headers().add("charset", "utf-8");
        if (onvifPort != 80) {
            request.headers().set("Host", ipAddress + ":" + onvifPort);
        } else {
            request.headers().set("Host", ipAddress);
        }
        request.headers().set("Connection", HttpHeaderValues.CLOSE);
        request.headers().set("Accept-Encoding", "gzip, deflate");
        String fullXml = "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\"" + extraEnvelope + ">"
                + headers
                + "<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
                + getXml(requestType) + "</s:Body></s:Envelope>";
        String actionString = fetchXML(getXml(requestType), requestType, "xmlns=\"");
        request.headers().add("SOAPAction", "\"" + actionString + "/" + requestType + "\"");
        ByteBuf bbuf = Unpooled.copiedBuffer(fullXml, StandardCharsets.UTF_8);
        request.headers().set("Content-Length", bbuf.readableBytes());
        request.content().clear().writeBytes(bbuf);
        return request;
    }

    /**
     * The {@link removeIPfromUrl} Will throw away all text before the cameras IP, also removes the IP and the PORT
     * leaving just the
     * URL.
     *
     * @author Matthew Skinner - Initial contribution
     */
    String removeIPfromUrl(String url) {
        int index = url.indexOf(ipAddress);
        if (index != -1) {// now remove the :port
            index = url.indexOf("/", index + ipAddress.length());
        }
        if (index == -1) {
            logger.debug("We hit an issue parsing url:{}", url);
            return "";
        }
        return url.substring(index);
    }

    void parseXAddr(String message) {
        // Normally I would search '<tt:XAddr>' instead but Foscam needed this work around.
        String temp = removeIPfromUrl(fetchXML(message, "<tt:Device", "tt:XAddr"));
        if (!temp.isEmpty()) {
            deviceXAddr = temp;
            logger.debug("deviceXAddr:{}", deviceXAddr);
        }
        temp = removeIPfromUrl(fetchXML(message, "<tt:Events", "tt:XAddr"));
        if (!temp.isEmpty()) {
            subscriptionXAddr = eventXAddr = temp;
            logger.debug("eventsXAddr:{}", eventXAddr);
        }
        temp = removeIPfromUrl(fetchXML(message, "<tt:Media", "tt:XAddr"));
        if (!temp.isEmpty()) {
            mediaXAddr = temp;
            logger.debug("mediaXAddr:{}", mediaXAddr);
        }

        ptzXAddr = removeIPfromUrl(fetchXML(message, "<tt:PTZ", "tt:XAddr"));
        if (ptzXAddr.isEmpty()) {
            ptzDevice = false;
            logger.trace("Camera must not support PTZ, it failed to give a <tt:PTZ><tt:XAddr>:{}", message);
        } else {
            logger.debug("ptzXAddr:{}", ptzXAddr);
        }
    }

    private void parseDateAndTime(String message) {
        String minute = fetchXML(message, "UTCDateTime", "Minute>");
        String hour = fetchXML(message, "UTCDateTime", "Hour>");
        String second = fetchXML(message, "UTCDateTime", "Second>");
        logger.debug("Cameras  UTC time is : {}:{}:{}", hour, minute, second);
        String day = fetchXML(message, "UTCDateTime", "Day>");
        String month = fetchXML(message, "UTCDateTime", "Month>");
        String year = fetchXML(message, "UTCDateTime", "Year>");
        logger.debug("Cameras  UTC date is : {}-{}-{}", year, month, day);
    }

    private String getUTCdateTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }

    String createNonce() {
        Random nonce = new Random();
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
            msgDigest.update(beforeEncryption.getBytes("utf8"));
            encryptedRaw = msgDigest.digest();
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }
        return Base64.getEncoder().encodeToString(encryptedRaw);
    }

    @SuppressWarnings("null")
    public void sendOnvifRequest(HttpRequest request) {
        if (bootstrap == null) {
            bootstrap = new Bootstrap();
            bootstrap.group(mainEventLoopGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
            bootstrap.option(ChannelOption.SO_SNDBUF, 1024 * 8);
            bootstrap.option(ChannelOption.SO_RCVBUF, 1024 * 1024);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast("idleStateHandler", new IdleStateHandler(0, 0, 70));
                    socketChannel.pipeline().addLast("HttpClientCodec", new HttpClientCodec());
                    socketChannel.pipeline().addLast("OnvifCodec", new OnvifCodec(getHandle()));
                }
            });
        }
        bootstrap.connect(new InetSocketAddress(ipAddress, onvifPort)).addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(@Nullable ChannelFuture future) {
                if (future == null) {
                    return;
                }
                if (future.isDone() && future.isSuccess()) {
                    Channel ch = future.channel();
                    ch.writeAndFlush(request);
                } else { // an error occured
                    logger.debug("Camera is not reachable on ONVIF port:{} or the port may be wrong.", onvifPort);
                    if (isConnected) {
                        disconnect();
                    }
                }
            }
        });
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
            logger.debug("No Onvif Port found when parsing:{}", url);
        }
    }

    public void gotoPreset(int index) {
        if (ptzDevice) {
            if (index > 0) {// 0 is reserved for HOME as cameras seem to start at preset 1.
                if (presetTokens.isEmpty()) {
                    logger.warn("Camera did not report any ONVIF preset locations, updating preset tokens now.");
                    sendPTZRequest("GetPresets");
                } else {
                    presetTokenIndex = index - 1;
                    sendPTZRequest("GotoPreset");
                }
            }
        }
    }

    public void eventRecieved(String eventMessage) {
        String topic = fetchXML(eventMessage, "Topic", "tns1:");
        String dataName = fetchXML(eventMessage, "tt:Data", "Name=\"");
        String dataValue = fetchXML(eventMessage, "tt:Data", "Value=\"");
        if (!topic.isEmpty()) {
            logger.debug("Onvif Event Topic:{}, Data:{}, Value:{}", topic, dataName, dataValue);
        }
        switch (topic) {
            case "RuleEngine/CellMotionDetector/Motion":
                if (dataValue.equals("true")) {
                    ipCameraHandler.motionDetected(CHANNEL_CELL_MOTION_ALARM);
                } else if (dataValue.equals("false")) {
                    ipCameraHandler.noMotionDetected(CHANNEL_CELL_MOTION_ALARM);
                }
                break;
            case "VideoSource/MotionAlarm":
                if (dataValue.equals("true")) {
                    ipCameraHandler.motionDetected(CHANNEL_MOTION_ALARM);
                } else if (dataValue.equals("false")) {
                    ipCameraHandler.noMotionDetected(CHANNEL_MOTION_ALARM);
                }
                break;
            case "AudioAnalytics/Audio/DetectedSound":
                if (dataValue.equals("true")) {
                    ipCameraHandler.audioDetected();
                } else if (dataValue.equals("false")) {
                    ipCameraHandler.noAudioDetected();
                }
                break;
            case "RuleEngine/FieldDetector/ObjectsInside":
                if (dataValue.equals("true")) {
                    ipCameraHandler.motionDetected(CHANNEL_FIELD_DETECTION_ALARM);
                } else if (dataValue.equals("false")) {
                    ipCameraHandler.noMotionDetected(CHANNEL_FIELD_DETECTION_ALARM);
                }
                break;
            case "RuleEngine/LineDetector/Crossed":
                if (dataName.equals("ObjectId")) {
                    ipCameraHandler.motionDetected(CHANNEL_LINE_CROSSING_ALARM);
                } else {
                    ipCameraHandler.noMotionDetected(CHANNEL_LINE_CROSSING_ALARM);
                }
                break;
            case "RuleEngine/TamperDetector/Tamper":
                if (dataValue.equals("true")) {
                    ipCameraHandler.changeAlarmState(CHANNEL_TAMPER_ALARM, "ON");
                } else if (dataValue.equals("false")) {
                    ipCameraHandler.changeAlarmState(CHANNEL_TAMPER_ALARM, "OFF");
                }
                break;
            case "Device/HardwareFailure/StorageFailure":
                if (dataValue.equals("true")) {
                    ipCameraHandler.changeAlarmState(CHANNEL_STORAGE_ALARM, "ON");
                } else if (dataValue.equals("false")) {
                    ipCameraHandler.changeAlarmState(CHANNEL_STORAGE_ALARM, "OFF");
                }
                break;
            case "VideoSource/ImageTooDark/AnalyticsService":
            case "VideoSource/ImageTooDark/ImagingService":
            case "VideoSource/ImageTooDark/RecordingService":
                if (dataValue.equals("true")) {
                    ipCameraHandler.changeAlarmState(CHANNEL_TOO_DARK_ALARM, "ON");
                } else if (dataValue.equals("false")) {
                    ipCameraHandler.changeAlarmState(CHANNEL_TOO_DARK_ALARM, "OFF");
                }
                break;
            case "VideoSource/GlobalSceneChange/AnalyticsService":
            case "VideoSource/GlobalSceneChange/ImagingService":
            case "VideoSource/GlobalSceneChange/RecordingService":
                if (dataValue.equals("true")) {
                    ipCameraHandler.changeAlarmState(CHANNEL_SCENE_CHANGE_ALARM, "ON");
                } else if (dataValue.equals("false")) {
                    ipCameraHandler.changeAlarmState(CHANNEL_SCENE_CHANGE_ALARM, "OFF");
                }
                break;
            case "VideoSource/ImageTooBright/AnalyticsService":
            case "VideoSource/ImageTooBright/ImagingService":
            case "VideoSource/ImageTooBright/RecordingService":
                if (dataValue.equals("true")) {
                    ipCameraHandler.changeAlarmState(CHANNEL_TOO_BRIGHT_ALARM, "ON");
                } else if (dataValue.equals("false")) {
                    ipCameraHandler.changeAlarmState(CHANNEL_TOO_BRIGHT_ALARM, "OFF");
                }
                break;
            case "VideoSource/ImageTooBlurry/AnalyticsService":
            case "VideoSource/ImageTooBlurry/ImagingService":
            case "VideoSource/ImageTooBlurry/RecordingService":
                if (dataValue.equals("true")) {
                    ipCameraHandler.changeAlarmState(CHANNEL_TOO_BLURRY_ALARM, "ON");
                } else if (dataValue.equals("false")) {
                    ipCameraHandler.changeAlarmState(CHANNEL_TOO_BLURRY_ALARM, "OFF");
                }
                break;
            default:
        }
        sendOnvifRequest(requestBuilder("Renew", subscriptionXAddr));
    }

    public boolean supportsPTZ() {
        return ptzDevice;
    }

    public void getStatus() {
        if (ptzDevice) {
            sendPTZRequest("GetStatus");
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
            sendPTZRequest("AbsoluteMove");
        }
    }

    public void setSelectedMediaProfile(int mediaProfileIndex) {
        this.mediaProfileIndex = mediaProfileIndex;
    }

    LinkedList<String> listOfResults(String message, String heading, String key) {
        LinkedList<String> results = new LinkedList<String>();
        String temp = "";
        for (int startLookingFromIndex = 0; startLookingFromIndex != -1;) {
            startLookingFromIndex = message.indexOf(heading, startLookingFromIndex);
            if (startLookingFromIndex >= 0) {
                temp = fetchXML(message.substring(startLookingFromIndex), heading, key);
                if (!temp.isEmpty()) {
                    logger.trace("String was found:{}", temp);
                    results.add(temp);
                    ++startLookingFromIndex;
                }
            }
        }
        return results;
    }

    public static String fetchXML(String message, String sectionHeading, String key) {
        String result = "";
        int sectionHeaderBeginning = 0;
        if (!sectionHeading.isEmpty()) {// looking for a sectionHeading
            sectionHeaderBeginning = message.indexOf(sectionHeading);
        }
        if (sectionHeaderBeginning == -1) {
            return "";
        }
        int startIndex = message.indexOf(key, sectionHeaderBeginning + sectionHeading.length());
        if (startIndex == -1) {
            return "";
        }
        int endIndex = message.indexOf("<", startIndex + key.length());
        if (endIndex > startIndex) {
            result = message.substring(startIndex + key.length(), endIndex);
        }
        // remove any quotes and anything after the quote.
        sectionHeaderBeginning = result.indexOf("\"");
        if (sectionHeaderBeginning > 0) {
            result = result.substring(0, sectionHeaderBeginning);
        }
        // remove any ">" and anything after it.
        sectionHeaderBeginning = result.indexOf(">");
        if (sectionHeaderBeginning > 0) {
            result = result.substring(0, sectionHeaderBeginning);
        }
        return result;
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
            logger.debug("Pan is updating to:{} and the cam value is {}", Math.round(currentPanPercentage),
                    currentPanCamValue);
        } else {
            logger.warn("turning off PTZ functions as binding could not determin current PTZ locations.");
            ptzDevice = false;
            return;
        }

        beginIndex = result.indexOf("y=\"");
        endIndex = result.indexOf("\"", (beginIndex + 3));
        if (beginIndex >= 0 && endIndex >= 0) {
            currentTiltCamValue = Float.parseFloat(result.substring(beginIndex + 3, endIndex));
            currentTiltPercentage = (((tiltRangeMin - currentTiltCamValue) * -1) / ((tiltRangeMin - tiltRangeMax) * -1))
                    * 100;
            logger.debug("Tilt is updating to:{} and the cam value is {}", Math.round(currentTiltPercentage),
                    currentTiltCamValue);
        } else {
            logger.warn("turning off PTZ functions as binding could not determin current PTZ locations.");
            ptzDevice = false;
            return;
        }

        beginIndex = result.lastIndexOf("x=\"");
        endIndex = result.indexOf("\"", (beginIndex + 3));
        if (beginIndex >= 0 && endIndex >= 0) {
            currentZoomCamValue = Float.parseFloat(result.substring(beginIndex + 3, endIndex));
            currentZoomPercentage = (((zoomMin - currentZoomCamValue) * -1) / ((zoomMin - zoomMax) * -1)) * 100;
            logger.debug("Zoom is updating to:{} and the cam value is {}", Math.round(currentZoomPercentage),
                    currentZoomCamValue);
        } else {
            logger.warn("turning off PTZ functions as binding could not determin current PTZ locations.");
            ptzDevice = false;
            return;
        }
        ptzDevice = true;
    }

    public void sendPTZRequest(String string) {
        sendOnvifRequest(requestBuilder(string, ptzXAddr));
    }

    public void sendEventRequest(String string) {
        sendOnvifRequest(requestBuilder(string, eventXAddr));
    }

    public void connect(boolean useEvents) {
        if (!isConnected) {
            sendOnvifRequest(requestBuilder("GetSystemDateAndTime", deviceXAddr));
            usingEvents = useEvents;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void disconnect() {
        if (usingEvents && isConnected) {
            sendOnvifRequest(requestBuilder("Unsubscribe", subscriptionXAddr));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        isConnected = false;
        presetTokens.clear();
        mediaProfileTokens.clear();
        if (!mainEventLoopGroup.isShutdown()) {
            try {
                mainEventLoopGroup.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.info("Onvif was not shutdown correctly due to being interrupted");
            } finally {
                mainEventLoopGroup = new NioEventLoopGroup();
                bootstrap = null;
            }
        }
    }
}
