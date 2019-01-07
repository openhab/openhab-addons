/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Contains the handler configuration and default values. The field names represent the configuration names,
 * do not rename them if you don't intend to break the configuration interface.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class NetworkHandlerConfiguration {
    public String hostname = "";
    public @Nullable Integer port;
    public Integer retry = 1;
    public Integer refreshInterval = 60000;
    public Integer timeout = 5000;
}
