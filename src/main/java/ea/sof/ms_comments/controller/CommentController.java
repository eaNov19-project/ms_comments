package ea.sof.ms_comments.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import ea.sof.ms_comments.entity.CommentAnswerEntity;
import ea.sof.ms_comments.entity.CommentQuestionEntity;
import ea.sof.ms_comments.model.CommentReqModel;
import ea.sof.ms_comments.repository.CommentAnswerRepository;
import ea.sof.ms_comments.repository.CommentQuestionRepository;
import ea.sof.ms_comments.service.AuthService;
import ea.sof.shared.models.CommentAnswer;
import ea.sof.shared.models.CommentQuestion;
import ea.sof.shared.models.Response;
import ea.sof.shared.models.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comments")
@CrossOrigin
public class CommentController {
    @Autowired
    CommentAnswerRepository commentAnswerRepository;

    @Autowired
    CommentQuestionRepository commentQuestionRepository;

    @Autowired
    AuthService authService;


    @GetMapping("/questions/{questionId}")
    public ResponseEntity<?> getAllCommentsByQuestionId(@PathVariable("questionId") String questionId) {
        List<CommentQuestionEntity> commentQuestionEntities = commentQuestionRepository.findCommentQuestionEntitiesByQuestionId(questionId);
        List<CommentQuestion> comments = commentQuestionEntities.stream().map(cm -> cm.toCommentQuestionModel()).collect(Collectors.toList());

        Response response = new Response(true, "");
        response.getData().put("comments", comments);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/answers/{answerId}")
    public ResponseEntity<?> getAllCommentsByAnswerId(@PathVariable("answerId") String answerId) {
        List<CommentAnswerEntity> commentEntities = commentAnswerRepository.findCommentAnswerEntitiesByAnswerId(answerId);
        List<CommentAnswer> comments = commentEntities.stream().map(cm -> cm.toCommentAnswerModel()).collect(Collectors.toList());

        Response response = new Response(true, "");
        response.getData().put("comments", comments);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/questions/{questionId}")
    public ResponseEntity<?> createCommentForQuestion(@RequestBody @Valid CommentReqModel commentReqModel, @PathVariable("questionId") String questionId, @RequestHeader("Authorization") String token) {

        //Check if request is authorized
        Response authCheckResp = isAuthorized(token);
        if (!authCheckResp.getSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(false, "Invalid Token"));
        }
        //TokenUser decodedToken = (TokenUser) authCheckResp.getData().get("decoded_token");

        ObjectMapper mapper = new ObjectMapper();
        TokenUser decodedToken = mapper.convertValue(authCheckResp.getData().get("decoded_token"), TokenUser.class);
        CommentQuestionEntity commentQuestionEntity = new CommentQuestionEntity(commentReqModel);
        commentQuestionEntity.setQuestionId(questionId);
        commentQuestionEntity.setUserId(decodedToken.getUserId().toString());
        //todo: setUsername
        Response response = new Response(true, "Comment has been created");
        commentQuestionEntity = commentQuestionRepository.save(commentQuestionEntity);
        response.getData().put("comment", commentQuestionEntity.toCommentQuestionModel());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/answers/{answerId}")
    public ResponseEntity<?> createCommentForAnswer(@RequestBody @Valid CommentReqModel commentReqModel, @PathVariable("answerId") String answerId, @RequestHeader("Authorization") String token) {

        //Check if request is authorized
        Response authCheckResp = isAuthorized(token);
        if (!authCheckResp.getSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(false, "Invalid Token"));
        }
        //TokenUser decodedToken = (TokenUser) authCheckResp.getData().get("decoded_token");
        ObjectMapper mapper = new ObjectMapper();
        TokenUser decodedToken = mapper.convertValue(authCheckResp.getData().get("decoded_token"), TokenUser.class);
        CommentAnswerEntity commentAnswerEntity = new CommentAnswerEntity(commentReqModel);
        commentAnswerEntity.setAnswerId(answerId);
        commentAnswerEntity.setUserId(decodedToken.getUserId().toString());
        //todo: setUsername
        Response response = new Response(true, "Comment has been created");
        commentAnswerEntity = commentAnswerRepository.save(commentAnswerEntity);
        response.getData().put("comment", commentAnswerEntity.toCommentAnswerModel());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private Response isAuthorized(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new Response(false, "Invalid token");
        }
        try {
            ResponseEntity<Response> result = authService.validateToken(authHeader);

            if (!result.getBody().getSuccess()) {
                return new Response(false, "Invalid token");
            }
            return result.getBody();

        }catch (Exception e){
            return new Response(false, "exception", e);
        }
    }
}
