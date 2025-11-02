package com.strux.auth_service.client;

import com.strux.auth_service.dto.UpdateKeycloakIdRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", path = "/api/users/internal")
public interface UserServiceClient {

    @PatchMapping("/{userId}/keycloak-id")
    void updateKeycloakId(
            @PathVariable("userId") String userId,
            @RequestBody UpdateKeycloakIdRequest request
    );
}
