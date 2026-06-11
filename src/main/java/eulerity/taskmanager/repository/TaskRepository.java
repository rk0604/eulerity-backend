package eulerity.taskmanager.repository;

import eulerity.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Inherits findAll, findById, save, deleteById, existsById, etc.
    // Custom derived queries (e.g. findByStatus) can be added here later.
}
