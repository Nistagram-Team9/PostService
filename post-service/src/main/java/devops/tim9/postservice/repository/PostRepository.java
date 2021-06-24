package devops.tim9.postservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import devops.tim9.postservice.model.Post;

public interface PostRepository extends JpaRepository<Post, Integer> {

}
