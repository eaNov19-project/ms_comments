package ea.sof.ms_comments.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import ea.sof.ms_comments.entity.CommentAnswerEntity;
import ea.sof.ms_comments.entity.CommentQuestionEntity;
import ea.sof.ms_comments.model.CommentReqModel;
import ea.sof.ms_comments.repository.CommentAnswerRepository;
import ea.sof.ms_comments.repository.CommentQuestionRepository;
import ea.sof.ms_comments.service.AuthService;
import ea.sof.ms_comments.service.AuthServiceCircuitBreaker;
import ea.sof.shared.models.CommentAnswer;
import ea.sof.shared.models.CommentQuestion;
import ea.sof.shared.models.Response;
import ea.sof.shared.models.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comments")
@CrossOrigin
@Slf4j
public class CommentController {
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private Environment env;

    @Autowired
    CommentAnswerRepository commentAnswerRepository;

    @Autowired
    CommentQuestionRepository commentQuestionRepository;

    @Autowired
    AuthServiceCircuitBreaker authService;

    private Gson gson = new Gson();

    @GetMapping("/health")
    public ResponseEntity<String> index() {
        String host = "Unknown host";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>("Comments service. Host: " + host, HttpStatus.OK);
    }

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
    public ResponseEntity<?> createCommentForQuestion(@RequestBody @Valid CommentReqModel commentReqModel, @PathVariable("questionId") String questionId,  HttpServletRequest request) {
        log.info("\nAdd Comment for question :: New request: " + questionId);
        //Check if request is authorized
        Response authCheckResp = isAuthorized(request.getHeader("Authorization"));
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

        //Response response = new Response(true, "Comment has been created");
        Response response = new Response();
        try {
            commentQuestionEntity = commentQuestionRepository.save(commentQuestionEntity);
            //response.getData().put("comment", commentQuestionEntity.toCommentQuestionModel());
            response = new Response(true, "Comment added for question");
            response.addObject("comment",commentQuestionEntity.toCommentQuestionModel() );
            log.info("Add Comment :: Saved successfully" + commentQuestionEntity.toString());
        } catch (Exception ex) {
            response.setSuccess(false);
            response.setMessage(ex.getMessage());
            log.warn("Add Comment for question :: Error. " + ex.getMessage());
        }
        //sending topic::topicNewQuestionComment
        kafkaTemplate.send(env.getProperty("topicNewQuestionComment"), gson.toJson(commentQuestionEntity));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/answers/{answerId}")
    public ResponseEntity<?> createCommentForAnswer(@RequestBody @Valid CommentReqModel commentReqModel, @PathVariable("answerId") String answerId, HttpServletRequest request) {

        //Check if request is authorized
        Response authCheckResp = isAuthorized(request.getHeader("Authorization"));
        if (!authCheckResp.getSuccess()) {
            log.warn("Invalid Token::UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(false, "Invalid Token"));
        }
        //TokenUser decodedToken = (TokenUser) authCheckResp.getData().get("decoded_token");
        ObjectMapper mapper = new ObjectMapper();
        TokenUser decodedToken = mapper.convertValue(authCheckResp.getData().get("decoded_token"), TokenUser.class);
        CommentAnswerEntity commentAnswerEntity = new CommentAnswerEntity(commentReqModel);
        commentAnswerEntity.setAnswerId(answerId);
        commentAnswerEntity.setUserId(decodedToken.getUserId().toString());
        //todo: setUsername

        Response response = new Response();
        try{
            response = new Response(true, "Comment has been created");
            commentAnswerEntity = commentAnswerRepository.save(commentAnswerEntity);
            response.addObject("comment", commentAnswerEntity.toCommentAnswerModel());

            log.info("Add Comment for answer:: Saved successfully" + commentAnswerEntity.toString());
        } catch (Exception ex) {
            response.setSuccess(false);
            response.setMessage(ex.getMessage());
            log.warn("Add Comment for Answer :: Error. " + ex.getMessage());
        }

        kafkaTemplate.send(env.getProperty("topicNewAnswerComment"), gson.toJson(commentAnswerEntity));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private Response isAuthorized(String authHeader) {
        log.info("JWT :: Checking authorization... ");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Invalid token. Header null or 'Bearer ' is not provided.");
            return new Response(false, "Invalid token");
        }
        try {
            log.info("Calling authService.validateToken... ");
            ResponseEntity<Response> result = authService.validateToken(authHeader);

            log.info("AuthService replied... ");
            if (!result.getBody().getSuccess()) {
                log.warn("Filed to authorize. JWT is invalid");
                return result.getBody();
//				return new Response(false, "Invalid token");
            }

            log.info("Authorized successfully");
            return result.getBody();

        } catch (Exception e) {
            log.warn("Failed. " + e.getMessage());
            return new Response(false, "exception", e);
        }
    }
}
