/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.matter.internal.util;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.Endpoint;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster.OctetString;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BasicInformationCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BridgedDeviceBasicInformationCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DescriptorCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DescriptorCluster.DeviceTypeStruct;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DeviceTypes;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.FixedLabelCluster;

/**
 * Utility class for creating and manipulating the labels for Matter devices.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MatterLabelUtils {

    public static String normalizeString(@Nullable String input) {
        /*
         * \\p{C}: Matches control characters (category Cc).
         * \\p{Z}: Matches whitespace characters.
         * &&[^\\u0020]: Excludes the regular space (\u0020) from removal.
         */
        return input == null ? "" : input.replaceAll("[\\p{C}\\p{Z}&&[^\\u0020]]", "").trim();
    }

    public static String labelForNode(Endpoint root) {
        String label = "";
        String vendorName = "";
        String productName = "";
        String nodeLabel = "";

        BaseCluster cluster = root.clusters.get(BasicInformationCluster.CLUSTER_NAME);
        if (cluster != null && cluster instanceof BasicInformationCluster basicCluster) {
            vendorName = normalizeString(basicCluster.vendorName);
            productName = normalizeString(basicCluster.productName);
            nodeLabel = normalizeString(basicCluster.nodeLabel);
        }

        if (!nodeLabel.isEmpty()) {
            label = nodeLabel;
        } else {
            label = productName.startsWith(vendorName) ? productName : vendorName + " " + productName;
        }

        return label.trim();
    }

    public static String labelForEndpoint(Endpoint endpoint) {
        Map<String, BaseCluster> clusters = endpoint.clusters;
        Object basicInfoObject = clusters.get(BasicInformationCluster.CLUSTER_NAME);

        Integer deviceTypeID = primaryDeviceTypeForEndpoint(endpoint);

        // labels will look like "Device Type : Custom Node Label Or Product Label"
        final StringBuffer label = new StringBuffer(splitAndCapitalize(DeviceTypes.DEVICE_MAPPING.get(deviceTypeID)))
                .append(": ");

        // Check if a "nodeLabel" is set, otherwise use the product label. This varies from vendor to vendor
        if (basicInfoObject != null) {
            BasicInformationCluster basicInfo = (BasicInformationCluster) basicInfoObject;
            String basicInfoString = normalizeString(basicInfo.nodeLabel);
            label.append(!basicInfoString.isEmpty() ? basicInfoString : normalizeString(basicInfo.productLabel));
        }

        // Fixed labels are a way of vendors to label endpoints with additional meta data.
        if (clusters.get(FixedLabelCluster.CLUSTER_NAME) instanceof FixedLabelCluster fixedLabelCluster) {
            fixedLabelCluster.labelList
                    .forEach(fixedLabel -> label.append(" " + fixedLabel.label + " " + fixedLabel.value));
        }

        // label for the Group Channel
        return label.toString().trim();
    }

    public static String labelForBridgeEndpoint(Endpoint endpoint) {
        Map<String, BaseCluster> clusters = endpoint.clusters;
        Object basicInfoObject = clusters.get(BridgedDeviceBasicInformationCluster.CLUSTER_NAME);

        // labels will look like "Device Type : Custom Node Label Or Product Label"
        final StringBuffer label = new StringBuffer();
        // Check if a "nodeLabel" is set, otherwise use the product label. This varies from vendor to vendor
        if (basicInfoObject != null) {
            BridgedDeviceBasicInformationCluster basicInfo = (BridgedDeviceBasicInformationCluster) basicInfoObject;
            String nodeLabel = normalizeString(basicInfo.nodeLabel);
            String productLabel = normalizeString(basicInfo.productLabel);

            if (!nodeLabel.isEmpty()) {
                label.append(nodeLabel);
            } else {
                label.append(productLabel);
            }
        }

        if (label.isEmpty()) {
            Integer deviceTypeID = primaryDeviceTypeForEndpoint(endpoint);
            String deviceTypeLabel = splitAndCapitalize(DeviceTypes.DEVICE_MAPPING.get(deviceTypeID));
            label.append(deviceTypeLabel + " (" + endpoint.number.toString() + ")");
        }

        // Fixed labels are a way of vendors to label endpoints with additional meta data.
        if (clusters.get(FixedLabelCluster.CLUSTER_NAME) instanceof FixedLabelCluster fixedLabelCluster) {
            fixedLabelCluster.labelList
                    .forEach(fixedLabel -> label.append(" " + fixedLabel.label + " " + fixedLabel.value));
        }

        // label for the Group Channel
        return label.toString().trim();
    }

    public static String splitAndCapitalize(@Nullable String camelCase) {
        if (camelCase == null) {
            return "";
        }
        return Pattern.compile("(?<=[a-z])(?=[A-Z])").splitAsStream(camelCase)
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    public static Integer primaryDeviceTypeForEndpoint(Endpoint endpoint) {
        // The matter spec requires a descriptor cluster and device type, so this should always be present
        DescriptorCluster descriptorCluster = (DescriptorCluster) endpoint.clusters.get(DescriptorCluster.CLUSTER_NAME);
        Integer deviceTypeID = -1;
        if (descriptorCluster != null && !descriptorCluster.deviceTypeList.isEmpty()) {
            for (DeviceTypeStruct ds : descriptorCluster.deviceTypeList) {
                // ignore bridge types
                if (!DeviceTypes.BRIDGED_NODE.equals(ds.deviceType) && !DeviceTypes.AGGREGATOR.equals(ds.deviceType)) {
                    deviceTypeID = ds.deviceType;
                    break;
                }
            }
            if (deviceTypeID == -1) {
                deviceTypeID = descriptorCluster.deviceTypeList.get(0).deviceType;
            }
        }
        return deviceTypeID;
    }

    public static String formatMacAddress(@Nullable OctetString mac) {
        if (mac == null) {
            return "";
        }
        String macString = mac.toString();
        return IntStream.range(0, macString.length()).filter(i -> i % 2 == 0)
                .mapToObj(i -> macString.substring(i, i + 2)).collect(Collectors.joining(":"));
    }

    public static String formatIPv4Address(@Nullable OctetString os) {
        if (os == null || os.value == null || os.value.length != 4) {
            return "";
        }

        byte[] addr = os.value;
        return (addr[0] & 0xFF) + "." + (addr[1] & 0xFF) + "." + (addr[2] & 0xFF) + "." + (addr[3] & 0xFF);
    }

    public static String formatIPv6Address(@Nullable OctetString os) {
        if (os == null || os.value == null || os.value.length != 16) {
            return "";
        }

        try {
            // passing in host=null ensures this call does not block or throw
            Inet6Address ip = (Inet6Address) Inet6Address.getByAddress(null, os.value, -1);
            return ip.getHostAddress();
        } catch (UnknownHostException e) {
            return os.toHexString();
        }
    }
}
