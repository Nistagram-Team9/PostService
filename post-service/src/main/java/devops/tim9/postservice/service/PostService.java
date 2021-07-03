package devops.tim9.postservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

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
	
	@Autowired
	AdminEventsProducer adminEventsProducer;

	public PostService(UserRepository userRepository, PostRepository postRepository,
			ImageStorageService imageStorageService, CommentRepository commentRepository) {
		this.userRepository = userRepository;
		this.postRepository = postRepository;
		this.imageStorageService = imageStorageService;
		this.commentRepository = commentRepository;
	}

	public Post createPost(String description, List<String> taggedUsernames, MultipartFile file)
			throws ImageStorageException {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		Post post = postRepository.save(new Post(null, description, null, user, new ArrayList<>(), new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
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

	public void commentPost(Integer id, String content, List<String> usernames) {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		Optional<Post> optionalPost = postRepository.findById(id);
		if (optionalPost.isPresent()) {
			Post post = optionalPost.get();
			Comment comment = commentRepository.save(new Comment(null, user, post, content, new ArrayList<>()));
			for (String username : usernames) {
				User user2 = userRepository.findByUsername(username);
				if (user2 != null && user2.getCanBeTagged()) {
					user2.getTaggedInComment().add(comment);
					userRepository.save(user2);
				}
			}
			user.getUsersComments().add(comment);
			userRepository.save(user);
			postRepository.save(post);
		}

	}

	public List<Post> viewUsersPosts(String username) {
		User user = userRepository.findByUsername(username);
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
	
	public List<Post> likedByUser(String username){
		User user = userRepository.findByUsername(username);
		if (user != null) {
			return user.getLikedPosts();
		}
		return new ArrayList<>();
	}
	
	public List<Post> dislikedByUser(String username){
		User user = userRepository.findByUsername(username);
		if (user != null) {
			return user.getDislikedPosts();
		}
		return new ArrayList<>();
	}
	
	public List<Post> searchByTag(String username){
		User user = userRepository.findByUsername(username);
		if (user != null) {
			return postRepository.findByTagged(user.getId());
		}
		return new ArrayList<>();	
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
	
	
	
	

}
