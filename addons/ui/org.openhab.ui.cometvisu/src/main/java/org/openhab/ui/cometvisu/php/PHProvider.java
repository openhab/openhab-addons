package org.openhab.ui.cometvisu.php;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PHProvider {
    public void createQuercusEngine();

    public void setIni(String key, String value);

    public void init(String absolutePath, String defaultUserDir, ServletContext _servletContext);

    public void phpService(File file, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;
}
