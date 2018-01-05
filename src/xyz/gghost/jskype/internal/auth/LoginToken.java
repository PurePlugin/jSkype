package xyz.gghost.jskype.internal.auth;

public class LoginToken {
	private String XToken;
	private String reg;
	private String endPoint;

	public void setEndPoint(String split) {
		endPoint = split;
	}

	public void setReg(String split) {
		reg = split;
	}

	public void setXToken(String attr) {
		XToken = attr;
	}

	public String getEndPoint() {
		return endPoint;
	}

	public String getXToken() {
		return XToken;
	}

	public String getReg() {
		return reg;
	}
}
