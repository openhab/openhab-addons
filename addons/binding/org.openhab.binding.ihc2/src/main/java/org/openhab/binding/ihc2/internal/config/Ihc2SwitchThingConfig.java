/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.config;

/**
 * The {@link Ihc2SwitchThingConfig} holds information about the pulse width if the switch is used as push button.
 *
 *
 * @author Niels Peter Enemark - Initial contribution
 */
public class Ihc2SwitchThingConfig extends Ihc2ReadOnlyThingConfig {
    private int pulseTime;

    public int getPulseTime() {
        return pulseTime;
    }

    public void setPulseTime(int pulseTime) {
        this.pulseTime = pulseTime;
    }

}
