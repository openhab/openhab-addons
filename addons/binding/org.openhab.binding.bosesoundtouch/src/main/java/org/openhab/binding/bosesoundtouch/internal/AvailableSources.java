/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal;

/**
 * The {@link AvailableSources} is used to find out, which sources are available
 *
 * @author Thomas Traunbauer
 */
public interface AvailableSources {

    public boolean isBluetoothAvailable();

    public boolean isAUXAvailable();

    public boolean isAUX1Available();

    public boolean isAUX2Available();

    public boolean isAUX3Available();

    public boolean isTVAvailable();

    public boolean isHDMI1Available();

    public boolean isInternetRadioAvailable();

    public boolean isStoredMusicAvailable();

    public boolean isBassAvailable();

    public void setAUXAvailable(boolean aux);

    public void setAUX1Available(boolean aux1);

    public void setAUX2Available(boolean aux2);

    public void setAUX3Available(boolean aux3);

    public void setStoredMusicAvailable(boolean storedMusic);

    public void setInternetRadioAvailable(boolean internetRadio);

    public void setBluetoothAvailable(boolean bluetooth);

    public void setTVAvailable(boolean tv);

    public void setHDMI1Available(boolean hdmi1);

    public void setBassAvailable(boolean bass);
}
