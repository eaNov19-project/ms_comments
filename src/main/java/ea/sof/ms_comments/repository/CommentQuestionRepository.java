package ea.sof.ms_comments.repository;


import ea.sof.ms_comments.entity.CommentAnswerEntity;
import ea.sof.ms_comments.entity.CommentQuestionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CommentQuestionRepository extends MongoRepository<CommentQuestionEntity, String> {
    Optional<CommentQuestionEntity> findById(String id);

    List<CommentQuestionEntity> findCommentQuestionEntitiesByQuestionId(String questionId);

}
