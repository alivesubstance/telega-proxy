package com.mycompany.teleproxy;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.appengine.repackaged.com.google.common.base.Predicates.not;

@WebServlet(name = "TeleProxyServlet", value = "/*")
public class TeleProxyServlet extends HttpServlet {

	private static final String TARGET_URL = "https://api.telegram.org";
	protected Set<String> WHITE_LIST = new HashSet<>();
	protected static final Pattern REGEX = Pattern.compile("/bot(.*?)/");

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getRequestURI();

		String botKey = findBotKey(path);

		if (botKey == null) {
			resp.sendError(404, "Bot key not found in path " + path);
			return;
		}

		if (!WHITE_LIST.isEmpty() && !WHITE_LIST.contains(botKey)) {
			resp.sendError(403);
			return;
		}

		forwardCall(path, req, resp);

	}

	private void forwardCall(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String url = TARGET_URL + path;
		String qString = req.getQueryString();
		if (qString != null) {
			url = url + "?" + qString;
		}

		System.out.println(url);
		final boolean isPOST = "POST".equals(req.getMethod());

		HttpURLConnection conn = openConnection(url, isPOST);

		if (isPOST) {
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json; utf-8");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0");
			conn.setRequestProperty("Accept", "application/json");

			try (OutputStream proxyOut = conn.getOutputStream()) {
				IOUtils.copy(req.getInputStream(), proxyOut);
			}
		}
		
		resp.setStatus(conn.getResponseCode());
		
		try (InputStream proxyIn = conn.getInputStream();) {
			IOUtils.copy(proxyIn, resp.getOutputStream());
		}
	}
	
	private HttpURLConnection openConnection(String url, final boolean isPOST) throws MalformedURLException, IOException {
		URL urlObj = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
		conn.setConnectTimeout(600000);
		conn.setDoOutput(isPOST);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		return conn;
	}
	
	public static String findBotKey(String path) {
		Matcher matcher = REGEX.matcher(path);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
	
	@Override
	public void init() throws ServletException {
		String whiteList = System.getProperty("teleproxy.whitelist");
		if (whiteList == null || whiteList.isEmpty()) {
			return;
		}
		
		Optional<String> wl = Optional.of(whiteList);
		WHITE_LIST = Stream.of(wl.orElse("").split(",")).filter(not(String::isEmpty)).collect(Collectors.toSet());
	}
	
}