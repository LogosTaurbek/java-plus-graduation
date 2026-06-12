package ru.practicum.explorewithme.service.category.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.service.category.model.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    Optional<Category> findByNameIgnoreCaseAndIdNot(String name, Long id);
}
