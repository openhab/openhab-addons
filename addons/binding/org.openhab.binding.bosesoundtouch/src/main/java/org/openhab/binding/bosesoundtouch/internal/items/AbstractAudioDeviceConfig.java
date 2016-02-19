/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal.items;

/**
 * GenericAudioItems are any kind of items that deal with audio data and can be
 * muted or their volume can be changed.
 *
 * @author Christian Niessner - Initial contribution
 */
public abstract class AbstractAudioDeviceConfig {

    public enum State {
        SUSPENDED,
        IDLE,
        RUNNING,
        CORKED,
        DRAINED
    }

    protected State state;
    protected boolean muted;
    protected int volume;
    protected int id;
    protected String name;

    public AbstractAudioDeviceConfig(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * returns the internal id of this device
     *
     * @return
     */
    public int getId() {
        return id;
    }

    public String getUIDName() {
        return name.replaceAll("[^A-Za-z0-9_]", "_");
    }

    public String getPaName() {
        return name;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " #" + id + " " + name + ", muted: " + muted + ", state: " + state
                + ", volume: " + volume;
    }

}
