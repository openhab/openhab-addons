/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
