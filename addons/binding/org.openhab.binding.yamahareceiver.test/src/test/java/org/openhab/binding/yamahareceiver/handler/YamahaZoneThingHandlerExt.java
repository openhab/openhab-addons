package org.openhab.binding.yamahareceiver.handler;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.yamahareceiver.internal.protocol.IStateUpdatable;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;

/**
 * We need to extend the {@link YamahaZoneThingHandler} to make some protected methods
 * of super classes available for tests and turn the asynchronous update behaviour into
 * a synchronous one.
 *
 * @author David Graeff - Initial contribution
 */
public class YamahaZoneThingHandlerExt extends YamahaZoneThingHandler {
    private YamahaBridgeHandler bridgeHandler;
    Exception updateException = null;

    public YamahaZoneThingHandlerExt(Thing thing, YamahaBridgeHandler bridgeHandler) {
        super(thing);
        this.bridgeHandler = bridgeHandler;
    }

    /**
     * Don't overwrite the mocked thing with a newly created thing from a ThingBuilder.
     */
    @Override
    protected void updateThing(Thing thing) {
    }

    @Override
    protected YamahaBridgeHandler getBridgeHandler() {
        return bridgeHandler;
    }

    @Override
    protected boolean isLinked(String channelId) {
        return true;
    }

    // Do not update asynchronously for the test
    @Override
    protected void updateAsyncMakeOfflineIfFail(IStateUpdatable stateUpdateable) {
        try {
            stateUpdateable.update();
        } catch (IOException | ReceivedMessageParseException e) {
            if (updateException == null) {
                updateException = e;
            }
        }
    }
}
