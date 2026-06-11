package eulerity.taskmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eulerity.taskmanager.ai.GeneratedSubtask;
import eulerity.taskmanager.ai.TaskBreakdownGenerator;
import eulerity.taskmanager.dto.TaskBreakdownResponse;
import eulerity.taskmanager.dto.TaskRequest;
import eulerity.taskmanager.dto.TaskResponse;
import eulerity.taskmanager.enums.Priority;
import eulerity.taskmanager.enums.Status;
import eulerity.taskmanager.exception.ResourceNotFoundException;
import eulerity.taskmanager.mapper.TaskMapper;
import eulerity.taskmanager.model.Task;
import eulerity.taskmanager.repository.TaskRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pure unit tests for the business logic. Repository and mapper are mocked, so
 * these assert the service's behavior (defaults, lookups, error paths) in
 * isolation — no Spring context, no database.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskBreakdownGenerator breakdownGenerator;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTask_appliesDefaults_whenPriorityAndStatusOmitted() {
        TaskRequest request = TaskRequest.builder().title("New task").build();
        Task mapped = Task.builder().title("New task").build(); // no priority/status
        when(taskMapper.toEntity(request)).thenReturn(mapped);
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toResponse(any(Task.class)))
                .thenReturn(TaskResponse.builder().id(1L).title("New task").build());

        taskService.createTask(request);

        // The entity actually saved must have had defaults applied.
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Status.TODO);
        assertThat(captor.getValue().getPriority()).isEqualTo(Priority.MEDIUM);
    }

    @Test
    void createTask_keepsProvidedValues_whenPriorityAndStatusGiven() {
        TaskRequest request = TaskRequest.builder()
                .title("Urgent").priority(Priority.HIGH).status(Status.IN_PROGRESS).build();
        Task mapped = Task.builder()
                .title("Urgent").priority(Priority.HIGH).status(Status.IN_PROGRESS).build();
        when(taskMapper.toEntity(request)).thenReturn(mapped);
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toResponse(any(Task.class)))
                .thenReturn(TaskResponse.builder().id(2L).build());

        taskService.createTask(request);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Status.IN_PROGRESS);
        assertThat(captor.getValue().getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void getAllTasks_mapsEveryEntity() {
        when(taskRepository.findAll())
                .thenReturn(List.of(new Task(), new Task()));
        when(taskMapper.toResponse(any(Task.class)))
                .thenReturn(TaskResponse.builder().build());

        List<TaskResponse> result = taskService.getAllTasks();

        assertThat(result).hasSize(2);
        verify(taskMapper, org.mockito.Mockito.times(2)).toResponse(any(Task.class));
    }

    @Test
    void getTaskById_returnsResponse_whenFound() {
        Task task = Task.builder().id(5L).title("Found").build();
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task))
                .thenReturn(TaskResponse.builder().id(5L).title("Found").build());

        TaskResponse result = taskService.getTaskById(5L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getTitle()).isEqualTo("Found");
    }

    @Test
    void getTaskById_throws_whenMissing() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateTask_appliesMapperUpdate_andSaves_whenFound() {
        Task existing = Task.builder().id(7L).title("Old").build();
        TaskRequest request = TaskRequest.builder().title("Updated").build();
        when(taskRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toResponse(any(Task.class)))
                .thenReturn(TaskResponse.builder().id(7L).title("Updated").build());

        TaskResponse result = taskService.updateTask(7L, request);

        verify(taskMapper).updateEntity(existing, request);
        verify(taskRepository).save(existing);
        assertThat(result.getTitle()).isEqualTo("Updated");
    }

    @Test
    void updateTask_throws_whenMissing() {
        when(taskRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(404L, TaskRequest.builder().title("x").build()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void deleteTask_deletes_whenExists() {
        when(taskRepository.existsById(3L)).thenReturn(true);

        taskService.deleteTask(3L);

        verify(taskRepository).deleteById(3L);
    }

    @Test
    void deleteTask_throws_whenMissing() {
        when(taskRepository.existsById(3L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.deleteTask(3L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void breakdownTask_returnsMappedSubtasks_whenFound() {
        Task task = Task.builder().id(8L).title("Plan launch").description("Q3 release").build();
        when(taskRepository.findById(8L)).thenReturn(Optional.of(task));
        when(breakdownGenerator.generate("Plan launch", "Q3 release"))
                .thenReturn(List.of(
                        new GeneratedSubtask("Draft timeline", "Lay out milestones"),
                        new GeneratedSubtask("Assign owners", "Map tasks to people")));

        TaskBreakdownResponse result = taskService.breakdownTask(8L);

        assertThat(result.getTaskId()).isEqualTo(8L);
        assertThat(result.getTitle()).isEqualTo("Plan launch");
        assertThat(result.getSubtasks()).hasSize(2);
        assertThat(result.getSubtasks().get(0).getTitle()).isEqualTo("Draft timeline");
        assertThat(result.getSubtasks().get(1).getDescription()).isEqualTo("Map tasks to people");
    }

    @Test
    void breakdownTask_throws_whenMissing_andDoesNotCallAi() {
        when(taskRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.breakdownTask(404L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(breakdownGenerator, never()).generate(any(), any());
    }
}
