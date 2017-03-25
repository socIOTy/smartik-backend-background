package com.socioty.smartik.backend.background.simulate;

public interface DeviceSimulator<A extends DeviceSimulatorAction<?>> {

	String getDeviceTypeId();
	
	A getAction(String actionString);
	
	Object toMessagePayload();
}
