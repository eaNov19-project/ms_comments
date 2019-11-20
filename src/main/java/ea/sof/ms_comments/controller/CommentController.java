package ea.sof.ms_comments.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import ea.sof.ms_comments.entity.CommentAnswerEntity;
import ea.sof.ms_comments.entity.CommentQuestionEntity;
import ea.sof.ms_comments.model.CommentReqModel;
import ea.sof.ms_comments.repository.CommentAnswerRepository;
import ea.sof.ms_comments.repository.CommentQuestionRepository;
import ea.sof.ms_comments.service.AuthServiceCircuitBreaker;
import ea.sof.shared.models.CommentAnswer;
import ea.sof.shared.models.CommentQuestion;
import ea.sof.shared.models.Response;
import ea.sof.shared.models.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceCircuitBreaker.class);

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

    @Value("${app.version}")
    private String appVersion;

    @GetMapping("/health")
    public ResponseEntity<?> index() {
        String host = "Unknown host";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>("Comments service (" + appVersion + "). Host: " + host, HttpStatus.OK);
    }


    @GetMapping("/questions/{questionId}")
    public ResponseEntity<?> getAllCommentsByQuestionId(@PathVariable("questionId") String questionId) {
        LOGGER.info("getAllCommentsByQuestionId :: questionId: " + questionId);

        List<CommentQuestionEntity> commentQuestionEntities = commentQuestionRepository.findAllCommentQuestionEntitiesByQuestionIdAndActiveEquals(questionId, 1);
        List<CommentQuestion> comments = commentQuestionEntities.stream().map(cm -> cm.toCommentQuestionModel()).collect(Collectors.toList());

        Response response = new Response(true, "");
        response.getData().put("comments", comments);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/answers/{answerId}")
    public ResponseEntity<?> getAllCommentsByAnswerId(@PathVariable("answerId") String answerId) {
        LOGGER.info("getAllCommentsByAnswerId :: answerId: " + answerId);

        List<CommentAnswerEntity> commentEntities = commentAnswerRepository.findAllCommentAnswerEntitiesByAnswerIdAndActiveEquals(answerId, 1);
        List<CommentAnswer> comments = commentEntities.stream().map(cm -> cm.toCommentAnswerModel()).collect(Collectors.toList());

        Response response = new Response(true, "");
        response.getData().put("comments", comments);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/questions/{questionId}")
    public ResponseEntity<?> createCommentForQuestion(@RequestBody @Valid CommentReqModel commentReqModel, @PathVariable("questionId") String questionId,  HttpServletRequest request) {
        LOGGER.info("Add Comment for question :: New request: " + questionId);
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
        commentQuestionEntity.setUserEmail(decodedToken.getEmail());

        //Response response = new Response(true, "Comment has been created");
        Response response = new Response();
        try {
            commentQuestionEntity = commentQuestionRepository.save(commentQuestionEntity);
            //response.getData().put("comment", commentQuestionEntity.toCommentQuestionModel());
            response = new Response(true, "Comment added for question");
            response.addObject("comment",commentQuestionEntity.toCommentQuestionModel() );
            LOGGER.info("Add Comment :: Saved successfully" + commentQuestionEntity.toString());
        } catch (Exception ex) {
            response.setSuccess(false);
            response.setMessage(ex.getMessage());
            LOGGER.warn("Add Comment for question :: Error. " + ex.getMessage());
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
            LOGGER.warn("Invalid Token::UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(false, "Invalid Token"));
        }
        //TokenUser decodedToken = (TokenUser) authCheckResp.getData().get("decoded_token");
        ObjectMapper mapper = new ObjectMapper();
        TokenUser decodedToken = mapper.convertValue(authCheckResp.getData().get("decoded_token"), TokenUser.class);
        CommentAnswerEntity commentAnswerEntity = new CommentAnswerEntity(commentReqModel);
        commentAnswerEntity.setAnswerId(answerId);
        commentAnswerEntity.setUserId(decodedToken.getUserId().toString());
        commentAnswerEntity.setUserEmail(decodedToken.getEmail());

        Response response = new Response();
        try{
            response = new Response(true, "Comment has been created");
            commentAnswerEntity = commentAnswerRepository.save(commentAnswerEntity);
            response.addObject("comment", commentAnswerEntity.toCommentAnswerModel());

            LOGGER.info("Add Comment for answer:: Saved successfully" + commentAnswerEntity.toString());
        } catch (Exception ex) {
            response.setSuccess(false);
            response.setMessage(ex.getMessage());
            LOGGER.warn("Add Comment for Answer :: Error. " + ex.getMessage());
        }

        kafkaTemplate.send(env.getProperty("topicNewAnswerComment"), gson.toJson(commentAnswerEntity));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private Response isAuthorized(String authHeader) {
        LOGGER.info("JWT :: Checking authorization... ");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.warn("Invalid token. Header null or 'Bearer ' is not provided.");
            return new Response(false, "Invalid token");
        }
        try {
            LOGGER.info("Calling authService.validateToken... ");
            ResponseEntity<Response> result = authService.validateToken(authHeader);

            LOGGER.info("AuthService replied... ");
            if (!result.getBody().getSuccess()) {
                LOGGER.warn("Filed to authorize. JWT is invalid");
                return result.getBody();
//				return new Response(false, "Invalid token");
            }

            LOGGER.info("Authorized successfully");
            return result.getBody();

        } catch (Exception e) {
            LOGGER.warn("Failed. " + e.getMessage());
            return new Response(false, "exception", e);
        }
    }
}
