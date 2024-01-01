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
package org.openhab.binding.bosesoundtouch.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AvailableSources} is used to find out, which sources and functions are available
 *
 * @author Thomas Traunbauer - Initial contribution
 */

@NonNullByDefault
public interface AvailableSources {

    boolean isBluetoothAvailable();

    boolean isAUXAvailable();

    boolean isAUX1Available();

    boolean isAUX2Available();

    boolean isAUX3Available();

    boolean isTVAvailable();

    boolean isHDMI1Available();

    boolean isInternetRadioAvailable();

    boolean isStoredMusicAvailable();

    boolean isBassAvailable();

    void setAUXAvailable(boolean aux);

    void setAUX1Available(boolean aux1);

    void setAUX2Available(boolean aux2);

    void setAUX3Available(boolean aux3);

    void setStoredMusicAvailable(boolean storedMusic);

    void setInternetRadioAvailable(boolean internetRadio);

    void setBluetoothAvailable(boolean bluetooth);

    void setTVAvailable(boolean tv);

    void setHDMI1Available(boolean hdmi1);

    void setBassAvailable(boolean bass);
}
