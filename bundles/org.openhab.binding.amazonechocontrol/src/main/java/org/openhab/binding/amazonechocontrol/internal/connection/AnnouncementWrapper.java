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
package org.openhab.binding.amazonechocontrol.internal.connection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.dto.DeviceTO;
import org.openhab.binding.amazonechocontrol.internal.dto.request.AnnouncementContentTO;

/**
 * The {@link AnnouncementWrapper} is a wrapper for announcement instructions
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class AnnouncementWrapper {
    private final List<DeviceTO> devices = new ArrayList<>();
    private final List<@Nullable Integer> ttsVolumes = new ArrayList<>();
    private final List<@Nullable Integer> standardVolumes = new ArrayList<>();

    private final String speak;
    private final String bodyText;
    private final @Nullable String title;

    public AnnouncementWrapper(String speak, String bodyText, @Nullable String title) {
        this.speak = speak;
        this.bodyText = bodyText;
        this.title = title;
    }

    public void add(DeviceTO device, @Nullable Integer ttsVolume, @Nullable Integer standardVolume) {
        devices.add(device);
        ttsVolumes.add(ttsVolume);
        standardVolumes.add(standardVolume);
    }

    public List<DeviceTO> getDevices() {
        return devices;
    }

    public String getSpeak() {
        return speak;
    }

    public String getBodyText() {
        return bodyText;
    }

    public @Nullable String getTitle() {
        return title;
    }

    public List<@Nullable Integer> getTtsVolumes() {
        return ttsVolumes;
    }

    public List<@Nullable Integer> getStandardVolumes() {
        return standardVolumes;
    }

    public AnnouncementContentTO toAnnouncementTO() {
        AnnouncementContentTO announcement = new AnnouncementContentTO();
        announcement.display.body = bodyText;
        String title = this.title;
        announcement.display.title = (title == null || title.isBlank()) ? "openHAB" : title;
        announcement.speak.value = speak;
        announcement.speak.type = (speak.startsWith("<speak>") && speak.endsWith("</speak>")) ? "ssml" : "text";
        return announcement;
    }
}
