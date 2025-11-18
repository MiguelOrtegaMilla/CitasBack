package com.general.citas.DTO;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserRequestDTO {

   @NotBlank
   private String name;

   @NotBlank
   @Email
   private String email;

   @NotBlank
   private String password; // Cifrar en la capa de seguridad

   @NotBlank
   private String phone;
}

//DTO que recibe informacion del frontend, este es usado para crear usuarios