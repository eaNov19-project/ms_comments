package ea.sof.ms_comments.entity;

import ea.sof.ms_comments.model.CommentReqModel;
import ea.sof.shared.models.Comment;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Document(collection = "comments")
public class CommentEntity {
    @Id
    private String id;
    private String userId;
    private String body;
    private LocalDateTime created;
    private String questionId;
    private String answerId;

    public CommentEntity(CommentReqModel commentReqModel) {
        this.body = commentReqModel.getBody();
        this.created = LocalDateTime.now();
    }

    public Comment toCommentModel() {
        Comment commentModel = new Comment();
        commentModel.setId(this.id);
        commentModel.setBody(this.body);
        commentModel.setDate(this.created);
        commentModel.setQuestionId(this.questionId);

        //todo: why comment model didn't have answerId
        //todo: why comment model has userName;
        return commentModel;
    }
}
