package devops.tim9.postservice.service;

import java.util.List;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import devops.tim9.postservice.config.domain.UserEvent;
import devops.tim9.postservice.model.User;
import devops.tim9.postservice.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ObjectMapper objectMapper;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = { "user-events" })
	public void onMessage(ConsumerRecord<Integer, String> consumerRecord) {
		String value = consumerRecord.value();
		try {
			UserEvent userEvent = objectMapper.readValue(value, UserEvent.class);
			User user = userEvent.getUser();
			if (userEvent.getAction().equalsIgnoreCase("registerUser")
					|| userEvent.getAction().equalsIgnoreCase("registerAdmin")
					|| userEvent.getAction().equalsIgnoreCase("update")) {
				this.create(user);
			} else if (userEvent.getAction().equalsIgnoreCase("delete")) {
				this.delete(user.getId());
			}

		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public User findById(Integer id) {
		return userRepository.findById(id).orElse(null);
	}

	public boolean usernameTaken(String username) {
		User user = userRepository.findByUsername(username);
		return user != null;
	}

	public List<User> getAll() {
		return userRepository.findAll();
	}

	public User create(User user) {
		return userRepository.save(user);
	}

	public User delete(Integer id) {
		Optional<User> user = userRepository.findById(id);
		userRepository.delete(user.get());
		return user.get();
	}

	public User findUserByToken(String token) {
		return userRepository.findByToken(token);
	}

	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username);
	}

}
