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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link HeosGroup} represents the group within the
 * HEOS network
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosGroup extends HeosMediaObject {
    private static final String[] SUPPORTED_GROUP_INFO = { NAME, GID, LEADER };
    private static final String[] SUPPORTED_GRIOUP_STATE_STRINGS = { STATE, LEVEL, MUTE };

    private Map<String, String> groupInfo;
    private Map<String, String> groupState;
    private List<Map<String, String>> playerList;
    private List<String> groupMemberPidList;
    private List<String> groupMemberPidListSorted;

    // Group Infos Variables
    private String name;
    private String gid;
    private String leader;
    private String nameHash;
    private String groupMembersHash;
    private String groupUIDHash;
    private boolean online;
    private String shuffle;
    private String repeatMode;

    // Group State Variables
    private String state;
    private String level;
    private String mute;

    public HeosGroup() {
        initGroup();
    }

    private void initGroup() {
        groupInfo = new HashMap<>(8);
        groupState = new HashMap<>(5);
        playerList = new ArrayList<>(5);

        for (String key : SUPPORTED_GROUP_INFO) {
            groupInfo.put(key, null);
        }

        for (String key : SUPPORTED_GRIOUP_STATE_STRINGS) {
            groupState.put(key, null);
        }

        updateGroupInfo(groupInfo);
        updateGroupState(groupState);
    }

    public void updateGroupInfo(Map<String, String> values) {
        groupInfo = values;
        for (String key : values.keySet()) {
            if (key.equals(NAME)) {
                name = values.get(key);
                if (values.get(key) != null) {
                    nameHash = Integer.toUnsignedString(values.get(key).hashCode());
                } else {
                    nameHash = "";
                }
            }
            if (key.equals(LEADER)) {
                leader = values.get(key);
            }
            if (key.equals(GID)) {
                gid = values.get(key);
            }
        }
    }

    public void updateGroupState(Map<String, String> values) {
        groupState = values;
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
        }
    }

    /**
     * Updates the group members. *
     * Generates the {@code groupMembersHash} from the group member PIDs
     *
     * @param playerList The List of the Player (player as: HashMap<String,String>)
     */
    public void updateGroupPlayers(List<Map<String, String>> playerList) {
        this.playerList = playerList;
        groupMemberPidList = new ArrayList<>(playerList.size());
        // Defining the leader and groupID (gid) and placing the leader at the
        // first position of the groupMemberPidList
        playerList.forEach(player -> {
            if (player.containsValue(LEADER)) {
                gid = player.get(PID);
                leader = gid;
                groupMemberPidList.add(0, player.get(PID));
            } else {
                groupMemberPidList.add(player.get(PID));
            }
            ;
        });
        generateGroupMemberHash();
    }

    /**
     * Updated the group members by a string. Members have to be separated by ";"
     * Also the group leader has to be at first position.
     *
     * @param players the group members as string
     */
    public void updateGroupPlayers(String players) {
        String[] playerArray = players.split(";");
        groupMemberPidList = Arrays.asList(playerArray);
        gid = groupMemberPidList.get(0);
        leader = gid;
        generateGroupMemberHash();
    }

    /**
     * Generates the hash value out of the group members from an sorted list
     * to get the same has value regardless if the sorting within the HEOS
     * system has changed
     */
    private void generateGroupMemberHash() {
        groupMemberPidListSorted = new ArrayList<>(playerList.size());
        groupMemberPidListSorted.addAll(groupMemberPidList);
        Collections.sort(groupMemberPidListSorted);
        groupMembersHash = Integer.toUnsignedString(groupMemberPidListSorted.hashCode());
    }

    public String getGroupMembersAsString() {
        return String.join(";", groupMemberPidList);
    }

    public String generateGroupUID() {
        List<String> groupUIDHashList = new ArrayList<>();
        groupUIDHashList.add(name);
        groupUIDHashList.add(gid);
        groupUIDHashList.add(leader);
        groupUIDHashList.add(groupMembersHash);
        groupUIDHash = Integer.toUnsignedString(groupUIDHashList.hashCode());

        return groupUIDHash;
    }

    public Map<String, String> getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(Map<String, String> groupInfo) {
        this.groupInfo = groupInfo;
    }

    public Map<String, String> getGroupState() {
        return groupState;
    }

    public void setGroupState(Map<String, String> groupState) {
        this.groupState = groupState;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        groupInfo.put("name", name);
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
        groupInfo.put(GID, gid);
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
        groupInfo.put(LEADER, leader);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
        groupInfo.put(STATE, state);
    }

    public String getLevel() {
        return level;
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

    public void setLevel(String level) {
        this.level = level;
        groupInfo.put(LEVEL, level);
    }

    public String getMute() {
        return mute;
    }

    public void setMute(String mute) {
        this.mute = mute;
        groupInfo.put(MUTE, mute);
    }

    public String[] getSupportedGroupInfo() {
        return SUPPORTED_GROUP_INFO;
    }

    public String[] getSupportedGroupStates() {
        return SUPPORTED_GRIOUP_STATE_STRINGS;
    }

    public String getNameHash() {
        return nameHash;
    }

    public void setNameHash(String nameHash) {
        this.nameHash = nameHash;
    }

    public String getGroupMemberHash() {
        return groupMembersHash;
    }

    public void setGroupMemberHash(String groupMemberHash) {
        this.groupMembersHash = groupMemberHash;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getGroupUIDHash() {
        return groupUIDHash;
    }

    public List<String> getGroupMemberPidList() {
        return groupMemberPidList;
    }
}
