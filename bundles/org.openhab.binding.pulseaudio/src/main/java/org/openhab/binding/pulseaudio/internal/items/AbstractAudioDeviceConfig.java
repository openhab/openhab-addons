/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.pulseaudio.internal.items;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * GenericAudioItems are any kind of items that deal with audio data and can be
 * muted or their volume can be changed.
 *
 * @author Tobias Bräutigam - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractAudioDeviceConfig extends AbstractDeviceConfig {

    public enum State {
        SUSPENDED,
        IDLE,
        RUNNING,
        CORKED,
        DRAINED
    }

    protected @Nullable State state;
    protected boolean muted;
    protected int volume;
    protected @Nullable Module module;

    public AbstractAudioDeviceConfig(int id, String name, @Nullable Module module) {
        super(id, name);
        this.module = module;
    }

    public @Nullable Module getModule() {
        return module;
    }

    public @Nullable State getState() {
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
        return this.getClass().getSimpleName() + " #" + id + " (Module: " + module + ") " + name + ", muted: " + muted
                + ", state: " + state + ", volume: " + volume;
    }
}
