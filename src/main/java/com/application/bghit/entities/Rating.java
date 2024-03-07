package com.application.bghit.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name="user_rating")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    private String comment ="";

    @Builder.Default
    private String category ="";

    @Column(nullable = false,name = "rater_id")
    private Long raterId;

    @Column(nullable = false,name = "user_id")
    private Long userId;

    @Column(nullable = false,name = "rater_name")
    private String raterName;

    @Column(nullable = false,name = "rater_picture")
    private String raterPicture;

    @Column(nullable = false)
    private Date timestamp = new Date();
}