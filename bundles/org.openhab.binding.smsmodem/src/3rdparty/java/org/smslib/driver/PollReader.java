package org.smslib.driver;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.CommunicationException;

/**
 * Manage communications with a serial modem
 * Extracted from SMSLib
 */
public class PollReader extends Thread {

    static Logger logger = LoggerFactory.getLogger(AbstractModemDriver.class);

    private boolean shouldCancel = false;

    private boolean foundClip = false;

    public PollReader(AbstractModemDriver modemDriver, String threadId) {
        this.modemDriver = modemDriver;
        this.threadId = threadId;
    }

    private AbstractModemDriver modemDriver;

    private String threadId;

    public void cancel() {
        this.shouldCancel = true;
        this.interrupt();
    }

    @Override
    public void run() {
        logger.debug("Started!");
        currentThread().setName("OH-binding-smsmodem-" + threadId);
        while (!this.shouldCancel) {
            try {
                while (modemDriver.hasData()) {
                    char c = (char) modemDriver.read();
                    modemDriver.buffer.append(c);
                    if (modemDriver.buffer.indexOf("+CLIP") >= 0) {
                        if (!this.foundClip) {
                            this.foundClip = true;
                            new ClipReader().start();
                        }
                    } else {
                        this.foundClip = false;
                    }
                }
            } catch (IOException e) {
                logger.debug("Cannot proceed to poll device", e);
                modemDriver.modem.error();
            }
            AbstractModemDriver.countSheeps(Integer.valueOf(modemDriver.getModemSettings("poll_reader")));
        }
        logger.debug("Stopped!");
    }

    public class ClipReader extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                modemDriver.atATWithResponse();
            } catch (InterruptedException | CommunicationException e) {
                logger.debug("Cannot proceed to read clip", e);
            }
        }
    }

}