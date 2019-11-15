package ea.sof.ms_comments.controller;


import ea.sof.ms_comments.entity.CommentAnswerEntity;
import ea.sof.ms_comments.entity.CommentQuestionEntity;
import ea.sof.ms_comments.model.CommentReqModel;
import ea.sof.ms_comments.repository.CommentAnswerRepository;
import ea.sof.ms_comments.repository.CommentQuestionRepository;
import ea.sof.shared.models.CommentAnswer;
import ea.sof.shared.models.CommentQuestion;
import ea.sof.shared.models.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    @Autowired
    CommentAnswerRepository commentAnswerRepository;

    @Autowired
    CommentQuestionRepository commentQuestionRepository;

    @GetMapping("/questions/{questionId}")
    public ResponseEntity<?> getAllCommentsByQuestionId(@PathVariable("questionId") String questionId) {
        List<CommentQuestionEntity> commentQuestionEntities = commentQuestionRepository.findCommentQuestionEntitiesByQuestionId(questionId);
        List<CommentQuestion> comments = commentQuestionEntities.stream().map(cm -> cm.toCommentQuestionModel()).collect(Collectors.toList());

        Response response = new Response(true, "");
        response.getData().put("comments", comments);

        return ResponseEntity.ok(response);
    }

    //todo: add getAllCommentsByAnswerId
    @GetMapping("/answers/{answerId}")
    public ResponseEntity<?> getAllCommentsByAnswerId(@PathVariable("answerId") String answerId) {
        List<CommentAnswerEntity> commentEntities = commentAnswerRepository.findCommentAnswerEntitiesByAnswerId(answerId);
        List<CommentAnswer> comments = commentEntities.stream().map(cm -> cm.toCommentAnswerModel()).collect(Collectors.toList());

        Response response = new Response(true, "");
        response.getData().put("comments", comments);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/questions/{questionId}")
    public ResponseEntity<?> createCommentForQuestion(@RequestBody @Valid CommentReqModel commentReqModel, @PathVariable("questionId") String questionId) {

        CommentQuestionEntity commentQuestionEntity = new CommentQuestionEntity(commentReqModel);
        commentQuestionEntity.setQuestionId(questionId);

        Response response = new Response(true, "Comment has been created");
        commentQuestionEntity = commentQuestionRepository.save(commentQuestionEntity);
        response.getData().put("comment", commentQuestionEntity);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/answers/{answerId}")
    public ResponseEntity<?> createCommentForAnswer(@RequestBody @Valid CommentReqModel commentReqModel, @PathVariable("answerId") String answerId) {

        CommentAnswerEntity commentAnswerEntity = new CommentAnswerEntity(commentReqModel);
        commentAnswerEntity.setAnswerId(answerId);

        Response response = new Response(true, "Comment has been created");
        commentAnswerEntity = commentAnswerRepository.save(commentAnswerEntity);
        response.getData().put("comment", commentAnswerEntity);
        return ResponseEntity.status(201).body(response);
    }
}