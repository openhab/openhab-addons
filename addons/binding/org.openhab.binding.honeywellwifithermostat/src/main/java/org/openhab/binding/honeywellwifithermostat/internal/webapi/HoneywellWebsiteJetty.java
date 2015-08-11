package org.openhab.binding.honeywellwifithermostat.internal.webapi;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openhab.binding.honeywellwifithermostat.handler.honeywellWifiThermostatHandler;
import org.openhab.binding.honeywellwifithermostat.internal.data.HoneywellThermostatData;
import org.openhab.binding.honeywellwifithermostat.internal.data.HoneywellThermostatFanMode;
import org.openhab.binding.honeywellwifithermostat.internal.data.HoneywellThermostatSystemMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoneywellWebsiteJetty {

    private static HoneywellWebsiteJetty instance = null;
    private Logger logger = LoggerFactory.getLogger(honeywellWifiThermostatHandler.class);

    private String username;
    private String password;

    HttpClient httpclient;

    protected HoneywellWebsiteJetty() {
    }

    public static HoneywellWebsiteJetty getInstance() {
        if (instance == null) {
            instance = new HoneywellWebsiteJetty();
        }
        return instance;
    }

    public void dispose() {
        try {
            httpclient.stop();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public boolean isLoginValid() {
        if (username == null || password == null) {
            return false;
        }
        if (httpclient == null) {
            tryLogin();
        }
        try {
            ContentResponse cr = httpclient.GET("https://mytotalconnectcomfort.com/portal/Locations/");

            if (cr.getStatus() == 500) {
                return false;
            }
            if (cr.getStatus() == 401) {
                // Try to login again.
                tryLogin();
                cr = httpclient.GET("https://mytotalconnectcomfort.com/portal/Locations/");

            }
            if (cr.getStatus() != 200) {
                return false;
            }

        } catch (InterruptedException | TimeoutException | ExecutionException e) {

            e.printStackTrace();
        }

        return true;
    }

    private boolean tryLogin() {

        SslContextFactory sslFactory = new SslContextFactory();

        sslFactory.setExcludeProtocols(new String[] { "TLS", "TLSv1.2", "TLSv1.1" });

        httpclient = new HttpClient(sslFactory);
        httpclient.setFollowRedirects(true);
        try {
            httpclient.start();

            // ContentResponse cr = httpclient.POST("https://mytotalconnectcomfort.com/portal/").param("timeOffset",
            // "0")
            // .param("UserName", username).param("Password", password).send();

            Fields fields = new Fields();
            fields.add("timeOffset", "0");
            fields.add("UserName", username);
            fields.add("Password", password);

            ContentResponse cr = httpclient.FORM("https://mytotalconnectcomfort.com/portal/", fields);

            Document doc = Jsoup.parse(cr.getContentAsString());
            Element result = doc.select("div.validation-summary-errors").first();
            if (result != null) {
                result = result.select("span").first();
                if (result.text().matches("Login was unsuccessful.")) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.debug("Successfully logged into Honeywell Total Connect Comfort website.");
        return true;
    }

    public boolean submitThermostatChange(String deviceID, HoneywellThermostatData thermodata) {
        logger.debug("Submitting thermostat data to website...");

        String jsonData = "{\"DeviceID\":" + deviceID.toString() + ",\"SystemSwitch\":"
                + thermodata.getCurrentSystemMode().getValue() + ",\"HeatSetpoint\":"
                + Integer.toString(thermodata.getHeatSetPoint()) + ",\"CoolSetpoint\":"
                + Integer.toString(thermodata.getCoolSetPoint())
                + ",\"HeatNextPeriod\":null,\"CoolNextPeriod\":null,\"StatusHeat\":null,\"StatusCool\":null,\"FanMode\":"
                + thermodata.getCurrentFanMode().getValue() + "}";

        if (isLoginValid()) {

            try {
                ContentResponse cr = httpclient
                        .POST("https://mytotalconnectcomfort.com/portal/Device/SubmitControlScreenChanges")
                        .content(new StringContentProvider("application/json", jsonData, Charset.forName("UTF-8")))
                        .send();

                if (!cr.getContentAsString().equals("{\"success\":1}")) {
                    logger.error("Failed to sumbit thermostat data.");
                    return false;
                }

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.debug("Successfully submitted thermostat data");
            return true;
        } else {
            return false;
        }
    }

    public HoneywellThermostatData getTherostatData(String deviceID) {
        HoneywellThermostatData thermodata = new HoneywellThermostatData();

        if (isLoginValid()) {
            String jsonData;
            try {

                ContentResponse cr = httpclient.newRequest(
                        "https://mytotalconnectcomfort.com/portal/Device/CheckDataSession/" + deviceID.toString())
                        .header("X-Requested-With", "XMLHttpRequest").send();

                if (cr.getStatus() != 200) {
                    logger.error("Failed to retrieve thermostat data.");
                    return null;
                }

                jsonData = cr.getContentAsString();

                logger.debug("Retrieved thermostat data successfully.");
                JSONObject obj = new JSONObject(jsonData);

                thermodata.setCurrentTemperature(
                        obj.getJSONObject("latestData").getJSONObject("uiData").getInt("DispTemperature"));
                thermodata.setHeatSetPoint(
                        obj.getJSONObject("latestData").getJSONObject("uiData").getInt("HeatSetpoint"));
                thermodata.setCoolSetPoint(
                        obj.getJSONObject("latestData").getJSONObject("uiData").getInt("CoolSetpoint"));

                switch (obj.getJSONObject("latestData").getJSONObject("uiData").getInt("SystemSwitchPosition")) {
                    case 1:
                        thermodata.setCurrentSystemMode(HoneywellThermostatSystemMode.HEAT);
                        break;

                    case 2:
                        thermodata.setCurrentSystemMode(HoneywellThermostatSystemMode.OFF);
                        break;

                    case 3:
                        thermodata.setCurrentSystemMode(HoneywellThermostatSystemMode.COOL);
                        break;

                    default:
                        thermodata.setCurrentSystemMode(HoneywellThermostatSystemMode.OFF);
                        break;
                }

                switch (obj.getJSONObject("latestData").getJSONObject("fanData").getInt("fanMode")) {
                    case 0:
                        thermodata.setCurrentFanMode(HoneywellThermostatFanMode.AUTO);
                        break;
                    case 1:
                        thermodata.setCurrentFanMode(HoneywellThermostatFanMode.ON);
                        break;
                    default:
                        thermodata.setCurrentFanMode(HoneywellThermostatFanMode.AUTO);
                }

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return thermodata;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
