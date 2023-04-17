package tn.esprit.projetdev.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetdev.entities.User;
import tn.esprit.projetdev.services.iUserServiceImp;
import java.util.List;
import java.util.Optional;
/*
 * cette classe définit un contrôleur d'autorisation test qui contient 4 API
 * *

 */
@CrossOrigin(origins =  "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {
    private iUserServiceImp iUserServiceImp;
    //(api/test/all) pour l'accès public

    @GetMapping("/all")
    public String allAccess() {
        return "Public Content.";
    }
    //(api/test/user) pour les utilisateurs à ROLE_USER ou ROLE_VENDEUR ou ROLE_ADMIN

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('VENDEUR') or hasRole('ADMIN')")
    public String userAccess() {
        return "User Content.";
    }

    @GetMapping("/vendeur")
    @PreAuthorize("hasRole('VENDEUR')")
    public String moderatorAccess() {
        return "vendeur Board.";
    }
    //(api/test/admin) pour les utilisateurs à ROLE_ADMIN

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Admin Board.";
    }



}
