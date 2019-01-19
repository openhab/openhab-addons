/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgtvserial.internal.protocol.serial;

import org.eclipse.smarthome.core.types.State;

/**
 * This interface represents the information from a response received and parsed to a State object from the device.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public interface LGSerialResponse {

    int getSetID();

    State getState();

    boolean isSuccess();

}
