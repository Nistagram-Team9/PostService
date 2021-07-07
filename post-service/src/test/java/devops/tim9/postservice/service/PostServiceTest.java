package devops.tim9.postservice.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import devops.tim9.postservice.dto.UserDto;
import devops.tim9.postservice.model.Post;
import devops.tim9.postservice.model.User;
import devops.tim9.postservice.repository.PostRepository;
import devops.tim9.postservice.repository.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
@EmbeddedKafka(partitions = 1, controlledShutdown = false, brokerProperties = { "listeners=PLAINTEXT://localhost:9093",
		"port=9093" })
@Transactional
public class PostServiceTest {
	@Autowired
	UserService userService;

	@Autowired
	PostService postService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	TestRestTemplate testRestTemplate;

	@Autowired
	private AuthenticationManager authenticationManager;

	@MockBean
	private ImageStorageService imageStorageService;

	@Autowired
	private PostRepository postRepository;

	@Test
	public void createPost_test_happy() throws Exception {
		userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny",
				"johnny.web", "biography", false, true, true, "123"));
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("johnny", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		Mockito.when(imageStorageService.storeImage(null, "johnny")).thenReturn("johnnyPic");
		SecurityContextHolder.setContext(securityContext);
		Post post = postService.createPost("desc", new ArrayList<>(), null);
		assertEquals("desc", post.getDescription());

	}

	@Test
	public void getOne_test_happy() throws Exception {
		User user = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny2", "johnny.web", "biography", false, true, true, "123"));
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("johnny2", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Post post = postRepository.save(new Post(null, "desc", "pic", new Date(), user, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		Post found = postService.getOne(post.getId());
		assertEquals("desc", found.getDescription());
		assertEquals("pic", found.getPicture());

	}

	@Test(expected = EntityNotFoundException.class)
	public void getOne_test_sad() throws Exception {
		User user = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny3", "johnny.web", "biography", false, true, true, "123"));
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("johnny3", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Post post = postService.getOne(30);
		assertNull(post);

	}

	@Test
	public void likePost_test_happy() throws Exception {
		User user = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny4", "johnny.web", "biography", false, true, true, "123"));
		user.setDislikedPosts(new ArrayList<>());
		user.setLikedPosts(new ArrayList<>());
		userRepository.save(user);
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("johnny4", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Post post = postRepository.save(new Post(null, "desc", "pic", new Date(), user, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		postService.likePost(post.getId());
		User found = userRepository.findById(user.getId()).get();
		assertEquals(1, found.getLikedPosts().size());
	}

	@Test
	public void dislikePost_test_happy() throws Exception {
		User user = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny5", "johnny.web", "biography", false, true, true, "123"));
		user.setDislikedPosts(new ArrayList<>());
		user.setLikedPosts(new ArrayList<>());
		userRepository.save(user);
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("johnny5", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Post post = postRepository.save(new Post(null, "desc", "pic", new Date(), user, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		postService.dislikePost(post.getId());
		User found = userRepository.findById(user.getId()).get();
		assertEquals(1, found.getDislikedPosts().size());
	}

	@Test
	public void reportedPost_test_happy() throws Exception {
		User user = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny6", "johnny.web", "biography", false, true, true, "123"));
		user.setReportedPosts(new ArrayList<>());
		userRepository.save(user);
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("johnny6", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Post post = postRepository.save(new Post(null, "desc", "pic", new Date(), user, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		postService.reportPost(post.getId());
		Post found = postRepository.findById(post.getId()).get();
		assertEquals(1, found.getReportedBy().size());
	}
	
	@Test
	public void commentPost_test_happy() throws Exception {
		User user = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny7", "johnny.web", "biography", false, true, true, "123"));
		user.setReportedPosts(new ArrayList<>());
		userRepository.save(user);
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("johnny7", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Post post = postRepository.save(new Post(null, "desc", "pic", new Date(), user, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		postService.commentPost(post.getId(), "comment");
		Post found = postRepository.findById(post.getId()).get();
		assertEquals(1, found.getPostComments().size());
	}
	
	
	@Test
	public void viewMyPosts_test_happy() throws Exception {
		User user = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny8", "johnny.web", "biography", false, true, true, "123"));
		user.setReportedPosts(new ArrayList<>());
		userRepository.save(user);
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("johnny8", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Post post = postRepository.save(new Post(null, "desc", "pic", new Date(), user, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		List<Post> myPosts = postService.viewMyPosts();
		assertEquals(1, myPosts.size());
	}
	
	@Test
	public void viewUsersPosts_test_happy() throws Exception {
		User user = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny9", "johnny.web", "biography", false, true, true, "123"));
		user.setReportedPosts(new ArrayList<>());
		userRepository.save(user);
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("johnny9", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Post post = postRepository.save(new Post(null, "desc", "pic", new Date(), user, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		List<Post> usersPosts = postService.viewUsersPosts("johnny9");
		assertEquals(1, usersPosts.size());
	}
	
	
	@Test
	public void savePost_test_happy() throws Exception {
		User user = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny10", "johnny.web", "biography", false, true, true, "123"));
		user.setSavedPosts(new ArrayList<>());
		userRepository.save(user);
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("johnny10", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Post post = postRepository.save(new Post(null, "desc", "pic", new Date(), user, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		postService.savePost(post.getId());
		Post found = postRepository.findById(post.getId()).get();
		assertEquals(1, post.getSavedBy().size());
	}
	
	@Test
	public void likedByUser_test_happy() throws Exception {
		User user = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny11", "johnny.web", "biography", false, true, true, "123"));
		user.setDislikedPosts(new ArrayList<>());
		user.setLikedPosts(new ArrayList<>());
		userRepository.save(user);
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("johnny11", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Post post = postRepository.save(new Post(null, "desc", "pic", new Date(), user, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		postService.likePost(post.getId());
		assertEquals(1, postService.likedByUser().size());
		
	}
	
	@Test
	public void dislikedByUser_test_happy() throws Exception {
		User user = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny11", "johnny.web", "biography", false, true, true, "123"));
		user.setDislikedPosts(new ArrayList<>());
		user.setLikedPosts(new ArrayList<>());
		userRepository.save(user);
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("johnny11", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Post post = postRepository.save(new Post(null, "desc", "pic", new Date(), user, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		postService.dislikePost(post.getId());
		assertEquals(1, postService.dislikedByUser().size());
		
	}
	
	
	@Test
	public void acceptAgentRequest_test_happy() throws Exception {
		User user = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny12", "johnny.web", "biography", false, true, true, "123"));
		user.setDislikedPosts(new ArrayList<>());
		user.setLikedPosts(new ArrayList<>());
		userRepository.save(user);
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("johnny12", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		User user2 = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny13", "johnny.web", "biography", false, true, true, "123"));
		user2.setIsAccepted(false);
		userRepository.save(user2);
		postService.acceptAgentRequest("johnny13");
		assertTrue(userRepository.findByUsername("johnny13").getIsAccepted());
		
		
	}
	
	
	@Test
	public void getAgentRequests_test_happy() throws Exception {
		User user = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny14", "johnny.web", "biography", false, true, true, "123"));
		user.setDislikedPosts(new ArrayList<>());
		user.setLikedPosts(new ArrayList<>());
		userRepository.save(user);
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("johnny14", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		User user2 = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny15", "johnny.web", "biography", false, true, true, "123"));
		user2.setIsAccepted(false);
		userRepository.save(user2);
		assertEquals(1, postService.getAgentRequests().size());
		
		
	}
}
