package com.application.bghit.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(name="app_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,name = "last_name")
    private String lastName;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Builder.Default
    private String picture = "default/default-image.jpg";

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;


    @Column(name = "is_email_validated")
    private boolean isEmailValidated = false;

    @Builder.Default
    private String telephone = "";

    @Builder.Default
    private String adresse = "";

    @Builder.Default
    private Double latitude = 0.0;

    @Builder.Default
    private Double longitude = 0.0;

    @Builder.Default
    private Double rating = 0.0;

    @OneToMany // Utilisez @ManyToOne ou @ManyToMany selon votre cas d'usage
    @Builder.Default
    private List<Rating> ratings = new ArrayList<>();

    @Builder.Default
    private Date dateInscription = new Date();

    @Builder.Default
    private boolean isParticulier = false;

    @Builder.Default
    private String aboutMe = "";

    @Builder.Default
    private Timestamp reponseDelai = new Timestamp(System.currentTimeMillis());

    @Builder.Default
    private boolean profileCompleted = false;

    @Builder.Default
    private int affairesConcluses = 0;

    @Builder.Default
    private boolean disponibleALHeure = true;

    @Embedded
    @Builder.Default
    private Verified verified = new Verified(false, false, false, false);

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.HORS_LIGNE;

    @ManyToMany
    @Builder.Default
    private List<Demande> favoris = new ArrayList<>();

    @OneToMany
    @Builder.Default
    private List<Search> searchFavoris = new ArrayList<>(); // Assurez-vous que Search a un constructeur par défaut

    @OneToMany
    @Builder.Default
    private List<PhotoCollection> photos = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "settings_id", referencedColumnName = "id")
    private Settings settings;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateCreated DESC")
    private List<Notification> notifications;


    private String resetPasswordToken;

    public void addPhotoCollection(PhotoCollection image) {
        photos.add(image);
    }

    public void removeImage(PhotoCollection image) {
        photos.remove(image);
    }


    @Override
    public String toString() {
        // Replace "notifications" with any other fields you want to display in toString()
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                "}";
    }
}


