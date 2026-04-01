package com.backend.usuarios.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- DATOS PERSONALES ---
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String nombre;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 18, message = "Edad mínima 18")
    @Max(value = 100, message = "Edad máxima 100")
    private Integer edad;

    @NotBlank(message = "El género es obligatorio")
    private String genero;

    // --- CONTACTO ---
    @Email(message = "Correo inválido")
    @NotBlank(message = "El correo es obligatorio")
    @Column(unique = true)
    private String correo;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    // --- EXTRA ---
    private String fotoUrl; // URL imagen perfil

    private String ocupacion; // opcional

    private String direccion; // opcional
}