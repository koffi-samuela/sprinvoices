package com.example.sprinvoices.security;

import com.example.sprinvoices.models.UserAccount;
import com.example.sprinvoices.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserAccountRepository userAccountRepository;

@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    UserAccount account = userAccountRepository.findByUsername(username)
            .orElseThrow(() ->
                new UsernameNotFoundException("Utilisateur introuvable : " + username)
            );

    return new User(
            account.getUsername(),
            account.getPassword(),
            List.of(new SimpleGrantedAuthority(account.getRole().getName()))
    );
}
}