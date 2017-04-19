package com.socioty.smartik.backend.background.rabbit.listener;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.socioty.smartik.backend.background.ActionListener;
import com.socioty.smartik.backend.background.BackgroundInitializer.GenerateTokenPayload;

import cloud.artik.api.DevicesApi;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.client.Configuration;
import cloud.artik.client.auth.OAuth;
import cloud.artik.model.DeviceTokenEnvelope;

@Component("message.listener")
public class BackgroundMessageListener implements MessageListener {
	
	@XmlRootElement
	public static class Parameter implements Serializable {

		private static final long serialVersionUID = 3501326080889688336L;

		private String operation;
		private String deviceId;
		
		public Parameter() {
			
		}

		public String getOperation() {
			return operation;
		}
		
		public String getDeviceId() {
			return deviceId;
		}

	}

	@Autowired
	private MessageConverter messageConverter;
	
	
	@Override
	@Transactional
	public void onMessage(Message message) {
		final Parameter parameter = new Gson().fromJson((String) messageConverter.fromMessage(message), Parameter.class);
		final String deviceId = parameter.deviceId;
		
		if (parameter.getOperation().equals("CREATE")) {
			final String accessToken = generateToken();
			final ApiClient apiClient = Configuration.getDefaultApiClient();
			final OAuth artikcloud_oauth = (OAuth) apiClient.getAuthentication("artikcloud_oauth");
			artikcloud_oauth.setAccessToken(accessToken);
			final DevicesApi devicesApi = new DevicesApi(apiClient);

			try {
				System.out.println(deviceId);
				final DeviceTokenEnvelope deviceTokenEnvelope = devicesApi.updateDeviceToken(deviceId);
				final String deviceToken = deviceTokenEnvelope.getData().getAccessToken();
				final String deviceTypeId = devicesApi.getDevice(deviceId).getData().getDtid();
				new ActionListener<>(deviceId, deviceToken, deviceTypeId, null).listen();
			} catch (ApiException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		} if (parameter.getOperation().equals("DELETE")) {
			// NOTHING TO DO
			System.out.println("Ignoring deletion of deviceId " + deviceId);
		}
		
		
	}
	
	private String generateToken() {
		try {
			final CloseableHttpClient httpclient = HttpClients.createDefault();
			final HttpPost httpPost = new HttpPost("https://accounts.artik.cloud/token?grant_type=client_credentials");
			httpPost.addHeader("Authorization",
					"Basic ZjY4YzAzNzk1MWNkNDIyNDgxZGU0OTFiNGExZTE1M2Y6OWJjNTM4YjBiNjFhNGQ3MjhjMDU2MDE5NzU2ZWQ3Nzc=");
			final StringEntity input = new StringEntity("{\"grant_type\":\"client_credentials\"}");
			input.setContentType("application/json");
			httpPost.setEntity(input);
			final CloseableHttpResponse response = httpclient.execute(httpPost);
			try {
				final HttpEntity entity = response.getEntity();
				final GenerateTokenPayload payoad = new Gson().fromJson(new InputStreamReader(entity.getContent()),
						GenerateTokenPayload.class);
				EntityUtils.consume(entity);
				return payoad.access_token;
			} finally {
				response.close();
			}

		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

	}

}