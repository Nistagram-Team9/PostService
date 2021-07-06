package devops.tim9.postservice.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import devops.tim9.postservice.config.domain.FollowEvent;
import devops.tim9.postservice.config.domain.AcceptAgentEvent;
import devops.tim9.postservice.config.domain.DisableUserEvent;
import devops.tim9.postservice.exception.ImageStorageException;
import devops.tim9.postservice.model.Comment;
import devops.tim9.postservice.model.Post;
import devops.tim9.postservice.model.User;
import devops.tim9.postservice.producer.AdminEventsProducer;
import devops.tim9.postservice.repository.CommentRepository;
import devops.tim9.postservice.repository.PostRepository;
import devops.tim9.postservice.repository.UserRepository;

@Service
public class PostService {

	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final ImageStorageService imageStorageService;
	private final CommentRepository commentRepository;
	private final ObjectMapper objectMapper;

	@Autowired
	AdminEventsProducer adminEventsProducer;

	public PostService(UserRepository userRepository, PostRepository postRepository,
			ImageStorageService imageStorageService, CommentRepository commentRepository, ObjectMapper objectMapper) {
		this.userRepository = userRepository;
		this.postRepository = postRepository;
		this.imageStorageService = imageStorageService;
		this.commentRepository = commentRepository;
		this.objectMapper = objectMapper;
	}

	public Post createPost(String description, List<String> taggedUsernames, MultipartFile file)
			throws Exception {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		Post post = postRepository.save(new Post(null, description, null, new Date(), user, new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		if (taggedUsernames.size() > 0) {
			for (String username : taggedUsernames) {
				User user2 = userRepository.findByUsername(username);
				if (user2 != null && user2.getCanBeTagged()) {
					user2.getTaggedInPost().add(post);
					userRepository.save(user2);
				}
			}
		}
		String savedImagePath = imageStorageService.storeImage(file, user.getUsername());
		post.setPicture(savedImagePath);
		return postRepository.save(post);
	}
	
	public Post getOne(Integer id) {
		return postRepository.getOne(id);
	}

	public void likePost(Integer id) {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		Optional<Post> optionalPost = postRepository.findById(id);
		if (optionalPost.isPresent()) {
			Post post = optionalPost.get();
			if (user.getDislikedPosts().contains(post)) {
				user.getDislikedPosts().remove(post);
			}
			user.getLikedPosts().add(post);
			userRepository.save(user);
		}

	}

	public void dislikePost(Integer id) {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		Optional<Post> optionalPost = postRepository.findById(id);
		if (optionalPost.isPresent()) {
			Post post = optionalPost.get();
			if (user.getLikedPosts().contains(post)) {
				user.getLikedPosts().remove(post);
			}
			user.getDislikedPosts().add(post);
			userRepository.save(user);
		}

	}

	public void reportPost(Integer id) {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		Optional<Post> optionalPost = postRepository.findById(id);
		if (optionalPost.isPresent()) {
			Post post = optionalPost.get();
			post.getReportedBy().add(user);
			postRepository.save(post);
		}

	}

	public void commentPost(Integer id, String content) {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		Optional<Post> optionalPost = postRepository.findById(id);
//		if (optionalPost.isPresent()) {
//			Post post = optionalPost.get();
//			Comment comment = commentRepository.save(new Comment(null, user, post, content, new ArrayList<>()));
//			for (String username : usernames) {
//				User user2 = userRepository.findByUsername(username);
//				if (user2 != null && user2.getCanBeTagged()) {
//					user2.getTaggedInComment().add(comment);
//					userRepository.save(user2);
//				}
//			}
//			user.getUsersComments().add(comment);
//			userRepository.save(user);
//			postRepository.save(post);
//		}
		
		Post post = postRepository.findById(id).get();
		Comment comment = commentRepository.save(new Comment(null, user, post, content, new ArrayList<>()));
		post.getPostComments().add(comment);
		userRepository.save(user);
		postRepository.save(post);
		commentRepository.save(comment);
		

	}

	public List<Post> viewUsersPosts(String username) {
		User user = userRepository.findByUsername(username);
		if (user != null) {
			return postRepository.findByUser(user);
		}
		return new ArrayList<>();
	}
	
	public List<Post> viewMyPosts() {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		if (user != null) {
			return postRepository.findByUser(user);
		}
		return new ArrayList<>();
	}
	
	public void savePost(Integer id) {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		Optional<Post> optionalPost = postRepository.findById(id);
		if (optionalPost.isPresent()) {
			Post post = optionalPost.get();
			post.getSavedBy().add(user);
			postRepository.save(post);
		}
	}

	public List<Post> likedByUser(){
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		Optional<Post> optionalPost = postRepository.findById(user.getId());
		if (user != null) {
			return user.getLikedPosts();
		}
		return new ArrayList<>();
	}
	
	public List<Post> dislikedByUser(){
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		Optional<Post> optionalPost = postRepository.findById(user.getId());
		if (user != null) {
			return user.getDislikedPosts();
		}
		return new ArrayList<>();
	}
	
	public List<Post> searchByTag(String username){
		User user = userRepository.findByUsername(username);
		return user.getPosts();
//		if (user != null) {
//			return postRepository.findByTagged(user.getId());
//		}
	}
	
	public List<Post> getReportedPosts(){
		return postRepository.findByReported();
	}
	
	public void deletePost(Integer id) {
		postRepository.deleteById(id);
	}
	
	public List<User> getAgentRequests(){
		return userRepository.findByIsAccepted(false);
	}
	
	public void disableAccount(String username) {
		DisableUserEvent disableUserEvent = new DisableUserEvent(null, username);
		try {
			adminEventsProducer.sendDisableUserEvent(disableUserEvent);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void acceptAgentRequest(String username) {
		User user = userRepository.findByUsername(username);
		if(user!=null) {
			user.setIsAccepted(true);
			userRepository.save(user);
			AcceptAgentEvent acceptAgentEvent = new AcceptAgentEvent(null,username);
			try {
				adminEventsProducer.sendAcceptAgentEvent(acceptAgentEvent);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	

	public void createFollowing(FollowEvent followEvent) {
		User userFollowed = userRepository.findByUsername(followEvent.getUsernameFollowed());
		User userFollowing = userRepository.findByUsername(followEvent.getUsernameFollowedBy());
		userFollowed.getFollowers().add(userFollowing);
		userRepository.save(userFollowed);

	}

	@KafkaListener(topics = { "follow-events" })
	public void onMessage(ConsumerRecord<Integer, String> consumerRecord) {
		String value = consumerRecord.value();
		try {
			FollowEvent followEvent = objectMapper.readValue(value, FollowEvent.class);
			createFollowing(followEvent);
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<Post> getFollowingPosts() {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Post> followersPosts = new ArrayList<>();
		for (User u : userRepository.getFollowing(user.getId())) {
			followersPosts.addAll(postRepository.findByUser(u));
		}
		Comparator<Post> comparator = Comparator.comparing(Post::getCreatedAt);
		Collections.sort(followersPosts, comparator.reversed());
		return followersPosts;

	}

}
