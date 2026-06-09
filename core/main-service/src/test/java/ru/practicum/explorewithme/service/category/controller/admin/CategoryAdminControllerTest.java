package ru.practicum.explorewithme.service.category.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.category.dto.NewCategoryRequest;
import ru.practicum.explorewithme.service.category.dto.UpdateCategoryRequest;
import ru.practicum.explorewithme.service.category.service.CategoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryAdminController.class)
public class CategoryAdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSaveCategory() throws Exception {
        Long categoryId = 1L;
        NewCategoryRequest request = new NewCategoryRequest("new category");
        CategoryDto response = new CategoryDto(categoryId, request.getName());

        Mockito.when(categoryService.createCategory(any()))
                .thenReturn(response);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        Long catId = 1L;
        UpdateCategoryRequest request = new UpdateCategoryRequest(catId, "updated category");
        CategoryDto updatedCategory = new CategoryDto(catId, "updated category");

        Mockito.when(categoryService.changeCategory(any(), any()))
                .thenReturn(updatedCategory);

        mockMvc.perform(patch("/admin/categories/{catId}", catId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(catId))
                .andExpect(jsonPath("$.name").value("updated category"));
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        Long catId = 1L;

        Mockito.doNothing().when(categoryService).removeCategory(catId);

        mockMvc.perform(delete("/admin/categories/{catId}", catId))
                .andExpect(status().isNoContent());

        Mockito.verify(categoryService).removeCategory(catId);
    }
}
