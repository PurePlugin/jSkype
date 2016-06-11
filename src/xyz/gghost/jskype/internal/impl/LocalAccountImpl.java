package xyz.gghost.jskype.internal.impl;

import lombok.Data;
import xyz.gghost.jskype.model.LocalAccount;

@Data
public class LocalAccountImpl implements LocalAccount
{
	private String location;
	private String displayName;
	private String name;
	private String email;
	private String DOB;
	private String phoneNumber;
	private String mood;
	private String site;
	private String avatar;
	private String firstLoginIP;
	private String language;
	private String creationTime;
	private String microsoftRank;
}