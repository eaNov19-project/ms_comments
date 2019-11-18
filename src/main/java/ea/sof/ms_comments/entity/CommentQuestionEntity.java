package ea.sof.ms_comments.entity;

import ea.sof.ms_comments.model.CommentReqModel;
import ea.sof.shared.models.CommentQuestion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "question_comments")
public class CommentQuestionEntity {
    @Id
    private String id;
    private String userId;
    private String body;
    private LocalDateTime created;
    private String questionId;

    public CommentQuestionEntity(CommentReqModel commentReqModel) {
        this.body = commentReqModel.getBody();
        this.created = LocalDateTime.now();
    }

    public CommentQuestion toCommentQuestionModel() {
        CommentQuestion commentModel = new CommentQuestion();
        commentModel.setId(this.id);
        commentModel.setBody(this.body);
        commentModel.setDate(this.created);
        commentModel.setQuestionId(this.questionId);
        commentModel.setUserId(this.userId);
        //todo: commentModel.setUserName();
        return commentModel;
    }
}
