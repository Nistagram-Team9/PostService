package devops.tim9.postservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import devops.tim9.postservice.model.Tag;

public interface TagRepository extends JpaRepository<Tag, Integer>{
	
	Boolean existsByName(String name);
	Tag findByName(String name);

}
