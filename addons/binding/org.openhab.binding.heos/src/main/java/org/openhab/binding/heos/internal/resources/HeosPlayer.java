/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.resources;

import java.util.HashMap;

/**
 * The {@link HeosPlayer} represents a player within the HEOS
 * network
 *
 * @author Johannes Einig - Initial contribution
 */

public class HeosPlayer extends HeosMediaObject {

    private final String[] supportedPlayerInfo = { "name", "pid", "gip", "ip", "model", "version", "lineout, network" };
    private final String[] supportedPlayerStates = { "state", "level", "mute", "duration", "cur_pos" };

    private HashMap<String, String> playerInfo;
    private HashMap<String, String> playerState;

    // Player Infos Variables
    private String pid;
    private String name;
    private String model;
    private String ip;
    private String version;
    private String network;
    private String lineout;
    private String gid;
    private boolean online;

    // Player State Variables
    private String state;
    private String level;
    private String mute;
    private String duration;
    private String cur_pos;

    public HeosPlayer() {
        super();
        initPlayer();

    }

    public void updatePlayerInfo(HashMap<String, String> values) {

        playerInfo = values;
        for (String key : values.keySet()) {
            if (key.equals("name")) {
                name = values.get(key);
            }
            if (key.equals("pid")) {
                pid = values.get(key);
            }
            if (key.equals("ip")) {
                ip = values.get(key);
            }
            if (key.equals("model")) {
                model = values.get(key);
            }
            if (key.equals("version")) {
                version = values.get(key);
            }
            if (key.equals("lineout")) {
                lineout = values.get(key);
            }
            if (key.equals("network")) {
                network = values.get(key);
            }
            if (key.equals("gid")) {
                gid = values.get(key);
            }
        }

    }

    public void updatePlayerState(HashMap<String, String> values) {

        playerState = values;
        for (String key : values.keySet()) {
            if (key.equals("state")) {
                state = values.get(key);
            }
            if (key.equals("level")) {
                level = values.get(key);
            }
            if (key.equals("mute")) {
                mute = values.get(key);
            }
            if (key.equals("duration")) {
                duration = values.get(key);
            }
            if (key.equals("cur_pos")) {
                cur_pos = values.get(key);
            }

        }

    }

    private void initPlayer() {

        playerInfo = new HashMap<>(8);
        playerState = new HashMap<>(5);

        for (String key : supportedPlayerInfo) {
            playerInfo.put(key, null);
        }

        for (String key : supportedPlayerStates) {
            playerState.put(key, null);
        }

        updatePlayerInfo(playerInfo);
        updatePlayerState(playerState);

    }

    public HashMap<String, String> getPlayerInfo() {
        return playerInfo;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
        playerInfo.put("pid", pid);
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
        playerInfo.put("gid", gid);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        playerInfo.put("name", name);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
        playerInfo.put("model", model);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
        playerInfo.put("ip", ip);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
        playerInfo.put("version", version);
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
        playerInfo.put("network", network);
    }

    public String getLineout() {
        return lineout;
    }

    public void setLineout(String lineout) {
        this.lineout = lineout;
        playerInfo.put("lineout", lineout);
    }

    public String[] getSupportedPlayerInfo() {
        return supportedPlayerInfo;
    }

    // Player States

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
        playerState.put("state", state);
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
        playerState.put("level", level);
    }

    public String getMute() {
        return mute;
    }

    public void setMute(String mute) {
        this.mute = mute;
        playerState.put("mute", mute);
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
        playerState.put("duration", duration);
    }

    public String getCur_pos() {
        return cur_pos;
    }

    public void setCur_pos(String cur_pos) {
        this.cur_pos = cur_pos;
        playerState.put("cur_pos", cur_pos);
    }

    public String[] getSupportedPlayerStates() {
        return supportedPlayerStates;
    }

    public HashMap<String, String> getPlayerState() {
        return playerState;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

}
