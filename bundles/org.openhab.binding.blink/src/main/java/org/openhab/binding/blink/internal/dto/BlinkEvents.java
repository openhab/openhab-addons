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
 * @author Robert T. Brown (-rb) - support Blink Authentication changes in 2025 (OAUTHv2)
 * @author Volker Bier - add support for Doorbells
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
            return true;
            /**
             * Was:
             * return "catalina".equals(device);
             *
             * But I have observed these device names:
             *
             * device......common name
             * camera......XT and XT2 cameras (the 2 original generations)
             * catalina....gen3
             * sedona......gen4
             * lotus.......doorbell
             * owl.........mini
             *
             * These are all cameras, so I'm hardcoding "true" here until someone identifies a media event NOT generated
             * by a camera.
             **/
        }

        public boolean isCamera(CameraConfiguration config) {
            return isCamera() && network_id == config.networkId && device_id == config.cameraId;
        }

        @Override
        public String toString() {
            return ("NetworkId/DeviceId/MediaId: " + network_id + "/" + device_id + "/" + id + ", Device: " + device
                    + ", thumbnail: " + thumbnail + ", Watched=" + watched + ", Deleted?=" + deleted + ", Created at: "
                    + created_at + ", Updated at: " + updated_at);
        }
    }
}
