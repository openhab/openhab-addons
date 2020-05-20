package org.openhab.binding.internal.kostal.inverter.secondgeneration;

import java.security.MessageDigest;
import java.util.Base64;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link SecondGenerationConfigurationHandler} is responsible for configuration changes,
 * regarded to second generation part of the binding.
 *
 * @author Örjan Backsell - Initial contribution Piko1020, Piko New Generation
 */

public class SecondGenerationConfigurationHandler {

    public static void executeConfigurationChanges(HttpClient httpClient, String url, String username, String password,
            String dxsId, String value) throws Exception {

        httpClient.start();
        String urlLogin = url + "/api/login.json?";
        String salt = "";
        String sessionId = "";

        String getAuthenticateResponse = httpClient.GET(urlLogin).getContentAsString();
        try {

            JsonObject getAuthenticateResponseJsonObject = (JsonObject) new JsonParser()
                    .parse(transformJsonResponse(getAuthenticateResponse));

            sessionId = extractSessionId(getAuthenticateResponseJsonObject);

            JsonObject authenticateJsonObject = new JsonParser().parse(getAuthenticateResponse.toString())
                    .getAsJsonObject();
            salt = authenticateJsonObject.get("salt").getAsString();

            String saltedPassword = new StringBuffer(password).append(salt).toString();
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");

            byte[] mDigestedPassword = mDigest.digest(saltedPassword.getBytes());
            StringBuffer loginPostStringBuffer = new StringBuffer();
            for (int i = 0; i < mDigestedPassword.length; i++) {
                loginPostStringBuffer.append(Integer.toString((mDigestedPassword[i] & 0xff) + 0x100, 16).substring(1));
            }
            String saltedmDigestedPwd = Base64.getEncoder().encodeToString(mDigest.digest(saltedPassword.getBytes()));

            String loginPostJsonData = "{\"mode\":1,\"userId\":\"" + username + "\",\"pwh\":\"" + saltedmDigestedPwd
                    + "\"}";

            Request loginPostJsonResponse = httpClient.POST(urlLogin + "?sessionId=" + sessionId);
            loginPostJsonResponse.header(HttpHeader.CONTENT_TYPE, "application/json");
            loginPostJsonResponse.content(new StringContentProvider(loginPostJsonData));
            ContentResponse loginPostJsonDataContentResponse = loginPostJsonResponse.send();

            String loginPostResponse = new String(loginPostJsonDataContentResponse.getContent());

            JsonObject loginPostJsonObject = (JsonObject) new JsonParser()
                    .parse(transformJsonResponse(loginPostResponse));

            sessionId = extractSessionId(loginPostJsonObject);

            // Part to sending data to Inverter
            String postJsonData = "";

            if (dxsId.contentEquals("16777984")) {
                // Works with inverterName, name will be changed, due to "" around value
                postJsonData = "{\"dxsEntries\":[{\"dxsId\":" + dxsId + ",\"value\":\"" + value + "\"}]}";
            } else {
                // Works not with inverterName, name will not be changed, due to "" around value, but the other
                // configuration options will be changed.
                postJsonData = "{\"dxsEntries\":[{\"dxsId\":" + dxsId + ",\"value\":" + value + "}]}";
            }

            Request postJsonDataRequest = httpClient.POST(url + "/api/dxs.json?sessionId=" + sessionId);
            postJsonDataRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
            postJsonDataRequest.content(new StringContentProvider(postJsonData));

            ContentResponse postJsonDataContentResponse = postJsonDataRequest.send();
            String postResponse = new String(postJsonDataContentResponse.getContent());

            JsonObject postJsonObject = (JsonObject) new JsonParser().parse(transformJsonResponse(postResponse));
            sessionId = extractSessionId(postJsonObject);

        } catch (JsonIOException e) {
            e.printStackTrace();
        }

        httpClient.stop();
    }

    static String transformJsonResponse(String jsonResponse) {
        // Method transformJsonResponse converts response,due to missing [] in JSON getAuthenticateResponse.

        int sessionStartPosition = jsonResponse.indexOf("session");
        StringBuffer transformStringBuffer = new StringBuffer();

        transformStringBuffer.append(jsonResponse);

        transformStringBuffer.insert(sessionStartPosition + 9, '[');
        int codeStartPosition = jsonResponse.indexOf("roleId");
        transformStringBuffer.insert(codeStartPosition + 11, ']');

        String transformJsonObject = transformStringBuffer.toString();

        return transformJsonObject;
    }

    static String extractSessionId(JsonObject extractJsonObject) throws Exception {
        // Method extractSessionId extracts sessionId from JsonObject
        String extractSessionId = "";
        JsonArray extractJsonArray = extractJsonObject.getAsJsonArray("session");
        for (int i = 0; i < extractJsonArray.size(); i++) {
            extractSessionId = extractJsonArray.get(i).getAsJsonObject().get("sessionId").getAsString();
        }
        return extractSessionId;
    }
}
