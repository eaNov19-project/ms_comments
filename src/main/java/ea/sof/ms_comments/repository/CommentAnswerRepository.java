package ea.sof.ms_comments.repository;


import ea.sof.ms_comments.entity.CommentAnswerEntity;
import ea.sof.shared.entities.CommentQuestionEntity;
import ea.sof.shared.models.CommentAnswer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CommentAnswerRepository extends MongoRepository<CommentAnswerEntity, String> {
    Optional<CommentAnswerEntity> findById(String id);

    List<CommentAnswerEntity> findCommentAnswerEntitiesByAnswerIdAndActiveEquals(String answerId, Integer active);

}
