package com.socioty.smartik.backend.model;

import org.springframework.data.annotation.Id;

import com.google.common.base.MoreObjects;

public class Account {

	@Id
	private String id;
	private String email;
	private DeviceMap deviceMap;

	protected Account() {
	}

	public Account(final String email, final DeviceMap deviceMap) {
		this.email = email;
		this.deviceMap = deviceMap;
	}

	public String getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public DeviceMap getDeviceMap() {
		return deviceMap;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", id).add("email", email).add("deviceMap", deviceMap)
				.toString();
	}
}
