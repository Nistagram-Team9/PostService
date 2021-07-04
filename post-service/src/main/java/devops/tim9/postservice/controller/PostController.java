package devops.tim9.postservice.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
import devops.tim9.postservice.service.ImageStorageService;
import devops.tim9.postservice.service.PostService;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
public class PostController {
	
	private PostService postService;
	private ImageStorageService imageStorageService;

	public PostController(PostService postService, ImageStorageService imageStorageService) {
		this.postService = postService;
		this.imageStorageService = imageStorageService;
	}
	
	
	@PostMapping
	public ResponseEntity<MessageDto> createPost(@RequestParam String description, @RequestParam List<String> tags, @RequestParam MultipartFile file) {
		System.out.println("Creating post");
		System.out.println(description);
		try {
			postService.createPost(description, tags, file);
		} catch (ImageStorageException e) {
			return new ResponseEntity<>(new MessageDto("Error", "Error while creating the product."),
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new MessageDto("Success", "Post is successfully created."), HttpStatus.CREATED);
	}
	@GetMapping("/download-image/{id}")
    public ResponseEntity<Resource> downloadImage(@PathVariable Integer id, HttpServletRequest request) {
		System.out.println("Uslo u download image");
        Post post = postService.getOne(id);
        if (post.getPicture() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Resource resource = imageStorageService.loadResource(post.getPicture());
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            System.out.println("Could not determine file type.");
        }
        if(contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
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
	
	@PostMapping(value="/comment/{id}/{content}")
	public ResponseEntity<MessageDto> commentPost(@PathVariable Integer id, @PathVariable String content) {
		postService.commentPost(id,content);
		return new ResponseEntity<>(new MessageDto("Success", "Comment is successfuly made."), HttpStatus.OK);
	}
	
	@GetMapping(value="/view-my-posts")
	public ResponseEntity<List<Post>> viewMyPosts(){
		return new ResponseEntity<>(postService.viewMyPosts(), HttpStatus.OK);
		
	}
	
	@GetMapping(value="/view/{username}")
	public ResponseEntity<List<Post>> viewUsersPosts(@PathVariable String username){
		return new ResponseEntity<>(postService.viewUsersPosts(username), HttpStatus.OK);
		
	}
	
	@PostMapping(value="/save/{id}")
	public ResponseEntity<MessageDto> savePost(@PathVariable Integer id){
		postService.savePost(id);
		return new ResponseEntity<>(new MessageDto("Success", "Post is successfully saved."), HttpStatus.OK);
		
	}
	
	@GetMapping(value="/liked")
	public ResponseEntity<List<Post>> likedByUser(){
		return new ResponseEntity<>(postService.likedByUser(), HttpStatus.OK);
		
	}
	
	@GetMapping(value="/disliked")
	public ResponseEntity<List<Post>> dislikedByUser(){
		return new ResponseEntity<>(postService.dislikedByUser(), HttpStatus.OK);
		
	}
	
	@GetMapping(value="/search/{username}")
	public ResponseEntity<List<Post>> searchByTag(@PathVariable String username){
		return new ResponseEntity<>(postService.searchByTag(username), HttpStatus.OK);
	}
	
	
	

}
