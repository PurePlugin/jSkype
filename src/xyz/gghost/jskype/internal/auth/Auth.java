package xyz.gghost.jskype.internal.auth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.exception.BadResponseException;
import xyz.gghost.jskype.exception.BadUsernamePasswordException;
import xyz.gghost.jskype.exception.FailedToLoginException;
import xyz.gghost.jskype.exception.RecaptchaException;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;

public class Auth
{
	private String url = "";
	private PacketBuilder packet;

	private Document postData(SkypeAPI api) throws BadResponseException, UnsupportedEncodingException
	{
		Date now = new Date();

		PacketBuilder getIdsPacket = new PacketBuilder(api);
		getIdsPacket.setSendLoginHeaders(false);
		getIdsPacket.setUrl("https://login.skype.com/login?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com");
		getIdsPacket.setType(RequestType.GET);

		String htmlIds = getIdsPacket.makeRequest();

		if (htmlIds == null)
			throw new BadResponseException();

		String pie = htmlIds.split("name=\"pie\" id=\"pie\" value=\"")[1].split("\"")[0];
		String etm = htmlIds.split("name=\"etm\" id=\"etm\" value=\"")[1].split("\"")[0];

		StringBuilder data = new StringBuilder();
		data.append("username=").append(URLEncoder.encode(api.getClient().getUsername(), "UTF-8"));
		data.append("&password=").append(URLEncoder.encode(api.getClient().getPassword(), "UTF-8"));
		data.append("&timezone_field=").append(URLEncoder.encode(new SimpleDateFormat("XXX").format(now).replace(':', '|'), "UTF-8"));
		data.append("&js_time=").append(String.valueOf(now.getTime() / 1000));
		data.append("&pie=").append(URLEncoder.encode(pie, "UTF-8"));
		data.append("&etm=").append(URLEncoder.encode(etm, "UTF-8"));
		data.append("&client_id=").append(URLEncoder.encode("578134", "UTF-8"));
		data.append("&redirect_uri=").append(URLEncoder.encode("https://web.skype.com", "UTF-8"));

		String formData = data.toString();

		PacketBuilder login = new PacketBuilder(api);
		login.setSendLoginHeaders(false);
		login.setType(RequestType.POST);
		login.setIsForm(true);
		login.setData(formData);
		login.setUrl("https://login.skype.com/login?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com");

		String html = login.makeRequest();

		if (html == null)
			throw new BadResponseException();

		return Jsoup.parse(html);
	}

	public void login(SkypeAPI api) throws Exception
	{
		handle(postData(api), api);

		try
		{
			prepare(api);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void handle(Document loginResponseDocument, SkypeAPI api) throws FailedToLoginException, RecaptchaException
	{
		try
		{
			Elements inputs = loginResponseDocument.select("input[name=skypetoken]");

			if (inputs.size() > 0)
			{
				api.getClient().getLoginTokens().setXToken(inputs.get(0).attr("value"));
			}
			else if (loginResponseDocument.html().contains("var skypeHipUrl = \"https://clien"))
			{
				api.getLogger().severe("Failed to connect due to a recaptcha!");
				throw new RecaptchaException();
			}
			else
			{
				Elements elements = loginResponseDocument.select(".message_error");

				if (elements.size() > 0)
				{
					Element div = elements.get(0);

					if (div.children().size() > 1)
					{
						Element span = div.child(1);

						throw new FailedToLoginException(span.text());
					}
				}
				throw new FailedToLoginException("Could not find error message. Dumping entire page. \n" + loginResponseDocument.html());
			}
		}
		catch (FailedToLoginException e)
		{
			throw e;
		}
		catch (RecaptchaException e)
		{
			if (!api.getClient().isRelog())
				throw e;

			try
			{
				api.stop();
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
	}

	public void prepare(SkypeAPI api) throws BadUsernamePasswordException, FailedToLoginException
	{
		authLogin(api);

		if (!reg(api))
		{
			api.getLogger().severe("Failed to get update data from skype due to a login error... Attempting to relogin, however this wont work until the auto pinger kicks in.");
			authLogin(api);

			try
			{
				Thread.sleep(1750);
				prepare(api);
			}
			catch (InterruptedException ignored)
			{
			}
		}
		save(api);
	}

	public void authLogin(SkypeAPI api) throws FailedToLoginException
	{
		url = location(api).split("://")[1].split("/")[0];
		api.getClient().getLoginTokens().setReg(packet.getCon().getHeaderField("Set-RegistrationToken").split(";")[0]);
		api.getClient().getLoginTokens().setEndPoint(packet.getCon().getHeaderField("Set-RegistrationToken").split(";")[2].split("=")[1]);
	}

	public boolean save(SkypeAPI api)
	{
		String id = "{\"id\":\"messagingService\",\"type\":\"EndpointPresenceDoc\",\"selfLink\":\"uri\",\"publicInfo\":{\"capabilities\":\"video|audio\",\"type\":\"1\",\"skypeNameVersion\":\"skype.com\",\"nodeInfo\":\"2\",\"version\":\"2\"},\"privateInfo\":{\"epname\":\"Skype\"}}";
		PacketBuilder packet = new PacketBuilder(api);
		packet.setType(RequestType.PUT);
		packet.setData(id);
		packet.setUrl("https://" + url + "/v1/users/ME/endpoints/" + api.getClient().getLoginTokens().getEndPoint() + "/presenceDocs/messagingService");
		return packet.makeRequest() != null;
	}

	public boolean reg(SkypeAPI api)
	{
		PacketBuilder packet = new PacketBuilder(api);
		String id = "{\"channelType\":\"httpLongPoll\",\"template\":\"raw\",\"interestedResources\":[\"/v1/users/ME/conversations/ALL/properties\",\"/v1/users/ME/conversations/ALL/messages\",\"/v1/users/ME/contacts/ALL\",\"/v1/threads/ALL\"]}";
		packet.setData(id);
		packet.setType(RequestType.POST);
		packet.setUrl("https://" + url + "/v1/users/ME/endpoints/SELF/subscriptions");
		return packet.makeRequest() != null;
	}

	public String location(SkypeAPI api) throws FailedToLoginException
	{
		packet = new PacketBuilder(api);
		packet.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints");
		packet.setType(RequestType.POST);
		packet.setSendLoginHeaders(false);

		packet.addHeader("Authentication", "skypetoken=" + api.getClient().getLoginTokens().getXToken());
		packet.setData("{}");
		String data = packet.makeRequest();

		if (data == null)
			throw new FailedToLoginException("Bad account!");

		return packet.getCon().getHeaderField("Location");
	}
}