/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * Chime
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ChimeCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0556;
    public static final String CLUSTER_NAME = "Chime";
    public static final String CLUSTER_PREFIX = "chime";
    public static final String ATTRIBUTE_INSTALLED_CHIME_SOUNDS = "installedChimeSounds";
    public static final String ATTRIBUTE_SELECTED_CHIME = "selectedChime";
    public static final String ATTRIBUTE_ENABLED = "enabled";

    /**
     * This attribute shall contain all installed chime sounds, represented by a list of Chime Sounds. Each entry in
     * this list shall have a unique ChimeID value and a unique Name value.
     */
    public List<ChimeSoundStruct> installedChimeSounds; // 0 list R V
    /**
     * Indicates the currently selected chime sound that will be played when PlayChimeSound is invoked and shall be the
     * ChimeID value for the requested Chime Sound within InstalledChimeSounds.
     * This attribute may be written by the client to request a different chime sound. An attempt to write a value that
     * is not contained within InstalledChimeSounds shall be failed with a NOT_FOUND response. Writes to this attribute
     * while a chime is currently playing shall NOT affect the playback in progress and shall only apply starting at the
     * next PlayChimeSound command invocation.
     */
    public Integer selectedChime; // 1 uint8 RW VO
    /**
     * Indicates if chime sounds can currently be played or not, and may be written by the client to enable / disable
     * playing of chime sounds.
     */
    public Boolean enabled; // 2 bool RW VO

    // Structs
    /**
     * This event shall indicate a Chime sound has just started playing.
     * The data on this event shall contain the following information.
     */
    public static class ChimeStartedPlaying {
        /**
         * This field shall represent the unique ID for the Chime sound that just started playing.
         */
        public Integer chimeId; // uint8

        public ChimeStartedPlaying(Integer chimeId) {
            this.chimeId = chimeId;
        }
    }

    /**
     * This struct is used to encode information needed to define a Chime Sound.
     */
    public static class ChimeSoundStruct {
        /**
         * This field shall represent the unique ID for a Chime sound.
         */
        public Integer chimeId; // uint8
        /**
         * This field shall represent the unique user friendly name of the Chime Sound.
         */
        public String name; // string

        public ChimeSoundStruct(Integer chimeId, String name) {
            this.chimeId = chimeId;
            this.name = name;
        }
    }

    public ChimeCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1366, "Chime");
    }

    protected ChimeCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command will play the currently selected chime or the chime passed in. In either case the server shall
     * generate the ChimeStartedPlaying event.
     */
    public static ClusterCommand playChimeSound(Integer chimeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (chimeId != null) {
            map.put("chimeId", chimeId);
        }
        return new ClusterCommand("playChimeSound", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "installedChimeSounds : " + installedChimeSounds + "\n";
        str += "selectedChime : " + selectedChime + "\n";
        str += "enabled : " + enabled + "\n";
        return str;
    }
}
