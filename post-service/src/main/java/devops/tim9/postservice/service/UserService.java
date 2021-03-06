package devops.tim9.postservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import devops.tim9.postservice.config.domain.UserEvent;
import devops.tim9.postservice.dto.UserDto;
import devops.tim9.postservice.model.User;
import devops.tim9.postservice.repository.UserRepository;
import devops.tim9.postservice.security.Authority;
import devops.tim9.postservice.security.Role;

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

	@KafkaListener(topics = {"user-events"})
	public void onMessage(ConsumerRecord<Integer, String> consumerRecord) {
		String value = consumerRecord.value();
		try {
			UserEvent userEvent = objectMapper.readValue(value, UserEvent.class);
			User user = userEvent.getUser();
			if (userEvent.getAction().equalsIgnoreCase("registerUser") || userEvent.getAction().equalsIgnoreCase("registerAdmin") || userEvent.getAction().equalsIgnoreCase("update")) {
				List<Authority> authorities = new ArrayList<>();
				if (userEvent.getAction().equalsIgnoreCase("registerUser")) {
					authorities.add(new Authority(Role.ROLE_USER));

				} else {
					authorities.add(new Authority(Role.ROLE_ADMIN));

				}
				user.setAuthorities(authorities);
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

	public User registerUser(UserDto userDto) throws Exception {
		if (this.usernameTaken(userDto.getUsername())) {
			throw new IllegalArgumentException("Username is already taken.");
		}
		User user = new User(userDto);
		user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_USER));
		user.setAuthorities(authorities);
		this.create(user);
		return user;
	}
	
	public User registerAdmin(UserDto userDto) throws Exception {
		if (this.usernameTaken(userDto.getUsername())) {
			throw new IllegalArgumentException("Username is already taken.");
		}
		User user = new User(userDto);
		user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user.setAuthorities(authorities);
		this.create(user);
		return user;
	}
	
	public User update(Integer id, UserDto userDto) {
		User user = this.findById(id);
		if (user != null) {
			user.setName(userDto.getName());
			user.setSurname(userDto.getSurname());
			user.setEmail(userDto.getEmail());
			user.setPhoneNumber(userDto.getPhoneNumber());
			user.setSex(userDto.getSex());
			user.setBirthDate(userDto.getBirthDate());
			user.setUsername(userDto.getUsername());
			user.setWebsite(userDto.getWebsite());
			user.setBiography(userDto.getBiography());
			user.setIsPrivate(userDto.getIsPrivate());
			user.setCanBeTagged(userDto.getCanBeTagged());
			user.setIsActive(userDto.getIsActive());
			user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
			this.create(user);
			
		}
		return user;
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
