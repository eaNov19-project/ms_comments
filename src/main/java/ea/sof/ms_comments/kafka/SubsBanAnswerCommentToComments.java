package ea.sof.ms_comments.kafka;

import com.google.gson.Gson;
import ea.sof.ms_comments.entity.CommentAnswerEntity;
import ea.sof.ms_comments.repository.CommentAnswerRepository;
import ea.sof.ms_comments.service.AuthServiceCircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SubsBanAnswerCommentToComments {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceCircuitBreaker.class);

    @Autowired
    CommentAnswerRepository commentAnswerRepository;

    @KafkaListener(topics = "${topicBanAnswerComment}", groupId = "${subsBanAnswerCommentToComments}")
    public void listener(String message) {
        LOGGER.info("SubsBanAnswerCommentToComments :: New message from topic 'topicBanAnswerComment': " + message);
        String answerCommentId = "";
        try {
            //Gson gson = new Gson();
            //answerCommentId = gson.fromJson(message, String.class);
            answerCommentId = message;
        } catch (Exception ex) {
            LOGGER.error("SubsBanAnswerCommentToComments :: Failed to convert Json: " + ex.getMessage());
            return;
        }

        //CommentQuestionEntity questionEntity = commentQuestionRepository.findById(questionCommentId).orElse(null);
        CommentAnswerEntity commentAnswerEntity = commentAnswerRepository.findById(answerCommentId).orElse(null);
        if (commentAnswerEntity == null) {
            LOGGER.error("SubsBanAnswerCommentToComments :: Failed to retrieve Entity.");
            return;
        }

        try {
            commentAnswerEntity.setActive(0);
            commentAnswerRepository.save(commentAnswerEntity);
            LOGGER.info("SubsBanAnswerCommentToComments :: Comment banned");
        } catch (Exception ex){
            LOGGER.error("SubsBanAnswerCommentToComments :: Failed to ban Comment: " + ex.getMessage());
        }
    }
}
