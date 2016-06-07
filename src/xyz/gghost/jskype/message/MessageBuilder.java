package xyz.gghost.jskype.message;

import lombok.Getter;

public class MessageBuilder
{
	private StringBuilder outHtml = new StringBuilder("");

	@Getter
	private boolean blink, italic, bold, underlined, strikethrough, code;
	@Getter
	private int size = 0;

	public MessageBuilder(String s)
	{
		outHtml = new StringBuilder(s);
	}

	public MessageBuilder()
	{
	}

	public MessageBuilder setBlinking(boolean a)
	{
		blink = a;
		return this;
	}

	public MessageBuilder setBold(boolean a)
	{
		bold = a;
		return this;
	}

	public MessageBuilder setItalic(boolean a)
	{
		italic = a;
		return this;
	}

	public MessageBuilder setCode(boolean a)
	{
		code = a;
		return this;
	}

	public MessageBuilder setStrikethrough(boolean a)
	{
		strikethrough = a;
		return this;
	}

	public MessageBuilder setUnderlined(boolean a)
	{
		underlined = a;
		return this;
	}

	public MessageBuilder setSize(int a)
	{
		size = a;
		return this;
	}

	public MessageBuilder addEmoji(String s)
	{
		outHtml.append(FormatUtils.emoji(s));
		return this;
	}

	public MessageBuilder addHtml(String s)
	{
		outHtml.append(s);
		return this;
	}

	public MessageBuilder addLink(String link)
	{
		outHtml.append(FormatUtils.link(FormatUtils.encodeRawText(link)));
		return this;
	}

	public MessageBuilder addText(String s)
	{
		outHtml.append(FormatUtils.encodeRawText(s));
		return this;
	}

	public String build()
	{
		String out = outHtml.toString();
		if (bold)
			out = FormatUtils.bold(out);
		if (italic)
			out = FormatUtils.italic(out);
		if (blink)
			out = FormatUtils.blink(out);
		if (underlined)
			out = FormatUtils.underline(out);
		if (strikethrough)
			out = FormatUtils.strikethrough(out);
		if (size > 0)
			out = FormatUtils.size(out, size);
		if (code)
			out = FormatUtils.code(out);
		return out;
	}
}