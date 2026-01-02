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
package org.openhab.binding.blink.internal.dto;

import java.time.OffsetDateTime;
import java.util.List;

import org.openhab.binding.blink.internal.config.CameraConfiguration;

/**
 * The {@link BlinkEvents} class is the DTO for the video events api call, wrapping items showed in the new media list,
 * also being used to watch for motion.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
public class BlinkEvents {

    public List<Media> media;

    public static class Media {

        public long id;
        public OffsetDateTime created_at;
        public OffsetDateTime updated_at;
        public boolean deleted;
        public boolean watched;
        public String thumbnail;
        public String device;
        public long device_id;
        public long network_id;

        public boolean isDeletedEvent() {
            return deleted;
        }

        public boolean isUpdatedEvent() {
            return !created_at.isEqual(updated_at);
        }

        public boolean isNewEvent() {
            return !isDeletedEvent() && !isUpdatedEvent();
        }

        public boolean isCamera() {
            return "catalina".equals(device);
        }

        public boolean isCamera(CameraConfiguration config) {
            return isCamera() && network_id == config.networkId && device_id == config.cameraId;
        }
    }
}
