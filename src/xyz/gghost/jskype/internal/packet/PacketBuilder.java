package xyz.gghost.jskype.internal.packet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import xyz.gghost.jskype.SkypeAPI;

public class PacketBuilder {
	protected SkypeAPI api;
	protected final List<Header> headers = new ArrayList<>();

	protected String url = "";
	protected String data = "";
	protected int code = 200;
	protected RequestType type;
	protected Boolean isForm = false;
	protected HttpURLConnection con;
	protected boolean sendLoginHeaders = true;
	protected boolean file = false;
	protected String cookies = "";

	public PacketBuilder(SkypeAPI api) {
		this.api = api;
	}

	public String getUrl() {
		return url;
	}

	public void addHeader(String key, String value) {
		headers.add(new Header(key, value));
	}

	public String makeRequest() {
		try {
			con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod((type == RequestType.GET ? "GET" : (type == RequestType.POST ? "POST" : (type == RequestType.PUT ? "PUT" : (type == RequestType.DELETE ? "DELETE" : "OPTIONS")))));

			con.setRequestProperty("Content-Type", isForm ? "application/x-www-form-urlencoded" : (file ? "application/octet-stream" : "application/json; charset=utf-8"));
			con.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
			con.setRequestProperty("User-Agent", "0/7.7.0.103// libhttpX.X");

			if (!cookies.equals(""))
				con.setRequestProperty("Cookie", cookies);

			con.setDoOutput(true);

			if (sendLoginHeaders) {
				addHeader("RegistrationToken", api.getClient().getAuth().getLoginToken().getReg());
				addHeader("X-Skypetoken", api.getClient().getAuth().getLoginToken().getXToken());
			}

			for (Header header : headers) {
				con.addRequestProperty(header.getType(), header.getData());
			}

			if (!(data.getBytes().length == 0)) {
				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
				wr.write(data.getBytes());
				wr.flush();
				wr.close();
			}

			code = con.getResponseCode();

			if (code == 200 || code == 201) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null)
					response.append(inputLine);

				in.close();
				return response.toString() == null ? "" : response.toString();

			} else if (code == 401) {
				api.getLogger().severe("Bad login...");
				api.getLogger().severe(this.url + " returned 401. \nHave you been running jSkype for more than 2 days?\nWithin 4 seconds the ping-er should relog you in.\n\n");

				for (Header header : headers) {
					api.getLogger().severe(header.getType() + ": " + header.getData());
				}
				return "---";
			} else if (code == 204) {
				return "";
			} else {
				if (code == 404 && url.toLowerCase().contains("endpoint")) {
					api.getLogger().severe("Lost connection to skype.\nReloggin!");

					try {
						api.login();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				// GetProfile will handle the debugging info
				if (url.equals("https://api.skype.com/users/self/contacts/profiles"))
					return null;

				// Debug info
				api.getLogger().severe("Error contacting skype\nUrl: " + url + "\nCode: " + code + "\nData: " + data + "\nType: " + type);

				for(Header header : headers) api.getLogger().severe(header.getType() + ": " + header.getData());
				return null;
			}
		} catch (Exception e) {
			System.out.println("Unable to request the skype api. URL='" + url + "'");
			e.printStackTrace();
			return null;
		}
	}

	public void setUrl(String u) {
		url = u;
	}

	public String getData() {
		return data;
	}

	public void setData(String d) {
		data = d;
	}

	public int getCode() {
		return code;
	}

	public void setType(RequestType t) {
		type = t;
	}

	public void setIsForm(boolean b) {
		isForm = b;
	}

	public HttpURLConnection getCon() {
		return con;
	}

	public void setSendLoginHeaders(boolean s) {
		sendLoginHeaders = s;
	}

	public void setFile(boolean f) {
		file = f;
	}
}
