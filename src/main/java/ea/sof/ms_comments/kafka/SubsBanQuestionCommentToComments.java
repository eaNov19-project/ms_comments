package ea.sof.ms_comments.kafka;

import com.google.gson.Gson;

import ea.sof.ms_comments.entity.CommentQuestionEntity;
import ea.sof.ms_comments.repository.CommentQuestionRepository;


import ea.sof.ms_comments.service.AuthServiceCircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SubsBanQuestionCommentToComments {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceCircuitBreaker.class);

	@Autowired
	CommentQuestionRepository commentQuestionRepository;

	@KafkaListener(topics = "${topicBanQuestionComment}", groupId = "${subsBanQuestionCommentToComments}")
	public void listener(String message) {
		LOGGER.info("SubsBanQuestionCommentToComments :: New message from topic 'topicBanQuestionComment': " + message);

		//CommentQuestionEntity commentQuestionEntity = null;
		String questionCommentId = "";
		try {
			Gson gson = new Gson();
			questionCommentId = gson.fromJson(message, String.class);
		} catch (Exception ex) {
			LOGGER.error("SubsBanQuestionCommentToComments :: Failed to convert Json: " + ex.getMessage());
			return;
		}

		//CommentQuestionEntity questionEntity = commentQuestionRepository.findById(questionCommentId).orElse(null);
		CommentQuestionEntity commentQuestionEntity = commentQuestionRepository.findById(questionCommentId).orElse(null);
		if (commentQuestionEntity == null) {
			LOGGER.error("SubsBanQuestionCommentToComments :: Failed to retrieve Entity.");
			return;
		}

		try {
			commentQuestionEntity.setActive(0);
			commentQuestionRepository.save(commentQuestionEntity);
			LOGGER.info("SubsBanQuestionCommentToComments :: Comment banned");
		} catch (Exception ex){
			LOGGER.error("SubsBanQuestionCommentToComments :: Failed to ban Comment: " + ex.getMessage());
		}
	}

}
