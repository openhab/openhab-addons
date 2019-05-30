package org.openhab.binding.openthermgateway.internal;

public enum MessageType {
    ReadData, // 000
    ReadAck, // 100
    WriteData, // 001
    WriteAck, // 101
    InvalidData, // 010
    DataInvalid, // 110
    Reserved, // 011
    UnknownDataId // 111
}
