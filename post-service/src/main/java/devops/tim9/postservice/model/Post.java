package devops.tim9.postservice.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

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
	
	private Date createdAt;

	@ManyToOne
	private User user;

	@ManyToMany(mappedBy = "likedPosts", cascade=CascadeType.REMOVE)
	private List<User> likedBy;

	@ManyToMany(mappedBy = "dislikedPosts", cascade=CascadeType.REMOVE)
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

	@OneToMany(mappedBy="post",cascade=CascadeType.REMOVE)
	private List<Comment> postComments;

	@ManyToMany(mappedBy="taggedInPost",cascade=CascadeType.REMOVE)
	private List<User> taggedUsers;

}
