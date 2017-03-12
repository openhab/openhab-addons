package org.openhab.binding.insteonplm.handler;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.InsteonPLMBindingConstants;
import org.openhab.binding.insteonplm.internal.config.PollingHandlerInfo;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.message.modem.X10MessageReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class X10ThingHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(X10ThingHandler.class);
    private Map<ChannelUID, List<DeviceFeature>> featureChannelMapping = Maps.newHashMap();
    private Map<ChannelUID, PollingHandlerInfo> pollHandlers = Maps.newHashMap();
    private PriorityQueue<InsteonThingMessageQEntry> requestQueue = new PriorityQueue<InsteonThingMessageQEntry>();

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
        for (DeviceFeature feature : featureChannelMapping.get(channelUID)) {
            feature.handleCommand(this, channelUID, command);
        }
    }

    public void handlerX10Message(X10MessageReceived x10Mess) {
        // X10 Message.
        byte rawX10 = x10Mess.getRawX10();
        int cmd = (rawX10 & 0x0f);
        for (ChannelUID channelId : featureChannelMapping.keySet()) {
            List<DeviceFeature> features = featureChannelMapping.get(channelId);
            for (DeviceFeature feature : features) {
                List<MessageHandler> allHandlers = feature.getMsgHandlers().get(cmd & 0xff);
                if (allHandlers != null) {
                    for (MessageHandler handler : allHandlers) {
                        if (handler.matches(x10Mess)) {
                            handler.handleMessage(this, -1, (byte) cmd, message,
                                    getThing().getChannel(channelId.getId()));
                        }
                    }
                }
            }
        }
    }
}
