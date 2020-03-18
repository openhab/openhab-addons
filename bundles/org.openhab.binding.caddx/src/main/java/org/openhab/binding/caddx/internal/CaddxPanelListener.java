package org.openhab.binding.caddx.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface CaddxPanelListener {
    public void caddxMessage(CaddxCommunicator communicator, CaddxMessage message);
}
