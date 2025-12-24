package com.OTRAS.DemoProject.Security;

import com.OTRAS.DemoProject.Entity.Candidate;
import com.OTRAS.DemoProject.Entity.User;
import com.OTRAS.DemoProject.Repository.CandidateRepository;
import com.OTRAS.DemoProject.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // First try Candidate
        Optional<Candidate> candidateOpt = candidateRepository.findByEmail(email);
        if (candidateOpt.isPresent()) {
            Candidate candidate = candidateOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    candidate.getEmail(),
                    candidate.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CANDIDATE"))
            );
        }

        // Then try regular User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}