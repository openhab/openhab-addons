/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
