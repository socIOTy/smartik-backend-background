package com.socioty.smartik.backend.background;

import static java.lang.String.format;

import java.util.Map;
import java.util.Set;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.socioty.smartik.backend.background.simulate.DeviceSimulator;
import com.socioty.smartik.backend.background.simulate.DeviceSimulatorAction;
import com.socioty.smartik.backend.background.simulate.DeviceSimulatorFactory;

public class ActionListener<D extends DeviceSimulator<A>, A extends DeviceSimulatorAction<D>> {

	public static class Action {
		private String name;
		private Map<String, Object> parameters;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Map<String, Object> getParameters() {
			return parameters;
		}

		public void setParameters(Map<String, Object> parameters) {
			this.parameters = parameters;
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this).add("name", name).add("parameters", parameters).toString();
		}
	}

	public static class ActionPayload {
		private Set<Action> actions;

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this).add("actions", actions).toString();
		}
	}

	private static final String broker = "ssl://api.artik.cloud:8883";
	private static final String actionTopicPattern = "/v1.1/actions/%s";
	private static final String messageTopicPattern = "/v1.1/messages/%s";

	private final String deviceId;
	private final String deviceToken;
	@SuppressWarnings("unused")
	private final String deviceTypeId;
	private final String actionTopic;
	private final String messageTopic;
	private final D device;

	@SuppressWarnings("unchecked")
	public ActionListener(final String deviceId, final String deviceToken, final String deviceTypeId,
			final Map<String, Object> initialState) {
		this.deviceId = deviceId;
		this.deviceToken = deviceToken;
		this.deviceTypeId = deviceTypeId;
		this.actionTopic = format(actionTopicPattern, deviceId);
		this.messageTopic = format(messageTopicPattern, deviceId);
		this.device = (D) DeviceSimulatorFactory.instantiate(deviceTypeId, deviceId, initialState);
	}

	public void listen() {
		try {
			final MqttClient sampleClient = new MqttClient(broker, deviceToken);
			final MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			connOpts.setConnectionTimeout(0);
			connOpts.setAutomaticReconnect(true);
			connOpts.setUserName(deviceId);
			connOpts.setPassword(deviceToken.toCharArray());
			System.out.println("Connecting to broker: " + broker);
			sampleClient.connect(connOpts);
			System.out.println("Connected");
			sampleClient.subscribe(actionTopic, new IMqttMessageListener() {
				@Override
				public void messageArrived(final String topic, final MqttMessage message) throws Exception {
					final ActionPayload payload = parsePayload(message);
					for (final Action action : payload.actions) {
						device.getAction(action.name).execute(device, action.parameters);
					}

					// publish message with device current state
					final MqttMessage messageToSend = parseMessage();
					sampleClient.publish(messageTopic, messageToSend);
				}
			});
		} catch (MqttException me) {
			System.out.println("reason " + me.getReasonCode());
			System.out.println("msg " + me.getMessage());
			System.out.println("loc " + me.getLocalizedMessage());
			System.out.println("cause " + me.getCause());
			System.out.println("excep " + me);
			me.printStackTrace();
		}
	}

	private ActionPayload parsePayload(final MqttMessage message) {
		final Gson gson = new Gson();
		final ActionPayload payload = gson.fromJson(message.toString(), ActionPayload.class);
		System.out.println("Payload: " + payload);
		return payload;
	}

	private MqttMessage parseMessage() {
		final Gson gson = new Gson();
		final String messagePayloadStr = gson.toJson(device.toMessagePayload());
		return new MqttMessage(messagePayloadStr.getBytes());
	}
}
