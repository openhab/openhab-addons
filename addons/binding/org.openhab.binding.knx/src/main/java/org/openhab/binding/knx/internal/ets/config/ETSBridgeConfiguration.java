/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.ets.config;

import org.openhab.binding.knx.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.config.BridgeConfiguration;

/**
 * {@link KNXBridgeBaseThingHandler} configuration
 *
 * @author Karel Goderis - Initial contribution
 *
 */
public class ETSBridgeConfiguration extends BridgeConfiguration {

    private String knxProj;

    public String getKnxProj() {
        return knxProj;
    }
}
