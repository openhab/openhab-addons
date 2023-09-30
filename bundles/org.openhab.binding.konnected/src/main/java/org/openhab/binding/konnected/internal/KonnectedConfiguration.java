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
package org.openhab.binding.konnected.internal;

import org.openhab.core.config.core.Configuration;

/**
 * The {@link KonnectedConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Zachary Christiansen - Initial contribution
 */
public class KonnectedConfiguration extends Configuration {

    /**
     * blink identifies whether the Konnected Alarm Panel LED will blink on transmission of Wifi Commands.
     */
    public boolean blink;

    /**
     * discovery identifies whether the Konnected Alarm Panel will be discoverable via UPnP.
     */
    public boolean discovery;
}
