package org.openhab.binding.lgtvserial.internal.protocol.serial;

import org.eclipse.smarthome.core.thing.ChannelUID;

public interface LGSerialResponseListener {

    int getSetID();

    void onSuccess(ChannelUID channel, LGSerialResponse response);

    void onFailure(ChannelUID channel, LGSerialResponse response);

}
