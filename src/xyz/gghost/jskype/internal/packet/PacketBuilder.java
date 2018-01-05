package xyz.gghost.jskype.internal.packet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import lombok.Data;
import xyz.gghost.jskype.SkypeAPI;

@Data
public class PacketBuilder
{
	protected final SkypeAPI api;
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

	public void addHeader(String key, String value)
	{
		headers.add(new Header(key, value));
	}

	public String makeRequest()
	{
		try
		{
			con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod((type == RequestType.GET ? "GET" : (type == RequestType.POST ? "POST" : (type == RequestType.PUT ? "PUT" : (type == RequestType.DELETE ? "DELETE" : "OPTIONS")))));

			con.setRequestProperty("Content-Type", isForm ? "application/x-www-form-urlencoded" : (file ? "application/octet-stream" : "application/json; charset=utf-8"));
			con.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
			con.setRequestProperty("User-Agent", "0/7.7.0.103// libhttpX.X");

			if (!cookies.equals(""))
				con.setRequestProperty("Cookie", cookies);

			con.setDoOutput(true);

			if (sendLoginHeaders)
			{
				addHeader("RegistrationToken", api.getClient().getAuth().getLoginToken().getReg());
				addHeader("X-Skypetoken", api.getClient().getAuth().getLoginToken().getXToken());
			}

			headers.forEach(header -> con.addRequestProperty(header.getType(), header.getData()));

			if (!(data.getBytes().length == 0))
			{
				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
				wr.write(data.getBytes());
				wr.flush();
				wr.close();
			}

			code = con.getResponseCode();

			if (code == 200 || code == 201)
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null)
					response.append(inputLine);

				in.close();
				return response.toString() == null ? "" : response.toString();

			}
			else if (code == 401)
			{
				api.getLogger().severe("Bad login...");
				api.getLogger().severe(this.url + " returned 401. \nHave you been running jSkype for more than 2 days?\nWithin 4 seconds the ping-er should relog you in.\n\n");

				headers.forEach(header -> api.getLogger().severe(header.getType() + ": " + header.getData()));
				return "---";
			}
			else if (code == 204)
			{
				return "";
			}
			else
			{
				if (code == 404 && url.toLowerCase().contains("endpoint"))
				{
					api.getLogger().severe("Lost connection to skype.\nReloggin!");

					try
					{
						api.login();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				// GetProfile will handle the debugging info
				if (url.equals("https://api.skype.com/users/self/contacts/profiles"))
					return null;

				// Debug info
				api.getLogger().severe("Error contacting skype\nUrl: " + url + "\nCode: " + code + "\nData: " + data + "\nType: " + type);

				headers.forEach(header -> api.getLogger().severe(header.getType() + ": " + header.getData()));
				return null;
			}
		}
		catch (Exception e)
		{
			api.getLogger().log(Level.SEVERE, "Unable to request the skype api. URL='" + url + "'", e);
			return null;
		}
	}
}