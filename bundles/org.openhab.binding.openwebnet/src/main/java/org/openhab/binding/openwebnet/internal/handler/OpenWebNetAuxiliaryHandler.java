package org.openhab.binding.openwebnet.internal.handler;
import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.*;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.message.Auxiliary;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereAuxiliary;
import org.openwebnet4j.message.Who;
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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.AUX_SUPPORTED_THING_TYPES;

    private static long lastAllDevicesRefreshTS = -1; // timestamp when the last request for all device refresh was sent
    // for this handler

    protected static final int ALL_DEVICES_REFRESH_INTERVAL_MSEC = 60000; // interval in msec before sending another

    // all devices refresh request
    public OpenWebNetAuxiliaryHandler(Thing thing) {
        super(thing);
    }

    /**
     * Handles Auxiliary switch command for a channel
     *
     * @param channel the channel
     * @param command the Command
     */
    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {
        logger.debug("handleSwitchCommand() (command={} - channel={})", command, channel);
        if (channel.getId().equals(CHANNEL_SWITCH)) {
            if (command instanceof OnOffType) {
                try {
                    if (OnOffType.ON.equals(command)) {
                        send(Auxiliary.requestTurnOn(toWhere(channel.getId())));
                    } else if (OnOffType.OFF.equals(command)) {
                        send(Auxiliary.requestTurnOff(toWhere(channel.getId())));
                    }

                } catch (OWNException e) {
                    logger.warn("Exception while processing command {}: {}", command, e.getMessage());
                }
            } else {
                logger.warn("Unsupported ChannelUID {}", channel);
            }
        }
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {

    }

    @Override
    protected void refreshDevice(boolean refreshAll) {

    }

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        return new WhereAuxiliary(wStr);
    }

    @Override
    protected String ownIdPrefix() {
        return Who.AUX.value().toString();
    }

    /**
     * @param channelId the channelId string
     * @return a WHERE address string based on channelId string
     */
    @Nullable
    private String toWhere(String channelId) {
        Where w = deviceWhere;
        if (w != null) {
            OpenWebNetBridgeHandler brH = bridgeHandler;
            if (brH != null) {
                return w.value();
            }else if (channelId.equals(CHANNEL_SWITCH)){
                return w.value();
            }
        }
        return null;
    }
}


