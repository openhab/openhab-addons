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
 * Pojo for widget updates.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class WidgetUpdates {
    private List<Frame> frames;

    public List<Frame> getFrames() {
        return frames;
    }

    public void setFrames(List<Frame> frames) {
        this.frames = frames;
    }

    public WidgetUpdates withFrames(List<Frame> frames) {
        this.frames = frames;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WidgetUpdates [frames=");
        builder.append(frames);
        builder.append("]");
        return builder.toString();
    }
}
