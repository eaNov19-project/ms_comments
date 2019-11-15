package ea.sof.ms_comments.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentReqModel {
    @NotEmpty(message = "Please provide the body")
    private String body;
}
