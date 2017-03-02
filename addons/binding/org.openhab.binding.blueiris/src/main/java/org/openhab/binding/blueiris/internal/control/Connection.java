package org.openhab.binding.blueiris.internal.control;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.blueiris.internal.config.Config;
import org.openhab.binding.blueiris.internal.data.BlueIrisCommandRequest;
import org.openhab.binding.blueiris.internal.data.CamListRequest;
import org.openhab.binding.blueiris.internal.data.LoginReply;
import org.openhab.binding.blueiris.internal.data.LoginRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The connection to the blue iris machine.
 *
 * @author David Bennett - Initial Contribution
 */
public class Connection {
    private static final long SOCKET_TIMEOUT = 0;

    private Logger logger = LoggerFactory.getLogger(Connection.class);

    private HttpClient client;
    private Config config;
    private Gson gson;
    private MessageDigest messageDigest;
    private String md5Hash;
    private String session;
    private List<ConnectionListener> listeners = Lists.newArrayList();
    private LoginReply loginReply;

    public Connection(Config config) throws NoSuchAlgorithmException {
        this.client = new HttpClient();
        this.config = config;
        this.gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        this.messageDigest = MessageDigest.getInstance("MD5");
    }

    /**
     * Initialize the connection and get the first list of cameras from blue iris.
     */
    public boolean initialize() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LoginRequest login = new LoginRequest();
                if (sendCommand(login)) {
                    loginReply = login.getReply();
                    // Now we hash up stuff.
                    String format = String.format("%s:%s:%s", config.user, loginReply.getSession(), config.password);
                    messageDigest.reset();
                    try {
                        messageDigest.update(format.getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        logger.error("Unable to handle md5 hash", e);
                        for (ConnectionListener listener : listeners) {
                            listener.onFailedToLogin();
                        }
                        return;
                    }
                    byte[] digest = messageDigest.digest();
                    BigInteger bigInt = new BigInteger(1, digest);
                    md5Hash = bigInt.toString(16);
                    session = loginReply.getSession();
                    LoginRequest finalLogin = new LoginRequest();
                    finalLogin.setResponse(md5Hash);
                    if (sendCommand(finalLogin)) {
                        for (ConnectionListener listener : listeners) {
                            listener.onLogin(finalLogin.getReply());
                        }
                        // Now get the list of cameras too/
                        CamListRequest request = new CamListRequest();
                        if (sendCommand(request)) {
                            for (ConnectionListener listener : listeners) {
                                listener.onCamList(request.getReply());
                            }
                        }
                    }
                }
            }

        });
        try {
            this.client.start();
        } catch (Exception e) {
            logger.error("Unable to start http client", e);
            return false;
        }
        thread.start();
        return true;
    }

    /**
     * @return The login reply after the initialization thingy
     */
    public LoginReply getLoginReply() {
        return loginReply;
    }

    /**
     * Send the command to blue iris and deserialize the response.
     *
     * @param <T>
     *
     * @param request The request to send
     * @return true if it was successfully sent
     */
    public <T> boolean sendCommand(BlueIrisCommandRequest<T> request) {
        String url = "http://" + this.config.ipAddress + ":" + this.config.port + "/json";

        try {
            if (session != null) {
                request.setSession(this.session);
                // request.setResponse(this.md5Hash);
            }
            String output = gson.toJson(request);
            if (request.getCmd().equals("camconfig")) {
                output = "{\"cmd\":\"camconfig\",\"camera\":\"cam1\",\"session\":\"" + this.session + "\"}";
            }

            ContentResponse response;
            synchronized (client) {
                Request httpRequest = this.client.newRequest(url).method(HttpMethod.POST)
                        .timeout(SOCKET_TIMEOUT, TimeUnit.MILLISECONDS).content(new StringContentProvider(output));
                logger.error("Sending {}", output);
                response = httpRequest.send();
            }

            if (response.getStatus() == HttpStatus.OK_200) {
                Object data = request.deserializeReply(new StringReader(response.getContentAsString()), gson);
                if (data != null) {
                    logger.error("Returned {}", data.toString());
                    return true;
                }
                logger.info("No data returned {}", response.getContentAsString());
            }
            return false;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Unable to send http request to {}", url, e);
            return false;
        }
    }

    public void addListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Dispose the object, clean up any references it has in memory.
     */
    public void dispose() {
        this.gson = null;
        this.client = null;
        this.config = null;
        this.listeners.clear();
        this.md5Hash = null;
        this.messageDigest = null;
        this.session = null;
        try {
            this.client.stop();
        } catch (Exception e) {
            logger.error("Unable to stop http client", e);
        }
        this.client = null;
    }
}
