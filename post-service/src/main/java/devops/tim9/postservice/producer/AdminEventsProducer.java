package devops.tim9.postservice.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import devops.tim9.postservice.config.domain.AcceptAgentEvent;
import devops.tim9.postservice.config.domain.DisableUserEvent;

@Component
public class AdminEventsProducer {
	
	@Autowired
	KafkaTemplate<Integer, String> kafkaTemplate;

	@Autowired
	ObjectMapper objectMapper;

	public void sendAcceptAgentEvent(AcceptAgentEvent acceptAgentEvent) throws JsonProcessingException {
		Integer key = acceptAgentEvent.getAcceptAgentEventId();
		String value = objectMapper.writeValueAsString(acceptAgentEvent);
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send("accept-agent-event",key, value);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
			@Override
			public void onFailure(Throwable ex) {

			}

			@Override
			public void onSuccess(SendResult<Integer, String> result) {
				handleSuccess(key, value, result);
			}
		});
	}

	private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
		System.out.println("Message Sent Successfully for the key: {} and the value is {} , partition is {}" +  key +  value + result.getRecordMetadata());

	}

	private void handleFailure(Integer key, String value, Throwable ex) {
		System.out.println("Message Sent Successfully for the key: {} and the value is {} , partition is {}" + ex.getMessage());

	}
	
	public void sendDisableUserEvent(DisableUserEvent disableUserEvent) throws JsonProcessingException {
		Integer key = disableUserEvent.getDisableUserEventId();
		String value = objectMapper.writeValueAsString(disableUserEvent);
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send("disable-user-event",key, value);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
			@Override
			public void onFailure(Throwable ex) {

			}

			@Override
			public void onSuccess(SendResult<Integer, String> result) {
				handleSuccess(key, value, result);
			}
		});
	}


}
