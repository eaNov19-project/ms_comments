package ea.sof.ms_comments.entity;

import ea.sof.ms_comments.model.CommentReqModel;
import ea.sof.shared.models.CommentAnswer;
import ea.sof.shared.queue_models.CommentAnswerQueueModel;
import ea.sof.shared.queue_models.CommentQuestionQueueModel;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Document(collection = "answer_comments")
public class CommentAnswerEntity {
    @Id
    private String id;
    private String userId;
    private String body;
    private LocalDateTime created;
    private String answerId;
    private Integer active;

    public CommentAnswerEntity(CommentReqModel commentReqModel) {
        this.body = commentReqModel.getBody();
        this.created = LocalDateTime.now();
    }

    public CommentAnswer toCommentAnswerModel() {
        CommentAnswer commentModel = new CommentAnswer();
        commentModel.setId(this.id);
        commentModel.setBody(this.body);
        commentModel.setCreated(this.created);
        commentModel.setAnswerId(this.answerId);
        commentModel.setUserId(this.userId);
        //todo: commentModel.setUserName();
        commentModel.setActive(this.active);
        return commentModel;
    }

    public CommentAnswerQueueModel toCommentAnswerQueueModel() {
        CommentAnswerQueueModel commentAnswerQueueModel = new CommentAnswerQueueModel();
        commentAnswerQueueModel.setId(this.id);
        commentAnswerQueueModel.setBody(this.body);
        commentAnswerQueueModel.setActive(this.active);
        return commentAnswerQueueModel;
    }
}
