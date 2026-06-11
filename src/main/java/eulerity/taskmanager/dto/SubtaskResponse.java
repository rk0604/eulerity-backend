package eulerity.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A single AI-suggested subtask, as returned to the client. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskResponse {

    private String title;

    private String description;
}
