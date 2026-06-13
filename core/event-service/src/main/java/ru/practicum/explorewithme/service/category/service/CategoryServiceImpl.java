package ru.practicum.explorewithme.service.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.service.category.dal.CategoryRepository;
import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.category.dto.NewCategoryRequest;
import ru.practicum.explorewithme.service.category.dto.UpdateCategoryRequest;
import ru.practicum.explorewithme.service.category.mapper.CategoryMapper;
import ru.practicum.explorewithme.service.category.model.Category;
import ru.practicum.explorewithme.service.event.dal.EventRepository;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto createCategory(NewCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new ConflictException("Категория " +
                    request.getName() + " уже существует");
        }
        Category category = CategoryMapper.mapToCategory(request);
        category = categoryRepository.save(category);
        return CategoryMapper.mapToCategoryDto(category);
    }

    @Override
    public CategoryDto changeCategory(Long catId, UpdateCategoryRequest request) {
        if (!categoryRepository.findByNameIgnoreCaseAndIdNot(request.getName(), catId).isEmpty()) {
            throw new ConflictException("Такая категория уже существует" + request.getName());
        }
        Category category = categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException("Категория " + catId + " не была найдена"));
        Category updatedCategory = CategoryMapper.mapToUpdateCategory(request, category);

        categoryRepository.save(updatedCategory);
        return CategoryMapper.mapToCategoryDto(updatedCategory);
    }

    @Override
    public void removeCategory(Long catId) {
        if (eventRepository.countByCategoryId(catId) > 0) {
            throw new ConflictException("В этой категории уже существуют события");
        }
        Category category = categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException("Категория " + catId + " не была найдена"));
        categoryRepository.delete(category);
    }

    @Override
    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream().map(CategoryMapper::mapToCategoryDto).toList();
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(() -> {
            throw new NotFoundException("Категория c id " + catId + " не найдена");
        });
        return CategoryMapper.mapToCategoryDto(category);
    }
}
