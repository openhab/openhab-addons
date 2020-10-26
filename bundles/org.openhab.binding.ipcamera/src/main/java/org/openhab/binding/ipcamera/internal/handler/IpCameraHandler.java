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

package org.openhab.binding.ipcamera.internal.handler;

import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
import org.openhab.binding.ipcamera.internal.MyNettyAuthHandler;
import org.openhab.binding.ipcamera.internal.StreamServerHandler;
import org.openhab.binding.ipcamera.internal.onvif.OnvifConnection;
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
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
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
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedWriteHandler;
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
    private ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(4);
    private GroupTracker groupTracker;
    public CameraConfig cameraConfig;

    // ChannelGroup is thread safe
    public final ChannelGroup mjpegChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final ChannelGroup snapshotMjpegChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final ChannelGroup autoSnapshotMjpegChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public final ChannelGroup openChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public @Nullable Ffmpeg ffmpegHLS = null;
    public @Nullable Ffmpeg ffmpegRecord = null;
    public @Nullable Ffmpeg ffmpegGIF = null;
    public @Nullable Ffmpeg ffmpegRtspHelper = null;
    public @Nullable Ffmpeg ffmpegMjpeg = null;
    public @Nullable Ffmpeg ffmpegSnapshot = null;
    public boolean streamingAutoFps = false;
    public boolean motionDetected = false;

    private @Nullable ScheduledFuture<?> cameraConnectionJob = null;
    private @Nullable ScheduledFuture<?> pollCameraJob = null;
    private @Nullable ScheduledFuture<?> snapshotJob = null;
    private @Nullable Bootstrap mainBootstrap;
    private @Nullable ServerBootstrap serverBootstrap;

    private EventLoopGroup mainEventLoopGroup = new NioEventLoopGroup();
    private EventLoopGroup serversLoopGroup = new NioEventLoopGroup();
    private FullHttpRequest putRequestWithBody = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod("PUT"),
            "");
    private String gifFilename = "ipcamera";
    private String gifHistory = "";
    private String mp4History = "";
    public int gifHistoryLength;
    public int mp4HistoryLength;
    private String mp4Filename = "ipcamera";
    private int mp4RecordTime;
    private int gifRecordTime = 5;
    private LinkedList<byte[]> fifoSnapshotBuffer = new LinkedList<byte[]>();
    private int snapCount;
    private boolean updateImageChannel = false;
    private boolean updateAutoFps = false;
    private byte lowPriorityCounter = 0;
    public String hostIp;
    public Map<String, ChannelTracking> channelTrackingMap = new ConcurrentHashMap<>();
    public List<String> lowPriorityRequests = new ArrayList<>(0);

    // basicAuth MUST remain private as it holds the cameraConfig.getPassword()
    private String basicAuth = "";
    public boolean useBasicAuth = false;
    public boolean useDigestAuth = false;
    public String snapshotUri = "";
    public String mjpegUri = "";
    private @Nullable ChannelFuture serverFuture = null;
    private Object firstStreamedMsg = new Object();
    public byte[] currentSnapshot = new byte[] { (byte) 0x00 };
    public ReentrantLock lockCurrentSnapshot = new ReentrantLock();
    public String rtspUri = "";
    public boolean audioAlarmUpdateSnapshot = false;
    private boolean motionAlarmUpdateSnapshot = false;
    private boolean isOnline = false; // Used so only 1 error is logged when a network issue occurs.
    private boolean firstAudioAlarm = false;
    private boolean firstMotionAlarm = false;
    public Double motionThreshold = 0.0016;
    public int audioThreshold = 35;
    @SuppressWarnings("unused")
    private @Nullable StreamServerHandler streamServerHandler;
    private boolean streamingSnapshotMjpeg = false;
    public boolean motionAlarmEnabled = false;
    public boolean audioAlarmEnabled = false;
    public boolean ffmpegSnapshotGeneration = false;
    public boolean snapshotPolling = false;
    public OnvifConnection onvifCamera = new OnvifConnection(this, "", "", "");

    // These methods handle the response from all camera brands, nothing specific to 1 brand.
    private class CommonCameraHandler extends ChannelDuplexHandler {
        private int bytesToRecieve = 0;
        private int bytesAlreadyRecieved = 0;
        private byte[] incomingJpeg = new byte[0];
        private String incomingMessage = "";
        private String contentType = "empty";
        private Object reply = new Object();
        private String requestUrl = "";
        private boolean closeConnection = true;
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
                if (msg instanceof HttpResponse) {
                    HttpResponse response = (HttpResponse) msg;
                    if (response.status().code() != 401) {
                        if (!response.headers().isEmpty()) {
                            for (String name : response.headers().names()) {
                                // Some cameras use first letter uppercase and others dont.
                                switch (name.toLowerCase()) { // Possible localization issues doing this
                                    case "content-type":
                                        contentType = response.headers().getAsString(name);
                                        break;
                                    case "content-length":
                                        bytesToRecieve = Integer.parseInt(response.headers().getAsString(name));
                                        break;
                                    case "connection":
                                        if (response.headers().getAsString(name).contains("keep-alive")) {
                                            closeConnection = false;
                                        }
                                        break;
                                    case "transfer-encoding":
                                        if (response.headers().getAsString(name).contains("chunked")) {
                                            isChunked = true;
                                        }
                                        break;
                                }
                            }
                            if (contentType.contains("multipart")) {
                                closeConnection = false;
                                if (mjpegUri.contains(requestUrl)) {
                                    if (msg instanceof HttpMessage) {
                                        // very start of stream only
                                        ReferenceCountUtil.retain(msg, 1);
                                        firstStreamedMsg = msg;
                                        streamToGroup(firstStreamedMsg, mjpegChannelGroup, true);
                                    }
                                }
                            } else if (contentType.contains("image/jp")) {
                                if (bytesToRecieve == 0) {
                                    bytesToRecieve = 768000; // 0.768 Mbyte when no Content-Length is sent
                                    logger.debug("Camera has no Content-Length header, we have to guess how much RAM.");
                                }
                                incomingJpeg = new byte[bytesToRecieve];
                            }
                        }
                    }
                }
                if (msg instanceof HttpContent) {
                    if (mjpegUri.contains(requestUrl)) {
                        // multiple MJPEG stream packets come back as this.
                        ReferenceCountUtil.retain(msg, 1);
                        streamToGroup(msg, mjpegChannelGroup, true);
                    } else {
                        HttpContent content = (HttpContent) msg;
                        // Found some cameras uses Content-Type: image/jpg instead of image/jpeg
                        if (contentType.contains("image/jp")) {
                            for (int i = 0; i < content.content().capacity(); i++) {
                                incomingJpeg[bytesAlreadyRecieved++] = content.content().getByte(i);
                            }
                            if (content instanceof LastHttpContent) {
                                processSnapshot(incomingJpeg);
                                // testing next line and if works need to do a full cleanup of this function.
                                closeConnection = true;
                                if (closeConnection) {
                                    ctx.close();
                                } else {
                                    bytesToRecieve = 0;
                                    bytesAlreadyRecieved = 0;
                                }
                            }
                        } else { // incomingMessage that is not an IMAGE
                            if (incomingMessage.isEmpty()) {
                                incomingMessage = content.content().toString(CharsetUtil.UTF_8);
                            } else {
                                incomingMessage += content.content().toString(CharsetUtil.UTF_8);
                            }
                            bytesAlreadyRecieved = incomingMessage.length();
                            if (content instanceof LastHttpContent) {
                                // If it is not an image send it on to the next handler//
                                if (bytesAlreadyRecieved != 0) {
                                    reply = incomingMessage;
                                    super.channelRead(ctx, reply);
                                }
                            }
                            // HIKVISION alertStream never has a LastHttpContent as it always stays open//
                            if (contentType.contains("multipart")) {
                                if (bytesAlreadyRecieved != 0) {
                                    reply = incomingMessage;
                                    incomingMessage = "";
                                    bytesToRecieve = 0;
                                    bytesAlreadyRecieved = 0;
                                    super.channelRead(ctx, reply);
                                }
                            }
                            // Foscam needs this as will other cameras with chunks//
                            if (isChunked && bytesAlreadyRecieved != 0) {
                                reply = incomingMessage;
                                super.channelRead(ctx, reply);
                            }
                        }
                    }
                } else { // msg is not HttpContent
                    // Foscam and Amcrest cameras need this
                    if (!contentType.contains("image/jp") && bytesAlreadyRecieved != 0) {
                        reply = incomingMessage;
                        logger.debug("Packet back from camera is {}", incomingMessage);
                        super.channelRead(ctx, reply);
                    }
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelReadComplete(@Nullable ChannelHandlerContext ctx) {
        }

        @Override
        public void handlerAdded(@Nullable ChannelHandlerContext ctx) {
        }

        @Override
        public void handlerRemoved(@Nullable ChannelHandlerContext ctx) {
        }

        @Override
        public void exceptionCaught(@Nullable ChannelHandlerContext ctx, @Nullable Throwable cause) {
            if (cause == null || ctx == null) {
                return;
            }
            if (cause instanceof ArrayIndexOutOfBoundsException) {
                logger.debug("Camera sent {} bytes when the content-length header was {}.", bytesAlreadyRecieved,
                        bytesToRecieve);
            } else {
                logger.warn("!!!! Camera possibly closed the channel on the binding, cause reported is: {}",
                        cause.getMessage());
            }
            ctx.close();
        }

        @Override
        public void userEventTriggered(@Nullable ChannelHandlerContext ctx, @Nullable Object evt) throws Exception {
            if (ctx == null) {
                return;
            }
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                // If camera does not use the channel for X amount of time it will close.
                if (e.state() == IdleState.READER_IDLE) {
                    String urlToKeepOpen = "";
                    switch (thing.getThingTypeUID().getId()) {
                        case DAHUA_THING:
                            urlToKeepOpen = "/cgi-bin/eventManager.cgi?action=attach&codes=[All]";
                            break;
                        case HIKVISION_THING:
                            urlToKeepOpen = "/ISAPI/Event/notification/alertStream";
                            break;
                        case DOORBIRD_THING:
                            urlToKeepOpen = "/bha-api/monitor.cgi?ring=doorbell,motionsensor";
                            break;
                    }
                    ChannelTracking channelTracking = channelTrackingMap.get(urlToKeepOpen);
                    if (channelTracking != null) {
                        if (channelTracking.getChannel() == ctx.channel()) {
                            return; // don't auto close this as it is for the alarms.
                        }
                    }
                    ctx.close();
                }
            }
        }
    }

    public IpCameraHandler(Thing thing, @Nullable String ipAddress, GroupTracker groupTracker) {
        super(thing);
        cameraConfig = getConfigAs(CameraConfig.class);
        if (ipAddress != null) {
            hostIp = ipAddress;
        } else {
            hostIp = Helper.getLocalIpAddress();
        }
        this.groupTracker = groupTracker;
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
            // due to camera may have been sent multiple requests before the auth was set, this may trigger falsely.
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

    private String getCorrectUrlFormat(String longUrl) {
        String temp = longUrl;
        URL url;

        if (longUrl.isEmpty() || longUrl.equals("ffmpeg")) {
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

    // Always use this as sendHttpGET(GET/POST/PUT/DELETE, "/foo/bar",null)//
    // The authHandler will generate a digest string and re-send using this same function when needed.
    @SuppressWarnings("null")
    public void sendHttpRequest(String httpMethod, String httpRequestURLFull, @Nullable String digestString) {
        int port = getPortFromShortenedUrl(httpRequestURLFull);
        String httpRequestURL = getTinyUrl(httpRequestURLFull);

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
                            socketChannel.pipeline()
                                    .addLast(new HikvisionHandler(getHandle(), cameraConfig.getNvrChannel()));
                            break;
                        case INSTAR_THING:
                            socketChannel.pipeline().addLast(INSTAR_HANDLER, new InstarHandler(getHandle()));
                            break;
                        default:
                            socketChannel.pipeline().addLast(new HttpOnlyHandler(getHandle()));
                            break;
                    }
                }
            });
        }

        FullHttpRequest request;
        if (!"PUT".equals(httpMethod) || (useDigestAuth && digestString == null)) {
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod(httpMethod), httpRequestURL);
            request.headers().set("Host", cameraConfig.getIp() + ":" + port);
            request.headers().set("Connection", HttpHeaderValues.KEEP_ALIVE);
        } else {
            request = putRequestWithBody;
        }

        if (!basicAuth.isEmpty()) {
            if (useDigestAuth) {
                logger.warn("Camera at IP:{} had both Basic and Digest set to be used", cameraConfig.getIp());
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
                            if (!isOnline) {
                                bringCameraOnline();
                            }
                            logger.trace("Sending camera: {}: http://{}:{}{}", httpMethod, cameraConfig.getIp(), port,
                                    httpRequestURL);
                            channelTrackingMap.put(httpRequestURL, new ChannelTracking(ch, httpRequestURL));

                            CommonCameraHandler commonHandler = (CommonCameraHandler) ch.pipeline().get(COMMON_HANDLER);
                            commonHandler.setURL(httpRequestURLFull);
                            MyNettyAuthHandler authHandler = (MyNettyAuthHandler) ch.pipeline().get(AUTH_HANDLER);
                            authHandler.setURL(httpMethod, httpRequestURL);

                            switch (thing.getThingTypeUID().getId()) {
                                case AMCREST_THING:
                                    AmcrestHandler amcrestHandler = (AmcrestHandler) ch.pipeline().get(AMCREST_HANDLER);
                                    amcrestHandler.setURL(httpRequestURL);
                                    break;
                                case INSTAR_THING:
                                    InstarHandler instarHandler = (InstarHandler) ch.pipeline().get(INSTAR_HANDLER);
                                    instarHandler.setURL(httpRequestURL);
                                    break;
                            }
                            ch.writeAndFlush(request);
                        } else { // an error occured
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
        }

        if (streamingSnapshotMjpeg) {
            sendMjpegFrame(incommingSnapshot, snapshotMjpegChannelGroup);
        }
        if (streamingAutoFps) {
            if (motionDetected) {
                sendMjpegFrame(incommingSnapshot, autoSnapshotMjpegChannelGroup);
            } else if (updateAutoFps) {
                // only happens every 8 seconds as some browsers need a frame that often to keep stream alive.
                sendMjpegFrame(incommingSnapshot, autoSnapshotMjpegChannelGroup);
                updateAutoFps = false;
            }
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

    public void stopStreamServer() {
        serversLoopGroup.shutdownGracefully();
        serverBootstrap = null;
    }

    @SuppressWarnings("null")
    public void startStreamServer() {
        if (serverBootstrap == null) {
            try {
                serversLoopGroup = new NioEventLoopGroup();
                serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(serversLoopGroup);
                serverBootstrap.channel(NioServerSocketChannel.class);
                // IP "0.0.0.0" will bind the server to all network connections//
                serverBootstrap.localAddress(new InetSocketAddress("0.0.0.0", cameraConfig.getServerPort()));
                serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast("idleStateHandler", new IdleStateHandler(0, 60, 0));
                        socketChannel.pipeline().addLast("HttpServerCodec", new HttpServerCodec());
                        socketChannel.pipeline().addLast("ChunkedWriteHandler", new ChunkedWriteHandler());
                        socketChannel.pipeline().addLast("streamServerHandler", new StreamServerHandler(getHandle()));
                    }
                });
                serverFuture = serverBootstrap.bind().sync();
                serverFuture.await(4000);
                logger.debug("File server for camera at {} has started on port {} for all NIC's.", cameraConfig.getIp(),
                        cameraConfig.getServerPort());
                updateState(CHANNEL_MJPEG_URL,
                        new StringType("http://" + hostIp + ":" + cameraConfig.getServerPort() + "/ipcamera.mjpeg"));
                updateState(CHANNEL_HLS_URL,
                        new StringType("http://" + hostIp + ":" + cameraConfig.getServerPort() + "/ipcamera.m3u8"));
                updateState(CHANNEL_IMAGE_URL,
                        new StringType("http://" + hostIp + ":" + cameraConfig.getServerPort() + "/ipcamera.jpg"));
            } catch (Exception e) {
                cameraConfigError("Exception when starting server. Try changing the Server Port to another number.");
            }
        }
    }

    public void setupSnapshotStreaming(boolean stream, ChannelHandlerContext ctx, boolean auto) {
        if (stream) {
            sendMjpegFirstPacket(ctx);
            if (auto) {
                autoSnapshotMjpegChannelGroup.add(ctx.channel());
                lockCurrentSnapshot.lock();
                try {
                    sendMjpegFrame(currentSnapshot, autoSnapshotMjpegChannelGroup);
                    // iOS uses a FIFO? and needs two frames to display a pic
                    sendMjpegFrame(currentSnapshot, autoSnapshotMjpegChannelGroup);
                } finally {
                    lockCurrentSnapshot.unlock();
                }
                streamingAutoFps = true;
            } else {
                snapshotMjpegChannelGroup.add(ctx.channel());
                lockCurrentSnapshot.lock();
                try {
                    sendMjpegFrame(currentSnapshot, snapshotMjpegChannelGroup);
                } finally {
                    lockCurrentSnapshot.unlock();
                }
                streamingSnapshotMjpeg = true;
                startSnapshotPolling();
            }
        } else {
            snapshotMjpegChannelGroup.remove(ctx.channel());
            autoSnapshotMjpegChannelGroup.remove(ctx.channel());
            if (streamingSnapshotMjpeg && snapshotMjpegChannelGroup.isEmpty()) {
                streamingSnapshotMjpeg = false;
                stopSnapshotPolling();
                logger.debug("All snapshots.mjpeg streams have stopped.");
            } else if (streamingAutoFps && autoSnapshotMjpegChannelGroup.isEmpty()) {
                streamingAutoFps = false;
                stopSnapshotPolling();
                logger.debug("All autofps.mjpeg streams have stopped.");
            }
        }
    }

    // If start is true the CTX is added to the list to stream video to, false stops
    // the stream.
    public void setupMjpegStreaming(boolean start, ChannelHandlerContext ctx) {
        if (start) {
            if (mjpegChannelGroup.isEmpty()) {// first stream being requested.
                mjpegChannelGroup.add(ctx.channel());
                if (mjpegUri.isEmpty() || mjpegUri.equals("ffmpeg")) {
                    sendMjpegFirstPacket(ctx);
                    setupFfmpegFormat(FFmpegFormat.MJPEG);
                } else {
                    try {
                        // fix Dahua reboots when refreshing a mjpeg stream.
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    sendHttpGET(mjpegUri);
                }
            } else if (ffmpegMjpeg != null) {// not first stream and we will use ffmpeg
                sendMjpegFirstPacket(ctx);
                mjpegChannelGroup.add(ctx.channel());
            } else {// not first stream and camera supplies the mjpeg source.
                ctx.channel().writeAndFlush(firstStreamedMsg);
                mjpegChannelGroup.add(ctx.channel());
            }
        } else {
            mjpegChannelGroup.remove(ctx.channel());
            if (mjpegChannelGroup.isEmpty()) {
                logger.debug("All ipcamera.mjpeg streams have stopped.");
                if (mjpegUri.equals("ffmpeg")) {
                    if (ffmpegMjpeg != null) {
                        ffmpegMjpeg.stopConverting();
                    }
                } else if (!mjpegUri.isEmpty()) {
                    closeChannel(getTinyUrl(mjpegUri));
                } else {
                    if (ffmpegMjpeg != null) {
                        ffmpegMjpeg.stopConverting();
                    }
                }
            }
        }
    }

    void closeChannel(String url) {
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
    void cleanChannels() {
        for (Channel channel : openChannels) {
            boolean oldChannel = true;
            for (ChannelTracking channelTracking : channelTrackingMap.values()) {
                if (!channelTracking.getChannel().isOpen() && channelTracking.getReply().isEmpty()) {
                    channelTrackingMap.remove(channelTracking.getRequestUrl());
                }
                if (channelTracking.getChannel() == channel) {
                    logger.trace("Open channel to camera is used for URL:{}", channelTracking.getRequestUrl());
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

    // sends direct to ctx so can be either snapshots.mjpeg or normal mjpeg stream
    public void sendMjpegFirstPacket(ChannelHandlerContext ctx) {
        final String boundary = "thisMjpegStream";
        String contentType = "multipart/x-mixed-replace; boundary=" + boundary;
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_CACHE);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        response.headers().add("Access-Control-Allow-Origin", "*");
        response.headers().add("Access-Control-Expose-Headers", "*");
        ctx.channel().writeAndFlush(response);
    }

    public void sendMjpegFrame(byte[] jpg, ChannelGroup channelGroup) {
        final String boundary = "thisMjpegStream";
        ByteBuf imageByteBuf = Unpooled.copiedBuffer(jpg);
        int length = imageByteBuf.readableBytes();
        String header = "--" + boundary + "\r\n" + "content-type: image/jpeg" + "\r\n" + "content-length: " + length
                + "\r\n\r\n";
        ByteBuf headerBbuf = Unpooled.copiedBuffer(header, 0, header.length(), StandardCharsets.UTF_8);
        ByteBuf footerBbuf = Unpooled.copiedBuffer("\r\n", 0, 2, StandardCharsets.UTF_8);
        streamToGroup(headerBbuf, channelGroup, false);
        streamToGroup(imageByteBuf, channelGroup, false);
        streamToGroup(footerBbuf, channelGroup, true);
    }

    public void streamToGroup(Object msg, ChannelGroup channelGroup, boolean flush) {
        channelGroup.write(msg);
        if (flush) {
            channelGroup.flush();
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
        if (rtspUri.toLowerCase().contains("rtsp")) {
            if (inputOptions.isEmpty()) {
                inputOptions = "-rtsp_transport tcp";
            } else {
                inputOptions = inputOptions + " -rtsp_transport tcp";
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
                if (ffmpegHLS != null) {
                    ffmpegHLS.startConverting();
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
                if (ffmpegGIF != null) {
                    ffmpegGIF.startConverting();
                    if (gifHistory.isEmpty()) {
                        gifHistory = gifFilename;
                    } else if (!gifFilename.equals("ipcamera")) {
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
                } else if (!mp4Filename.equals("ipcamera")) {
                    mp4History = mp4Filename + "," + mp4History;
                    if (mp4HistoryLength > 49) {
                        int endIndex = mp4History.lastIndexOf(",");
                        mp4History = mp4History.substring(0, endIndex);
                    }
                }
                setChannelState(CHANNEL_MP4_HISTORY, new StringType(mp4History));
                break;
            case RTSP_ALARMS:
                if (ffmpegRtspHelper != null) {
                    ffmpegRtspHelper.stopConverting();
                    if (!audioAlarmEnabled && !motionAlarmEnabled) {
                        return;
                    }
                }
                String input = (cameraConfig.getAlarmInputUrl().isEmpty()) ? rtspUri : cameraConfig.getAlarmInputUrl();
                String outputOptions = "-f null -";
                String filterOptions = "";
                if (!audioAlarmEnabled) {
                    filterOptions = "-an";
                } else {
                    filterOptions = "-af silencedetect=n=-" + audioThreshold + "dB:d=2";
                }
                if (!motionAlarmEnabled && !ffmpegSnapshotGeneration) {
                    filterOptions = filterOptions.concat(" -vn");
                } else if (motionAlarmEnabled) {
                    filterOptions = filterOptions
                            .concat(" -vf select='gte(scene," + motionThreshold + ")',metadata=print");
                }
                if (!cameraConfig.getUser().isEmpty()) {
                    filterOptions += " ";// add space as the Framework does not allow spaces at start of config.
                }
                ffmpegRtspHelper = new Ffmpeg(this, format, cameraConfig.getFfmpegLocation(), inputOptions, input,
                        filterOptions + cameraConfig.getMotionOptions(), outputOptions, cameraConfig.getUser(),
                        cameraConfig.getPassword());
                ffmpegRtspHelper.startConverting();
                break;
            case MJPEG:
                if (ffmpegMjpeg == null) {
                    if (inputOptions.isEmpty()) {
                        inputOptions = "-hide_banner -loglevel warning";
                    } else {
                        inputOptions = inputOptions + " -hide_banner -loglevel warning";
                    }
                    ffmpegMjpeg = new Ffmpeg(this, format, cameraConfig.getFfmpegLocation(), inputOptions, rtspUri,
                            cameraConfig.getMjpegOptions(),
                            "http://127.0.0.1:" + cameraConfig.getServerPort() + "/ipcamera.jpg",
                            cameraConfig.getUser(), cameraConfig.getPassword());
                }
                if (ffmpegMjpeg != null) {
                    ffmpegMjpeg.startConverting();
                }
                break;
            case SNAPSHOT:
                // if mjpeg stream you can use ffmpeg -i input.h264 -codec:v copy -bsf:v mjpeg2jpeg output%03d.jpg
                if (ffmpegSnapshot == null) {
                    if (inputOptions.isEmpty()) {
                        // iFrames only
                        inputOptions = "-threads 1 -skip_frame nokey -hide_banner -loglevel warning";
                    } else {
                        inputOptions = inputOptions + " -threads 1 -skip_frame nokey -hide_banner -loglevel warning";
                    }
                    ffmpegSnapshot = new Ffmpeg(this, format, cameraConfig.getFfmpegLocation(), inputOptions, rtspUri,
                            "-an -vsync vfr -update 1",
                            "http://127.0.0.1:" + cameraConfig.getServerPort() + "/snapshot.jpg",
                            cameraConfig.getUser(), cameraConfig.getPassword());
                }
                if (ffmpegSnapshot != null) {
                    ffmpegSnapshot.startConverting();
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
                    sendHttpGET(snapshotUri);
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
                    sendHttpGET(snapshotUri);
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
                        motionAlarmEnabled = true;
                    } else if (OnOffType.OFF.equals(command) || DecimalType.ZERO.equals(command)) {
                        motionAlarmEnabled = false;
                        noMotionDetected(CHANNEL_MOTION_ALARM);
                    } else {
                        motionAlarmEnabled = true;
                        motionThreshold = Double.valueOf(command.toString());
                        motionThreshold = motionThreshold / 10000;
                    }
                    setupFfmpegFormat(FFmpegFormat.RTSP_ALARMS);
                    return;
                case CHANNEL_START_STREAM:
                    if (OnOffType.ON.equals(command)) {
                        setupFfmpegFormat(FFmpegFormat.HLS);
                        if (ffmpegHLS != null) {
                            ffmpegHLS.setKeepAlive(-1);// will keep running till manually stopped.
                        }
                    } else {
                        if (ffmpegHLS != null) {
                            ffmpegHLS.setKeepAlive(1);
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
                case CHANNEL_POLL_IMAGE:
                    if (OnOffType.ON.equals(command)) {
                        if (snapshotUri.isEmpty()) {
                            ffmpegSnapshotGeneration = true;
                            setupFfmpegFormat(FFmpegFormat.SNAPSHOT);
                            updateImageChannel = false;
                        } else {
                            updateImageChannel = true;
                            sendHttpGET(snapshotUri);// Allows this to change Image FPS on demand
                        }
                    } else {
                        if (ffmpegSnapshot != null) {
                            ffmpegSnapshot.stopConverting();
                            ffmpegSnapshotGeneration = false;
                        }
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
                        threadPool.schedule(this::sendPTZRequest, 500, TimeUnit.MILLISECONDS);
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
                        threadPool.schedule(this::sendPTZRequest, 500, TimeUnit.MILLISECONDS);
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
                        threadPool.schedule(this::sendPTZRequest, 500, TimeUnit.MILLISECONDS);
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
                if (lowPriorityRequests.isEmpty()) {
                    lowPriorityRequests = hikvisionHandler.getLowPriorityRequests();
                }
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

    void bringCameraOnline() {
        isOnline = true;
        updateStatus(ThingStatus.ONLINE);
        groupTracker.listOfOnlineCameraHandlers.add(this);
        groupTracker.listOfOnlineCameraUID.add(getThing().getUID().getId());
        if (cameraConnectionJob != null) {
            cameraConnectionJob.cancel(false);
        }

        if (cameraConfig.getGifPreroll() > 0 || cameraConfig.getUpdateImageWhen().contains("1")) {
            snapshotPolling = true;
            snapshotJob = threadPool.scheduleAtFixedRate(this::snapshotRunnable, 1000, cameraConfig.getPollTime(),
                    TimeUnit.MILLISECONDS);
        }

        pollCameraJob = threadPool.scheduleWithFixedDelay(this::pollCameraRunnable, 1000, 8000, TimeUnit.MILLISECONDS);

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
    }

    void snapshotIsFfmpeg() {
        bringCameraOnline();
        snapshotUri = "";// ffmpeg is a valid option. Simplify further checks.
        logger.debug(
                "Binding has no snapshot url. Will use your CPU and FFmpeg to create snapshots from the cameras RTSP.");
        if (!rtspUri.isEmpty()) {
            updateImageChannel = false;
            ffmpegSnapshotGeneration = true;
            setupFfmpegFormat(FFmpegFormat.SNAPSHOT);
            updateState(CHANNEL_POLL_IMAGE, OnOffType.ON);
        } else {
            cameraConfigError("Binding can not find a RTSP url for this camera, please provide a FFmpeg Input URL.");
        }
    }

    void pollingCameraConnection() {
        if (thing.getThingTypeUID().getId().equals(GENERIC_THING)) {
            if (rtspUri.isEmpty()) {
                logger.warn("Binding has not been supplied with a FFmpeg Input URL, so some features will not work.");
            }
            if (snapshotUri.isEmpty() || snapshotUri.equals("ffmpeg")) {
                snapshotIsFfmpeg();
            } else {
                sendHttpRequest("GET", snapshotUri, null);
            }
            return;
        }
        if (!onvifCamera.isConnected()) {
            logger.debug("About to connect to the IP Camera using the ONVIF PORT at IP:{}:{}", cameraConfig.getIp(),
                    cameraConfig.getOnvifPort());
            onvifCamera.connect(thing.getThingTypeUID().getId().equals(ONVIF_THING));
        }
        if (snapshotUri.equals("ffmpeg")) {
            snapshotIsFfmpeg();
        } else if (!snapshotUri.isEmpty()) {
            sendHttpRequest("GET", snapshotUri, null);
        } else if (!rtspUri.isEmpty()) {
            snapshotIsFfmpeg();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Camera failed to report a valid Snaphot and/or RTSP URL. See readme on how to use the SNAPSHOT_URL_OVERRIDE feature.");
        }
    }

    public void cameraConfigError(String reason) {
        // wont try to reconnect again due to a config error being the cause.
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, reason);
        dispose();
    }

    public void cameraCommunicationError(String reason) {
        // will try to reconnect again as camera may be rebooting.
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        if (isOnline) {// if already offline dont try reconnecting in 6 seconds, we want 30sec wait.
            resetAndRetryConnecting();
        }
    }

    boolean streamIsStopped(String url) {
        ChannelTracking channelTracking = channelTrackingMap.get(url);
        if (channelTracking != null) {
            if (channelTracking.getChannel().isOpen()) {
                return false; // stream is running.
            }
        }
        return true; // Stream stopped or never started.
    }

    void snapshotRunnable() {
        // Snapshot should be first to keep consistent time between shots
        sendHttpGET(snapshotUri);
        if (snapCount > 0) {
            if (--snapCount == 0) {
                setupFfmpegFormat(FFmpegFormat.GIF);
            }
        }
    }

    public void stopSnapshotPolling() {
        if (!streamingSnapshotMjpeg && cameraConfig.getGifPreroll() == 0
                && !cameraConfig.getUpdateImageWhen().contains("1")) {
            snapshotPolling = false;
            if (snapshotJob != null) {
                snapshotJob.cancel(true);
            }
        } else if (cameraConfig.getUpdateImageWhen().contains("4")) { // only during Motion Alarms
            snapshotPolling = false;
            if (snapshotJob != null) {
                snapshotJob.cancel(true);
            }
        }
    }

    public void startSnapshotPolling() {
        if (snapshotPolling || ffmpegSnapshotGeneration) {
            return; // Already polling or creating with FFmpeg from RTSP
        }
        if (streamingSnapshotMjpeg || streamingAutoFps) {
            snapshotPolling = true;
            snapshotJob = threadPool.scheduleAtFixedRate(this::snapshotRunnable, 200, cameraConfig.getPollTime(),
                    TimeUnit.MILLISECONDS);
        } else if (cameraConfig.getUpdateImageWhen().contains("4")) { // During Motion Alarms
            snapshotPolling = true;
            snapshotJob = threadPool.scheduleAtFixedRate(this::snapshotRunnable, 200, cameraConfig.getPollTime(),
                    TimeUnit.MILLISECONDS);
        }
    }

    // runs every 8 seconds due to mjpeg streams not staying open unless they update this often.
    void pollCameraRunnable() {
        // Snapshot should be first to keep consistent time between shots
        if (!snapshotUri.isEmpty()) {
            if (updateImageChannel) {
                sendHttpGET(snapshotUri);
            }
        }
        if (streamingAutoFps) {
            updateAutoFps = true;
            if (!snapshotPolling && !ffmpegSnapshotGeneration) {
                // Dont need to poll if creating from RTSP stream with FFmpeg or we are polling at full rate already.
                sendHttpGET(snapshotUri);
            }
        }
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
                break;
            case ONVIF_THING:
                if (!onvifCamera.isConnected()) {
                    onvifCamera.connect(true);
                }
                break;
            case INSTAR_THING:
                noMotionDetected(CHANNEL_MOTION_ALARM);
                noMotionDetected(CHANNEL_PIR_ALARM);
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
        }
        if (ffmpegHLS != null) {
            ffmpegHLS.checkKeepAlive();
        }
        if (openChannels.size() > 18) {
            logger.debug("There are {} open Channels being tracked.", openChannels.size());
            cleanChannels();
        }
    }

    @Override
    public void initialize() {
        cameraConfig = getConfigAs(CameraConfig.class);
        snapshotUri = getCorrectUrlFormat(cameraConfig.getSnapshotUrl());
        mjpegUri = getCorrectUrlFormat(cameraConfig.getMjpegUrl());
        rtspUri = cameraConfig.getFfmpegInput();

        if (cameraConfig.getServerPort() < 1) {
            logger.warn(
                    "The Server Port is not set to a valid number which disables a lot of binding features. See readme for more info.");
        } else if (cameraConfig.getServerPort() < 1025) {
            logger.warn("The Server Port is <= 1024 and may cause permission errors under Linux, try a higher number.");
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
                break;
            case INSTAR_THING:
                if (snapshotUri.isEmpty()) {
                    snapshotUri = "/tmpfs/snap.jpg";
                }
                if (mjpegUri.isEmpty()) {
                    mjpegUri = "/mjpegstream.cgi?-chn=12";
                }
                break;
        }

        // Onvif and Instar event handling needs the host IP and the server started.
        if (cameraConfig.getServerPort() > 0) {
            startStreamServer();
        }

        if (!thing.getThingTypeUID().getId().equals(GENERIC_THING)) {
            onvifCamera = new OnvifConnection(this, cameraConfig.getIp() + ":" + cameraConfig.getOnvifPort(),
                    cameraConfig.getUser(), cameraConfig.getPassword());
            onvifCamera.setSelectedMediaProfile(cameraConfig.getOnvifMediaProfile());
            // Only use ONVIF events if it is not an API camera.
            onvifCamera.connect(thing.getThingTypeUID().getId().equals(ONVIF_THING));
        }

        // for poll times above 9 seconds don't display a warning about the Image channel.
        if (9000 <= cameraConfig.getPollTime() && cameraConfig.getUpdateImageWhen().contains("1")) {
            logger.warn(
                    "The Image channel is set to update more often than 8 seconds. This is not recommended. The Image channel is best used only for higher poll times. See the readme file on how to display the cameras picture for best results or use a higher poll time.");
        }
        // Waiting 3 seconds for ONVIF to discover the urls before running.
        cameraConnectionJob = threadPool.scheduleWithFixedDelay(this::pollingCameraConnection, 4, 30, TimeUnit.SECONDS);
    }

    // What the camera needs to re-connect if the initialize() is not called.
    private void resetAndRetryConnecting() {
        dispose();
        initialize();
    }

    @Override
    public void dispose() {
        isOnline = false;
        snapshotPolling = false;
        onvifCamera.disconnect();
        if (pollCameraJob != null) {
            pollCameraJob.cancel(true);
            pollCameraJob = null;
        }
        if (snapshotJob != null) {
            snapshotJob.cancel(true);
            snapshotJob = null;
        }
        if (cameraConnectionJob != null) {
            cameraConnectionJob.cancel(true);
            cameraConnectionJob = null;
        }
        threadPool.shutdown();
        threadPool = Executors.newScheduledThreadPool(4);

        groupTracker.listOfOnlineCameraHandlers.remove(this);
        groupTracker.listOfOnlineCameraUID.remove(getThing().getUID().getId());
        // inform all group handlers that this camera has gone offline
        for (IpCameraGroupHandler handle : groupTracker.listOfGroupHandlers) {
            handle.cameraOffline(this);
        }
        basicAuth = ""; // clear out stored Password hash
        useDigestAuth = false;
        stopStreamServer();
        openChannels.close();

        if (ffmpegHLS != null) {
            ffmpegHLS.stopConverting();
            ffmpegHLS = null;
        }
        if (ffmpegRecord != null) {
            ffmpegRecord.stopConverting();
            ffmpegRecord = null;
        }
        if (ffmpegGIF != null) {
            ffmpegGIF.stopConverting();
            ffmpegGIF = null;
        }
        if (ffmpegRtspHelper != null) {
            ffmpegRtspHelper.stopConverting();
            ffmpegRtspHelper = null;
        }
        if (ffmpegMjpeg != null) {
            ffmpegMjpeg.stopConverting();
            ffmpegMjpeg = null;
        }
        if (ffmpegSnapshot != null) {
            ffmpegSnapshot.stopConverting();
            ffmpegSnapshot = null;
        }
        channelTrackingMap.clear();
    }

    public void setStreamServerHandler(StreamServerHandler streamServerHandler2) {
        streamServerHandler = streamServerHandler2;
    }

    public String getWhiteList() {
        return cameraConfig.getIpWhitelist();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(IpCameraActions.class);
    }
}
