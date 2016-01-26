package org.openhab.binding.mysensors.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openhab.binding.mysensors.handler.MySensorsUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MySensorsBridgeConnection extends Thread {

    private Logger logger = LoggerFactory.getLogger(MySensorsBridgeConnection.class);

    public List<MySensorsUpdateListener> updateListeners;

    public List<MySensorsMessage> MySensorsMessageOutboundQueue = Collections
            .synchronizedList(new LinkedList<MySensorsMessage>());

    public boolean connected = false;

    protected boolean stopReader = false;

    public MySensorsBridgeConnection() {
        updateListeners = new ArrayList<>();
    }

    /**
     * @param listener An Object, that wants to listen on status updates
     */
    public void addUpdateListener(MySensorsUpdateListener listener) {
        synchronized (updateListeners) {
            updateListeners.add(listener);
        }
    }

    public void removeUpdateListener(MySensorsUpdateListener listener) {
        synchronized (updateListeners) {
            if (updateListeners.contains(listener)) {
                updateListeners.remove(listener);
            }
        }
    }

    public void addMySensorsOutboundMessage(MySensorsMessage msg) {
        MySensorsMessageOutboundQueue.add(msg);
    }

    public void removeMySensorsOutboundMessage(MySensorsMessage msg) {
        Iterator<MySensorsMessage> iterator = MySensorsMessageOutboundQueue.iterator();
        while (iterator.hasNext()) {
            MySensorsMessage msgInQueue = iterator.next();
            if (msgInQueue.getNodeId() == msg.getNodeId() && msgInQueue.getChildId() == msg.getChildId()
                    && msgInQueue.getMsgType() == msg.getMsgType() && msgInQueue.getSubType() == msg.getSubType()
                    && msgInQueue.getAck() == msg.getAck() && msgInQueue.getMsg().equals(msg.getMsg())) {
                iterator.remove();
            }
        }
    }

    /**
     * Stop the communication with the serial/ip interface (stop Thread)
     */
    public void stopReader() {
        this.stopReader = true;
    }
}
