package org.openhab.binding.insteonplm.internal.config;

import org.openhab.binding.insteonplm.InsteonPLMBindingConstants;
import org.openhab.binding.insteonplm.InsteonPLMBindingConstants.ExtendedData;
import org.openhab.binding.insteonplm.internal.device.PollHandler;
import org.openhab.binding.insteonplm.internal.message.StandardInsteonMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pulls the poll handler details out of the configuration, which is then used to
 * setup and initialize the poll handler. Do this to verify the format of the system
 * during the config parsing phase.
 *
 * @author David Bennett - Initial Contribution
 */
public class PollingHandlerInfo {
    private Logger logger = LoggerFactory.getLogger(PollingHandlerInfo.class);
    byte data1 = 0;
    byte data2 = 0;
    byte data3 = 0;
    StandardInsteonMessages cmd1;
    byte cmd2 = 0;
    ExtendedData extendedData;
    String pollHandlerType;
    private PollHandler pollHandler;

    public PollingHandlerInfo(String pollHandlerData) {
        String[] typeArgs = pollHandlerData.split(":");
        if (typeArgs.length != 2) {
            logger.error("Invalid poll data {}", pollHandlerData);
        } else {
            pollHandlerType = typeArgs[0];
            String[] args = typeArgs[1].split(",");
            extendedData = InsteonPLMBindingConstants.ExtendedData.valueOf(args[0]);
            cmd1 = StandardInsteonMessages.fromByte(fromHexString(args[1]));
            cmd2 = fromHexString(args[2]);
            if (args.length > 3) {
                data1 = fromHexString(args[3]);
                data2 = fromHexString(args[4]);
                data3 = fromHexString(args[5]);
            }
        }
    }

    public String getPollHandlerType() {
        return pollHandlerType;
    }

    public void setPollHandler(PollHandler handler) {
        this.pollHandler = handler;
        pollHandler.setCmd1(cmd1);
        pollHandler.setCmd2(cmd2);
        pollHandler.setExtended(extendedData);
        pollHandler.setData2(data2);
        pollHandler.setData3(data3);
    }

    public PollHandler getPollHandler() {
        return pollHandler;
    }

    private byte fromHexString(String str) {
        if (str.startsWith("0x")) {
            return Byte.parseByte(str.substring(2));
        } else {
            return Byte.parseByte(str);
        }
    }
}
