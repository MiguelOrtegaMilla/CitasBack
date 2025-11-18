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
import com.general.citas.security.AccountLocker.AccLockService;


@Service
public class UsersDetailSeviceImp implements UserDetailsService{

    @Autowired
    private AccLockService accLockService;

    @Autowired
    private UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            User user = userRepository.findByName(username)
                    .orElseThrow(() -> new UsernameNotFoundException("El usuario " + username + " no existe"))
            ;

            boolean accountNonLocked = true;
        if (accLockService != null) {
            accountNonLocked = !accLockService.isUserLocked(user.getUuid());
        }
    
            Collection<? extends GrantedAuthority> authorities = Set.of(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );
    
            return new org.springframework.security.core.userdetails.User (
                user.getName() , 
                user.getPassword(), 
                user.isEnabled(), 
                true , 
                true , 
                accountNonLocked,
                authorities
            );  
        }
}
