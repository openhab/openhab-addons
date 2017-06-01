/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.handler;

import java.math.BigDecimal;

/**
 * @author Karel Goderis - Initial contribution
 */
public class BasicConfig {

    private String address;
    private Boolean fetch;
    private BigDecimal interval;

    public String getAddress() {
        return address;
    }

    public Boolean getFetch() {
        return fetch;
    }

    public BigDecimal getInterval() {
        return interval;
    }

}
