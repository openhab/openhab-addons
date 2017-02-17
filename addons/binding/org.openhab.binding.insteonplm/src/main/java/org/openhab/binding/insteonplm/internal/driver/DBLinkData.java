package org.openhab.binding.insteonplm.internal.driver;

import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Message;

import com.google.common.collect.Lists;

public class DBLinkData {
    private final List<Byte> data;
    private final byte group;
    private final byte linkType;
    private final InsteonAddress linkAddr;

    public DBLinkData(Message message) throws FieldException {
        group = message.getByte("AllLinkGroup");
        linkAddr = message.getAddress("LinkAddr");
        linkType = message.getByte("RecordFlags");
        data = Lists.newArrayList(message.getByte("LinkData1"), message.getByte("LinkData2"),
                message.getByte("LinkData1"));
    }

    public byte getGroup() {
        return group;
    }

    public byte getLinkType() {
        return linkType;
    }

    public List<Byte> getData() {
        return data;
    }

    public InsteonAddress getLinkAddress() {
        return linkAddr;
    }
}
