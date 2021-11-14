package org.openhab.binding.openwebnet.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openwebnet4j.message.Where;
import org.slf4j.*;
import java.util.Set;

/**
 * The {@link OpenWebNetAuxiliaryHandler} is responsible for handling Auxiliary (AUX) commands/messages
 * for an OpenWebNet device.
 * It extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution
 * @author Giovanni Fabiani - Auxiliary message support
 */
@NonNullByDefault
public class OpenWebNetAuxiliaryHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetAuxiliaryHandler.class);

    public static final Set<ThingTypeUID>SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.AUX_SUPPORTED_THING_TYPES;

    private static long lastAllDevicesRefreshTS = -1; // timestamp when the last request for all device refresh was sent
                                                      // for this handler

    protected static final int ALL_DEVICES_REFRESH_INTERVAL_MSEC = 60000; // interval in msec before sending another
                                                                          // all devices refresh request


    public OpenWebNetAuxiliaryHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {

    }

    @Override
    protected void requestChannelState(ChannelUID channel) {

    }

    @Override
    protected void refreshDevice(boolean refreshAll) {

    }

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        return null;
    }

    @Override
    protected String ownIdPrefix() {
        return null;
    }

    /**
     * @param channelId the channelId string
     * @return a WHERE address string based on channelId string
     */
    private String toWhere(String channelId) {
        Where w = deviceWhere;
        if (w != null) {
            OpenWebNetBridgeHandler brH = bridgeHandler;
            if (brH != null)
                if (brH.isBusGateway()) {
                    return w.value();
                }
        }
        return null;
    }
}