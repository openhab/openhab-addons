package org.openhab.binding.ownet.handler;

import static org.openhab.binding.ownet.OWNetBindingConstants.THING_TYPE_TCP;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.ownet.internal.OWDiscoveryResult;
import org.openhab.binding.ownet.internal.OWNetAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.OneWireContainer12;
import com.dalsemi.onewire.container.OneWireContainer1D;
import com.dalsemi.onewire.container.OneWireContainer26;
import com.dalsemi.onewire.container.OneWireContainer28;
import com.dalsemi.onewire.container.OneWireContainer29;
import com.dalsemi.onewire.container.OneWireContainer3A;

/**
 * The {@link OWNetHandler} class is responsible
 * for connection to Modbus device using
 * tcp communications
 *
 * @author Dmitry Krasnov - Initial contribution
 */
public class OWNetHandler extends BaseBridgeHandler implements DiscoveryParticipant {
    private Logger logger = LoggerFactory.getLogger(OWNetHandler.class);

    private static final String PROP_ADDRESS = "address";
    private static final String PROP_PORT = "port";
    private final static String PROP_REFRESH = "refresh";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_TCP);
    private String address = "localhost";
    private String port = "6161";

    private int refresh = 3000;
    private ScheduledFuture<?> pollingJob;

    private OWNetAdapter adapter = new OWNetAdapter();
    boolean adapterBusy = false;

    private Set<ThingUID> cachedDevices = new HashSet<ThingUID>();

    /**
     * {@inheritDoc}
     */

    boolean isAdapterBusy() {
        return adapterBusy;
    }

    public OWNetHandler(Bridge thing) {
        super(thing);
        try {
            address = thing.getConfiguration().get(PROP_ADDRESS).toString();
            port = thing.getConfiguration().get(PROP_PORT).toString();
            refresh = ((BigDecimal) thing.getConfiguration().get(PROP_REFRESH)).intValue();
        } catch (Exception e) {
        }
    }

    class polling implements Runnable {
        OWNetHandler handler = null;

        public polling(OWNetHandler h) {
            handler = h;
        }

        @Override
        public void run() {
            if (handler != null) {
                handler.update();
            }
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        super.initialize();
        boolean isValid = true;
        try {

            adapter.selectPort(address + ":" + port);
            // if (!adapter.adapterDetected()) {
            // isValid = false;
            // }
        } catch (Exception e) {
            isValid = false;
        }
        updateStatus(isValid ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
        pollingJob = scheduler.scheduleAtFixedRate(new polling(this), 0, refresh, TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public ThingUID getUID() {
        // TODO Auto-generated method stub
        return getThing().getUID();
    }

    public synchronized void update() {
        // synchronized (adapterBusy) {
        // while (adapterBusy) {
        // try {
        // wait();
        // } catch (InterruptedException e) {
        // break;
        // }
        // }
        adapterBusy = true;
        // logger.debug("execute() method is called!");
        try {
            if (!adapter.adapterDetected()) {
                adapterBusy = false;
                notifyAll();
                return;
            }
            adapter.beginExclusive(true);
            adapter.reset();
            adapter.setSearchAllDevices();
            adapter.targetAllFamilies();
            adapter.setSpeed(DSPortAdapter.SPEED_REGULAR);
        } catch (Exception e) {
            adapter.reconnect();
            adapterBusy = false;
            notifyAll();
            return;
        }
        cachedDevices.clear();
        for (Thing t : getThing().getThings()) {
            ThingUID uid = t.getUID();
            String id = t.getUID().getId();
            try {
                if (!adapter.isPresent(id)) {
                    continue;
                }
                OneWireContainer owd = adapter.getDeviceContainer(id);
                int family = Integer.parseInt(id.substring(id.length() - 2), 16);
                byte[] state;
                switch (family) {
                    case 0x12:
                        if (owd instanceof OneWireContainer12) {
                            OneWireContainer12 c = (OneWireContainer12) owd;
                            state = c.readDevice();
                            ((OWDeviceHandler) t.getHandler()).update("contact-1",
                                    c.getLevel(0, state) ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
                            ((OWDeviceHandler) t.getHandler()).update("contact-2",
                                    c.getLevel(1, state) ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
                        }
                        break;
                    case 0x1D:
                        if (owd instanceof OneWireContainer1D) {
                            OneWireContainer1D c = (OneWireContainer1D) owd;
                            ((OWDeviceHandler) t.getHandler()).update("counter-1", new DecimalType(c.readCounter(14)));
                            ((OWDeviceHandler) t.getHandler()).update("counter-2", new DecimalType(c.readCounter(15)));
                        }
                        break;
                    case 0x26:
                        if (owd instanceof OneWireContainer26) {
                            OneWireContainer26 c = (OneWireContainer26) owd;
                            state = c.readDevice();
                            c.doTemperatureConvert(state);
                            ((OWDeviceHandler) t.getHandler()).update("temperature",
                                    new DecimalType(c.getTemperature(state)));
                            c.doHumidityConvert(state);
                            ((OWDeviceHandler) t.getHandler()).update("humidity",
                                    new DecimalType(c.getHumidity(state)));
                        }
                        break;
                    case 0x28:
                        if (owd instanceof OneWireContainer28) {
                            double read;
                            OneWireContainer28 c = (OneWireContainer28) owd;
                            state = c.readDevice();
                            c.doTemperatureConvert(state);
                            read = c.getTemperature(state);
                            ((OWDeviceHandler) t.getHandler()).update("temperature", new DecimalType(read));
                        }
                        break;
                    case 0x29:
                        if (owd instanceof OneWireContainer29) {
                            OneWireContainer29 c = (OneWireContainer29) owd;
                            state = c.readDevice();
                            ((OWDeviceHandler) t.getHandler()).update("switch-1",
                                    c.getLatchState(0, state) ? OnOffType.ON : OnOffType.OFF);
                            ((OWDeviceHandler) t.getHandler()).update("switch-2",
                                    c.getLatchState(1, state) ? OnOffType.ON : OnOffType.OFF);
                            ((OWDeviceHandler) t.getHandler()).update("switch-3",
                                    c.getLatchState(2, state) ? OnOffType.ON : OnOffType.OFF);
                            ((OWDeviceHandler) t.getHandler()).update("switch-4",
                                    c.getLatchState(3, state) ? OnOffType.ON : OnOffType.OFF);
                            ((OWDeviceHandler) t.getHandler()).update("switch-5",
                                    c.getLatchState(4, state) ? OnOffType.ON : OnOffType.OFF);
                            ((OWDeviceHandler) t.getHandler()).update("switch-6",
                                    c.getLatchState(5, state) ? OnOffType.ON : OnOffType.OFF);
                            ((OWDeviceHandler) t.getHandler()).update("switch-7",
                                    c.getLatchState(6, state) ? OnOffType.ON : OnOffType.OFF);
                            ((OWDeviceHandler) t.getHandler()).update("switch-8",
                                    c.getLatchState(7, state) ? OnOffType.ON : OnOffType.OFF);
                        }
                        break;
                    case 0x3A:
                        if (owd instanceof OneWireContainer3A) {
                            OneWireContainer3A c = (OneWireContainer3A) owd;
                            state = c.readDevice();
                            boolean level = c.getLatchState(0, state);
                            ((OWDeviceHandler) t.getHandler()).update("switch-1", level ? OnOffType.OFF : OnOffType.ON);
                            ((OWDeviceHandler) t.getHandler()).update("switch-2",
                                    c.getLatchState(1, state) ? OnOffType.OFF : OnOffType.ON);
                        }
                        break;
                }
                cachedDevices.add(uid);
            } catch (OneWireException e) {
                logger.info(e.getMessage());
                // adapter.reconnect();
            }
        }

        adapter.endExclusive();
        adapterBusy = false;
        notifyAll();
    }
    // }

    @Override
    public synchronized OWDiscoveryResult getDiscoveryResult() {
        Set<ThingUID> workingDevices = new HashSet<ThingUID>();
        Set<ThingUID> addedDevices = new HashSet<ThingUID>();
        // synchronized (adapterBusy) {
        // while (adapterBusy) {
        // try {
        // wait();
        // } catch (InterruptedException e) {
        // break;
        // }
        // }
        adapterBusy = true;
        try {
            if (adapter.adapterDetected()) {
                // if (!adapter.beginExclusive(true)) {
                // adapterBusy = false;
                // notifyAll();
                // return null;
                // }
                adapter.reset();
                for (Enumeration owd_enum = adapter.getAllDeviceContainers(); owd_enum.hasMoreElements();) {
                    OneWireContainer owd = (OneWireContainer) owd_enum.nextElement();
                    String sid = owd.getAddressAsString();
                    ThingUID id = OWDeviceHandler.uidFromAddress(sid);
                    if (cachedDevices.contains(id)) {
                        workingDevices.add(id);
                        cachedDevices.remove(id);
                    } else {
                        addedDevices.add(id);
                    }
                }
                // adapter.reset();
                adapter.endExclusive();
            } else {
                adapter.reconnect();
            }
        } catch (Exception e) {
            adapterBusy = false;
            notifyAll();
            return null;
        }
        OWDiscoveryResult result = new OWDiscoveryResult();
        result.toAdd = addedDevices;
        result.toRemove = cachedDevices;
        cachedDevices = workingDevices;
        cachedDevices.addAll(addedDevices);
        adapterBusy = false;
        notifyAll();
        return result;
    }
    // }

    @Override
    public boolean unDiscover(ThingUID thingUID) {
        if (cachedDevices.contains(thingUID)) {
            cachedDevices.remove(thingUID);
            return true;
        }
        return false;
    }

}