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
 * The {@link FreeboxAirMediaReceiver} is the Java class used to map the "AirMediaReceiver"
 * structure used by the available AirMedia receivers API
 * https://dev.freebox.fr/sdk/os/airmedia/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxAirMediaReceiver {
    private String name;
    private boolean passwordProtected;
    private FreeboxAirMediaReceiverCapabilities capabilities;

    public String getName() {
        return name;
    }

    public boolean isPasswordProtected() {
        return passwordProtected;
    }

    public FreeboxAirMediaReceiverCapabilities getCapabilities() {
        return capabilities;
    }

    public boolean isPhotoCapable() {
        return capabilities.isPhotoCapable();
    }

    public boolean isScreenCapable() {
        return capabilities.isScreenCapable();
    }

    public boolean isVideoCapable() {
        return capabilities.isVideoCapable();
    }

    public boolean isAudioCapable() {
        return capabilities.isAudioCapable();
    }
}
