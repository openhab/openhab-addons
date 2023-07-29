/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ecovacs.internal.api.impl;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecovacs.internal.api.EcovacsDevice;
import org.openhab.binding.ecovacs.internal.api.EcovacsDevice.EventListener;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.xml.CleaningInfo;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.xml.DeviceInfo;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.xml.WaterSystemInfo;
import org.openhab.binding.ecovacs.internal.api.model.ChargeMode;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.openhab.binding.ecovacs.internal.api.util.XPathUtils;
import org.slf4j.Logger;
import org.w3c.dom.Node;

import com.google.gson.Gson;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
class XmlReportParser implements ReportParser {
    private final EcovacsDevice device;
    private final EventListener listener;
    private final Gson gson;
    private final Logger logger;

    XmlReportParser(EcovacsDevice device, EventListener listener, Gson gson, Logger logger) {
        this.device = device;
        this.listener = listener;
        this.gson = gson;
        this.logger = logger;
    }

    @Override
    public void handleMessage(String eventName, String payload) throws DataParsingException {
        switch (eventName.toLowerCase()) {
            case "batteryinfo":
                listener.onBatteryLevelUpdated(device, DeviceInfo.parseBatteryInfo(payload));
                break;
            case "chargestate": {
                ChargeMode mode = DeviceInfo.parseChargeInfo(payload, gson);
                if (mode == ChargeMode.RETURNING) {
                    listener.onCleaningModeUpdated(device, CleanMode.RETURNING, Optional.empty());
                }
                listener.onChargingStateUpdated(device, mode == ChargeMode.CHARGING);
                break;
            }
            case "cleanreport": {
                CleaningInfo.CleanStateInfo info = CleaningInfo.parseCleanStateInfo(payload, gson);
                if (info.mode == CleanMode.CUSTOM_AREA) {
                    logger.debug("{}: Custom area cleaning stated with area definition {}", device.getSerialNumber(),
                            info.areaDefinition);
                }
                listener.onCleaningModeUpdated(device, info.mode, info.areaDefinition);
                // Full report:
                // <ctl td='CleanReport'><clean type='auto' speed='standard' st='s' rsn='a'/></ctl>
                break;
            }
            case "cleanrptbgdata": {
                Node fromChargerNode = XPathUtils.getFirstXPathMatch(payload, "//@IsFrmCharger");
                if ("1".equals(fromChargerNode.getNodeValue())) {
                    // Device just started cleaning, but likely won't send us a ChargeState report,
                    // so update charging state from here
                    listener.onChargingStateUpdated(device, false);
                }
                // Full report:
                // <ctl td='CleanRptBgdata' ts='1643044172' Battery='102' CleanID='1333688018' iCleanID='0497265223'
                // MapID='1430814334' rsn='a' IsFrmCharger='1' CleanType='auto' Speed='standard' OnOffRag='0'
                // WorkMode='s' Spray='2' WorkArea='002'/>
                break;
            }
            case "cleanst": {
                String area = XPathUtils.getFirstXPathMatch(payload, "//@a").getNodeValue();
                String duration = XPathUtils.getFirstXPathMatch(payload, "//@l").getNodeValue();
                listener.onCleaningStatsUpdated(device, Integer.valueOf(area), Integer.valueOf(duration));
                break;
            }
            case "error":
                DeviceInfo.parseErrorInfo(payload).ifPresent(errorCode -> {
                    listener.onErrorReported(device, errorCode);
                });
                break;
            case "waterboxinfo":
                listener.onWaterSystemPresentUpdated(device, WaterSystemInfo.parseWaterBoxInfo(payload));
                break;
        }
    }
}
