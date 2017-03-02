package org.openhab.binding.blueiris.internal.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * cam list data from blue iris.
 *
 * @author David Bennett - Initial COntribution
 *
 */
public class CamListRequest extends BlueIrisCommandRequest {

    private static Logger logger = LoggerFactory.getLogger(CamListRequest.class);
    private CamListReply reply;

    public CamListRequest() {
        super("camlist");
    }

    public CamListReply getCamListReply() {
        return reply;
    }

    /**
     * How to handle the reply, doing the deserialization and returning the right result.
     */
    @Override
    public CamListReply deserializeReply(InputStreamReader str, Gson gson) {
        BufferedReader buffer = new BufferedReader(str);
        String line;
        try {
            line = buffer.readLine();
            logger.error("Line {}", line);
            this.reply = gson.fromJson(line, CamListReply.class);
        } catch (IOException e) {
            logger.error("Failure reading from the port {}", e);
        } catch (Exception e) {
            logger.error("Got an exception {}", e);
        }
        return this.reply;
    }
}
