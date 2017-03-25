package com.socioty.smartik.backend.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.MoreObjects;

public class Floor {

	private Set<Room> rooms;

	protected Floor() {
	}

	public Floor(final Set<Room> rooms) {
		this.rooms = new HashSet<>(rooms);
	}
	
	public Set<Room> getRooms() {
		return Collections.unmodifiableSet(rooms);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("rooms", rooms).toString();
	}
}
