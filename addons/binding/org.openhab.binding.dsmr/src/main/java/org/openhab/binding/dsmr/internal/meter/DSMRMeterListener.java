/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.meter;

import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;

/**
 * The DSMRMeterListener provides the interface for listeners for new meter values
 * for this meter
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public interface DSMRMeterListener {
    /**
     * This method is called when a new meter value is received.
     *
     * The value received is a {@link CosemObject}
     *
     * @param newMeterValue {@link CosemObject} containing the new meter value
     */
    public void meterValueReceived(CosemObject newMeterValue);
}
