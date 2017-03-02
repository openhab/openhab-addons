package org.openhab.binding.blueiris.internal.control;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
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
    private Logger logger = LoggerFactory.getLogger(Connection.class);

    private HttpClient client;
    private Config config;
    private Gson gson;
    private MessageDigest messageDigest;
    private String md5Hash;
    private String session;
    private List<ConnectionListener> listeners = Lists.newArrayList();

    public Connection(Config config) throws NoSuchAlgorithmException {
        this.client = new DefaultHttpClient();
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
                    LoginReply reply = login.getLoginReply();
                    // Now we hash up stuff.
                    String format = String.format("%s:%s:%s", config.user, reply.getSession(), config.password);
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
                    session = reply.getSession();
                    LoginRequest finalLogin = new LoginRequest();
                    if (sendCommand(finalLogin)) {
                        for (ConnectionListener listener : listeners) {
                            listener.onLogin(finalLogin.getLoginReply());
                        }
                        // Now get the list of cameras too/
                        CamListRequest request = new CamListRequest();
                        if (sendCommand(request)) {
                            for (ConnectionListener listener : listeners) {
                                listener.onCamList(request.getCamListReply());
                            }
                        }
                    }
                }
            }

        });
        thread.start();
        return true;
    }

    /**
     * Send the command to blue iris and deserialize the response.
     *
     * @param request The request to send
     * @return true if it was successfully sent
     */
    public boolean sendCommand(BlueIrisCommandRequest request) {
        String url = "http://" + this.config.ipAddress + ":" + this.config.port + "/json";
        HttpPost post = new HttpPost(url);
        StringEntity input;
        try {
            if (session != null) {
                request.setSession(this.session);
                request.setResponse(this.md5Hash);
            }
            String output = gson.toJson(request);
            input = new StringEntity(output);
            post.setEntity(input);
            HttpResponse response = client.execute(post);
            Object data = request.deserializeReply(new InputStreamReader(response.getEntity().getContent()), gson);
            if (data != null) {
                logger.error("Returned {}", data.toString());
                if (response.getStatusLine().getStatusCode() != 200) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        } catch (ClientProtocolException e) {
            logger.error("Unable to connect to the blue iris system {}", this.config, e);
            return false;
        } catch (UnsupportedEncodingException e) {
            logger.error("Unable to connect to the blue iris system {}", this.config, e);
            return false;
        } catch (IOException e) {
            logger.error("Unable to connect to the blue iris system {}", this.config, e);
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
    }
}
