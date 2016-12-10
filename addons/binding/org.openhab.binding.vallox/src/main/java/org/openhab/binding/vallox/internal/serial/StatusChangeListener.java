/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vallox.internal.serial;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;

/**
 * Simple interface to allow some listener to listen to status changes.
 *
 * @author Hauke Fuhrmann - Initial contribution
 *
 */
public interface StatusChangeListener {

    public void statusChanged(ThingStatus status, ThingStatusDetail detail, String message);

}
