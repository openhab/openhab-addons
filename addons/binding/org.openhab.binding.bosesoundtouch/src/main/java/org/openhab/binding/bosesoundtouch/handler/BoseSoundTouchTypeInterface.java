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
 * The {@link BoseSoundTouchTypeInterface} is responsible for handling commands, which are
 * sent to one of the channels.
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

}
