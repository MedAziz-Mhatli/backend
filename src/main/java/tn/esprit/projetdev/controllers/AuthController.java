package tn.esprit.projetdev.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetdev.Repository.RoleRepository;
import tn.esprit.projetdev.Repository.UserRepository;
import tn.esprit.projetdev.entities.ERole;
import tn.esprit.projetdev.entities.RefreshToken;
import tn.esprit.projetdev.entities.Role;
import tn.esprit.projetdev.entities.User;
import tn.esprit.projetdev.exception.TokenRefreshException;
import tn.esprit.projetdev.payload.request.*;
import tn.esprit.projetdev.payload.response.JwtResponse;
import tn.esprit.projetdev.payload.response.MessageResponse;
import tn.esprit.projetdev.payload.response.TokenRefreshResponse;
import tn.esprit.projetdev.security.jwt.JwtUtils;
import tn.esprit.projetdev.security.services.RefreshTokenService;
import tn.esprit.projetdev.security.services.UserDetailsImpl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RefreshTokenService refreshTokenService;
    /*
     * authentifier {username and password}
     * met à jour SecurityContext à l'aide de l'objet d'authentification
     * génère JWT
     * obtenir un UserDetails à partir de l'objet d'authentification
     * La réponse contient des données JWT et UserDetails
     * */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new JwtResponse(jwt,
                refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getUsername()
                , userDetails.getEmail()
                , roles));
    }
    /*
     * Vérifie si username et email existe
     * crée un nouveau User (avec ROLE_USER sinon spécifier le rôle)
     * enrigistre le User dans la base de données en utilisant UserRepository
     * */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest, HttpServletRequest request) {
    	if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),signUpRequest.getAdresse(),signUpRequest.getFullname());


        String strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();
        System.out.println(strRoles);
        switch (strRoles) {
        case "admin":
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(adminRole);

            break;
        case "vendeur":
            Role modRole = roleRepository.findByName(ERole.ROLE_VENDEUR)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(modRole);

            break;
        default:
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
    }

        user.setRoles(roles);
        String verificationCode= UUID.randomUUID().toString();

        user.setVerificationCode(verificationCode);
        user.setAdresse(signUpRequest.getAdresse());
        
        User u=userRepository.save(user);
        
        //Envoi de mail de confirmation avec le code de verification
        String appUrl = request.getScheme()+"://"+request.getServerName();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(signUpRequest.getEmail());
        mailMessage.setSubject("Inscription réussie");
        mailMessage.setText("Bonjour " + signUpRequest.getUsername() + ",\n\nVotre inscription sur notre site a été effectuée avec succès." +
                " Pour vérifier votre compte, veuillez cliquer sur le lien suivant : "
                 + "http://localhost:4200/#/authentication/verification/" + verificationCode);
        
        javaMailSender.send(mailMessage);
        //return ResponseEntity.ok(new MessageResponse("User registered successfully! \n email de confirmation vous a été envoyé à l'adresse \" "+ signUpRequest.getEmail() + "\". Veuillez suivre les instructions pour vérifier votre compte."));
        return ResponseEntity.ok(u);
    }


    ////////////
    //Verfication par code
    ////////////

    @GetMapping("/signup/verify")
    public ResponseEntity<?> verifySignUp(@RequestParam String code) {
        User user = userRepository.findByVerificationCode(code);
        if (user == null) {
            return ResponseEntity.ok(new MessageResponse("Code de vérification invalide."));
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Votre compte a été vérifié avec succès."));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();
        refreshTokenService.deleteByUserId(userId);
        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }

    ////////////
    // Envoyer un email avec code de vérification (Reset Password)
    ////////////
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPassRequest forgotPassRequest, HttpServletRequest request) {
        if (!userRepository.existsByEmail(forgotPassRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("il n'existe aucun utilisateur avec cet email, vérifiez vos données"));
        } else {
            User user = userRepository.findUserByEmail(forgotPassRequest.getEmail());
            // Générer un code de vérification aléatoire

            String code = UUID.randomUUID().toString();

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(forgotPassRequest.getEmail());
            mailMessage.setSubject("Password Verification");

            mailMessage.setText(" Bonjour vous trouvez ci-joint un code de vérification pour réinitialiser votre mot de passe : "+ 
            "http://localhost:4200/#/authentication/reset/" + code +"/"+forgotPassRequest.getEmail());
            javaMailSender.send(mailMessage);

            // Enregistrer le code de vérification dans la base de données
            user.setResetpasswordcode(code);
            user.setVerified(false);

            userRepository.save(user);


            return ResponseEntity.ok().body(new MessageResponse("Verification code sent successfully!"));
        }
    }

    @GetMapping("/verifyCodeReset")
    public ResponseEntity<?> verifyCodeReset(@RequestParam String email, @RequestParam String code) {
    	User user = userRepository.findUserByEmail(email);
    	if(user==null) {
    		return ResponseEntity.ok(new MessageResponse("invalid"));
    	}
        // Vérifier si le code de vérification est valide
        if (user.getResetpasswordcode()!=null) {
        	if(!user.getResetpasswordcode().equals(code)) {
        		return ResponseEntity.ok(new MessageResponse("invalid"));
        	}
        }
        else {
        	return ResponseEntity.ok(new MessageResponse("invalid"));
        }
        return ResponseEntity.ok(new MessageResponse("valid"));
    }
    ////////////
    //Modification de Password
    ////////////

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPassRequest resetPassRequest) {
        // Vérifier si l'utilisateur existe
        /*if (!userRepository.existsByEmail(resetPassRequest.getEmail()) ){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("il n'existe aucun utilisateur avec cet email, vérifiez vos données"));
        }*/
        User user = userRepository.findUserByEmail(resetPassRequest.getEmail());


        // Vérifier si le code de vérification est valide
        /*if (!user.getResetpasswordcode().equals(resetPassRequest.getResetpasswordcode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Invalid verification code!"));
        }*/

        // Enregistrer le nouveau mot de passe dans la base de données
        user.setPassword(encoder.encode(resetPassRequest.getPassword()));
        user.setVerified(true);
        user.setResetpasswordcode(null);
        userRepository.save(user);

        return ResponseEntity.ok().body(new MessageResponse("Password reset successfull!"));
    }


}
