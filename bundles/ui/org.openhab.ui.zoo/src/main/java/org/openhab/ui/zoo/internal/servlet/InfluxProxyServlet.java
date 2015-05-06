package org.openhab.ui.zoo.internal.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.net.util.Base64;
import org.apache.http.HttpRequest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class InfluxProxyServlet extends ProxyServlet {

	private static final long serialVersionUID = 1L;
	private static final String AUTH_HEADER_KEY = "Authorization";

	private static String authHeaderValue;



	@Override
	public void init() throws ServletException {
		super.init();

		final String username = getConfigParam("influxUser");
		final String password = getConfigParam("influxPassword");
		assert(username != null && username.length() > 0);
		assert(password != null && password.length() > 0);

		final String authString = username + ":" + password;
		final String base64EncodedUsernamePassword = Base64.encodeBase64String(authString.getBytes());
		authHeaderValue = "Basic " + base64EncodedUsernamePassword;
	}



	@Override
	protected void addAdditionalHeaders(HttpServletRequest servletRequest,
			HttpRequest proxyRequest) {
		proxyRequest.addHeader(AUTH_HEADER_KEY, authHeaderValue);
	}



	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		// Add wrapper to req to hijack parameter methods for filtering out user & pw
		super.service(new ParamFilterHttpServletRequestWrapper((HttpServletRequest) req), res);
	}



	/*@Override
	protected String rewriteUrlFromRequest(HttpServletRequest servletRequest) {
		return super.rewriteUrlFromRequest(servletRequest);
	}*/

	class ParamFilterHttpServletRequestWrapper extends HttpServletRequestWrapper {

		private static final String INFLUX_PARAM_USER = "u";
		private static final String INFLUX_PARAM_PASSWORD = "p";

		public ParamFilterHttpServletRequestWrapper(HttpServletRequest request) {
			super(request);
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			Map<String, String[]> params = super.getParameterMap();
			Builder<String,String[]> newParams = ImmutableMap.<String, String[]>builder();

			for (Map.Entry<String, String[]> param : params.entrySet()) {
				if (!(param.getValue().equals(INFLUX_PARAM_PASSWORD) && param.getValue().equals(INFLUX_PARAM_USER))) {
					newParams.put(param.getKey(), param.getValue());
				}
			}
			return newParams.build();
		}


	}


}
