package ru.practicum.explorewithme.service.compilation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.explorewithme.service.compilation.dal.CompilationRepository;
import ru.practicum.explorewithme.service.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.service.compilation.dto.NewCompilationDto;
import ru.practicum.explorewithme.service.compilation.dto.UpdateCompilationRequestDto;
import ru.practicum.explorewithme.service.compilation.model.Compilation;
import ru.practicum.explorewithme.service.event.dal.EventRepository;
import ru.practicum.explorewithme.service.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompilationServiceImplTest {
    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    private Compilation compilation;
    private NewCompilationDto newCompilationDto;

    @BeforeEach
    void setUp() {
        compilation = new Compilation();
        ReflectionTestUtils.setField(compilation, "id", 1L);
        compilation.setTitle("Test Compilation");
        compilation.setPinned(true);
        compilation.setEvents(new HashSet<>());

        newCompilationDto = new NewCompilationDto("Test Compilation", true, List.of());
    }

    @Test
    void create_ShouldReturnCompilationDto() {
        when(compilationRepository.save(any(Compilation.class))).thenReturn(compilation);

        CompilationDto result = compilationService.create(newCompilationDto);

        assertThat(result).isNotNull();
        verify(compilationRepository).save(any(Compilation.class));
    }

    @Test
    void update_ShouldReturnUpdatedCompilation() {
        UpdateCompilationRequestDto request = new UpdateCompilationRequestDto("Updated", false, List.of());
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(compilationRepository.save(any(Compilation.class))).thenReturn(compilation);

        CompilationDto result = compilationService.update(1L, request);

        assertThat(result).isNotNull();
        verify(compilationRepository).save(any(Compilation.class));
    }

    @Test
    void update_WhenNotFound_ShouldThrowNotFound() {
        UpdateCompilationRequestDto request = new UpdateCompilationRequestDto("Updated", false, List.of());
        when(compilationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compilationService.update(999L, request)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_ShouldCallRepository() {
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        doNothing().when(compilationRepository).delete(compilation);

        compilationService.delete(1L);

        verify(compilationRepository).delete(compilation);
    }

    @Test
    void delete_WhenNotFound_ShouldThrowNotFound() {
        when(compilationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compilationService.delete(999L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_ShouldReturnCompilation() {
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));

        CompilationDto result = compilationService.getById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void getById_WhenNotFound_ShouldThrowNotFound() {
        when(compilationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compilationService.getById(999L)).isInstanceOf(NotFoundException.class);
    }
}