package tn.esprit.projetdev.controllers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetdev.Repository.RoleRepository;
import tn.esprit.projetdev.Repository.UserRepository;
import tn.esprit.projetdev.entities.User;
import tn.esprit.projetdev.payload.response.MessageResponse;
import tn.esprit.projetdev.services.iUserServiceImp;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<?> editMyAccount(@Valid @RequestBody User u ) throws IOException {
        String x = encoder.encode(u.getPassword());
        User us = getConnectedUser();
        System.out.println(x);
        System.out.println(us.getPassword());
        boolean verif = encoder.matches(u.getPassword(), us.getPassword());

        if (verif==false){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Wrong Password"));
        }
        else{

            u.setRoles(us.getRoles());
            u.setId(us.getId());
            u.setPassword(x);
            userRepository.save(u);
            return ResponseEntity.ok(new MessageResponse("User Profile changed successfully!"));

        }

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
