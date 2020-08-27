/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.elkm1.internal;

/**
 * The listener for the elk m1 handler.
 *
 * @author David Bennett - Initial Contribution
 */

public interface ElkM1HandlerListener {
    /** Called when a zone is discovered. */
    public void onZoneDiscovered(int zoneNum, String label);

    /** Called when an area is discovered. */
    public void onAreaDiscovered(int thingNum, String text);
}
