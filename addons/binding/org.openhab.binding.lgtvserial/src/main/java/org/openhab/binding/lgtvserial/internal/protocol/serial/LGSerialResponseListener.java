/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgtvserial.internal.protocol.serial;

import org.eclipse.smarthome.core.thing.ChannelUID;

/**
 * This interface represents a listener that will handle the transition after the response
 * from the device is received to update the items.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public interface LGSerialResponseListener {

    int getSetID();

    void onSuccess(ChannelUID channel, LGSerialResponse response);

    void onFailure(ChannelUID channel, LGSerialResponse response);

}
