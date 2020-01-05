package org.openhab.binding.network.internal.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class LatencyParserTest {

    @Test
    public void parseLinuxAndMacResultFoundTest() {
        // Arrange
        LatencyParser latencyParser = new LatencyParser();
        String input = "64 bytes from 192.168.1.1: icmp_seq=0 ttl=64 time=1.225 ms";

        // Act
        Optional<Double> resultLatency = latencyParser.parseLatency(input);

        // Assert
        Assert.assertTrue(resultLatency.isPresent());
        Assert.assertEquals(1.225, resultLatency.get(), 0);
    }

    @Test
    public void parseLinuxAndMacResultNotFoundTest() {
        // Arrange
        LatencyParser latencyParser = new LatencyParser();
        // This is the output of the command. We exclude the line which contains the latency, because here we want
        // to test that no latency is returned for all other lines.
        String[] inputLines = {
                "ping -c 1 192.168.1.1",
                "PING 192.168.1.1 (192.168.1.1): 56 data bytes",
                // "64 bytes from 192.168.1.1: icmp_seq=0 ttl=64 time=1.225 ms",
                "--- 192.168.1.1 ping statistics ---",
                "1 packets transmitted, 1 packets received, 0.0% packet loss",
                "round-trip min/avg/max/stddev = 1.225/1.225/1.225/0.000 ms"
        };

        for (String inputLine : inputLines) {
            // Act
            Optional<Double> resultLatency = latencyParser.parseLatency(inputLine);

            // Assert
            Assert.assertFalse(resultLatency.isPresent());
        }
    }
}
