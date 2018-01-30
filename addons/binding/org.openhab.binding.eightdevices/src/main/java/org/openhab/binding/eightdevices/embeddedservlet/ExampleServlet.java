package org.openhab.binding.eightdevices.embeddedservlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.eightdevices.handler.EightDevicesHandler;

public class ExampleServlet extends HttpServlet {
    public List<String> RecievedAsyncResponsesID = new ArrayList<String>();
    List<String> RecievedAsyncResponsesPayload = new ArrayList<String>();
    List<String> DecodedPayload = new ArrayList<String>();
    static List<EightDevicesHandler> asyncHandlers = new ArrayList<EightDevicesHandler>();

    public ExampleServlet() {
        super();
        /*
         * try {
         * throw new RuntimeException("Init");
         * } catch (Exception e) {
         * e.printStackTrace();
         * }
         */
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpStatus.OK_200);
        resp.getWriter().println(req);
        System.out.println(req);
        System.out.println("GET method worked");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] splitArr;
        String id = "";
        String payload = "";
        resp.setStatus(HttpStatus.OK_200);
        BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
        String data = br.readLine();
        splitArr = data.split("\"");
        for (int i = 0; i < splitArr.length; i++) {
            if (splitArr[i].equals("id")) {
                id = splitArr[i + 2];
            }
            if (splitArr[i].equals("payload")) {
                payload = splitArr[i + 2];
                for (EightDevicesHandler h : asyncHandlers) {
                    h.handleAsync(id, payload);
                }
            }
        }
        System.out.println(this);
        System.out.println(asyncHandlers.size());
    }

    public void addAsyncListener(EightDevicesHandler h) {
        asyncHandlers.add(h);
        System.out.println("Listener registration");
        System.out.println(this);
        System.out.println(asyncHandlers.size());
    }
}