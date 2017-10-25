/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wso2iots.internal.jsonLight;

import java.math.BigDecimal;

/**
 * The {@link Values} class returns light value
 * from json wso2iotsresponse string
 *
 * @author Ramesha Karunasena - Initial contribution
 */

public class Values {

    private BigDecimal light;

    public BigDecimal getData() {
        return light;
    }
}
