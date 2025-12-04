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
package org.openhab.binding.linkplay.internal.client.upnp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Helper class providing methods for executing UPnP commands defined in the LinkPlay SCPD specifications.
 * This class provides convenient wrapper methods for all UPnP actions supported by LinkPlay devices
 * across AVTransport, RenderingControl, ConnectionManager, PlayQueue, and QPlay services.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class LinkPlayUpnpCommands {

    // Service ID Constants
    public static final String SERVICE_AV_TRANSPORT = "AVTransport";
    public static final String SERVICE_RENDERING_CONTROL = "RenderingControl";
    public static final String SERVICE_CONNECTION_MANAGER = "ConnectionManager";
    public static final String SERVICE_PLAY_QUEUE = "PlayQueue";
    public static final String SERVICE_QPLAY = "QPlay";
    public static final String SERVICE_NAMESPACE = "wiimu-com";

    // Default instance ID used for most UPnP commands
    private static final String DEFAULT_INSTANCE_ID = "0";

    private final UpnpActionExecutor executor;

    /**
     * Interface for executing UPnP actions
     */
    public interface UpnpActionExecutor {
        CompletableFuture<Map<String, String>> executeAction(String serviceId, String actionId,
                @Nullable Map<String, String> inputs);

        CompletableFuture<Map<String, String>> executeAction(String namespace, String serviceId, String actionId,
                @Nullable Map<String, String> inputs);

        // void actions are commands that have no direct return value (fire and forget)
        CompletableFuture<Void> executeVoidAction(String serviceId, String actionId,
                @Nullable Map<String, String> inputs);

        CompletableFuture<Void> executeVoidAction(String namespace, String serviceId, String actionId,
                @Nullable Map<String, String> inputs);
    }

    public LinkPlayUpnpCommands(UpnpActionExecutor executor) {
        this.executor = executor;
    }

    // ==================== AVTransport Service Actions ====================

    /**
     * Get the current transport actions available
     */
    public CompletableFuture<Map<String, String>> getCurrentTransportActions() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeAction(SERVICE_AV_TRANSPORT, "GetCurrentTransportActions", inputs);
    }

    /**
     * Get device capabilities (supported media types)
     */
    public CompletableFuture<Map<String, String>> getDeviceCapabilities() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeAction(SERVICE_AV_TRANSPORT, "GetDeviceCapabilities", inputs);
    }

    /**
     * Get information about current media (URI, duration, tracks)
     */
    public CompletableFuture<Map<String, String>> getMediaInfo() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeAction(SERVICE_AV_TRANSPORT, "GetMediaInfo", inputs);
    }

    /**
     * Get position information (track, duration, time position)
     */
    public CompletableFuture<Map<String, String>> getPositionInfo() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeAction(SERVICE_AV_TRANSPORT, "GetPositionInfo", inputs);
    }

    /**
     * Get transport state (playing, paused, stopped)
     */
    public CompletableFuture<Map<String, String>> getTransportInfo() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeAction(SERVICE_AV_TRANSPORT, "GetTransportInfo", inputs);
    }

    /**
     * Get transport settings (play mode, recording quality)
     */
    public CompletableFuture<Map<String, String>> getTransportSettings() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeAction(SERVICE_AV_TRANSPORT, "GetTransportSettings", inputs);
    }

    /**
     * Skip to next track
     */
    public CompletableFuture<Void> next() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeVoidAction(SERVICE_AV_TRANSPORT, "Next", inputs);
    }

    /**
     * Pause playback
     */
    public CompletableFuture<Void> pause() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeVoidAction(SERVICE_AV_TRANSPORT, "Pause", inputs);
    }

    /**
     * Start or resume playback
     */
    public CompletableFuture<Void> play(String speed) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("Speed", speed);
        return executor.executeVoidAction(SERVICE_AV_TRANSPORT, "Play", inputs);
    }

    public CompletableFuture<Void> play() {
        return play("1");
    }

    /**
     * Skip to previous track
     */
    public CompletableFuture<Void> previous() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeVoidAction(SERVICE_AV_TRANSPORT, "Previous", inputs);
    }

    /**
     * Seek to position in track by seconds
     *
     * @param seconds Target position in seconds
     */
    public CompletableFuture<Void> seek(int seconds) {
        String target = secondsToHhMmSs(seconds);
        return seek("REL_TIME", target);
    }

    /**
     * Seek to position in track
     *
     * @param unit Seek mode: "REL_TIME" for time-based, "TRACK_NR" for track number
     * @param target Target position (e.g., "00:01:30" for time, "3" for track number)
     */
    public CompletableFuture<Void> seek(String unit, String target) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("Unit", unit);
        inputs.put("Target", target);
        return executor.executeVoidAction(SERVICE_AV_TRANSPORT, "Seek", inputs);
    }

    /**
     * Set the current transport URI
     *
     * @param currentUri URI to play
     * @param currentUriMetaData DIDL-Lite metadata for the URI
     */
    public CompletableFuture<Void> setAvTransportUri(String currentUri, String currentUriMetaData) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("CurrentURI", currentUri);
        inputs.put("CurrentURIMetaData", currentUriMetaData);
        return executor.executeVoidAction(SERVICE_AV_TRANSPORT, "SetAVTransportURI", inputs);
    }

    /**
     * Stop playback
     */
    public CompletableFuture<Void> stop() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeVoidAction(SERVICE_AV_TRANSPORT, "Stop", inputs);
    }

    /**
     * Set the next transport URI (for gapless playback)
     */
    public CompletableFuture<Void> setNextAvTransportUri(String nextUri, String nextUriMetaData) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("NextURI", nextUri);
        inputs.put("NextURIMetaData", nextUriMetaData);
        return executor.executeVoidAction(SERVICE_AV_TRANSPORT, "SetNextAVTransportURI", inputs);
    }

    /**
     * Get the play type
     */
    public CompletableFuture<Map<String, String>> getPlayType() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeAction(SERVICE_AV_TRANSPORT, "GetPlayType", inputs);
    }

    /**
     * Get extended information (combines transport, position, volume, group info, etc.)
     */
    public CompletableFuture<Map<String, String>> getInfoEx() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeAction(SERVICE_AV_TRANSPORT, "GetInfoEx", inputs);
    }

    /**
     * Set play mode (normal, repeat one, repeat all, shuffle)
     *
     * @param playMode Play mode: "NORMAL", "REPEAT_ONE", "REPEAT_ALL", "SHUFFLE"
     */
    public CompletableFuture<Void> setPlayMode(String playMode) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("NewPlayMode", playMode);
        return executor.executeVoidAction(SERVICE_AV_TRANSPORT, "SetPlayMode", inputs);
    }

    /**
     * Fast forward
     */

    public CompletableFuture<Void> seekForward() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeVoidAction(SERVICE_AV_TRANSPORT, "SeekForward", inputs);
    }

    /**
     * Rewind
     */
    public CompletableFuture<Void> seekBackward() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeVoidAction(SERVICE_AV_TRANSPORT, "SeekBackward", inputs);
    }

    // ==================== RenderingControl Service Actions ====================

    /**
     * Get mute state
     */
    public CompletableFuture<Map<String, String>> getMute(String channel) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("Channel", channel);
        return executor.executeAction(SERVICE_RENDERING_CONTROL, "GetMute", inputs);
    }

    /**
     * Set mute state
     *
     * @param desiredMute "1" to mute, "0" to unmute
     */
    public CompletableFuture<Void> setMute(String channel, String desiredMute) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("Channel", channel);
        inputs.put("DesiredMute", desiredMute);
        return executor.executeVoidAction(SERVICE_RENDERING_CONTROL, "SetMute", inputs);
    }

    /**
     * Get volume level
     */
    public CompletableFuture<Map<String, String>> getVolume(String channel) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("Channel", channel);
        return executor.executeAction(SERVICE_RENDERING_CONTROL, "GetVolume", inputs);
    }

    /**
     * Set volume level
     *
     * @param desiredVolume Volume level (0-100)
     */
    public CompletableFuture<Void> setVolume(String channel, String desiredVolume) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("Channel", channel);
        inputs.put("DesiredVolume", desiredVolume);
        return executor.executeVoidAction(SERVICE_RENDERING_CONTROL, "SetVolume", inputs);
    }

    /**
     * Get current channel
     */
    public CompletableFuture<Map<String, String>> getChannel(String channel) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("Channel", channel);
        return executor.executeAction(SERVICE_RENDERING_CONTROL, "GetChannel", inputs);
    }

    /**
     * Set channel
     */
    public CompletableFuture<Void> setChannel(String channel, String desiredChannel) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("Channel", channel);
        inputs.put("DesiredChannel", desiredChannel);
        return executor.executeVoidAction(SERVICE_RENDERING_CONTROL, "SetChannel", inputs);
    }

    /**
     * Get equalizer settings
     */
    public CompletableFuture<Map<String, String>> getEqualizer(String channel) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("Channel", channel);
        return executor.executeAction(SERVICE_RENDERING_CONTROL, "GetEqualizer", inputs);
    }

    /**
     * Set equalizer settings
     */
    public CompletableFuture<Void> setEqualizer(String channel, String desiredEqualizer) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("Channel", channel);
        inputs.put("DesiredEqualizer", desiredEqualizer);
        return executor.executeVoidAction(SERVICE_RENDERING_CONTROL, "SetEqualizer", inputs);
    }

    /**
     * Get stream services capability
     */
    public CompletableFuture<Map<String, String>> streamServicesCapability(String appVersion) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("AppVersion", appVersion);
        return executor.executeAction(SERVICE_RENDERING_CONTROL, "StreamServicesCapability", inputs);
    }

    /**
     * Set stream services capability
     */
    public CompletableFuture<Void> setStreamServicesCapability(String command) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("Command", command);
        return executor.executeVoidAction(SERVICE_RENDERING_CONTROL, "SetStreamServicesCapability", inputs);
    }

    /**
     * List available presets
     */
    public CompletableFuture<Map<String, String>> listPresets() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeAction(SERVICE_RENDERING_CONTROL, "ListPresets", inputs);
    }

    /**
     * Select a preset
     *
     * @param presetName Preset name: "FactoryDefaults" or "InstallationDefaults"
     */
    public CompletableFuture<Void> selectPreset(String presetName) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("PresetName", presetName);
        return executor.executeVoidAction(SERVICE_RENDERING_CONTROL, "SelectPreset", inputs);
    }

    /**
     * Get simple device information
     */
    public CompletableFuture<Map<String, String>> getSimpleDeviceInfo() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeAction(SERVICE_RENDERING_CONTROL, "GetSimpleDeviceInfo", inputs);
    }

    /**
     * Get control device information
     */
    public CompletableFuture<Map<String, String>> getControlDeviceInfo() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeAction(SERVICE_RENDERING_CONTROL, "GetControlDeviceInfo", inputs);
    }

    /**
     * Join a multiroom group
     *
     * @param masterInfo Information about the master device
     */
    public CompletableFuture<Void> multiRoomJoinGroup(String masterInfo) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        inputs.put("MasterInfo", masterInfo);
        return executor.executeVoidAction(SERVICE_RENDERING_CONTROL, "MultiRoomJoinGroup", inputs);
    }

    /**
     * Leave a multiroom group
     */
    public CompletableFuture<Void> multiRoomLeaveGroup() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", DEFAULT_INSTANCE_ID);
        return executor.executeVoidAction(SERVICE_RENDERING_CONTROL, "MultiRoomLeaveGroup", inputs);
    }

    /**
     * Set alarm queue
     */
    public CompletableFuture<Void> setAlarmQueue(String alarmContext) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("AlarmContext", alarmContext);
        return executor.executeVoidAction(SERVICE_RENDERING_CONTROL, "SetAlarmQueue", inputs);
    }

    /**
     * Get alarm queue
     */
    public CompletableFuture<Map<String, String>> getAlarmQueue(String alarmName) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("AlarmName", alarmName);
        return executor.executeAction(SERVICE_RENDERING_CONTROL, "GetAlarmQueue", inputs);
    }

    /**
     * Delete alarm queue
     */
    public CompletableFuture<Void> deleteAlarmQueue(String alarmName) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("AlarmName", alarmName);
        return executor.executeVoidAction(SERVICE_RENDERING_CONTROL, "DeleteAlarmQueue", inputs);
    }

    /**
     * Set device name
     */
    public CompletableFuture<Void> setDeviceName(String name) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("Name", name);
        return executor.executeVoidAction(SERVICE_RENDERING_CONTROL, "SetDeviceName", inputs);
    }

    /**
     * Set AirPlay auto sync delay
     *
     * @param command "Start" or "Stop"
     */
    public CompletableFuture<Void> airplayAutoSyncDelay(String command) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("Name", command);
        return executor.executeVoidAction(SERVICE_RENDERING_CONTROL, "AirplayAutoSyncDelay", inputs);
    }

    // ==================== ConnectionManager Service Actions ====================

    /**
     * Get current connection IDs
     */
    public CompletableFuture<Map<String, String>> getCurrentConnectionIds() {
        return executor.executeAction(SERVICE_CONNECTION_MANAGER, "GetCurrentConnectionIDs", null);
    }

    /**
     * Get information about a connection
     */
    public CompletableFuture<Map<String, String>> getCurrentConnectionInfo(String connectionId) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("ConnectionID", connectionId);
        return executor.executeAction(SERVICE_CONNECTION_MANAGER, "GetCurrentConnectionInfo", inputs);
    }

    /**
     * Get protocol information (supported formats)
     */
    public CompletableFuture<Map<String, String>> getProtocolInfo() {
        return executor.executeAction(SERVICE_CONNECTION_MANAGER, "GetProtocolInfo", null);
    }

    // ==================== PlayQueue Service Actions ====================

    /**
     * Create a new queue
     */
    public CompletableFuture<Void> createQueue(String queueContext) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueContext", queueContext);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "CreateQueue", inputs);
    }

    /**
     * Replace existing queue
     */
    public CompletableFuture<Void> replaceQueue(String queueContext) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueContext", queueContext);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "ReplaceQueue", inputs);
    }

    /**
     * Delete a queue
     */
    public CompletableFuture<Void> deleteQueue(String queueName) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueName", queueName);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "DeleteQueue", inputs);
    }

    /**
     * Backup queue
     */
    public CompletableFuture<Void> backUpQueue(String queueContext) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueContext", queueContext);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "BackUpQueue", inputs);
    }

    /**
     * Append to queue
     */
    public CompletableFuture<Void> appendQueue(String queueContext) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueContext", queueContext);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "AppendQueue", inputs);
    }

    /**
     * Browse queue contents
     */
    public CompletableFuture<Map<String, String>> browseQueue(String queueName) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueName", queueName);
        return executor.executeAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "BrowseQueue", inputs);
    }

    /**
     * Set queue loop mode
     */
    public CompletableFuture<Void> setQueueLoopMode(String loopMode) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("LoopMode", loopMode);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "SetQueueLoopMode", inputs);
    }

    /**
     * Get queue loop mode
     */
    public CompletableFuture<Map<String, String>> getQueueLoopMode() {
        return executor.executeAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "GetQueueLoopMode", null);
    }

    /**
     * Set queue policy
     */
    public CompletableFuture<Void> setQueuePolicy(String queueName) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueName", queueName);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "SetQueuePolicy", inputs);
    }

    /**
     * Play queue starting at index
     */
    public CompletableFuture<Void> playQueueWithIndex(String queueName, String index) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueName", queueName);
        inputs.put("Index", index);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "PlayQueueWithIndex", inputs);
    }

    /**
     * Append tracks to queue
     */
    public CompletableFuture<Void> appendTracksInQueue(String queueContext) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueContext", queueContext);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "AppendTracksInQueue", inputs);
    }

    /**
     * Remove tracks from queue
     */
    public CompletableFuture<Void> removeTracksInQueue(String queueName, String rangeStart, String rangeEnd,
            String action) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueName", queueName);
        inputs.put("RangStart", rangeStart);
        inputs.put("RangEnd", rangeEnd);
        inputs.put("Action", action);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "RemoveTracksInQueue", inputs);
    }

    /**
     * Move tracks within queue
     */
    public CompletableFuture<Void> moveTracksInQueue(String queueName, String indexList, String toIndex) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueName", queueName);
        inputs.put("IndexList", indexList);
        inputs.put("ToIndex", toIndex);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "MoveTracksInQueue", inputs);
    }

    /**
     * Append tracks to queue (extended)
     */
    public CompletableFuture<Void> appendTracksInQueueEx(String queueContext, String direction, String startIndex,
            String play, String action) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueContext", queueContext);
        inputs.put("Direction", direction);
        inputs.put("StartIndex", startIndex);
        inputs.put("Play", play);
        inputs.put("Action", action);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "AppendTracksInQueueEx", inputs);
    }

    /**
     * Set key mapping
     */
    public CompletableFuture<Void> setKeyMapping(String queueContext) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueContext", queueContext);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "SetKeyMapping", inputs);
    }

    /**
     * Get key mapping
     */
    public CompletableFuture<Map<String, String>> getKeyMapping() {
        return executor.executeAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "GetKeyMapping", null);
    }

    /**
     * Get online queue
     */
    public CompletableFuture<Map<String, String>> getQueueOnline(String queueName, String queueId, String queueType,
            String queueLimit, String queueAutoInsert) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueName", queueName);
        inputs.put("QueueID", queueId);
        inputs.put("QueueType", queueType);
        inputs.put("Queuelimit", queueLimit);
        inputs.put("QueueAutoInsert", queueAutoInsert);
        return executor.executeAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "GetQueueOnline", inputs);
    }

    /**
     * Search online queue
     */
    public CompletableFuture<Map<String, String>> searchQueueOnline(String queueName, String searchKey,
            String queueLimit) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueName", queueName);
        inputs.put("SearchKey", searchKey);
        inputs.put("Queuelimit", queueLimit);
        return executor.executeAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "SearchQueueOnline", inputs);
    }

    /**
     * Set queue record
     */
    public CompletableFuture<Void> setQueueRecord(String queueName, String queueId, String action) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueName", queueName);
        inputs.put("QueueID", queueId);
        inputs.put("Action", action);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "SetQueueRecord", inputs);
    }

    /**
     * Set songs record
     */
    public CompletableFuture<Void> setSongsRecord(String queueName, String songId, String action) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueName", queueName);
        inputs.put("SongID", songId);
        inputs.put("Action", action);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "SetSongsRecord", inputs);
    }

    /**
     * Register user
     */
    public CompletableFuture<Void> userRegister(String queueName, String userName, String password) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueName", queueName);
        inputs.put("UserName", userName);
        inputs.put("PassWord", password);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "UserRegister", inputs);
    }

    /**
     * Login user
     */
    public CompletableFuture<Void> userLogin(String accountSource, String userName, String password, String savePass,
            String code, String proxy) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("AccountSource", accountSource);
        inputs.put("UserName", userName);
        inputs.put("PassWord", password);
        inputs.put("SavePass", savePass);
        inputs.put("Code", code);
        inputs.put("Proxy", proxy);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "UserLogin", inputs);
    }

    /**
     * Logout user
     */
    public CompletableFuture<Void> userLogout(String accountSource) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("AccountSource", accountSource);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "UserLogout", inputs);
    }

    /**
     * Get user information
     */
    public CompletableFuture<Map<String, String>> getUserInfo(String accountSource) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("AccountSource", accountSource);
        return executor.executeAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "GetUserInfo", inputs);
    }

    /**
     * Get user account history
     */
    public CompletableFuture<Map<String, String>> getUserAccountHistory(String accountSource, String number) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("AccountSource", accountSource);
        inputs.put("Number", number);
        return executor.executeAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "GetUserAccountHistory", inputs);
    }

    /**
     * Set user favorites
     */
    public CompletableFuture<Void> setUserFavorites(String accountSource, String action, String mediaType,
            String mediaId, String mediaContext) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("AccountSource", accountSource);
        inputs.put("Action", action);
        inputs.put("MediaType", mediaType);
        inputs.put("MediaID", mediaId);
        inputs.put("MediaContext", mediaContext);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "SetUserFavorites", inputs);
    }

    /**
     * Get user favorites
     */
    public CompletableFuture<Map<String, String>> getUserFavorites(String accountSource, String mediaType,
            String filter) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("AccountSource", accountSource);
        inputs.put("MediaType", mediaType);
        inputs.put("Filter", filter);
        return executor.executeAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "GetUserFavorites", inputs);
    }

    /**
     * Get queue index
     */
    public CompletableFuture<Map<String, String>> getQueueIndex(String queueName) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueName", queueName);
        return executor.executeAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "GetQueueIndex", inputs);
    }

    /**
     * Set Spotify preset
     */
    public CompletableFuture<Void> setSpotifyPreset(String keyIndex) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("KeyIndex", keyIndex);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "SetSpotifyPreset", inputs);
    }

    /**
     * Delete action queue
     */
    public CompletableFuture<Void> deleteActionQueue(String pressType) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("PressType", pressType);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "DeleteActionQueue", inputs);
    }

    /**
     * Get basic user info
     */
    public CompletableFuture<Map<String, String>> getBasicUserInfo() {
        return executor.executeAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "GetBasicUserInfo", null);
    }

    /**
     * Take play control
     */
    public CompletableFuture<Void> takePlayControl() {
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "TakePlayControl", null);
    }

    /**
     * Set rating
     */
    public CompletableFuture<Void> setRating(String source, String trackId, String rating) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("Source", source);
        inputs.put("TrackID", trackId);
        inputs.put("Rating", rating);
        return executor.executeVoidAction(SERVICE_NAMESPACE, SERVICE_PLAY_QUEUE, "SetRating", inputs);
    }

    // ==================== QPlay Service Actions ====================

    /**
     * Insert tracks into QPlay queue
     */
    public CompletableFuture<Void> insertTracks(String queueId, String startingIndex, String tracksMetaData) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueID", queueId);
        inputs.put("StartingIndex", startingIndex);
        inputs.put("TracksMetaData", tracksMetaData);
        return executor.executeVoidAction(SERVICE_QPLAY, "InsertTracks", inputs);
    }

    /**
     * Remove tracks from QPlay queue
     */
    public CompletableFuture<Void> removeTracks(String queueId, String startingIndex, String numberOfTracks) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueID", queueId);
        inputs.put("StartingIndex", startingIndex);
        inputs.put("NumberOfTracks", numberOfTracks);
        return executor.executeVoidAction(SERVICE_QPLAY, "RemoveTracks", inputs);
    }

    /**
     * Remove all tracks from QPlay queue
     */
    public CompletableFuture<Void> removeAllTracks(String queueId) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueID", queueId);
        return executor.executeVoidAction(SERVICE_QPLAY, "RemoveAllTracks", inputs);
    }

    /**
     * Get tracks info from QPlay queue
     */
    public CompletableFuture<Map<String, String>> getTracksInfo(String startingIndex, String numberOfTracks) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("StartingIndex", startingIndex);
        inputs.put("NumberOfTracks", numberOfTracks);
        return executor.executeAction(SERVICE_QPLAY, "GetTracksInfo", inputs);
    }

    /**
     * Set tracks info in QPlay queue
     */
    public CompletableFuture<Void> setTracksInfo(String queueId, String startingIndex, String nextIndex,
            String tracksMetaData) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("QueueID", queueId);
        inputs.put("StartingIndex", startingIndex);
        inputs.put("NextIndex", nextIndex);
        inputs.put("TracksMetaData", tracksMetaData);
        return executor.executeVoidAction(SERVICE_QPLAY, "SetTracksInfo", inputs);
    }

    /**
     * Get tracks count in QPlay queue
     */
    public CompletableFuture<Map<String, String>> getTracksCount() {
        return executor.executeAction(SERVICE_QPLAY, "GetTracksCount", null);
    }

    /**
     * Get maximum tracks supported
     */
    public CompletableFuture<Map<String, String>> getMaxTracks() {
        return executor.executeAction(SERVICE_QPLAY, "GetMaxTracks", null);
    }

    /**
     * QPlay authentication
     */
    public CompletableFuture<Map<String, String>> qPlayAuth(String seed) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("Seed", seed);
        return executor.executeAction(SERVICE_QPLAY, "QPlayAuth", inputs);
    }

    /**
     * Set network configuration
     *
     * @param ssid Network SSID
     * @param key Network password
     * @param authAlgo Authentication algorithm: "open", "shared", "WPA", "WPAPSK", "WPA2", "WPA2PSK"
     * @param cipherAlgo Cipher algorithm: "none", "WEP", "TKIP", "AES"
     */
    public CompletableFuture<Void> setNetwork(String ssid, String key, String authAlgo, String cipherAlgo) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("SSID", ssid);
        inputs.put("Key", key);
        inputs.put("AuthAlgo", authAlgo);
        inputs.put("CipherAlgo", cipherAlgo);
        return executor.executeVoidAction(SERVICE_QPLAY, "SetNetwork", inputs);
    }

    // ==================== Utility Methods ====================

    /**
     * Convert seconds to HH:MM:SS string (REL_TIME format).
     *
     * @param seconds non-negative number of seconds
     * @return formatted time in HH:MM:SS
     */
    public static String secondsToHhMmSs(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("seconds must be non-negative");
        }
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * Parse HH:MM:SS (REL_TIME format) to total seconds.
     *
     * @param time time string in HH:MM:SS
     * @return total seconds
     */
    public static int hhMmSsToSeconds(String time) throws IllegalArgumentException {
        String[] parts = time.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("time must be in HH:MM:SS format");
        }
        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            if (hours < 0 || minutes < 0 || minutes > 59 || seconds < 0 || seconds > 59) {
                throw new IllegalArgumentException("Invalid HH:MM:SS values");
            }
            return (hours * 3600) + (minutes * 60) + seconds;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("time must be numeric HH:MM:SS", e);
        }
    }
}
