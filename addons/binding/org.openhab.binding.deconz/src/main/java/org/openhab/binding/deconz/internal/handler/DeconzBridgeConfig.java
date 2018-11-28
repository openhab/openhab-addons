/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link DeconzBridgeConfig} class holds the configuration properties of the bridge.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class DeconzBridgeConfig {
    public String host = "";
    public @Nullable String apikey;
    int timeout = 1000;
}
