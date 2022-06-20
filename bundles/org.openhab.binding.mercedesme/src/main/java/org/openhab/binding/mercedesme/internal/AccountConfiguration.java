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
package org.openhab.binding.mercedesme.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AccountConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AccountConfiguration {

    public String clientId = Constants.NOT_SET;
    public String clientSecret = Constants.NOT_SET;

    // Advanced Parameters
    public String callbackIP = Constants.NOT_SET;
    public int callbackPort = -1;
    public String scope = Constants.NOT_SET;

    @Override
    public String toString() {
        return "ID " + clientId + ", Secret " + clientSecret + ", IP " + callbackIP + ", Port " + callbackPort
                + ", scope " + scope;
    }
}
