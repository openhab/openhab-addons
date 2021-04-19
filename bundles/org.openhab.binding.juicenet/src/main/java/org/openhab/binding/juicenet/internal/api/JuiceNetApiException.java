package org.openhab.binding.juicenet.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.juicenet.internal.handler.JuiceNetBridgeHandler;

/**
 * The {@link JuiceNetBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetApiException extends Exception {

    public JuiceNetApiException(String message) {
        super(message);
    }
}
