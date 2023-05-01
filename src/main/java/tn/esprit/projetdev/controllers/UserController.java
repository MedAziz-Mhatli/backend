package tn.esprit.projetdev.controllers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetdev.Repository.RoleRepository;
import tn.esprit.projetdev.Repository.UserRepository;
import tn.esprit.projetdev.entities.ERole;
import tn.esprit.projetdev.entities.RefreshToken;
import tn.esprit.projetdev.entities.Role;
import tn.esprit.projetdev.entities.User;
import tn.esprit.projetdev.payload.request.SignupRequest;
import tn.esprit.projetdev.payload.response.JwtResponse;
import tn.esprit.projetdev.payload.response.MessageResponse;
import tn.esprit.projetdev.security.jwt.JwtUtils;
import tn.esprit.projetdev.security.services.RefreshTokenService;
import tn.esprit.projetdev.security.services.UserDetailsImpl;
import tn.esprit.projetdev.services.iUserServiceImp;

import javax.validation.Valid;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/user/")
@CrossOrigin(maxAge = 3600)
public class UserController {
    @Autowired
    private  iUserServiceImp iUserServiceImp;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;
    
    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    RefreshTokenService refreshTokenService;
    
    @GetMapping("/all")
    public List<User> getAll(){
        return iUserServiceImp.getAllUsers();

    }

    @GetMapping("get/{numUser}")
    public Optional<User> retrieveUser(@PathVariable Long numUser) {

        return  iUserServiceImp.getUser(numUser);

    }
    @PostMapping("/new")
    public User addUser(@RequestBody User user) {
        return iUserServiceImp.addUser(user);
    }
    @PutMapping("/update")
    public User updateUser(@RequestBody User user) {

        return  iUserServiceImp.updateUser(user);

    }
    @DeleteMapping("/delete/{numUser}")
    public void removeUser(@PathVariable Long numUser){

        iUserServiceImp.removeUser(numUser);
    }


    @PutMapping("/userEdit")
    @ResponseBody
    public ResponseEntity<?> editAccount(@Valid @RequestBody SignupRequest signUpRequest) throws IOException {
    	Optional<User> us = iUserServiceImp.getUser(signUpRequest.getId());
    	User user=us.get();
    	
    	
    	
    	if(!user.getUsername().equals(signUpRequest.getUsername())) {
    		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
            }
    	}
    	if(!user.getEmail().equals(signUpRequest.getEmail())) {
    		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
            }
    	}
    	
    	user.setEmail(signUpRequest.getEmail());
    	user.setUsername(signUpRequest.getUsername());
        user.setAdresse(signUpRequest.getAdresse());
        
        
        user.setFullname(signUpRequest.getFullname());
        
        if(signUpRequest.getPassword()!="") {
        	user.setPassword(encoder.encode(signUpRequest.getPassword()));
        }
        User u=userRepository.save(user);
        System.out.println(u.getFullname());
        return ResponseEntity.ok(u);
    	
    }

    public User getConnectedUser() {
        String username;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }
        User us = userRepository.findByUsername(username).orElse(null);

        return us;


    }

}
