package devops.tim9.postservice.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {
	@Id
	@GeneratedValue
	private Integer id;

	private String description;
	
	private String picture;

	@ManyToOne
	private User user;

	@ManyToMany(mappedBy = "likedPosts")
	private List<User> likedBy;

	@ManyToMany(mappedBy = "dislikedPosts")
	private List<User> dislikedBy;

	@JsonIgnore
	@ManyToMany
	@JoinTable(name = "saved_post_user", 
	  joinColumns = @JoinColumn(name = "post_id"), 
	  inverseJoinColumns = @JoinColumn(name = "user_id"))
	private List<User> savedBy;
	
	@JsonIgnore
	@ManyToMany
	@JoinTable(name = "reported_post_user", 
	  joinColumns = @JoinColumn(name = "post_id"), 
	  inverseJoinColumns = @JoinColumn(name = "user_id"))
	private List<User> reportedBy;

	@OneToMany
	private List<Comment> postComments;

	@JsonIgnore
	@ManyToMany
	@JoinTable(name = "post_tags", 
	  joinColumns = @JoinColumn(name = "post_id"), 
	  inverseJoinColumns = @JoinColumn(name = "tag_id"))
	private List<Tag> tags = new ArrayList<>();

}
