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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.GroupConfig;
import org.openhab.binding.ipcamera.internal.GroupTracker;
import org.openhab.binding.ipcamera.internal.Helper;
import org.openhab.binding.ipcamera.internal.StreamServerGroupHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * The {@link IpCameraGroupHandler} is responsible for finding cameras that are part of this group and displaying a
 * group picture.
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class IpCameraGroupHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public GroupConfig groupConfig;
    private BigDecimal pollTimeInSeconds = new BigDecimal(2);
    public ArrayList<IpCameraHandler> cameraOrder = new ArrayList<IpCameraHandler>(2);
    private EventLoopGroup serversLoopGroup = new NioEventLoopGroup();
    private final ScheduledExecutorService pollCameraGroup = Executors.newSingleThreadScheduledExecutor();
    private @Nullable ScheduledFuture<?> pollCameraGroupJob = null;
    private @Nullable ServerBootstrap serverBootstrap;
    private @Nullable ChannelFuture serverFuture = null;
    public String hostIp;
    private boolean motionChangesOrder = true;
    public int serverPort = 0;
    public String playList = "";
    private String playingNow = "";
    public int cameraIndex = 0;
    public boolean hlsTurnedOn = false;
    private int entries = 0;
    private int mediaSequence = 1;
    private int discontinuitySequence = 0;
    private GroupTracker groupTracker;

    public IpCameraGroupHandler(Thing thing, @Nullable String openhabIpAddress, GroupTracker groupTracker) {
        super(thing);
        groupConfig = getConfigAs(GroupConfig.class);
        if (openhabIpAddress != null) {
            hostIp = openhabIpAddress;
        } else {
            hostIp = Helper.getLocalIpAddress();
        }
        this.groupTracker = groupTracker;
    }

    public String getPlayList() {
        return playList;
    }

    public String getOutputFolder(int index) {
        IpCameraHandler handle = cameraOrder.get(index);
        return handle.cameraConfig.getFfmpegOutput();
    }

    private String readCamerasPlaylist(int cameraIndex) {
        String camerasm3u8 = "";
        IpCameraHandler handle = cameraOrder.get(cameraIndex);
        try {
            String file = handle.cameraConfig.getFfmpegOutput() + "ipcamera.m3u8";
            camerasm3u8 = new String(Files.readAllBytes(Paths.get(file)));
        } catch (IOException e) {
            logger.warn("Error occured fetching a groupDisplay cameras m3u8 file :{}", e.getMessage());
        }
        return camerasm3u8;
    }

    String keepLast(String string, int numberToRetain) {
        int start = string.length();
        for (int loop = numberToRetain; loop > 0; loop--) {
            start = string.lastIndexOf("#EXTINF:", start - 1);
            if (start == -1) {
                logger.warn(
                        "Playlist did not contain enough entries, check all cameras in groups use the same HLS settings.");
                return "";
            }
        }
        entries = entries + numberToRetain;
        return string.substring(start);
    }

    String removeFromStart(String string, int numberToRemove) {
        int startingFrom = string.indexOf("#EXTINF:");
        for (int loop = numberToRemove; loop > 0; loop--) {
            startingFrom = string.indexOf("#EXTINF:", startingFrom + 27);
            if (startingFrom == -1) {
                logger.warn(
                        "Playlist failed to remove entries from start, check all cameras in groups use the same HLS settings.");
                return string;
            }
        }
        mediaSequence = mediaSequence + numberToRemove;
        entries = entries - numberToRemove;
        return string.substring(startingFrom);
    }

    int howManySegments(String m3u8File) {
        int start = m3u8File.length();
        int numberOfFiles = 0;
        for (BigDecimal totalTime = new BigDecimal(0); totalTime.intValue() < pollTimeInSeconds
                .intValue(); numberOfFiles++) {
            start = m3u8File.lastIndexOf("#EXTINF:", start - 1);
            if (start != -1) {
                totalTime = totalTime.add(new BigDecimal(m3u8File.substring(start + 8, m3u8File.indexOf(",", start))));
            } else {
                logger.debug("Group did not find enough segments, lower the poll time if this message continues.");
                break;
            }
        }
        return numberOfFiles;
    }

    public void createPlayList() {
        String m3u8File = readCamerasPlaylist(cameraIndex);
        if (m3u8File == "") {
            return;
        }
        int numberOfSegments = howManySegments(m3u8File);
        logger.debug("Using {} segmented files to make up a poll period.", numberOfSegments);
        m3u8File = keepLast(m3u8File, numberOfSegments);
        m3u8File = m3u8File.replace("ipcamera", cameraIndex + "ipcamera"); // add index so we can then fetch output path
        if (entries > numberOfSegments * 3) {
            playingNow = removeFromStart(playingNow, entries - (numberOfSegments * 3));
        }
        playingNow = playingNow + "#EXT-X-DISCONTINUITY\n" + m3u8File;
        playList = "#EXTM3U\n#EXT-X-VERSION:6\n#EXT-X-TARGETDURATION:5\n#EXT-X-ALLOW-CACHE:NO\n#EXT-X-DISCONTINUITY-SEQUENCE:"
                + discontinuitySequence + "\n#EXT-X-MEDIA-SEQUENCE:" + mediaSequence + "\n" + playingNow;
    }

    private IpCameraGroupHandler getHandle() {
        return this;
    }

    @SuppressWarnings("null")
    public void startStreamServer(boolean start) {
        if (!start) {
            serversLoopGroup.shutdownGracefully(8, 8, TimeUnit.SECONDS);
            serverBootstrap = null;
        } else {
            if (serverBootstrap == null) {
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
                                    new StreamServerGroupHandler(getHandle()));
                        }
                    });
                    serverFuture = serverBootstrap.bind().sync();
                    serverFuture.await(4000);
                    logger.info("IpCamera file server for a group of cameras has started on port {} for all NIC's.",
                            serverPort);
                    updateState(CHANNEL_MJPEG_URL,
                            new StringType("http://" + hostIp + ":" + serverPort + "/ipcamera.mjpeg"));
                    updateState(CHANNEL_HLS_URL,
                            new StringType("http://" + hostIp + ":" + serverPort + "/ipcamera.m3u8"));
                    updateState(CHANNEL_IMAGE_URL,
                            new StringType("http://" + hostIp + ":" + serverPort + "/ipcamera.jpg"));
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Exception occured when starting the streaming server. Try changing the serverPort to another number.");
                }
            }
        }
    }

    void addCamera(String UniqueID) {
        if (groupTracker.listOfOnlineCameraUID.contains(UniqueID)) {
            for (IpCameraHandler handler : groupTracker.listOfOnlineCameraHandlers) {
                if (handler.getThing().getUID().getId().equals(UniqueID)) {
                    if (!cameraOrder.contains(handler)) {
                        logger.info("Adding {} to a camera group.", UniqueID);
                        if (hlsTurnedOn) {
                            logger.info("Starting HLS for the new camera.");
                            String channelPrefix = "ipcamera:" + handler.getThing().getThingTypeUID() + ":"
                                    + handler.getThing().getUID().getId() + ":";
                            handler.handleCommand(new ChannelUID(channelPrefix + CHANNEL_START_STREAM), OnOffType.ON);
                        }
                        cameraOrder.add(handler);
                    }
                }
            }
        }
    }

    // Event based. This is called as each camera comes online after the group handler is registered.
    public void cameraOnline(String uid) {
        logger.debug("New camera {} came online, checking if part of this group", uid);
        if (groupConfig.getFirstCamera().equals(uid)) {
            addCamera(uid);
        } else if (groupConfig.getSecondCamera().equals(uid)) {
            addCamera(uid);
        } else if (groupConfig.getThirdCamera().equals(uid)) {
            addCamera(uid);
        } else if (groupConfig.getForthCamera().equals(uid)) {
            addCamera(uid);
        }
    }

    // Event based. This is called as each camera comes online after the group handler is registered.
    public void cameraOffline(IpCameraHandler handle) {
        if (cameraOrder.remove(handle)) {
            logger.info("Camera {} went offline and was removed from a group.", handle.getThing().getUID().getId());
        }
    }

    boolean addIfOnline(String UniqueID) {
        if (groupTracker.listOfOnlineCameraUID.contains(UniqueID)) {
            addCamera(UniqueID);
            return true;
        }
        return false;
    }

    void createCameraOrder() {
        addIfOnline(groupConfig.getFirstCamera());
        addIfOnline(groupConfig.getSecondCamera());
        if (!groupConfig.getThirdCamera().isEmpty()) {
            addIfOnline(groupConfig.getThirdCamera());
        }
        if (!groupConfig.getForthCamera().isEmpty()) {
            addIfOnline(groupConfig.getForthCamera());
        }
        // Cameras can now send events of when they go on and offline.
        groupTracker.listOfGroupHandlers.add(this);
    }

    int checkForMotion(int nextCamerasIndex) {
        int checked = 0;
        for (int index = nextCamerasIndex; checked < cameraOrder.size(); checked++) {
            if (cameraOrder.get(index).motionDetected) {
                return index;
            }
            if (++index >= cameraOrder.size()) {
                index = 0;
            }
        }
        return nextCamerasIndex;
    }

    void pollCameraGroup() {
        if (cameraOrder.isEmpty()) {
            createCameraOrder();
        }
        if (++cameraIndex >= cameraOrder.size()) {
            cameraIndex = 0;
        }
        if (motionChangesOrder) {
            cameraIndex = checkForMotion(cameraIndex);
        }
        if (hlsTurnedOn) {
            discontinuitySequence++;
            createPlayList();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            switch (channelUID.getId()) {
                case CHANNEL_START_STREAM:
                    if (OnOffType.ON.equals(command)) {
                        hlsTurnedOn = true;
                        for (IpCameraHandler handler : cameraOrder) {
                            String channelPrefix = "ipcamera:" + handler.getThing().getThingTypeUID() + ":"
                                    + handler.getThing().getUID().getId() + ":";
                            handler.handleCommand(new ChannelUID(channelPrefix + CHANNEL_START_STREAM), OnOffType.ON);
                        }
                    } else {
                        // Do we turn all controls OFF, or do we remember the state before we turned them all on?
                        hlsTurnedOn = false;
                    }
            }
        }
    }

    @Override
    public void initialize() {
        groupConfig = getConfigAs(GroupConfig.class);
        serverPort = groupConfig.getServerPort();
        pollTimeInSeconds = new BigDecimal(groupConfig.getPollTime());
        pollTimeInSeconds = pollTimeInSeconds.divide(new BigDecimal(1000), 1, RoundingMode.HALF_UP);
        motionChangesOrder = groupConfig.getMotionChangesOrder();

        if (serverPort == -1) {
            logger.warn("The serverPort = -1 which disables a lot of features. See readme for more info.");
        } else if (serverPort < 1025) {
            logger.warn("The serverPort is <= 1024 and may cause permission errors under Linux, try a higher port.");
        }
        if (groupConfig.getServerPort() > 0) {
            startStreamServer(true);
        }
        updateStatus(ThingStatus.ONLINE);
        pollCameraGroupJob = pollCameraGroup.scheduleAtFixedRate(this::pollCameraGroup, 10000,
                groupConfig.getPollTime(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        startStreamServer(false);
        groupTracker.listOfGroupHandlers.remove(this);
        if (pollCameraGroupJob != null) {
            pollCameraGroupJob.cancel(true);
            pollCameraGroupJob = null;
        }
        cameraOrder.clear();
    }
}
