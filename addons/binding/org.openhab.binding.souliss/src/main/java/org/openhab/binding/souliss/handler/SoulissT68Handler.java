package org.openhab.binding.souliss.handler;

import org.eclipse.smarthome.core.thing.Thing;

/**
 * The {@link SoulissT68Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 */
public class SoulissT68Handler extends SoulissT6nHandler {

    private float analogSetpointValue;

    // constructor
    public SoulissT68Handler(Thing _thing) {
        super(_thing);
    }
}
