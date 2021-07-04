package devops.tim9.postservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import devops.tim9.postservice.exception.ImageStorageException;
import devops.tim9.postservice.model.Comment;
import devops.tim9.postservice.model.Post;
import devops.tim9.postservice.model.User;
import devops.tim9.postservice.repository.CommentRepository;
import devops.tim9.postservice.repository.PostRepository;
import devops.tim9.postservice.repository.UserRepository;

@Service
public class PostService {

	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final ImageStorageService imageStorageService;
	private final CommentRepository commentRepository;

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
		if (user != null) {
			return postRepository.findByTagged(user.getId());
		}
		return new ArrayList<>();	
	}
	

}
