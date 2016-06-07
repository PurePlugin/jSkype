package xyz.gghost.jskype.internal.packet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import xyz.gghost.jskype.Logger.Level;
import xyz.gghost.jskype.SkypeAPI;

public class PacketBuilderUploader extends PacketBuilder
{
	public PacketBuilderUploader(SkypeAPI api)
	{
		super(api);
	}

	@SuppressWarnings("deprecation")
	public String makeRequest(InputStream ss)
	{
		try
		{
			// read the file
			byte[] bytes;
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[16384];
			while ((nRead = ss.read(data, 0, data.length)) != -1)
			{
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			bytes = buffer.toByteArray();
			// end read of file

			URL obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod((type == RequestType.GET ? "GET" : (type == RequestType.POST ? "POST" : (type == RequestType.PUT ? "PUT" : (type == RequestType.DELETE ? "DELETE" : "OPTIONS")))));

			con.setRequestProperty("Content-Type", isForm ? "application/x-www-form-urlencoded" : (file ? "application/octet-stream" : "application/json; charset=utf-8"));
			con.setRequestProperty("Content-Length", Integer.toString(bytes.length));
			con.setRequestProperty("User-Agent", "0/7.7.0.103// libhttpX.X");
			// con.setRequestProperty("Cookie", api.cookies);
			con.setDoOutput(true);

			if (sendLoginHeaders)
				addLogin(api);

			for (Header s : headers)
			{
				con.addRequestProperty(s.getType(), s.getData());
			}

			OutputStream wr = con.getOutputStream();

			wr.write(bytes);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			if (responseCode == 200 || responseCode == 201)
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuilder response = new StringBuilder();
				while ((inputLine = in.readLine()) != null)
				{
					response.append(inputLine);
				}
				in.close();
				return response.toString();

			}
			else if (responseCode == 401)
			{
				api.getLogger().log(Level.ERROR, "\n\nBad login...");
				api.getLogger().log(Level.ERROR, this.url + " returned 401. \nHave you been running jSkype for more than 2 days?\nWithin 4 seconds the ping-er should relog you in.\n\n");
				return "---";
			}
			else if (responseCode == 204)
			{
				return "";
			}
			else
			{
				// Debug info
				System.out.println("Error contacting skype\nUrl: " + url + "\nCode: " + responseCode + "\nData: " + Arrays.toString(data));
				for (Header header : headers)
				{
					api.getLogger().log(Level.ERROR, header.getType() + ": " + header.getData());
				}
				return null;
			}
		}
		catch (Exception e)
		{
			api.getLogger().log(Level.ERROR, "================================================");
			api.getLogger().log(Level.ERROR, "========Unable to request  the skype api========");
			api.getLogger().log(Level.ERROR, "================================================");
			e.printStackTrace();
			return null;
		}
	}
}
