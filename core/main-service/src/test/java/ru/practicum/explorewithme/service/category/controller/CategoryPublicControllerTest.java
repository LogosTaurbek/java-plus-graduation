package ru.practicum.explorewithme.service.category.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.category.service.CategoryService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CategoryPublicController.class)
public class CategoryPublicControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;


    //получение всех категорий
    @Test
    void shouldReturnCategories() throws Exception {
        CategoryDto cat1 = new CategoryDto(1L, "cat 1");
        CategoryDto cat2 = new CategoryDto(2L, "cat 2");

        Mockito.when(categoryService.getAllCategories(0, 10))
                .thenReturn(List.of(cat1, cat2));

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("to", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].name").value("cat 1"))
                .andExpect(jsonPath("$.[1].id").value(2))
                .andExpect(jsonPath("$.[1].name").value("cat 2"));
    }

    @Test
    void shouldReturnCategoryById() throws Exception {
        Long catId = 1L;
        CategoryDto category = new CategoryDto(catId, "category");

        Mockito.when(categoryService.getCategoryById(catId))
                .thenReturn(category);

        mockMvc.perform(get("/categories/{catId}", catId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(catId))
                .andExpect(jsonPath("$.name").value("category"));
    }
}
