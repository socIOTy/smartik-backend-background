package com.socioty.smartik.backend.background.simulate.bulb;

import static com.socioty.smartik.backend.background.simulate.bulb.NestThermostatSimulator.Mode.COOL;
import static com.socioty.smartik.backend.background.simulate.bulb.NestThermostatSimulator.Mode.HEAT;
import static com.socioty.smartik.backend.background.simulate.bulb.NestThermostatSimulator.Mode.HEAT_COOL;

import java.util.Map;

import com.google.common.base.CaseFormat;
import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.socioty.smartik.backend.background.simulate.DeviceSimulator;
import com.socioty.smartik.backend.background.simulate.DeviceSimulatorAction;
import com.socioty.smartik.backend.background.simulate.bulb.NestThermostatSimulator.Action;

public class NestThermostatSimulator implements DeviceSimulator<Action> {

	public static class MessagePayload {
		private boolean can_cool;
		private boolean can_heat;
		private double target_temperature_c;
		

		public MessagePayload(final boolean can_cool, final boolean can_heat, final double target_temperature_c) {
			this.can_cool = can_cool;
			this.can_heat = can_heat;
			this.target_temperature_c = target_temperature_c;
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this).add("can_cool", can_cool).add("can_heat", can_heat)
					.add("target_temperature_c", target_temperature_c).toString();
		}
	}

	public enum Action implements DeviceSimulatorAction<NestThermostatSimulator> {
		COOL_MODE {
			@Override
			public void execute(final NestThermostatSimulator device, final Map<String, Object> parameters) {
				device.setMode(COOL);
			}
		},
		HEAT_COOL_MODE {
			@Override
			public void execute(final NestThermostatSimulator device, final Map<String, Object> parameters) {
				device.setMode(HEAT_COOL);
			}
		},
		HEAT_MODE {
			@Override
			public void execute(final NestThermostatSimulator device, final Map<String, Object> parameters) {
				device.setMode(HEAT);
			}
		},
		OFF {
			@Override
			public void execute(final NestThermostatSimulator device, final Map<String, Object> parameters) {
				device.setMode(Mode.OFF);
			}
		},
		TEMPERATURE {
			@Override
			public void execute(final NestThermostatSimulator device, final Map<String, Object> parameters) {
				final double temperature = (double) parameters.get("temp");
				device.setTemperature(temperature);
			}
		},
		TEMPERATURE_IN_FAHRENHEIT {
			@Override
			public void execute(final NestThermostatSimulator device, final Map<String, Object> parameters) {
				final double temperatureInFahrenheit = (double) parameters.get("temp");
				final double temperatureInCelsius = (temperatureInFahrenheit - 32) / 1.8d;
				device.setTemperature(temperatureInCelsius);
			}
		};

		public static Action parseAction(final String actionString) {
			if (!actionString.startsWith("set")) {
				throw new IllegalArgumentException("Invalid action");
			}

			final String temp = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, actionString.substring(3));
			return Action.valueOf(temp);
		}
	}
	
	public enum Mode {
		COOL(true, false),
		HEAT(false, true),
		HEAT_COOL(true, true),
		OFF(false, false);
		
		private final boolean canCool;
		private final boolean canHeat;
		
		private Mode(final boolean canCool, final boolean canHeat) {
			this.canCool = canCool;
			this.canHeat = canHeat;
		}
		
		public static Mode parseMode(final boolean canCool, final boolean canHeat) {
			for (final Mode mode : values()) {
				if (mode.canCool == canCool && mode.canHeat == canHeat) {
					return mode;
				}
			}
			throw new IllegalArgumentException("Unknown mode.");
		}
	}

	private static final String deviceTypeId = "dt5247379d38fa4ac78e4723f8e92de681";

	private Mode mode;
	private double temperature;

	public NestThermostatSimulator(final Map<String,Object> initialState) {
		if (initialState != null) {
			final Gson gson = new Gson();
			final MessagePayload payload = gson.fromJson(gson.toJson(initialState), MessagePayload.class);
			
			this.mode = Mode.parseMode(payload.can_cool, payload.can_heat);
			this.temperature = payload.target_temperature_c;	
		}
	}

	@Override
	public String getDeviceTypeId() {
		return deviceTypeId;
	}

	@Override
	public Action getAction(final String actionName) {
		return Action.parseAction(actionName);
	}

	public Mode getMode() {
		return mode;
	}
	
	public void setMode(final Mode mode) {
		this.mode = mode;
	}
	
	public double getTemperature() {
		return temperature;
	}
	
	public void setTemperature(final double temperature) {
		this.temperature = temperature;
	}

	public MessagePayload toMessagePayload() {
		return new MessagePayload(mode.canCool, mode.canHeat, temperature);
	}

}
