/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.internal;

import java.math.BigDecimal;

/**
 * Contains the handler configuration and default values. The field names represent the configuration names,
 * do not rename them if you don't intend to break the configuration interface.
 *
 * @author Gerrit Beine
 */
public class FroniusHandlerConfiguration {
    public String hostname;
    public BigDecimal device = BigDecimal.ONE;
    public BigDecimal refreshInterval = BigDecimal.valueOf(60000);
}
