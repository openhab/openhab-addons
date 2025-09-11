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
package org.openhab.binding.ipcamera.internal.handler;

import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.AmcrestHandler;
import org.openhab.binding.ipcamera.internal.CameraConfig;
import org.openhab.binding.ipcamera.internal.ChannelTracking;
import org.openhab.binding.ipcamera.internal.DahuaHandler;
import org.openhab.binding.ipcamera.internal.DoorBirdHandler;
import org.openhab.binding.ipcamera.internal.Ffmpeg;
import org.openhab.binding.ipcamera.internal.FoscamHandler;
import org.openhab.binding.ipcamera.internal.GroupTracker;
import org.openhab.binding.ipcamera.internal.Helper;
import org.openhab.binding.ipcamera.internal.HikvisionHandler;
import org.openhab.binding.ipcamera.internal.HttpOnlyHandler;
import org.openhab.binding.ipcamera.internal.InstarHandler;
import org.openhab.binding.ipcamera.internal.IpCameraActions;
import org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.FFmpegFormat;
import org.openhab.binding.ipcamera.internal.IpCameraDynamicStateDescriptionProvider;
import org.openhab.binding.ipcamera.internal.MyNettyAuthHandler;
import org.openhab.binding.ipcamera.internal.ReolinkHandler;
import org.openhab.binding.ipcamera.internal.onvif.OnvifConnection;
import org.openhab.binding.ipcamera.internal.servlet.CameraServlet;
import org.openhab.core.OpenHAB;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * The {@link IpCameraHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class IpCameraHandler extends BaseThingHandler {
    public final Logger logger = LoggerFactory.getLogger(getClass());
    public final IpCameraDynamicStateDescriptionProvider stateDescriptionProvider;
    private ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(2);
    private GroupTracker groupTracker;
    public CameraConfig cameraConfig = new CameraConfig();

    // ChannelGroup is thread safe
    public final ChannelGroup openChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final HttpService httpService;
    private @Nullable CameraServlet servlet;
    public String mjpegContentType = "";
    public @Nullable Ffmpeg ffmpegHLS = null;
    public @Nullable Ffmpeg ffmpegRecord = null;
    public @Nullable Ffmpeg ffmpegGIF = null;
    public @Nullable Ffmpeg ffmpegRtspHelper = null;
    public @Nullable Ffmpeg ffmpegMjpeg = null;
    public @Nullable Ffmpeg ffmpegSnapshot = null;
    public boolean streamingAutoFps = false;
    public boolean motionDetected = false;
    public Instant lastSnapshotRequest = Instant.now();
    public Instant currentSnapshotTime = Instant.now();
    private @Nullable ScheduledFuture<?> cameraConnectionJob = null;
    private @Nullable ScheduledFuture<?> pollCameraJob = null;
    private @Nullable ScheduledFuture<?> snapshotJob = null;
    private @Nullable ScheduledFuture<?> authenticationJob = null;
    private @Nullable Bootstrap mainBootstrap;
    private EventLoopGroup mainEventLoopGroup = new NioEventLoopGroup(1);
    private FullHttpRequest putRequestWithBody = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.PUT, "");
    private FullHttpRequest postRequestWithBody = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "");
    private String gifFilename = "ipcamera";
    private String gifHistory = "";
    private String mp4History = "";
    public int gifHistoryLength;
    public int mp4HistoryLength;
    private String mp4Filename = "ipcamera";
    private int mp4RecordTime;
    private int gifRecordTime = 5;
    private LinkedList<byte[]> fifoSnapshotBuffer = new LinkedList<>();
    private int snapCount;
    private boolean updateImageChannel = false;
    private byte lowPriorityCounter = 0;
    public String hostIp;
    public Map<String, ChannelTracking> channelTrackingMap = new ConcurrentHashMap<>();
    public List<String> lowPriorityRequests = new ArrayList<>(0);

    // basicAuth MUST remain private as it holds the cameraConfig.getPassword()
    private String basicAuth = "";
    public String reolinkAuth = "&token=null";
    public int reolinkScheduleVersion = 0;
    public boolean useBasicAuth = false;
    public boolean useDigestAuth = false;
    public boolean newInstarApi = false;
    public String snapshotUri = "";
    public String mjpegUri = "";
    private byte[] currentSnapshot = new byte[] { (byte) 0x00 };
    public ReentrantLock lockCurrentSnapshot = new ReentrantLock();
    public String rtspUri = "";
    public boolean audioAlarmUpdateSnapshot = false;
    private boolean motionAlarmUpdateSnapshot = false;
    private AtomicBoolean isOnline = new AtomicBoolean(); // Used so only 1 error is logged when a network issue occurs.
    private boolean firstAudioAlarm = false;
    private boolean firstMotionAlarm = false;
    public BigDecimal motionThreshold = BigDecimal.ZERO;
    public int audioThreshold = 35;
    public boolean streamingSnapshotMjpeg = false;
    public boolean ffmpegMotionAlarmEnabled = false;
    public boolean ffmpegAudioAlarmEnabled = false;
    public boolean ffmpegSnapshotGeneration = false;
    public boolean snapshotPolling = false;
    public OnvifConnection onvifCamera = new OnvifConnection(this, "", "", "");

    // These methods handle the response from all camera brands, nothing specific to 1 brand.
    private class CommonCameraHandler extends ChannelDuplexHandler {
        private int bytesToReceive = 0;
        private int bytesAlreadyReceived = 0;
        private byte[] incomingJpeg = new byte[0];
        private String incomingMessage = "";
        private String contentType = "empty";
        private String boundary = "";
        private Object reply = new Object();
        private String requestUrl = "";
        private boolean isChunked = false;

        public void setURL(String url) {
            requestUrl = url;
        }

        @Override
        public void channelRead(@Nullable ChannelHandlerContext ctx, @Nullable Object msg) throws Exception {
            if (msg == null || ctx == null) {
                return;
            }
            try {
                if (msg instanceof HttpResponse response) {
                    if (response.status().code() == 200) {
                        if (!response.headers().isEmpty()) {
                            for (String name : response.headers().names()) {
                                // Some cameras use first letter uppercase and others dont.
                                switch (name.toLowerCase()) { // Possible localization issues doing this
                                    case "content-type":
                                        contentType = response.headers().getAsString(name);
                                        break;
                                    case "content-length":
                                        bytesToReceive = Integer.parseInt(response.headers().getAsString(name));
                                        break;
                                    case "transfer-encoding":
                                        if (response.headers().getAsString(name).contains("chunked")) {
                                            isChunked = true;
                                        }
                                        break;
                                }
                            }
                            if (contentType.contains("multipart")) {
                                boundary = Helper.searchString(contentType, "boundary=");
                                if (mjpegUri.endsWith(requestUrl)) {
                                    if (msg instanceof HttpMessage) {
                                        // very start of stream only
                                        mjpegContentType = contentType;
                                        CameraServlet localServlet = servlet;
                                        if (localServlet != null) {
                                            logger.debug("Setting Content-Type to: {}", contentType);
                                            localServlet.openStreams.updateContentType(contentType, boundary);
                                        }
                                    }
                                }
                            } else if (contentType.contains("image/jp")) {
                                if (bytesToReceive == 0) {
                                    bytesToReceive = 768000; // 0.768 Mbyte when no Content-Length is sent
                                    logger.debug("Camera has no Content-Length header, we have to guess how much RAM.");
                                }
                                incomingJpeg = new byte[bytesToReceive];
                            }
                        }
                    } else {
                        // Non 200 OK replies are logged and handled in pipeline by MyNettyAuthHandler.java
                        return;
                    }
                }
                if (msg instanceof HttpContent content) {
                    if (mjpegUri.endsWith(requestUrl) && !(content instanceof LastHttpContent)) {
                        // multiple MJPEG stream packets come back as this.
                        byte[] chunkedFrame = new byte[content.content().readableBytes()];
                        content.content().getBytes(content.content().readerIndex(), chunkedFrame);
                        CameraServlet localServlet = servlet;
                        if (localServlet != null) {
                            localServlet.openStreams.queueFrame(chunkedFrame);
                        }
                    } else {
                        // Found some cameras use Content-Type: image/jpg instead of image/jpeg
                        if (contentType.contains("image/jp")) {
                            for (int i = 0; i < content.content().capacity(); i++) {
                                incomingJpeg[bytesAlreadyReceived++] = content.content().getByte(i);
                            }
                            if (content instanceof LastHttpContent) {
                                processSnapshot(incomingJpeg);
                                ctx.close();
                            }
                        } else { // incomingMessage that is not an IMAGE
                            if (incomingMessage.isEmpty()) {
                                incomingMessage = content.content().toString(CharsetUtil.UTF_8);
                            } else {
                                incomingMessage += content.content().toString(CharsetUtil.UTF_8);
                            }
                            bytesAlreadyReceived = incomingMessage.length();
                            if (content instanceof LastHttpContent) {
                                // If it is not an image send it on to the next handler//
                                if (bytesAlreadyReceived != 0) {
                                    reply = incomingMessage;
                                    super.channelRead(ctx, reply);
                                }
                            }
                            // Alarm Streams never have a LastHttpContent as they always stay open//
                            else if (contentType.contains("multipart")) {
                                int beginIndex, endIndex;
                                if (bytesToReceive == 0) {
                                    beginIndex = incomingMessage.indexOf("Content-Length:");
                                    if (beginIndex != -1) {
                                        endIndex = incomingMessage.indexOf("\r\n", beginIndex);
                                        if (endIndex != -1) {
                                            bytesToReceive = Integer.parseInt(
                                                    incomingMessage.substring(beginIndex + 15, endIndex).strip());
                                        }
                                    }
                                }
                                // --boundary and headers are not included in the Content-Length value
                                if (bytesAlreadyReceived > bytesToReceive) {
                                    // Check if message has a second --boundary
                                    endIndex = incomingMessage.indexOf("--" + boundary, bytesToReceive);
                                    if (endIndex == -1) {
                                        reply = incomingMessage;
                                        incomingMessage = "";
                                        bytesToReceive = 0;
                                        bytesAlreadyReceived = 0;
                                    } else {
                                        reply = incomingMessage.substring(0, endIndex);
                                        incomingMessage = incomingMessage.substring(endIndex, incomingMessage.length());
                                        bytesToReceive = 0;// Triggers search next time for Content-Length:
                                        bytesAlreadyReceived = incomingMessage.length() - endIndex;
                                    }
                                    super.channelRead(ctx, reply);
                                }
                            }
                            // Foscam needs this as will other cameras with chunks//
                            if (isChunked && bytesAlreadyReceived != 0) {
                                reply = incomingMessage;
                            }
                        }
                    }
                } else { // msg is not HttpContent
                    // Foscam cameras need this
                    if (!contentType.contains("image/jp") && bytesAlreadyReceived != 0) {
                        reply = incomingMessage;
                        logger.trace("Packet back from camera is {}", incomingMessage);
                        super.channelRead(ctx, reply);
                    }
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void exceptionCaught(@Nullable ChannelHandlerContext ctx, @Nullable Throwable cause) {
            if (cause == null || ctx == null) {
                return;
            }
            if (cause instanceof ArrayIndexOutOfBoundsException) {
                logger.debug("Camera sent {} bytes when the content-length header was {}.", bytesAlreadyReceived,
                        bytesToReceive);
            } else {
                logger.warn("Camera possibly closed the channel on the binding for URL: {}, cause reported is: {}",
                        requestUrl, cause.getMessage());
            }
            ctx.close();
        }

        @Override
        public void userEventTriggered(@Nullable ChannelHandlerContext ctx, @Nullable Object evt) throws Exception {
            if (ctx == null) {
                return;
            }
            if (evt instanceof IdleStateEvent e) {
                // If camera does not use the channel for X amount of time it will close.
                if (e.state() == IdleState.READER_IDLE) {
                    String urlToKeepOpen = "";
                    switch (thing.getThingTypeUID().getId()) {
                        case DAHUA_THING:
                            urlToKeepOpen = "/cgi-bin/eventManager.cgi?action=attach&codes=[All]";
                            break;
                        case DOORBIRD_THING:
                            urlToKeepOpen = "/bha-api/monitor.cgi?ring=doorbell,motionsensor";
                            break;
                    }
                    ChannelTracking channelTracking = channelTrackingMap.get(urlToKeepOpen);
                    if (channelTracking != null) {
                        if (channelTracking.getChannel().equals(ctx.channel())) {
                            return; // don't auto close this as it is for the alarms.
                        }
                    }
                    logger.debug("Closing an idle channel for {}{}", cameraConfig.getIp(), requestUrl);
                    ctx.close();
                }
            }
        }
    }

    public IpCameraHandler(Thing thing, @Nullable String ipAddress, GroupTracker groupTracker,
            IpCameraDynamicStateDescriptionProvider stateDescriptionProvider, HttpService httpService) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        if (ipAddress != null) {
            hostIp = ipAddress;
        } else {
            hostIp = Helper.getLocalIpAddress();
        }
        this.groupTracker = groupTracker;
        this.httpService = httpService;
    }

    private IpCameraHandler getHandle() {
        return this;
    }

    // false clears the stored user/pass hash, true creates the hash
    public boolean setBasicAuth(boolean useBasic) {
        if (!useBasic) {
            logger.debug("Clearing out the stored BASIC auth now.");
            basicAuth = "";
            return false;
        } else if (!basicAuth.isEmpty()) {
            // If the binding sends multiple requests before basicAuth was set, this may trigger falsely.
            logger.warn("Camera is reporting your username and/or password is wrong.");
            return false;
        }
        if (!cameraConfig.getUser().isEmpty() && !cameraConfig.getPassword().isEmpty()) {
            String authString = cameraConfig.getUser() + ":" + cameraConfig.getPassword();
            ByteBuf byteBuf = null;
            try {
                byteBuf = Base64.encode(Unpooled.wrappedBuffer(authString.getBytes(CharsetUtil.UTF_8)));
                basicAuth = byteBuf.getCharSequence(0, byteBuf.capacity(), CharsetUtil.UTF_8).toString();
            } finally {
                if (byteBuf != null) {
                    byteBuf.release();
                }
            }
            return true;
        } else {
            cameraConfigError("Camera is asking for Basic Auth when you have not provided a username and/or password.");
        }
        return false;
    }

    public String getCorrectUrlFormat(String longUrl) {
        String temp = longUrl;
        URL url;

        if (longUrl.isEmpty() || "ffmpeg".equals(longUrl)) {
            return longUrl;
        }

        try {
            url = new URL(longUrl);
            int port = url.getPort();
            if (port == -1) {
                if (url.getQuery() == null) {
                    temp = url.getPath();
                } else {
                    temp = url.getPath() + "?" + url.getQuery();
                }
            } else {
                if (url.getQuery() == null) {
                    temp = ":" + url.getPort() + url.getPath();
                } else {
                    temp = ":" + url.getPort() + url.getPath() + "?" + url.getQuery();
                }
            }
        } catch (MalformedURLException e) {
            cameraConfigError("A non valid URL has been given to the binding, check they work in a browser.");
        }
        return temp;
    }

    public void sendHttpPUT(String httpRequestURL, FullHttpRequest request) {
        putRequestWithBody = request; // use Global so the authhandler can use it when resent with DIGEST.
        sendHttpRequest("PUT", httpRequestURL, null);
    }

    public void sendHttpPOST(String httpPostURL, String content) {
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, httpPostURL);
        request.headers().set("Host", cameraConfig.getIp());
        request.headers().add("Content-Type", "application/json");
        request.headers().add("User-Agent",
                "openHAB/" + FrameworkUtil.getBundle(this.getClass()).getVersion().toString());
        request.headers().add("Accept", "*/*");
        ByteBuf bbuf = Unpooled.copiedBuffer(content, StandardCharsets.UTF_8);
        request.headers().set("Content-Length", bbuf.readableBytes());
        request.content().clear().writeBytes(bbuf);
        postRequestWithBody = request; // use Global so the authhandler can use it when resent with DIGEST.
        sendHttpRequest("POST", httpPostURL, null);
    }

    public void sendHttpPOST(String httpRequestURL) {
        sendHttpRequest("POST", httpRequestURL, null);
    }

    public void sendHttpGET(String httpRequestURL) {
        sendHttpRequest("GET", httpRequestURL, null);
    }

    public int getPortFromShortenedUrl(String httpRequestURL) {
        if (httpRequestURL.startsWith(":")) {
            int end = httpRequestURL.indexOf("/");
            return Integer.parseInt(httpRequestURL.substring(1, end));
        }
        return cameraConfig.getPort();
    }

    public String getTinyUrl(String httpRequestURL) {
        if (httpRequestURL.startsWith(":")) {
            int beginIndex = httpRequestURL.indexOf("/");
            return httpRequestURL.substring(beginIndex);
        }
        return httpRequestURL;
    }

    private void checkCameraConnection() {
        if (snapshotPolling) { // Currently polling a real URL for snapshots, so camera must be online.
            return;
        } else if (ffmpegSnapshotGeneration) {
            Ffmpeg localSnapshot = ffmpegSnapshot;
            if (localSnapshot != null && !localSnapshot.isAlive()) {
                cameraCommunicationError("FFmpeg Snapshots Stopped: Check that your camera can be reached.");
            }
            return; // RTSP stream is creating snapshots, so camera is online.
        }

        if (supportsOnvifEvents() && onvifCamera.isConnected() && onvifCamera.getEventsSupported()) {
            return;// ONVIF cameras that are getting event messages must be online
        }

        // Open a HTTP connection without sending any requests as we do not need a snapshot.
        Bootstrap localBootstrap = mainBootstrap;
        if (localBootstrap != null) {
            ChannelFuture chFuture = localBootstrap
                    .connect(new InetSocketAddress(cameraConfig.getIp(), cameraConfig.getPort()));
            if (chFuture.awaitUninterruptibly(500)) {
                chFuture.channel().close();
                return;
            }
        }
        cameraCommunicationError("Connection Timeout: Check your IP:" + cameraConfig.getIp() + " and PORT:"
                + cameraConfig.getPort() + " are correct and the camera can be reached.");
    }

    // Always use this as sendHttpGET(GET/POST/PUT/DELETE, "/foo/bar",null)//
    // The authHandler will generate a digest string and re-send using this same function when needed.
    @SuppressWarnings("null")
    public void sendHttpRequest(String httpMethod, String httpRequestURLFull, @Nullable String digestString) {
        int port = getPortFromShortenedUrl(httpRequestURLFull);
        String httpRequestURL = getTinyUrl(httpRequestURLFull);
        logger.trace("Sending camera: {}: http://{}:{}{}", httpMethod, cameraConfig.getIp(), port, httpRequestURL);
        if (mainBootstrap == null) {
            mainBootstrap = new Bootstrap();
            mainBootstrap.group(mainEventLoopGroup);
            mainBootstrap.channel(NioSocketChannel.class);
            mainBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            mainBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4500);
            mainBootstrap.option(ChannelOption.SO_SNDBUF, 1024 * 8);
            mainBootstrap.option(ChannelOption.SO_RCVBUF, 1024 * 1024);
            mainBootstrap.option(ChannelOption.TCP_NODELAY, true);
            mainBootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel socketChannel) throws Exception {
                    // HIK Alarm stream needs > 9sec idle to stop stream closing
                    socketChannel.pipeline().addLast(new IdleStateHandler(18, 0, 0));
                    socketChannel.pipeline().addLast(new HttpClientCodec());
                    socketChannel.pipeline().addLast(AUTH_HANDLER,
                            new MyNettyAuthHandler(cameraConfig.getUser(), cameraConfig.getPassword(), getHandle()));
                    socketChannel.pipeline().addLast(COMMON_HANDLER, new CommonCameraHandler());

                    switch (thing.getThingTypeUID().getId()) {
                        case AMCREST_THING:
                            socketChannel.pipeline().addLast(AMCREST_HANDLER, new AmcrestHandler(getHandle()));
                            break;
                        case DAHUA_THING:
                            socketChannel.pipeline()
                                    .addLast(new DahuaHandler(getHandle(), cameraConfig.getNvrChannel()));
                            break;
                        case DOORBIRD_THING:
                            socketChannel.pipeline().addLast(new DoorBirdHandler(getHandle()));
                            break;
                        case FOSCAM_THING:
                            socketChannel.pipeline().addLast(
                                    new FoscamHandler(getHandle(), cameraConfig.getUser(), cameraConfig.getPassword()));
                            break;
                        case HIKVISION_THING:
                            socketChannel.pipeline().addLast(HIKVISION_HANDLER,
                                    new HikvisionHandler(getHandle(), cameraConfig.getNvrChannel()));
                            break;
                        case INSTAR_THING:
                            socketChannel.pipeline().addLast(INSTAR_HANDLER, new InstarHandler(getHandle()));
                            break;
                        case REOLINK_THING:
                            socketChannel.pipeline().addLast(REOLINK_HANDLER, new ReolinkHandler(getHandle()));
                            break;
                        default:
                            socketChannel.pipeline().addLast(new HttpOnlyHandler(getHandle()));
                            break;
                    }
                }
            });
        }

        FullHttpRequest request;
        if ("GET".equals(httpMethod) || (useDigestAuth && digestString == null)) {
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod(httpMethod), httpRequestURL);
            request.headers().set("Host", cameraConfig.getIp() + ":" + port);
            request.headers().set("Connection", HttpHeaderValues.CLOSE);
        } else if ("PUT".equals(httpMethod)) {
            request = putRequestWithBody;
        } else {
            request = postRequestWithBody;
        }

        if (!basicAuth.isEmpty()) {
            if (useDigestAuth) {
                logger.warn("Camera at IP: {} had both Basic and Digest set to be used", cameraConfig.getIp());
                setBasicAuth(false);
            } else {
                request.headers().set("Authorization", "Basic " + basicAuth);
            }
        }

        if (useDigestAuth) {
            if (digestString != null) {
                request.headers().set("Authorization", "Digest " + digestString);
            }
        }

        mainBootstrap.connect(new InetSocketAddress(cameraConfig.getIp(), port))
                .addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(@Nullable ChannelFuture future) {
                        if (future == null) {
                            return;
                        }
                        if (future.isDone() && future.isSuccess()) {
                            Channel ch = future.channel();
                            openChannels.add(ch);
                            if (cameraConnectionJob != null && !isOnline.get()) {
                                bringCameraOnline();
                            }
                            openChannel(ch, httpRequestURL);
                            CommonCameraHandler commonHandler = (CommonCameraHandler) ch.pipeline().get(COMMON_HANDLER);
                            commonHandler.setURL(httpRequestURLFull);
                            MyNettyAuthHandler authHandler = (MyNettyAuthHandler) ch.pipeline().get(AUTH_HANDLER);
                            authHandler.setURL(httpMethod, httpRequestURL);

                            switch (thing.getThingTypeUID().getId()) {
                                case AMCREST_THING:
                                    AmcrestHandler amcrestHandler = (AmcrestHandler) ch.pipeline().get(AMCREST_HANDLER);
                                    amcrestHandler.setURL(httpRequestURL);
                                    break;
                                case HIKVISION_THING:
                                    HikvisionHandler hikvisionHandler = (HikvisionHandler) ch.pipeline()
                                            .get(HIKVISION_HANDLER);
                                    hikvisionHandler.setURL(httpRequestURL);
                                    break;
                                case INSTAR_THING:
                                    InstarHandler instarHandler = (InstarHandler) ch.pipeline().get(INSTAR_HANDLER);
                                    instarHandler.setURL(httpRequestURL);
                                    break;
                                case REOLINK_THING:
                                    ReolinkHandler reolinkHandler = (ReolinkHandler) ch.pipeline().get(REOLINK_HANDLER);
                                    reolinkHandler.setURL(httpRequestURL);
                                    break;
                            }
                            ch.writeAndFlush(request);
                        } else { // an error occurred
                            cameraCommunicationError(
                                    "Connection Timeout: Check your IP and PORT are correct and the camera can be reached.");
                        }
                    }
                });
    }

    public void processSnapshot(byte[] incommingSnapshot) {
        lockCurrentSnapshot.lock();
        try {
            currentSnapshot = incommingSnapshot;
            if (cameraConfig.getGifPreroll() > 0) {
                fifoSnapshotBuffer.add(incommingSnapshot);
                if (fifoSnapshotBuffer.size() > (cameraConfig.getGifPreroll() + gifRecordTime)) {
                    fifoSnapshotBuffer.removeFirst();
                }
            }
        } finally {
            lockCurrentSnapshot.unlock();
            currentSnapshotTime = Instant.now();
        }

        if (updateImageChannel) {
            updateState(CHANNEL_IMAGE, new RawType(incommingSnapshot, "image/jpeg"));
        } else if (firstMotionAlarm || motionAlarmUpdateSnapshot) {
            updateState(CHANNEL_IMAGE, new RawType(incommingSnapshot, "image/jpeg"));
            firstMotionAlarm = motionAlarmUpdateSnapshot = false;
        } else if (firstAudioAlarm || audioAlarmUpdateSnapshot) {
            updateState(CHANNEL_IMAGE, new RawType(incommingSnapshot, "image/jpeg"));
            firstAudioAlarm = audioAlarmUpdateSnapshot = false;
        }
    }

    public void startStreamServer() {
        servlet = new CameraServlet(this, httpService);
        updateState(CHANNEL_HLS_URL, new StringType("http://" + hostIp + ":" + SERVLET_PORT + "/ipcamera/"
                + getThing().getUID().getId() + "/ipcamera.m3u8"));
        updateState(CHANNEL_IMAGE_URL, new StringType("http://" + hostIp + ":" + SERVLET_PORT + "/ipcamera/"
                + getThing().getUID().getId() + "/ipcamera.jpg"));
        updateState(CHANNEL_MJPEG_URL, new StringType("http://" + hostIp + ":" + SERVLET_PORT + "/ipcamera/"
                + getThing().getUID().getId() + "/ipcamera.mjpeg"));
    }

    public void openCamerasStream() {
        if (usingRtspForMjpeg()) {
            setupFfmpegFormat(FFmpegFormat.MJPEG);
            return;
        }
        closeChannel(getTinyUrl(mjpegUri));
        // Dahua cameras crash if you refresh (close and open) the stream without this delay.
        mainEventLoopGroup.schedule(this::openMjpegStream, 300, TimeUnit.MILLISECONDS);
    }

    private void openMjpegStream() {
        sendHttpGET(mjpegUri);
    }

    private void openChannel(Channel channel, String httpRequestURL) {
        ChannelTracking tracker = channelTrackingMap.get(httpRequestURL);
        if (tracker != null && !tracker.getReply().isEmpty()) {// We need to keep the stored reply
            tracker.setChannel(channel);
            return;
        }
        channelTrackingMap.put(httpRequestURL, new ChannelTracking(channel, httpRequestURL));
    }

    public void closeChannel(String url) {
        ChannelTracking channelTracking = channelTrackingMap.get(url);
        if (channelTracking != null) {
            if (channelTracking.getChannel().isOpen()) {
                channelTracking.getChannel().close();
                return;
            }
        }
    }

    /**
     * This method should never run under normal use, if there is a bug in a camera or binding it may be possible to
     * open large amounts of channels. This may help to keep it under control and WARN the user every 8 seconds this is
     * still occurring.
     */
    private void cleanChannels() {
        for (Channel channel : openChannels) {
            boolean oldChannel = true;
            for (ChannelTracking channelTracking : channelTrackingMap.values()) {
                if (!channelTracking.getChannel().isOpen() && channelTracking.getReply().isEmpty()) {
                    channelTrackingMap.remove(channelTracking.getRequestUrl());
                }
                if (channelTracking.getChannel().equals(channel)) {
                    logger.debug("Open channel to camera is used for URL: {}", channelTracking.getRequestUrl());
                    oldChannel = false;
                }
            }
            if (oldChannel) {
                channel.close();
            }
        }
    }

    public void storeHttpReply(String url, String content) {
        ChannelTracking channelTracking = channelTrackingMap.get(url);
        if (channelTracking != null) {
            channelTracking.setReply(content);
        }
    }

    private void storeSnapshots() {
        int count = 0;
        // Need to lock as fifoSnapshotBuffer is not thread safe and new snapshots can be incoming.
        lockCurrentSnapshot.lock();
        try {
            for (byte[] foo : fifoSnapshotBuffer) {
                File file = new File(cameraConfig.getFfmpegOutput() + "snapshot" + count + ".jpg");
                count++;
                try {
                    OutputStream fos = new FileOutputStream(file);
                    fos.write(foo);
                    fos.close();
                } catch (FileNotFoundException e) {
                    logger.warn("FileNotFoundException {}", e.getMessage());
                } catch (IOException e) {
                    logger.warn("IOException {}", e.getMessage());
                }
            }
        } finally {
            lockCurrentSnapshot.unlock();
        }
    }

    public void setupFfmpegFormat(FFmpegFormat format) {
        String inputOptions = cameraConfig.getFfmpegInputOptions();
        if (cameraConfig.getFfmpegOutput().isEmpty()) {
            logger.warn("The camera tried to use a FFmpeg feature when the output folder is not set.");
            return;
        }
        if (rtspUri.isEmpty()) {
            logger.warn("The camera tried to use a FFmpeg feature when no valid input for FFmpeg is provided.");
            return;
        }
        if (cameraConfig.getFfmpegLocation().isEmpty()) {
            logger.warn("The camera tried to use a FFmpeg feature when the location for FFmpeg is not known.");
            return;
        }
        if (mjpegUri.toLowerCase().startsWith("rtsp://") || rtspUri.toLowerCase().startsWith("rtsp://")) {
            if (inputOptions.isEmpty()) {
                inputOptions = "-rtsp_transport tcp";
            }
        }

        // Make sure the folder exists, if not create it.
        new File(cameraConfig.getFfmpegOutput()).mkdirs();
        switch (format) {
            case HLS:
                if (ffmpegHLS == null) {
                    if (!inputOptions.isEmpty()) {
                        ffmpegHLS = new Ffmpeg(this, format, cameraConfig.getFfmpegLocation(),
                                "-hide_banner -loglevel warning " + inputOptions, rtspUri,
                                cameraConfig.getHlsOutOptions(), cameraConfig.getFfmpegOutput() + "ipcamera.m3u8",
                                cameraConfig.getUser(), cameraConfig.getPassword());
                    } else {
                        ffmpegHLS = new Ffmpeg(this, format, cameraConfig.getFfmpegLocation(),
                                "-hide_banner -loglevel warning", rtspUri, cameraConfig.getHlsOutOptions(),
                                cameraConfig.getFfmpegOutput() + "ipcamera.m3u8", cameraConfig.getUser(),
                                cameraConfig.getPassword());
                    }
                }
                Ffmpeg localHLS = ffmpegHLS;
                if (localHLS != null) {
                    localHLS.startConverting();
                }
                break;
            case GIF:
                if (cameraConfig.getGifPreroll() > 0) {
                    ffmpegGIF = new Ffmpeg(this, format, cameraConfig.getFfmpegLocation(),
                            "-y -r 1 -hide_banner -loglevel warning", cameraConfig.getFfmpegOutput() + "snapshot%d.jpg",
                            "-frames:v " + (cameraConfig.getGifPreroll() + gifRecordTime) + " "
                                    + cameraConfig.getGifOutOptions(),
                            cameraConfig.getFfmpegOutput() + gifFilename + ".gif", cameraConfig.getUser(),
                            cameraConfig.getPassword());
                } else {
                    if (!inputOptions.isEmpty()) {
                        inputOptions = "-y -t " + gifRecordTime + " -hide_banner -loglevel warning " + inputOptions;
                    } else {
                        inputOptions = "-y -t " + gifRecordTime + " -hide_banner -loglevel warning";
                    }
                    ffmpegGIF = new Ffmpeg(this, format, cameraConfig.getFfmpegLocation(), inputOptions, rtspUri,
                            cameraConfig.getGifOutOptions(), cameraConfig.getFfmpegOutput() + gifFilename + ".gif",
                            cameraConfig.getUser(), cameraConfig.getPassword());
                }
                if (cameraConfig.getGifPreroll() > 0) {
                    storeSnapshots();
                }
                Ffmpeg localGIF = ffmpegGIF;
                if (localGIF != null) {
                    localGIF.startConverting();
                    if (gifHistory.isEmpty()) {
                        gifHistory = gifFilename;
                    } else if (!"ipcamera".equals(gifFilename)) {
                        gifHistory = gifFilename + "," + gifHistory;
                        if (gifHistoryLength > 49) {
                            int endIndex = gifHistory.lastIndexOf(",");
                            gifHistory = gifHistory.substring(0, endIndex);
                        }
                    }
                    setChannelState(CHANNEL_GIF_HISTORY, new StringType(gifHistory));
                }
                break;
            case RECORD:
                if (!inputOptions.isEmpty()) {
                    inputOptions = "-y -t " + mp4RecordTime + " -hide_banner -loglevel warning " + inputOptions;
                } else {
                    inputOptions = "-y -t " + mp4RecordTime + " -hide_banner -loglevel warning";
                }
                ffmpegRecord = new Ffmpeg(this, format, cameraConfig.getFfmpegLocation(), inputOptions, rtspUri,
                        cameraConfig.getMp4OutOptions(), cameraConfig.getFfmpegOutput() + mp4Filename + ".mp4",
                        cameraConfig.getUser(), cameraConfig.getPassword());
                ffmpegRecord.startConverting();
                if (mp4History.isEmpty()) {
                    mp4History = mp4Filename;
                } else if (!"ipcamera".equals(mp4Filename)) {
                    mp4History = mp4Filename + "," + mp4History;
                    if (mp4HistoryLength > 49) {
                        int endIndex = mp4History.lastIndexOf(",");
                        mp4History = mp4History.substring(0, endIndex);
                    }
                }
                setChannelState(CHANNEL_MP4_HISTORY, new StringType(mp4History));
                break;
            case RTSP_ALARMS:
                Ffmpeg localAlarms = ffmpegRtspHelper;
                if (localAlarms != null) {
                    localAlarms.stopConverting();
                    if (!ffmpegAudioAlarmEnabled && !ffmpegMotionAlarmEnabled) {
                        return;
                    }
                }
                String input = (cameraConfig.getAlarmInputUrl().isEmpty()) ? rtspUri : cameraConfig.getAlarmInputUrl();
                String filterOptions = "";
                if (!ffmpegAudioAlarmEnabled) {
                    filterOptions = "-an";
                } else {
                    filterOptions = "-af silencedetect=n=-" + audioThreshold + "dB:d=2";
                }
                if (!ffmpegMotionAlarmEnabled && !ffmpegSnapshotGeneration) {
                    filterOptions = filterOptions.concat(" -vn");
                } else if (ffmpegMotionAlarmEnabled && !cameraConfig.getMotionOptions().isEmpty()) {
                    String usersMotionOptions = cameraConfig.getMotionOptions();
                    if (usersMotionOptions.startsWith("-")) {
                        // Need to put the users custom options first in the chain before the motion is detected
                        filterOptions += " " + usersMotionOptions + ",select='gte(scene,"
                                + motionThreshold.divide(BIG_DECIMAL_SCALE_MOTION) + ")',metadata=print";
                    } else {
                        filterOptions = filterOptions + " " + usersMotionOptions + " -vf select='gte(scene,"
                                + motionThreshold.divide(BIG_DECIMAL_SCALE_MOTION) + ")',metadata=print";
                    }
                } else if (ffmpegMotionAlarmEnabled) {
                    filterOptions = filterOptions.concat(" -vf select='gte(scene,"
                            + motionThreshold.divide(BIG_DECIMAL_SCALE_MOTION) + ")',metadata=print");
                }
                ffmpegRtspHelper = new Ffmpeg(this, format, cameraConfig.getFfmpegLocation(), inputOptions, input,
                        filterOptions, "-f null -", cameraConfig.getUser(), cameraConfig.getPassword());
                ffmpegRtspHelper.startConverting();
                break;
            case MJPEG:
                if (ffmpegMjpeg == null) {
                    if (inputOptions.isEmpty()) {
                        inputOptions = "-hide_banner";
                    } else {
                        inputOptions += " -hide_banner";
                    }
                    if (mjpegUri.toLowerCase().startsWith("rtsp://")) {
                        ffmpegMjpeg = new Ffmpeg(this, format, cameraConfig.getFfmpegLocation(), inputOptions, mjpegUri,
                                cameraConfig.getMjpegOptions(), "http://127.0.0.1:" + SERVLET_PORT + "/ipcamera/"
                                        + getThing().getUID().getId() + "/ipcamera.jpg",
                                cameraConfig.getUser(), cameraConfig.getPassword());
                    } else {
                        ffmpegMjpeg = new Ffmpeg(this, format, cameraConfig.getFfmpegLocation(), inputOptions, rtspUri,
                                cameraConfig.getMjpegOptions(), "http://127.0.0.1:" + SERVLET_PORT + "/ipcamera/"
                                        + getThing().getUID().getId() + "/ipcamera.jpg",
                                cameraConfig.getUser(), cameraConfig.getPassword());
                    }
                }
                Ffmpeg localMjpeg = ffmpegMjpeg;
                if (localMjpeg != null) {
                    localMjpeg.startConverting();
                }
                break;
            case SNAPSHOT:
                // if mjpeg stream you can use 'ffmpeg -i input -codec:v copy -bsf:v mjpeg2jpeg output.jpg'
                if (ffmpegSnapshot == null) {
                    if (inputOptions.isEmpty()) {
                        // iFrames only
                        inputOptions = "-threads 1 -skip_frame nokey -hide_banner";
                    } else {
                        inputOptions += " -threads 1 -skip_frame nokey -hide_banner";
                    }
                    ffmpegSnapshot = new Ffmpeg(this, format, cameraConfig.getFfmpegLocation(), inputOptions, rtspUri,
                            cameraConfig.getSnapshotOptions(), "http://127.0.0.1:" + SERVLET_PORT + "/ipcamera/"
                                    + getThing().getUID().getId() + "/snapshot.jpg",
                            cameraConfig.getUser(), cameraConfig.getPassword());
                }
                Ffmpeg localSnaps = ffmpegSnapshot;
                if (localSnaps != null) {
                    localSnaps.startConverting();
                }
                break;
        }
    }

    public void noMotionDetected(String thisAlarmsChannel) {
        setChannelState(thisAlarmsChannel, OnOffType.OFF);
        firstMotionAlarm = false;
        motionAlarmUpdateSnapshot = false;
        motionDetected = false;
        if (streamingAutoFps) {
            stopSnapshotPolling();
        } else if (cameraConfig.getUpdateImageWhen().contains("4")) { // During Motion Alarms
            stopSnapshotPolling();
        }
    }

    /**
     * The {@link changeAlarmState} To only be used to change alarms channels that are not counted as motion. This will
     * allow logic to be added here in the future. Example more than 1 type of alarm may indicate that someone is
     * tampering with the camera.
     */
    public void changeAlarmState(String thisAlarmsChannel, OnOffType state) {
        updateState(thisAlarmsChannel, state);
    }

    public void motionDetected(String thisAlarmsChannel) {
        updateState(CHANNEL_LAST_MOTION_TYPE, new StringType(thisAlarmsChannel));
        updateState(thisAlarmsChannel, OnOffType.ON);
        motionDetected = true;
        if (streamingAutoFps) {
            startSnapshotPolling();
        }
        if (cameraConfig.getUpdateImageWhen().contains("2")) {
            if (!firstMotionAlarm) {
                if (!snapshotUri.isEmpty()) {
                    updateSnapshot();
                }
                firstMotionAlarm = true;// reset back to false when the jpg arrives.
            }
        } else if (cameraConfig.getUpdateImageWhen().contains("4")) { // During Motion Alarms
            if (!snapshotPolling) {
                startSnapshotPolling();
            }
            firstMotionAlarm = true;
            motionAlarmUpdateSnapshot = true;
        }
    }

    public void audioDetected() {
        updateState(CHANNEL_AUDIO_ALARM, OnOffType.ON);
        if (cameraConfig.getUpdateImageWhen().contains("3")) {
            if (!firstAudioAlarm) {
                if (!snapshotUri.isEmpty()) {
                    updateSnapshot();
                }
                firstAudioAlarm = true;// reset back to false when the jpg arrives.
            }
        } else if (cameraConfig.getUpdateImageWhen().contains("5")) {// During audio alarms
            firstAudioAlarm = true;
            audioAlarmUpdateSnapshot = true;
        }
    }

    public void noAudioDetected() {
        setChannelState(CHANNEL_AUDIO_ALARM, OnOffType.OFF);
        firstAudioAlarm = false;
        audioAlarmUpdateSnapshot = false;
    }

    public void recordMp4(String filename, int seconds) {
        mp4Filename = filename;
        mp4RecordTime = seconds;
        setupFfmpegFormat(FFmpegFormat.RECORD);
        setChannelState(CHANNEL_RECORDING_MP4, DecimalType.valueOf(new String("" + seconds)));
    }

    public void recordGif(String filename, int seconds) {
        gifFilename = filename;
        gifRecordTime = seconds;
        if (cameraConfig.getGifPreroll() > 0) {
            snapCount = seconds;
        } else {
            setupFfmpegFormat(FFmpegFormat.GIF);
        }
        setChannelState(CHANNEL_RECORDING_GIF, DecimalType.valueOf(new String("" + seconds)));
    }

    public void reboot() {
        switch (thing.getThingTypeUID().getId()) {
            case REOLINK_THING:
                ReolinkHandler reolinkHandler = new ReolinkHandler(getHandle());
                reolinkHandler.reboot();
                break;
            default:
                logger.warn("Reboot is not yet supported for ipcamera type {}", thing.getThingTypeUID().getId());
        }
    }

    private void getReolinkToken() {
        sendHttpPOST("/api.cgi?cmd=Login",
                "[{\"cmd\":\"Login\", \"param\":{ \"User\":{ \"Version\": \"0\", \"userName\":\""
                        + cameraConfig.getUser() + "\", \"password\":\"" + cameraConfig.getPassword() + "\"}}}]");
    }

    public String returnValueFromString(String rawString, String searchedString) {
        String result = "";
        int index = rawString.indexOf(searchedString);
        if (index != -1) // -1 means "not found"
        {
            result = rawString.substring(index + searchedString.length(), rawString.length());
            index = result.indexOf("\r\n"); // find a carriage return to find the end of the value.
            if (index == -1) {
                return result; // Did not find a carriage return.
            } else {
                return result.substring(0, index);
            }
        }
        return ""; // Did not find the String we were searching for
    }

    private void sendPTZRequest() {
        onvifCamera.sendPTZRequest(OnvifConnection.RequestType.AbsoluteMove);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_MJPEG_URL:
                updateState(CHANNEL_MJPEG_URL, new StringType("http://" + hostIp + ":" + SERVLET_PORT + "/ipcamera/"
                        + getThing().getUID().getId() + "/ipcamera.mjpeg"));
                break;
            case CHANNEL_HLS_URL:
                updateState(CHANNEL_HLS_URL, new StringType("http://" + hostIp + ":" + SERVLET_PORT + "/ipcamera/"
                        + getThing().getUID().getId() + "/ipcamera.m3u8"));
                break;
            case CHANNEL_IMAGE_URL:
                updateState(CHANNEL_IMAGE_URL, new StringType("http://" + hostIp + ":" + SERVLET_PORT + "/ipcamera/"
                        + getThing().getUID().getId() + "/ipcamera.jpg"));
                break;
        }
    }

    public void removeChannels(List<org.openhab.core.thing.Channel> removeChannels) {
        if (!removeChannels.isEmpty()) {
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withoutChannels(removeChannels);
            updateThing(thingBuilder.build());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_PAN:
                    if (onvifCamera.supportsPTZ()) {
                        updateState(CHANNEL_PAN, new PercentType(Math.round(onvifCamera.getAbsolutePan())));
                    }
                    return;
                case CHANNEL_TILT:
                    if (onvifCamera.supportsPTZ()) {
                        updateState(CHANNEL_TILT, new PercentType(Math.round(onvifCamera.getAbsoluteTilt())));
                    }
                    return;
                case CHANNEL_ZOOM:
                    if (onvifCamera.supportsPTZ()) {
                        updateState(CHANNEL_ZOOM, new PercentType(Math.round(onvifCamera.getAbsoluteZoom())));
                    }
                    return;
                case CHANNEL_GOTO_PRESET:
                    if (onvifCamera.supportsPTZ()) {
                        onvifCamera.sendPTZRequest(OnvifConnection.RequestType.GetPresets);
                    }
                    return;
            }
        } // caution "REFRESH" can still progress to brand Handlers below the else.
        else {
            switch (channelUID.getId()) {
                case CHANNEL_MP4_HISTORY_LENGTH:
                    if (DecimalType.ZERO.equals(command)) {
                        mp4HistoryLength = 0;
                        mp4History = "";
                        setChannelState(CHANNEL_MP4_HISTORY, new StringType(mp4History));
                    }
                    return;
                case CHANNEL_GIF_HISTORY_LENGTH:
                    if (DecimalType.ZERO.equals(command)) {
                        gifHistoryLength = 0;
                        gifHistory = "";
                        setChannelState(CHANNEL_GIF_HISTORY, new StringType(gifHistory));
                    }
                    return;
                case CHANNEL_FFMPEG_MOTION_CONTROL:
                    if (OnOffType.ON.equals(command)) {
                        ffmpegMotionAlarmEnabled = true;
                    } else if (OnOffType.OFF.equals(command) || DecimalType.ZERO.equals(command)) {
                        ffmpegMotionAlarmEnabled = false;
                        noMotionDetected(CHANNEL_FFMPEG_MOTION_ALARM);
                    } else if (command instanceof PercentType percentCommand) {
                        ffmpegMotionAlarmEnabled = true;
                        motionThreshold = percentCommand.toBigDecimal();
                    }
                    setupFfmpegFormat(FFmpegFormat.RTSP_ALARMS);
                    return;
                case CHANNEL_START_STREAM:
                    Ffmpeg localHLS;
                    if (OnOffType.ON.equals(command)) {
                        localHLS = ffmpegHLS;
                        if (localHLS == null) {
                            setupFfmpegFormat(FFmpegFormat.HLS);
                            localHLS = ffmpegHLS;
                        }
                        if (localHLS != null) {
                            localHLS.setKeepAlive(-1);// Now will run till manually stopped.
                            localHLS.startConverting();
                        }
                    } else {
                        localHLS = ffmpegHLS;
                        if (localHLS != null) {
                            // Still runs but will be able to auto stop when the HLS stream is no longer used.
                            localHLS.setKeepAlive(1);
                        }
                    }
                    return;
                case CHANNEL_EXTERNAL_MOTION:
                    if (OnOffType.ON.equals(command)) {
                        motionDetected(CHANNEL_EXTERNAL_MOTION);
                    } else {
                        noMotionDetected(CHANNEL_EXTERNAL_MOTION);
                    }
                    return;
                case CHANNEL_GOTO_PRESET:
                    if (onvifCamera.supportsPTZ()) {
                        onvifCamera.gotoPreset(Integer.valueOf(command.toString()));
                    }
                    return;
                case CHANNEL_CREATE_SNAPSHOTS:
                    if (OnOffType.ON.equals(command)) {
                        if (snapshotUri.isEmpty()) {
                            ffmpegSnapshotGeneration = true;
                            setupFfmpegFormat(FFmpegFormat.SNAPSHOT);
                        }
                    } else {
                        Ffmpeg localSnaps = ffmpegSnapshot;
                        if (localSnaps != null) {
                            localSnaps.stopConverting();
                            ffmpegSnapshotGeneration = false;
                            updateState(CHANNEL_CREATE_SNAPSHOTS, OnOffType.OFF);
                        }
                    }
                    return;
                case CHANNEL_POLL_IMAGE:
                    if (OnOffType.ON.equals(command)) {
                        updateImageChannel = true;
                        updateSnapshot();// Allows this to change Image FPS on demand
                    } else {
                        updateImageChannel = false;
                    }
                    return;
                case CHANNEL_PAN:
                    if (onvifCamera.supportsPTZ()) {
                        if (command instanceof IncreaseDecreaseType) {
                            if (command == IncreaseDecreaseType.INCREASE) {
                                if (cameraConfig.getPtzContinuous()) {
                                    onvifCamera.sendPTZRequest(OnvifConnection.RequestType.ContinuousMoveLeft);
                                } else {
                                    onvifCamera.sendPTZRequest(OnvifConnection.RequestType.RelativeMoveLeft);
                                }
                            } else {
                                if (cameraConfig.getPtzContinuous()) {
                                    onvifCamera.sendPTZRequest(OnvifConnection.RequestType.ContinuousMoveRight);
                                } else {
                                    onvifCamera.sendPTZRequest(OnvifConnection.RequestType.RelativeMoveRight);
                                }
                            }
                            return;
                        } else if (OnOffType.OFF.equals(command)) {
                            onvifCamera.sendPTZRequest(OnvifConnection.RequestType.Stop);
                            return;
                        }
                        onvifCamera.setAbsolutePan(Float.valueOf(command.toString()));
                        mainEventLoopGroup.schedule(this::sendPTZRequest, 500, TimeUnit.MILLISECONDS);
                    }
                    return;
                case CHANNEL_TILT:
                    if (onvifCamera.supportsPTZ()) {
                        if (command instanceof IncreaseDecreaseType) {
                            if (IncreaseDecreaseType.INCREASE.equals(command)) {
                                if (cameraConfig.getPtzContinuous()) {
                                    onvifCamera.sendPTZRequest(OnvifConnection.RequestType.ContinuousMoveUp);
                                } else {
                                    onvifCamera.sendPTZRequest(OnvifConnection.RequestType.RelativeMoveUp);
                                }
                            } else {
                                if (cameraConfig.getPtzContinuous()) {
                                    onvifCamera.sendPTZRequest(OnvifConnection.RequestType.ContinuousMoveDown);
                                } else {
                                    onvifCamera.sendPTZRequest(OnvifConnection.RequestType.RelativeMoveDown);
                                }
                            }
                            return;
                        } else if (OnOffType.OFF.equals(command)) {
                            onvifCamera.sendPTZRequest(OnvifConnection.RequestType.Stop);
                            return;
                        }
                        onvifCamera.setAbsoluteTilt(Float.valueOf(command.toString()));
                        mainEventLoopGroup.schedule(this::sendPTZRequest, 500, TimeUnit.MILLISECONDS);
                    }
                    return;
                case CHANNEL_ZOOM:
                    if (onvifCamera.supportsPTZ()) {
                        if (command instanceof IncreaseDecreaseType) {
                            if (IncreaseDecreaseType.INCREASE.equals(command)) {
                                if (cameraConfig.getPtzContinuous()) {
                                    onvifCamera.sendPTZRequest(OnvifConnection.RequestType.ContinuousMoveIn);
                                } else {
                                    onvifCamera.sendPTZRequest(OnvifConnection.RequestType.RelativeMoveIn);
                                }
                            } else {
                                if (cameraConfig.getPtzContinuous()) {
                                    onvifCamera.sendPTZRequest(OnvifConnection.RequestType.ContinuousMoveOut);
                                } else {
                                    onvifCamera.sendPTZRequest(OnvifConnection.RequestType.RelativeMoveOut);
                                }
                            }
                            return;
                        } else if (OnOffType.OFF.equals(command)) {
                            onvifCamera.sendPTZRequest(OnvifConnection.RequestType.Stop);
                            return;
                        }
                        onvifCamera.setAbsoluteZoom(Float.valueOf(command.toString()));
                        mainEventLoopGroup.schedule(this::sendPTZRequest, 500, TimeUnit.MILLISECONDS);
                    }
                    return;
            }
        }
        // commands and refresh now get passed to brand handlers
        switch (thing.getThingTypeUID().getId()) {
            case AMCREST_THING:
                AmcrestHandler amcrestHandler = new AmcrestHandler(getHandle());
                amcrestHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests.isEmpty()) {
                    lowPriorityRequests = amcrestHandler.getLowPriorityRequests();
                }
                break;
            case DAHUA_THING:
                DahuaHandler dahuaHandler = new DahuaHandler(getHandle(), cameraConfig.getNvrChannel());
                dahuaHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests.isEmpty()) {
                    lowPriorityRequests = dahuaHandler.getLowPriorityRequests();
                }
                break;
            case DOORBIRD_THING:
                DoorBirdHandler doorBirdHandler = new DoorBirdHandler(getHandle());
                doorBirdHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests.isEmpty()) {
                    lowPriorityRequests = doorBirdHandler.getLowPriorityRequests();
                }
                break;
            case HIKVISION_THING:
                HikvisionHandler hikvisionHandler = new HikvisionHandler(getHandle(), cameraConfig.getNvrChannel());
                hikvisionHandler.handleCommand(channelUID, command);
                break;
            case FOSCAM_THING:
                FoscamHandler foscamHandler = new FoscamHandler(getHandle(), cameraConfig.getUser(),
                        cameraConfig.getPassword());
                foscamHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests.isEmpty()) {
                    lowPriorityRequests = foscamHandler.getLowPriorityRequests();
                }
                break;
            case INSTAR_THING:
                InstarHandler instarHandler = new InstarHandler(getHandle());
                instarHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests.isEmpty()) {
                    lowPriorityRequests = instarHandler.getLowPriorityRequests();
                }
                break;
            case REOLINK_THING:
                ReolinkHandler reolinkHandler = new ReolinkHandler(getHandle());
                reolinkHandler.handleCommand(channelUID, command);
                break;
            default:
                HttpOnlyHandler defaultHandler = new HttpOnlyHandler(getHandle());
                defaultHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests.isEmpty()) {
                    lowPriorityRequests = defaultHandler.getLowPriorityRequests();
                }
                break;
        }
    }

    public void setChannelState(String channelToUpdate, State valueOf) {
        updateState(channelToUpdate, valueOf);
    }

    private void bringCameraOnline() {
        isOnline.set(true);
        updateStatus(ThingStatus.ONLINE);
        groupTracker.listOfOnlineCameraHandlers.add(this);
        groupTracker.listOfOnlineCameraUID.add(getThing().getUID().getId());
        Future<?> localFuture = cameraConnectionJob;
        if (localFuture != null) {
            localFuture.cancel(false);
            cameraConnectionJob = null;
        }
        if (!snapshotUri.isEmpty()) {
            if (cameraConfig.getGifPreroll() > 0 || cameraConfig.getUpdateImageWhen().contains("1")) {
                snapshotPolling = true;
                snapshotJob = threadPool.scheduleWithFixedDelay(this::snapshotRunnable, 1000,
                        cameraConfig.getPollTime(), TimeUnit.MILLISECONDS);
            }
        }

        pollCameraJob = threadPool.scheduleWithFixedDelay(this::pollCameraRunnable, 1000, 8000, TimeUnit.MILLISECONDS);

        // auto restart mjpeg stream now camera is back online.
        CameraServlet localServlet = servlet;
        if (localServlet != null && !localServlet.openStreams.isEmpty()) {
            openCamerasStream();
        }

        if (!rtspUri.isEmpty()) {
            updateState(CHANNEL_RTSP_URL, new StringType(rtspUri));
        }
        if (updateImageChannel) {
            updateState(CHANNEL_POLL_IMAGE, OnOffType.ON);
        } else {
            updateState(CHANNEL_POLL_IMAGE, OnOffType.OFF);
        }
        if (!groupTracker.listOfGroupHandlers.isEmpty()) {
            for (IpCameraGroupHandler handle : groupTracker.listOfGroupHandlers) {
                handle.cameraOnline(getThing().getUID().getId());
            }
        }
        if (thing.getThingTypeUID().getId().equals(REOLINK_THING) && cameraConfig.useToken
                && authenticationJob == null) {
            logger.debug("Token thread for REOLINK was stopped, restarting it now.");
            authenticationJob = threadPool.scheduleWithFixedDelay(this::getReolinkToken, 0, 45, TimeUnit.MINUTES);
        }
        // Ask camera and update openHAB controls to match cameras settings
        List<org.openhab.core.thing.Channel> channels = thing.getChannels();
        for (org.openhab.core.thing.Channel channel : channels) {
            this.handleCommand(channel.getUID(), RefreshType.REFRESH);
        }
    }

    void snapshotIsFfmpeg() {
        snapshotUri = "";// ffmpeg is a valid option. Simplify further checks.
        logger.debug(
                "Binding has no snapshot url. Will use your CPU and FFmpeg to create snapshots from the cameras RTSP.");
        bringCameraOnline();
        if (!rtspUri.isEmpty()) {
            updateImageChannel = false;
            ffmpegSnapshotGeneration = true;
            setupFfmpegFormat(FFmpegFormat.SNAPSHOT);
            updateState(CHANNEL_CREATE_SNAPSHOTS, OnOffType.ON);
        } else {
            cameraConfigError("Binding can not find a RTSP url for this camera, please provide a FFmpeg Input URL.");
        }
    }

    /**
     * The {@link pollingCameraConnection} This polls to see if the camera is reachable only until the camera
     * successfully connects.
     *
     */

    void pollingCameraConnection() {
        keepMjpegRunning();
        if (thing.getThingTypeUID().getId().equals(GENERIC_THING)
                || thing.getThingTypeUID().getId().equals(DOORBIRD_THING)) {
            if (rtspUri.isEmpty()) {
                logger.warn("Binding has not been supplied with a FFmpeg Input URL, so some features will not work.");
            }
            if (snapshotUri.isEmpty() || "ffmpeg".equals(snapshotUri)) {
                snapshotIsFfmpeg();
            } else {
                ffmpegSnapshotGeneration = false;
                updateSnapshot();
            }
            return;
        }
        if (cameraConfig.getOnvifPort() > 0 && !onvifCamera.isConnected()) {
            logger.debug("About to connect to the IP Camera using the ONVIF PORT at IP: {}:{}", cameraConfig.getIp(),
                    cameraConfig.getOnvifPort());
            onvifCamera.connect(supportsOnvifEvents());
            return;
        }
        if ("ffmpeg".equals(snapshotUri)) {
            snapshotIsFfmpeg();
        } else if (!snapshotUri.isEmpty()) {
            ffmpegSnapshotGeneration = false;
            updateSnapshot();
        } else if (!rtspUri.isEmpty()) {
            snapshotIsFfmpeg();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Camera failed to report a valid Snaphot and/or RTSP URL. Check user/pass is correct, or use the advanced configs to manually provide a URL.");
        }
    }

    public void cameraConfigError(String reason) {
        // won't try to reconnect again due to a config error being the cause.
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, reason);
        dispose();
    }

    public void cameraCommunicationError(String reason) {
        // will try to reconnect again as camera may be rebooting.
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        if (isOnline.get()) { // if already offline dont try reconnecting in 6 seconds, we want 30sec wait.
            resetAndRetryConnecting();
        }
    }

    private boolean streamIsStopped(String url) {
        ChannelTracking channelTracking = channelTrackingMap.get(url);
        if (channelTracking != null) {
            if (channelTracking.getChannel().isActive()) {
                return false; // stream is running.
            }
        }
        return true; // Stream stopped or never started.
    }

    void snapshotRunnable() {
        // Snapshot should be first to keep consistent time between shots
        updateSnapshot();
        if (snapCount > 0) {
            if (--snapCount == 0) {
                setupFfmpegFormat(FFmpegFormat.GIF);
            }
        }
    }

    private void takeSnapshot() {
        sendHttpGET(snapshotUri);
    }

    private void updateSnapshot() {
        lastSnapshotRequest = Instant.now();
        mainEventLoopGroup.schedule(this::takeSnapshot, 0, TimeUnit.MILLISECONDS);
    }

    public byte[] getSnapshot() {
        if (!isOnline.get()) {
            // Single gray pixel JPG to keep streams open when the camera goes offline so they dont stop.
            return new byte[] { (byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xe0, 0x00, 0x10, 0x4a, 0x46, 0x49, 0x46,
                    0x00, 0x01, 0x01, 0x01, 0x00, 0x48, 0x00, 0x48, 0x00, 0x00, (byte) 0xff, (byte) 0xdb, 0x00, 0x43,
                    0x00, 0x03, 0x02, 0x02, 0x02, 0x02, 0x02, 0x03, 0x02, 0x02, 0x02, 0x03, 0x03, 0x03, 0x03, 0x04,
                    0x06, 0x04, 0x04, 0x04, 0x04, 0x04, 0x08, 0x06, 0x06, 0x05, 0x06, 0x09, 0x08, 0x0a, 0x0a, 0x09,
                    0x08, 0x09, 0x09, 0x0a, 0x0c, 0x0f, 0x0c, 0x0a, 0x0b, 0x0e, 0x0b, 0x09, 0x09, 0x0d, 0x11, 0x0d,
                    0x0e, 0x0f, 0x10, 0x10, 0x11, 0x10, 0x0a, 0x0c, 0x12, 0x13, 0x12, 0x10, 0x13, 0x0f, 0x10, 0x10,
                    0x10, (byte) 0xff, (byte) 0xc9, 0x00, 0x0b, 0x08, 0x00, 0x01, 0x00, 0x01, 0x01, 0x01, 0x11, 0x00,
                    (byte) 0xff, (byte) 0xcc, 0x00, 0x06, 0x00, 0x10, 0x10, 0x05, (byte) 0xff, (byte) 0xda, 0x00, 0x08,
                    0x01, 0x01, 0x00, 0x00, 0x3f, 0x00, (byte) 0xd2, (byte) 0xcf, 0x20, (byte) 0xff, (byte) 0xd9 };
        }
        // Most cameras will return a 503 busy error if snapshot is faster than 1 second
        long lastUpdatedMs = Duration.between(lastSnapshotRequest, Instant.now()).toMillis();
        if (!snapshotPolling && !ffmpegSnapshotGeneration && lastUpdatedMs >= cameraConfig.getPollTime()) {
            updateSnapshot();
        }
        lockCurrentSnapshot.lock();
        try {
            return currentSnapshot;
        } finally {
            lockCurrentSnapshot.unlock();
        }
    }

    public void stopSnapshotPolling() {
        Future<?> localFuture;
        if (!streamingSnapshotMjpeg && cameraConfig.getGifPreroll() == 0
                && !cameraConfig.getUpdateImageWhen().contains("1")) {
            snapshotPolling = false;
            localFuture = snapshotJob;
            if (localFuture != null) {
                localFuture.cancel(true);
            }
        } else if (cameraConfig.getUpdateImageWhen().contains("4")) { // only during Motion Alarms
            snapshotPolling = false;
            localFuture = snapshotJob;
            if (localFuture != null) {
                localFuture.cancel(true);
            }
        }
    }

    public void startSnapshotPolling() {
        if (snapshotPolling || ffmpegSnapshotGeneration) {
            return; // Already polling or creating with FFmpeg from RTSP
        }
        if (streamingSnapshotMjpeg || streamingAutoFps || cameraConfig.getUpdateImageWhen().contains("4")) {
            snapshotPolling = true;
            snapshotJob = threadPool.scheduleWithFixedDelay(this::snapshotRunnable, 0, cameraConfig.getPollTime(),
                    TimeUnit.MILLISECONDS);
        }
    }

    /**
     * {@link pollCameraRunnable} Polls every 8 seconds, to check camera is still ONLINE and keep alarm
     * streams open and more.
     *
     */
    void pollCameraRunnable() {
        // NOTE: Use lowPriorityRequests if get request is not needed every poll.
        if (!lowPriorityRequests.isEmpty()) {
            if (lowPriorityCounter >= lowPriorityRequests.size()) {
                lowPriorityCounter = 0;
            }
            sendHttpGET(lowPriorityRequests.get(lowPriorityCounter++));
        }
        // what needs to be done every poll//
        switch (thing.getThingTypeUID().getId()) {
            case GENERIC_THING:
                checkCameraConnection();
                break;
            case ONVIF_THING:
                onvifCamera.checkAndRenewEventSubscription();
                break;
            case INSTAR_THING:
                checkCameraConnection();
                noMotionDetected(CHANNEL_MOTION_ALARM);
                noMotionDetected(CHANNEL_PIR_ALARM);
                noMotionDetected(CHANNEL_HUMAN_ALARM);
                noMotionDetected(CHANNEL_CAR_ALARM);
                noMotionDetected(CHANNEL_ANIMAL_ALARM);
                noAudioDetected();
                break;
            case HIKVISION_THING:
                if (streamIsStopped("/ISAPI/Event/notification/alertStream")) {
                    logger.info("The alarm stream was not running for camera {}, re-starting it now",
                            cameraConfig.getIp());
                    sendHttpGET("/ISAPI/Event/notification/alertStream");
                }
                break;
            case AMCREST_THING:
                sendHttpGET("/cgi-bin/eventManager.cgi?action=getEventIndexes&code=VideoMotion");
                sendHttpGET("/cgi-bin/eventManager.cgi?action=getEventIndexes&code=AudioMutation");
                break;
            case REOLINK_THING:
                if (cameraConfig.getOnvifPort() == 0) {
                    sendHttpGET("/api.cgi?cmd=GetAiState&channel=" + cameraConfig.getNvrChannel() + reolinkAuth);
                    sendHttpGET("/api.cgi?cmd=GetMdState&channel=" + cameraConfig.getNvrChannel() + reolinkAuth);
                } else {
                    onvifCamera.checkAndRenewEventSubscription();
                }
                break;
            case DAHUA_THING:
                // Check for alarms, channel for NVRs appears not to work at filtering.
                if (streamIsStopped("/cgi-bin/eventManager.cgi?action=attach&codes=[All]")) {
                    logger.info("The alarm stream was not running for camera {}, re-starting it now",
                            cameraConfig.getIp());
                    sendHttpGET("/cgi-bin/eventManager.cgi?action=attach&codes=[All]");
                }
                break;
            case DOORBIRD_THING:
                // Check for alarms, channel for NVRs appears not to work at filtering.
                if (streamIsStopped("/bha-api/monitor.cgi?ring=doorbell,motionsensor")) {
                    logger.info("The alarm stream was not running for camera {}, re-starting it now",
                            cameraConfig.getIp());
                    sendHttpGET("/bha-api/monitor.cgi?ring=doorbell,motionsensor");
                }
                break;
            case FOSCAM_THING:
                sendHttpGET("/cgi-bin/CGIProxy.fcgi?cmd=getDevState&usr=" + cameraConfig.getUser() + "&pwd="
                        + cameraConfig.getPassword());
                break;
        }
        Ffmpeg localFfmpeg = ffmpegHLS;
        if (localFfmpeg != null) {
            localFfmpeg.checkKeepAlive();
        }
        if (ffmpegMotionAlarmEnabled || ffmpegAudioAlarmEnabled) {
            localFfmpeg = ffmpegRtspHelper;
            if (localFfmpeg == null || !localFfmpeg.isAlive()) {
                setupFfmpegFormat(FFmpegFormat.RTSP_ALARMS);
            }
        }
        // check if the thread has frozen due to camera doing a soft reboot
        localFfmpeg = ffmpegMjpeg;
        if (localFfmpeg != null && !localFfmpeg.isAlive()) {
            logger.debug("MJPEG was not being produced by FFmpeg when it should have been, restarting FFmpeg.");
            localFfmpeg.stopConverting();
            setupFfmpegFormat(FFmpegFormat.MJPEG);
        }
        if (openChannels.size() > 10) {
            logger.debug("There are {} open Channels being tracked.", openChannels.size());
            cleanChannels();
        }
    }

    @Override
    public void initialize() {
        cameraConfig = getConfigAs(CameraConfig.class);
        threadPool = Executors.newScheduledThreadPool(2);
        mainEventLoopGroup = new NioEventLoopGroup(3);
        snapshotUri = getCorrectUrlFormat(cameraConfig.getSnapshotUrl());
        mjpegUri = cameraConfig.getMjpegUrl();
        if (!mjpegUri.toLowerCase().startsWith("rtsp://")) {
            mjpegUri = getCorrectUrlFormat(mjpegUri);
        }
        rtspUri = cameraConfig.getFfmpegInput();
        if (cameraConfig.getFfmpegOutput().isEmpty()) {
            cameraConfig
                    .setFfmpegOutput(OpenHAB.getUserDataFolder() + "/ipcamera/" + this.thing.getUID().getId() + "/");
        }
        // Known cameras will connect quicker if we skip ONVIF questions.
        switch (thing.getThingTypeUID().getId()) {
            case AMCREST_THING:
            case DAHUA_THING:
                if (mjpegUri.isEmpty()) {
                    mjpegUri = "/cgi-bin/mjpg/video.cgi?channel=" + cameraConfig.getNvrChannel() + "&subtype=1";
                }
                if (snapshotUri.isEmpty()) {
                    snapshotUri = "/cgi-bin/snapshot.cgi?channel=" + cameraConfig.getNvrChannel();
                }
                break;
            case DOORBIRD_THING:
                if (mjpegUri.isEmpty()) {
                    mjpegUri = "/bha-api/video.cgi";
                }
                if (snapshotUri.isEmpty()) {
                    snapshotUri = "/bha-api/image.cgi";
                }
                break;
            case FOSCAM_THING:
                // Foscam needs any special char like spaces (%20) to be encoded for URLs.
                cameraConfig.setUser(Helper.encodeSpecialChars(cameraConfig.getUser()));
                cameraConfig.setPassword(Helper.encodeSpecialChars(cameraConfig.getPassword()));
                if (mjpegUri.isEmpty()) {
                    mjpegUri = "/cgi-bin/CGIStream.cgi?cmd=GetMJStream&usr=" + cameraConfig.getUser() + "&pwd="
                            + cameraConfig.getPassword();
                }
                if (snapshotUri.isEmpty()) {
                    snapshotUri = "/cgi-bin/CGIProxy.fcgi?usr=" + cameraConfig.getUser() + "&pwd="
                            + cameraConfig.getPassword() + "&cmd=snapPicture2";
                }
                break;
            case HIKVISION_THING:// The 02 gives you the first sub stream which needs to be set to MJPEG
                if (mjpegUri.isEmpty()) {
                    mjpegUri = "/ISAPI/Streaming/channels/" + cameraConfig.getNvrChannel() + "02" + "/httppreview";
                }
                if (snapshotUri.isEmpty()) {
                    snapshotUri = "/ISAPI/Streaming/channels/" + cameraConfig.getNvrChannel() + "01/picture";
                }
                if (lowPriorityRequests.isEmpty()) {
                    lowPriorityRequests.add("/ISAPI/System/IO/capabilities");
                }
                break;
            case INSTAR_THING:
                if (snapshotUri.isEmpty()) {
                    snapshotUri = "/tmpfs/snap.jpg";
                }
                if (mjpegUri.isEmpty()) {
                    mjpegUri = "/mjpegstream.cgi?-chn=12";
                }
                // Newer Instar cameras use this to setup the Alarm Server, plus it is used to work out which API is
                // implemented based on the response to these two requests.
                sendHttpGET(
                        "/param.cgi?cmd=setasaction&-server=1&enable=1&-interval=1&cmd=setasattr&-as_index=1&-as_server="
                                + hostIp + "&-as_port=" + SERVLET_PORT + "&-as_path=/ipcamera/"
                                + getThing().getUID().getId()
                                + "/instar&-as_ssl=0&-as_insecure=0&-as_mode=0&-as_activequery=1&-as_auth=0&-as_query1=0&-as_query2=0&-as_query3=0&-as_query4=0&-as_query5=0");
                // Older Instar cameras use this to setup the Alarm Server
                sendHttpGET(
                        "/param.cgi?cmd=setmdalarm&-aname=server2&-switch=on&-interval=1&cmd=setalarmserverattr&-as_index=3&-as_server="
                                + hostIp + "&-as_port=" + SERVLET_PORT + "&-as_path=/ipcamera/"
                                + getThing().getUID().getId()
                                + "/instar&-as_ssl=0&-as_mode=1&-as_activequery=1&-as_auth=0&-as_query1=0&-as_query2=0&-as_query3=0&-as_query4=0&-as_query5=0");
                break;
            case REOLINK_THING:
                if (cameraConfig.useToken) {
                    authenticationJob = threadPool.scheduleWithFixedDelay(this::getReolinkToken, 0, 45,
                            TimeUnit.MINUTES);
                } else {
                    reolinkAuth = "&user=" + cameraConfig.getUser() + "&password=" + cameraConfig.getPassword();
                    // The reply to api.cgi?cmd=Login also sends this only with a token
                    sendHttpPOST("/api.cgi?cmd=GetAbility" + reolinkAuth,
                            "[{ \"cmd\":\"GetAbility\", \"param\":{ \"User\":{ \"userName\":\"admin\" }}}]");
                }
                if (snapshotUri.isEmpty()) {
                    // ReolinkHandler will change the snapshotUri in the response to /api.cgi?cmd=Login
                    snapshotUri = "/cgi-bin/api.cgi?cmd=Snap&channel=" + cameraConfig.getNvrChannel() + "&rs=openHAB"
                            + reolinkAuth;
                }
                // channel numbers for snapshots start at 0, while the rtsp start at 1
                if (rtspUri.isEmpty()) {
                    rtspUri = "rtsp://" + cameraConfig.getIp() + ":554/h264Preview_0"
                            + (cameraConfig.getNvrChannel() + 1) + "_main";
                }
                if (mjpegUri.isEmpty()) {
                    mjpegUri = "rtsp://" + cameraConfig.getIp() + ":554/h264Preview_0"
                            + (cameraConfig.getNvrChannel() + 1) + "_sub";
                }
                if (cameraConfig.getAlarmInputUrl().isEmpty()) {
                    cameraConfig.setAlarmInputUrl("rtsp://" + cameraConfig.getIp() + ":554/h264Preview_0"
                            + (cameraConfig.getNvrChannel() + 1) + "_sub");
                }
                break;
        }
        // for poll times 9 seconds and above don't display a warning about the Image channel.
        if (9000 > cameraConfig.getPollTime() && cameraConfig.getUpdateImageWhen().contains("1")) {
            logger.warn(
                    "The Image channel is set to update more often than 8 seconds. This is not recommended. The Image channel is best used only for higher poll times. See the readme file on how to display the cameras picture for best results or use a higher poll time.");
        }
        // ONVIF and Instar event handling need the server started before connecting.
        startStreamServer();
        tryConnecting();
    }

    private void tryConnecting() {
        if (!thing.getThingTypeUID().getId().equals(GENERIC_THING)
                && !thing.getThingTypeUID().getId().equals(DOORBIRD_THING) && cameraConfig.getOnvifPort() > 0) {
            onvifCamera = new OnvifConnection(this, cameraConfig.getIp() + ":" + cameraConfig.getOnvifPort(),
                    cameraConfig.getUser(), cameraConfig.getPassword());
            onvifCamera.setSelectedMediaProfile(cameraConfig.getOnvifMediaProfile());
            // Only use ONVIF events if it is not an API camera.
            onvifCamera.connect(supportsOnvifEvents());
        }
        cameraConnectionJob = threadPool.scheduleWithFixedDelay(this::pollingCameraConnection, 4, 12, TimeUnit.SECONDS);
    }

    private boolean supportsOnvifEvents() {
        switch (thing.getThingTypeUID().getId()) {
            case ONVIF_THING:
                return true;
            case REOLINK_THING:
                if (cameraConfig.getOnvifPort() > 0) {
                    return true;
                }
        }
        return false;
    }

    private void keepMjpegRunning() {
        CameraServlet localServlet = servlet;
        if (localServlet != null && !localServlet.openStreams.isEmpty()) {
            if (!usingRtspForMjpeg()) {
                localServlet.openStreams.queueFrame(("--" + localServlet.openStreams.boundary + "\r\n\r\n").getBytes());
            }
            localServlet.openStreams.queueFrame(getSnapshot());
        }
    }

    // What the camera needs to re-connect if the initialize() is not called.
    private void resetAndRetryConnecting() {
        offline();
        tryConnecting();
    }

    private void offline() {
        isOnline.set(false);
        snapshotPolling = false;
        Future<?> localFuture = pollCameraJob;
        if (localFuture != null) {
            localFuture.cancel(true);
            pollCameraJob = null;
        }
        localFuture = authenticationJob;
        if (localFuture != null) {
            localFuture.cancel(true);
            authenticationJob = null;
        }
        localFuture = snapshotJob;
        if (localFuture != null) {
            localFuture.cancel(true);
            snapshotJob = null;
        }
        localFuture = cameraConnectionJob;
        if (localFuture != null) {
            localFuture.cancel(true);
            cameraConnectionJob = null;
        }
        Ffmpeg localFfmpeg = ffmpegHLS;
        if (localFfmpeg != null) {
            localFfmpeg.stopConverting();
            ffmpegHLS = null;
        }
        localFfmpeg = ffmpegRecord;
        if (localFfmpeg != null) {
            localFfmpeg.stopConverting();
            ffmpegRecord = null;
        }
        localFfmpeg = ffmpegGIF;
        if (localFfmpeg != null) {
            localFfmpeg.stopConverting();
            ffmpegGIF = null;
        }
        localFfmpeg = ffmpegRtspHelper;
        if (localFfmpeg != null) {
            localFfmpeg.stopConverting();
            ffmpegRtspHelper = null;
        }
        localFfmpeg = ffmpegMjpeg;
        if (localFfmpeg != null) {
            localFfmpeg.stopConverting();
            ffmpegMjpeg = null;
        }
        localFfmpeg = ffmpegSnapshot;
        if (localFfmpeg != null) {
            localFfmpeg.stopConverting();
            ffmpegSnapshot = null;
        }
        if (!thing.getThingTypeUID().getId().equals(GENERIC_THING)) { // generic cameras do not have ONVIF support
            onvifCamera.disconnect();
        }
        openChannels.close();
    }

    @Override
    public void dispose() {
        offline();
        CameraServlet localServlet = servlet;
        if (localServlet != null) {
            localServlet.dispose();
            servlet = null;
        }
        threadPool.shutdown();
        // inform all group handlers that this camera has gone offline
        groupTracker.listOfOnlineCameraHandlers.remove(this);
        groupTracker.listOfOnlineCameraUID.remove(getThing().getUID().getId());
        for (IpCameraGroupHandler handle : groupTracker.listOfGroupHandlers) {
            handle.cameraOffline(this);
        }
        basicAuth = ""; // clear out stored Password hash
        useDigestAuth = false;
        mainEventLoopGroup.shutdownGracefully();
        mainBootstrap = null;
        channelTrackingMap.clear();
    }

    public String getWhiteList() {
        return cameraConfig.getIpWhitelist();
    }

    public boolean usingRtspForMjpeg() {
        return (mjpegUri.isEmpty() || "ffmpeg".equals(mjpegUri) || mjpegUri.toLowerCase().startsWith("rtsp://"));
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(IpCameraActions.class);
    }
}
