package org.openhab.binding.antiferencematrix.internal.model;

import java.util.LinkedList;
import java.util.List;

public class PortList extends Response {

    private List<Port> ports;

    /**
     * A list of InputPort and OutputPort
     *
     * @return The ports on this matrix
     */
    public List<Port> getPorts() {
        return ports;
    }

    public List<InputPort> getInputPorts() {
        List<InputPort> inputPorts = new LinkedList<InputPort>();
        for (Port port : ports) {
            if (port instanceof InputPort) {
                inputPorts.add((InputPort) port);
            }
        }
        return inputPorts;
    }

    public List<OutputPort> getOutputPorts() {
        List<OutputPort> outputPorts = new LinkedList<OutputPort>();
        for (Port port : ports) {
            if (port instanceof OutputPort) {
                outputPorts.add((OutputPort) port);
            }
        }
        return outputPorts;
    }

}
