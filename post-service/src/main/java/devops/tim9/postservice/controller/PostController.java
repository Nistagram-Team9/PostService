package devops.tim9.postservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import devops.tim9.postservice.dto.MessageDto;
import devops.tim9.postservice.exception.ImageStorageException;
import devops.tim9.postservice.model.Post;
import devops.tim9.postservice.service.PostService;

@RestController
@RequestMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
public class PostController {
	
	private PostService postService;

	public PostController(PostService postService) {
		this.postService = postService;
	}
	
	
	@PostMapping
	public ResponseEntity<MessageDto> createPost(@RequestParam String description, @RequestParam List<String> tags, @RequestParam MultipartFile file) {
		try {
			postService.createPost(description, tags, file);
		} catch (ImageStorageException e) {
			return new ResponseEntity<>(new MessageDto("Error", "Error while creating the product."),
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new MessageDto("Success", "Post is successfully created."), HttpStatus.CREATED);
	}
	
	@GetMapping(value = "/{tag}")
	public ResponseEntity<List<Post>> findByTag(@PathVariable String tag) {
		return new ResponseEntity<>(postService.findByTag(tag), HttpStatus.OK);
	}
	
	@PostMapping(value = "/like/{id}")
	public ResponseEntity<MessageDto> likePost(@PathVariable Integer id) {
		postService.likePost(id);
		return new ResponseEntity<>(new MessageDto("Success", "Post is successfully liked."), HttpStatus.OK);
	}
	
	@PostMapping(value = "/dislike/{id}")
	public ResponseEntity<MessageDto> dislikePost(@PathVariable Integer id) {
		postService.dislikePost(id);
		return new ResponseEntity<>(new MessageDto("Success", "Post is successfully disliked."), HttpStatus.OK);
	}
	
	@PostMapping(value = "/report/{id}")
	public ResponseEntity<MessageDto> reportPost(@PathVariable Integer id) {
		postService.reportPost(id);
		return new ResponseEntity<>(new MessageDto("Success", "Post is successfully reported."), HttpStatus.OK);
	}
	

}
