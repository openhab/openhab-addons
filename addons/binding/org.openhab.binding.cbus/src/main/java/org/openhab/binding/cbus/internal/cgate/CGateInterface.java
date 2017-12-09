/*
 * CGateInterface - A library to allow interaction with Clipsal C-Gate.
 *
 * Copyright 2008, 2009, 2012, 2017 Dave Oxley <dave@daveoxley.co.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.cbus.internal.cgate;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.common.ThreadPoolManager;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public final class CGateInterface extends CGateObject {

    private final static ExecutorService threadPool = ThreadPoolManager.getPool("CGateInterface-Helper");

    private CGateInterface() {
        super(null);
    }

    @Override
    protected String getKey() {
        return "";
    }

    @Override
    public CGateObject getCGateObject(String address) throws CGateException {
        throw new UnsupportedOperationException();
    }

    @Override
    String getProjectAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    String getResponseAddress(boolean id) {
        throw new UnsupportedOperationException();
    }

    /**
     * Connect to a C-Gate server using the supplied cgate_server and cgate_port.
     *
     * @param cgate_server The <code>InetAddress</code> of the C-Gate server
     * @param command_port The command port for the C-Gate server
     * @param event_port The event port for the C-Gate server
     * @param status_change_port The status change port for the C-Gate server
     * @return CGateSession The C-Gate session
     */
    public static CGateSession connect(InetAddress cgate_server, int command_port, int event_port,
            int status_change_port) {
        return new CGateSession(cgate_server, command_port, event_port, status_change_port);
    }

    /**
     * Issue a <code>noop</code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.76</i></a>
     * @param cgate_session
     * @throws com.daveoxley.cbus.CGateException
     */
    public static void noop2(CGateSession cgate_session) throws CGateException {
        ArrayList<String> resp_array = cgate_session.sendCommand("noop").toArray();
        if (resp_array.isEmpty()) {
            throw new CGateException();
        }

        String resp_str = resp_array.get(resp_array.size() - 1);
        String result_code = resp_str.substring(0, 3).trim();
        if (!result_code.equals("200")) {
            throw new CGateException(resp_str);
        }
    }

    public static boolean noop(CGateSession cGateSession) {
        try {
            CountDownLatch doneSignal = new CountDownLatch(1);
            NoopCheck thread = new NoopCheck(doneSignal, cGateSession);
            threadPool.execute(thread);
            if (!doneSignal.await(3, TimeUnit.SECONDS)) {
                thread.interrupt();
                cGateSession.close();
                return false;
            }
            return true;
        } catch (InterruptedException | CGateException e) {
            return false;
        }
    }

    private static class NoopCheck extends Thread {
        private final CountDownLatch doneSignal;
        private final CGateSession cGateSession;

        protected NoopCheck(CountDownLatch doneSignal, CGateSession cGateSession) {
            this.doneSignal = doneSignal;
            this.cGateSession = cGateSession;
        }

        @Override
        public void run() {
            try {
                ArrayList<String> resp_array = cGateSession.sendCommand("noop").toArray();
                if (resp_array.isEmpty()) {
                    throw new CGateException();
                }

                String resp_str = resp_array.get(resp_array.size() - 1);
                String result_code = resp_str.substring(0, 3).trim();
                if (!result_code.equals("200")) {
                    throw new CGateException(resp_str);
                }
                doneSignal.countDown();
            } catch (Exception e) {
            }
        }
    }
}
