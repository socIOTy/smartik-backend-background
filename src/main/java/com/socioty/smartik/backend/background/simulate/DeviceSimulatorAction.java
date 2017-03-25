package com.socioty.smartik.backend.background.simulate;

import java.util.Map;

public interface DeviceSimulatorAction<D extends DeviceSimulator<?>> {

	void execute(D device, Map<String,Object> parameters);
	
}
