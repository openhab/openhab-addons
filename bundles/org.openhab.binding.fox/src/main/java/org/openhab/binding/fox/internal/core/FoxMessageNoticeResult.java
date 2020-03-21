package org.openhab.binding.fox.internal.core;

import java.util.Scanner;

class FoxMessageNoticeResult extends FoxMessage {

    String result;

    public FoxMessageNoticeResult() {
        super();
        result = "";
    }

    @Override
    protected void prepareMessage() {

    }

    @Override
    protected void interpretMessage() {
        result = "";
        if (message.matches("do [R|T][0-9]+")) {
            Scanner scanner = new Scanner(message);
            scanner.next();
            result = scanner.next();
            scanner.close();
        }
    }

    String getResult() {
        return result;
    }
}
