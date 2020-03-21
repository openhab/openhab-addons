package org.openhab.binding.fox.internal.core;

public interface FoxMessenger {

    public void open() throws FoxException;

    public void write(String text) throws FoxException;

    public String read() throws FoxException;

    public void ping() throws FoxException;

    public void test() throws FoxException;

    public void close() throws FoxException;
}
