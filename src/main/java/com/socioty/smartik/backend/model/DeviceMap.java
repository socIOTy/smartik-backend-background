package com.socioty.smartik.backend.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.MoreObjects;

public class DeviceMap {

	private List<Floor> floors;

	protected DeviceMap() {
	}

	public DeviceMap(final List<Floor> floors) {
		this.floors = new ArrayList<>(floors);
	}
	
	public List<Floor> getFloors() {
		return Collections.unmodifiableList(floors);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("floors", floors).toString();
	}
}
