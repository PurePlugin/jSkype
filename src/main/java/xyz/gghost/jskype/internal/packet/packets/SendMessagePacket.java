package xyz.gghost.jskype.internal.packet.packets;

import org.json.JSONObject;

import lombok.AllArgsConstructor;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.message.Message;

@AllArgsConstructor
public class SendMessagePacket
{
	private final SkypeAPI api;

	public Message editMessage(Message msg, String edit)
	{
		PacketBuilder packet = new PacketBuilder(api);
		packet.setType(RequestType.POST);

		msg.setMessage(edit);

		if (edit == null || msg == null)
			return msg;

		packet.setData(new JSONObject().put("content", msg.getMessage()).put("messagetype", "RichText").put("contenttype", "text").put("skypeeditedid", msg.getId()).toString());

		packet.setUrl(msg.getUpdateUrl());
		packet.makeRequest();

		return msg;
	}

	public Message sendPing(String longId, Message msg, String ids)
	{
		String id = String.valueOf(System.currentTimeMillis());
		String url = "https://client-s.gateway.messenger.live.com/v1/users/ME/conversations/" + longId + "/messages";
		msg.setSender(api.getSimpleUser(api.getUsername()));
		msg.setUpdateUrl(url);
		msg.setTime(id);
		msg.setId(id);

		PacketBuilder packet = new PacketBuilder(api);
		packet.setType(RequestType.POST);
		String data = "{content: \"<URIObject type=\\\"Picture.1\\\" uri=\\\"https://api.asm.skype.com/v1/objects/" + ids + "\\\" url_thumbnail=\\\"https://api.asm.skype.com/v1/objects/" + ids + "/views/imgt1\\\">To view this shared photo, go to: <a href=\\\"https://api.asm.skype.com/s/i?" + ids + "\\\">https://api.asm.skype.com/s/i?" + ids + "<\\/a><OriginalName v=\\\"^005CFF2010F86CC63570CA528D9B2CCFE3BF3B54DF8A01E92E^pimgpsh_thumbnail_win_distr.jpg\\\"/><meta type=\\\"photo\\\" originalName=\\\"^005CFF2010F86CC63570CA528D9B2CCFE3BF3B54DF8A01E92E^pimgpsh_thumbnail_win_distr.jpg\\\"/><\\/URIObject>\", messagetype: \"RichText/UriObject\", contenttype: \"text\", clientmessageid: \"" + id + "\"}";
		packet.setData(data);
		packet.setUrl(url);
		packet.makeRequest();

		return msg;
	}

	public Message sendMessage(String longId, Message msg)
	{

		String id = String.valueOf(System.currentTimeMillis());
		String url = "https://client-s.gateway.messenger.live.com/v1/users/ME/conversations/" + longId + "/messages";

		msg.setSender(api.getSimpleUser(api.getUsername()));
		msg.setUpdateUrl(url);
		msg.setTime(id);
		msg.setId(id);

		PacketBuilder packet = new PacketBuilder(api);
		packet.setType(RequestType.POST);

		packet.setData(new JSONObject().put("content", msg.getMessage()).put("messagetype", "RichText").put("contenttype", "text").put("clientmessageid", id).toString());

		packet.setUrl(url);
		packet.makeRequest();

		return msg;
	}
}