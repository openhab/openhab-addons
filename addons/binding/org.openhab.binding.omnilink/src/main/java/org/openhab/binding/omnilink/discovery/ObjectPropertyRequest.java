package org.openhab.binding.omnilink.discovery;

import java.util.Iterator;

import org.openhab.binding.omnilink.handler.BridgeOfflineException;
import org.openhab.binding.omnilink.handler.OmnilinkBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectProperties;
import com.google.common.collect.AbstractIterator;

/**
 *
 * @author Craig Hamilton
 *
 * @param <T>
 */
class ObjectPropertyRequest<T extends ObjectProperties> implements Iterable<T> {

    private static final Logger logger = LoggerFactory.getLogger(ObjectPropertyRequest.class);

    @SuppressWarnings("unchecked")
    public static <T extends ObjectProperties, U extends ObjectPropertyRequests<T>> Builder<T> builder(
            OmnilinkBridgeHandler bridgeHandler, U request) {
        return new Builder(bridgeHandler, request);
    }

    private final OmnilinkBridgeHandler bridgeHandler;
    private final ObjectPropertyRequests<T> request;
    private final int filter1;
    private final int filter2;
    private final int filter3;

    private ObjectPropertyRequest(OmnilinkBridgeHandler bridgeHandler, ObjectPropertyRequests<T> request, int filter1,
            int filter2, int filter3) {
        this.bridgeHandler = bridgeHandler;
        this.request = request;
        this.filter1 = filter1;
        this.filter2 = filter2;
        this.filter3 = filter3;
    }

    @Override
    public Iterator<T> iterator() {
        return new AbstractIterator<T>() {
            private final static int RELATIVE_OFFSET_DIRECTION = 1;

            int currentObjectNumber = 0;

            @Override
            protected T computeNext() {
                try {
                    Message message = bridgeHandler.reqObjectProperties(request.getPropertyRequest(),
                            currentObjectNumber, RELATIVE_OFFSET_DIRECTION, filter1, filter2, filter3);
                    if (message.getMessageType() == Message.MESG_TYPE_OBJ_PROP) {
                        ObjectProperties objectProperties = (ObjectProperties) message;
                        currentObjectNumber = objectProperties.getNumber();
                        return request.getResponseType().cast(objectProperties);
                    } else {
                        return endOfData();
                    }
                } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
                    logger.error("Error retrieving object properties", e);
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static class Builder<T extends ObjectProperties> {
        private final OmnilinkBridgeHandler bridgeHandler;
        private final ObjectPropertyRequests<T> request;
        private int filter1 = ObjectProperties.FILTER_1_NONE;
        private int filter2 = ObjectProperties.FILTER_2_NONE;
        private int filter3 = ObjectProperties.FILTER_3_NONE;

        private Builder(OmnilinkBridgeHandler bridgeHandler, ObjectPropertyRequests<T> request) {
            this.bridgeHandler = bridgeHandler;
            this.request = request;
        }

        public Builder<T> selectNamed() {
            this.filter1 = ObjectProperties.FILTER_1_NAMED;
            return this;
        }

        public Builder<T> areaFilter(int area) {
            this.filter2 = area;
            return this;
        }

        public ObjectPropertyRequest<T> build() {
            return new ObjectPropertyRequest<T>(bridgeHandler, request, filter1, filter2, filter3);
        }

        public Builder<T> selectAnyLoad() {
            this.filter3 = ObjectProperties.FILTER_3_ANY_LOAD;
            return this;
        }

    }
}
