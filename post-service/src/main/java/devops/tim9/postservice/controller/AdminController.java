package devops.tim9.postservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import devops.tim9.postservice.dto.MessageDto;
import devops.tim9.postservice.model.Post;
import devops.tim9.postservice.model.User;
import devops.tim9.postservice.service.PostService;

@RestController
@RequestMapping(value = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
public class AdminController {
	
	private PostService postService;

	public AdminController(PostService postService) {
		this.postService = postService;
	}
	
	@GetMapping(value="/reported")
	public ResponseEntity<List<Post>> getReportedPosts(){
		return new ResponseEntity<>(postService.getReportedPosts(), HttpStatus.OK);
		
	}
	
	@DeleteMapping(value="/{id}")
	public ResponseEntity<MessageDto> deleteReportedPost(@PathVariable Integer id){
		postService.deletePost(id);
		return new ResponseEntity<>(new MessageDto("Success", "Post is successfuly deleted."), HttpStatus.OK);
		
	}
	
	@GetMapping(value="/agent-requests")
	public ResponseEntity<List<User>> getAgentRequests(){
		postService.getAgentRequests();
		return new ResponseEntity<>(postService.getAgentRequests(), HttpStatus.OK);
		
	}
	
	@PostMapping(value="/disable/{username}")
	public ResponseEntity<MessageDto> disableAccount(@PathVariable String username){
		postService.disableAccount(username);
		return new ResponseEntity<>(new MessageDto("Success", "Account is successfuly disabled."), HttpStatus.OK);
	}
	
	@PostMapping(value="/accept-agent/{username}")
	public ResponseEntity<MessageDto> acceptAgentRequest(@PathVariable String username){
		postService.acceptAgentRequest(username);
		return new ResponseEntity<>(new MessageDto("Success", "Account is successfuly registered."), HttpStatus.OK);
	}
	
	
	
	
	

}
