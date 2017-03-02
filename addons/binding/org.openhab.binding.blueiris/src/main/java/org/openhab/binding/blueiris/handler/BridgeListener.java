package org.openhab.binding.blueiris.handler;

import org.openhab.binding.blueiris.internal.data.CamListReply;

public interface BridgeListener {

    void onCamList(CamListReply camListReply);

}
