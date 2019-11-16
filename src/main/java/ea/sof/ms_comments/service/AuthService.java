package ea.sof.ms_comments.service;

import ea.sof.shared.showcases.MsAuthShowcase;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "svc-bank", url = "http://104.154.33.123:8080/auth")
public interface AuthService extends MsAuthShowcase {
/*
    @GetMapping("/validate-token")
    Response isValidToken(@RequestHeader("Authorization") String jwt);*/

}
