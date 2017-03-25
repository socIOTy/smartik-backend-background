package com.socioty.smartik.backend.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.MoreObjects;

public class Room {

	private String name;
	private Set<String> deviceIds;

	protected Room() {
	}

	public Room(final String name, final Set<String> deviceIds) {
		this.name = name;
		this.deviceIds = new HashSet<>(deviceIds);
	}

	public String getName() {
		return name;
	}

	public Set<String> getDeviceIds() {
		return Collections.unmodifiableSet(deviceIds);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("name", name).add("deviceIds", deviceIds).toString();
	}
}
