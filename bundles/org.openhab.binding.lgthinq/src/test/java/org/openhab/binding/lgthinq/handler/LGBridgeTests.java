/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.handler;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;
import static org.openhab.binding.lgthinq.internal.LGThinqBindingConstants.*;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.ws.rs.core.UriBuilder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.lgthinq.internal.LGThinqBindingConstants;
import org.openhab.binding.lgthinq.internal.LGThinqConfiguration;
import org.openhab.binding.lgthinq.internal.api.RestUtils;
import org.openhab.binding.lgthinq.internal.api.TokenManager;
import org.openhab.binding.lgthinq.internal.handler.LGBridgeHandler;
import org.openhab.binding.lgthinq.lgapi.LGApiClientService;
import org.openhab.binding.lgthinq.lgapi.LGApiV1ClientServiceImpl;
import org.openhab.binding.lgthinq.lgapi.LGApiV2ClientServiceImpl;
import org.openhab.binding.lgthinq.lgapi.model.LGDevice;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

/**
 * The {@link LGBridgeTests}
 *
 * @author Nemer Daud - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@WireMockTest(httpPort = 8880)
class LGBridgeTests {
    private static final Logger logger = LoggerFactory.getLogger(LGBridgeTests.class);
    private String fakeBridgeName = "fakeBridgeId";
    private String fakeLanguage = "pt-BR";
    private String fakeCountry = "BR";
    private String fakeUserName = "someone@some.url";
    private String fakePassword = "somepassword";
    private String gtwResponse = "{\n" + "   \"resultCode\":\"0000\",\n" + "   \"result\":{\n"
            + "      \"countryCode\":\"BR\",\n" + "      \"languageCode\":\"pt-BR\",\n"
            + "      \"thinq1Uri\":\"http://localhost:8880/api\",\n"
            + "      \"thinq2Uri\":\"http://localhost:8880/v1\",\n" + "      \"empUri\":\"http://localhost:8880\",\n"
            + "      \"empSpxUri\":\"http://localhost:8880/spx\",\n" + "      \"rtiUri\":\"localhost:8880\",\n"
            + "      \"mediaUri\":\"localhost:8880\",\n" + "      \"appLatestVer\":\"4.0.14230\",\n"
            + "      \"appUpdateYn\":\"N\",\n" + "      \"appLink\":\"market://details?id=com.lgeha.nuts\",\n"
            + "      \"uuidLoginYn\":\"N\",\n" + "      \"lineLoginYn\":\"N\",\n" + "      \"lineChannelId\":\"\",\n"
            + "      \"cicTel\":\"4004-5400\",\n" + "      \"cicUri\":\"\",\n" + "      \"isSupportVideoYn\":\"N\",\n"
            + "      \"countryLangDescription\":\"Brasil/Português\",\n"
            + "      \"empTermsUri\":\"http://localhost:8880\",\n"
            + "      \"googleAssistantUri\":\"https://assistant.google.com/services/invoke/uid/000000d26892b8a3\",\n"
            + "      \"smartWorldUri\":\"\",\n" + "      \"racUri\":\"br.rac.lgeapi.com\",\n"
            + "      \"cssUri\":\"https://aic-common.lgthinq.com\",\n"
            + "      \"cssWebUri\":\"http://s3-an2-op-t20-css-web-resource.s3-website.ap-northeast-2.amazonaws.com\",\n"
            + "      \"iotssUri\":\"https://aic-iotservice.lgthinq.com\",\n" + "      \"chatBotUri\":\"\",\n"
            + "      \"autoOrderSetUri\":\"\",\n" + "      \"autoOrderManageUri\":\"\",\n"
            + "      \"aiShoppingUri\":\"\",\n" + "      \"onestopCall\":\"\",\n"
            + "      \"onestopEngineerUri\":\"\",\n" + "      \"hdssUri\":\"\",\n" + "      \"amazonDrsYn\":\"N\",\n"
            + "      \"features\":{\n" + "         \"supportTvIoTServerYn\":\"Y\",\n"
            + "         \"disableWeatherCard\":\"Y\",\n" + "         \"thinqCss\":\"Y\",\n"
            + "         \"bleConfirmYn\":\"Y\",\n" + "         \"tvRcmdContentYn\":\"Y\",\n"
            + "         \"supportProductManualYn\":\"N\",\n" + "         \"clientDbYn\":\"Y\",\n"
            + "         \"androidAutoYn\":\"Y\",\n" + "         \"searchYn\":\"Y\",\n"
            + "         \"thinqFaq\":\"Y\",\n" + "         \"thinqNotice\":\"Y\",\n"
            + "         \"groupControlYn\":\"Y\",\n" + "         \"inAppReviewYn\":\"Y\",\n"
            + "         \"cicSupport\":\"Y\",\n" + "         \"qrRegisterYn\":\"Y\",\n"
            + "         \"supportBleYn\":\"Y\"\n" + "      },\n" + "      \"serviceCards\":[\n" + "         \n"
            + "      ],\n" + "      \"uris\":{\n"
            + "         \"takeATourUri\":\"https://s3-us2-op-t20-css-contents.s3.us-west-2.amazonaws.com/workexperience-new/ios/no-version/index.html\",\n"
            + "         \"gscsUri\":\"https://gscs-america.lge.com\",\n"
            + "         \"amazonDartUri\":\"https://shs.lgthinq.com\"\n" + "      }\n" + "   }\n" + "}";
    private String preLoginResponse = "{\n" + "   \"encrypted_pw\":\"SOME_DUMMY_ENC_PWD\",\n"
            + "   \"signature\":\"SOME_DUMMY_SIGNATURE\",\n" + "   \"tStamp\":\"1643236928\"\n" + "}";
    private String userIdType = "LGE";
    private String loginSessionId = "emp;11111111;222222222";
    private String loginSessionResponse = "{\n" + "   \"account\":{\n" + "      \"loginSessionID\":\"" + loginSessionId
            + "\",\n" + "      \"userID\":\"" + fakeUserName + "\",\n" + "      \"userIDType\":\"" + userIdType
            + "\",\n" + "      \"dateOfBirth\":\"05-05-1978\",\n" + "      \"country\":\"BR\",\n"
            + "      \"countryName\":\"Brazil\",\n" + "      \"blacklist\":\"N\",\n" + "      \"age\":\"43\",\n"
            + "      \"isSubscribe\":\"N\",\n" + "      \"changePw\":\"N\",\n" + "      \"toEmailId\":\"N\",\n"
            + "      \"periodPW\":\"N\",\n" + "      \"lgAccount\":\"Y\",\n" + "      \"isService\":\"Y\",\n"
            + "      \"userNickName\":\"faker\",\n" + "      \"termsList\":[\n" + "         \n" + "      ],\n"
            + "      \"userIDList\":[\n" + "         {\n" + "            \"lgeIDList\":[\n" + "               {\n"
            + "                  \"lgeIDType\":\"LGE\",\n" + "                  \"userID\":\"" + fakeUserName + "\"\n"
            + "               }\n" + "            ]\n" + "         }\n" + "      ],\n" + "      \"serviceList\":[\n"
            + "         {\n" + "            \"svcCode\":\"SVC202\",\n" + "            \"svcName\":\"LG ThinQ\",\n"
            + "            \"isService\":\"Y\",\n" + "            \"joinDate\":\"30-04-2020\"\n" + "         },\n"
            + "         {\n" + "            \"svcCode\":\"SVC710\",\n" + "            \"svcName\":\"EMP OAuth\",\n"
            + "            \"isService\":\"Y\",\n" + "            \"joinDate\":\"30-04-2020\"\n" + "         }\n"
            + "      ],\n" + "      \"displayUserID\":\"faker\",\n" + "      \"notiList\":{\n"
            + "         \"totCount\":\"0\",\n" + "         \"list\":[\n" + "            \n" + "         ]\n"
            + "      },\n" + "      \"authUser\":\"N\",\n" + "      \"dummyIdFlag\":\"N\"\n" + "   }\n" + "}";
    private String userInfoReturned = "{\n" + "   \"status\":1,\n" + "   \"account\":{\n" + "      \"userID\":\""
            + fakeUserName + "\",\n" + "      \"userNo\":\"BR2005200239023\",\n" + "      \"userIDType\":\"LGE\",\n"
            + "      \"displayUserID\":\"faker\",\n" + "      \"userIDList\":[\n" + "         {\n"
            + "            \"lgeIDList\":[\n" + "               {\n" + "                  \"lgeIDType\":\"LGE\",\n"
            + "                  \"userID\":\"" + fakeUserName + "\"\n" + "               }\n" + "            ]\n"
            + "         }\n" + "      ],\n" + "      \"dateOfBirth\":\"05-05-1978\",\n" + "      \"country\":\"BR\",\n"
            + "      \"countryName\":\"Brazil\",\n" + "      \"blacklist\":\"N\",\n" + "      \"age\":\"45\",\n"
            + "      \"isSubscribe\":\"N\",\n" + "      \"changePw\":\"N\",\n" + "      \"toEmailId\":\"N\",\n"
            + "      \"periodPW\":\"N\",\n" + "      \"lgAccount\":\"Y\",\n" + "      \"isService\":\"Y\",\n"
            + "      \"userNickName\":\"faker\",\n" + "      \"authUser\":\"N\",\n" + "      \"serviceList\":[\n"
            + "         {\n" + "            \"isService\":\"Y\",\n" + "            \"svcName\":\"LG ThinQ\",\n"
            + "            \"svcCode\":\"SVC202\",\n" + "            \"joinDate\":\"29-05-2018\"\n" + "         },\n"
            + "         {\n" + "            \"isService\":\"Y\",\n" + "            \"svcName\":\"LG Developer\",\n"
            + "            \"svcCode\":\"SVC609\",\n" + "            \"joinDate\":\"29-05-2018\"\n" + "         },\n"
            + "         {\n" + "            \"isService\":\"Y\",\n" + "            \"svcName\":\"MC OAuth\",\n"
            + "            \"svcCode\":\"SVC710\",\n" + "            \"joinDate\":\"29-05-2018\"\n" + "         }\n"
            + "      ]\n" + "   }\n" + "}";
    private String dashboardListReturned = "{\n" + "   \"resultCode\":\"0000\",\n" + "   \"result\":{\n"
            + "      \"langPackCommonVer\":\"125.6\",\n"
            + "      \"langPackCommonUri\":\"https://objectcontent.lgthinq.com/f1cae877-1d1e-4c12-8010-acbcdcce2df1?hdnts=exp=1706183232~hmac=257aa8146a089de87496cb13aa0b43761a19e7db225558dfb8996919746b465b\",\n"
            + "      \"item\":[\n" + "         {\n" + "            \"modelName\":\"RAC_056905_WW\",\n"
            + "            \"subModelName\":\"\",\n" + "            \"deviceType\":401,\n"
            + "            \"deviceCode\":\"AI01\",\n" + "            \"alias\":\"Bedroom\",\n"
            + "            \"deviceId\":\"abra-cadabra-0001-5771\",\n" + "            \"fwVer\":\"2.5.8_RTOS_3K\",\n"
            + "            \"imageFileName\":\"ac_home_wall_airconditioner_img.png\",\n"
            + "            \"imageUrl\":\"https://objectcontent.lgthinq.com/9e0177e7-0956-4284-916d-61e213f1f5ab?hdnts=exp=1702098013~hmac=e14659e3ccf369930e4cc92ca2511203037d3c258b75c627af013e4656fc49d6\",\n"
            + "            \"smallImageUrl\":\"https://objectcontent.lgthinq.com/c7e214d7-99f0-4641-b954-f238f9d55b64?hdnts=exp=1701658820~hmac=646137b7b590866c772649d03114184628b1477eb974ca8507c0dc4ede6807c5\",\n"
            + "            \"ssid\":\"dummy-ssid\",\n" + "            \"macAddress\":\"74:40:be:92:ac:08\",\n"
            + "            \"networkType\":\"02\",\n" + "            \"timezoneCode\":\"America/Sao_Paulo\",\n"
            + "            \"timezoneCodeAlias\":\"Brazil/Sao Paulo\",\n" + "            \"utcOffset\":-3,\n"
            + "            \"utcOffsetDisplay\":\"-03:00\",\n" + "            \"dstOffset\":-2,\n"
            + "            \"dstOffsetDisplay\":\"-02:00\",\n" + "            \"curOffset\":-2,\n"
            + "            \"curOffsetDisplay\":\"-02:00\",\n"
            + "            \"sdsGuide\":\"{\\\"deviceCode\\\":\\\"AI01\\\"}\",\n" + "            \"newRegYn\":\"N\",\n"
            + "            \"remoteControlType\":\"\",\n" + "            \"modelJsonVer\":7.13,\n"
            + "            \"modelJsonUri\":\"https://aic.lgthinq.com:46030/api/webContents/modelJSON?modelName=modelJSON_401&countryCode=KR&contentsId=abra-cadabra-0001-5771&authKey=thinq\",\n"
            + "            \"appModuleVer\":12.49,\n"
            + "            \"appModuleUri\":\"https://objectcontent.lgthinq.com/19b24102-f2c5-4ac4-97aa-bb1abe5b4c2e?hdnts=exp=1704438018~hmac=050615be890fedc1669a632310dc837b9c6c6ebfd428ed202e2b4b19c2e05155\",\n"
            + "            \"appRestartYn\":\"Y\",\n" + "            \"appModuleSize\":6082481,\n"
            + "            \"langPackProductTypeVer\":59.9,\n"
            + "            \"langPackProductTypeUri\":\"https://objectcontent.lgthinq.com/5642d2e1-cb10-41b4-8e99-f1831f20afe6?hdnts=exp=1705462185~hmac=68fe0ae9ef3fd02355c87668cff6d36c2ad8c312144d7406b9c040be992a15ea\",\n"
            + "            \"langPackModelVer\":\"\",\n" + "            \"langPackModelUri\":\"\",\n"
            + "            \"deviceState\":\"E\",\n" + "            \"online\":false,\n"
            + "            \"platformType\":\"thinq1\",\n" + "            \"regDt\":2.0200909053555E13,\n"
            + "            \"modelProtocol\":\"STANDARD\",\n" + "            \"order\":0,\n"
            + "            \"drServiceYn\":\"N\",\n" + "            \"fwInfoList\":[\n" + "               {\n"
            + "                  \"partNumber\":\"SAA38690433\",\n" + "                  \"checksum\":\"00000000\",\n"
            + "                  \"verOrder\":0\n" + "               }\n" + "            ],\n"
            + "            \"guideTypeYn\":\"Y\",\n" + "            \"guideType\":\"RAC_TYPE1\",\n"
            + "            \"regDtUtc\":\"20200909073555\",\n" + "            \"regIndex\":0,\n"
            + "            \"groupableYn\":\"Y\",\n" + "            \"controllableYn\":\"Y\",\n"
            + "            \"combinedProductYn\":\"N\",\n" + "            \"masterYn\":\"Y\",\n"
            + "            \"pccModelYn\":\"N\",\n" + "            \"sdsPid\":{\n" + "               \"sds4\":\"\",\n"
            + "               \"sds3\":\"\",\n" + "               \"sds2\":\"\",\n" + "               \"sds1\":\"\"\n"
            + "            },\n" + "            \"autoOrderYn\":\"N\",\n"
            + "            \"modelNm\":\"RAC_056905_WW\",\n" + "            \"initDevice\":false,\n"
            + "            \"existsEntryPopup\":\"N\",\n" + "            \"tclcount\":0\n" + "         },\n"
            + "         {\n" + "            \"appType\":\"NUTS\",\n" + "            \"modelCountryCode\":\"WW\",\n"
            + "            \"countryCode\":\"BR\",\n" + "            \"modelName\":\"RAC_056905_WW\",\n"
            + "            \"deviceType\":401,\n" + "            \"deviceCode\":\"AI01\",\n"
            + "            \"alias\":\"Office\",\n" + "            \"deviceId\":\"abra-cadabra-0001-5772\",\n"
            + "            \"fwVer\":\"\",\n"
            + "            \"imageFileName\":\"ac_home_wall_airconditioner_img.png\",\n"
            + "            \"imageUrl\":\"https://objectcontent.lgthinq.com/9e0177e7-0956-4284-916d-61e213f1f5ab?hdnts=exp=1702098013~hmac=e14659e3ccf369930e4cc92ca2511203037d3c258b75c627af013e4656fc49d6\",\n"
            + "            \"smallImageUrl\":\"https://objectcontent.lgthinq.com/c7e214d7-99f0-4641-b954-f238f9d55b64?hdnts=exp=1701658820~hmac=646137b7b590866c772649d03114184628b1477eb974ca8507c0dc4ede6807c5\",\n"
            + "            \"ssid\":\"smart-gameficacao\",\n" + "            \"softapId\":\"\",\n"
            + "            \"softapPass\":\"\",\n" + "            \"macAddress\":\"\",\n"
            + "            \"networkType\":\"02\",\n" + "            \"timezoneCode\":\"America/Sao_Paulo\",\n"
            + "            \"timezoneCodeAlias\":\"Brazil/Sao Paulo\",\n" + "            \"utcOffset\":-3,\n"
            + "            \"utcOffsetDisplay\":\"-03:00\",\n" + "            \"dstOffset\":-2,\n"
            + "            \"dstOffsetDisplay\":\"-02:00\",\n" + "            \"curOffset\":-2,\n"
            + "            \"curOffsetDisplay\":\"-02:00\",\n"
            + "            \"sdsGuide\":\"{\\\"deviceCode\\\":\\\"AI01\\\"}\",\n" + "            \"newRegYn\":\"N\",\n"
            + "            \"remoteControlType\":\"\",\n" + "            \"userNo\":\"BR2004259832795\",\n"
            + "            \"tftYn\":\"N\",\n" + "            \"modelJsonVer\":12.11,\n"
            + "            \"modelJsonUri\":\"https://objectcontent.lgthinq.com/544a6f1c-1b10-4244-a584-d103c8519910?hdnts=exp=1706145774~hmac=bf5e96e83ffdac724b7159b8ed3d7c52f5b9a2a0ef8b67cdbbcf96b1113bd25f\",\n"
            + "            \"appModuleVer\":12.49,\n"
            + "            \"appModuleUri\":\"https://objectcontent.lgthinq.com/19b24102-f2c5-4ac4-97aa-bb1abe5b4c2e?hdnts=exp=1704438018~hmac=050615be890fedc1669a632310dc837b9c6c6ebfd428ed202e2b4b19c2e05155\",\n"
            + "            \"appRestartYn\":\"Y\",\n" + "            \"appModuleSize\":6082481,\n"
            + "            \"langPackProductTypeVer\":59.9,\n"
            + "            \"langPackProductTypeUri\":\"https://objectcontent.lgthinq.com/5642d2e1-cb10-41b4-8e99-f1831f20afe6?hdnts=exp=1705462185~hmac=68fe0ae9ef3fd02355c87668cff6d36c2ad8c312144d7406b9c040be992a15ea\",\n"
            + "            \"deviceState\":\"E\",\n" + "            \"snapshot\":{\n"
            + "               \"airState.windStrength\":8.0,\n" + "               \"airState.wMode.lowHeating\":0.0,\n"
            + "               \"airState.diagCode\":0.0,\n"
            + "               \"airState.lightingState.displayControl\":1.0,\n"
            + "               \"airState.wDir.hStep\":0.0,\n" + "               \"mid\":8.4615358E7,\n"
            + "               \"airState.energy.onCurrent\":476.0,\n"
            + "               \"airState.wMode.airClean\":0.0,\n"
            + "               \"airState.quality.sensorMon\":0.0,\n"
            + "               \"airState.energy.accumulatedTime\":0.0,\n"
            + "               \"airState.miscFuncState.antiBugs\":0.0,\n"
            + "               \"airState.tempState.target\":18.0,\n" + "               \"airState.operation\":1.0,\n"
            + "               \"airState.wMode.jet\":0.0,\n" + "               \"airState.wDir.vStep\":2.0,\n"
            + "               \"timestamp\":1.643248573766E12,\n" + "               \"airState.powerSave.basic\":0.0,\n"
            + "               \"airState.quality.PM10\":0.0,\n" + "               \"static\":{\n"
            + "                  \"deviceType\":\"401\",\n" + "                  \"countryCode\":\"BR\"\n"
            + "               },\n" + "               \"airState.quality.overall\":0.0,\n"
            + "               \"airState.tempState.current\":25.0,\n"
            + "               \"airState.miscFuncState.extraOp\":0.0,\n"
            + "               \"airState.energy.accumulated\":0.0,\n"
            + "               \"airState.reservation.sleepTime\":0.0,\n"
            + "               \"airState.miscFuncState.autoDry\":0.0,\n"
            + "               \"airState.reservation.targetTimeToStart\":0.0,\n" + "               \"meta\":{\n"
            + "                  \"allDeviceInfoUpdate\":false,\n"
            + "                  \"messageId\":\"fVz2AE-2SC-rf3GnerGdeQ\"\n" + "               },\n"
            + "               \"airState.quality.PM1\":0.0,\n" + "               \"airState.wMode.smartCare\":0.0,\n"
            + "               \"airState.quality.PM2\":0.0,\n" + "               \"online\":true,\n"
            + "               \"airState.opMode\":0.0,\n"
            + "               \"airState.reservation.targetTimeToStop\":0.0,\n"
            + "               \"airState.filterMngStates.maxTime\":0.0,\n"
            + "               \"airState.filterMngStates.useTime\":0.0\n" + "            },\n"
            + "            \"online\":true,\n" + "            \"platformType\":\"thinq2\",\n"
            + "            \"area\":45883,\n" + "            \"regDt\":2.0220111184827E13,\n"
            + "            \"blackboxYn\":\"Y\",\n" + "            \"modelProtocol\":\"STANDARD\",\n"
            + "            \"order\":0,\n" + "            \"drServiceYn\":\"N\",\n" + "            \"fwInfoList\":[\n"
            + "               {\n" + "                  \"checksum\":\"00004105\",\n"
            + "                  \"order\":1.0,\n" + "                  \"partNumber\":\"SAA40128563\"\n"
            + "               }\n" + "            ],\n" + "            \"modemInfo\":{\n"
            + "               \"appVersion\":\"clip_hna_v1.9.116\",\n"
            + "               \"modelName\":\"RAC_056905_WW\",\n" + "               \"modemType\":\"QCOM_QCA4010\",\n"
            + "               \"ruleEngine\":\"y\"\n" + "            },\n" + "            \"guideTypeYn\":\"Y\",\n"
            + "            \"guideType\":\"RAC_TYPE1\",\n" + "            \"regDtUtc\":\"20220111204827\",\n"
            + "            \"regIndex\":0,\n" + "            \"groupableYn\":\"Y\",\n"
            + "            \"controllableYn\":\"Y\",\n" + "            \"combinedProductYn\":\"N\",\n"
            + "            \"masterYn\":\"Y\",\n" + "            \"pccModelYn\":\"N\",\n" + "            \"sdsPid\":{\n"
            + "               \"sds4\":\"\",\n" + "               \"sds3\":\"\",\n" + "               \"sds2\":\"\",\n"
            + "               \"sds1\":\"\"\n" + "            },\n" + "            \"autoOrderYn\":\"N\",\n"
            + "            \"initDevice\":false,\n" + "            \"existsEntryPopup\":\"N\",\n"
            + "            \"tclcount\":0\n" + "         }\n" + "      ],\n" + "      \"group\":[\n" + "         \n"
            + "      ]\n" + "   }\n" + "}";
    private String secretKey = "gregre9812012910291029120912091209";
    private String oauthTokenSearchKeyReturned = "{\"returnData\":\"" + secretKey + "\"}";
    private String refreshToken = "12897238974bb327862378ef290128390273aa7389723894734de";
    private String accessToken = "11a1222c39f16a5c8b3fa45bb4c9be2e00a29a69dced2fa7fe731f1728346ee669f1a96d1f0b4925e5aa330b6dbab882772";
    private String sessionTokenReturned = "{\"status\":1,\"access_token\":\"" + accessToken
            + "\",\"expires_in\":\"3600\",\"refresh_token\":\"" + refreshToken
            + "\",\"oauth2_backend_url\":\"http://localhost:8880/\"}";

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    @Test
    public void testDiscoveryThings() {
        stubFor(get(GATEWAY_SERVICE_PATH).willReturn(ok(gtwResponse)));
        String preLoginPwd = RestUtils.getPreLoginEncPwd(fakePassword);
        stubFor(post("/spx" + PRE_LOGIN_PATH).withRequestBody(containing("user_auth2=" + preLoginPwd))
                .willReturn(ok(preLoginResponse)));
        URI uri = UriBuilder.fromUri("http://localhost:8880").path("spx" + OAUTH_SEARCH_KEY_PATH)
                .queryParam("key_name", "OAUTH_SECRETKEY").queryParam("sever_type", "OP").build();
        stubFor(get(String.format("%s?%s", uri.getPath(), uri.getQuery())).willReturn(ok(oauthTokenSearchKeyReturned)));
        stubFor(post(V2_SESSION_LOGIN_PATH + fakeUserName).withRequestBody(containing("user_auth2=SOME_DUMMY_ENC_PWD"))
                .withHeader("X-Signature", equalTo("SOME_DUMMY_SIGNATURE"))
                .withHeader("X-Timestamp", equalTo("1643236928")).willReturn(ok(loginSessionResponse)));
        stubFor(get(V2_USER_INFO).willReturn(ok(userInfoReturned)));
        stubFor(get("/v1" + V2_LS_PATH).willReturn(ok(dashboardListReturned)));
        String currTimestamp = getCurrentTimestamp();
        Map<String, String> empData = new LinkedHashMap<>();
        empData.put("account_type", userIdType);
        empData.put("country_code", fakeCountry);
        empData.put("username", fakeUserName);

        stubFor(post("/emp/oauth2/token/empsession").withRequestBody(containing("account_type=" + userIdType))
                .withRequestBody(containing("country_code=" + fakeCountry))
                .withRequestBody(containing("username=" + URLEncoder.encode(fakeUserName, StandardCharsets.UTF_8)))
                .withHeader("lgemp-x-session-key", equalTo(loginSessionId)).willReturn(ok(sessionTokenReturned)));
        // faking some constants
        LGThinqBindingConstants.GATEWAY_URL = "http://localhost:8880" + GATEWAY_SERVICE_PATH;
        LGThinqBindingConstants.V2_EMP_SESS_URL = "http://localhost:8880/emp/oauth2/token/empsession";
        Bridge fakeThing = mock(Bridge.class);
        ThingUID fakeThingUid = mock(ThingUID.class);
        when(fakeThingUid.getId()).thenReturn(fakeBridgeName);
        when(fakeThing.getUID()).thenReturn(fakeThingUid);
        String tempDir = System.getProperty("java.io.tmpdir");
        LGThinqBindingConstants.THINQ_CONNECTION_DATA_FILE = tempDir + File.separator + "token.json";
        LGThinqBindingConstants.BASE_CAP_CONFIG_DATA_FILE = tempDir + File.separator + "thinq-cap.json";
        LGBridgeHandler b = new LGBridgeHandler(fakeThing);
        LGBridgeHandler spyBridge = spy(b);
        doReturn(new LGThinqConfiguration(fakeUserName, fakePassword, fakeCountry, fakeLanguage, 60)).when(spyBridge)
                .getConfigAs(any(Class.class));
        spyBridge.initialize();
        LGApiClientService service1 = LGApiV1ClientServiceImpl.getInstance();
        LGApiClientService service2 = LGApiV2ClientServiceImpl.getInstance();
        TokenManager tokenManager = TokenManager.getInstance();
        try {
            if (!tokenManager.isOauthTokenRegistered(fakeBridgeName)) {
                tokenManager.oauthFirstRegistration(fakeBridgeName, fakeLanguage, fakeCountry, fakeUserName,
                        fakePassword);
            }
            List<LGDevice> devices = service2.listAccountDevices("bridgeTest");
            assertEquals(devices.size(), 2);
        } catch (Exception e) {
            logger.error("Error testing facade", e);
        }
    }
}
