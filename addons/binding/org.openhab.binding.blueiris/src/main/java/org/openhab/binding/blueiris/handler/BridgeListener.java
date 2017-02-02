/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blueiris.handler;

import org.openhab.binding.blueiris.internal.data.CamListReply;

/**
 * Listens to the bridge to see if anything changed.
 *
 * @author David Bennett - Initial Contribution
 */
public interface BridgeListener {
    void onCamList(CamListReply camListReply);
}
