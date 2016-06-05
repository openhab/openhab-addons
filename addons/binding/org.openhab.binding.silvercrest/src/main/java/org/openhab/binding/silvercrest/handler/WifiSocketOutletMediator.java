package org.openhab.binding.silvercrest.handler;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.silvercrest.SilvercrestBindingConstants;
import org.openhab.binding.silvercrest.discovery.SilvercrestDiscoveryService;
import org.openhab.binding.silvercrest.wifisocketoutlet.entities.WifiSocketOutletResponse;
import org.openhab.binding.silvercrest.wifisocketoutlet.runnable.WifiSocketOutletUpdateReceiverRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WifiSocketOutletMediator} is responsible for receiving all the UDP packets and route correctly to
 * each handler.
 *
 * @author Jaime Vaz - Initial contribution
 */
public class WifiSocketOutletMediator {

    private static Logger LOG = LoggerFactory.getLogger(WifiSocketOutletMediator.class);

    private final Map<Thing, WifiSocketOutletHandler> handlersRegistredByThing = new HashMap<>();

    private WifiSocketOutletUpdateReceiverRunnable receiver;
    private Thread receiverThread;

    private final SilvercrestDiscoveryService silvercrestDiscoveryService;

    /**
     * Constructor of the mediator. The discovery service must be passed for notify when one new wifi socket has been
     * found.
     *
     * @param silvercrestDiscoveryService the {@link SilvercrestDiscoveryService}
     */
    public WifiSocketOutletMediator(final SilvercrestDiscoveryService silvercrestDiscoveryService) {
        this.silvercrestDiscoveryService = silvercrestDiscoveryService;
        this.initMediatorWifiSocketOutletUpdateReceiverRunnable();
    }

    /**
     * This method is called by the {@link WifiSocketOutletUpdateReceiverRunnable}, when one new message has been
     * received.
     *
     * @param receivedMessage the {@link WifiSocketOutletResponse} message.
     */
    public void processReceivedPacket(final WifiSocketOutletResponse receivedMessage) {
        LOG.debug("Received packet from: {} with content: [{}]", receivedMessage.getHostAddress(),
                receivedMessage.getType());

        WifiSocketOutletHandler handler = this.getHandlerRegistredByMac(receivedMessage.getMacAddress());

        if (handler != null) {
            // deliver message to handler.
            handler.newReceivedResponseMessage(receivedMessage);
            LOG.debug("Received message delivered with success to handler of mac {}", receivedMessage.getMacAddress());
        } else {
            LOG.debug("There is no handler registered for mac address:{}", receivedMessage.getMacAddress());
            // notify discovery service of thing found!
            this.silvercrestDiscoveryService.discoveredWifiSocketOutlet(receivedMessage.getMacAddress(),
                    receivedMessage.getHostAddress());
        }
    }

    /**
     * Regists one new {@link Thing} and the corresponding {@link WifiSocketOutletHandler}.
     *
     * @param thing the {@link Thing}.
     * @param handler the {@link WifiSocketOutletHandler}.
     */
    public void registerThingAndWifiSocketOutletHandler(final Thing thing, final WifiSocketOutletHandler handler) {
        this.handlersRegistredByThing.put(thing, handler);
    }

    /**
     * Unregists one {@link WifiSocketOutletHandler} by the corresponding {@link Thing}..
     *
     * @param thing the {@link Thing}.
     */
    public void unregisterWifiSocketOutletHandlerByThing(final Thing thing) {
        WifiSocketOutletHandler handler = this.handlersRegistredByThing.get(thing);
        if (handler != null) {
            this.handlersRegistredByThing.remove(thing);
        }

    }

    /**
     * Utilitary method to get the registered thing handler in mediator by the mac address.
     *
     * @param macAddress the mac address of the thing of the handler.
     * @return {@link WifiSocketOutletHandler} if found.
     */
    private WifiSocketOutletHandler getHandlerRegistredByMac(final String macAddress) {
        WifiSocketOutletHandler searchedHandler = null;
        for (WifiSocketOutletHandler handler : this.handlersRegistredByThing.values()) {
            if (macAddress.equals(handler.getMacAddress())) {
                searchedHandler = handler;
                // don't spend more computation. Found the handler.
                break;
            }
        }
        return searchedHandler;
    }

    /**
     * Inits the mediator WifiSocketOutletUpdateReceiverRunnable thread. This thread is responsible to receive all
     * packets from Wifi Socket Outlet devices, and redirect the messages to mediator.
     */
    private void initMediatorWifiSocketOutletUpdateReceiverRunnable() {
        // try with handler port if is null
        if ((this.receiver == null) || ((this.receiverThread != null)
                && (this.receiverThread.isInterrupted() || !this.receiverThread.isAlive()))) {
            try {
                // TODO jmvaz: get from configurations the default port
                this.receiver = new WifiSocketOutletUpdateReceiverRunnable(this,
                        SilvercrestBindingConstants.WIFI_SOCKET_OUTLET_DEFAULT_UDP_PORT);
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
