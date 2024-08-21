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
package org.openhab.binding.siemensrds.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RdsCloudConfiguration} class contains the thing configuration
 * parameters for the Climatix IC cloud user account
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class RdsCloudConfiguration {

    public String userEmail = "";
    public String userPassword = "";
    public int pollingInterval;
    public String apiKey = "";
}
