package ea.sof.ms_comments.repository;


import ea.sof.ms_comments.entity.CommentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends MongoRepository<CommentEntity, String> {
    Optional<CommentEntity> findById(String id);

    List<CommentEntity> findCommentEntitiesByQuestionId(String questionId);
    List<CommentEntity> findCommentEntitiesByAnswerId(String answerId);

}
