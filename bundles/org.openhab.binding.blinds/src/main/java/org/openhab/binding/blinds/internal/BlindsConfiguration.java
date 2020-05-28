/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.blinds.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BlindsConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Markus Pfleger - Initial contribution
 */
@NonNullByDefault
public class BlindsConfiguration {

    public Integer openSlatDelay = 0; // in s
    public Integer closeSlatDelay = 0; // in s

    @Override
    public String toString() {
        return "openSlatDelay=" + openSlatDelay + ", closeSlatDelay=" + closeSlatDelay;
    }

}
