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
package org.openhab.binding.omnilink.internal.discovery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.omnilink.internal.exceptions.BridgeOfflineException;
import org.openhab.binding.omnilink.internal.handler.OmnilinkBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectProperties;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;

/**
 * @author Craig Hamilton - Initial contribution
 *
 * @param <T>
 */
@NonNullByDefault
public class ObjectPropertyRequest<T extends ObjectProperties> implements Iterable<T> {
    private final Logger logger = LoggerFactory.getLogger(ObjectPropertyRequest.class);

    public static <T extends ObjectProperties, U extends ObjectPropertyRequests<T>> Builder<T> builder(
            OmnilinkBridgeHandler bridgeHandler, U request, int objectNumber, int offset) {
        return new Builder<>(bridgeHandler, request, objectNumber, offset);
    }

    private final OmnilinkBridgeHandler bridgeHandler;
    private final ObjectPropertyRequests<T> request;
    private final int objectNumber;
    private final int filter1;
    private final int filter2;
    private final int filter3;
    private final int offset;

    private ObjectPropertyRequest(OmnilinkBridgeHandler bridgeHandler, ObjectPropertyRequests<T> request,
            int objectNumber, int filter1, int filter2, int filter3, int offset) {
        this.bridgeHandler = bridgeHandler;
        this.request = request;
        this.objectNumber = objectNumber;
        this.filter1 = filter1;
        this.filter2 = filter2;
        this.filter3 = filter3;
        this.offset = offset;
    }

    @Override
    public Iterator<T> iterator() {
        List<T> messages = new ArrayList<>();
        int currentObjectNumber = objectNumber;

        while (true) {
            try {
                Message message = bridgeHandler.reqObjectProperties(request.getPropertyRequest(), currentObjectNumber,
                        offset, filter1, filter2, filter3);
                if (message.getMessageType() == Message.MESG_TYPE_OBJ_PROP) {
                    ObjectProperties objectProperties = (ObjectProperties) message;
                    messages.add(request.getResponseType().cast(objectProperties));
                    if (offset == 0) {
                        break;
                    } else if (offset == 1) {
                        currentObjectNumber++;
                    } else if (offset == -1) {
                        currentObjectNumber--;
                    }
                } else {
                    break;
                }
            } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
                logger.warn("Error retrieving object properties: {}", e.getMessage());
            }
        }
        return messages.iterator();
    }

    public static class Builder<T extends ObjectProperties> {
        private final OmnilinkBridgeHandler bridgeHandler;
        private final ObjectPropertyRequests<T> request;
        private final int objectNumber;
        private final int offset;
        private int filter1 = ObjectProperties.FILTER_1_NONE;
        private int filter2 = ObjectProperties.FILTER_2_NONE;
        private int filter3 = ObjectProperties.FILTER_3_NONE;

        private Builder(OmnilinkBridgeHandler bridgeHandler, ObjectPropertyRequests<T> request, int objectNumber,
                int offset) {
            this.bridgeHandler = bridgeHandler;
            this.request = request;
            this.objectNumber = objectNumber;
            this.offset = offset;
        }

        public Builder<T> selectNamed() {
            this.filter1 = ObjectProperties.FILTER_1_NAMED;
            return this;
        }

        public Builder<T> areaFilter(int area) {
            this.filter2 = area;
            return this;
        }

        public Builder<T> selectAnyLoad() {
            this.filter3 = ObjectProperties.FILTER_3_ANY_LOAD;
            return this;
        }

        public ObjectPropertyRequest<T> build() {
            return new ObjectPropertyRequest<>(bridgeHandler, request, objectNumber, filter1, filter2, filter3, offset);
        }
    }
}
