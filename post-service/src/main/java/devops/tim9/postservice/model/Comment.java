package devops.tim9.postservice.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

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
public class Comment {
	
	@Id
	@GeneratedValue
	private Integer id;
	
	@ManyToOne
	private User user;
	
	@JsonIgnore
	@ManyToOne
	private Post post;
	
	private String content;
	
	@ManyToMany(mappedBy="taggedInComment")
	private List<User> taggedUsers;

}
