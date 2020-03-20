package org.openhab.binding.fox.internal.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.openhab.binding.fox.internal.core.FoxException;
import org.openhab.binding.fox.internal.core.FoxMessenger;

public class FoxMessengerTcpIp implements FoxMessenger {

    String host = "";
    int port = 0;
    int timeout = 250;
    String password = null;
    PrintStream printStream = null;

    Socket socket;
    PrintWriter toServer;
    BufferedReader fromServer;

    public FoxMessengerTcpIp() {
        socket = null;
        toServer = null;
        fromServer = null;
    }

    public void setHost(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setHost(String host) {
        int colon = host.indexOf(":");
        if (colon < 1) {
            setHost(host, 23);
        } else {
            setHost(host.substring(0, colon), Integer.parseInt(host.substring(colon + 1)));
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setPassword(String password) {
        this.password = (password != null && password.length() > 0) ? password : null;
    }

    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }

    @Override
    public void open() throws FoxException {
        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(timeout);
            toServer = new PrintWriter(socket.getOutputStream(), true);
            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            boolean promptOk = false;
            StringBuilder sb = new StringBuilder();
            for (;;) {
                int value = fromServer.read();
                if (value < 0) {
                    break;
                }

                sb.append((char) value);
                String prompt = sb.toString();

                if (prompt.equals("pass: ")) {
                    if (password != null) {
                        toServer.println(password);
                        sb = new StringBuilder();
                    } else {
                        throw new FoxException("Password demand but no password set");
                    }
                } else if (prompt.equals("> Fox terminal")) {
                    promptOk = true;
                    fromServer.readLine();
                    break;
                } else if (prompt.equals("ACCESS DENIED")) {
                    throw new FoxException("Password rejected, access denied");
                }
            }

            if (!promptOk) {
                throw new FoxException("Wrong prompt received");
            }
        } catch (IOException e) {
            throw new FoxException(e.getMessage());
        }
    }

    private void printData(boolean toSystem, String text) {
        if (printStream != null && text.startsWith("@")) {
            printStream.println(String.format("%s Fox %s App %s",
                    (new SimpleDateFormat("HH:mm:ss.SSS")).format(Calendar.getInstance().getTime()),
                    toSystem ? "<--" : "-->", text.trim()));
        }
    }

    @Override
    public void write(String text) throws FoxException {
        write(text, null);
    }

    public void write(String text, String echo) throws FoxException {
        if (toServer == null) {
            throw new FoxException("Closed messenger");
        }
        toServer.println(text);
        printData(true, text);
        if (echo != null) {
            readEcho(echo);
        }
    }

    private String purify(String line) {
        String l = line != null ? line : "";
        while (l.length() > 0) {
            char start = l.charAt(0);
            if (start < 0x20 || start > 0x7f) {
                if (l.length() > 1) {
                    l = l.substring(1);
                } else {
                    l = "";
                }
            } else {
                return l;
            }
        }
        return l;
    }

    private String readLine() throws FoxException {
        try {
            if (fromServer == null) {
                throw new FoxException("Closed messenger");
            }
            return purify(fromServer.readLine());
        } catch (IOException e) {
            throw new FoxException("Reading error: " + e.getMessage());
        }
    }

    private void readEcho(String text) throws FoxException {
        String echo = readLine();
        if (!echo.equals(text.trim())) {
            throw new FoxException("Wrong echo received");
        }
    }

    @Override
    public String read() throws FoxException {
        String text = readLine();
        printData(false, text);
        return text;
    }

    @Override
    public void close() {
        try {
            if (toServer != null) {
                toServer.close();
                toServer = null;
            }
            if (fromServer != null) {
                fromServer.close();
                fromServer = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ping() throws FoxException {
        write("hello", null);
    }

    @Override
    public void test() throws FoxException {
        write("hello", "> Fox hello");
    }

}
