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
package org.openhab.binding.mybmw.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.utils.Constants;

/**
 * The {@link MyBMWConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MyBMWConfiguration {

    /**
     * Depending on the location the correct server needs to be called
     */
    public String region = Constants.EMPTY;

    /**
     * MyBMW App Username
     */
    public String userName = Constants.EMPTY;

    /**
     * MyBMW App Password
     */
    public String password = Constants.EMPTY;

    /**
     * Preferred Locale language
     */
    public String language = Constants.LANGUAGE_AUTODETECT;
}
