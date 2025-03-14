package com.general.citas.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.general.citas.DTO.UserRequestDTO;
import com.general.citas.DTO.UsersDetailsResponseDTO;
import com.general.citas.converter.UserConverter;
import com.general.citas.model.User;
import com.general.citas.model.User.Role;
import com.general.citas.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserConverter userConverter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UsersDetailsResponseDTO createUser(UserRequestDTO requestDTO) {
            if (userRepository.existsByName(requestDTO.getName())) {
                throw new RuntimeException("Username already exists");
            }
            if (userRepository.existsByEmail(requestDTO.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
    
            User user = userConverter.userRequestDTOtoUserEntity(requestDTO);
    
            user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
    
            user.setRole(Role.USER);
    
            userRepository.save(user);

            return userConverter.userEntityToUserDetailResponseDTO(user);
    }

    public UsersDetailsResponseDTO getUserById(String uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + uuid));
        return userConverter.userEntityToUserDetailResponseDTO(user);
    }

    public List<UsersDetailsResponseDTO> getAllUsers() {
         return userRepository.findAll().stream()
                .map(userConverter::userEntityToUserDetailResponseDTO)
                .collect(Collectors.toList());
    }

    public UsersDetailsResponseDTO updateUser(String uuid, UserRequestDTO requestDTO) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("User not found with uuid: " + uuid));

        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());
            
        if (requestDTO.getPassword() != null && !requestDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        }
            
        user.setRole(user.getRole());

        User updatedUser = userRepository.save(user);
        return userConverter.userEntityToUserDetailResponseDTO(updatedUser);
    }

    public void deleteUser(String uuid) {

        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + uuid));

        Long id = user.getId();

        userRepository.deleteById(id);
    }
        
}
