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
package org.openhab.binding.linkplay.internal.client.http;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.linkplay.internal.client.http.adaptors.BtPairStatusAdapter;
import org.openhab.binding.linkplay.internal.client.http.adaptors.PlayerStatusAdapter;
import org.openhab.binding.linkplay.internal.client.http.dto.AlarmClockInfo;
import org.openhab.binding.linkplay.internal.client.http.dto.AudioOutputHardwareMode;
import org.openhab.binding.linkplay.internal.client.http.dto.BTPairStatus;
import org.openhab.binding.linkplay.internal.client.http.dto.BluetoothDeviceList;
import org.openhab.binding.linkplay.internal.client.http.dto.DeviceStatus;
import org.openhab.binding.linkplay.internal.client.http.dto.EQBandResponse;
import org.openhab.binding.linkplay.internal.client.http.dto.EQStatResponse;
import org.openhab.binding.linkplay.internal.client.http.dto.PlayerStatus;
import org.openhab.binding.linkplay.internal.client.http.dto.PresetList;
import org.openhab.binding.linkplay.internal.client.http.dto.SlaveListResponse;
import org.openhab.binding.linkplay.internal.client.http.dto.SourceInputMode;
import org.openhab.binding.linkplay.internal.client.http.dto.StaticIpInfo;
import org.openhab.binding.linkplay.internal.client.http.dto.StatusResponse;
import org.openhab.binding.linkplay.internal.client.http.dto.TrackMetadata;
import org.openhab.binding.linkplay.internal.client.http.dto.WlanConnectState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * HTTP client for LinkPlay devices.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class LinkPlayHTTPClient {

    private final HttpClient httpClient;
    private String host = "";
    private int port = 0;
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(PlayerStatus.class, new PlayerStatusAdapter())
            .registerTypeAdapter(BTPairStatus.class, new BtPairStatusAdapter()).create();
    private final Logger logger = LoggerFactory.getLogger(LinkPlayHTTPClient.class);

    public LinkPlayHTTPClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public LinkPlayHTTPClient(HttpClient httpClient, String host, int port) {
        this.httpClient = httpClient;
        this.host = host;
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Command: getStatusEx – Retrieves detailed information about the device.
     */
    public CompletableFuture<DeviceStatus> getStatusEx() {
        return sendGetRequest("getStatusEx", DeviceStatus.class);
    }

    /**
     * Command: getMetaInfo – Retrieves current track metadata.
     */
    public CompletableFuture<TrackMetadata> getMetaInfo() {
        return sendGetRequest("getMetaInfo", TrackMetadata.class);
    }

    /**
     * Command: getPlayerStatus – Retrieves current playback status.
     */
    public CompletableFuture<PlayerStatus> getPlayerStatus() {
        return sendGetRequest("getPlayerStatus", PlayerStatus.class);
    }

    /**
     * Command: setPlayerCmd:hex_playlist:url:{index} – Play a specific track in a hex-encoded playlist.
     *
     * @param index playlist index to start from
     * @return a CompletableFuture with the raw response (typically "OK")
     */
    public CompletableFuture<String> setPlayerCmdHexPlaylistUrl(String index) {
        return sendGetRequest("setPlayerCmd:hex_playlist:url:" + index, String.class);
    }

    /**
     * Command: setPlayerCmd:pause – Pause playback.
     */
    public CompletableFuture<String> setPlayerCmdPause() {
        return sendGetRequest("setPlayerCmd:pause", String.class);
    }

    /**
     * Command: setPlayerCmd:resume – Resume playback.
     */
    public CompletableFuture<String> setPlayerCmdResume() {
        return sendGetRequest("setPlayerCmd:resume", String.class);
    }

    /**
     * Command: setPlayerCmd:onepause – Toggle pause/resume depending on current state.
     */
    public CompletableFuture<String> setPlayerCmdOnePause() {
        return sendGetRequest("setPlayerCmd:onepause", String.class);
    }

    /**
     * Command: setPlayerCmd:play:{url} – Play an audio stream by URL.
     *
     * @param url Stream URL to play (will be URL-encoded before sending).
     */
    public CompletableFuture<String> setPlayerCmdPlayUrl(String url) {
        return sendGetRequest("setPlayerCmd:play:" + encode(url), String.class);
    }

    /**
     * Command: setPlayerCmd:playlist:{url}:{index} – Play an audio playlist and start at a given index.
     *
     * @param url Playlist URL (m3u, ASX, etc.) to be URL-encoded.
     * @param index Start index inside the playlist (use "0" for first item)
     */
    public CompletableFuture<String> setPlayerCmdPlaylistUrl(String url, String index) {
        return sendGetRequest("setPlayerCmd:playlist:" + encode(url) + ":" + index, String.class);
    }

    /**
     * Command: setPlayerCmd:playPromptUrl:{url} – Play a prompt URL.
     * The device will lower current volume of playback (NETWORK or USB mode only), and play the url for notification
     * sound. Normally used in condition for a door bell in home automation system.
     *
     * @param url Prompt URL (will be URL-encoded before sending).
     */
    public CompletableFuture<String> playPromptUrl(String url) {
        return sendGetRequest("playPromptUrl:" + encode(url), String.class);
    }

    /**
     * Command: setPlayerCmd:prev – Skip to previous item.
     */
    public CompletableFuture<String> setPlayerCmdPrev() {
        return sendGetRequest("setPlayerCmd:prev", String.class);
    }

    /**
     * Command: setPlayerCmd:next – Skip to next item.
     */
    public CompletableFuture<String> setPlayerCmdNext() {
        return sendGetRequest("setPlayerCmd:next", String.class);
    }

    /**
     * Command: setPlayerCmd:seek:{position} – Seek to an absolute position (seconds).
     *
     * @param position target position in seconds (0 – duration)
     */
    public CompletableFuture<String> setPlayerCmdSeekPosition(int position) {
        return sendGetRequest("setPlayerCmd:seek:" + position, String.class);
    }

    /**
     * Command: setPlayerCmd:stop – Stop playback.
     */
    public CompletableFuture<String> setPlayerCmdStop() {
        return sendGetRequest("setPlayerCmd:stop", String.class);
    }

    /**
     * Command: setPlayerCmd:vol:{value} – Set volume (0-100).
     *
     * @param value volume percentage (0–100)
     */
    public CompletableFuture<String> setPlayerCmdVol(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Volume must be between 0 and 100");
        }
        return sendGetRequest("setPlayerCmd:vol:" + value, String.class);
    }

    /**
     * Command: setPlayerCmd:mute:{n} – Mute or unmute device.
     * 
     * @param n 1 to mute, 0 to unmute
     */
    public CompletableFuture<String> setPlayerCmdMute(int n) {
        if (n != 0 && n != 1) {
            throw new IllegalArgumentException("n must be 0 (unmute) or 1 (mute)");
        }
        return sendGetRequest("setPlayerCmd:mute:" + n, String.class);
    }

    /**
     * Command: setPlayerCmd:loopmode:{n} – Set loop/shuffle mode.
     * See API documentation for meaning of values (-1..5).
     */
    public CompletableFuture<String> setPlayerCmdLoopmode(int n) {
        if (n < -1 || n > 5) {
            throw new IllegalArgumentException("loopmode must be between -1 and 5");
        }
        return sendGetRequest("setPlayerCmd:loopmode:" + n, String.class);
    }

    /**
     * Command: getStaticIpInfo – Retrieve LAN/WLAN static IP configuration.
     */
    public CompletableFuture<StaticIpInfo> getStaticIpInfo() {
        return sendGetRequest("getStaticIpInfo", StaticIpInfo.class);
    }

    /**
     * Command: getStaticIP (deprecated) – Query networking status as raw string.
     */
    public CompletableFuture<String> getStaticIP() {
        return sendGetRequest("getStaticIP", String.class);
    }

    /**
     * Command: wlanGetConnectState – Get WiFi connection state.
     */
    public CompletableFuture<WlanConnectState> wlanGetConnectState() {
        return sendGetRequest("wlanGetConnectState", String.class).thenApply(WlanConnectState::fromString);
    }

    /**
     * Command: setWlanStaticIp:{IpAddress}:{GatewayIp}:{DnsServerIp} – Configure static WLAN IP.
     */
    public CompletableFuture<String> setWlanStaticIp(String ipAddress, String gatewayIp, String dnsServerIp) {
        return sendGetRequest(
                "setWlanStaticIp:" + encode(ipAddress) + ":" + encode(gatewayIp) + ":" + encode(dnsServerIp),
                String.class);
    }

    /**
     * Command: EQOn – Turn EQ on.
     */
    public CompletableFuture<StatusResponse> setEQOn() {
        return sendGetRequest("EQOn", StatusResponse.class);
    }

    /**
     * Command: EQOff – Turn EQ off.
     */
    public CompletableFuture<StatusResponse> setEQOff() {
        return sendGetRequest("EQOff", StatusResponse.class);
    }

    /**
     * Command: EQGetStat – Check if EQ is ON or OFF.
     */
    public CompletableFuture<EQStatResponse> getEQStat() {
        return sendGetRequest("EQGetStat", EQStatResponse.class);
    }

    /**
     * Command: EQGetList – Retrieve all available EQ presets.
     */
    public CompletableFuture<String[]> getEQList() {
        return sendGetRequest("EQGetList", String[].class);
    }

    /**
     * Command: EQGetBand – Get current EQ band configuration.
     */
    public CompletableFuture<EQBandResponse> getEQBand() {
        return sendGetRequest("EQGetBand", EQBandResponse.class);
    }

    /**
     * Command: EQLoad:{name} – Load a named EQ preset.
     * Accepts one of the names returned by getEQList().
     */
    public CompletableFuture<StatusResponse> loadEQByName(String name) {
        return sendGetRequest("EQLoad:" + encode(name), StatusResponse.class);
    }

    /**
     * Command: reboot – Reboot the device.
     */
    public CompletableFuture<StatusResponse> rebootDevice() {
        return sendGetRequest("reboot", StatusResponse.class);
    }

    /**
     * Command: setShutdown:{sec} – Schedule (or cancel) shutdown.
     *
     * @param sec seconds until shutdown (0 immediately, -1 cancel)
     */
    public CompletableFuture<StatusResponse> setShutdownTimer(int sec) {
        return sendGetRequest("setShutdown:" + sec, StatusResponse.class);
    }

    /**
     * Command: getShutdown – Retrieve current shutdown timer seconds.
     */
    public CompletableFuture<Integer> getShutdownTimer() {
        return sendGetRequest("getShutdown", Integer.class);
    }

    /**
     * Command: LED_SWITCH_SET:{n} – Turn status LED on/off.
     * 
     * @param n 1 on, 0 off
     */
    public CompletableFuture<String> setLedSwitch(int n) {
        if (n != 0 && n != 1) {
            throw new IllegalArgumentException("n must be 0 or 1");
        }
        return sendGetRequest("LED_SWITCH_SET:" + n, String.class);
    }

    /**
     * Command: Button_Enable_SET:{n} – Enable/disable touch controls.
     * 
     * @param n 1 on, 0 off
     */
    public CompletableFuture<String> setTouchControls(int n) {
        if (n != 0 && n != 1) {
            throw new IllegalArgumentException("n must be 0 or 1");
        }
        return sendGetRequest("Button_Enable_SET:" + n, String.class);
    }

    /**
     * Command: timeSync:{YYYYMMDDHHMMSS} – Sync device time (UTC).
     * Pass dateTime string exactly in required format.
     */
    public CompletableFuture<String> setTimeSync(String yyyymmddhhmmss) {
        return sendGetRequest("timeSync:" + yyyymmddhhmmss, String.class);
    }

    /**
     * Command: setAlarmClock:{n}:{trig}:{op}:{time}:{day}:{url}
     */
    public CompletableFuture<String> setAlarmClock(int number, int trigger, int operation, String time, String day,
            String url) {
        String path = String.format("setAlarmClock:%d:%d:%d:%s:%s:%s", number, trigger, operation, time, day,
                encode(url));
        return sendGetRequest(path, String.class);
    }

    /**
     * Command: getAlarmClock:{n}
     */
    public CompletableFuture<AlarmClockInfo> getAlarmClock(int number) {
        return sendGetRequest("getAlarmClock:" + number, AlarmClockInfo.class);
    }

    /**
     * Command: alarmStop – Stop current alarm.
     */
    public CompletableFuture<String> stopAlarmClock() {
        return sendGetRequest("alarmStop", String.class);
    }

    /**
     * Command: setPlayerCmd:switchmode:{mode} – Switch input source.
     */
    public CompletableFuture<String> setPlayerCmdSwitchMode(SourceInputMode mode) {
        return sendGetRequest("setPlayerCmd:switchmode:" + mode.toString(), String.class);
    }

    /**
     * Command: MCUKeyShortClick:{n} – Play preset by number.
     */
    public CompletableFuture<String> mcuKeyShortClick(int n) {
        if (n < 1 || n > 12) {
            throw new IllegalArgumentException("Preset number must be 1-12");
        }
        return sendGetRequest("MCUKeyShortClick:" + n, String.class);
    }

    /**
     * Command: getPresetInfo – Retrieve preset list.
     */
    public CompletableFuture<PresetList> getPresetInfo() {
        return sendGetRequest("getPresetInfo", PresetList.class);
    }

    /**
     * Command: getNewAudioOutputHardwareMode – Get audio output hardware mode.
     */
    public CompletableFuture<AudioOutputHardwareMode> getNewAudioOutputHardwareMode() {
        return sendGetRequest("getNewAudioOutputHardwareMode", AudioOutputHardwareMode.class);
    }

    /**
     * Command: setAudioOutputHardwareMode:{n} – Set audio output hardware mode.
     * 1: SPDIF, 2: AUX, 3: COAX
     */
    public CompletableFuture<String> setAudioOutputHardwareMode(int n) {
        if (n < 1 || n > 3) {
            throw new IllegalArgumentException("Hardware mode must be 1 (SPDIF), 2 (AUX) or 3 (COAX)");
        }
        return sendGetRequest("setAudioOutputHardwareMode:" + n, String.class);
    }

    /**
     * Command: getSpdifOutSwitchDelayMs – Get SPDIF sample-rate switch latency in ms.
     */
    public CompletableFuture<Integer> getSpdifOutSwitchDelayMs() {
        return sendGetRequest("getSpdifOutSwitchDelayMs", Integer.class);
    }

    /**
     * Command: setSpdifOutSwitchDelayMs:{Delay} – Set SPDIF switch latency.
     * Upper bound 3000 ms according to spec.
     */
    public CompletableFuture<String> setSpdifOutSwitchDelayMs(int delayMs) {
        if (delayMs < 0 || delayMs > 3000) {
            throw new IllegalArgumentException("Delay must be between 0 and 3000 milliseconds");
        }
        return sendGetRequest("setSpdifOutSwitchDelayMs:" + delayMs, String.class);
    }

    /**
     * Command: getChannelBalance – Get left/right channel balance.
     * Returns value as string ranging -1.0 to 1.0; converted to Double.
     */
    public CompletableFuture<Double> getChannelBalance() {
        return sendGetRequest("getChannelBalance", String.class).thenApply(Double::valueOf);
    }

    /**
     * Command: setChannelBalance:{n} – Set left/right channel balance (-1.0 left to 1.0 right).
     */
    public CompletableFuture<String> setChannelBalance(double balance) {
        if (balance < -1.0 || balance > 1.0) {
            throw new IllegalArgumentException("Balance must be between -1.0 and 1.0");
        }
        // format to avoid scientific notation
        String value = String.format(java.util.Locale.US, "%s", balance);
        return sendGetRequest("setChannelBalance:" + value, String.class);
    }

    // -------- Bluetooth Endpoints --------

    /**
     * Command: startbtdiscovery:{arg} – Start Bluetooth device scan.
     * The app uses 3, but other integers accepted.
     */
    public CompletableFuture<String> startBtDiscovery(int arg) {
        return sendGetRequest("startbtdiscovery:" + arg, String.class);
    }

    /**
     * Command: getbtdiscoveryresult – Fetch Bluetooth scan result.
     */
    public CompletableFuture<BluetoothDeviceList> getBtDiscoveryResult() {
        return sendGetRequest("getbtdiscoveryresult", BluetoothDeviceList.class);
    }

    /**
     * Command: clearbtdiscoveryresult – Clear BT scan results.
     */
    public CompletableFuture<String> clearBtDiscoveryResult() {
        return sendGetRequest("clearbtdiscoveryresult", String.class);
    }

    /**
     * Command: getbthistory – Retrieve paired BT devices.
     */
    public CompletableFuture<BluetoothDeviceList> getBtHistory() {
        return sendGetRequest("getbthistory", BluetoothDeviceList.class);
    }

    /**
     * Command: connectbta2dpsynk:{MAC} – Connect to BT device (A2DP sink).
     */
    public CompletableFuture<String> connectBtA2dpSynk(String macAddress) {
        return sendGetRequest("connectbta2dpsynk:" + encode(macAddress), String.class);
    }

    /**
     * Command: disconnectbta2dpsynk:{MAC} – Disconnect BT device.
     */
    public CompletableFuture<String> disconnectBtA2dpSynk(String macAddress) {
        return sendGetRequest("disconnectbta2dpsynk:" + encode(macAddress), String.class);
    }

    /**
     * Command: getbtpairstatus – Get BT pairing status.
     */
    public CompletableFuture<BTPairStatus> getBtPairStatus() {
        return sendGetRequest("getbtpairstatus", BTPairStatus.class);
    }

    // -------- Miscellaneous "Other" endpoints --------

    /**
     * Command: getMvRemoteSilenceUpdateTime – Gets remote silence update time.
     */
    public CompletableFuture<String> getMvRemoteSilenceUpdateTime() {
        return sendGetRequest("getMvRemoteSilenceUpdateTime", String.class);
    }

    /**
     * Command: getNetworkPreferDNS – Get preferred DNS setting.
     */
    public CompletableFuture<String> getNetworkPreferDNS() {
        return sendGetRequest("getNetworkPreferDNS", String.class);
    }

    /**
     * Command: getWlanBandConfig – Retrieve WLAN band configuration.
     */
    public CompletableFuture<String> getWlanBandConfig() {
        return sendGetRequest("getWlanBandConfig", String.class);
    }

    /**
     * Command: getWlanRoamConfig – Retrieve WLAN roaming configuration.
     */
    public CompletableFuture<String> getWlanRoamConfig() {
        return sendGetRequest("getWlanRoamConfig", String.class);
    }

    /**
     * Command: getIPV6Enable – Check if IPv6 is enabled.
     */
    public CompletableFuture<String> getIpv6Enable() {
        return sendGetRequest("getIPV6Enable", String.class);
    }

    /**
     * Command: getSpdifOutMaxCap – Retrieve SPDIF maximum capability.
     */
    public CompletableFuture<String> getSpdifOutMaxCap() {
        return sendGetRequest("getSpdifOutMaxCap", String.class);
    }

    /**
     * Command: getCoaxOutMaxCap – Retrieve COAX maximum capability.
     */
    public CompletableFuture<String> getCoaxOutMaxCap() {
        return sendGetRequest("getCoaxOutMaxCap", String.class);
    }

    /**
     * Command: GetFadeFeature – Retrieve fade feature information.
     */
    public CompletableFuture<String> getFadeFeature() {
        return sendGetRequest("GetFadeFeature", String.class);
    }

    /**
     * Command: getAuxVoltageSupportList – Get AUX voltage support list.
     */
    public CompletableFuture<String> getAuxVoltageSupportList() {
        return sendGetRequest("getAuxVoltageSupportList", String.class);
    }

    /**
     * Command: audio_cast:get_speaker_list – Deprecated, retrieve speaker list for audio cast.
     */
    public CompletableFuture<String> getAudioCastSpeakerList() {
        return sendGetRequest("audio_cast:get_speaker_list", String.class);
    }

    /**
     * Command: getSoundCardModeSupportList – Get sound card mode support list.
     */
    public CompletableFuture<String> getSoundCardModeSupportList() {
        return sendGetRequest("getSoundCardModeSupportList", String.class);
    }

    /**
     * Command: getActiveSoundCardOutputMode – Get active sound card output mode.
     */
    public CompletableFuture<String> getActiveSoundCardOutputMode() {
        return sendGetRequest("getActiveSoundCardOutputMode", String.class);
    }

    /**
     * Command: setLightOperationBrightConfig:{json} – WiiM Ultra LCD enable/disable config.
     * Wrapper around parameters s (auto_sense_enable), b (default_bright), d (disable).
     */
    public CompletableFuture<String> setLightOperationBrightConfig(int autoSenseEnable, int defaultBright,
            int disable) {
        String json = String.format("%%7B\"auto_sense_enable\":%d,\"default_bright\":%d,\"disable\":%d%%7D",
                autoSenseEnable, defaultBright, disable);
        return sendGetRequest("setLightOperationBrightConfig:" + json, String.class);
    }

    /**
     * Command: multiroom:getSlaveList – Fetch list of available LinkPlay slaves.
     */
    public CompletableFuture<SlaveListResponse> multiroomGetSlaveList() {
        return sendGetRequest("multiroom:getSlaveList", SlaveListResponse.class);
    }

    /**
     * Command: ConnectMasterAp:JoinGroupMaster:eth:{ip} –Join a group by IP.
     */
    public CompletableFuture<String> multiroomJoinGroupMaster(String ip) {
        return sendGetRequest("ConnectMasterAp:JoinGroupMaster:eth" + encode(ip) + ":wifi0.0.0.0", String.class);
    }

    /**
     * Command: multiroom:SlaveKickout:{ip} – Remove device from multiroom by IP.
     */
    public CompletableFuture<String> multiroomSlaveKickout(String ip) {
        return sendGetRequest("multiroom:SlaveKickout:" + encode(ip), String.class);
    }

    /**
     * Command: multiroom:SlaveMask:{ip} – Hide the IP address of a LinkPlay device (mask).
     */
    public CompletableFuture<String> multiroomSlaveMask(String ip) {
        return sendGetRequest("multiroom:SlaveMask:" + encode(ip), String.class);
    }

    /**
     * Command: multiroom:SlaveUnMask:{ip} – Unmask a previously masked device.
     */
    public CompletableFuture<String> multiroomSlaveUnMask(String ip) {
        return sendGetRequest("multiroom:SlaveUnMask:" + encode(ip), String.class);
    }

    /**
     * Command: multiroom:SlaveVolume:{ip}:{volume} – Adjust individual slave volume (1-100).
     */
    public CompletableFuture<String> multiroomSlaveVolume(String ip, int volume) {
        if (volume < 1 || volume > 100) {
            throw new IllegalArgumentException("Volume must be 1-100");
        }
        return sendGetRequest("multiroom:SlaveVolume:" + encode(ip) + ":" + volume, String.class);
    }

    /**
     * Command: setPlayerCmd:slave_vol:{volume} – Adjust overall multi-room volume.
     */
    public CompletableFuture<String> setPlayerCmdSlaveVol(int volume) {
        if (volume < 1 || volume > 100) {
            throw new IllegalArgumentException("Volume must be 1-100");
        }
        return sendGetRequest("setPlayerCmd:slave_vol:" + volume, String.class);
    }

    /**
     * Command: multiroom:SlaveMute:{ip}:{mute} – Mute/unmute individual slave (1 mute, 0 unmute).
     */
    public CompletableFuture<String> multiroomSlaveMute(String ip, int mute) {
        if (mute != 0 && mute != 1) {
            throw new IllegalArgumentException("mute must be 0 or 1");
        }
        return sendGetRequest("multiroom:SlaveMute:" + encode(ip) + ":" + mute, String.class);
    }

    /**
     * Command: setPlayerCmd:slave_mute:mute – Mute all devices in multi-room.
     */
    public CompletableFuture<String> setPlayerCmdSlaveMute() {
        return sendGetRequest("setPlayerCmd:slave_mute:mute", String.class);
    }

    /**
     * Command: setPlayerCmd:slave_mute:unmute – Unmute all devices in multi-room.
     */
    public CompletableFuture<String> setPlayerCmdSlaveUnmute() {
        return sendGetRequest("setPlayerCmd:slave_mute:unmute", String.class);
    }

    /**
     * Command: multiroom:SlaveChannel:{ip}:{channel} – Set individual slave channel (0 left, 1 right).
     */
    public CompletableFuture<String> multiroomSlaveChannel(String ip, int channel) {
        if (channel != 0 && channel != 1) {
            throw new IllegalArgumentException("channel must be 0 (left) or 1 (right)");
        }
        return sendGetRequest("multiroom:SlaveChannel:" + encode(ip) + ":" + channel, String.class);
    }

    /**
     * Command: setPlayerCmd:slave_channel:{channel} – Set overall channel for all devices.
     */
    public CompletableFuture<String> setPlayerCmdSlaveChannel(int channel) {
        if (channel != 0 && channel != 1) {
            throw new IllegalArgumentException("channel must be 0 (left) or 1 (right)");
        }
        return sendGetRequest("setPlayerCmd:slave_channel:" + channel, String.class);
    }

    /**
     * Command: multiroom:SlaveSetDeviceName:{ip}:{name} – Set name of individual device in multi-room.
     */
    public CompletableFuture<String> multiroomSlaveSetDeviceName(String ip, String name) {
        return sendGetRequest("multiroom:SlaveSetDeviceName:" + encode(ip) + ":" + encode(name), String.class);
    }

    /**
     * Command: multiroom:Ungroup – Disable multi-room mode (ungroup).
     */
    public CompletableFuture<String> multiroomUngroup() {
        return sendGetRequest("multiroom:Ungroup", String.class);
    }

    /**
     * Command: getMvRemoteUpdateStartCheck – Check if firmware update is available.
     */
    public CompletableFuture<String> getMvRemoteUpdateStartCheck() {
        return sendGetRequest("getMvRemoteUpdateStartCheck", String.class);
    }

    /**
     * Command: getMvRemoteUpdateStart – Trigger firmware update download process.
     */
    public CompletableFuture<String> getMvRemoteUpdateStart() {
        return sendGetRequest("getMvRemoteUpdateStart", String.class);
    }

    /**
     * Command: getMvRemoteUpdateStatus – Retrieve status of firmware update download.
     */
    public CompletableFuture<String> getMvRemoteUpdateStatus() {
        return sendGetRequest("getMvRemoteUpdateStatus", String.class);
    }

    /**
     * Command: getMvRomBurnPrecent – Retrieve firmware flashing progress (0-100%).
     */
    public CompletableFuture<String> getMvRomBurnPercent() {
        return sendGetRequest("getMvRomBurnPrecent", String.class);
    }

    /**
     * Command: setSSID:{value} – Change device SSID (hex string value).
     */
    public CompletableFuture<String> setSSID(String hexValue) {
        return sendGetRequest("setSSID:" + encode(hexValue), String.class);
    }

    /**
     * Command: setNetwork:{n}:{password} – Configure WiFi password and security.
     * n = 1 secure (WPA/WPA2), 0 open
     */
    public CompletableFuture<String> setNetwork(int secureFlag, String password) {
        if (secureFlag != 0 && secureFlag != 1) {
            throw new IllegalArgumentException("secureFlag must be 0 (open) or 1 (WPA)");
        }
        return sendGetRequest("setNetwork:" + secureFlag + ":" + encode(password), String.class);
    }

    /**
     * Command: restoreToDefault – Restore factory settings.
     */
    public CompletableFuture<String> restoreToDefault() {
        return sendGetRequest("restoreToDefault", String.class);
    }

    /**
     * Command: setPowerWifiDown – Turn off WiFi signal.
     */
    public CompletableFuture<String> setPowerWifiDown() {
        return sendGetRequest("setPowerWifiDown", String.class);
    }

    /**
     * Command: setDeviceName:{name} – Set UPnP/AirPlay device name (hex encoded).
     */
    public CompletableFuture<String> setDeviceName(String nameHex) {
        return sendGetRequest("setDeviceName:" + encode(nameHex), String.class);
    }

    private <T> CompletableFuture<T> sendGetRequest(String command, Class<T> responseType) {
        if (host.isEmpty() || port == 0) {
            throw new LinkPlayClientException("Host and port must be set");
        }
        Executor executor = httpClient.getExecutor();
        return CompletableFuture.supplyAsync(() -> {
            String url = String.format("%s://%s:%d/httpapi.asp?command=%s",
                    String.valueOf(port).endsWith("443") ? "https" : "http", host, port, command);
            try {
                logger.trace("Sending GET request to {}", url);
                ContentResponse response = httpClient.GET(url);
                String payload = response.getContentAsString();
                logger.trace("Response: {}", payload);
                if (responseType == String.class) {
                    @SuppressWarnings("unchecked")
                    T casted = (T) payload;
                    return casted;
                }
                if ("Failed".equals(payload)) {
                    throw new LinkPlayClientException("Response Failed");
                }

                @Nullable
                T result = gson.fromJson(payload, responseType);
                if (result == null) {
                    throw new LinkPlayClientException("Response is null");
                }
                return result;
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.trace("Failed to send GET request to {}: {}", url, e.getMessage());
                throw new LinkPlayClientException("Failed to send GET request to " + url, e);
            }
        }, executor);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
