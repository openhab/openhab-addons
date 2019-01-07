/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.internal;

import org.eclipse.smarthome.config.core.Configuration;

/**
 * The {@link KonnectedConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Zachary Christiansen - Initial contribution
 */
public class KonnectedConfiguration extends Configuration {

    /**
     * @param blink     identifies whether the Konnected Alarm Panel LED will blink on transmission of Wifi Commands
     * @param discovery identifies whether the Konnected Alarm Panel will be discoverable via UPnP
     */
    public boolean blink;
    public boolean discovery;
}
