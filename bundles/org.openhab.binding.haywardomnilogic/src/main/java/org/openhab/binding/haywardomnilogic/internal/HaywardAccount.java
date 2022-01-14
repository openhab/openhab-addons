/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.haywardomnilogic.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HaywardAccount} class contains fields mapping thing configuration parameters.
 *
 * @author Matt Myers - Initial contribution
 */

@NonNullByDefault
public class HaywardAccount {
    public String token = "";
    public String mspSystemID = "";
    public String userID = "";
    public String backyardName = "";
    public String address = "";
    public String firstName = "";
    public String lastName = "";
    public String roleType = "";
    public String units = "";
    public String vspSpeedFormat = "";
}
