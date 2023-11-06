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
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxAirMediaReceiverCapabilities} is the Java class used to map the
 * structure used inside the response of the available AirMedia receivers API to provide
 * the receiver capabilities
 * https://dev.freebox.fr/sdk/os/airmedia/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxAirMediaReceiverCapabilities {
    private boolean photo;
    private boolean screen;
    private boolean video;
    private boolean audio;

    public boolean isPhotoCapable() {
        return photo;
    }

    public boolean isScreenCapable() {
        return screen;
    }

    public boolean isVideoCapable() {
        return video;
    }

    public boolean isAudioCapable() {
        return audio;
    }
}
