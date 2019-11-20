package ea.sof.ms_comments.entity;

import ea.sof.ms_comments.model.CommentReqModel;
import ea.sof.shared.models.CommentQuestion;
import ea.sof.shared.queue_models.CommentQuestionQueueModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Document(collection = "question_comments")
public class CommentQuestionEntity {
    @Id
    private String id;
    private String userId;
    private String userEmail;
    private String body;
    private LocalDateTime created;
    private String questionId;
    private Integer active = 1;

    public CommentQuestionEntity(CommentReqModel commentReqModel) {
        this.body = commentReqModel.getBody();
        this.created = LocalDateTime.now();
    }

    public CommentQuestion toCommentQuestionModel() {
        CommentQuestion commentQuestionModel = new CommentQuestion();
        commentQuestionModel.setId(this.id);
        commentQuestionModel.setBody(this.body);
        commentQuestionModel.setCreated(this.created);
        commentQuestionModel.setQuestionId(this.questionId);
        commentQuestionModel.setUserId(this.userId);
        commentQuestionModel.setUserEmail(this.userEmail);
        commentQuestionModel.setActive(this.active);
        return commentQuestionModel;
    }

    public CommentQuestionQueueModel toCommentQuestionQueueModel() {
        CommentQuestionQueueModel commentQuestionQueueModel = new CommentQuestionQueueModel();
        commentQuestionQueueModel.setId(this.id);
        commentQuestionQueueModel.setBody(this.body);
        commentQuestionQueueModel.setActive(this.active);
        return commentQuestionQueueModel;
    }
}
