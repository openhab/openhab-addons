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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents a LinkPlay PlayQueue containing multiple playlists
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class PlayQueue {
    private int totalQueue = -1;
    private String currentPlayListName = "";
    private List<PlayList> playLists = new ArrayList<>();

    public int getTotalQueue() {
        return totalQueue;
    }

    public void setTotalQueue(int totalQueue) {
        this.totalQueue = totalQueue;
    }

    public String getCurrentPlayListName() {
        return currentPlayListName;
    }

    public void setCurrentPlayListName(String currentPlayListName) {
        this.currentPlayListName = currentPlayListName;
    }

    public List<PlayList> getPlayLists() {
        return playLists;
    }

    public void setPlayLists(List<PlayList> playLists) {
        this.playLists = playLists;
    }

    public void addPlayList(PlayList playList) {
        this.playLists.add(playList);
    }
}
