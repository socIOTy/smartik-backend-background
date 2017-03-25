package com.socioty.smartik.backend.background.simulate.bulb;

import java.awt.Color;
import java.util.Map;

import com.google.common.base.CaseFormat;
import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.socioty.smartik.backend.background.simulate.DeviceSimulator;
import com.socioty.smartik.backend.background.simulate.DeviceSimulatorAction;
import com.socioty.smartik.backend.background.simulate.bulb.SmartLedDeviceSimulator.Action;

public class SmartLedDeviceSimulator implements DeviceSimulator<Action> {

	public static class ColorPayload {
		private int r;
		private int g;
		private int b;

		public ColorPayload(final Color color) {
			this(color.getRed(), color.getGreen(), color.getBlue());
		}

		public ColorPayload(final int r, final int g, final int b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}
		
		public Color toColor() {
			return new Color(r, g, b);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this).add("r", r).add("g", g).add("b", b).toString();
		}
	}

	public static class MessagePayload {
		private ColorPayload colorRGB;
		private int intensity;
		private boolean state;

		public MessagePayload(final ColorPayload colorRGB, final int intensity, final boolean state) {
			this.colorRGB = colorRGB;
			this.intensity = intensity;
			this.state = state;
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this).add("colorRGB", colorRGB).add("intensity", intensity)
					.add("state", state).toString();
		}
	}

	public enum Action implements DeviceSimulatorAction<SmartLedDeviceSimulator> {
		ON {
			@Override
			public void execute(final SmartLedDeviceSimulator device, final Map<String, Object> parameters) {
				device.setState(true);
			}
		},
		OFF {
			@Override
			public void execute(final SmartLedDeviceSimulator device, final Map<String, Object> parameters) {
				device.setState(false);
			}
		},
		COLOR_AS_RGB {
			@Override
			public void execute(final SmartLedDeviceSimulator device, final Map<String, Object> parameters) {
				@SuppressWarnings("unchecked")
				final Map<String, Object> colorRGB = (Map<String, Object>) parameters.get("colorRGB");
				final int red = (int) ((double) colorRGB.get("r"));
				final int green = (int) ((double) colorRGB.get("g"));
				final int blue = (int) ((double) colorRGB.get("b"));
				device.setColor(new Color(red, green, blue));
			}
		},
		INTENSITY {
			@Override
			public void execute(final SmartLedDeviceSimulator device, final Map<String, Object> parameters) {
				final int intensity = (int) ((double) parameters.get("intensity"));
				device.setIntensity(intensity);
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

	private static final String deviceTypeId = "dt71c282d4fad94a69b22fa6d1e449fbbb";

	private boolean state;
	private int intensity;
	private Color color;

	public SmartLedDeviceSimulator(final Map<String,Object> initialState) {
		final Gson gson = new Gson();
		final MessagePayload payload = gson.fromJson(gson.toJson(initialState), MessagePayload.class);
		this.state = payload.state;
		this.intensity = payload.intensity;
		this.color = payload.colorRGB.toColor();
	}

	public SmartLedDeviceSimulator(final boolean state, final int intensity, final Color color) {
		this.state = state;
		this.intensity = intensity;
		this.color = color;
	}

	@Override
	public String getDeviceTypeId() {
		return deviceTypeId;
	}

	@Override
	public Action getAction(String actionName) {
		return Action.parseAction(actionName);
	}

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public int getIntensity() {
		return intensity;
	}

	public void setIntensity(int intensity) {
		this.intensity = intensity;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public MessagePayload toMessagePayload() {
		return new MessagePayload(new ColorPayload(getColor()), getIntensity(), isState());
	}

}
