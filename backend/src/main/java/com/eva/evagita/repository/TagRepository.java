package com.eva.evagita.repository;

import com.eva.evagita.model.Tag;
import com.eva.evagita.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByUser(User user);

    Optional<Tag> findByIdAndUser(Long id, User user);

    Optional<Tag> findByNameAndUser(String name, User user);

    boolean existsByNameAndUser(String name, User user);
}
