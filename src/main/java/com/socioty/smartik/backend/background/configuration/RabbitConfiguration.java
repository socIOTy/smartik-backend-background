package com.socioty.smartik.backend.background.configuration;

import static java.lang.System.getenv;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

@Configuration
public class RabbitConfiguration {

	@Bean
	public ConnectionFactory connectionFactory() {
		final SimpleRoutingConnectionFactory connectionFactory = new SimpleRoutingConnectionFactory();
		final Builder<Object, ConnectionFactory> mapBuilder = ImmutableMap
				.<Object, ConnectionFactory> builder();
		final ConnectionFactory consumerConnectionFactory = consumerConnectionFactory();
		mapBuilder.put("consumer.connection.factory", consumerConnectionFactory);
		mapBuilder.put("producer.connection.factory", producerConnectionFactory());
		connectionFactory.setTargetConnectionFactories(mapBuilder.build());
		connectionFactory.setDefaultTargetConnectionFactory(consumerConnectionFactory);
		return connectionFactory;
	}

	private ConnectionFactory consumerConnectionFactory() {
		final URI ampqUrl;
		try {
			ampqUrl = new URI(getEnvOrThrow("CLOUDAMQP_URL"));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		final CachingConnectionFactory factory = new CachingConnectionFactory();
		factory.setUsername(ampqUrl.getUserInfo().split(":")[0]);
		factory.setPassword(ampqUrl.getUserInfo().split(":")[1]);
		factory.setHost(ampqUrl.getHost());
		factory.setPort(ampqUrl.getPort());
		factory.setVirtualHost(ampqUrl.getPath().substring(1));

		return factory;
	}

	private ConnectionFactory producerConnectionFactory() {
		final URI ampqUrl;
		try {
			ampqUrl = new URI(getEnvOrThrow("CLOUDAMQP_URL"));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		final CachingConnectionFactory factory = new CachingConnectionFactory();
		factory.setUsername(ampqUrl.getUserInfo().split(":")[0]);
		factory.setPassword(ampqUrl.getUserInfo().split(":")[1]);
		factory.setHost(ampqUrl.getHost());
		factory.setPort(ampqUrl.getPort());
		factory.setVirtualHost(ampqUrl.getPath().substring(1));

		return factory;
	}

	@Bean
	@Autowired
	public AmqpAdmin consumerAmqpAdmin(final ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}

	@Bean
	@Autowired
	public RabbitTemplate template(final ConnectionFactory connectionFactory) {
		return new RabbitTemplate(connectionFactory);
	}

	@Bean(name = "messages.queue")
	public Queue tripsQueue() {
		return new Queue("messages");
	}

	private static String getEnvOrThrow(String name) {
		String env = System.getProperty(name);
		if (env == null) {
			env = getenv(name);
		}
		if (env == null) {
			throw new IllegalStateException("Environment variable [" + name
					+ "] is not set.");
		}
		return env;
	}

}
