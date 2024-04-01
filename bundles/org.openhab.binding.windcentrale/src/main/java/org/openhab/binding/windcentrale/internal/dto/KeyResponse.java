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
package org.openhab.binding.windcentrale.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provides the details required for getting tokens using SRP from the Windcentrale Cognito user pool.
 *
 * @see <a href="https://mijn.windcentrale.nl/api/v0/labels/key?domain=mijn.windcentrale.nl">
 *      https://mijn.windcentrale.nl/api/v0/labels/key?domain=mijn.windcentrale.nl</a>
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class KeyResponse {

    public String clientId = "";
    public String region = "";
    public String userPoolId = "";
}
