package org.openhab.binding.silvercrestwifisocket.handler;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.silvercrestwifisocket.SilvercrestWifiSocketBindingConstants;
import org.openhab.binding.silvercrestwifisocket.discovery.SilvercrestWifiSocketDiscoveryService;
import org.openhab.binding.silvercrestwifisocket.entities.SilvercrestWifiSocketResponse;
import org.openhab.binding.silvercrestwifisocket.runnable.SilvercrestWifiSocketUpdateReceiverRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SilvercrestWifiSocketMediator} is responsible for receiving all the UDP packets and route correctly to
 * each handler.
 *
 * @author Jaime Vaz - Initial contribution
 */
public class SilvercrestWifiSocketMediator {

    private static Logger LOG = LoggerFactory.getLogger(SilvercrestWifiSocketMediator.class);

    private final Map<Thing, SilvercrestWifiSocketHandler> handlersRegistredByThing = new HashMap<>();

    private SilvercrestWifiSocketUpdateReceiverRunnable receiver;
    private Thread receiverThread;

    private final SilvercrestWifiSocketDiscoveryService silvercrestDiscoveryService;

    /**
     * Constructor of the mediator. The discovery service must be passed for notify when one new wifi socket has been
     * found.
     *
     * @param silvercrestDiscoveryService the {@link SilvercrestWifiSocketDiscoveryService}
     */
    public SilvercrestWifiSocketMediator(final SilvercrestWifiSocketDiscoveryService silvercrestDiscoveryService) {
        this.silvercrestDiscoveryService = silvercrestDiscoveryService;
        this.initMediatorWifiSocketUpdateReceiverRunnable();
    }

    /**
     * This method is called by the {@link SilvercrestWifiSocketUpdateReceiverRunnable}, when one new message has been
     * received.
     *
     * @param receivedMessage the {@link SilvercrestWifiSocketResponse} message.
     */
    public void processReceivedPacket(final SilvercrestWifiSocketResponse receivedMessage) {
        LOG.debug("Received packet from: {} with content: [{}]", receivedMessage.getHostAddress(),
                receivedMessage.getType());

        SilvercrestWifiSocketHandler handler = this.getHandlerRegistredByMac(receivedMessage.getMacAddress());

        if (handler != null) {
            // deliver message to handler.
            handler.newReceivedResponseMessage(receivedMessage);
            LOG.debug("Received message delivered with success to handler of mac {}", receivedMessage.getMacAddress());
        } else {
            LOG.debug("There is no handler registered for mac address:{}", receivedMessage.getMacAddress());
            // notify discovery service of thing found!
            this.silvercrestDiscoveryService.discoveredWifiSocket(receivedMessage.getMacAddress(),
                    receivedMessage.getHostAddress());
        }
    }

    /**
     * Regists one new {@link Thing} and the corresponding {@link SilvercrestWifiSocketHandler}.
     *
     * @param thing the {@link Thing}.
     * @param handler the {@link SilvercrestWifiSocketHandler}.
     */
    public void registerThingAndWifiSocketHandler(final Thing thing, final SilvercrestWifiSocketHandler handler) {
        this.handlersRegistredByThing.put(thing, handler);
    }

    /**
     * Unregists one {@link SilvercrestWifiSocketHandler} by the corresponding {@link Thing}.
     *
     * @param thing the {@link Thing}.
     */
    public void unregisterWifiSocketHandlerByThing(final Thing thing) {
        SilvercrestWifiSocketHandler handler = this.handlersRegistredByThing.get(thing);
        if (handler != null) {
            this.handlersRegistredByThing.remove(thing);
        }

    }

    /**
     * Utilitary method to get the registered thing handler in mediator by the mac address.
     *
     * @param macAddress the mac address of the thing of the handler.
     * @return {@link SilvercrestWifiSocketHandler} if found.
     */
    private SilvercrestWifiSocketHandler getHandlerRegistredByMac(final String macAddress) {
        SilvercrestWifiSocketHandler searchedHandler = null;
        for (SilvercrestWifiSocketHandler handler : this.handlersRegistredByThing.values()) {
            if (macAddress.equals(handler.getMacAddress())) {
                searchedHandler = handler;
                // don't spend more computation. Found the handler.
                break;
            }
        }
        return searchedHandler;
    }

    /**
     * Inits the mediator WifiSocketUpdateReceiverRunnable thread. This thread is responsible to receive all
     * packets from Wifi Socket devices, and redirect the messages to mediator.
     */
    private void initMediatorWifiSocketUpdateReceiverRunnable() {
        // try with handler port if is null
        if ((this.receiver == null) || ((this.receiverThread != null)
                && (this.receiverThread.isInterrupted() || !this.receiverThread.isAlive()))) {
            try {
                this.receiver = new SilvercrestWifiSocketUpdateReceiverRunnable(this,
                        SilvercrestWifiSocketBindingConstants.WIFI_SOCKET_DEFAULT_UDP_PORT);
                this.receiverThread = new Thread(this.receiver);
                this.receiverThread.start();
                LOG.debug("Invoked the start of receiver thread.");
            } catch (SocketException e) {
                LOG.debug("Cannot start the socket with default port...");
            }
        }
    }

    /**
     * Returns all the {@link Thing} registered.
     *
     * @returns all the {@link Thing}.
     */
    public Set<Thing> getAllThingsRegistred() {
        return this.handlersRegistredByThing.keySet();
    }
}
