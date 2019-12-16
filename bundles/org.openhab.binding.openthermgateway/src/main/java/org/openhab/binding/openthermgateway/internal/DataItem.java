package org.openhab.binding.openthermgateway.internal;

public class DataItem {
    private int id;
    private Msg msg;
    private ByteType byteType;
    private DataType dataType;
    private int bitpos;
    private String subject;

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public Msg getMsg() {
        return msg;
    }

    public void setMsg(Msg msg) {
        this.msg = msg;
    }

    public void setByteType(ByteType byteType) {
        this.byteType = byteType;
    }

    public ByteType getByteType() {
        return this.byteType;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public int getBitPos() {
        return bitpos;
    }

    public void setBitPos(int bitpos) {
        this.bitpos = bitpos;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public DataItem(int id, Msg msg, ByteType byteType, DataType dataType, int bit, String subject) {
        this.id = id;
        this.msg = msg;
        this.byteType = byteType;
        this.dataType = dataType;
        this.bitpos = bit;
        this.subject = subject;
    }
}
