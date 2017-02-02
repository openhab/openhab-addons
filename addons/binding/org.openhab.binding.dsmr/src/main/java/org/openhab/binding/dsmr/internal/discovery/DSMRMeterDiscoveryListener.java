/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.discovery;

import org.openhab.binding.dsmr.internal.meter.DSMRMeterDescriptor;

/**
 * This interface is notified of new meter discoveries
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public interface DSMRMeterDiscoveryListener {
    /**
     * A new meter is discovered
     *
     * The implementation must return if the new discovered meter is accepted.
     *
     * @param meterDescriptor {@link DSMRMeterDescriptor} describing the new meter
     * @return true if the new meter is accepted, false otherwise
     */
    public boolean meterDiscovered(DSMRMeterDescriptor meterDescriptor);
}
