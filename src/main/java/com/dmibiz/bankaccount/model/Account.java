package com.dmibiz.bankaccount.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.Check;
import lombok.*;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Check is deprecated, but use it for simplicity instead of writing a separate SQL migration for that
@Check(
        constraints = "LENGTH(identification) = 7 AND identification ~ '^[0-9]+$'"
)
public class Account {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Pattern(regexp = "\\d{7}", message = "Identification must be exactly 7 digits")
    @Column(name = "identification", nullable = false, unique = true)
    private String identification;
}
