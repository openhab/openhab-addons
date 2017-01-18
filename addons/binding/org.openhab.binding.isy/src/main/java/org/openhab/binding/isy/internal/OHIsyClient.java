package org.openhab.binding.isy.internal;

import java.util.Collection;
import java.util.List;

public interface OHIsyClient {

    public boolean changeNodeState(String command, String value, String address);

    public boolean changeVariableState(String type, String id, int value);

    public List<Node> getNodes();

    public Collection<Program> getPrograms();

    public List<Variable> getVariables();

}
