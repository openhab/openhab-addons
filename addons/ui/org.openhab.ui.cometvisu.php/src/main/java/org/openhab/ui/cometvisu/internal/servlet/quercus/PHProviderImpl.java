/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal.servlet.quercus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openhab.ui.cometvisu.php.PHProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.java.WorkDir;
import com.caucho.quercus.QuercusContext;
import com.caucho.quercus.QuercusEngine;
import com.caucho.quercus.QuercusErrorException;
import com.caucho.quercus.QuercusExitException;
import com.caucho.quercus.QuercusLineRuntimeException;
import com.caucho.quercus.QuercusRequestAdapter;
import com.caucho.quercus.QuercusRuntimeException;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.QuercusValueException;
import com.caucho.quercus.env.StringValue;
import com.caucho.quercus.page.QuercusPage;
import com.caucho.quercus.servlet.api.QuercusHttpServletRequest;
import com.caucho.quercus.servlet.api.QuercusHttpServletRequestImpl;
import com.caucho.quercus.servlet.api.QuercusHttpServletResponse;
import com.caucho.quercus.servlet.api.QuercusHttpServletResponseImpl;
import com.caucho.quercus.servlet.api.QuercusServletContextImpl;
import com.caucho.util.CurrentTime;
import com.caucho.util.L10N;
import com.caucho.vfs.FilePath;
import com.caucho.vfs.Path;
import com.caucho.vfs.Vfs;
import com.caucho.vfs.WriteStream;

/**
 * Provides PHP5 execution service
 *
 * @author Tobias Bräutigam - initial contribution and API
 * @author BalusC - code for static files (taken from FileServlet)
 * @author Scott Ferguson - code for php files (taken from QuercusServletImpl)
 *
 * @link
 *       http://balusc.blogspot.com/2009/02/fileservlet-supporting-resume-and.html
 * @link http://quercus.caucho.com/
 *
 */
public class PHProviderImpl implements PHProvider {

    private final Logger logger = LoggerFactory.getLogger(PHProviderImpl.class);
    private static final L10N L = new L10N(PHProviderImpl.class);

    protected QuercusEngine engine;
    protected String defaultUserDir;
    protected ServletContext _servletContext;

    @Override
    public void createQuercusEngine() {
        this.engine = new QuercusEngine();
    }

    /**
     * Set an ini value
     *
     * @param name - name of the ini parameter
     * @param value - value of the ini parameter
     */
    @Override
    public void setIni(String name, String value) {
        engine.getQuercus().setIni(name, value);
    }

    /**
     * Initialize the quercus engine
     *
     * @param path - base path
     * @param userDir - default user directory
     * @param context - servlet context
     */
    @Override
    public void init(String path, String userDir, ServletContext context) {
        checkServletAPIVersion(context);

        this.defaultUserDir = userDir;
        this._servletContext = context;

        Path pwd = new FilePath(path);
        Path webInfDir = pwd;

        logger.debug("initial pwd {}", pwd);
        engine.getQuercus().setPwd(pwd);
        engine.getQuercus().setWebInfDir(webInfDir);

        // need to set these for non-Resin containers
        if (!CurrentTime.isTest() && !engine.getQuercus().isResin()) {
            Vfs.setPwd(pwd);
            WorkDir.setLocalWorkDir(webInfDir.lookup("work"));
        }
        engine.getQuercus().init();
        engine.getQuercus().start();
    }

    /**
     * Executes a php file and sends the result back to the {@HttpServletResponse}
     *
     * @param file - the php file which should be processed
     * @param request - the http request
     * @param response - the http response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public final void phpService(File file, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Env env = null;
        WriteStream ws = null;

        QuercusHttpServletRequest req = new QuercusHttpServletRequestImpl(request);
        QuercusHttpServletResponse res = new QuercusHttpServletResponseImpl(response);

        try {
            Path path = getPath(file, req);
            logger.debug("phpService path: {}", path);

            QuercusPage page;

            try {
                page = engine.getQuercus().parse(path);
            } catch (FileNotFoundException e) {
                // php/2001
                logger.debug("{}", e.toString(), e);

                response.sendError(HttpServletResponse.SC_NOT_FOUND);

                return;
            }

            ws = openWrite(response);

            // php/2002
            // for non-Resin containers
            // for servlet filters that do post-request work after Quercus
            ws.setDisableCloseSource(true);

            // php/6006
            ws.setNewlineString("\n");

            QuercusContext quercus = engine.getQuercus();

            env = quercus.createEnv(page, ws, req, res);

            // php/815d
            env.setPwd(path.getParent());
            logger.debug("setting user dir to {}", path.getParent().getNativePath());
            System.setProperty("user.dir", path.getParent().getNativePath());
            quercus.setServletContext(new QuercusServletContextImpl(_servletContext));

            try {
                env.start();

                // php/2030, php/2032, php/2033
                // Jetty hides server classes from web-app
                // http://docs.codehaus.org/display/JETTY/Classloading
                //
                // env.setGlobalValue("request", env.wrapJava(request));
                // env.setGlobalValue("response", env.wrapJava(response));
                // env.setGlobalValue("servletContext",
                // env.wrapJava(_servletContext));

                StringValue prepend = quercus.getIniValue("auto_prepend_file").toStringValue(env);
                if (prepend.length() > 0) {
                    Path prependPath = env.lookup(prepend);

                    if (prependPath == null) {
                        env.error(L.l("auto_prepend_file '{0}' not found.", prepend));
                    } else {
                        QuercusPage prependPage = engine.getQuercus().parse(prependPath);
                        prependPage.executeTop(env);
                    }
                }

                env.executeTop();

                StringValue append = quercus.getIniValue("auto_append_file").toStringValue(env);
                if (append.length() > 0) {
                    Path appendPath = env.lookup(append);

                    if (appendPath == null) {
                        env.error(L.l("auto_append_file '{0}' not found.", append));
                    } else {
                        QuercusPage appendPage = engine.getQuercus().parse(appendPath);
                        appendPage.executeTop(env);
                    }
                }
                // return;
            } catch (QuercusExitException e) {
                throw e;
            } catch (QuercusErrorException e) {
                throw e;
            } catch (QuercusLineRuntimeException e) {
                logger.debug("{}", e.toString(), e);

                ws.println(e.getMessage());
                // return;
            } catch (QuercusValueException e) {
                logger.debug("{}", e.toString(), e);

                ws.println(e.toString());

                // return;
            } catch (StackOverflowError e) {
                RuntimeException myException = new RuntimeException(L.l("StackOverflowError at {0}", env.getLocation()),
                        e);

                throw myException;
            } catch (Throwable e) {
                if (response.isCommitted()) {
                    e.printStackTrace(ws.getPrintWriter());
                }

                ws = null;

                throw e;
            } finally {
                if (env != null) {
                    env.close();
                }

                // don't want a flush for an exception
                if (ws != null && env != null && env.getDuplex() == null) {
                    ws.close();
                }

                System.setProperty("user.dir", defaultUserDir);
            }
        } catch (com.caucho.quercus.QuercusDieException e) {
            // normal exit
            logger.trace("{}", e.getMessage(), e);
        } catch (QuercusExitException e) {
            // normal exit
            logger.trace("{}", e.getMessage(), e);
        } catch (QuercusErrorException e) {
            // error exit
            logger.error("{}", e.getMessage(), e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            handleThrowable(response, e);
        }
    }

    protected Path getPath(File file, QuercusHttpServletRequest req) {
        // php/8173
        Path pwd = engine.getQuercus().getPwd().copy();

        String servletPath = QuercusRequestAdapter.getPageServletPath(req);

        if (servletPath.startsWith("/")) {
            servletPath = servletPath.substring(1);
        }

        Path path = pwd.lookupChild(servletPath);

        // php/2010, php/2011, php/2012
        if (path.isFile()) {
            return path;
        }

        StringBuilder sb = new StringBuilder();
        if (path.exists()) {
            sb.append(servletPath);
        }

        String pathInfo = QuercusRequestAdapter.getPagePathInfo(req);
        if (pathInfo != null) {
            if (pathInfo.startsWith("/")) {
                pathInfo = pathInfo.substring(1);
            }
            if (sb.length() > 1) {
                sb.append("/");
            }
            sb.append(pathInfo);

        }

        String scriptPath = sb.toString();

        path = pwd.lookupChild(scriptPath);
        if (file != null && !path.isFile()) {
            path = path.lookupChild(file.getName());
        }

        logger.debug("ServletPath '{}', PathInfo: '{}', ScriptPath: '{}' => Path '{}'", servletPath, pathInfo,
                scriptPath, path);

        return path;
    }

    protected void handleThrowable(HttpServletResponse response, Throwable e) throws IOException, ServletException {
        throw new ServletException(e);
    }

    protected WriteStream openWrite(HttpServletResponse response) throws IOException {
        WriteStream ws;

        OutputStream out = response.getOutputStream();

        ws = Vfs.openWrite(out);

        return ws;
    }

    /**
     * Makes sure the servlet container supports Servlet API 2.4+.
     */
    protected void checkServletAPIVersion(ServletContext context) {
        int major = context.getMajorVersion();
        int minor = context.getMinorVersion();

        if (major < 2 || major == 2 && minor < 4) {
            throw new QuercusRuntimeException(L.l("Quercus requires Servlet API 2.4+."));
        }
    }

}
