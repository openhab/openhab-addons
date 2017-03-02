package org.openhab.binding.blueiris.internal.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.openhab.binding.blueiris.internal.control.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Login to the blue iris system and get back a login reply with session details.
 *
 * @author David Bennett - Initial Contribution
 */
public class LoginRequest extends BlueIrisCommandRequest {
    private static Logger logger = LoggerFactory.getLogger(Connection.class);
    private LoginReply reply;

    public LoginRequest() {
        super("login");
    }

    public LoginReply getLoginReply() {
        return reply;
    }

    @Override
    public LoginReply deserializeReply(InputStreamReader str, Gson gson) {
        BufferedReader buffer = new BufferedReader(str);
        String line;
        try {
            line = buffer.readLine();
            logger.error("Line {}", line);
            this.reply = gson.fromJson(line, LoginReply.class);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("Failure reading from the port", e);
        } catch (Exception e) {
            logger.error("Got an exception");
        }
        return this.reply;
    }

}
