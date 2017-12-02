/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * The {@link HeosGroup} represents the group within the
 * HEOS network
 *
 * @author Johannes Einig - Initial contribution
 */

public class HeosGroup extends HeosMediaObject {
    private final String[] supportedGroupInfo = { "name", "gip", "leader" };
    private final String[] supportedGroupStates = { "state", "level", "mute" };

    private HashMap<String, String> groupInfo;
    private HashMap<String, String> groupState;
    private List<HashMap<String, String>> playerList;
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

    // Group State Variables
    private String state;
    private String level;
    private String mute;

    private static final String PID = "pid";
    private static final String GID = "gid";
    private static final String NAME = "name";
    private static final String LEADER = "leader";
    private static final String STATE = "state";
    private static final String LEVEL = "level";
    private static final String MUTE = "mute";

    public HeosGroup() {
        initGroup();
    }

    public void updateGroupInfo(HashMap<String, String> values) {
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

    public void updateGroupState(HashMap<String, String> values) {
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
     * Updates the group members.
     *
     * Generates the {@code groupMembersHash} from the group member PIDs
     *
     * @param playerList The List of the Player (player as: HashMap<String,String>)
     */

    public void updateGroupPlayers(List<HashMap<String, String>> playerList) {
        this.playerList = playerList;
        groupMemberPidList = new ArrayList<String>(playerList.size());
        for (int i = 0; i < this.playerList.size(); i++) {
            HashMap<String, String> player = playerList.get(i);
            groupMemberPidList.add(player.get(PID));
        }
        // Generating a dedicated sorted and un-sorted list for different purposes
        groupMemberPidListSorted = new ArrayList<String>(playerList.size());
        groupMemberPidListSorted.addAll(groupMemberPidList);
        // Collections.reverse(groupMemberPidList); // List has to be reversed so that leader is at the beginning
        Collections.sort(groupMemberPidListSorted);
        groupMembersHash = Integer.toUnsignedString(groupMemberPidListSorted.hashCode());
    }

    public String generateGroupUID() {
        List<String> groupUIDHashList = new ArrayList<String>();
        groupUIDHashList.add(name);
        groupUIDHashList.add(gid);
        groupUIDHashList.add(leader);
        groupUIDHashList.add(groupMembersHash);
        groupUIDHash = Integer.toUnsignedString(groupUIDHashList.hashCode());

        return groupUIDHash;
    }

    private void initGroup() {
        groupInfo = new HashMap<>(8);
        groupState = new HashMap<>(5);
        playerList = new ArrayList<>(5);

        for (String key : supportedGroupInfo) {
            groupInfo.put(key, null);
        }

        for (String key : supportedGroupStates) {
            groupState.put(key, null);
        }

        updateGroupInfo(groupInfo);
        updateGroupState(groupState);
    }

    public HashMap<String, String> getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(HashMap<String, String> groupInfo) {
        this.groupInfo = groupInfo;
    }

    public HashMap<String, String> getGroupState() {
        return groupState;
    }

    public void setGroupState(HashMap<String, String> groupState) {
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
        groupInfo.put("gid", gid);
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
        groupInfo.put("leader", leader);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
        groupInfo.put("state", state);
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
        groupInfo.put("level", level);
    }

    public String getMute() {
        return mute;
    }

    public void setMute(String mute) {
        this.mute = mute;
        groupInfo.put("mute", mute);
    }

    public String[] getSupportedGroupInfo() {
        return supportedGroupInfo;
    }

    public String[] getSupportedGroupStates() {
        return supportedGroupStates;
    }

    public String getNameHash() {
        return nameHash;
    }

    public String getGroupMemberHash() {
        return groupMembersHash;
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
