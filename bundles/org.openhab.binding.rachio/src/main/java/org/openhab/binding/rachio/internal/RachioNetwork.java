/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.util.ArrayList;

import org.apache.commons.lang.Validate;
import org.apache.commons.net.util.SubnetUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioHttp;
// import com.offbynull.portmapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * RachioNetwork: Implement network related functions
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioNetwork {
    private final Logger logger = LoggerFactory.getLogger(RachioNetwork.class);

    public class AwsIpAddressRange {
        @SerializedName("ip_prefix")
        public String ipPrefix = "";
        public String region = "";
        public String service = "";
    }

    public class AwsIpList {
        public String syncToken = "";
        public String createDate = "";
        public ArrayList<AwsIpAddressRange> prefixes = new ArrayList<>();
    }

    private ArrayList<AwsIpAddressRange> awsIpRanges = new ArrayList<>();

    public boolean initializeAwsList() throws RachioApiException {
        try {
            // Get currently assigned IP address ranges from AWS cloud.
            // The Rachio cloud service is hosted on AWS. The list will function as a filter to make sure that the
            // webhook call was originated by the AWS infrastructure - not perfect security, but helps to avoid abuse
            // and protect OH in
            // some kind
            RachioHttp http = new RachioHttp("");
            String jsonList = http.httpGet(AWS_IPADDR_DOWNLOAD_URL, "").resultString;
            Gson gson = new Gson();
            Validate.notNull(gson);
            AwsIpList list = gson.fromJson(jsonList, AwsIpList.class);
            Validate.notNull(list);
            for (int i = 0; i < list.prefixes.size(); i++) {
                AwsIpAddressRange entry = list.prefixes.get(i);
                Validate.notNull(entry);
                if (entry.region.startsWith(AWS_IPADDR_REGION_FILTER)) {
                    logger.trace("RachioNetwork: Adding range '{}' (region '{}' to AWS IP address list", entry.ipPrefix,
                            entry.region);
                    awsIpRanges.add(entry);
                }
            }
            logger.debug(
                    "RachioNetwork: AWS address list initialized, {} entries (will be used to verify inboud Rachio events)",
                    awsIpRanges.size());
            return true;
        } catch (RuntimeException e) {
            logger.warn("RachioNetwork: Unable to initialize: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Checks if client ip equals or is in range of ip networks provided by
     * semicolon separated list
     *
     * @param clientIp in numeric form like "192.168.0.10"
     * @param ipList like "127.0.0.1;192.168.0.0/24;10.0.0.0/8"
     * @return true if client ip from the list os ips and networks
     */
    @SuppressWarnings("null")
    public static boolean isIpInSubnet(String clientIp, String ipList) {
        if ((ipList == null) || ipList.isEmpty()) {
            // No ip address provided
            return true;
        }
        String[] subnetMasks = ipList.split(";");
        for (String subnetMask : subnetMasks) {
            subnetMask = subnetMask.trim();
            if (clientIp.equals(subnetMask)) {
                return true;
            }
            if (subnetMask.contains("/")) {
                if (new SubnetUtils(subnetMask).getInfo().isInRange(clientIp)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("null")
    public boolean isIpInAwsList(String clientIp) {
        if (awsIpRanges.size() == 0) {
            // filtering not enabled
            return true;
        }

        for (int i = 0; i < awsIpRanges.size(); i++) {
            AwsIpAddressRange e = awsIpRanges.get(i);
            if ((e != null) && isIpInSubnet(clientIp, e.ipPrefix)) {
                return true;
            }
        }
        return false;
    }
}
