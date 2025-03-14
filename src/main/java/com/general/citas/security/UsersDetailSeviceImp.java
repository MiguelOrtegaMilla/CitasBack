package com.general.citas.security;

import java.util.Collection;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.general.citas.model.User;
import com.general.citas.repository.UserRepository;
@Service
public class UsersDetailSeviceImp implements UserDetailsService{

    @Autowired
    private UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            User user = userRepository.findByName(username)
                    .orElseThrow(() -> new UsernameNotFoundException("El usuario " + username + " no existe"))
            ;
    
            Collection<? extends GrantedAuthority> authorities = Set.of(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );
    
            return new org.springframework.security.core.userdetails.User (
                user.getName() , 
                user.getPassword(), 
                true , true , true , true ,
                authorities
            );  
        }
}
