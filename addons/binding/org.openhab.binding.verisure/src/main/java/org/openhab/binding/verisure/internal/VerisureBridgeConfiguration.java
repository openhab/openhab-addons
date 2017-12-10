/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal;

import java.math.BigDecimal;

import org.openhab.binding.verisure.handler.VerisureBridgeHandler;

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
    public String pin;
}
