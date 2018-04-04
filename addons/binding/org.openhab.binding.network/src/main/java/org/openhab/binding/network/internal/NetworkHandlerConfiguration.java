/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal;

import java.math.BigDecimal;

/**
 * Contains the handler configuration and default values. The field names represent the configuration names,
 * do not rename them if you don't intend to break the configuration interface.
 *
 * @author David Graeff
 */
public class NetworkHandlerConfiguration {
    public String hostname;
    public BigDecimal port;
    public BigDecimal retry = BigDecimal.valueOf(1);
    public BigDecimal refreshInterval = BigDecimal.valueOf(60000);
    public BigDecimal timeout = BigDecimal.valueOf(5000);
}
