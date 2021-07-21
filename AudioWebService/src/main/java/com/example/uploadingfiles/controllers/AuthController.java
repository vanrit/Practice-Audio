package com.example.uploadingfiles.controllers;

import com.example.uploadingfiles.repository.UsersRepository;
import com.example.uploadingfiles.responses.RegistrationResponse;
import com.example.uploadingfiles.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.core.userdetails.UserDetailsResourceFactoryBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
public class AuthController {

    @Autowired
    UsersRepository usersRepository;

    @javax.annotation.Resource(name = "authenticationManager")
    private AuthenticationManager authenticationManager;

    /**
     * Регистраия пользователя.
     * @param user
     * @return
     */
    @PostMapping("/signup")
    public RegistrationResponse processRegister(@RequestBody User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        if (usersRepository.findByUsername(user.getUsername()) != null) {
            return new RegistrationResponse(user.getUsername(), "User already exist!", (long) -1);
        }

        Long userId = (long) -1;

        try {
            usersRepository.save(user);
            userId = usersRepository.findByUsername(user.getUsername()).getUserId();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new RegistrationResponse(user.getUsername(), "Registration error!", (long) -1);
        }

        return new RegistrationResponse(user.getUsername(), "User registered!", user.getUserId());
    }

    /**
     * Авторизация пользователя.
     * @param username
     * @param password
     * @param request
     */
    @PostMapping("/login")
    public void login(@RequestParam("username") final String username,
                      @RequestParam("password") final String password,
                      final HttpServletRequest request){

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
    }
}
