/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.ism8.server;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingStatus;

/**
 * The {@link IDataPointChangeListener} is in interface for a data point changed consumer
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public interface IDataPointChangeListener {
    /**
     * This method will be called in case a data-point has changed.
     *
     */
    void dataPointChanged(DataPointChangedEvent e);

    /**
     * This method will be called in case the connection status has changed.
     *
     */
    void connectionStatusChanged(ThingStatus status);
}
