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
