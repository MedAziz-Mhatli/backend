package tn.esprit.projetdev.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.projetdev.Interfaces.IUserService;
import tn.esprit.projetdev.Repository.RoleRepository;
import tn.esprit.projetdev.Repository.UserRepository;
import tn.esprit.projetdev.entities.User;

import java.util.List;
import java.util.Optional;
@Service

public class iUserServiceImp implements IUserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User addUser(User u) {
        return userRepository.save(u);
    }

    @Override
    public void removeUser(Long id) {
        userRepository.deleteById(id);

    }

    @Override
    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User updateUser(User u) {
        return userRepository.save(u);
    }
}
