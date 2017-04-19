package com.socioty.smartik.backend.background.configuration;


import org.apache.commons.logging.Log;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.ConditionalExceptionLogger;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackgroundRabbitConfiguration {

	private static class ByPassConditionalExceptionLogger implements
			ConditionalExceptionLogger {

		private final SimpleMessageListenerContainer listenerContainer;

		public ByPassConditionalExceptionLogger(
				final SimpleMessageListenerContainer listenerContainer) {
			this.listenerContainer = listenerContainer;
		}

		@Override
		public void log(Log logger, String message, Throwable t) {
			listenerContainer.stop();
		}
	}

	@Bean
	MessageConverter messageConverter() {
		return new SimpleMessageConverter();
	}

	@Bean(name = "queue.listener")
	@Autowired
	SimpleMessageListenerContainer tripQueueListener(
			final ConnectionFactory connectionFactory,
			@Qualifier("message.listener") final MessageListener listener) {
		SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer();
		listenerContainer.setConnectionFactory(connectionFactory);
		listenerContainer.setExclusive(true);
		listenerContainer.setQueueNames("messages");
		listenerContainer.setMessageListener(listener);
		listenerContainer.setExclusiveConsumerExceptionLogger(new ByPassConditionalExceptionLogger(listenerContainer));
		return listenerContainer;
	}
}
