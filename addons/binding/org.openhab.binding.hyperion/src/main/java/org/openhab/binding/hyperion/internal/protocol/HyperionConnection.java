package org.openhab.binding.hyperion.internal.protocol;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.hyperion.internal.protocol.effect.Effect;
import org.openhab.binding.hyperion.internal.protocol.request.ClearAllCommand;
import org.openhab.binding.hyperion.internal.protocol.request.ClearCommand;
import org.openhab.binding.hyperion.internal.protocol.request.ColorCommand;
import org.openhab.binding.hyperion.internal.protocol.request.EffectCommand;
import org.openhab.binding.hyperion.internal.protocol.request.HyperionCommand;
import org.openhab.binding.hyperion.internal.protocol.request.ServerInfoCommand;
import org.openhab.binding.hyperion.internal.protocol.request.TransformCommand;
import org.openhab.binding.hyperion.internal.protocol.transform.ValueGain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;

public class HyperionConnection {

    private Logger logger = LoggerFactory.getLogger(HyperionConnection.class);
    private InetAddress address;
    private int port;
    private List<HyperionStateListener> listeners = new ArrayList<HyperionStateListener>();

    private double valueGain = Double.MIN_VALUE;

    public final static String PROPERTY_VALUEGAIN = "valueGain";
    private final static String JSONPATH_VALUEGAIN = "$.info.transform[0].valueGain";

    public HyperionConnection(InetAddress address, int port) {
        this.setAddress(address);
        this.port = port;
    }

    public HyperionConnection(String sAddress, int port) throws UnknownHostException {
        this(InetAddress.getByName(sAddress), port);
    }

    public void addListener(HyperionStateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(HyperionStateListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public String send(HyperionCommand command) {
        Gson gson = new Gson();
        String sCommand = gson.toJson(command);
        String response = null;
        try (Socket hyperionServer = new Socket(address, port)) {
            DataOutputStream outToServer = new DataOutputStream(hyperionServer.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(hyperionServer.getInputStream()));
            logger.debug("Sending: {}", sCommand);
            outToServer.writeBytes(sCommand + '\n');
            outToServer.flush();
            response = inFromServer.readLine();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        logger.debug(response);
        return response;
    }

    public void setColor(int red, int green, int blue, int priority) throws IOException {
        HyperionCommand command = new ColorCommand(red, green, blue, priority);
        send(command);
    }

    public void clearPriority(int priority) throws IOException {
        HyperionCommand command = new ClearCommand(priority);
        send(command);
    }

    public void clearAll() throws IOException {
        HyperionCommand command = new ClearAllCommand();
        send(command);
    }

    public void setEffect(String effect, int priority) throws IOException {
        HyperionCommand command = new EffectCommand(new Effect(effect), priority);
        send(command);
    }

    public void setValueGain(double value) throws IOException {
        HyperionCommand command = new TransformCommand(new ValueGain(value));
        send(command);
    }

    public String serverInfo() {
        HyperionCommand command = new ServerInfoCommand();
        return send(command);
    }

    public void synchronize() {
        String info = serverInfo();
        double valueGain = JsonPath.read(info, JSONPATH_VALUEGAIN);
        valueGainChangeTo(valueGain);
    }

    protected void valueGainChangeTo(double newValueGain) {
        if (valueGain != newValueGain) {
            notifyListeners(PROPERTY_VALUEGAIN, valueGain, newValueGain);
        }
        this.valueGain = newValueGain;
    }

    private void notifyListeners(String property, Object oldValue, Object newValue) {
        for (HyperionStateListener listener : listeners) {
            listener.stateChanged(property, oldValue, newValue);
        }
    }
}
