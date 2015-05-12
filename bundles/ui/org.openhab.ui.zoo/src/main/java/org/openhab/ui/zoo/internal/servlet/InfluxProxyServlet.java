package org.openhab.ui.zoo.internal.servlet;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Will proxy requests to http://127.0.0.1:8083/ by default. Set influxHostname
 * and influxPort in servlet initParams to config.
 *
 * @author sja
 *
 */
public class InfluxProxyServlet extends ProxyServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(InfluxProxyServlet.class);
	private static final String PARAM_USER = "u";
	private static final String PARAM_PASSWORD = "p";

	private String paramString = "";

	public InfluxProxyServlet(final String username, final String password) {
		super();
		if (username != null) {
			paramString = PARAM_USER + "=" + username;
			if (password != null) {
				paramString += "&" + PARAM_PASSWORD + "=" + password;
			}
		}

		if (logger.isTraceEnabled()) {
			logger.trace("Set Influx auth params to {}", paramString);
		} else {
			logger.info("Set Influx auth params to {}", paramString.replaceAll(PARAM_PASSWORD + "=" + password, PARAM_PASSWORD + "=***"));
		}
	}

	@Override
	protected String rewriteUrlFromRequest(HttpServletRequest request) {
		String rewrittenUrl = super.rewriteUrlFromRequest(request);
		if (!(rewrittenUrl.endsWith("&") || rewrittenUrl.endsWith("?"))) {
			if (rewrittenUrl.contains("?")) {
				rewrittenUrl += "&";
			} else {
				rewrittenUrl += "?";
			}
		}
		return rewrittenUrl + paramString;
	}

}
