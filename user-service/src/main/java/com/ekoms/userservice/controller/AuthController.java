package com.ekoms.userservice.controller;

import com.ekoms.userservice.dto.AuthRequest;
import com.ekoms.userservice.dto.AuthResponse;
import com.ekoms.userservice.dto.UserDto;
import com.ekoms.userservice.model.User;
import com.ekoms.userservice.security.JwtTokenUtil;
import com.ekoms.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          UserDetailsService userDetailsService,
                          JwtTokenUtil jwtTokenUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto userDto) {
        try {
            User user = userService.registerUser(userDto);
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtTokenUtil.generateToken(userDetails);

            AuthResponse response = AuthResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .token(token)
                    .role(user.getRole().name())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            System.out.println("Attempting to authenticate user: " + authRequest.getUsername());

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            System.out.println("Authentication successful");

            User user = userService.findByUsername(authRequest.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtTokenUtil.generateToken(userDetails);

            AuthResponse response = AuthResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .token(token)
                    .role(user.getRole().name())
                    .build();

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            System.out.println("Authentication failed: Bad credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (Exception e) {
            System.out.println("Authentication failed with exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentication error: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("user service is alive");
    }
}
