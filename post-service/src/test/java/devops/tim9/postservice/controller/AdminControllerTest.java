package devops.tim9.postservice.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import devops.tim9.postservice.config.JwtAuthenticationRequest;
import devops.tim9.postservice.config.WebSecurityConfig;
import devops.tim9.postservice.dto.MessageDto;
import devops.tim9.postservice.dto.UserDto;
import devops.tim9.postservice.model.Post;
import devops.tim9.postservice.model.User;
import devops.tim9.postservice.repository.PostRepository;
import devops.tim9.postservice.repository.UserRepository;
import devops.tim9.postservice.security.Authority;
import devops.tim9.postservice.security.Role;
import devops.tim9.postservice.security.UserTokenState;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
@EmbeddedKafka(partitions = 1, controlledShutdown = false, brokerProperties = { "listeners=PLAINTEXT://localhost:9093",
		"port=9093" })
public class AdminControllerTest {
	
	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private WebSecurityConfig webSecurityConfig;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private PostRepository postRepository;
	
	@Test
	public void reported_test_happy() throws Exception {
		User user1 = new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny",
				"johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		User saved = userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny", "123", null));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		Post post = postRepository.save(new Post(null, "desc", "aaa", new Date(), saved, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		List<User> usersReport = new ArrayList<>();
		usersReport.add(user1);
		post.setReportedBy(usersReport);
		ResponseEntity<Object> responseEntity = testRestTemplate.exchange("/admin/reported", HttpMethod.GET, httpEntity,
				Object.class);
		@SuppressWarnings("unchecked")
		List<Post> posts = (List<Post>) responseEntity.getBody();
		assertEquals(1, posts.size());
	}
	
	@Test
	public void deleteReportedPost_test_happy() throws Exception {
		User user1 = new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny2",
				"johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		User saved = userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny2", "123", null));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		Post post = postRepository.save(new Post(null, "desc", "aaa", new Date(), saved, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		List<User> usersReport = new ArrayList<>();
		usersReport.add(user1);
		post.setReportedBy(usersReport);
		ResponseEntity<MessageDto> responseEntity = testRestTemplate.exchange("/admin/"+post.getId(), HttpMethod.DELETE, httpEntity,
				MessageDto.class);
		assertEquals("Success", responseEntity.getBody().getHeader());
		assertEquals("Post is successfuly deleted.",responseEntity.getBody().getMessage());
	}
	
	@Test
	public void agentRequests_test_happy() throws Exception {
		User user = new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny90",
				"johnny.web", "biography", false, true, true, "123"));
		user.setIsAccepted(false);
		userRepository.save(user);
		User user1 = new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny3",
				"johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		User saved = userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny3", "123", null));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<Object> responseEntity = testRestTemplate.exchange("/admin/agent-requests", HttpMethod.GET, httpEntity,
				Object.class);
		@SuppressWarnings("unchecked")
		List<User> users = (List<User>) responseEntity.getBody();
		assertEquals(1,users.size());
	}
	
	@Test
	public void acceptAgent_test_happy() throws Exception {
		User user = new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny91",
				"johnny.web", "biography", false, true, true, "123"));
		user.setIsAccepted(false);
		userRepository.save(user);
		User user1 = new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny4",
				"johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		User saved = userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny4", "123", null));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<MessageDto> responseEntity = testRestTemplate.exchange("/admin/accept-agent/"+user.getUsername(), HttpMethod.POST, httpEntity,
				MessageDto.class);
		User accepted = userRepository.findByUsername("johnny91");
		assertTrue(accepted.getIsAccepted());
		assertEquals("Success", responseEntity.getBody().getHeader());
		assertEquals("Account is successfuly registered.",responseEntity.getBody().getMessage());
		
	}


}
