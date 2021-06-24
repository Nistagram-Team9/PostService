package devops.tim9.postservice.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import devops.tim9.postservice.exception.ImageStorageException;
import devops.tim9.postservice.model.Comment;
import devops.tim9.postservice.model.Post;
import devops.tim9.postservice.model.Tag;
import devops.tim9.postservice.model.User;
import devops.tim9.postservice.repository.PostRepository;
import devops.tim9.postservice.repository.TagRepository;
import devops.tim9.postservice.repository.UserRepository;

@Service
public class PostService {

	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final TagRepository tagRepository;
	private final ImageStorageService imageStorageService;

	public PostService(UserRepository userRepository, PostRepository postRepository, TagRepository tagRepository,
			ImageStorageService imageStorageService) {
		this.userRepository = userRepository;
		this.postRepository = postRepository;
		this.tagRepository = tagRepository;
		this.imageStorageService = imageStorageService;
	}

	public Post createPost(String description, List<String> tags, MultipartFile file) throws ImageStorageException {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		Post post = new Post(null, description, null, user, new ArrayList<>(), new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		if (tags.size() > 0) {
			for (String tag : tags) {
				if (!tagRepository.existsByName(tag)) {
					Tag tag2 = tagRepository.save(new Tag(null, tag, new ArrayList<>()));
					post.getTags().add(tag2);
				} else {
					Tag tag2 = tagRepository.findByName(tag);
					post.getTags().add(tag2);
				}
			}
		}
		String savedImagePath = imageStorageService.storeImage(file, user.getUsername());
		post.setPicture(savedImagePath);
		return postRepository.save(post);
	}

}
