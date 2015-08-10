package org.openhab.binding.honeywellwifithermostat.internal.webapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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

/**
 * The {@link honeywellWifiThermostatBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author JD Steffen - Initial contribution
 */

public class HoneywellWebsite {
    private static HoneywellWebsite instance = null;
    private Logger logger = LoggerFactory.getLogger(honeywellWifiThermostatHandler.class);

    private String username;
    private String password;

    private CloseableHttpClient httpclient;

    protected HoneywellWebsite() {
    }

    public static HoneywellWebsite getInstance() {
        if (instance == null) {
            instance = new HoneywellWebsite();
        }
        return instance;
    }

    public void dispose() {
        try {
            httpclient.close();
        } catch (IOException e) {
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
        HttpPost req = new HttpPost("https://mytotalconnectcomfort.com/portal/Locations/");
        req.addHeader("X-Requested-With", "XMLHttpRequest");

        try {
            CloseableHttpResponse resp = httpclient.execute(req);
            if (resp.getStatusLine().getStatusCode() == 500) {
                return false;
            }
            if (resp.getStatusLine().getStatusCode() == 401) {
                // Try to login again.
                tryLogin();
                resp = httpclient.execute(req);
                if (resp.getStatusLine().getStatusCode() != 200) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean tryLogin() {
        try {
            SSLContext ctx = SSLContexts.createSystemDefault();

            SSLConnectionSocketFactory fac = new SSLConnectionSocketFactory(ctx, new String[] { "TLSv1" }, null,
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            httpclient = HttpClientBuilder.create().setSSLSocketFactory(fac)
                    .setRedirectStrategy(new LaxRedirectStrategy()).disableContentCompression().build();

            HttpPost httpPost = new HttpPost("https://mytotalconnectcomfort.com/portal/");
            List<NameValuePair> fields = new ArrayList<NameValuePair>();
            fields.add(new BasicNameValuePair("timeOffset", "0"));
            fields.add(new BasicNameValuePair("UserName", username));
            fields.add(new BasicNameValuePair("Password", password));

            httpPost.setEntity(new UrlEncodedFormEntity(fields));
            CloseableHttpResponse resp = httpclient.execute(httpPost);

            HttpEntity entity = resp.getEntity();

            Document doc = Jsoup.parse(EntityUtils.toString(entity));

            Element result = doc.select("div.validation-summary-errors").first();
            if (result != null) {
                result = result.select("span").first();
                if (result.text().matches("Login was unsuccessful.")) {
                    return false;
                }
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
        logger.debug("Successfully logged into Honeywell Total Connect Comfort website.");
        return true;
    }

    public boolean submitThermostatChange(String deviceID, HoneywellThermostatData thermodata) {
        logger.debug("Submitting thermostat data to website...");

        HttpPost httpPost = new HttpPost("https://mytotalconnectcomfort.com/portal/Device/SubmitControlScreenChanges");
        String jsonData = "{\"DeviceID\":" + deviceID.toString() + ",\"SystemSwitch\":"
                + thermodata.getCurrentSystemMode().getValue() + ",\"HeatSetpoint\":"
                + Integer.toString(thermodata.getHeatSetPoint()) + ",\"CoolSetpoint\":"
                + Integer.toString(thermodata.getCoolSetPoint())
                + ",\"HeatNextPeriod\":null,\"CoolNextPeriod\":null,\"StatusHeat\":null,\"StatusCool\":null,\"FanMode\":"
                + thermodata.getCurrentFanMode().getValue() + "}";
        httpPost.setEntity(new StringEntity(jsonData, ContentType.APPLICATION_JSON));

        if (isLoginValid()) {
            try {
                CloseableHttpResponse resp = httpclient.execute(httpPost);
                String str = EntityUtils.toString(resp.getEntity());

                if (!str.equals("{\"success\":1}")) {
                    logger.error("Failed to sumbit thermostat data.");
                    return false;
                }
            } catch (IOException e) {
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

        HttpPost req = new HttpPost(
                "https://mytotalconnectcomfort.com/portal/Device/CheckDataSession/" + deviceID.toString());
        req.addHeader("X-Requested-With", "XMLHttpRequest");

        String jsonData;

        if (isLoginValid()) {
            try {
                CloseableHttpResponse resp = httpclient.execute(req);
                if (resp.getStatusLine().getStatusCode() != 200) {
                    logger.error("Failed to retrieve thermostat data.");
                    return null;
                }
                jsonData = EntityUtils.toString(resp.getEntity());
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

            } catch (IOException e) {
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
