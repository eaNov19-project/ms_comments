package ea.sof.ms_comments.controller;


import ea.sof.ms_comments.entity.CommentEntity;
import ea.sof.ms_comments.model.CommentReqModel;
import ea.sof.ms_comments.repository.CommentRepository;
import ea.sof.shared.models.Answer;
import ea.sof.shared.models.Comment;
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
    CommentRepository commentRepository;

    @GetMapping("/question/{questionId}")
    public ResponseEntity<?> getAllCommentsByQuestionId(@PathVariable("questionId") String questionId) {
        List<CommentEntity> commentEntities = commentRepository.findCommentEntitiesByQuestionId(questionId);
        List<Comment> comments = commentEntities.stream().map(cm -> cm.toCommentModel()).collect(Collectors.toList());

        Response response = new Response(true, "");
        response.getData().put("comments", comments);

        return ResponseEntity.ok(response);
    }

    //todo: add getAllCommentsByAnswerId

    @PostMapping("/question/{questionId}")
    public ResponseEntity<?> createCommentForQuestion(@RequestBody @Valid CommentReqModel commentReqModel, @PathVariable("questionId") String questionId) {

        CommentEntity commentEntity = new CommentEntity(commentReqModel);
        commentEntity.setQuestionId(questionId);

        Response response = new Response(true, "Comment has been created");
        commentEntity = commentRepository.save(commentEntity);
        response.getData().put("comment", commentEntity);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/answer/{answerId}")
    public ResponseEntity<?> createCommentForAnswer(@RequestBody @Valid CommentReqModel commentReqModel, @PathVariable("answerId") String answerId) {

        CommentEntity commentEntity = new CommentEntity(commentReqModel);
        commentEntity.setAnswerId(answerId);

        Response response = new Response(true, "Comment has been created");
        commentEntity = commentRepository.save(commentEntity);
        response.getData().put("comment", commentEntity);
        return ResponseEntity.status(201).body(response);
    }
}
