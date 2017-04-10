package org.openhab.binding.mailserver.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.binding.mailserver.handler.MailServerBridgeHandler;
import org.openhab.binding.mailserver.handler.MailServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;

/**
 * The {@link SmtpReceivedMessageHandler} is responsible for handling received messages
 *
 * @author Jereme Guenther - Initial contribution
 */
public class SmtpReceivedMessageHandler implements MessageHandler {
    MessageContext ctx = null;

    /**
     * The Thing that spawned this factory instance.
     */
    public MailServerBridgeHandler h = null;

    private Logger logger = LoggerFactory.getLogger(MailServerHandler.class);
    protected ItemRegistry itemRegistry = null;
    protected EventPublisher eventPublisher = null;

    private String toAddress = "";

    /**
     * Constructor
     *
     * @param ctx the the context this incoming message is in
     * @param h the bridge that owns this server and wants to receive the message
     */
    public SmtpReceivedMessageHandler(MessageContext ctx, MailServerBridgeHandler h) {
        this.ctx = ctx;
        this.h = h;
    }

    /**
     * A validation function giving the chance to reject a form address
     *
     * @param from the incoming from address
     */
    @Override
    public void from(String from) throws RejectException {
        logger.debug("SMTP Server FROM: {}", from);
    }

    /**
     * A validation function giving the chance to reject a recipient address
     *
     * @param recipient the destination address
     */
    @Override
    public void recipient(String recipient) throws RejectException {
        logger.debug("SMTP Server RECIPIENT: {}", recipient);
    }

    /**
     * The actual receiving function that gets all the message data
     *
     * @param data incoming message data
     */
    @Override
    public void data(InputStream data) throws IOException {
        String convertData = this.convertStreamToString(data);
        logger.debug("MAIL DATA: {}", convertData);

        h.distributeRawMessageBodyData(toAddress, convertData);
    }

    /**
     * A disposal method to clean up things, or to know that things are finished
     */
    @Override
    public void done() {
        logger.debug("SMTP Server Finished");
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        Boolean found = false;
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                if (!found) {
                    String testLine = line.toLowerCase().trim();
                    if (testLine.startsWith("To: ") || testLine.startsWith("for ")) {
                        toAddress = testLine.substring(4);
                        found = true;
                    }
                }
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            logger.warn("SMTP Stream Convert Error {}", e.getMessage());
        }
        return sb.toString();
    }

}
