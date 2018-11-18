/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.runtime;

import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * Runtime API keeps some runtime information used by multiple services. By doing this we simply limit the binding to
 * support just 1 instance of the bridge, but to support more would require much more work than just changing this. Also
 * avoided issues with OSGI dependencies since otherwise either discovery service would need to depend on handlers or
 * vice versa. Neither of which actually worked cleanly
 *
 * @author Lubos Housa - Initial Contribution
 */
public interface Runtime {

    /**
     * Detect the runtime bridgeUID
     */
    ThingUID getBridgeUID();

    /**
     * Sets the runtime bridge UID
     *
     * @param bridgeUID new value to set
     */
    void setBridgeUID(ThingUID bridgeUID);
}
