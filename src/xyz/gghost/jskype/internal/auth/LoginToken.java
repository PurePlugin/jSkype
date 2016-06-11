package xyz.gghost.jskype.internal.auth;

import lombok.Data;

@Data
public class LoginToken
{
	private String XToken;
	private String reg;
	private String endPoint;
}