package org.smslib.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.Capabilities;
import org.smslib.CommunicationException;
import org.smslib.Capabilities.Caps;
import org.smslib.DeviceInformation.Modes;
import org.smslib.Modem;
import org.smslib.ModemResponse;
import org.smslib.UnrecoverableSmslibException;

/**
 * Extracted from SMSLib
 */
@NonNullByDefault
public abstract class AbstractModemDriver {
    static Logger logger = LoggerFactory.getLogger(AbstractModemDriver.class);

    private Lock lock = new ReentrantLock();

    Properties modemProperties;

    @NonNullByDefault({})
    InputStream in;

    @NonNullByDefault({})
    OutputStream out;

    StringBuffer buffer = new StringBuffer(4096);

    PollReader pollReader = new PollReader(this, "undefined");

    Modem modem;

    boolean responseOk;

    String memoryLocations = "";

    int atATHCounter = 0;

    public abstract void openPort() throws CommunicationException;

    public abstract void closePort();

    public abstract String getPortInfo();

    public AbstractModemDriver(Modem modem) {
        modemProperties = new Properties();
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            if (classLoader != null) {
                try (InputStream inputStream = classLoader.getResourceAsStream("modem.properties")) {
                    modemProperties.load(inputStream);
                }
            }
        } catch (IOException e) {
            throw new UnrecoverableSmslibException("Cannot instantiate modem driver", e);
        }
        this.modem = modem;
    }

    public ModemResponse write(String data) throws CommunicationException {
        return write(data, false);
    }

    public ModemResponse write(String data, boolean skipResponse) throws CommunicationException {
        this.lock.lock();
        try {
            logger.debug("{} <== {}", getPortInfo(), data);
            write(data.getBytes());
            countSheeps(Integer.valueOf(getModemSettings("command_wait_unit")));
            return (new ModemResponse((skipResponse ? "" : getResponse()), (skipResponse ? true : this.responseOk)));
        } finally {
            this.lock.unlock();
        }
    }

    protected boolean hasData() throws IOException {
        return ((this.in != null) && (this.in.available() > 0));
    }

    protected int read() throws IOException {
        return this.in.read();
    }

    protected void write(byte[] s) throws CommunicationException {
        int charDelay = Integer.valueOf(getModemSettings("char_wait_unit"));
        try {
            if (charDelay == 0) {
                this.out.write(s);
            } else {
                for (int i = 0; i < s.length; i++) {
                    byte b = s[i];
                    this.out.write(b);
                    countSheeps(charDelay);
                }
            }
        } catch (IOException e) {
            throw new CommunicationException("Cannot write to device", e);
        }
    }

    protected void write(byte s) throws CommunicationException {
        try {
            this.out.write(s);
        } catch (IOException e) {
            throw new CommunicationException("Cannot write data", e);
        }
    }

    private String getResponse() throws CommunicationException {
        StringBuffer raw = new StringBuffer(256);
        StringBuffer b = new StringBuffer(256);
        try {
            while (true) {
                String line = getLineFromBuffer();
                logger.debug("{} >>> {}", getPortInfo(), line);
                this.buffer.delete(0, line.length() + 2);
                if (line.isBlank()) {
                    continue;
                }
                if (line.charAt(0) == '^') {
                    continue;
                }
                if (line.charAt(0) == '*') {
                    continue;
                }
                if (line.startsWith("RING")) {
                    continue;
                }
                if (line.startsWith("+STIN:")) {
                    continue;
                }
                if (Integer.valueOf(getModemSettings("cpin_without_ok")) == 1) {
                    if (line.startsWith("+CPIN:")) {
                        raw.append(line);
                        raw.append("$");
                        b.append(line);
                        this.responseOk = true;
                        break;
                    }
                }
                if (line.startsWith("+CLIP:")) {
                    write("+++", true);
                    countSheeps(Integer.valueOf(getModemSettings("wait_unit")));
                    write("ATH\r", true);
                    logger.debug("+++ INCREASE ATH");
                    this.atATHCounter++;
                    // no need for a call handler. discard
                    countSheeps(Integer.valueOf(getModemSettings("wait_unit")));
                    continue;
                }
                if (line.indexOf("OK") == 0) {
                    if (this.atATHCounter > 0) {
                        logger.debug("--- DECREASE ATH");
                        this.atATHCounter--;
                        continue;
                    }
                    this.responseOk = true;
                    break;
                }
                if ((line.indexOf("ERROR") == 0) || (line.indexOf("+CMS ERROR") == 0)
                        || (line.indexOf("+CME ERROR") == 0)) {
                    logger.warn("{} ERR==> {}", getPortInfo(), line);
                    this.responseOk = false;
                    break;
                }
                if (b.length() > 0) {
                    b.append('\n');
                }
                raw.append(line);
                raw.append("$");
                b.append(line);
            }
        } catch (IOException | TimeoutException e) {
            throw new CommunicationException("Cannot get response", e);
        }
        logger.debug("{} ==> {}", getPortInfo(), raw.toString());
        return b.toString();
    }

    private String getLineFromBuffer() throws TimeoutException, IOException {
        long startTimeout = System.currentTimeMillis();
        long endTimeout = startTimeout;
        while (this.buffer.indexOf("\r") == -1) {
            endTimeout += Integer.valueOf(getModemSettings("wait_unit"));
            if ((endTimeout - startTimeout) > Integer.valueOf(getModemSettings("timeout"))) {
                throw new TimeoutException("Timeout elapsed for " + getPortInfo());
            }
            countSheeps(Integer.valueOf(getModemSettings("wait_unit")));
        }
        BufferedReader r = new BufferedReader(new StringReader(this.buffer.toString()));
        String line = r.readLine();
        r.close();
        return line;
    }

    public void clearResponses() {
        countSheeps(Integer.valueOf(getModemSettings("wait_unit")) * 1);
        while (this.buffer.length() > 0) {
            this.buffer.delete(0, this.buffer.length());
            countSheeps(Integer.valueOf(getModemSettings("wait_unit")) * 1);
        }
    }

    public String getMemoryLocations() {
        return this.memoryLocations;
    }

    public void initializeModem() throws CommunicationException {
        int counter = 0;
        this.lock.lock();
        try {
            atAT();
            atAT();
            atAT();
            atAT();
            atEchoOff();
            clearResponses();
            this.modem.getDeviceInformation().setManufacturer(atGetManufacturer().getResponseData());
            this.modem.getDeviceInformation().setModel(atGetModel().getResponseData());
            countSheeps(Integer.valueOf(getModemSettings("wait_unit")));
            atFromModemSettings("init1");
            countSheeps(Integer.valueOf(getModemSettings("wait_unit"))
                    * Integer.valueOf(getModemSettings("delay_after_init1")));
            atFromModemSettings("init2");
            countSheeps(Integer.valueOf(getModemSettings("wait_unit"))
                    * Integer.valueOf(getModemSettings("delay_after_init2")));
            clearResponses();
            atEchoOff();
            clearResponses();
            atFromModemSettings("pre_pin");
            countSheeps(Integer.valueOf(getModemSettings("wait_unit"))
                    * Integer.valueOf(getModemSettings("delay_after_pre_pin")));
            while (true) {
                counter++;
                if (counter == 5) {
                    throw new CommunicationException("Modem does not correspond correctly, giving up...");
                }
                ModemResponse simStatus = atGetSimStatus();
                if (simStatus.getResponseData().indexOf("SIM PIN") >= 0) {
                    if (this.modem.getSimPin().isBlank()) {
                        throw new CommunicationException("SIM PIN requested but not defined!");
                    }
                    atEnterPin(this.modem.getSimPin());
                } else if (simStatus.getResponseData().indexOf("READY") >= 0) {
                    break;
                } else if (simStatus.getResponseData().indexOf("OK") >= 0) {
                    break;
                } else if (simStatus.getResponseData().indexOf("ERROR") >= 0) {
                    logger.error("SIM PIN error!");
                }
                logger.debug("SIM PIN Not ok, waiting for a while...");
                countSheeps(Integer.valueOf(getModemSettings("wait_unit"))
                        * Integer.valueOf(getModemSettings("delay_on_sim_error")));
            }
            atFromModemSettings("post_pin");
            countSheeps(Integer.valueOf(getModemSettings("wait_unit"))
                    * Integer.valueOf(getModemSettings("delay_after_post_pin")));
            atEnableClip();
            if (!atNetworkRegistration().isResponseOk()) {
                throw new CommunicationException("Network registration failed!");
            }
            atVerboseOff();
            if (atSetPDUMode().isResponseOk()) {
                this.modem.getDeviceInformation().setMode(Modes.PDU);
            } else {
                logger.debug("Modem does not support PDU, trying to switch to TEXT...");
                if (atSetTEXTMode().isResponseOk()) {
                    Capabilities caps = new Capabilities();
                    caps.set(Caps.CanSendMessage);
                    this.modem.setCapabilities(caps);
                    this.modem.getDeviceInformation().setMode(Modes.TEXT);
                } else {
                    throw new CommunicationException("Neither PDU nor TEXT mode are supported by this modem!");
                }
            }
            atCnmiOff();
            retrieveMemoryLocations();
            refreshDeviceInformation();

        } finally {
            this.lock.unlock();
        }
    }

    public void refreshDeviceInformation() throws CommunicationException {
        this.modem.getDeviceInformation().setManufacturer(atGetManufacturer().getResponseData());
        this.modem.getDeviceInformation().setModel(atGetModel().getResponseData());
        this.modem.getDeviceInformation().setSerialNo(atGetSerialNo().getResponseData());
        this.modem.getDeviceInformation().setImsi(atGetImsi().getResponseData());
        this.modem.getDeviceInformation().setSwVersion(atGetSWVersion().getResponseData());
        this.refreshRssi();
    }

    public void refreshRssi() throws CommunicationException {
        String s = atGetSignalStrengh().getResponseData();
        if (this.responseOk) {
            String s1 = s.split("\\R")[0]; // ensure to get first line only
            s1 = s1.substring(s.indexOf(':') + 1).trim();
            StringTokenizer tokens = new StringTokenizer(s1, ",");
            int rssi = Integer.valueOf(tokens.nextToken().trim());
            this.modem.getDeviceInformation().setRssi(rssi == 99 ? 99 : (-113 + 2 * rssi));
        }
    }

    void retrieveMemoryLocations() throws CommunicationException {
        if (this.memoryLocations.isBlank()) {
            this.memoryLocations = getModemSettings("memory_locations");
            if (this.memoryLocations.isBlank()) {
                this.memoryLocations = "";
            }
            if (this.memoryLocations.isBlank()) {
                try {
                    String response = atGetMemoryLocations().getResponseData();
                    if (response.indexOf("+CPMS:") >= 0) {
                        int i, j;
                        i = response.indexOf('(');
                        while (response.charAt(i) == '(') {
                            i++;
                        }
                        j = i;
                        while (response.charAt(j) != ')') {
                            j++;
                        }
                        response = response.substring(i, j);
                        StringTokenizer tokens = new StringTokenizer(response, ",");
                        while (tokens.hasMoreTokens()) {
                            String loc = tokens.nextToken().replaceAll("\"", "");
                            if (!"MT".equalsIgnoreCase(loc) && this.memoryLocations.indexOf(loc) < 0) {
                                this.memoryLocations += loc;
                            }
                        }
                    } else {
                        this.memoryLocations = "SM";
                        logger.debug("CPMS detection failed, proceeding with default memory 'SM'.");
                    }
                } catch (CommunicationException e) {
                    this.memoryLocations = "SM";
                    logger.debug("CPMS detection failed, proceeding with default memory 'SM'.", e);
                }
            }
        } else {
            logger.debug("Using given memory locations: {}", this.memoryLocations);
        }
    }

    public String getSignature(boolean complete) {
        String manufacturer = this.modem.getDeviceInformation().getManufacturer().toLowerCase().replaceAll(" ", "")
                .replaceAll(" ", "").replaceAll(" ", "");
        String model = this.modem.getDeviceInformation().getModel().toLowerCase().replaceAll(" ", "")
                .replaceAll(" ", "").replaceAll(" ", "");
        return (complete ? manufacturer + "_" + model : manufacturer);
    }

    protected ModemResponse atAT() throws CommunicationException {
        return write("AT\r", true);
    }

    protected ModemResponse atATWithResponse() throws CommunicationException {
        return write("AT\r");
    }

    protected ModemResponse atEchoOff() throws CommunicationException {
        return write("ATE0\r", true);
    }

    protected ModemResponse atGetSimStatus() throws CommunicationException {
        return write("AT+CPIN?\r");
    }

    protected ModemResponse atEnterPin(String pin) throws CommunicationException {
        return write(String.format("AT+CPIN=\"%s\"\r", pin));
    }

    protected ModemResponse atNetworkRegistration() throws CommunicationException {
        write("AT+CREG=1\r");
        countSheeps(Integer.valueOf(getModemSettings("wait_unit"))
                * Integer.valueOf(getModemSettings("delay_network_registration")));
        return write("AT+CREG?\r");
    }

    protected ModemResponse atEnableClip() throws CommunicationException {
        return write("AT+CLIP=1\r");
    }

    protected ModemResponse atVerboseOff() throws CommunicationException {
        return write("AT+CMEE=0\r");
    }

    protected ModemResponse atSetPDUMode() throws CommunicationException {
        return write("AT+CMGF=0\r");
    }

    protected ModemResponse atSetTEXTMode() throws CommunicationException {
        return write("AT+CMGF=1\r");
    }

    protected ModemResponse atCnmiOff() throws CommunicationException {
        return write("AT+CNMI=2,0,0,0,0\r");
    }

    protected ModemResponse atGetManufacturer() throws CommunicationException {
        return write("AT+CGMI\r");
    }

    protected ModemResponse atGetModel() throws CommunicationException {
        return write("AT+CGMM\r");
    }

    protected ModemResponse atGetImsi() throws CommunicationException {
        return write("AT+CIMI\r");
    }

    protected ModemResponse atGetSerialNo() throws CommunicationException {
        return write("AT+CGSN\r");
    }

    protected ModemResponse atGetSWVersion() throws CommunicationException {
        return write("AT+CGMR\r");
    }

    protected ModemResponse atGetSignalStrengh() throws CommunicationException {
        return write("AT+CSQ\r");
    }

    public int atSendPDUMessage(int size, String pdu) throws CommunicationException {
        write(String.format("AT+CMGS=%d\r", size), true);
        while (this.buffer.length() == 0) {
            countSheeps(Integer.valueOf(getModemSettings("wait_unit")));
        }
        countSheeps(Integer.valueOf(getModemSettings("wait_unit"))
                * Integer.valueOf(getModemSettings("delay_before_send_pdu")));
        clearResponses();
        write(pdu, true);
        write((byte) 26);
        String response = getResponse();
        if (this.responseOk && response.contains(":")) {
            return Integer.parseInt(response.substring(response.indexOf(":") + 1).trim());
        }
        return -1;
    }

    public int atSendTEXTMessage(String recipient, String text) throws CommunicationException {
        write(String.format("AT+CSCS=\"%s\"\r", "UTF-8"), true);
        if (!this.responseOk) {
            throw new CommunicationException("Unsupported encoding: UTF-8");
        }
        write(String.format("AT+CMGS=\"%s\"\r", recipient), true);
        while (this.buffer.length() == 0) {
            countSheeps(Integer.valueOf(getModemSettings("wait_unit")));
        }
        countSheeps(Integer.valueOf(getModemSettings("wait_unit"))
                * Integer.valueOf(getModemSettings("delay_before_send_pdu")));
        clearResponses();
        write(text, true);
        write((byte) 26);
        String response = getResponse();
        if (this.responseOk) {
            return Integer.parseInt(response.substring(response.indexOf(":") + 1).trim());
        }
        return -1;
    }

    public ModemResponse atGetMemoryLocations() throws CommunicationException {
        return write("AT+CPMS=?\r");
    }

    public ModemResponse atSwitchMemoryLocation(String memoryLocation) throws CommunicationException {
        return write(String.format("AT+CPMS=\"%s\"\r", memoryLocation));
    }

    public ModemResponse atGetMessages(String memoryLocation) throws CommunicationException {
        if (atSwitchMemoryLocation(memoryLocation).isResponseOk()) {
            return (this.modem.getDeviceInformation().getMode() == Modes.PDU ? write("AT+CMGL=4\r")
                    : write("AT+CMGL=\"ALL\"\r"));
        }
        return new ModemResponse("", false);
    }

    public ModemResponse atDeleteMessage(String memoryLocation, int memoryIndex) throws CommunicationException {
        if (atSwitchMemoryLocation(memoryLocation).isResponseOk()) {
            return write(String.format("AT+CMGD=%d\r", memoryIndex));
        }
        return new ModemResponse("", false);
    }

    public ModemResponse atFromModemSettings(String key) throws CommunicationException {
        String atCommand = getModemSettings(key);
        if (!atCommand.isBlank()) {
            return write(atCommand);
        }
        return new ModemResponse("", true);
    }

    public String getModemSettings(String key) {
        String fullSignature = getSignature(true);
        String shortSignature = getSignature(false);
        String value = "";
        if (!fullSignature.isBlank()) {
            value = modemProperties.getProperty(fullSignature + "." + key);
        }
        if ((value == null || value.isBlank()) && !shortSignature.isBlank()) {
            value = modemProperties.getProperty(shortSignature + "." + key);
        }
        if (value == null || value.isBlank()) {
            value = modemProperties.getProperty("default" + "." + key);
        }
        return ((value == null || value.isBlank()) ? "" : value);
    }

    public void lock() {
        this.lock.lock();
    }

    public void unlock() {
        this.lock.unlock();
    }

    protected static void countSheeps(int n) {
        try {
            Thread.sleep(n);
        } catch (InterruptedException e) {
            // Nothing here...
        }
    }
}
