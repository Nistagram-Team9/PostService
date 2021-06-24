package devops.tim9.postservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import devops.tim9.postservice.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer>{

}
