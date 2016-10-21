package org.openhab.binding.amazondashbutton.internal.pcap;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.pcap4j.core.PcapNetworkInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * The {@link PcapNetworkInterfaceService} is a singleton which can be obtained by calling {@link #instance()}.
 * It provides all available {@link PcapNetworkInterface}s which are bound to an address. These network interfaces can
 * be retrieved by calling {@link #getNetworkInterfaces()}.
 *
 * Moreover the {@link PcapNetworkInterfaceService} provided the possibility to register a
 * {@link PcapNetworkInterfaceListener}s which are notified on new and removed {@link PcapNetworkInterface}s.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class PcapNetworkInterfaceService {

    private static final Logger logger = LoggerFactory.getLogger(PcapNetworkInterfaceService.class);

    private static PcapNetworkInterfaceService instance = null;
    private static final String THREADPOOL_NAME = "pcapNetworkInterfaceService";

    private final Set<PcapNetworkInterfaceListener> listeners = new CopyOnWriteArraySet<>();
    private final Set<PcapNetworkInterface> pcapNetworkInterfaces = new CopyOnWriteArraySet<>();

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(THREADPOOL_NAME);

    private ScheduledFuture<?> future = null;

    private final Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            synchronized (pcapNetworkInterfaces) {
                // We cannot use Sets because of https://github.com/kaitoy/pcap4j/issues/73
                final List<PcapNetworkInterface> determinedNetworkInterfaces = Lists
                        .newArrayList(determineBoundNetworkInterfaces());
                final List<PcapNetworkInterface> currentNetworkInterfaces = Lists.newArrayList(pcapNetworkInterfaces);

                final Collection<PcapNetworkInterface> newNetworkInterfaces = Collections2
                        .filter(determinedNetworkInterfaces, Predicates.not(Predicates.in(currentNetworkInterfaces)));
                final Collection<PcapNetworkInterface> removedNetworkInterfaces = Collections2
                        .filter(currentNetworkInterfaces, Predicates.not(Predicates.in(determinedNetworkInterfaces)));

                pcapNetworkInterfaces.clear();
                pcapNetworkInterfaces.addAll(determinedNetworkInterfaces);

                for (PcapNetworkInterface pcapNetworkInterface : newNetworkInterfaces) {
                    notifyNetworkInterfacesAdded(pcapNetworkInterface);
                }

                for (PcapNetworkInterface pcapNetworkInterface : removedNetworkInterfaces) {
                    notifyNetworkInterfacesRemoved(pcapNetworkInterface);
                }
            }
        }
    };

    private PcapNetworkInterfaceService() {

    }

    /**
     * Returns the {@link PcapNetworkInterfaceService} singleton instance.
     *
     * @return The {@link PcapNetworkInterfaceService} singleton
     */
    public static synchronized PcapNetworkInterfaceService instance() {
        if (instance == null) {
            instance = new PcapNetworkInterfaceService();
        }
        return instance;
    }

    /**
     * Returns a {@link Set} of {@link PcapNetworkInterface}s which are bound to an address.
     *
     * @return the network interface set
     */
    public Set<PcapNetworkInterface> getNetworkInterfaces() {
        synchronized (pcapNetworkInterfaces) {
            return ImmutableSet.copyOf(pcapNetworkInterfaces);
        }
    }

    /**
     * Registers the given {@link PcapNetworkInterfaceListener}. If it is already registered, this method returns
     * immediately.
     *
     * @param networkInterfaceListener The {@link PcapNetworkInterfaceListener} to be registered.
     */
    public void registerListener(PcapNetworkInterfaceListener networkInterfaceListener) {
        final boolean isAdded = listeners.add(networkInterfaceListener);
        if (isAdded) {
            updatePollingState();
        }

    }

    /**
     * Unregisters the given {@link PcapNetworkInterfaceListener}. If it is already unregistered, this method returns
     * immediately.
     *
     * @param networkInterfaceListener The {@link PcapNetworkInterfaceListener} to be unregistered.
     */
    public void unregisterListener(PcapNetworkInterfaceListener networkInterfaceListener) {
        final boolean isRemoved = listeners.remove(networkInterfaceListener);
        if (isRemoved) {
            updatePollingState();
        }
    }

    private void notifyNetworkInterfacesAdded(PcapNetworkInterface pcapNetworkInterface) {
        for (PcapNetworkInterfaceListener listener : listeners) {
            notifyNetworkInterfacesAdded(listener, pcapNetworkInterface);
        }
    }

    private void notifyNetworkInterfacesRemoved(PcapNetworkInterface pcapNetworkInterface) {
        for (PcapNetworkInterfaceListener listener : listeners) {
            notifyNetworkInterfacesRemoved(listener, pcapNetworkInterface);
        }
    }

    private void notifyNetworkInterfacesAdded(PcapNetworkInterfaceListener listener,
            PcapNetworkInterface pcapNetworkInterface) {
        try {
            listener.onPcapNetworkInterfaceAdded(pcapNetworkInterface);
        } catch (Exception e) {
            logger.error("An exception occurred while calling onPcapNetworkInterfaceAdded for " + listener, e);
        }
    }

    private void notifyNetworkInterfacesRemoved(PcapNetworkInterfaceListener listener,
            PcapNetworkInterface pcapNetworkInterface) {
        try {
            listener.onPcapNetworkInterfaceRemoved(pcapNetworkInterface);
        } catch (Exception e) {
            logger.error("An exception occurred while calling onPcapNetworkInterfaceRemoved for " + listener, e);
        }
    }

    /**
     * Returns all pcap network interfaces relying on {@link PcapUtil#getAllNetworkInterfaces()}. The list is filtered
     * as all interfaces which are not bound to an address are excluded.
     *
     * @return An {@link Iterable} of all {@link PcapNetworkInterface}s which are bound to an address
     */
    private Iterable<PcapNetworkInterface> determineBoundNetworkInterfaces() {
        final List<PcapNetworkInterface> allNetworkInterfaces = PcapUtil.getAllNetworkInterfaces();
        return Iterables.filter(allNetworkInterfaces, new Predicate<PcapNetworkInterface>() {

            @Override
            public boolean apply(PcapNetworkInterface networkInterface) {
                final boolean suitable = !networkInterface.getAddresses().isEmpty();
                if (!suitable) {
                    logger.debug("{} is not a suitable network interfaces as no addresses are bound to it.",
                            networkInterface.getName());
                }
                return suitable;
            }
        });
    }

    private void updatePollingState() {
        boolean isPolling = future != null;
        if (isPolling && listeners.isEmpty()) {
            future.cancel(true);
            future = null;
            return;
        }
        if (!isPolling && !listeners.isEmpty()) {
            future = scheduler.scheduleAtFixedRate(pollingRunnable, 0, 2, TimeUnit.SECONDS);
        }
    }

}
