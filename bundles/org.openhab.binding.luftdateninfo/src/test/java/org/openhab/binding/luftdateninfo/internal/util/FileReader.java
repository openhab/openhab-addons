package org.openhab.binding.luftdateninfo.internal.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileReader {

    public static String readFileInString(String filename) {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "CP1252"));) {

            StringBuffer buf = new StringBuffer();
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                buf.append(sCurrentLine);
            }
            return buf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
