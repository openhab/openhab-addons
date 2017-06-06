package org.openhab.binding.insteonplm.internal.device.commands;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.X10ThingHandler;
import org.openhab.binding.insteonplm.internal.device.X10CommandHandler;
import org.openhab.binding.insteonplm.internal.device.X10DeviceFeature;
import org.openhab.binding.insteonplm.internal.message.X10Command;
import org.openhab.binding.insteonplm.internal.message.modem.SendX10Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler to do the x10 version of percentage.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class X10PercentCommandHandler extends X10CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(X10PercentCommandHandler.class);

    X10PercentCommandHandler(X10DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(X10ThingHandler conf, ChannelUID channelId, Command cmd) {
        //
        // I did not have hardware that would respond to the PRESET_DIM codes.
        // This code path needs testing.
        //
        SendX10Message mess = new SendX10Message(conf.getAddress());
        conf.enqueueMessage(mess);
        PercentType pc = (PercentType) cmd;
        logger.debug("{}: changing level of {} to {}", nm(), conf.getAddress(), pc.intValue());
        int level = (pc.intValue() * 32) / 100;
        X10Command cmdCode = (level >= 16) ? X10Command.PreSetDim2 : X10Command.PreSetDim;
        level = level % 16;
        if (level <= 0) {
            level = 0;
        }
        SendX10Message sendMessage = new SendX10Message(cmdCode, (byte) s_X10CodeForLevel[level]);
        conf.enqueueMessage(sendMessage);
    }

    static private final int[] s_X10CodeForLevel = { 0, 8, 4, 12, 2, 10, 6, 14, 1, 9, 5, 13, 3, 11, 7, 15 };
}
