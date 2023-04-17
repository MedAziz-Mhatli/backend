package tn.esprit.projetdev.Interfaces;

import tn.esprit.projetdev.entities.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {

    List<User> getAllUsers();
    User addUser(User u);

    void removeUser(Long id);

    Optional<User> getUser(Long id);

    User updateUser(User u);

}
