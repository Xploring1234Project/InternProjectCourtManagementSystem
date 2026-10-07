package com.cognizant.JusticeServe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "citizens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Citizen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long citizenId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",unique = true,nullable = false)
    private User user;

    @Column(nullable = false,length = 100)
    private String name;

    private LocalDate dob;

    @Column(length = 10)
    private String gender;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 200)
    private String contactInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 10)
    private User.Status status;

}