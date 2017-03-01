package org.openhab.binding.blueiris.internal.control;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openhab.binding.blueiris.internal.config.Config;
import org.openhab.binding.blueiris.internal.data.BlueIrisCommandRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The connection to the blue iris machine.
 *
 * @author David Bennett - Initial Contribution
 */
public class Connection {
    private Logger logger = LoggerFactory.getLogger(Connection.class);
    HttpClient client;
    Config config;

    public Connection(Config config) {
        this.client = new DefaultHttpClient();
        this.config = config;
    }

    public boolean sendCommand(BlueIrisCommandRequest request) {
        Gson gson = new Gson();
        String url = "http://" + this.config.host + ":" + this.config.port + "/json";
        HttpPost post = new HttpPost(url);
        StringEntity input;
        try {
            input = new StringEntity(gson.toJson(request));
            post.setEntity(input);
            HttpResponse response = client.execute(post);
            Object data = request.deserializeReply(new InputStreamReader(response.getEntity().getContent()), gson);
            if (data != null) {
                logger.debug("Returned {}", data.toString());
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
}
