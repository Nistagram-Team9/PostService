package devops.tim9.postservice.controller;

import static org.junit.Assert.assertEquals;

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
import org.springframework.web.util.UriComponentsBuilder;

import devops.tim9.postservice.config.WebSecurityConfig;
import devops.tim9.postservice.repository.PostRepository;
import devops.tim9.postservice.repository.UserRepository;
import devops.tim9.postservice.config.JwtAuthenticationRequest;
import devops.tim9.postservice.dto.MessageDto;
import devops.tim9.postservice.dto.UserDto;
import devops.tim9.postservice.model.Post;
import devops.tim9.postservice.model.User;
import devops.tim9.postservice.security.Authority;
import devops.tim9.postservice.security.Role;
import devops.tim9.postservice.security.UserTokenState;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
@EmbeddedKafka(partitions = 1, controlledShutdown = false, brokerProperties = { "listeners=PLAINTEXT://localhost:9093",
		"port=9093" })
public class PostControllerTest {
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
	public void likePost_test_happy() throws Exception {
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
		testRestTemplate.exchange("/posts/like/" + post.getId(), HttpMethod.POST, httpEntity, MessageDto.class);
		User found = userRepository.findByUsername("johnny");
		assertEquals(1, found.getLikedPosts().size());

	}

	@Test
	public void likePost_test_sad() throws Exception {
		User user1 = new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny1",
				"johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		User saved = userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny1", "123", null));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		Post post = postRepository.save(new Post(null, "desc", "aaa", new Date(), saved, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		testRestTemplate.exchange("/posts/like/90", HttpMethod.POST, httpEntity, MessageDto.class);
		User found = userRepository.findByUsername("johnny1");
		assertEquals(0, found.getLikedPosts().size());

	}

	@Test
	public void dislikePost_test_happy() throws Exception {
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
		Post post = postRepository.save(new Post(null, "desc", "aaa", new Date(), saved, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		testRestTemplate.exchange("/posts/dislike/" + post.getId(), HttpMethod.POST, httpEntity, MessageDto.class);
		User found = userRepository.findByUsername("johnny3");
		assertEquals(1, found.getDislikedPosts().size());

	}

	@Test
	public void dislikePost_test_sad() throws Exception {
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
		postRepository.save(new Post(null, "desc", "aaa", new Date(), saved, new ArrayList<>(), new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		testRestTemplate.exchange("/posts/dislike/90", HttpMethod.POST, httpEntity, MessageDto.class);
		User found = userRepository.findByUsername("johnny4");
		assertEquals(0, found.getDislikedPosts().size());

	}

	@Test
	public void reportPost_test_happy() throws Exception {
		User user1 = new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny5",
				"johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		User saved = userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny5", "123", null));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		Post post = postRepository.save(new Post(null, "desc", "aaa", new Date(), saved, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		testRestTemplate.exchange("/posts/report/" + post.getId(), HttpMethod.POST, httpEntity, MessageDto.class);
		User found = userRepository.findByUsername("johnny5");
		assertEquals(1, found.getReportedPosts().size());

	}

	@Test
	public void reportPost_test_sad() throws Exception {
		User user1 = new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny6",
				"johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		User saved = userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny6", "123", null));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		Post post = postRepository.save(new Post(null, "desc", "aaa", new Date(), saved, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		testRestTemplate.exchange("/posts/report/90", HttpMethod.POST, httpEntity, MessageDto.class);
		User found = userRepository.findByUsername("johnny6");
		assertEquals(0, found.getReportedPosts().size());

	}

	@Test
	public void commentPost_test_happy() throws Exception {
		User user1 = new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny7",
				"johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		User saved = userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny7", "123", null));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		Post post = postRepository.save(new Post(null, "desc", "aaa", new Date(), saved, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		testRestTemplate.exchange("/posts/comment/" + post.getId() + "/hello", HttpMethod.POST, httpEntity,
				MessageDto.class);
		Post found = postRepository.findById(post.getId()).get();
		assertEquals("hello", found.getPostComments().get(0).getContent());
	}

	@Test
	public void viewMyPosts_test_happy() throws Exception {
		User user1 = new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny8",
				"johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		User saved = userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny8", "123", null));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		Post post = postRepository.save(new Post(null, "desc", "aaa", new Date(), saved, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		ResponseEntity<Object> responseEntity = testRestTemplate.exchange("/posts/view-my-posts", HttpMethod.GET, httpEntity,
				Object.class);
		@SuppressWarnings("unchecked")
		List<Post> posts = (List<Post>) responseEntity.getBody();
		assertEquals(1, posts.size());
	}
	
	@Test
	public void viewMyPosts_test_sad() throws Exception {
		User user1 = new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny9",
				"johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		User saved = userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny9", "123", null));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<Object> responseEntity = testRestTemplate.exchange("/posts/view-my-posts", HttpMethod.GET, httpEntity,
				Object.class);
		@SuppressWarnings("unchecked")
		List<Post> posts = (List<Post>) responseEntity.getBody();
		assertEquals(0, posts.size());
	}
	
	@Test
	public void savePost_test_happy() throws Exception {
		User user1 = new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny10",
				"johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		User saved = userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny10", "123", null));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		Post post = postRepository.save(new Post(null, "desc", "aaa", new Date(), saved, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		testRestTemplate.exchange("/posts/save/" + post.getId(), HttpMethod.POST, httpEntity, MessageDto.class);
		User found = userRepository.findByUsername("johnny10");
		assertEquals(1, found.getSavedPosts().size());

	}
	
	@Test
	public void savePost_test_sad() throws Exception {
		User user1 = new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny11",
				"johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		User saved = userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny11", "123", null));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		Post post = postRepository.save(new Post(null, "desc", "aaa", new Date(), saved, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		testRestTemplate.exchange("/posts/save/90", HttpMethod.POST, httpEntity, MessageDto.class);
		User found = userRepository.findByUsername("johnny11");
		assertEquals(0, found.getSavedPosts().size());

	}

}
