package tn.esprit.projetdev.payload.request;

import javax.validation.constraints.NotBlank;

public class EditProfileRequest {

    @NotBlank
    private String email;
    @NotBlank
    private String username;
    @NotBlank
    private String fullname;
    @NotBlank
    private String adresse;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
