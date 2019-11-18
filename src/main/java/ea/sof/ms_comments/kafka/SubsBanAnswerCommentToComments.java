package ea.sof.ms_comments.kafka;

import com.google.gson.Gson;
import ea.sof.ms_comments.entity.CommentAnswerEntity;
import ea.sof.ms_comments.entity.CommentQuestionEntity;
import ea.sof.ms_comments.repository.CommentAnswerRepository;
import ea.sof.ms_comments.repository.CommentQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SubsBanAnswerCommentToComments {
    @Autowired
    CommentAnswerRepository commentAnswerRepository;

    @KafkaListener(topics = "${topicBanAnswerComment}", groupId = "${subsBanAnswerCommentToComments}")
    public void listener(String message) {
        System.out.println("\nSubsBanAnswerCommentToComments :: New message from topic 'topicBanAnswerComment': " + message);
        String answerCommentId = "";
        try {
            Gson gson = new Gson();
            answerCommentId = gson.fromJson(message, String.class);
        } catch (Exception ex) {
            System.out.println("SubsBanAnswerCommentToComments :: Failed to convert Json: " + ex.getMessage());
        }

        //CommentQuestionEntity questionEntity = commentQuestionRepository.findById(questionCommentId).orElse(null);
        CommentAnswerEntity commentAnswerEntity = commentAnswerRepository.findById(answerCommentId).orElse(null);
        if (commentAnswerEntity == null) {
            System.out.println("SubsBanAnswerCommentToComments :: Failed to retrieve Entity.");
            return;
        }

        try {
            commentAnswerEntity.setActive(0);
            commentAnswerRepository.save(commentAnswerEntity);
            System.out.println("SubsBanAnswerCommentToComments :: Comment banned");
        } catch (Exception ex){
            System.out.println("SubsBanAnswerCommentToComments :: Failed to ban Comment: " + ex.getMessage());
        }
    }
}
