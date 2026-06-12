package ru.practicum.explorewithme.service.category.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.explorewithme.service.category.dal.CategoryRepository;
import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.category.dto.NewCategoryRequest;
import ru.practicum.explorewithme.service.category.dto.UpdateCategoryRequest;
import ru.practicum.explorewithme.service.category.model.Category;
import ru.practicum.explorewithme.service.event.dal.EventRepository;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    NewCategoryRequest request;
    Category category;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        request = new NewCategoryRequest("new category");
        category = new Category(1L, "new category");
    }

//    сохраняет новую категорию

    @Test
    void shouldSaveCategory() {
        Mockito.when(categoryRepository.existsByName(Mockito.anyString()))
                .thenReturn(false);

        Mockito.when(categoryRepository.save(Mockito.any()))
                .thenReturn(category);

        CategoryDto addedCategory = categoryService.createCategory(request);
        Assertions.assertEquals(category.getName(), addedCategory.getName());
    }

    //    бросает ошибку, когда данные повторяются при создании категории
    @Test
    void shouldThrowExceptionWhenCategoryDuplicated() {
        Mockito.when(categoryRepository.existsByName(Mockito.anyString()))
                .thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.createCategory(request));

        verify(categoryRepository, times(1)).existsByName(request.getName());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    // изменяет категорию
    @Test
    void shouldReturnUpdatedCategory() {
        Long catId = 1L;
        UpdateCategoryRequest request1 = new UpdateCategoryRequest(catId, "updated request");
        Category category1 = new Category(catId, "updated category");
        Mockito.when(categoryRepository.findById(any()))
                .thenReturn(Optional.ofNullable(category));
        Mockito.when(categoryRepository.save(any()))
                .thenReturn(category1);

        CategoryDto dto = categoryService.changeCategory(catId, request1);
        Assertions.assertEquals(dto.getId(), request1.getId());
        Assertions.assertEquals(dto.getName(), request1.getName());
    }

    //бросает ошибку, если категория не найдена
    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        Long catId = 1L;
        Mockito.when(categoryRepository.findById(catId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> {
            categoryService.getCategoryById(catId);
        });
    }

    // должен удалять категорию
    @Test
    void shouldDeleteCategory() {
        when(categoryRepository.findById(category.getId()))
                .thenReturn(Optional.of(category));

        when(eventRepository.countByCategoryId(anyLong()))
                .thenReturn(0);

        categoryService.removeCategory(category.getId());

        verify(categoryRepository).findById(category.getId());
        verify(categoryRepository).delete(category);
    }

    //должен возвращать список категорий
    @Test
    void shouldReturnPagedCategories() {
        int from = 0;
        int size = 10;

        Category cat1 = new Category(1L, "cat 1");

        Page<Category> page = new PageImpl<>(List.of(cat1, category));

        when(categoryRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        List<CategoryDto> result = categoryService.getAllCategories(from, size);

        assertEquals(2, result.size());
        assertEquals("cat 1", result.get(0).getName());
        assertEquals("new category", result.get(1).getName());

        verify(categoryRepository).findAll(any(Pageable.class));
    }

    //должен возвращать категорию по id
    @Test
    void shouldReturnCategoryById() {
        when(categoryRepository.findById(category.getId()))
                .thenReturn(Optional.of(category));

        CategoryDto result = categoryService.getCategoryById(category.getId());

        assertEquals(category.getId(), result.getId());
        assertEquals("new category", result.getName());
    }
}
