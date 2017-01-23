package org.openhab.binding.tankerkoenig.internal.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.openhab.binding.tankerkoenig.internal.config.TankerkoenigListResult;
import org.openhab.binding.tankerkoenig.internal.serializer.CustomTankerkoenigListResultDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/***
 * Serivce class requesting data from tankerkoenig api and providing result objects
 *
 * @author Dennis Dollinger
 *
 */
public class TankerkoenigService {

    public TankerkoenigListResult GetTankstellenListData(String apikey, String locationIDs) {
        TankerkoenigListResult result = this.GetTankerkoenigListResult(apikey, locationIDs);
        return result;
    }

    String response = "";

    private String getResponseString(String apikey, String locationIDs) throws IOException {
        String urlbase = "https://creativecommons.tankerkoenig.de/json/prices.php?";
        String urlcomplete = urlbase + "ids=" + locationIDs + "&apikey=" + apikey;

        try {

            URL url = new URL(urlcomplete);
            URLConnection connection = url.openConnection();
            response = IOUtils.toString(connection.getInputStream());
            return response;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private TankerkoenigListResult GetTankerkoenigListResult(String apikey, String locationIDs) {

        String jsonData = "";

        try {
            jsonData = getResponseString(apikey, locationIDs);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(TankerkoenigListResult.class, new CustomTankerkoenigListResultDeserializer());
        Gson gson = gsonBuilder.create();
        TankerkoenigListResult res = gson.fromJson(jsonData, TankerkoenigListResult.class);

        return res;

    }

    /**
     *
     * @return Returns a validation result. Empty String is OK, otherwise the error message will be returned
     */

    // public String validate(String apikey, String locationIDs) {
    //
    // TankerkoenigResult result = this.GetTankerkoenigResult(apikey, locationIDs);
    //
    // if (result.getStatus().equals("error")) {
    // return "Error while validating tankerkoénig request. Tankerkoenig.de message: '" + result.getMessage()
    // + "'";
    // }
    // return "";
    //
    // }

}
