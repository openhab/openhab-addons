package org.openhab.binding.insteonplm.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.insteonplm.InsteonPLMBindingConstants;
import org.openhab.binding.insteonplm.internal.device.X10Address;
import org.openhab.binding.insteonplm.internal.device.X10DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.X10MessageHandler;
import org.openhab.binding.insteonplm.internal.message.modem.SendX10Message;
import org.openhab.binding.insteonplm.internal.message.modem.X10MessageReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Handler for all the x10 devices.
 *
 * @author David Bennett - Initial Contribution
 */
public class X10ThingHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(X10ThingHandler.class);
    private Map<ChannelUID, List<X10DeviceFeature>> featureChannelMapping = Maps.newHashMap();
    private X10Address address;

    public X10ThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        String productKey = this.getThing().getProperties()
                .get(InsteonPLMBindingConstants.PROPERTY_INSTEON_PRODUCT_KEY);
        if (productKey == null) {
            logger.error("Product Key is not set in {}", this.getThing().getUID());
            return;
        }

        // TODO: Shouldn't the framework do this for us???
        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeStatusChanged(bridge.getStatusInfo());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Lookup the features to do stuff.
        for (X10DeviceFeature feature : featureChannelMapping.get(channelUID)) {
            feature.handleCommand(this, channelUID, command);
        }
    }

    public void handlerX10Message(X10MessageReceived x10Mess) {
        // X10 Message.
        for (ChannelUID channelId : featureChannelMapping.keySet()) {
            List<X10DeviceFeature> features = featureChannelMapping.get(channelId);
            for (X10DeviceFeature feature : features) {
                List<X10MessageHandler> allHandlers = feature.getMsgHandlers().get(x10Mess.getCmd().ordinal());
                if (allHandlers != null) {
                    for (X10MessageHandler handler : allHandlers) {
                        if (handler.matches(x10Mess)) {
                            handler.handleMessage(this, x10Mess, getThing().getChannel(channelId.getId()));
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates the channel based on the new input state. This will lookup the correct channel
     * from the feature details and then update it.
     *
     * @param f the feature details to use for lookup
     * @param newState the new state to broadcast
     */
    public void updateFeatureState(Channel channel, State newState) {
        updateState(channel.getUID(), newState);
    }

    public void enqueueMessage(SendX10Message message) {

    }

    public X10Address getAddress() {
        return this.address;
    }
}
