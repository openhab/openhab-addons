package org.openhab.binding.blueiris.internal.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

/**
 * Base class for all command requests to the blue iris connection.
 *
 * @author David Bennett - Initial Contribution
 */
public abstract class BlueIrisCommandRequest<T> {
    private static Logger logger = LoggerFactory.getLogger(CamListRequest.class);

    @Expose
    private String cmd;
    @Expose
    private String session;
    @Expose
    private String response;
    private T reply;
    private Class<T> classType;

    /**
     * Create a blue iris request. Yay!
     *
     * @param classType the type of the class to turn into a request
     * @param cmd the cmd name
     */
    public BlueIrisCommandRequest(Class<T> classType, String cmd) {
        this.cmd = cmd;
        this.classType = classType;
    }

    /**
     * @return The name of the command.
     */
    public String getCmd() {
        return cmd;
    }

    /**
     * @return The session associated with this request.
     */
    public String getSession() {
        return session;
    }

    /**
     * @param session The session associated with this request.
     */
    public void setSession(String session) {
        this.session = session;
    }

    /**
     * @return The response associated with this request.
     */
    public String getResponse() {
        return response;
    }

    /**
     * @param response The response associated with this request.
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * @return The reply to the request.
     */
    public T getReply() {
        return reply;
    }

    @Override
    public String toString() {
        return "BlueIrisCommandRequest [cmd=" + cmd + "]";
    }

    /**
     * How to handle the reply, doing the deserialization and returning the right result.
     */
    public T deserializeReply(Reader str, Gson gson) {
        BufferedReader buffer = new BufferedReader(str);
        String line;
        try {
            line = buffer.readLine();
            logger.error("Line {}", line);
            this.reply = gson.fromJson(line, this.classType);
        } catch (IOException e) {
            logger.error("Failure reading from the port {}", e);
        } catch (Exception e) {
            logger.error("Got an exception {}", e);
        }
        return this.reply;
    }
}
