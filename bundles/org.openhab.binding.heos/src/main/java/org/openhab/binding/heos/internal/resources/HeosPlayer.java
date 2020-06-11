/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.heos.internal.resources;

import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link HeosPlayer} represents a player within the HEOS
 * network
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosPlayer extends HeosMediaObject {

    private static final String[] SUPPORTED_PLAYER_INFO = { NAME, PID, GID, IP, MODEL, VERSION, LINE_OUT, NETWORK,
            CONTROL, SERIAL };
    private static final String[] SUPPORTED_PLAYER_STATE = { STATE, LEVEL, MUTE, DURATION, CUR_POS };

    private Map<String, String> playerInfo;
    private Map<String, String> playerState;

    // Player Infos Variables
    private String pid;
    private String name;
    private String model;
    private String ip;
    private String version;
    private String network;
    private String lineout;
    private String gid;
    private String control;
    private String serial;
    private boolean online;

    // Player State Variables
    private String state;
    private String level;
    private String mute;
    private String duration;
    private String curPos;
    private String shuffle;
    private String repeatMode;

    public HeosPlayer() {
        super();
        initPlayer();
    }

    public void updatePlayerInfo(Map<String, String> values) {
        playerInfo = values;
        for (String key : values.keySet()) {
            if (key.equals(NAME)) {
                name = values.get(key);
            }
            if (key.equals(PID)) {
                pid = values.get(key);
            }
            if (key.equals(IP)) {
                ip = values.get(key);
            }
            if (key.equals(MODEL)) {
                model = values.get(key);
            }
            if (key.equals(VERSION)) {
                version = values.get(key);
            }
            if (key.equals(LINE_OUT)) {
                lineout = values.get(key);
            }
            if (key.equals(NETWORK)) {
                network = values.get(key);
            }
            if (key.equals(GID)) {
                gid = values.get(key);
            }
            if (key.equals(CONTROL)) {
                control = values.get(key);
            }
            if (key.equals(SERIAL)) {
                serial = values.get(key);
            }
        }
    }

    public void updatePlayerState(Map<String, String> values) {
        playerState = values;
        for (String key : values.keySet()) {
            if (key.equals(STATE)) {
                state = values.get(key);
            }
            if (key.equals(LEVEL)) {
                level = values.get(key);
            }
            if (key.equals(MUTE)) {
                mute = values.get(key);
            }
            if (key.equals(DURATION)) {
                duration = values.get(key);
            }
            if (key.equals(CUR_POS)) {
                curPos = values.get(key);
            }
        }
    }

    private void initPlayer() {
        playerInfo = new HashMap<>(10);
        playerState = new HashMap<>(5);

        for (String key : SUPPORTED_PLAYER_INFO) {
            playerInfo.put(key, null);
        }
        for (String key : SUPPORTED_PLAYER_STATE) {
            playerState.put(key, null);
        }
        updatePlayerInfo(playerInfo);
        updatePlayerState(playerState);
    }

    public Map<String, String> getPlayerInfo() {
        return playerInfo;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
        playerInfo.put(PID, pid);
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
        playerInfo.put(GID, gid);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        playerInfo.put(NAME, name);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
        playerInfo.put(MODEL, model);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
        playerInfo.put(IP, ip);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
        playerInfo.put(VERSION, version);
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
        playerInfo.put(NETWORK, network);
    }

    public String getLineout() {
        return lineout;
    }

    public void setLineout(String lineout) {
        this.lineout = lineout;
        playerInfo.put(LINE_OUT, lineout);
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
        playerInfo.put(CONTROL, control);
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
        playerInfo.put(SERIAL, serial);
    }

    public String[] getSupportedPlayerInfo() {
        return SUPPORTED_PLAYER_INFO;
    }

    // Player States

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
        playerState.put(STATE, state);
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
        playerState.put(LEVEL, level);
    }

    public String getMute() {
        return mute;
    }

    public void setMute(String mute) {
        this.mute = mute;
        playerState.put(MUTE, mute);
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
        playerState.put(DURATION, duration);
    }

    public String getCurPos() {
        return curPos;
    }

    public void setCurPos(String curPos) {
        this.curPos = curPos;
        playerState.put(CUR_POS, curPos);
    }

    public String[] getSupportedPlayerStates() {
        return SUPPORTED_PLAYER_STATE;
    }

    public Map<String, String> getPlayerState() {
        return playerState;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getShuffle() {
        return shuffle;
    }

    public void setShuffle(String shuffle) {
        this.shuffle = shuffle;
    }

    public String getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(String repeatMode) {
        this.repeatMode = repeatMode;
    }
}
