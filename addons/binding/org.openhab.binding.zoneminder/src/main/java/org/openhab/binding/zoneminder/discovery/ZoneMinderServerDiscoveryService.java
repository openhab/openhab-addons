package org.openhab.binding.zoneminder.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
//import org.apache.commons.net.util.SubnetUtils;
//import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.handler.ZoneMinderServerBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZoneMinderServerDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(ZoneMinderServerDiscoveryService.class);

    public ZoneMinderServerDiscoveryService() throws IllegalArgumentException {
        super(ZoneMinderServerBridgeHandler.SUPPORTED_THING_TYPES, 10);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return ZoneMinderServerBridgeHandler.SUPPORTED_THING_TYPES;
    }

    /*
     * Background discovery disabled for now!
     *
     * @Override
     * protected void startBackgroundDiscovery() {
     * scheduler.schedule(new Runnable() {
     *
     * @Override
     * public void run() {
     * discoverZoneMinderServer();
     * }
     * }, 0, TimeUnit.MILLISECONDS);
     * }
     */

    @Override
    protected void startBackgroundDiscovery() {

    }

    @Override
    protected void startScan() {
        // discoverZoneMinderServer();
    }

    /**
     * Method for ZoneMinder Server Discovery.
     */
    public synchronized void discoverZoneMinderServer() {
        logger.debug("Starting ZoneMinder Bridge Discovery.");
        String ipAddress = "";
        SubnetUtils subnetUtils = null;
        SubnetInfo subnetInfo = null;
        long lowIP = 0;
        long highIP = 0;

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
            subnetUtils = new SubnetUtils(localHost.getHostAddress() + "/"
                    + networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength());
            subnetInfo = subnetUtils.getInfo();
            lowIP = convertIPToNumber(subnetInfo.getLowAddress());
            highIP = convertIPToNumber(subnetInfo.getHighAddress());
        } catch (IllegalArgumentException e) {
            logger.error("discoverZoneMinderServer(): Illegal Argument Exception - {}", e.toString());
            return;
        } catch (Exception e) {
            logger.error("discoverZoneMinderServer(): Error - Unable to get Subnet Information! {}", e.toString());
            return;
        }

        logger.debug("   Local IP Address: {} - {}", subnetInfo.getAddress(),
                convertIPToNumber(subnetInfo.getAddress()));
        logger.debug("   Subnet:           {} - {}", subnetInfo.getNetworkAddress(),
                convertIPToNumber(subnetInfo.getNetworkAddress()));
        logger.debug("   Network Prefix:   {}", subnetInfo.getCidrSignature().split("/")[1]);
        logger.debug("   Network Mask:     {}", subnetInfo.getNetmask());
        logger.debug("   Low IP:           {}", convertNumberToIP(lowIP));
        logger.debug("   High IP:          {}", convertNumberToIP(highIP));

        for (long ip = lowIP; ip <= highIP; ip++) {
            try (Socket socket = new Socket()) {
                ipAddress = convertNumberToIP(ip);

                logger.debug("Discoververing ZoneMinder Server at IPAddress '{}'", ipAddress);
                discoverZoneMinderServerHttp(ipAddress);

            } catch (IllegalArgumentException e) {
                logger.error("discoverZoneMinderServer(): Illegal Argument Exception - {}", e.toString());

            } catch (IOException e) {
                logger.error("discoverZoneMinderServer(): IO Exception! [{}] - {}", ipAddress, e.toString());
            }
        }
    }

    /**
     * Convert an IP address to a number.
     *
     * @param ipAddress
     * @return
     */
    private long convertIPToNumber(String ipAddress) {

        String octets[] = ipAddress.split("\\.");

        if (octets.length != 4) {
            throw new IllegalArgumentException("Invalid IP address: " + ipAddress);
        }

        long ip = 0;

        for (int i = 3; i >= 0; i--) {
            long octet = Long.parseLong(octets[3 - i]);

            if (octet != (octet & 0xff)) {
                throw new IllegalArgumentException("Invalid IP address: " + ipAddress);
            }

            ip |= octet << (i * 8);
        }

        return ip;
    }

    /**
     * Convert a number to an IP address.
     *
     * @param ip
     * @return
     */
    private String convertNumberToIP(long ip) {
        StringBuilder ipAddress = new StringBuilder(15);

        for (int i = 0; i < 4; i++) {

            ipAddress.insert(0, Long.toString(ip & 0xff));

            if (i < 3) {
                ipAddress.insert(0, '.');
            }

            ip = ip >> 8;
        }

        return ipAddress.toString();
    }

    /**
     * Looks for devices that respond back with the proper title tags
     */
    private void discoverZoneMinderServerHttp(String ipAddress) {
        String response = "";

        try {
            response = getHttpDocumentAsString("http://" + ipAddress + "/zm", 1000);
        } catch (Exception e) {

        }

        if (response == null) {
            return;
        }

        if (response.contains("<title>ZM - Login</title>") || response.contains("<title>ZM - Console</title>")
                || response.contains("ZoneMinder")) {
            logger.debug("Discovered ZoneMinder Server at '{}'", ipAddress);

            ThingUID uid = new ThingUID(ZoneMinderConstants.THING_TYPE_BRIDGE_ZONEMINDER_SERVER,
                    ZoneMinderConstants.BRIDGE_ZONEMINDER_SERVER);
            Map<String, Object> properties = new HashMap<>(1);
            properties.put("hostname", ipAddress);
            properties.put("port", new Integer(80));
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel("ZoneMinder Server").build();
            thingDiscovered(result);
        }

    }

    /**
     * Performs a get request
     *
     * @param url
     *            to get
     * @return the string response or null
     * @throws IOException
     */
    /*
     * private String get(String url) {
     * String response = null;
     * try {
     * URL _url = new URL(url);
     * URLConnection connection = _url.openConnection();
     * response = IOUtils.toString(connection.getInputStream());
     * } catch (MalformedURLException e) {
     * logger.debug("Constructed url '{}' is not valid: {}", url, e.getMessage());
     * } catch (IOException e) {
     * logger.debug("Error accessing url '{}' : {} ", url, e.getMessage());
     * }
     * return response;
     * }
     */
    protected String getHttpDocumentAsString(String url, int timeout) throws IOException {
        StringBuffer response = new StringBuffer();

        URL _url = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) _url.openConnection();

        // Set Connection timeout
        conn.setConnectTimeout(timeout);

        // default is GET
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);

        // act like a browser
        // conn.setRequestProperty("User-Agent", USER_AGENT);
        // conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        // conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        /*
         * if (cookies != null) {
         * for (String cookie : this.cookies) {
         * conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
         * }
         * }
         */
        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            conn.disconnect();
            // Update the cookies
            // setCookies(conn.getHeaderFields().get("Set-Cookie"));
        } else {
            String message = "";
            switch (responseCode) {
                case 404:
                    break;
                default:
                    message = String.format(
                            "An error occured while communicating with ZoneMinder Server: URL='%s', ResponseCode='%d', ResponseMessage='%s'",
                            _url.toString(), responseCode, conn.getResponseMessage());
            }
            logger.error(message);
        }

        return response.toString();

    }

}
