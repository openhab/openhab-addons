package org.openhab.binding.atlona.internal.pro3;

public class AtlonaPro3Utilities {
    /**
     * Helper method to create a channel id from a group with no port number attached
     *
     * @param group a group name
     * @param channelId the channel id
     * @return group + "#" + channelId
     */
    public static String createChannelID(String group, String channelId) {
        return group + "#" + channelId;
    }

    /**
     * Helper method to create a channel id from a group, port number and channel id
     *
     * @param group the group name
     * @param portNbr the port number
     * @param channelId the channel id
     * @return group + portNbr + "#" + channelId
     */
    public static String createChannelID(String group, int portNbr, String channelId) {
        return group + portNbr + "#" + channelId;
    }
}
