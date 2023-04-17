package tn.esprit.projetdev.controllers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetdev.entities.User;
import tn.esprit.projetdev.services.iUserServiceImp;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@RestController
@RequestMapping("/user/")
@CrossOrigin(origins =  "*", maxAge = 3600)
public class UserController {
    @Autowired
    private  iUserServiceImp iUserServiceImp;

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

}
