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
package org.openhab.binding.enocean.internal.eep;

import static org.openhab.binding.enocean.internal.messages.ESP3Packet.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.eep.Base.UTEResponse;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.eep.Base._4BSTeachInVariation3Response;
import org.openhab.binding.enocean.internal.eep.Base._RPSMessage;
import org.openhab.binding.enocean.internal.eep.D5_00.D5_00_01;
import org.openhab.binding.enocean.internal.eep.F6_01.F6_01_01;
import org.openhab.binding.enocean.internal.eep.F6_02.F6_02_01;
import org.openhab.binding.enocean.internal.eep.F6_05.F6_05_02;
import org.openhab.binding.enocean.internal.eep.F6_10.F6_10_00;
import org.openhab.binding.enocean.internal.eep.F6_10.F6_10_00_EltakoFPE;
import org.openhab.binding.enocean.internal.eep.F6_10.F6_10_01;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;
import org.openhab.binding.enocean.internal.messages.EventMessage;
import org.openhab.binding.enocean.internal.messages.EventMessage.EventMessageType;
import org.openhab.binding.enocean.internal.messages.responses.SMACKTeachInResponse;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class EEPFactory {

    private static final Logger logger = LoggerFactory.getLogger(EEPFactory.class);

    public static EEP createEEP(EEPType eepType) {
        try {
            Class<? extends EEP> cl = eepType.getEEPClass();
            if (cl == null) {
                throw new IllegalArgumentException("Message " + eepType + " not implemented");
            }
            return cl.getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static EEP buildEEP(EEPType eepType, ERP1Message packet) {
        try {
            Class<? extends EEP> cl = eepType.getEEPClass();
            if (cl == null) {
                throw new IllegalArgumentException("Message " + eepType + " not implemented");
            }
            return cl.getConstructor(ERP1Message.class).newInstance(packet);
        } catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            logger.error("Cannot instantiate EEP {}-{}-{}: {}",
                    HexUtils.bytesToHex(new byte[] { eepType.getRORG().getValue() }),
                    HexUtils.bytesToHex(new byte[] { (byte) eepType.getFunc() }),
                    HexUtils.bytesToHex(new byte[] { (byte) eepType.getType() }), e.getMessage());

            throw new IllegalArgumentException(e);
        }
    }

    private static @Nullable EEPType getGenericEEPTypeFor(byte rorg) {
        logger.info("Received unsupported EEP teach in, trying to fallback to generic thing");
        RORG r = RORG.getRORG(rorg);
        if (r == RORG._4BS) {
            logger.info("Fallback to 4BS generic thing");
            return EEPType.Generic4BS;
        } else if (r == RORG.VLD) {
            logger.info("Fallback to VLD generic thing");
            return EEPType.GenericVLD;
        } else {
            logger.info("Fallback not possible");
            return null;
        }
    }

    public static @Nullable EEP buildEEPFromTeachInERP1(ERP1Message msg) {
        if (!msg.getIsTeachIn() && !(msg.getRORG() == RORG.RPS)) {
            return null;
        }

        switch (msg.getRORG()) {
            case RPS:
                try {
                    _RPSMessage result = new F6_10_00(msg);
                    if (result.isValidForTeachIn()) {
                        return result;
                    }
                } catch (Exception e) {
                }

                try {
                    _RPSMessage result = new F6_10_01(msg);
                    if (result.isValidForTeachIn()) {
                        return result;
                    }
                } catch (Exception e) {
                }

                try {
                    _RPSMessage result = new F6_02_01(msg);
                    if (result.isValidForTeachIn()) {
                        return result;
                    }
                } catch (Exception e) {
                }

                try {
                    _RPSMessage result = new F6_05_02(msg);
                    if (result.isValidForTeachIn()) {
                        return result;
                    }
                } catch (Exception e) {
                }

                try {
                    _RPSMessage result = new F6_01_01(msg);
                    if (result.isValidForTeachIn()) {
                        return result;
                    }
                } catch (Exception e) {
                }

                try {
                    _RPSMessage result = new F6_10_00_EltakoFPE(msg);
                    if (result.isValidForTeachIn()) {
                        return result;
                    }
                } catch (Exception e) {
                }

                return null;
            case _1BS:
                return new D5_00_01(msg);
            case _4BS: {
                int db0 = msg.getPayload()[4];
                if ((db0 & _4BSMessage.LRN_TYPE_MASK) == 0) { // Variation 1
                    logger.info("Received 4BS Teach In variation 1 without EEP, fallback to generic thing");
                    return buildEEP(EEPType.Generic4BS, msg);
                }

                byte db3 = msg.getPayload()[1];
                byte db2 = msg.getPayload()[2];
                byte db1 = msg.getPayload()[3];

                int func = (db3 & 0xFF) >>> 2;
                int type = ((db3 & 0b11) << 5) + ((db2 & 0xFF) >>> 3);
                int manufId = ((db2 & 0b111) << 8) + (db1 & 0xff);

                logger.debug("Received 4BS Teach In with EEP A5-{}-{} and manufacturerID {}",
                        HexUtils.bytesToHex(new byte[] { (byte) func }),
                        HexUtils.bytesToHex(new byte[] { (byte) type }),
                        HexUtils.bytesToHex(new byte[] { (byte) manufId }));

                EEPType eepType = EEPType.getType(RORG._4BS, func, type, manufId);
                if (eepType == null) {
                    eepType = getGenericEEPTypeFor(RORG._4BS.getValue());
                }

                if (eepType != null) {
                    return buildEEP(eepType, msg);
                }
            }
                break;
            case UTE: {
                byte[] payload = msg.getPayload();

                byte rorg = payload[payload.length - 1 - ESP3_STATUS_LENGTH - ESP3_SENDERID_LENGTH];
                byte func = payload[payload.length - 1 - ESP3_STATUS_LENGTH - ESP3_SENDERID_LENGTH - ESP3_RORG_LENGTH];
                byte type = payload[payload.length - 1 - ESP3_STATUS_LENGTH - ESP3_SENDERID_LENGTH - ESP3_RORG_LENGTH
                        - 1];

                byte manufIdMSB = payload[payload.length - 1 - ESP3_STATUS_LENGTH - ESP3_SENDERID_LENGTH
                        - ESP3_RORG_LENGTH - 2];
                byte manufIdLSB = payload[payload.length - 1 - ESP3_STATUS_LENGTH - ESP3_SENDERID_LENGTH
                        - ESP3_RORG_LENGTH - 3];
                int manufId = ((manufIdMSB & 0b111) << 8) + (manufIdLSB & 0xff);

                EEPType eepType = EEPType.getType(RORG.getRORG(rorg), func, type, manufId);
                if (eepType == null) {
                    eepType = getGenericEEPTypeFor(rorg);
                }

                if (eepType != null) {
                    return buildEEP(eepType, msg);
                }
            }
                break;
            default:
                return null;
        }

        return null;
    }

    public static @Nullable EEP buildEEPFromTeachInSMACKEvent(EventMessage event) {
        if (event.getEventMessageType() != EventMessageType.SA_CONFIRM_LEARN) {
            return null;
        }

        byte[] payload = event.getPayload();
        byte manufIdMSB = payload[2];
        byte manufIdLSB = payload[3];
        int manufId = ((manufIdMSB & 0b111) << 8) + (manufIdLSB & 0xff);

        byte rorg = payload[4];
        int func = payload[5] & 0xFF;
        int type = payload[6] & 0xFF;

        byte[] senderId = Arrays.copyOfRange(payload, 12, 12 + 4);

        logger.debug("Received SMACK Teach In with EEP {}-{}-{} and manufacturerID {}",
                HexUtils.bytesToHex(new byte[] { (byte) rorg }), HexUtils.bytesToHex(new byte[] { (byte) func }),
                HexUtils.bytesToHex(new byte[] { (byte) type }), HexUtils.bytesToHex(new byte[] { (byte) manufId }));

        EEPType eepType = EEPType.getType(RORG.getRORG(rorg), func, type, manufId);
        if (eepType == null) {
            eepType = getGenericEEPTypeFor(rorg);
        }

        return (eepType == null) ? null : createEEP(eepType).setSenderId(senderId);
    }

    public static @Nullable EEP buildResponseEEPFromTeachInERP1(ERP1Message msg, byte[] senderId, boolean teachIn) {
        switch (msg.getRORG()) {
            case UTE:
                EEP result = new UTEResponse(msg, teachIn);
                result.setSenderId(senderId);

                return result;
            case _4BS:
                result = new _4BSTeachInVariation3Response(msg, teachIn);
                result.setSenderId(senderId);

                return result;
            default:
                return null;
        }
    }

    public static SMACKTeachInResponse buildResponseFromSMACKTeachIn(EventMessage event, boolean sendTeachOuts) {
        SMACKTeachInResponse response = new SMACKTeachInResponse();

        byte priority = event.getPayload()[1];
        if ((priority & 0b1001) == 0b1001) {
            logger.debug("gtw is already postmaster");
            if (sendTeachOuts) {
                logger.debug("Repeated learn is not allow hence send teach out");
                response.setTeachOutResponse();
            } else {
                logger.debug("Send a repeated learn in");
                response.setRepeatedTeachInResponse();
            }
        } else if ((priority & 0b100) == 0) {
            logger.debug("no place for further mailbox");
            response.setNoPlaceForFurtherMailbox();
        } else if ((priority & 0b10) == 0) {
            logger.debug("rssi is not good enough");
            response.setBadRSSI();
        } else if ((priority & 0b1) == 0b1) {
            logger.debug("gtw is candidate for postmaster => teach in");
            response.setTeachIn();
        }

        return response;
    }
}
