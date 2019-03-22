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
package org.openhab.binding.domintell.internal.config;

import java.math.BigDecimal;

/**
 * The {@link ContactItemConfig} class contains configuration for contact channels
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class ContactItemConfig {
    private Boolean inverted;
    private BigDecimal resetTimeout;

    //getters
    public Boolean isInverted() {
        return inverted != null ? inverted: false;
    }

    public Integer getResetTimeout() {
        return resetTimeout != null ? resetTimeout.intValue(): 0;
    }
}
