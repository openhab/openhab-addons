package org.openhab.binding.souliss.internal.discovery;

import java.net.InetAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result callback interface.
 */
@NonNullByDefault
public interface DiscoverResult {
    static boolean IS_GATEWAY_DETECTED = false;

    void gatewayDetected(InetAddress addr, String id);

    void thingDetectedTypicals(byte lastByteGatewayIP, byte typical, byte node, byte slot);

    void thingDetectedActionMessages(String sTopicNumber, String sTopicVariant);
}