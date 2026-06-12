package ru.practicum.explorewithme.service.category.service;

import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.category.dto.NewCategoryRequest;
import ru.practicum.explorewithme.service.category.dto.UpdateCategoryRequest;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryRequest request);

    CategoryDto changeCategory(Long catId, UpdateCategoryRequest request);

    void removeCategory(Long catId);

    List<CategoryDto> getAllCategories(Integer from, Integer size);

    CategoryDto getCategoryById(Long catId);
}
