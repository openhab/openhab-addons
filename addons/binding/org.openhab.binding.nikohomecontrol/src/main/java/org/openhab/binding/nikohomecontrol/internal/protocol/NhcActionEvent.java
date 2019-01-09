/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.nikohomecontrol.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NhcActionEvent} interface is used to pass action events received from the Niko Home Control controller to
 * the consuming client. It is designed to pass events to openHAB handlers that implement this interface. Because of
 * the design, the org.openhab.binding.nikohomecontrol.internal.protocol package can be extracted and used independent
 * of openHAB.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public interface NhcActionEvent {

    /**
     * This method is called when an action event is received from the Niko Home Control controller.
     *
     * @param state
     */
    public void actionEvent(int state);

}
