package com.socioty.smartik.backend.background.simulate;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.socioty.smartik.backend.background.simulate.bulb.NestThermostatSimulator;
import com.socioty.smartik.backend.background.simulate.bulb.SmartLedDeviceSimulator;

public class DeviceSimulatorFactory {

	private static final Map<String, Class<?>> factoryClassCache = new HashMap<>();
	private static final Map<String, DeviceSimulator<?>> factoryInstanceCache = new HashMap<>();

	static {
		factoryClassCache.put("dt71c282d4fad94a69b22fa6d1e449fbbb", SmartLedDeviceSimulator.class);
		factoryClassCache.put("dt5247379d38fa4ac78e4723f8e92de681", NestThermostatSimulator.class);
	}

	public static DeviceSimulator<?> instantiate(final String deviceTypeId, final String deviceId, final Map<String,Object> initialState) {
		try {
			DeviceSimulator<?> deviceSimulator = factoryInstanceCache.get(deviceId);
			if (deviceSimulator == null) {
				final Class<?> clazz = factoryClassCache.get(deviceTypeId);
				deviceSimulator = (DeviceSimulator<?>) clazz.getConstructor(Map.class).newInstance(initialState);
				factoryInstanceCache.put(deviceId, deviceSimulator);
			}
			return deviceSimulator;
		} catch (final InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
