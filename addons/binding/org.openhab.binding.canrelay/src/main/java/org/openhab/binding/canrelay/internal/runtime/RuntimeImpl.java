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
import org.osgi.service.component.annotations.Component;

/**
 * Default runtime implementation
 *
 * @author Lubos Housa - Initial Contribution
 */
@Component(service = Runtime.class, immediate = true, configurationPid = "runtime.canrelay")
public class RuntimeImpl implements Runtime {

    private ThingUID bridgeUID;

    @Override
    public ThingUID getBridgeUID() {
        return bridgeUID;
    }

    @Override
    public void setBridgeUID(ThingUID bridgeUID) {
        this.bridgeUID = bridgeUID;
    }

}
