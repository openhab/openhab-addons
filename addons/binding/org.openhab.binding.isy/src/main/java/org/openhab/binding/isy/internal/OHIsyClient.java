package org.openhab.binding.isy.internal;

import java.util.List;

public interface OHIsyClient {

    public boolean changeNodeState(String command, String value, String address);

    public List<Node> getNodes();

}
