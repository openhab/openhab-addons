package org.openhab.binding.souliss.internal.config;

/**
 * The {@link GatewayConfig} is responsible for holding souliss gateway config
 *
 * @author Luca Calcaterra - Initial Contribution
 */
public final class GatewayConfig {
    public int pingInterval;
    public int subscriptionInterval;
    public int healthyInterval;
    public int sendInterval;
    public int timeoutToRequeue;
    public int timeoutToRemovePacket;
    public int preferredLocalPortNumber;
    public int gatewayPortNumber;
    public int userIndex;
    public int nodeIndex;
    public String gatewayIpAddress = "";
}
