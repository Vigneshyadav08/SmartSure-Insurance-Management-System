package in.cg.main.admin.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "policy-service")
public interface PolicyFeignClient {

    @GetMapping("/policies/purchase/user/{username}")
    List<Object> getPurchasedPolicies(@PathVariable("username") String username);

    @PutMapping("/policies/purchase/{id}/expiry")
    String expirePolicy(@PathVariable("id") Long id);
}
