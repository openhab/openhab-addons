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

package org.openhab.binding.ipcamera.handler;

import static org.openhab.binding.ipcamera.IpCameraBindingConstants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.ipcamera.internal.AmcrestHandler;
import org.openhab.binding.ipcamera.internal.DahuaHandler;
import org.openhab.binding.ipcamera.internal.DoorBirdHandler;
import org.openhab.binding.ipcamera.internal.Ffmpeg;
import org.openhab.binding.ipcamera.internal.FoscamHandler;
import org.openhab.binding.ipcamera.internal.HikvisionHandler;
import org.openhab.binding.ipcamera.internal.HttpOnlyHandler;
import org.openhab.binding.ipcamera.internal.InstarHandler;
import org.openhab.binding.ipcamera.internal.MyNettyAuthHandler;
import org.openhab.binding.ipcamera.internal.StreamServerHandler;
import org.openhab.binding.ipcamera.onvif.OnvifConnection;
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
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_ONVIF, THING_TYPE_HTTPONLY, THING_TYPE_AMCREST, THING_TYPE_DAHUA,
                    THING_TYPE_INSTAR, THING_TYPE_FOSCAM, THING_TYPE_DOORBIRD, THING_TYPE_HIKVISION));
    public static ArrayList<IpCameraHandler> listOfOnlineCameraHandlers = new ArrayList<IpCameraHandler>(1);
    public static ArrayList<IpCameraGroupHandler> listOfGroupHandlers = new ArrayList<IpCameraGroupHandler>(0);
    public static ArrayList<String> listOfOnlineCameraUID = new ArrayList<String>(1);
    public final Logger logger = LoggerFactory.getLogger(getClass());
    private ScheduledExecutorService cameraConnection = Executors.newScheduledThreadPool(1);
    private ScheduledExecutorService scheduledMovePTZ = Executors.newScheduledThreadPool(1);
    private ScheduledExecutorService pollCamera = Executors.newScheduledThreadPool(1);
    private ScheduledExecutorService snapshot = Executors.newScheduledThreadPool(1);
    public Configuration config;

    // ChannelGroup is thread safe
    public final ChannelGroup mjpegChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    final ChannelGroup snapshotMjpegChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    final ChannelGroup autoSnapshotMjpegChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public @Nullable Ffmpeg ffmpegHLS = null;
    public @Nullable Ffmpeg ffmpegRecord = null;
    public @Nullable Ffmpeg ffmpegGIF = null;
    public @Nullable Ffmpeg ffmpegRtspHelper = null;
    public @Nullable Ffmpeg ffmpegMjpeg = null;
    public @Nullable Ffmpeg ffmpegSnapshot = null;
    public boolean streamingAutoFps = false;
    boolean motionDetected = false;

    private @Nullable ScheduledFuture<?> cameraConnectionJob = null;
    private @Nullable ScheduledFuture<?> pollCameraJob = null;
    private @Nullable ScheduledFuture<?> snapshotJob = null;
    private @Nullable Bootstrap mainBootstrap;
    private @Nullable ServerBootstrap serverBootstrap;
    private String username = "";
    private String password = "";
    private int selectedMediaProfile = 0;

    private EventLoopGroup mainEventLoopGroup = new NioEventLoopGroup();
    private EventLoopGroup serversLoopGroup = new NioEventLoopGroup();
    private FullHttpRequest putRequestWithBody = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod("PUT"),
            "");
    private String nvrChannel = "";
    private String gifFilename = "ipcamera";
    private String gifHistory = "";
    private String mp4History = "";
    public int gifHistoryLength = 0;
    public int mp4HistoryLength = 0;
    private String mp4Filename = "ipcamera";
    int mp4RecordTime = 0;
    int mp4Preroll = 0;
    private LinkedList<byte[]> fifoSnapshotBuffer = new LinkedList<byte[]>();
    private int preroll, postroll, snapCount = 0;
    private boolean updateImageChannel = false;
    private boolean updateAutoFps = false;
    private byte lowPriorityCounter = 0;
    public String hostIp = "0.0.0.0";
    private String ffmpegOutputFolder = "";

    public ArrayList<String> listOfRequests = new ArrayList<String>(18);
    public ArrayList<Channel> listOfChannels = new ArrayList<Channel>(18);
    // Status can be -2=storing a reply, -1=closed, 0=closing (do not re-use
    // channel), 1=open, 2=open and ok to reuse
    public ArrayList<Byte> listOfChStatus = new ArrayList<Byte>(18);
    public ArrayList<String> listOfReplies = new ArrayList<String>(18);
    public ArrayList<String> lowPriorityRequests = new ArrayList<String>(0);
    public ReentrantLock lock = new ReentrantLock();

    // basicAuth MUST remain private as it holds the password
    private String basicAuth = "";
    public boolean useDigestAuth = false;
    public String snapshotUri = "";
    public String mjpegUri = "";
    private @Nullable ChannelFuture serverFuture = null;
    public int serverPort = 0;
    private Object firstStreamedMsg = new Object();
    public byte[] currentSnapshot = new byte[] { (byte) 0x00 };
    public ReentrantLock lockCurrentSnapshot = new ReentrantLock();
    public String rtspUri = "";
    public String ipAddress = "empty";
    public String updateImageEvents = "";
    public boolean audioAlarmUpdateSnapshot = false;
    boolean motionAlarmUpdateSnapshot = false;
    boolean isOnline = false; // Used so only 1 error is logged when a network issue occurs.
    boolean firstAudioAlarm = false;
    boolean firstMotionAlarm = false;
    public Double motionThreshold = 0.0016;
    public int audioThreshold = 35;
    @SuppressWarnings("unused")
    private @Nullable StreamServerHandler streamServerHandler;
    boolean streamingSnapshotMjpeg = false;
    public boolean motionAlarmEnabled = false;
    public boolean audioAlarmEnabled = false;
    public boolean ffmpegSnapshotGeneration = false;
    public boolean snapshotPolling = false;
    public OnvifConnection onvifCamera = new OnvifConnection(this, "", "", "");

    public IpCameraHandler(Thing thing) {
        super(thing);
        config = thing.getConfiguration();
    }

    private IpCameraHandler getHandle() {
        return this;
    }

    // false clears the stored user/pass hash, true creates the hash
    public boolean setBasicAuth(boolean useBasic) {
        if (useBasic == false) {
            logger.debug("Clearing out the stored BASIC auth now.");
            basicAuth = "";
            return false;
        } else if (!basicAuth.equals("")) {
            // due to camera may have been sent multiple requests before the auth was set, this may trigger falsely.
            logger.warn("Camera is reporting your username and/or password is wrong.");
            return false;
        }
        if (!username.equals("") && !password.equals("")) {
            String authString = username + ":" + password;
            ByteBuf byteBuf = null;
            try {
                byteBuf = Base64.encode(Unpooled.wrappedBuffer(authString.getBytes(CharsetUtil.UTF_8)));
                basicAuth = byteBuf.getCharSequence(0, byteBuf.capacity(), CharsetUtil.UTF_8).toString();
            } finally {
                if (byteBuf != null) {
                    byteBuf.release();
                    byteBuf = null;
                }
            }
            return true;
        } else {
            cameraConfigError(
                    "Camera is asking for Basic Auth when you have not provided a username and/or password !");
        }
        return false;
    }

    private String getCorrectUrlFormat(String longUrl) {
        String temp = longUrl;
        URL url;

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
            if (!longUrl.equals("ffmpeg")) {
                cameraConfigError("A non valid URL has been given to the binding, check they work in a browser.");
                return "";
            }
        }
        return temp;
    }

    private void cleanChannels() {
        lock.lock();
        for (byte index = 0; index < listOfRequests.size(); index++) {
            logger.debug("Channel status is {} for URL:{}", listOfChStatus.get(index), listOfRequests.get(index));
            switch (listOfChStatus.get(index)) {
                case 2: // Open and OK to reuse
                case 1: // Open
                case 0: // Closing but still open
                    Channel channel = listOfChannels.get(index);
                    if (channel.isOpen()) {
                        break;
                    } else {
                        listOfChStatus.set(index, (byte) -1);
                        logger.warn("Cleaning the channels has just found a connection with wrong open state.");
                    }
                case -1: // closed
                    listOfRequests.remove(index);
                    listOfChStatus.remove(index);
                    listOfChannels.remove(index);
                    listOfReplies.remove(index);
                    index--;
                    break;
            }
        }
        lock.unlock();
    }

    private void closeChannel(String url) {
        lock.lock();
        try {
            for (byte index = 0; index < listOfRequests.size(); index++) {
                if (listOfRequests.get(index).equals(url)) {
                    switch (listOfChStatus.get(index)) {
                        case 2: // Still open and OK to reuse
                        case 1: // Still open
                        case 0: // Marked as closing but channel still needs to be closed.
                            Channel chan = listOfChannels.get(index);
                            chan.close();// We can't wait as OH kills any handler that takes >5 seconds.
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void closeAllChannels() {
        lock.lock();
        try {
            for (byte index = 0; index < listOfRequests.size(); index++) {
                logger.debug("Channel status is {} for URL:{}", listOfChStatus.get(index), listOfRequests.get(index));
                switch (listOfChStatus.get(index)) {
                    case 2: // Still open and ok to reuse
                    case 1: // Still open
                    case 0: // Marked as closing but channel still needs to be closed.
                        Channel chan = listOfChannels.get(index);
                        chan.close();
                        // Handlers may get shutdown by Openhab if total delay >5 secs so no wait.
                        break;
                }
            }
        } finally {
            lock.unlock();
        }
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
        return Integer.parseInt(config.get(CONFIG_PORT).toString());
    }

    public String getTinyUrl(String httpRequestURL) {
        if (httpRequestURL.startsWith(":")) {
            int beginIndex = httpRequestURL.indexOf("/");
            return httpRequestURL.substring(beginIndex);
        }
        return httpRequestURL;
    }

    // Always use this as sendHttpGET(GET/POST/PUT/DELETE, "/foo/bar",null,false)//
    // The authHandler will use the url inside a digest string as needed.
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
                    socketChannel.pipeline().addLast("idleStateHandler", new IdleStateHandler(18, 0, 0));
                    socketChannel.pipeline().addLast("HttpClientCodec", new HttpClientCodec());
                    socketChannel.pipeline().addLast("authHandler",
                            new MyNettyAuthHandler(username, password, getHandle()));
                    socketChannel.pipeline().addLast("commonHandler", new CommonCameraHandler());

                    switch (thing.getThingTypeUID().getId()) {
                        case "AMCREST":
                            socketChannel.pipeline().addLast("amcrestHandler", new AmcrestHandler(getHandle()));
                            break;
                        case "DAHUA":
                            socketChannel.pipeline().addLast("brandHandler", new DahuaHandler(getHandle(), nvrChannel));
                            break;
                        case "DOORBIRD":
                            socketChannel.pipeline().addLast("brandHandler", new DoorBirdHandler(getHandle()));
                            break;
                        case "FOSCAM":
                            socketChannel.pipeline().addLast("brandHandler",
                                    new FoscamHandler(getHandle(), username, password));
                            break;
                        case "HIKVISION":
                            socketChannel.pipeline().addLast("brandHandler",
                                    new HikvisionHandler(getHandle(), nvrChannel));
                            break;
                        case "INSTAR":
                            socketChannel.pipeline().addLast("instarHandler", new InstarHandler(getHandle()));
                            break;
                        default:
                            socketChannel.pipeline().addLast("brandHandler", new HttpOnlyHandler(getHandle()));
                            break;
                    }
                }
            });
        }

        FullHttpRequest request;
        if ("PUT".equals(httpMethod)) {
            if (useDigestAuth && digestString == null) {
                request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod(httpMethod), httpRequestURL);
                request.headers().set(HttpHeaderNames.HOST, ipAddress);
                request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            } else {
                request = putRequestWithBody;
            }
        } else {
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod(httpMethod), httpRequestURL);
            request.headers().set(HttpHeaderNames.HOST, ipAddress);
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        if (!basicAuth.equals("")) {
            if (useDigestAuth) {
                logger.warn("Camera at IP:{} had both Basic and Digest set to be used", ipAddress);
                setBasicAuth(false);
            } else {
                request.headers().set(HttpHeaderNames.AUTHORIZATION, "Basic " + basicAuth);
            }
        }

        if (useDigestAuth) {
            if (digestString != null) {
                request.headers().set(HttpHeaderNames.AUTHORIZATION, "Digest " + digestString);
            }
        }

        mainBootstrap.connect(new InetSocketAddress(ipAddress, port)).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(@Nullable ChannelFuture future) {
                if (future == null) {
                    return;
                }
                if (future.isDone() && future.isSuccess()) {
                    logger.trace("Sending camera: {}: http://{}{}", httpMethod, ipAddress, httpRequestURL);
                    lock.lock();
                    byte indexInLists = -1;
                    try {
                        for (byte index = 0; index < listOfRequests.size(); index++) {
                            boolean done = false;
                            if (listOfRequests.get(index).equals(httpRequestURL)) {
                                switch (listOfChStatus.get(index)) {
                                    case 2: // Open and ok to reuse
                                        Channel ch = listOfChannels.get(index);
                                        if (ch.isOpen()) {
                                            logger.debug("Using the already open channel:{} \t{}:{}", index, httpMethod,
                                                    httpRequestURL);
                                            CommonCameraHandler commonHandler = (CommonCameraHandler) ch.pipeline()
                                                    .get("commonHandler");
                                            commonHandler.setURL(httpRequestURLFull);
                                            MyNettyAuthHandler authHandler = (MyNettyAuthHandler) ch.pipeline()
                                                    .get("authHandler");
                                            authHandler.setURL(httpMethod, httpRequestURL);
                                            ch.writeAndFlush(request);
                                            return;
                                        }
                                    case -1: // Closed
                                        indexInLists = index;
                                        listOfChStatus.set(indexInLists, (byte) 1);
                                        done = true;
                                        break;
                                }
                                if (done) {
                                    break;
                                }
                            }
                        }
                    } finally {
                        lock.unlock();
                    }

                    Channel ch = future.channel();
                    CommonCameraHandler commonHandler = (CommonCameraHandler) ch.pipeline().get("commonHandler");
                    MyNettyAuthHandler authHandler = (MyNettyAuthHandler) ch.pipeline().get("authHandler");
                    commonHandler.setURL(httpRequestURL);
                    authHandler.setURL(httpMethod, httpRequestURL);

                    switch (thing.getThingTypeUID().getId()) {
                        case "AMCREST":
                            AmcrestHandler amcrestHandler = (AmcrestHandler) ch.pipeline().get("amcrestHandler");
                            amcrestHandler.setURL(httpRequestURL);
                            break;
                        case "INSTAR":
                            InstarHandler instarHandler = (InstarHandler) ch.pipeline().get("instarHandler");
                            instarHandler.setURL(httpRequestURL);
                            break;
                    }

                    if (indexInLists >= 0) {
                        lock.lock();
                        try {
                            listOfChannels.set(indexInLists, ch);
                        } finally {
                            lock.unlock();
                        }
                        // logger.debug("Have re-opened the closed channel:{} \t{}:{}", indexInLists, httpMethod,
                        // httpRequestURL);
                    } else {
                        lock.lock();
                        try {
                            listOfRequests.add(httpRequestURL);
                            listOfChannels.add(ch);
                            listOfChStatus.add((byte) 1);
                            listOfReplies.add("");
                        } finally {
                            lock.unlock();
                        }
                        // logger.debug("Have opened a brand NEW channel:{} \t{}:{}", listOfRequests.size() - 1,
                        // httpMethod, httpRequestURL);
                    }
                    ch.writeAndFlush(request);
                    if (!isOnline) {
                        bringCameraOnline();
                    }
                    return;
                } else { // an error occured
                    cameraCommunicationError(
                            "Connection Timeout: Check your IP and PORT are correct and the camera can be reached.");
                }
            }
        });
    }

    public void processSnapshot() {
        lockCurrentSnapshot.lock();

        if (streamingSnapshotMjpeg) {
            sendMjpegFrame(currentSnapshot, snapshotMjpegChannelGroup);
        }
        if (streamingAutoFps) {
            if (motionDetected) {
                sendMjpegFrame(currentSnapshot, autoSnapshotMjpegChannelGroup);
            } else if (updateAutoFps) {
                sendMjpegFrame(currentSnapshot, autoSnapshotMjpegChannelGroup);
                updateAutoFps = false;
            }
        }
        if (preroll > 0) {
            fifoSnapshotBuffer.add(currentSnapshot);
            if (fifoSnapshotBuffer.size() > (preroll + postroll)) {
                fifoSnapshotBuffer.removeFirst();
            }
        }

        if (updateImageChannel) {
            updateState(CHANNEL_IMAGE, new RawType(currentSnapshot, "image/jpeg"));
        } else if (firstMotionAlarm || motionAlarmUpdateSnapshot) {
            updateState(CHANNEL_IMAGE, new RawType(currentSnapshot, "image/jpeg"));
            firstMotionAlarm = motionAlarmUpdateSnapshot = false;
        } else if (firstAudioAlarm || audioAlarmUpdateSnapshot) {
            updateState(CHANNEL_IMAGE, new RawType(currentSnapshot, "image/jpeg"));
            firstAudioAlarm = audioAlarmUpdateSnapshot = false;
        }
        lockCurrentSnapshot.unlock();
    }

    // These methods handle the response from all Camera brands, nothing specific to
    // any brand should be in here //

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
                // logger.trace("{}", msg.toString());
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
                            if (closeConnection) {
                                lock.lock();
                                try {
                                    byte indexInLists = (byte) listOfChannels.indexOf(ctx.channel());
                                    if (indexInLists >= 0) {
                                        listOfChStatus.set(indexInLists, (byte) 0);
                                    } else {
                                        logger.debug("!!!! Could not find the ch for a Connection: close URL:{}",
                                                requestUrl);
                                    }
                                } finally {
                                    lock.unlock();
                                }
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
                                lockCurrentSnapshot.lock();
                                currentSnapshot = incomingJpeg;
                                lockCurrentSnapshot.unlock();
                                processSnapshot();
                                // testing next line and if works need to do a full cleanup of this function.
                                closeConnection = true;
                                if (closeConnection) {
                                    // logger.trace("Snapshot recieved: Binding will now close the channel.");
                                    ctx.close();
                                } else {
                                    lock.lock();
                                    try {
                                        byte indexInLists = (byte) listOfChannels.indexOf(ctx.channel());
                                        if (indexInLists >= 0) {
                                            listOfChStatus.set(indexInLists, (byte) 2);
                                        }
                                    } finally {
                                        lock.unlock();
                                        bytesToRecieve = 0;
                                        bytesAlreadyRecieved = 0;
                                    }
                                }
                            }
                        } else { // incomingMessage that is not an IMAGE
                            if (incomingMessage.equals("")) {
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
                    // logger.debug("Packet back from camera is not matching HttpContent");
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
            if (ctx == null) {
                return;
            }
            lock.lock();
            try {
                byte indexInLists = (byte) listOfChannels.indexOf(ctx.channel());
                if (indexInLists >= 0) {
                    // logger.debug("commonCameraHandler closed channel:{} \tURL:{}", indexInLists, requestUrl);
                    listOfChStatus.set(indexInLists, (byte) -1);
                } else {
                    if (listOfChannels.size() > 0) {
                        logger.warn("Can't find ch when removing handler \t\tURL:{}", requestUrl);
                    }
                }
            } finally {
                lock.unlock();
            }
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
                    lock.lock();
                    try {
                        byte indexInLists = (byte) listOfChannels.indexOf(ctx.channel());
                        if (indexInLists >= 0) {
                            String urlToKeepOpen;
                            switch (thing.getThingTypeUID().getId()) {
                                case "DAHUA":
                                    urlToKeepOpen = listOfRequests.get(indexInLists);
                                    if ("/cgi-bin/eventManager.cgi?action=attach&codes=[All]"
                                            .contentEquals(urlToKeepOpen)) {
                                        return;
                                    }
                                    break;
                                case "HIKVISION":
                                    urlToKeepOpen = listOfRequests.get(indexInLists);
                                    if ("/ISAPI/Event/notification/alertStream".contentEquals(urlToKeepOpen)) {
                                        return;
                                    }
                                    break;
                                case "DOORBIRD":
                                    urlToKeepOpen = listOfRequests.get(indexInLists);
                                    if ("/bha-api/monitor.cgi?ring=doorbell,motionsensor"
                                            .contentEquals(urlToKeepOpen)) {
                                        return;
                                    }
                                    break;
                            }
                            logger.debug("! Channel was found idle for more than 15 seconds so closing it down. !");
                            listOfChStatus.set(indexInLists, (byte) 0);
                        } else {
                            logger.warn("!?! Channel that was found idle could not be located in our tracking. !?!");
                        }
                    } finally {
                        lock.unlock();
                    }
                    ctx.close();
                }
            }
        }
    }

    public String getLocalIpAddress() {
        String ipAddress = "";
        try {
            for (Enumeration<NetworkInterface> enumNetworks = NetworkInterface.getNetworkInterfaces(); enumNetworks
                    .hasMoreElements();) {
                NetworkInterface networkInterface = enumNetworks.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr
                        .hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().toString().length() < 18
                            && inetAddress.isSiteLocalAddress()) {
                        ipAddress = inetAddress.getHostAddress().toString();
                        logger.debug("Possible NIC/IP match found:{}", ipAddress);
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return ipAddress;
    }

    @SuppressWarnings("null")
    public void startStreamServer(boolean start) {

        if (!start) {
            serversLoopGroup.shutdownGracefully();
            serverBootstrap = null;
        } else {
            if (serverBootstrap == null) {
                hostIp = getLocalIpAddress();
                try {
                    serversLoopGroup = new NioEventLoopGroup();
                    serverBootstrap = new ServerBootstrap();
                    serverBootstrap.group(serversLoopGroup);
                    serverBootstrap.channel(NioServerSocketChannel.class);
                    // IP "0.0.0.0" will bind the server to all network connections//
                    serverBootstrap.localAddress(new InetSocketAddress("0.0.0.0", serverPort));
                    serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("idleStateHandler", new IdleStateHandler(0, 25, 0));
                            socketChannel.pipeline().addLast("HttpServerCodec", new HttpServerCodec());
                            socketChannel.pipeline().addLast("ChunkedWriteHandler", new ChunkedWriteHandler());
                            socketChannel.pipeline().addLast("streamServerHandler",
                                    new StreamServerHandler(getHandle()));
                        }
                    });
                    serverFuture = serverBootstrap.bind().sync();
                    serverFuture.await(4000);
                    logger.debug("File server for camera at {} has started on port {} for all NIC's.", ipAddress,
                            serverPort);
                    updateState(CHANNEL_STREAM_URL,
                            new StringType("http://" + hostIp + ":" + serverPort + "/ipcamera.mjpeg"));
                    updateState(CHANNEL_HLS_URL,
                            new StringType("http://" + hostIp + ":" + serverPort + "/ipcamera.m3u8"));
                    updateState(CHANNEL_IMAGE_URL,
                            new StringType("http://" + hostIp + ":" + serverPort + "/ipcamera.jpg"));
                } catch (Exception e) {
                    cameraConfigError(
                            "Exception occured when starting the streaming server. Try changing the SERVER_PORT to another number.");
                }
            }
        }
    }

    public void setupSnapshotStreaming(boolean stream, ChannelHandlerContext ctx, boolean auto) {
        if (stream) {
            sendMjpegFirstPacket(ctx);
            if (auto) {
                autoSnapshotMjpegChannelGroup.add(ctx.channel());
                lockCurrentSnapshot.lock();
                sendMjpegFrame(currentSnapshot, autoSnapshotMjpegChannelGroup);
                // iOS uses a FIFO? and needs two frames to display a pic
                sendMjpegFrame(currentSnapshot, autoSnapshotMjpegChannelGroup);
                lockCurrentSnapshot.unlock();
                streamingAutoFps = true;
            } else {
                snapshotMjpegChannelGroup.add(ctx.channel());
                lockCurrentSnapshot.lock();
                sendMjpegFrame(currentSnapshot, snapshotMjpegChannelGroup);
                lockCurrentSnapshot.unlock();
                streamingSnapshotMjpeg = true;
                startSnapshotPolling();
            }
        } else {
            snapshotMjpegChannelGroup.remove(ctx.channel());
            autoSnapshotMjpegChannelGroup.remove(ctx.channel());
            if (streamingSnapshotMjpeg && snapshotMjpegChannelGroup.isEmpty()) {
                streamingSnapshotMjpeg = false;
                stopSnapshotPolling();
                logger.debug("All Snapshot based MJPEG streams have stopped.");
            } else if (streamingAutoFps && autoSnapshotMjpegChannelGroup.isEmpty()) {
                streamingAutoFps = false;
                stopSnapshotPolling();
                logger.debug("All AutoFps Snapshot based MJPEG streams have stopped.");
            }
        }
    }

    // If start is true the CTX is added to the list to stream video to, false stops
    // the stream.
    public void setupMjpegStreaming(boolean start, ChannelHandlerContext ctx) {
        if (start) {
            if (mjpegChannelGroup.isEmpty()) {// first stream being requested.
                mjpegChannelGroup.add(ctx.channel());
                if (mjpegUri.equals("") || mjpegUri.equals("ffmpeg")) {
                    sendMjpegFirstPacket(ctx);
                    setupFfmpegFormat("MJPEG");
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
                logger.debug("All Mjpeg streams have stopped, cleaning up now");
                if (mjpegUri.equals("ffmpeg")) {
                    if (ffmpegMjpeg != null) {
                        ffmpegMjpeg.stopConverting();
                    }
                } else if (!mjpegUri.equals("")) {
                    closeChannel(getTinyUrl(mjpegUri));
                } else {
                    if (ffmpegMjpeg != null) {
                        ffmpegMjpeg.stopConverting();
                    }
                }
            }
        }
    }

    // sends direct to ctx so can be either snapshots.mjpeg or normal mjpeg stream
    public void sendMjpegFirstPacket(ChannelHandlerContext ctx) {
        final String BOUNDARY = "thisMjpegStream";
        String contentType = "multipart/x-mixed-replace; boundary=" + BOUNDARY;
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_CACHE);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        response.headers().add("Access-Control-Allow-Origin", "*");
        response.headers().add("Access-Control-Expose-Headers", "*");
        ctx.channel().writeAndFlush(response);
    }

    public void sendMjpegFrame(byte[] jpg, ChannelGroup channelGroup) {
        final String BOUNDARY = "thisMjpegStream";
        ByteBuf imageByteBuf = Unpooled.copiedBuffer(jpg);
        int length = imageByteBuf.readableBytes();
        String header = "--" + BOUNDARY + "\r\n" + "content-type: image/jpeg" + "\r\n" + "content-length: " + length
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
        logger.debug("Storing snapshots now to disk for GIF");
        for (Object incomingJpeg : fifoSnapshotBuffer) {
            byte[] foo = (byte[]) incomingJpeg;
            File file = new File(ffmpegOutputFolder + "snapshot" + count + ".jpg");
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
        lockCurrentSnapshot.unlock();
    }

    public void setupFfmpegFormat(String format) {
        String inOptions = "";
        if (ffmpegOutputFolder.equals("")) {
            logger.warn("The camera tried to use a FFmpeg feature when the output folder is not set.");
            return;
        }
        if (rtspUri.equals("")) {
            logger.warn("The camera tried to use a FFmpeg feature when no valid input for FFmpeg is provided.");
            return;
        }
        if (config.get(CONFIG_FFMPEG_LOCATION) == null) {
            logger.warn("The camera tried to use a FFmpeg feature when the location for FFmpeg is not known.");
            return;
        }

        // Make sure the folder exists, if not create it.
        new File(ffmpegOutputFolder).mkdirs();
        switch (format) {
            case "HLS":
                if (ffmpegHLS == null) {
                    if (rtspUri.contains(":554")) {
                        ffmpegHLS = new Ffmpeg(this, format, config.get(CONFIG_FFMPEG_LOCATION).toString(),
                                "-hide_banner -loglevel warning -rtsp_transport tcp", rtspUri,
                                config.get(CONFIG_FFMPEG_HLS_OUT_ARGUMENTS).toString(),
                                ffmpegOutputFolder + "ipcamera.m3u8", username, password);
                    } else {
                        ffmpegHLS = new Ffmpeg(this, format, config.get(CONFIG_FFMPEG_LOCATION).toString(),
                                "-hide_banner -loglevel warning", rtspUri,
                                config.get(CONFIG_FFMPEG_HLS_OUT_ARGUMENTS).toString(),
                                ffmpegOutputFolder + "ipcamera.m3u8", username, password);
                    }
                }
                if (ffmpegHLS != null) {
                    ffmpegHLS.startConverting();
                }
                break;
            case "GIF":
                if (preroll > 0) {
                    ffmpegGIF = new Ffmpeg(this, format, config.get(CONFIG_FFMPEG_LOCATION).toString(),
                            "-y -r 1 -hide_banner -loglevel warning", ffmpegOutputFolder + "snapshot%d.jpg",
                            "-frames:v " + (preroll + postroll) + " "
                                    + config.get(CONFIG_FFMPEG_GIF_OUT_ARGUMENTS).toString(),
                            ffmpegOutputFolder + gifFilename + ".gif", username, password);
                } else {
                    inOptions = "-y -t " + postroll + " -rtsp_transport tcp -hide_banner -loglevel warning";
                    if (!rtspUri.contains("rtsp")) {
                        inOptions = "-y -t " + postroll;
                    }
                    ffmpegGIF = new Ffmpeg(this, format, config.get(CONFIG_FFMPEG_LOCATION).toString(), inOptions,
                            rtspUri, config.get(CONFIG_FFMPEG_GIF_OUT_ARGUMENTS).toString(),
                            ffmpegOutputFolder + gifFilename + ".gif", username, password);
                }
                if (preroll > 0) {
                    storeSnapshots();
                }
                if (ffmpegGIF != null) {
                    ffmpegGIF.startConverting();
                    if (gifHistory.equals("")) {
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
            case "RECORD":
                inOptions = "-y -t " + mp4RecordTime + " -rtsp_transport tcp -hide_banner -loglevel warning";
                if (!rtspUri.contains("rtsp")) {
                    inOptions = "-y -t " + mp4RecordTime;
                }
                ffmpegRecord = new Ffmpeg(this, format, config.get(CONFIG_FFMPEG_LOCATION).toString(), inOptions,
                        rtspUri, config.get(CONFIG_FFMPEG_MP4_OUT_ARGUMENTS).toString(),
                        ffmpegOutputFolder + mp4Filename + ".mp4", username, password);
                if (mp4Preroll > 0) {
                    // fetchFromHLS(); todo: not done yet
                }
                if (ffmpegRecord != null) {
                    ffmpegRecord.startConverting();
                    if (mp4History.equals("")) {
                        mp4History = mp4Filename;
                    } else if (!mp4Filename.equals("ipcamera")) {
                        mp4History = mp4Filename + "," + mp4History;
                        if (mp4HistoryLength > 49) {
                            int endIndex = mp4History.lastIndexOf(",");
                            mp4History = mp4History.substring(0, endIndex);
                        }
                    }
                    setChannelState(CHANNEL_MP4_HISTORY, new StringType(mp4History));
                }
                break;
            case "RTSPHELPER":
                if (ffmpegRtspHelper != null) {
                    ffmpegRtspHelper.stopConverting();
                    if (!audioAlarmEnabled && !motionAlarmEnabled) {
                        return;
                    }
                }
                String input = (config.get(CONFIG_FFMPEG_MOTION_INPUT) == null) ? rtspUri
                        : config.get(CONFIG_FFMPEG_MOTION_INPUT).toString();
                String OutputOptions = "-f null -";
                String filterOptions = "";
                inOptions = "-rtsp_transport tcp";
                if (!input.contains("rtsp")) {
                    inOptions = "";
                }
                if (audioAlarmEnabled == false) {
                    filterOptions = "-an";
                } else if (audioAlarmEnabled == true) {
                    filterOptions = "-af silencedetect=n=-" + audioThreshold + "dB:d=2";
                }
                if (motionAlarmEnabled == false && ffmpegSnapshotGeneration == false) {
                    filterOptions = filterOptions.concat(" -vn");
                } else if (motionAlarmEnabled == true) {
                    filterOptions = filterOptions
                            .concat(" -vf select='gte(scene," + motionThreshold + ")',metadata=print");
                }
                if (config.get(CONFIG_USERNAME) != null) {
                    filterOptions += " ";// add space as the Framework does not allow spaces at start of config.
                }
                ffmpegRtspHelper = new Ffmpeg(this, format, config.get(CONFIG_FFMPEG_LOCATION).toString(), inOptions,
                        input, filterOptions + config.get(CONFIG_FFMPEG_MOTION_ARGUMENTS), OutputOptions, username,
                        password);
                ffmpegRtspHelper.startConverting();
                break;
            case "MJPEG":
                if (ffmpegMjpeg == null) {
                    inOptions = "-rtsp_transport tcp -hide_banner -loglevel warning";
                    if (!rtspUri.contains("rtsp")) {
                        inOptions = "-hide_banner -loglevel warning";
                    }
                    ffmpegMjpeg = new Ffmpeg(this, format, config.get(CONFIG_FFMPEG_LOCATION).toString(), inOptions,
                            rtspUri, config.get(CONFIG_FFMPEG_MJPEG_ARGUMENTS).toString(),
                            "http://127.0.0.1:" + serverPort + "/ipcamera.jpg", username, password);
                }
                if (ffmpegMjpeg != null) {
                    ffmpegMjpeg.startConverting();
                }
                break;
            case "SNAPSHOT":
                // if mjpeg stream you can use ffmpeg -i input.h264 -codec:v copy -bsf:v mjpeg2jpeg output%03d.jpg
                if (ffmpegSnapshot == null) {
                    inOptions = "-rtsp_transport tcp -threads 1 -skip_frame nokey -hide_banner -loglevel warning";// iFrames
                                                                                                                  // only
                    if (!rtspUri.contains("rtsp")) {
                        inOptions = "-threads 1 -skip_frame nokey -hide_banner -loglevel warning";
                    }
                    ffmpegSnapshot = new Ffmpeg(this, format, config.get(CONFIG_FFMPEG_LOCATION).toString(), inOptions,
                            rtspUri, "-an -vsync vfr -update 1", "http://127.0.0.1:" + serverPort + "/snapshot.jpg",
                            username, password);
                }
                if (ffmpegSnapshot != null) {
                    ffmpegSnapshot.startConverting();
                }
                break;
        }
    }

    public void noMotionDetected(String thisAlarmsChannel) {
        setChannelState(thisAlarmsChannel, OnOffType.valueOf("OFF"));
        firstMotionAlarm = false;
        motionAlarmUpdateSnapshot = false;
        motionDetected = false;
        if (streamingAutoFps) {
            stopSnapshotPolling();
        } else if (updateImageEvents.contains("4")) { // During Motion Alarms
            stopSnapshotPolling();
        }
    }

    // Change alarms that are not counted as motion detecting.
    public void changeAlarmState(String thisAlarmsChannel, String state) {
        updateState(thisAlarmsChannel, OnOffType.valueOf(state));
    }

    public void motionDetected(String thisAlarmsChannel) {
        updateState(CHANNEL_LAST_MOTION_TYPE, new StringType(thisAlarmsChannel));
        updateState(thisAlarmsChannel, OnOffType.valueOf("ON"));
        motionDetected = true;
        if (streamingAutoFps) {
            startSnapshotPolling();
        }
        if (updateImageEvents.contains("2")) {
            if (!firstMotionAlarm) {
                if (!snapshotUri.isEmpty()) {
                    sendHttpGET(snapshotUri);
                }
                firstMotionAlarm = true;// reset back to false when the jpg arrives.
            }
        } else if (updateImageEvents.contains("4")) { // During Motion Alarms
            if (!snapshotPolling) {
                startSnapshotPolling();
            }
            firstMotionAlarm = true;
            motionAlarmUpdateSnapshot = true;
        }
    }

    public void audioDetected() {
        updateState(CHANNEL_AUDIO_ALARM, OnOffType.valueOf("ON"));
        if (updateImageEvents.contains("3")) {
            if (!firstAudioAlarm) {
                if (!snapshotUri.isEmpty()) {
                    sendHttpGET(snapshotUri);
                }
                firstAudioAlarm = true;// reset back to false when the jpg arrives.
            }
        } else if (updateImageEvents.contains("5")) {// During audio alarms
            firstAudioAlarm = true;
            audioAlarmUpdateSnapshot = true;
        }
    }

    public void noAudioDetected() {
        setChannelState(CHANNEL_AUDIO_ALARM, OnOffType.valueOf("OFF"));
        firstAudioAlarm = false;
        audioAlarmUpdateSnapshot = false;
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

    public String searchString(String rawString, String searchedString) {
        String result = "";
        int index = 0;
        index = rawString.indexOf(searchedString);
        if (index != -1) // -1 means "not found"
        {
            result = rawString.substring(index + searchedString.length(), rawString.length());
            index = result.indexOf(',');
            if (index == -1) {
                index = result.indexOf('"');
                if (index == -1) {
                    index = result.indexOf('}');
                    if (index == -1) {
                        return result;
                    } else {
                        return result.substring(0, index);
                    }
                } else {
                    return result.substring(0, index);
                }
            } else {
                result = result.substring(0, index);
                index = result.indexOf('"');
                if (index == -1) {
                    return result;
                } else {
                    return result.substring(0, index);
                }
            }
        }
        return "";
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
            }
        } // caution "REFRESH" can still progress to brand Handlers below the else.
        else {
            switch (channelUID.getId()) {
                case CHANNEL_MP4_HISTORY_LENGTH:
                    if ("0".equals(command.toString())) {
                        mp4HistoryLength = 0;
                        mp4History = "";
                        setChannelState(CHANNEL_MP4_HISTORY, new StringType(mp4History));
                    }
                    return;
                case CHANNEL_GIF_HISTORY_LENGTH:
                    if ("0".equals(command.toString())) {
                        gifHistoryLength = 0;
                        gifHistory = "";
                        setChannelState(CHANNEL_GIF_HISTORY, new StringType(gifHistory));
                    }
                    return;
                case CHANNEL_FFMPEG_MOTION_CONTROL:
                    if ("ON".equals(command.toString())) {
                        motionAlarmEnabled = true;
                    } else if ("OFF".equals(command.toString()) || "0".equals(command.toString())) {
                        motionAlarmEnabled = false;
                        noMotionDetected(CHANNEL_MOTION_ALARM);
                    } else {
                        motionAlarmEnabled = true;
                        motionThreshold = Double.valueOf(command.toString());
                        motionThreshold = motionThreshold / 10000;
                    }
                    setupFfmpegFormat("RTSPHELPER");
                    return;
                case CHANNEL_GIF_FILENAME:
                    logger.debug("Changing the gif filename to {}.", command);
                    gifFilename = command.toString();
                    if (gifFilename.isEmpty()) {
                        gifFilename = "ipcamera";
                    }
                    return;
                case CHANNEL_MP4_FILENAME:
                    logger.debug("Changing the mp4 filename to {}.", command);
                    mp4Filename = command.toString();
                    if (mp4Filename.isEmpty()) {
                        mp4Filename = "ipcamera";
                    }
                    return;
                case CHANNEL_RECORD_MP4:
                    logger.debug("Recording {} Seconds to MP4 format.", command);
                    mp4RecordTime = Integer.parseInt(command.toString());
                    setupFfmpegFormat("RECORD");
                    return;
                case CHANNEL_START_STREAM:
                    if ("ON".equals(command.toString())) {
                        setupFfmpegFormat("HLS");
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
                    if ("ON".equals(command.toString())) {
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
                case CHANNEL_UPDATE_IMAGE_NOW:
                    if ("ON".equals(command.toString())) {
                        if (snapshotUri.equals("")) {
                            ffmpegSnapshotGeneration = true;
                            setupFfmpegFormat("SNAPSHOT");
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
                case CHANNEL_UPDATE_GIF:
                    if ("ON".equals(command.toString())) {
                        if (preroll > 0) {
                            snapCount = postroll;
                        } else {
                            setupFfmpegFormat("GIF");
                        }
                    }
                    return;
                case CHANNEL_PAN:
                    if (onvifCamera.supportsPTZ()) {
                        if (command instanceof IncreaseDecreaseType) {
                            if (command == IncreaseDecreaseType.INCREASE) {
                                if ((boolean) config.get(CONFIG_PTZ_CONTINUOUS)) {
                                    onvifCamera.sendPTZRequest("ContinuousMoveLeft");
                                } else {
                                    onvifCamera.sendPTZRequest("RelativeMoveLeft");
                                }
                            } else {
                                if ((boolean) config.get(CONFIG_PTZ_CONTINUOUS)) {
                                    onvifCamera.sendPTZRequest("ContinuousMoveRight");
                                } else {
                                    onvifCamera.sendPTZRequest("RelativeMoveRight");
                                }
                            }
                            return;
                        } else if ("OFF".equals(command.toString())) {
                            onvifCamera.sendPTZRequest("Stop");
                            return;
                        }
                        onvifCamera.setAbsolutePan(Float.valueOf(command.toString()));
                        scheduledMovePTZ.schedule(runnableMovePTZ, 500, TimeUnit.MILLISECONDS);
                    }
                    return;
                case CHANNEL_TILT:
                    if (onvifCamera.supportsPTZ()) {
                        if (command instanceof IncreaseDecreaseType) {
                            if ("INCREASE".equals(command.toString())) {
                                if ((boolean) config.get(CONFIG_PTZ_CONTINUOUS)) {
                                    onvifCamera.sendPTZRequest("ContinuousMoveUp");
                                } else {
                                    onvifCamera.sendPTZRequest("RelativeMoveUp");
                                }
                            } else {
                                if ((boolean) config.get(CONFIG_PTZ_CONTINUOUS)) {
                                    onvifCamera.sendPTZRequest("ContinuousMoveDown");
                                } else {
                                    onvifCamera.sendPTZRequest("RelativeMoveDown");
                                }
                            }
                            return;
                        } else if ("OFF".equals(command.toString())) {
                            onvifCamera.sendPTZRequest("Stop");
                            return;
                        }
                        onvifCamera.setAbsoluteTilt(Float.valueOf(command.toString()));
                        scheduledMovePTZ.schedule(runnableMovePTZ, 500, TimeUnit.MILLISECONDS);
                    }
                    return;
                case CHANNEL_ZOOM:
                    if (onvifCamera.supportsPTZ()) {
                        if (command instanceof IncreaseDecreaseType) {
                            if ("INCREASE".equals(command.toString())) {
                                if ((boolean) config.get(CONFIG_PTZ_CONTINUOUS)) {
                                    onvifCamera.sendPTZRequest("ContinuousMoveIn");
                                } else {
                                    onvifCamera.sendPTZRequest("RelativeMoveIn");
                                }
                            } else {
                                if ((boolean) config.get(CONFIG_PTZ_CONTINUOUS)) {
                                    onvifCamera.sendPTZRequest("ContinuousMoveOut");
                                } else {
                                    onvifCamera.sendPTZRequest("RelativeMoveOut");
                                }
                            }
                            return;
                        } else if ("OFF".equals(command.toString())) {
                            onvifCamera.sendPTZRequest("Stop");
                            return;
                        }
                        onvifCamera.setAbsoluteZoom(Float.valueOf(command.toString()));
                        scheduledMovePTZ.schedule(runnableMovePTZ, 500, TimeUnit.MILLISECONDS);
                    }
                    return;
            }
        }
        // commands and refresh now get passed to brand handlers
        switch (thing.getThingTypeUID().getId()) {
            case "AMCREST":
                AmcrestHandler amcrestHandler = new AmcrestHandler(getHandle());
                amcrestHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests.isEmpty()) {
                    lowPriorityRequests = amcrestHandler.getLowPriorityRequests();
                }
                break;
            case "DAHUA":
                DahuaHandler dahuaHandler = new DahuaHandler(getHandle(), nvrChannel);
                dahuaHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests.isEmpty()) {
                    lowPriorityRequests = dahuaHandler.getLowPriorityRequests();
                }
                break;
            case "DOORBIRD":
                DoorBirdHandler doorBirdHandler = new DoorBirdHandler(getHandle());
                doorBirdHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests.isEmpty()) {
                    lowPriorityRequests = doorBirdHandler.getLowPriorityRequests();
                }
                break;
            case "HIKVISION":
                HikvisionHandler hikvisionHandler = new HikvisionHandler(getHandle(), nvrChannel);
                hikvisionHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests.isEmpty()) {
                    lowPriorityRequests = hikvisionHandler.getLowPriorityRequests();
                }
                break;
            case "FOSCAM":
                FoscamHandler foscamHandler = new FoscamHandler(getHandle(), username, password);
                foscamHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests.isEmpty()) {
                    lowPriorityRequests = foscamHandler.getLowPriorityRequests();
                }
                break;
            case "INSTAR":
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

    Runnable runnableMovePTZ = new Runnable() {
        @Override
        public void run() {
            logger.debug("Trying to move with new PTZ Absolute move.");
            onvifCamera.sendPTZRequest("AbsoluteMove");
        }
    };

    public void setChannelState(String channelToUpdate, State valueOf) {
        updateState(channelToUpdate, valueOf);
    }

    public String encodeSpecialChars(String text) {
        String encodedString = "";
        try {
            encodedString = URLEncoder.encode(text, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            logger.warn("Failed to encode special characters for URL. {}", e.getMessage());
        }
        return encodedString;
    }

    void bringCameraOnline() {
        isOnline = true;
        updateStatus(ThingStatus.ONLINE);
        listOfOnlineCameraHandlers.add(this);
        listOfOnlineCameraUID.add(getThing().getUID().getId());
        if (cameraConnectionJob != null) {
            cameraConnectionJob.cancel(false);
        }

        if (preroll > 0 || updateImageEvents.contains("1")) {
            snapshotPolling = true;
            snapshotJob = snapshot.scheduleAtFixedRate(snapshotRunnable, 1000,
                    Integer.parseInt(config.get(CONFIG_POLL_CAMERA_MS).toString()), TimeUnit.MILLISECONDS);
        }

        pollCameraJob = pollCamera.scheduleWithFixedDelay(pollCameraRunnable, 1000, 8000, TimeUnit.MILLISECONDS);

        if (!rtspUri.equals("")) {
            updateState(CHANNEL_RTSP_URL, new StringType(rtspUri));
        }
        if (updateImageChannel) {
            updateState(CHANNEL_UPDATE_IMAGE_NOW, OnOffType.valueOf("ON"));
        } else {
            updateState(CHANNEL_UPDATE_IMAGE_NOW, OnOffType.valueOf("OFF"));
        }
        if (!listOfGroupHandlers.isEmpty()) {
            for (IpCameraGroupHandler handle : listOfGroupHandlers) {
                handle.cameraOnline(getThing().getUID().getId());
            }
        }
    }

    void snapshotIsFfmpeg() {
        bringCameraOnline();
        snapshotUri = "";// ffmpeg is a valid option. Simplify further checks.
        logger.info(
                "Binding has no snapshot url. Will now use your CPU and FFmpeg which must be installed to create snapshots from RTSP.");
        if (!rtspUri.equals("")) {
            updateImageChannel = false;
            ffmpegSnapshotGeneration = true;
            setupFfmpegFormat("SNAPSHOT");
            updateState(CHANNEL_UPDATE_IMAGE_NOW, OnOffType.valueOf("ON"));
        } else {
            cameraConfigError(
                    "Binding can not find a RTSP url for this camera, please OVERRIDE the url as per the readme.");
        }
    }

    Runnable pollingCameraConnection = new Runnable() {
        @Override
        public void run() {
            if (thing.getThingTypeUID().getId().equals("HTTPONLY")) {
                if (rtspUri.equals("")) {
                    logger.warn("Binding has not been supplied with a RTSP URL so some features will not work.");
                }
                if (snapshotUri.equals("") || snapshotUri.equals("ffmpeg")) {
                    snapshotIsFfmpeg();
                } else {
                    sendHttpRequest("GET", snapshotUri, null);
                }
                return;
            }
            if (!onvifCamera.isConnected()) {
                logger.debug("About to connect to the IP Camera using the ONVIF PORT at IP:{}:{}", ipAddress,
                        config.get(CONFIG_ONVIF_PORT).toString());
                onvifCamera.connect(thing.getThingTypeUID().getId().equals("ONVIF"));
            }
            if (snapshotUri.equals("ffmpeg")) {
                snapshotIsFfmpeg();
            } else if (!snapshotUri.equals("")) {
                sendHttpRequest("GET", snapshotUri, null);
            } else if (!rtspUri.equals("")) {
                snapshotIsFfmpeg();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Camera failed to report a valid Snaphot and/or RTSP URL. See readme on how to use the SNAPSHOT_URL_OVERRIDE feature.");
            }
        }
    };

    public void cameraConfigError(String reason) {
        // wont try to reconnect again due to a config error being the cause.
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, reason);
        restart();
    }

    public void cameraCommunicationError(String reason) {
        // will try to reconnect again as camera may be rebooting.
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        if (isOnline) {// if already offline dont try reconnecting in 6 seconds, we want 30sec wait.
            resetAndRetryConnecting();
        }
    }

    boolean streamIsStopped(String url) {
        byte indexInLists = 0;
        lock.lock();
        try {
            indexInLists = (byte) listOfRequests.lastIndexOf(url);
            if (indexInLists < 0) {
                return true; // Stream not found, probably first run.
            }
            // Stream was found in list now to check status
            // Status can be -1=closed, 0=closing (do not re-use channel), 1=open , 2=open
            // and ok to reuse
            else if (listOfChStatus.get(indexInLists) < 1) {
                // may need to check if more than one is in the lists.
                return true; // Stream was open, but not now.
            }
        } finally {
            lock.unlock();
        }
        return false; // Stream is still open
    }

    Runnable snapshotRunnable = new Runnable() {
        @Override
        public void run() {
            // Snapshot should be first to keep consistent time between shots
            sendHttpGET(snapshotUri);
            if (snapCount > 0) {
                if (--snapCount == 0) {
                    setupFfmpegFormat("GIF");
                }
            }
        }
    };

    public void stopSnapshotPolling() {
        if (!streamingSnapshotMjpeg && preroll == 0 && !updateImageEvents.contains("1")) {
            snapshotPolling = false;
            if (snapshotJob != null) {
                snapshotJob.cancel(true);
            }
        } else if (updateImageEvents.contains("4")) { // only during Motion Alarms
            snapshotPolling = false;
            if (snapshotJob != null) {
                snapshotJob.cancel(true);
            }
        }
    }

    public void startSnapshotPolling() {
        if (snapshotPolling) {
            return; // Already polling
        }
        if (streamingSnapshotMjpeg || streamingAutoFps) {
            snapshotPolling = true;
            snapshotJob = snapshot.scheduleAtFixedRate(snapshotRunnable, 200,
                    Integer.parseInt(config.get(CONFIG_POLL_CAMERA_MS).toString()), TimeUnit.MILLISECONDS);
        } else if (updateImageEvents.contains("4")) { // During Motion Alarms
            snapshotPolling = true;
            snapshotJob = snapshot.scheduleAtFixedRate(snapshotRunnable, 200,
                    Integer.parseInt(config.get(CONFIG_POLL_CAMERA_MS).toString()), TimeUnit.MILLISECONDS);
        }
    }

    // runs every 8 seconds due to mjpeg streams not staying open unless they update this often.
    Runnable pollCameraRunnable = new Runnable() {
        @Override
        public void run() {
            // Snapshot should be first to keep consistent time between shots
            if (!snapshotUri.equals("")) {
                if (updateImageChannel) {
                    sendHttpGET(snapshotUri);
                }
            }
            if (streamingAutoFps) {
                updateAutoFps = true;
                if (!snapshotPolling) {
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
                case "HTTPONLY":
                    break;
                case "ONVIF":
                    if (!onvifCamera.isConnected()) {
                        onvifCamera.connect(true);
                    }
                    break;
                case "INSTAR":
                    noMotionDetected(CHANNEL_MOTION_ALARM);
                    noMotionDetected(CHANNEL_PIR_ALARM);
                    noAudioDetected();
                    break;
                case "HIKVISION":
                    if (streamIsStopped("/ISAPI/Event/notification/alertStream")) {
                        logger.info("The alarm stream was not running for camera {}, re-starting it now", ipAddress);
                        sendHttpGET("/ISAPI/Event/notification/alertStream");
                    }
                    break;
                case "AMCREST":
                    sendHttpGET("/cgi-bin/eventManager.cgi?action=getEventIndexes&code=VideoMotion");
                    sendHttpGET("/cgi-bin/eventManager.cgi?action=getEventIndexes&code=AudioMutation");
                    break;
                case "DAHUA":
                    // Check for alarms, channel for NVRs appears not to work at filtering.
                    if (streamIsStopped("/cgi-bin/eventManager.cgi?action=attach&codes=[All]")) {
                        logger.info("The alarm stream was not running for camera {}, re-starting it now", ipAddress);
                        sendHttpGET("/cgi-bin/eventManager.cgi?action=attach&codes=[All]");
                    }
                    break;
                case "DOORBIRD":
                    // Check for alarms, channel for NVRs appears not to work at filtering.
                    if (streamIsStopped("/bha-api/monitor.cgi?ring=doorbell,motionsensor")) {
                        logger.info("The alarm stream was not running for camera {}, re-starting it now", ipAddress);
                        sendHttpGET("/bha-api/monitor.cgi?ring=doorbell,motionsensor");
                    }
                    break;
            }
            if (ffmpegHLS != null) {
                ffmpegHLS.checkKeepAlive();
            }
            if (listOfRequests.size() > 12) {
                logger.debug(
                        "There are {} channels being tracked, cleaning out old channels now to try and reduce this to 12 or below.",
                        listOfRequests.size());
                cleanChannels();
            }
        }
    };

    @Override
    public void initialize() {
        config = thing.getConfiguration();
        ipAddress = config.get(CONFIG_IPADDRESS).toString();
        username = (config.get(CONFIG_USERNAME) == null) ? "" : config.get(CONFIG_USERNAME).toString();
        password = (config.get(CONFIG_PASSWORD) == null) ? "" : config.get(CONFIG_PASSWORD).toString();
        preroll = Integer.parseInt(config.get(CONFIG_GIF_PREROLL).toString());
        postroll = Integer.parseInt(config.get(CONFIG_GIF_POSTROLL).toString());
        updateImageEvents = config.get(CONFIG_IMAGE_UPDATE_EVENTS).toString();
        updateImageChannel = (boolean) config.get(CONFIG_UPDATE_IMAGE);

        snapshotUri = (config.get(CONFIG_SNAPSHOT_URL_OVERRIDE) == null) ? ""
                : getCorrectUrlFormat(config.get(CONFIG_SNAPSHOT_URL_OVERRIDE).toString());

        mjpegUri = (config.get(CONFIG_STREAM_URL_OVERRIDE) == null) ? ""
                : getCorrectUrlFormat(config.get(CONFIG_STREAM_URL_OVERRIDE).toString());

        nvrChannel = (config.get(CONFIG_NVR_CHANNEL) == null) ? "" : config.get(CONFIG_NVR_CHANNEL).toString();

        selectedMediaProfile = (config.get(CONFIG_ONVIF_PROFILE_NUMBER) == null) ? 0
                : Integer.parseInt(config.get(CONFIG_ONVIF_PROFILE_NUMBER).toString());

        serverPort = Integer.parseInt(config.get(CONFIG_SERVER_PORT).toString());
        if (serverPort == -1) {
            logger.warn("The SERVER_PORT = -1 which disables a lot of features. See readme for more info.");
        } else if (serverPort < 1025) {
            logger.warn("The SERVER_PORT is <= 1024 and may cause permission errors under Linux, try a higher port.");
        }

        rtspUri = (config.get(CONFIG_FFMPEG_INPUT) == null) ? "" : config.get(CONFIG_FFMPEG_INPUT).toString();

        ffmpegOutputFolder = (config.get(CONFIG_FFMPEG_OUTPUT) == null) ? ""
                : config.get(CONFIG_FFMPEG_OUTPUT).toString();

        // Known cameras will connect quicker if we skip ONVIF questions.
        switch (thing.getThingTypeUID().getId()) {
            case "AMCREST":
            case "DAHUA":
                if (mjpegUri.equals("")) {
                    mjpegUri = "/cgi-bin/mjpg/video.cgi?channel=" + nvrChannel + "&subtype=1";
                }
                if (snapshotUri.equals("")) {
                    snapshotUri = "/cgi-bin/snapshot.cgi?channel=" + nvrChannel;
                }
                break;
            case "DOORBIRD":
                if (mjpegUri.equals("")) {
                    mjpegUri = "/bha-api/video.cgi";
                }
                if (snapshotUri.equals("")) {
                    snapshotUri = "/bha-api/image.cgi";
                }
                break;
            case "FOSCAM":
                // Foscam needs any special char like spaces (%20) to be encoded for URLs.
                username = encodeSpecialChars(username);
                password = encodeSpecialChars(password);
                if (mjpegUri.equals("")) {
                    mjpegUri = "/cgi-bin/CGIStream.cgi?cmd=GetMJStream&usr=" + username + "&pwd=" + password;
                }
                if (snapshotUri.equals("")) {
                    snapshotUri = "/cgi-bin/CGIProxy.fcgi?usr=" + username + "&pwd=" + password + "&cmd=snapPicture2";
                }
                break;
            case "HIKVISION":// The 02 gives you the first sub stream which needs to be set to MJPEG
                if (mjpegUri.equals("")) {
                    mjpegUri = "/ISAPI/Streaming/channels/" + nvrChannel + "02" + "/httppreview";
                }
                if (snapshotUri.equals("")) {
                    snapshotUri = "/ISAPI/Streaming/channels/" + nvrChannel + "01/picture";
                }
                break;
            case "INSTAR":
                if (snapshotUri.equals("")) {
                    snapshotUri = "/tmpfs/snap.jpg";
                }
                if (mjpegUri.equals("")) {
                    mjpegUri = "/mjpegstream.cgi?-chn=12";
                }
                break;
        }

        // Onvif and Instar event handling needs the host IP and the server started.
        if (!"-1".contentEquals(config.get(CONFIG_SERVER_PORT).toString())) {
            startStreamServer(true);
        }

        if (!thing.getThingTypeUID().getId().equals("HTTPONLY")) {
            BigDecimal test = new BigDecimal(config.get(CONFIG_ONVIF_PORT).toString());
            onvifCamera = new OnvifConnection(this, ipAddress + ":" + test.intValue(), username, password);
            onvifCamera.setSelectedMediaProfile(selectedMediaProfile);
            onvifCamera.connect(thing.getThingTypeUID().getId().equals("ONVIF"));
        }

        // for poll times above 5 seconds don't display a warning about the Image channel.
        if (9000 <= Integer.parseInt(config.get(CONFIG_POLL_CAMERA_MS).toString()) && updateImageChannel) {
            logger.warn(
                    "The Image channel is set to update more often than 8 seconds. This is not recommended. The Image channel is best used only for higher poll times. See the readme file on how to display the cameras picture for best results or use a higher poll time.");
        }
        // Waiting 3 seconds for ONVIF to discover the urls before running.
        cameraConnectionJob = cameraConnection.scheduleWithFixedDelay(pollingCameraConnection, 6, 30, TimeUnit.SECONDS);
    }

    // What the camera needs to re-connect if the initialize() is not called.
    private void resetAndRetryConnecting() {
        restart();
        initialize();
    }

    // Called when camera goes offline but the main handler is not destroyed.
    private void restart() {
        isOnline = false;
        snapshotPolling = false;
        onvifCamera.disconnect();
        if (pollCameraJob != null) {
            pollCameraJob.cancel(true);
            pollCamera.shutdown();
            pollCamera = Executors.newScheduledThreadPool(1);
            pollCameraJob = null;
        }
        if (snapshotJob != null) {
            snapshotJob.cancel(true);
            snapshot.shutdown();
            snapshot = Executors.newScheduledThreadPool(1);
            snapshotJob = null;
        }
        if (cameraConnectionJob != null) {
            cameraConnectionJob.cancel(true);
            cameraConnection.shutdown();
            cameraConnection = Executors.newScheduledThreadPool(1);
            cameraConnectionJob = null;
        }

        listOfOnlineCameraHandlers.remove(this);
        listOfOnlineCameraUID.remove(getThing().getUID().getId());
        // inform all group handlers that this camera has gone offline
        for (IpCameraGroupHandler handle : listOfGroupHandlers) {
            handle.cameraOffline(this);
        }
        basicAuth = ""; // clear out stored password hash
        useDigestAuth = false;
        startStreamServer(false);
        closeAllChannels();

        if (ffmpegHLS != null) {
            ffmpegHLS.setKeepAlive(8);
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

        lock.lock();
        try {
            listOfRequests.clear();
            listOfChannels.clear();
            listOfChStatus.clear();
            listOfReplies.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void dispose() {
        restart();
    }

    public void setStreamServerHandler(StreamServerHandler streamServerHandler2) {
        streamServerHandler = streamServerHandler2;
    }

    public String getWhiteList() {
        return config.get(CONFIG_IP_WHITELIST).toString();
    }
}
