package org.openhab.binding.insteonplm.internal.device.messages;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.message.modem.StandardMessageReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message handler that processes replies to queries.
 * If command2 == 0xFF then the light has been turned on
 * else if command2 == 0x00 then the light has been turned off
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */

public class SwitchRequestReplyHandler extends MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(SwitchRequestReplyHandler.class);

    SwitchRequestReplyHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, StandardMessageReceived msg, Channel f) {
        InsteonAddress a = handler.getAddress();
        int cmd2 = msg.getCmd2();
        if (getButton() < 0) {
            handleNoButtons(handler, f, cmd2, a, msg);
        } else {
            boolean isOn = isLEDLit(cmd2, getButton());
            logger.info("{}: dev {} button {} switched to {}", nm(), a, getButton(), isOn ? "ON" : "OFF");
            handler.updateFeatureState(f, isOn ? OnOffType.ON : OnOffType.OFF);
        }

    }

    /**
     * Handle the case where no buttons have been configured.
     * In this situation, the only return values should be 0 (light off)
     * or 0xff (light on)
     *
     * @param cmd2
     */
    void handleNoButtons(InsteonThingHandler handler, Channel f, int cmd2, InsteonAddress a,
            StandardMessageReceived msg) {
        if (cmd2 == 0) {
            logger.info("{}: set device {} to OFF", nm(), a);
            handler.updateFeatureState(f, OnOffType.OFF);
        } else if (cmd2 == 0xff) {
            logger.info("{}: set device {} to ON", nm(), a);
            handler.updateFeatureState(f, OnOffType.ON);
        } else {
            logger.warn("{}: {} ignoring unexpected cmd2 in msg: {}", nm(), a, msg);
        }
    }

    /**
     * Test if cmd byte indicates that button is lit.
     * The cmd byte has the LED status bitwise from the left:
     * 87654321
     * Note that the 2487S has buttons assigned like this:
     * 22|6543|11
     * They used the basis of the 8-button remote, and assigned
     * the ON button to 1+2, the OFF button to 7+8
     *
     * @param cmd cmd byte as received in message
     * @param button button to test (number in range 1..8)
     * @return true if button is lit, false otherwise
     */
    private boolean isLEDLit(int cmd, int button) {
        boolean isSet = (cmd & (0x1 << (button - 1))) != 0;
        logger.trace("cmd: {} button {}", Integer.toBinaryString(cmd), button);
        logger.trace("msk: {} isSet: {}", Integer.toBinaryString(0x1 << (button - 1)), isSet);
        return (isSet);
    }
}
