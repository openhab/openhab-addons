/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.verisure.internal;

import java.math.BigDecimal;

import org.openhab.binding.verisure.internal.handler.VerisureBridgeHandler;

/**
 * Configuration class for {@link VerisureBridgeHandler} bridge used to connect to the
 * Verisure MyPage.
 *
 * @author Jarle Hjortland - Initial contribution
 */
public class VerisureBridgeConfiguration {
    public String username;
    public String password;
    public BigDecimal refresh;
    public BigDecimal pin;
    public BigDecimal numberOfInstallations;
}
