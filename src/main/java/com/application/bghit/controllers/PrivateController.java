package com.application.bghit.controllers;

import com.application.bghit.dtos.MessageDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PrivateController {

    @GetMapping("/messages")
    public ResponseEntity<MessageDto> privateMessages() {
        return ResponseEntity.ok(new MessageDto("private content Done"));
    }
}
