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

    private final OmnilinkBridgeHandler bridgeHandler;
    private final int objectType;
    private final int nameFilter;
    private final int areaFilter;
    private final int roomFilter;

    ObjectPropertyRequest(OmnilinkBridgeHandler bridgeHandler, int objectType, int nameFilter, int areaFilter,
            int roomFilter) {
        this.bridgeHandler = bridgeHandler;
        this.objectType = objectType;
        this.nameFilter = nameFilter;
        this.areaFilter = areaFilter;
        this.roomFilter = roomFilter;
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
                            RELATIVE_OFFSET_DIRECTION, nameFilter, areaFilter, roomFilter);
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
}
