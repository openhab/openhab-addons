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
