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
package org.openhab.binding.renault.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RenaultConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Doug Culnane - Initial contribution
 */
@NonNullByDefault
public class RenaultConfiguration {

    public String accountType = "MYRENAULT";
    public String myRenaultUsername = "";
    public String myRenaultPassword = "";
    public String locale = "";
    public String vin = "";
    public int refreshInterval = 10;
    public int updateDelay = 30;
    public String kamereonApiKey = "YjkKtHmGfaceeuExUDKGxrLZGGvtVS0J";
}
