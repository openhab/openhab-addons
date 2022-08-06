package org.openhab.binding.liqiudcheck.internal.discovery;

import static org.openhab.binding.liqiudcheck.internal.LiqiudCheckBindingConstants.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.liqiudcheck.internal.json.Response;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.liquidcheck")
public class LiquidCheckDiscoveryService extends AbstractDiscoveryService {

    private static final int DISCOVER_TIMEOUT_SECONDS = 60;
    private static final int BACKGROUND_DISCOVERY_AFTER_MINUTES = 60;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private @Nullable ScheduledFuture<?> liquidCheckBackgroundDiscoveryJob;

    public LiquidCheckDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS);
    }

    @Override
    protected void startScan() {
        // TODO Auto-generated method stub
        try {
            List<InetAddress> addresses = getIPv4Adresses();
            List<InetAddress> hosts = findActiveHosts(addresses);
            HttpClient client = new HttpClient();
            client.start();
            for (InetAddress host : hosts) {
                Request request = client.newRequest("http://" + host.getHostAddress() + "/infos.json");
                request.followRedirects(false);
                ContentResponse response = request.send();
                if (response.getStatus() == 200) {
                    Response json = new Gson().fromJson(response.getContentAsString(), Response.class);
                    if (null != json) {
                        buildDiscoveryResult(json);
                    } else {
                        logger.debug("Response Object is null!");
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Method to stop the scan
     */
    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    /**
     * Method for starting the background discovery
     */
    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> avoidNullException = this.liquidCheckBackgroundDiscoveryJob;
        if (null == avoidNullException || avoidNullException.isCancelled()) {
            this.liquidCheckBackgroundDiscoveryJob = scheduler.scheduleWithFixedDelay(liquidCheckDiscoveryRunnable(), 0,
                    BACKGROUND_DISCOVERY_AFTER_MINUTES, TimeUnit.MINUTES);
        }
    }

    /**
     * Method for stopping the background discovery
     */
    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> avoidNullException = this.liquidCheckBackgroundDiscoveryJob;
        if (null != avoidNullException && !avoidNullException.isCancelled()) {
            avoidNullException.cancel(true);
            this.liquidCheckBackgroundDiscoveryJob = null;
        }
    }

    /**
     * Method for starting the scan
     */
    protected Runnable liquidCheckDiscoveryRunnable() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                startScan();
            }
        };
        return runnable;
    }

    /**
     * This Method retrieves all IPv4 addresses of the server
     * 
     * @return A list of all available IPv4 Adresses that are registered
     * @throws SocketException
     */
    private List<InetAddress> getIPv4Adresses() throws SocketException {
        Iterator<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces().asIterator();
        List<InetAddress> addresses = new ArrayList<>();
        // Get IPv4 addresses from all network interfaces
        if (null != networkInterfaces) {
            while (networkInterfaces.hasNext()) {
                NetworkInterface currentNetworkInterface = networkInterfaces.next();
                Iterator<InetAddress> inetAddresses = currentNetworkInterface.getInetAddresses().asIterator();
                while (inetAddresses.hasNext()) {
                    InetAddress currentAddress = inetAddresses.next();
                    if (currentAddress instanceof Inet4Address && !currentAddress.isLoopbackAddress()) {
                        addresses.add(currentAddress);
                    }
                }
            }
        }
        return addresses;
    }

    private List<InetAddress> findActiveHosts(List<InetAddress> addresses) throws UnknownHostException, IOException {
        List<InetAddress> hosts = new ArrayList<>();
        for (InetAddress inetAddress : addresses) {
            String[] adresStrings = inetAddress.getHostAddress().split("[.]");
            String subnet = adresStrings[0] + "." + adresStrings[1] + "." + adresStrings[2];
            int timeout = 50;
            for (int i = 1; i < 255; i++) {
                String host = subnet + "." + i;
                if (InetAddress.getByName(host).isReachable(timeout)) {
                    hosts.add(InetAddress.getByName(host));
                }
            }
        }
        return hosts;
    }

    private void buildDiscoveryResult(Response response) {
        LiquidCheckProperties lcproperties = new LiquidCheckProperties(response);
        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_ID_FIRMWARE, lcproperties.firmware);
        properties.put(CONFIG_ID_HARDWARE, lcproperties.hardware);
        properties.put(CONFIG_ID_NAME, lcproperties.name);
        properties.put(CONFIG_ID_MANUFACTURER, lcproperties.manufacturer);
        properties.put(CONFIG_ID_UUID, lcproperties.uuid);
        properties.put(CONFIG_ID_SECURITY_CODE, lcproperties.code);
        properties.put(CONFIG_ID_IP, lcproperties.ip);
        properties.put(CONFIG_ID_MAC, lcproperties.mac);
        properties.put(CONFIG_ID_SSID, lcproperties.ssid);
        ThingUID thingUID = new ThingUID(THING_TYPE_LIQUID_CHEK, lcproperties.uuid);
        DiscoveryResult dResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withLabel(lcproperties.name + "_DEBUG").build();
        thingDiscovered(dResult);
    }
}
