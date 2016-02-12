package org.openhab.binding.mysensors.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mysensors.handler.MySensorsUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MySensorsBridgeConnection {

    private Logger logger = LoggerFactory.getLogger(MySensorsBridgeConnection.class);

    public List<MySensorsUpdateListener> updateListeners;
    public boolean pauseWriter = false;

    // FIXME must be replaced with a blocking queue
    /*
     * public List<MySensorsMessage> MySensorsMessageOutboundQueue = Collections
     * .synchronizedList(new LinkedList<MySensorsMessage>());
     */

    private BlockingQueue<MySensorsMessage> outboundMessageQueue = null;

    protected boolean connected = false;

    public MySensorsBridgeConnection() {
        outboundMessageQueue = new LinkedBlockingQueue<MySensorsMessage>();
        updateListeners = new ArrayList<>();
    }

    /**
     * startup connection with bridge
     *
     * @return
     */
    public abstract boolean connect();

    /**
     * shutodown method that allows the correct disconnection with the used bridge
     *
     * @return
     */
    public abstract void disconnect();

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

    public MySensorsMessage pollMySensorsOutboundQueue() throws InterruptedException {
        return outboundMessageQueue.poll(1, TimeUnit.DAYS);
    }

    public void addMySensorsOutboundMessage(MySensorsMessage msg) {
        // Is there already are message in the queue for this nodeId & childId? --> Remove it first!
        // We don't want to stack messages

        Iterator<MySensorsMessage> iterator = outboundMessageQueue.iterator();

        while (iterator.hasNext()) {
            MySensorsMessage msgInQueue = iterator.next();
            if (msgInQueue.getNodeId() == msg.getNodeId() && msgInQueue.getChildId() == msg.getChildId()) {
                iterator.remove();
            }
        }

        try {
            outboundMessageQueue.put(msg);
        } catch (InterruptedException e) {
            logger.error("Interrupted message while ruuning");
        }
    }

    public void removeMySensorsOutboundMessage(MySensorsMessage msg) {
        // Pause the writer, so the msg we will try to remove is not blocked by the writer
        pauseWriter = true;

        // Give the writer some time to settle
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Iterator<MySensorsMessage> iterator = outboundMessageQueue.iterator();

        while (iterator.hasNext()) {
            MySensorsMessage msgInQueue = iterator.next();
            if (msgInQueue.getNodeId() == msg.getNodeId() && msgInQueue.getChildId() == msg.getChildId()
                    && msgInQueue.getMsgType() == msg.getMsgType() && msgInQueue.getSubType() == msg.getSubType()
                    && msgInQueue.getAck() == msg.getAck() && msgInQueue.getMsg().equals(msg.getMsg())) {
                iterator.remove();
            }
        }

        pauseWriter = false;
    }
}
