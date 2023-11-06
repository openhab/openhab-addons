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
package org.openhab.binding.icloud;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.icloud.internal.ICloudBindingConstants.THING_TYPE_ICLOUDDEVICE;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openhab.binding.icloud.internal.ICloudApiResponseException;
import org.openhab.binding.icloud.internal.ICloudBindingConstants;
import org.openhab.binding.icloud.internal.ICloudService;
import org.openhab.binding.icloud.internal.handler.ICloudDeviceHandler;
import org.openhab.binding.icloud.internal.handler.dto.json.response.ICloudAccountDataResponse;
import org.openhab.binding.icloud.internal.utilities.JsonUtils;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.storage.json.internal.JsonStorage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * Class to test/experiment with iCloud api.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public class TestICloud {

    private final String iCloudTestEmail;
    private final String iCloudTestPassword;

    private final Logger logger = LoggerFactory.getLogger(TestICloud.class);

    @BeforeEach
    public void setUp() {
        final Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (logger instanceof ch.qos.logback.classic.Logger qLogger) {
            qLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
        }
    }

    public TestICloud() {
        String sysPropMail = System.getProperty("icloud.test.email");
        String sysPropPW = System.getProperty("icloud.test.pw");
        iCloudTestEmail = sysPropMail != null ? sysPropMail : "notset";
        iCloudTestPassword = sysPropPW != null ? sysPropPW : "notset";
    }

    @Test
    @EnabledIfSystemProperty(named = "icloud.test.email", matches = ".*", disabledReason = "Only for manual execution.")
    public void testAuth() throws IOException, InterruptedException, ICloudApiResponseException, JsonSyntaxException {
        File jsonStorageFile = new File(System.getProperty("user.home"), "openhab.json");
        logger.info(jsonStorageFile.toString());

        JsonStorage<String> stateStorage = new JsonStorage<String>(jsonStorageFile, TestICloud.class.getClassLoader(),
                0, 1000, 1000, List.of());

        ICloudService service = new ICloudService(iCloudTestEmail, iCloudTestPassword, stateStorage);
        service.authenticate(false);
        if (service.requires2fa()) {
            PrintStream consoleOutput = System.out;
            if (consoleOutput != null) {
                consoleOutput.print("Code: ");
            }
            @SuppressWarnings("resource")
            Scanner in = new Scanner(System.in);
            String code = in.nextLine();
            assertTrue(service.validate2faCode(code));
            if (!service.isTrustedSession()) {
                service.trustSession();
            }
            if (!service.isTrustedSession()) {
                logger.info("Trust failed!!!");
            }
        }
        ICloudAccountDataResponse deviceInfo = JsonUtils.fromJson(service.getDevices().refreshClient(),
                ICloudAccountDataResponse.class);
        assertNotNull(deviceInfo);
        stateStorage.flush();
    }

    @Test
    @EnabledIfSystemProperty(named = "icloud.test.email", matches = ".*", disabledReason = "Only for manual execution.")
    public void testDiscovery() {
        String icloudDeviceRespond = """
                         {
                     "userInfo": {
                         "accountFormatter": 0,
                         "firstName": "Firstname",
                         "lastName": "Lastname",
                         "membersInfo": {
                             "XXX~": {
                                 "accountFormatter": 0,
                                 "firstName": "Firstname",
                                 "lastName": "Lastname",
                                 "deviceFetchStatus": "LOADING",
                                 "useAuthWidget": true,
                                 "isHSA": true,
                                 "appleId": "dummy@dummy.local"
                             }
                         },
                         "hasMembers": true
                     },
                     "alert": null,
                     "serverContext": {
                         "minCallbackIntervalInMS": 5000,
                         "preferredLanguage": "de-de",
                         "enable2FAFamilyActions": false,
                         "lastSessionExtensionTime": null,
                         "validRegion": true,
                         "callbackIntervalInMS": 2000,
                         "enableMapStats": true,
                         "timezone": {
                             "currentOffset": -25200000,
                             "previousTransition": 1678615199999,
                             "previousOffset": -28800000,
                             "tzCurrentName": "Pacific Daylight Time",
                             "tzName": "America/Los_Angeles"
                         },
                         "authToken": null,
                         "maxCallbackIntervalInMS": 60000,
                         "classicUser": false,
                         "isHSA": true,
                         "trackInfoCacheDurationInSecs": 86400,
                         "imageBaseUrl": "https://statici.icloud.com",
                         "minTrackLocThresholdInMts": 100,
                         "itemLearnMoreURL": "https://support.apple.com/kb/HT211331?viewlocale=de_DE",
                         "maxLocatingTime": 90000,
                         "itemsTabEnabled": true,
                         "sessionLifespan": 900000,
                         "info": "",
                         "prefsUpdateTime": 1679378160178,
                         "useAuthWidget": true,
                         "inaccuracyRadiusThreshold": 200,
                         "clientId": "",
                         "serverTimestamp": 1679640300550,
                         "enable2FAFamilyRemove": false,
                         "deviceImageVersion": "22",
                         "macCount": 0,
                         "deviceLoadStatus": "200",
                         "maxDeviceLoadTime": 60000,
                         "prsId": 146786264,
                         "showSllNow": false,
                         "cloudUser": true,
                         "enable2FAErase": false
                     },
                     "userPreferences": {
                         "webPrefs": {
                             "id": "web_prefs",
                             "selectedDeviceId": "XXX"
                         }
                     },
                     "content": [
                         {
                             "msg": null,
                             "activationLocked": true,
                             "passcodeLength": 6,
                             "features": {
                                 "BTR": false,
                                 "LLC": false,
                                 "CLK": false,
                                 "TEU": true,
                                 "SND": true,
                                 "ALS": false,
                                 "CLT": false,
                                 "SVP": false,
                                 "SPN": false,
                                 "XRM": false,
                                 "NWF": true,
                                 "CWP": false,
                                 "MSG": true,
                                 "LOC": true,
                                 "LME": false,
                                 "LMG": false,
                                 "LYU": false,
                                 "LKL": false,
                                 "LST": true,
                                 "LKM": false,
                                 "WMG": true,
                                 "SCA": false,
                                 "PSS": false,
                                 "EAL": false,
                                 "LAE": false,
                                 "PIN": false,
                                 "LCK": true,
                                 "REM": true,
                                 "MCS": false,
                                 "KEY": false,
                                 "KPD": false,
                                 "WIP": true
                             },
                             "scd": false,
                             "id": "device.id",
                             "remoteLock": null,
                             "rm2State": 0,
                             "modelDisplayName": "iPad",
                             "fmlyShare": false,
                             "lostModeCapable": true,
                             "wipedTimestamp": null,
                             "encodedDeviceId": null,
                             "scdPh": "",
                             "locationCapable": true,
                             "trackingInfo": null,
                             "name": "My iPad",
                             "isMac": false,
                             "thisDevice": false,
                             "deviceClass": "iPad",
                             "nwd": false,
                             "remoteWipe": null,
                             "canWipeAfterLock": true,
                             "baUUID": "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF",
                             "lostModeEnabled": false,
                             "wipeInProgress": false,
                             "deviceStatus": "200",
                             "deviceColor": "1-1-0",
                             "isConsideredAccessory": false,
                             "deviceWithYou": false,
                             "lowPowerMode": false,
                             "rawDeviceModel": "iPad13,1",
                             "deviceDiscoveryId": "AAAAAAAA-FFFF-FFFF-FFFF-FFFFFFFFFFFF",
                             "isLocating": true,
                             "lostTimestamp": "",
                             "mesg": null,
                             "batteryLevel": 0.1599999964237213,
                             "locationEnabled": true,
                             "lockedTimestamp": null,
                             "locFoundEnabled": false,
                             "snd": null,
                             "lostDevice": null,
                             "deviceDisplayName": "iPad Air (4th Generation)",
                             "prsId": null,
                             "audioChannels": [

                             ],
                             "batteryStatus": "NotCharging",
                             "location": {
                                 "isOld": false,
                                 "isInaccurate": false,
                                 "altitude": 0.0,
                                 "positionType": "Unknown",
                                 "secureLocation": null,
                                 "secureLocationTs": 0,
                                 "latitude": 20.00000000000000,
                                 "floorLevel": 0,
                                 "horizontalAccuracy": 1.0,
                                 "locationType": "",
                                 "timeStamp": 1679640074735,
                                 "locationFinished": true,
                                 "verticalAccuracy": 0.0,
                                 "locationMode": null,
                                 "longitude": 10.00000000000000
                             },
                             "deviceModel": "FourthGen-1-1-0",
                             "maxMsgChar": 160,
                             "darkWake": false
                         },
                         {
                             "msg": null,
                             "activationLocked": true,
                             "passcodeLength": 6,
                             "features": {
                                 "BTR": false,
                                 "LLC": false,
                                 "CLK": false,
                                 "TEU": true,
                                 "SND": true,
                                 "ALS": false,
                                 "CLT": false,
                                 "SVP": false,
                                 "SPN": false,
                                 "XRM": false,
                                 "NWF": true,
                                 "CWP": false,
                                 "MSG": true,
                                 "LOC": true,
                                 "LME": false,
                                 "LMG": false,
                                 "LYU": false,
                                 "LKL": false,
                                 "LST": true,
                                 "LKM": false,
                                 "WMG": true,
                                 "SCA": false,
                                 "PSS": false,
                                 "EAL": false,
                                 "LAE": false,
                                 "PIN": false,
                                 "LCK": true,
                                 "REM": true,
                                 "MCS": false,
                                 "KEY": false,
                                 "KPD": false,
                                 "WIP": true
                             },
                             "scd": false,
                             "id": "device.id",
                             "remoteLock": null,
                             "rm2State": 0,
                             "modelDisplayName": "iPad",
                             "fmlyShare": false,
                             "lostModeCapable": true,
                             "wipedTimestamp": null,
                             "encodedDeviceId": null,
                             "scdPh": "",
                             "locationCapable": true,
                             "trackingInfo": null,
                             "name": "iPad Air without ID",
                             "isMac": false,
                             "thisDevice": false,
                             "deviceClass": "iPad",
                             "nwd": false,
                             "remoteWipe": null,
                             "canWipeAfterLock": true,
                             "baUUID": "",
                             "lostModeEnabled": false,
                             "wipeInProgress": false,
                             "deviceStatus": "200",
                             "deviceColor": "1-1-0",
                             "isConsideredAccessory": false,
                             "deviceWithYou": false,
                             "lowPowerMode": false,
                             "rawDeviceModel": "iPad13,1",
                             "deviceDiscoveryId": "",
                             "isLocating": true,
                             "lostTimestamp": "",
                             "mesg": null,
                             "batteryLevel": 0.1599999964237213,
                             "locationEnabled": true,
                             "lockedTimestamp": null,
                             "locFoundEnabled": false,
                             "snd": null,
                             "lostDevice": null,
                             "deviceDisplayName": "iPad",
                             "prsId": null,
                             "audioChannels": [

                             ],
                             "batteryStatus": "NotCharging",
                             "location": {
                                 "isOld": false,
                                 "isInaccurate": false,
                                 "altitude": 0.0,
                                 "positionType": "Unknown",
                                 "secureLocation": null,
                                 "secureLocationTs": 0,
                                 "latitude": 20.00000000000000,
                                 "floorLevel": 0,
                                 "horizontalAccuracy": 1.0,
                                 "locationType": "",
                                 "timeStamp": 1679640074735,
                                 "locationFinished": true,
                                 "verticalAccuracy": 0.0,
                                 "locationMode": null,
                                 "longitude": 10.00000000000000
                             },
                             "deviceModel": "FourthGen-1-1-0",
                             "maxMsgChar": 160,
                             "darkWake": false
                         }
                     ],
                     "statusCode": "200"
                 }
                """;
        ICloudAccountDataResponse deviceInfo = JsonUtils.fromJson(icloudDeviceRespond, ICloudAccountDataResponse.class);

        // Check device with discoveryId
        ThingImpl thing = createThing("AAAAAAAA-FFFF-FFFF-FFFF-FFFFFFFFFFFF");
        ICloudDeviceHandler handler = createDeviceHandler(thing);

        handler.deviceInformationUpdate(deviceInfo.getICloudDeviceInformationList());

        assertEquals(ThingStatus.ONLINE, handler.getThing().getStatus());

        // Check device without discoveryId
        thing = createThing("iPad Air without ID");
        handler = createDeviceHandler(thing);

        handler.deviceInformationUpdate(deviceInfo.getICloudDeviceInformationList());

        assertEquals(ThingStatus.ONLINE, handler.getThing().getStatus());
    }

    /**
     * @return
     */
    private ThingImpl createThing(String deviceId) {
        String deviceIdHash = Integer.toHexString(deviceId.hashCode());

        ThingUID uid = new ThingUID(THING_TYPE_ICLOUDDEVICE, "dummyBridgeId", deviceIdHash);
        ThingImpl thing = new ThingImpl(THING_TYPE_ICLOUDDEVICE, uid);
        thing.getConfiguration().put(ICloudBindingConstants.DEVICE_PROPERTY_ID, deviceId);
        return thing;
    }

    /**
     * @param thing
     * @return
     */
    private ICloudDeviceHandler createDeviceHandler(ThingImpl thing) {
        ICloudDeviceHandler handler = new ICloudDeviceHandler(thing);
        handler.setCallback(new ThingHandlerCallback() {

            @Override
            public void validateConfigurationParameters(Channel channel, Map<String, Object> configurationParameters) {
            }

            @Override
            public void validateConfigurationParameters(Thing thing, Map<String, Object> configurationParameters) {
            }

            @Override
            public void thingUpdated(Thing thing) {
            }

            @Override
            public void statusUpdated(Thing thing, ThingStatusInfo thingStatus) {
                thing.setStatusInfo(thingStatus);
            }

            @Override
            public void stateUpdated(ChannelUID channelUID, State state) {
            }

            @Override
            public void postCommand(ChannelUID channelUID, Command command) {
            }

            @Override
            public void migrateThingType(Thing thing, ThingTypeUID thingTypeUID, Configuration configuration) {
            }

            @Override
            public boolean isChannelLinked(ChannelUID channelUID) {
                return false;
            }

            @Override
            public @Nullable ConfigDescription getConfigDescription(ThingTypeUID thingTypeUID) {
                return null;
            }

            @Override
            public @Nullable ConfigDescription getConfigDescription(ChannelTypeUID channelTypeUID) {
                return null;
            }

            @Override
            public @Nullable Bridge getBridge(ThingUID bridgeUID) {
                return null;
            }

            @Override
            public ChannelBuilder editChannel(Thing thing, ChannelUID channelUID) {
                return ChannelBuilder.create(channelUID); // dummy implementation, probably won't work.
            }

            @Override
            public List<ChannelBuilder> createChannelBuilders(ChannelGroupUID channelGroupUID,
                    ChannelGroupTypeUID channelGroupTypeUID) {
                return new ArrayList<ChannelBuilder>(); // dummy implementation, probably won't work.
            }

            @Override
            public ChannelBuilder createChannelBuilder(ChannelUID channelUID, ChannelTypeUID channelTypeUID) {
                return ChannelBuilder.create(channelUID); // dummy implementation, probably won't work.
            }

            @Override
            public void configurationUpdated(Thing thing) {
            }

            @Override
            public void channelTriggered(Thing thing, ChannelUID channelUID, String event) {
            }
        });
        handler.initialize();
        return handler;
    }
}
