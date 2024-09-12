/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.smartmeter.internal.sml;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openmuc.jsml.structures.EMessageBody;
import org.openmuc.jsml.structures.SmlFile;
import org.openmuc.jsml.structures.SmlMessage;
import org.openmuc.jsml.structures.responses.SmlAttentionRes;
import org.openmuc.jsml.structures.responses.SmlGetListRes;
import org.openmuc.jsml.structures.responses.SmlGetProcParameterRes;
import org.openmuc.jsml.structures.responses.SmlGetProfileListRes;
import org.openmuc.jsml.structures.responses.SmlGetProfilePackRes;
import org.openmuc.jsml.structures.responses.SmlPublicCloseRes;
import org.openmuc.jsml.structures.responses.SmlPublicOpenRes;

/**
 * Class to parse a SML_FILE
 *
 * @author Matthias Steigenberger - Initial contribution
 */
@NonNullByDefault
public class SmlFileDebugOutput {

    private SmlFileDebugOutput() {
        // private constructor to hide the implicit public one, since static methods should be accessed in static way so
        // there is no need of public constructor
    }

    /**
     * Prints the whole SML_File
     *
     * @param smlFile
     *            the SML file
     */
    public static void printFile(SmlFile smlFile, Consumer<String> consumer) {
        List<SmlMessage> smlMessages = smlFile.getMessages();

        for (SmlMessage smlMessage : smlMessages) {
            EMessageBody messageBody = smlMessage.getMessageBody().getTag();

            switch (messageBody) {
                case OPEN_REQUEST:
                    parseOpenRequest(smlMessage, consumer);
                    break;
                case OPEN_RESPONSE:
                    parseOpenResponse(smlMessage, consumer);
                    break;
                case CLOSE_REQUEST:
                    parseCloseRequest(smlMessage, consumer);
                    break;
                case CLOSE_RESPONSE:
                    parseCloseResponse(smlMessage, consumer);
                    break;
                case GET_PROFILE_PACK_REQUEST:
                    parseGetProfilePackRequest(smlMessage, consumer);
                    break;
                case GET_PROFILE_PACK_RESPONSE:
                    parseGetProfilePackResponse(smlMessage, consumer);
                    break;
                case GET_PROFILE_LIST_REQUEST:
                    parseGetProfileListRequest(smlMessage, consumer);
                    break;
                case GET_PROFILE_LIST_RESPONSE:
                    parseGetProfileListResponse(smlMessage, consumer);
                    break;
                case GET_PROC_PARAMETER_REQUEST:
                    parseGetProcParameterRequest(smlMessage, consumer);
                    break;
                case GET_PROC_PARAMETER_RESPONSE:
                    parseGetProcParameterResponse(smlMessage, consumer);
                    break;
                case SET_PROC_PARAMETER_REQUEST:
                    parseSetProcParameterRequest(smlMessage, consumer);
                    break;
                case GET_LIST_REQUEST:
                    parseGetListRequest(smlMessage, consumer);
                    break;
                case GET_LIST_RESPONSE:
                    parseGetListResponse(smlMessage, consumer);
                    break;
                case ATTENTION_RESPONSE:
                    parseAttentionResponse(smlMessage, consumer);
                    break;
                default:
                    consumer.accept("type not found");
            }
        }
    }

    // ========================= Responses =================================

    private static void parseGetListResponse(SmlMessage smlMessage, Consumer<String> consumer) {
        consumer.accept("Got GetListResponse");
        SmlGetListRes smlListRes = (SmlGetListRes) smlMessage.getMessageBody().getChoice();

        // consumer.accept(sml_listRes.toString());

        consumer.accept(smlListRes.toStringIndent(" "));
    }

    private static void parseAttentionResponse(SmlMessage smlMessage, Consumer<String> consumer) {
        consumer.accept("Got AttentionResponse");
        SmlAttentionRes smlAttentionRes = (SmlAttentionRes) smlMessage.getMessageBody().getChoice();
        consumer.accept(smlAttentionRes.toString());
    }

    private static void parseGetProcParameterResponse(SmlMessage smlMessage, Consumer<String> consumer) {
        consumer.accept("Got GetProcParameterResponse");
        SmlGetProcParameterRes smlGetProcParameterRes = (SmlGetProcParameterRes) smlMessage.getMessageBody()
                .getChoice();
        consumer.accept(smlGetProcParameterRes.toString());
    }

    private static void parseGetProfileListResponse(SmlMessage smlMessage, Consumer<String> consumer) {
        consumer.accept("Got GetProfileListResponse");
        SmlGetProfileListRes smlGetProfileListRes = (SmlGetProfileListRes) smlMessage.getMessageBody().getChoice();
        consumer.accept(smlGetProfileListRes.toString());
    }

    private static void parseOpenResponse(SmlMessage smlMessage, Consumer<String> consumer) {
        consumer.accept("Got OpenResponse");
        SmlPublicOpenRes smlPublicOpenRes = (SmlPublicOpenRes) smlMessage.getMessageBody().getChoice();
        consumer.accept(smlPublicOpenRes.toString());
    }

    private static void parseCloseResponse(SmlMessage smlMessage, Consumer<String> consumer) {
        consumer.accept("Got CloseResponse");
        SmlPublicCloseRes smlPublicCloseRes = (SmlPublicCloseRes) smlMessage.getMessageBody().getChoice();
        consumer.accept(smlPublicCloseRes.toString());
    }

    private static void parseGetProfilePackResponse(SmlMessage smlMessage, Consumer<String> consumer) {
        consumer.accept("Got GetProfilePackResponse");
        SmlGetProfilePackRes smlGetProfilePackRes = (SmlGetProfilePackRes) smlMessage.getMessageBody().getChoice();
        consumer.accept(smlGetProfilePackRes.toString());
    }

    // ========================= Requests =================================

    private static void parseCloseRequest(SmlMessage smlMessage, Consumer<String> consumer) {
        consumer.accept("Got CloseRequest");
    }

    private static void parseGetProfileListRequest(SmlMessage smlMessage, Consumer<String> consumer) {
        consumer.accept("Got GetProfileListRequest");
    }

    private static void parseGetProfilePackRequest(SmlMessage smlMessage, Consumer<String> consumer) {
        consumer.accept("Got GetProfilePackRequest");
    }

    private static void parseOpenRequest(SmlMessage smlMessage, Consumer<String> consumer) {
        consumer.accept("Got OpenRequest");
    }

    private static void parseGetProcParameterRequest(SmlMessage smlMessage, Consumer<String> consumer) {
        consumer.accept("Got GetProcParameterRequest");
    }

    private static void parseSetProcParameterRequest(SmlMessage smlMessage, Consumer<String> consumer) {
        consumer.accept("Got SetProcParameterRequest");
    }

    private static void parseGetListRequest(SmlMessage smlMessage, Consumer<String> consumer) {
        consumer.accept("Got GetListRequest");
    }
}
