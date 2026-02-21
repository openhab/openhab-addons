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
package org.openhab.binding.linkplay.internal.handler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linkplay.internal.client.upnp.LinkPlayUpnpClient;
import org.openhab.binding.linkplay.internal.client.upnp.LinkPlayUpnpCommands;
import org.openhab.binding.linkplay.internal.client.upnp.PlayList;
import org.openhab.binding.linkplay.internal.client.upnp.PlayListInfo;
import org.openhab.binding.linkplay.internal.client.upnp.PlayQueue;
import org.openhab.binding.linkplay.internal.client.upnp.TransportState;
import org.openhab.binding.linkplay.internal.client.upnp.UpnpValueListener;
import org.openhab.binding.linkplay.internal.client.upnp.UpnpXMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles notification playback for LinkPlay devices.
 * Manages notification queuing, playback, and restoration of previous playback state.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class LinkPlayNotificationHandler {

    private final Logger logger = LoggerFactory.getLogger(LinkPlayNotificationHandler.class);
    private static final String SERVICE_AV_TRANSPORT = "AVTransport";

    private final LinkPlayHandler handler;
    private final LinkPlayUpnpClient upnpClient;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean inNotification = new AtomicBoolean(false);

    private @Nullable ScheduledFuture<?> notificationTimeoutJob;

    public LinkPlayNotificationHandler(LinkPlayHandler handler, LinkPlayUpnpClient upnpClient,
            ScheduledExecutorService scheduler) {
        this.handler = handler;
        this.upnpClient = upnpClient;
        this.scheduler = scheduler;
    }

    public void dispose() {
        cancelNotificationTimeoutJob();
        inNotification.set(false);
    }

    public boolean isInNotification() {
        return inNotification.get();
    }

    /**
     * Play a notification on the device.
     * If the device is currently in a playlist, the playlist will be paused while the notification is playing, and then
     * resumed after the notification playback is complete.
     * If the device is not currently in a playlist, the notification will be played as a single track.
     *
     * @param url The URL of the notification to play
     * @return A future that completes when the notification playback is complete
     */
    public CompletableFuture<@Nullable Void> playNotification(String url) {
        if (inNotification.compareAndExchange(false, true)) {
            logger.debug("{}: Notification already in progress", handler.getUdn());
            return CompletableFuture.failedFuture(new IllegalStateException("Notification already in progress"));
        }

        final CompletableFuture<@Nullable Void> returnFuture = new CompletableFuture<@Nullable Void>();
        logger.debug("{}: Playing notification: {}", handler.getUdn(), url);

        // Cleanup when the notification playback is complete
        returnFuture.whenComplete((result, throwable) -> {
            if (throwable == null) {
                logger.debug("{}: Notification Playback Complete", handler.getUdn());
            } else {
                logger.debug("{}: Notification Playback ended with error: {}", handler.getUdn(),
                        throwable.getMessage());
            }
            // Ensure timeout is cancelled even if completion happened exceptionally
            cancelNotificationTimeoutJob();
            inNotification.set(false);
        });

        cancelNotificationTimeoutJob();
        // Add timeout to prevent hanging forever
        synchronized (this) {
            notificationTimeoutJob = scheduler.schedule(() -> {
                synchronized (LinkPlayNotificationHandler.this) {
                    if (!returnFuture.isDone()) {
                        logger.debug("{}: Notification timeout after 30 seconds", handler.getUdn());
                        returnFuture.completeExceptionally(
                                new TimeoutException("Notification playback did not start within 30 seconds"));
                    }
                }
            }, 30, TimeUnit.SECONDS);
        }

        try {
            PlayQueue playQueue = upnpClient.getPlayListQueue();
            if (playQueue != null && !playQueue.getCurrentPlayListName().isBlank()) {
                // We have an active playlist - append the notification track and play it
                playNotificationWithPlaylist(url, playQueue, returnFuture);
            } else {
                // No active playlist - just play the notification directly
                playNotificationDirect(url, returnFuture);
            }
        } catch (ExecutionException | InterruptedException e) {
            logger.error("{}: Error while playing notification: {}", handler.getUdn(), e.getMessage(), e);
            synchronized (this) {
                cancelNotificationTimeoutJob();
                inNotification.set(false);
                if (!returnFuture.isDone()) {
                    returnFuture.completeExceptionally(e);
                }
            }
        }
        return returnFuture;
    }

    private void playNotificationWithPlaylist(String url, PlayQueue playQueue,
            CompletableFuture<@Nullable Void> returnFuture) throws ExecutionException, InterruptedException {
        String queueName = playQueue.getCurrentPlayListName();
        LinkPlayUpnpCommands commands = upnpClient.getCommands();
        Map<String, String> q = commands.browseQueue(queueName).get();
        final String queueContext = q.get("QueueContext");

        if (queueContext == null || queueContext.isEmpty()) {
            synchronized (this) {
                cancelNotificationTimeoutJob();
                inNotification.set(false);
                if (!returnFuture.isDone()) {
                    returnFuture.completeExceptionally(new IllegalStateException("QueueContext is null or empty"));
                }
            }
            return;
        }

        // Parse the queue to get current track count
        PlayList playList = UpnpXMLParser.getPlayListFromBrowseQueueResponse(queueContext);
        PlayListInfo listInfo = playList != null ? playList.getListInfo() : null;
        if (playList == null || listInfo == null) {
            synchronized (this) {
                cancelNotificationTimeoutJob();
                inNotification.set(false);
                if (!returnFuture.isDone()) {
                    returnFuture.completeExceptionally(new IllegalStateException("Unable to parse playlist"));
                }
            }
            return;
        }

        int currentTrackCount = listInfo.getTrackNumber();
        int notificationIndex = currentTrackCount + 1;

        // Create a simple playlist XML with just the notification track
        String notificationTrackXml = UpnpXMLParser.createSimplePlayListXml(url, queueName);

        // Append the notification track to the current queue
        commands.appendTracksInQueue(notificationTrackXml).get();

        // Listen for playback state to know when notification starts and finishes
        AtomicBoolean notificationStarted = new AtomicBoolean(false);
        AtomicBoolean notificationPlayed = new AtomicBoolean(false);

        final UpnpValueListener listener = (variable, value, service) -> {
            if (SERVICE_AV_TRANSPORT.equals(service) && value != null) {
                Map<String, String> avt = UpnpXMLParser.getAVTransportFromXML(value);
                String currentTrackStr = avt.get("CurrentTrack");
                TransportState transportState = TransportState.fromString(avt.get("TransportState"));

                // Parse current track index if available
                Integer currentTrack = null;
                if (currentTrackStr != null) {
                    try {
                        currentTrack = Integer.parseInt(currentTrackStr);
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }

                // We just played the notification, so if we get PLAYING state, assume it started
                if (!notificationStarted.get() && transportState == TransportState.PLAYING) {
                    notificationStarted.set(true);
                    logger.debug("{}: Notification started playing at index {}", handler.getUdn(), notificationIndex);
                    cancelNotificationTimeoutJob();
                } else if (notificationStarted.get() && currentTrack != null && currentTrack != notificationIndex
                        && !notificationPlayed.getAndSet(true)) {
                    logger.debug("{}: Notification finished, playlist continuing to track {}", handler.getUdn(),
                            currentTrack);
                    synchronized (LinkPlayNotificationHandler.this) {
                        cancelNotificationTimeoutJob();
                        // Clear notification flag BEFORE completing so metadata events get processed
                        inNotification.set(false);
                        if (!returnFuture.isDone()) {
                            returnFuture.complete(null);
                        }
                    }
                }
            }
        };

        returnFuture.whenComplete((result, throwable) -> {
            upnpClient.unregisterUpnpValueListener(listener);
        });
        upnpClient.registerUpnpValueListener(listener);

        // Play the notification track
        commands.playQueueWithIndex(queueName, String.valueOf(notificationIndex)).get();
    }

    private void playNotificationDirect(String url, CompletableFuture<@Nullable Void> returnFuture)
            throws ExecutionException, InterruptedException {
        // No active playlist - just play the notification directly
        String didl = UpnpXMLParser.createNotificationMetadataForUri(url, "Notification");
        LinkPlayUpnpCommands commands = upnpClient.getCommands();
        commands.setAvTransportUri(url, didl).get();

        AtomicBoolean started = new AtomicBoolean(false);
        final UpnpValueListener listener = (variable, value, service) -> {
            if (SERVICE_AV_TRANSPORT.equals(service) && value != null) {
                Map<String, String> avt = UpnpXMLParser.getAVTransportFromXML(value);
                TransportState transportState = TransportState.fromString(avt.get("TransportState"));

                if (transportState == TransportState.PLAYING) {
                    if (!started.getAndSet(true)) {
                        logger.debug("{}: Notification Playback started", handler.getUdn());
                        cancelNotificationTimeoutJob();
                    }
                } else if (transportState == TransportState.STOPPED && started.get()) {
                    logger.debug("{}: Notification Playback stopped", handler.getUdn());
                    synchronized (LinkPlayNotificationHandler.this) {
                        cancelNotificationTimeoutJob();
                        inNotification.set(false);
                        if (!returnFuture.isDone()) {
                            returnFuture.complete(null);
                        }
                    }
                }
            }
        };

        returnFuture.whenComplete((result, throwable) -> {
            upnpClient.unregisterUpnpValueListener(listener);
        });
        upnpClient.registerUpnpValueListener(listener);
        commands.play().get();
    }

    private synchronized void cancelNotificationTimeoutJob() {
        ScheduledFuture<?> job = notificationTimeoutJob;
        if (job != null) {
            job.cancel(true);
            notificationTimeoutJob = null;
        }
    }
}
