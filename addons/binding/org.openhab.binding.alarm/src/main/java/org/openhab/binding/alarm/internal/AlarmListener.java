/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.alarm.internal;

import org.openhab.binding.alarm.internal.model.AlarmStatus;

/**
 * Listener for changes in the {@link AlarmController}
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface AlarmListener {

    /**
     * Called when the status of the alarm controller changed.
     */
    public void alarmStatusChanged(AlarmStatus status);

    /**
     * Called every second when a countdown is active.
     */
    public void alarmCountdownChanged(int value);

    /**
     * Called when ready to arm internally chanded.
     */
    public void readyToArmInternallyChanged(boolean isReady);

    /**
     * Called when ready to arm externally chanded.
     */
    public void readyToArmExternallyChanged(boolean isReady);

    /**
     * Called when passthrough chanded.
     */
    public void readyToPassthroughChanged(boolean isReady);

}
