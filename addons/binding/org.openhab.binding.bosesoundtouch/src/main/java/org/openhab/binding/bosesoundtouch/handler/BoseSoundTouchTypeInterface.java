/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.handler;

/**
 * The {@link BoseSoundTouchTypeInterface} is used to find out, which sources are available
 *
 * @author Thomas Traunbauer
 */
public interface BoseSoundTouchTypeInterface {

    public boolean hasBluetooth();

    public boolean hasAUX();

    public boolean hasAUX1();

    public boolean hasAUX2();

    public boolean hasAUX3();

    public boolean hasTV();

    public boolean hasHDMI1();

    public boolean hasInternetRadio();

    public boolean hasStoredMusic();

    public void setAUX(boolean aux);

    public void setAUX1(boolean aux1);

    public void setAUX2(boolean aux2);

    public void setAUX3(boolean aux3);

    public void setStoredMusic(boolean storedMusic);

    public void setInternetRadio(boolean internetRadio);

    public void setBluetooth(boolean bluetooth);

    public void setTV(boolean tv);

    public void setHDMI1(boolean hdmi1);
}
