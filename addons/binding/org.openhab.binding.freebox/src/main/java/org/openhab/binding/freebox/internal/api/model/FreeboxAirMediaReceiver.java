/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
