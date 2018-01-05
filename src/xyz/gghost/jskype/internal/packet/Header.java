package xyz.gghost.jskype.internal.packet;

public class Header {
	private final String type;
	private final String data;

	public Header(String type, String data) {
		this.type = type;
		this.data = data;
	}
	
	public String getType() {
		return type;
	}

	public String getData() {
		return data;
	}
}
