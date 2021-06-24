package devops.tim9.postservice.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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

	@ManyToOne
	private User user;

	@ManyToMany(mappedBy = "likedPosts")
	private List<User> likedBy = new ArrayList<>();

	@ManyToMany(mappedBy = "dislikedPosts")
	private List<User> dislikedBy = new ArrayList<>();;

	@ManyToMany(mappedBy = "savedPosts")
	private List<User> savedBy = new ArrayList<>();;

	@OneToMany
	private List<Comment> postComments = new ArrayList<>();

	@JsonIgnore
	@ManyToMany
	private List<Tag> tags = new ArrayList<>();

}
