package org.openhab.binding.amazondashbutton.internal.config;

/**
 * The configuration of the Amazon Dash Button
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class AmazonDashButtonConfig {

    /**
     * The MAC address of the Amazon Dash Button
     */
    public String macAddress;

    /**
     * The network interface which receives the packets of the Amazon Dash Button
     */
    public String pcapNetworkInterfaceName;

    /**
     * Often a single button press is recognized multiple times. You can specify how long any further detected button
     * pressed should be ignored after one click is handled (in ms).
     */
    public Integer packetInterval;
}
