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

class ObjectPropertyRequest implements Iterable<ObjectProperties> {

    private static final Logger logger = LoggerFactory.getLogger(ObjectPropertyRequest.class);

    public static Builder builder(OmnilinkBridgeHandler bridgeHandler, int objectType) {
        return new Builder(bridgeHandler, objectType);
    }

    private final OmnilinkBridgeHandler bridgeHandler;
    private final int objectType;
    private final int filter1;
    private final int filter2;
    private final int filter3;

    private ObjectPropertyRequest(OmnilinkBridgeHandler bridgeHandler, int objectType, int filter1, int filter2,
            int filter3) {
        this.bridgeHandler = bridgeHandler;
        this.objectType = objectType;
        this.filter1 = filter1;
        this.filter2 = filter2;
        this.filter3 = filter3;
    }

    @Override
    public Iterator<ObjectProperties> iterator() {
        return new AbstractIterator<ObjectProperties>() {
            private final static int RELATIVE_OFFSET_DIRECTION = 1;

            int currentObjectNumber = 0;

            @Override
            protected ObjectProperties computeNext() {
                try {
                    Message message = bridgeHandler.reqObjectProperties(objectType, currentObjectNumber,
                            RELATIVE_OFFSET_DIRECTION, filter1, filter2, filter3);
                    if (message.getMessageType() == Message.MESG_TYPE_OBJ_PROP) {
                        ObjectProperties objectProperties = (ObjectProperties) message;
                        currentObjectNumber = objectProperties.getNumber();
                        return objectProperties;
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

    public static class Builder {
        private final OmnilinkBridgeHandler bridgeHandler;
        private final int objectType;
        private int filter1 = ObjectProperties.FILTER_1_NONE;
        private int filter2 = ObjectProperties.FILTER_2_NONE;
        private int filter3 = ObjectProperties.FILTER_3_NONE;

        private Builder(OmnilinkBridgeHandler bridgeHandler, int objectType) {
            this.bridgeHandler = bridgeHandler;
            this.objectType = objectType;
        }

        public Builder selectNamed() {
            this.filter1 = ObjectProperties.FILTER_1_NAMED;
            return this;
        }

        public Builder areaFilter(int area) {
            this.filter2 = area;
            return this;
        }

        public ObjectPropertyRequest build() {
            return new ObjectPropertyRequest(bridgeHandler, objectType, filter1, filter2, filter3);
        }

        public Builder selectAnyLoad() {
            this.filter3 = ObjectProperties.FILTER_3_ANY_LOAD;
            return this;
        }

    }
}
