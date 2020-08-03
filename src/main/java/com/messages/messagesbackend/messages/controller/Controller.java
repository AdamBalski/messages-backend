package com.messages.messagesbackend.messages.controller;

import com.messages.messagesbackend.messages.dao.Dao;
import com.messages.messagesbackend.messages.dto.AddMessageDto;
import com.messages.messagesbackend.messages.dto.JwtDto;
import com.messages.messagesbackend.messages.dto.MessageDto;
import com.messages.messagesbackend.messages.dto.UserDto;
import com.messages.messagesbackend.messages.security.SecurityConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin(allowCredentials = "true")
public class Controller {
    private final Dao dao;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public Controller(Dao dao, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.dao = dao;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(value = "api/messages/{username2}")
    public List<MessageDto> getMessages(@PathVariable String username2) {
        String username1 = getUsername();

        // 204 if username1 is not a friend of username2
        if (!dao.getFriends(username1).contains(username2)) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }

        return dao.getMessages(username1, username2);
    }

    @GetMapping(value = "api/friends")
    public List<String> getFriendList() {
        return dao.getFriends(getUsername());
    }

    @PostMapping("/api/add-message")
    public void addMessage(@RequestBody AddMessageDto dto) {
        System.out.println(dto);
        dao.addMessage(getUsername(), dto.getTo(), dto.getMessage());
    }


    @PostMapping("/api/authenticate")
    public ResponseEntity<JwtDto> authenticate(@RequestBody UserDto userDto) {
        String username = userDto.getUsername();
        String password = userDto.getPassword();

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if(!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        JwtDto token = SecurityConfiguration
                .jwtUtil
                .generateJwtDto(username);

        return new ResponseEntity<>(
                token,
                HttpStatus.OK);
    }

    @GetMapping("/api/is-logged-in")
    public int isLoggedIn() {
        return 0;
    }

    private String getUsername() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }
}

