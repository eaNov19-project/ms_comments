package ea.sof.ms_comments.kafka;

import com.google.gson.Gson;

import ea.sof.ms_comments.entity.CommentQuestionEntity;
import ea.sof.ms_comments.repository.CommentQuestionRepository;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SubsBanQuestionCommentToComments {

	@Autowired
	CommentQuestionRepository commentQuestionRepository;

	@KafkaListener(topics = "${topicBanQuestionComment}", groupId = "${subsBanQuestionCommentToComments}")
	public void listener(String message) {
		System.out.println("\nSubsBanQuestionCommentToComments :: New message from topic 'topicBanQuestionComment': " + message);

		//CommentQuestionEntity commentQuestionEntity = null;
		String questionCommentId = "";
		try {
			Gson gson = new Gson();
			questionCommentId = gson.fromJson(message, String.class);
		} catch (Exception ex) {
			System.out.println("SubsBanQuestionCommentToComments :: Failed to convert Json: " + ex.getMessage());
		}

		//CommentQuestionEntity questionEntity = commentQuestionRepository.findById(questionCommentId).orElse(null);
		CommentQuestionEntity commentQuestionEntity = commentQuestionRepository.findById(questionCommentId).orElse(null);
		if (commentQuestionEntity == null) {
			System.out.println("SubsBanQuestionCommentToComments :: Failed to retrieve Entity.");
			return;
		}

		try {
			commentQuestionEntity.setActive(0);
			commentQuestionRepository.save(commentQuestionEntity);
			System.out.println("SubsBanQuestionCommentToComments :: Comment banned");
		} catch (Exception ex){
			System.out.println("SubsBanQuestionCommentToComments :: Failed to ban Comment: " + ex.getMessage());
		}
	}

}
