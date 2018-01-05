package xyz.gghost.jskype.internal.impl;

import xyz.gghost.jskype.model.LocalAccount;

public class LocalAccountImpl implements LocalAccount {

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

	public void setLocation(String string) {
		location = string;
	}

	public void setPhoneNumber(String string) {
		phoneNumber = string;
	}

	public void setMood(String string) {
		mood = string;
	}

	public void setDisplayName(String string) {
		displayName = string;
	}

	public void setName(String string) {
		name = string;
	}

	public void setDOB(String string) {
		DOB = string;
	}

	public void setAvatar(String string) {
		avatar = string;
	}

	public void setSite(String string) {
		site = string;
	}

	public void setMicrosoftRank(String string) {
		microsoftRank = string;
	}

	public void setEmail(String string) {
		email = string;
	}

	public void setLanguage(String string) {
		language = string;
	}

	public void setCreationTime(String string) {
		creationTime = string;
	}

	public void setFirstLoginIP(String string) {
		firstLoginIP = string;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public String getDOB() {
		return DOB;
	}

	@Override
	public String getPhoneNumber() {
		return phoneNumber;
	}

	@Override
	public String getMood() {
		return mood;
	}

	@Override
	public String getSite() {
		return site;
	}

	@Override
	public String getAvatar() {
		return avatar;
	}

	@Override
	public String getFirstLoginIP() {
		return firstLoginIP;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public String getCreationTime() {
		return creationTime;
	}

	@Override
	public String getMicrosoftRank() {
		return microsoftRank;
	}


}
