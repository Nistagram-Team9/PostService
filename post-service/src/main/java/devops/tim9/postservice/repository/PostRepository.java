package devops.tim9.postservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import devops.tim9.postservice.model.Post;
import devops.tim9.postservice.model.User;

public interface PostRepository extends JpaRepository<Post, Integer> {
	
	List<Post> findByUser(User user);
	
	@Query(value = "SELECT * FROM post LEFT JOIN user_tag_post ON user_tag_post.post_id = post.id WHERE user_tag_post.user_id = ?1", nativeQuery = true)
	List<Post> findByTagged(Integer id);
	
	@Query(value = "SELECT * FROM post LEFT JOIN reported_post_user ON reported_post_user.post_id = post.id", nativeQuery = true)
	List<Post> findByReported();

}
