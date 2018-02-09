/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.type;

import org.openhab.binding.homematic.internal.model.HmDevice;

/**
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface HomematicTypeGenerator {

    /**
     * Initializes the type generator.
     */
    public void initialize();

    /**
     * Generates the ThingType and ChannelTypes for the given device.
     */
    public void generate(HmDevice device);

    /**
     * Validates all devices for multiple firmware versions. Different firmware versions for the same device may have
     * different datapoints which may cause warnings in the logfile.
     */
    public void validateFirmwares();

}
