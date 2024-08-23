/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal.api.local.dto;

import java.util.List;

/**
 * Pojo for notification model.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class NotificationModel {
    private Integer cycles;
    private List<Frame> frames;
    private Sound sound;

    public Integer getCycles() {
        return cycles;
    }

    public void setCycles(Integer cycles) {
        this.cycles = cycles;
    }

    public NotificationModel withCycles(Integer cycles) {
        this.cycles = cycles;
        return this;
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public void setFrames(List<Frame> frames) {
        this.frames = frames;
    }

    public NotificationModel withFrames(List<Frame> frames) {
        this.frames = frames;
        return this;
    }

    public Sound getSound() {
        return sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }

    public NotificationModel withSound(Sound sound) {
        this.sound = sound;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NotificationModel [cycles=");
        builder.append(cycles);
        builder.append(", frames=");
        builder.append(frames);
        builder.append(", sound=");
        builder.append(sound);
        builder.append("]");
        return builder.toString();
    }
}
