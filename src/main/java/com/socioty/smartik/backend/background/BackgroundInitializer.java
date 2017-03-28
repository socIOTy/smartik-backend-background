package com.socioty.smartik.backend.background;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.socioty.smartik.backend.model.Account;
import com.socioty.smartik.backend.model.DeviceMap;
import com.socioty.smartik.backend.model.Floor;
import com.socioty.smartik.backend.model.Room;
import com.socioty.smartik.backend.repositories.AccountRepository;

import cloud.artik.api.DevicesApi;
import cloud.artik.api.MessagesApi;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.client.Configuration;
import cloud.artik.client.auth.OAuth;
import cloud.artik.model.DeviceTokenEnvelope;
import cloud.artik.model.NormalizedMessage;
import cloud.artik.model.NormalizedMessagesEnvelope;

@EnableAutoConfiguration
@EnableMongoRepositories(basePackages = { "com.socioty.smartik.backend.repositories" })
public class BackgroundInitializer implements CommandLineRunner {

	public static class GenerateTokenPayload {
		private String access_token;
		@SuppressWarnings("unused")
		private String token_type;
		@SuppressWarnings("unused")
		private long expires_in;
	}

	@Autowired
	private AccountRepository repository;

	public static void main(final String[] args) throws Exception {
		SpringApplication.run(BackgroundInitializer.class, args);
	}

	@Override
	public void run(String... arg0) throws Exception {
//		createTestAccounts();
		final Iterable<Account> accounts = repository.findAll();
		setUpDeviceSimulators(accounts);
	}

	private void setUpDeviceSimulators(final Iterable<Account> accounts) {
		final String accessToken = generateToken();
		final ApiClient apiClient = Configuration.getDefaultApiClient();
		final OAuth artikcloud_oauth = (OAuth) apiClient.getAuthentication("artikcloud_oauth");
		artikcloud_oauth.setAccessToken(accessToken);
		final DevicesApi devicesApi = new DevicesApi(apiClient);
		final MessagesApi messagesApi = new MessagesApi(apiClient);

		for (final Account account : accounts) {
			for (final Floor floor : account.getDeviceMap().getFloors()) {
				for (final Room room : floor.getRooms()) {
					for (final String deviceId : room.getDeviceIds()) {
						try {
							final DeviceTokenEnvelope deviceTokenEnvelope = devicesApi.updateDeviceToken(deviceId);
							final String deviceToken = deviceTokenEnvelope.getData().getAccessToken();
							final String deviceTypeId = devicesApi.getDevice(deviceId).getData().getDtid();
							final NormalizedMessagesEnvelope messagesEnvelope = messagesApi.getNormalizedMessages(null,
									deviceId, null, null, null, null, 1, 100l, System.currentTimeMillis(), "desc");
							final List<NormalizedMessage> messages = messagesEnvelope.getData();
							final Map<String, Object> initialState = !messages.isEmpty()
									? Iterables.getOnlyElement(messages).getData() : null;
							new ActionListener<>(deviceId, deviceToken, deviceTypeId, initialState).listen();
						} catch (ApiException e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}

					}
				}
			}
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

	
//	private void createTestAccounts() {
//		repository.deleteAll();
//		repository.save(new Account("willian.campos@gmail.com",
//				new DeviceMap(Lists.newArrayList(
//						new Floor(Sets.newHashSet(
//								new Room("Living room", Sets.newHashSet("aa251f8f408a4b09ace604553cfc9f2d", "c2e31b65ac6d4160bd9db7482765b5dc")))),
//						new Floor(Sets.newHashSet(new Room("Bedroom", Sets.newHashSet())))))));
//	}
}
