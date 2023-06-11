/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pulseaudio.internal.handler.DeviceIdentifier;

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
    protected String secondaryIdentifier;
    protected Map<String, String> properties;

    public AbstractAudioDeviceConfig(int id, String name, @Nullable String secondaryIdentifier,
            Map<String, String> properties, @Nullable Module module) {
        super(id, name);
        this.module = module;
        this.secondaryIdentifier = secondaryIdentifier == null ? "" : secondaryIdentifier;
        this.properties = properties;
    }

    /**
     *
     * @param deviceIdentifier The device identifier to check against
     * @return true if this device match the requested identifier, false otherwise
     */
    public boolean matches(DeviceIdentifier deviceIdentifier) {
        boolean matches = getPaName().equalsIgnoreCase(deviceIdentifier.getNameOrDescription())
                || secondaryIdentifier.equalsIgnoreCase(deviceIdentifier.getNameOrDescription());
        if (!matches) {
            return false; // stop analysis right here, no need to parse properties
        } else {
            List<Pattern> additionalFilters = deviceIdentifier.getAdditionalFilters();
            if (additionalFilters.isEmpty()) { // the additionalFilter property is not defined, don't check against
                return true;
            } else {
                for (Pattern patternToMatch : additionalFilters) {
                    if (!properties.values().stream().anyMatch(value -> patternToMatch.matcher(value).find())) {
                        return false;
                    }
                }
                return true;
            }
        }
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
