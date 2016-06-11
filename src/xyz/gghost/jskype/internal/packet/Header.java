package xyz.gghost.jskype.internal.packet;

import lombok.Data;

@Data
public class Header
{
	private final String type;
	private final String data;
}