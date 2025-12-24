//package com.OTRAS.DemoProject.Controller;
// 
//import java.io.IOException;
//
//import java.time.LocalDateTime;
//
//import java.util.Map;
//
//import java.util.Optional;
// 
//import org.springframework.beans.factory.annotation.Autowired;
//
//import org.springframework.http.ResponseEntity;
//
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import org.springframework.web.bind.annotation.GetMapping;
//
//import org.springframework.web.bind.annotation.PostMapping;
//
//import org.springframework.web.bind.annotation.RequestBody;
//
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import org.springframework.web.bind.annotation.RequestParam;
//
//import org.springframework.web.bind.annotation.RestController;
// 
//import com.OTRAS.DemoProject.DTO.PasswordResetRequest;
//
//import com.OTRAS.DemoProject.DTO.ProfileRequest;
//
//import com.OTRAS.DemoProject.DTO.SignupRequest;
//
//import com.OTRAS.DemoProject.Entity.Candidate;
//
//import com.OTRAS.DemoProject.Entity.User;
//import com.OTRAS.DemoProject.Repository.CandidateProfileRepository;
//import com.OTRAS.DemoProject.Repository.CandidateRepository;
//
//import com.OTRAS.DemoProject.Repository.UserRepository;
//
//import com.OTRAS.DemoProject.Security.JwtService;
//
//import com.OTRAS.DemoProject.Service.CandidateService;
//
//import com.OTRAS.DemoProject.Service.OtpService;
//import com.OTRAS.DemoProject.Service.UserService;
// 
// 
//@RestController
//
//@RequestMapping("/api/auth")
//
//public class UserController {
// 
//    @Autowired
//
//    private UserService userService;
// 
//    @Autowired
//
//    private UserRepository userRepository;
// 
//    @Autowired
//
//    private PasswordEncoder passwordEncoder;
// 
//    @Autowired
//
//    private OtpService otpService;
// 
//    @Autowired
//
//    private JwtService jwtService;
// 
//
//    @PostMapping("/send-email-otp")
//
//    public ResponseEntity<String> sendEmailOtp(@RequestBody Map<String, String> req) {
//
//        String result = otpService.sendOtp(req.get("email"), "EMAIL");
//
//        return ResponseEntity.ok(result);
//
//    }
// 
//
//    @PostMapping("/send-mobile-otp")
//
//    public ResponseEntity<String> sendMobileOtp(@RequestBody Map<String, String> req) {
//
//        String result = otpService.sendOtp(req.get("mobile"), "MOBILE");
//
//        return ResponseEntity.ok(result);
//
//    }
// 
//
//    @PostMapping("/verify-email-otp")
//
//    public ResponseEntity<?> verifyEmailOtp(@RequestBody Map<String, String> req) {
//
//        String result = otpService.verifyOtp(req.get("email"), req.get("otp"), "EMAIL");
//
//        return ResponseEntity.ok(Map.of("message", result));
//
//    }
// 
//
//    @PostMapping("/verify-mobile-otp")
//
//    public ResponseEntity<?> verifyMobileOtp(@RequestBody Map<String, String> req) throws IOException {
//
//        String result = otpService.verifyOtp(req.get("mobile"), req.get("otp"), "MOBILE");
//
//        return ResponseEntity.ok(Map.of("message", result));
//
//    }
// 
//
//    @PostMapping("/register")
//
//    public ResponseEntity<String> registerUser(@RequestBody Map<String, String> req) {
//
//        return ResponseEntity.ok(userService.registerUser(
//
//                req.get("email"),
//
//                req.get("emailOtp"),
//
//                req.get("mobile"),
//
//                req.get("mobileOtp"),
//
//                req.get("username"),
//
//                req.get("password"),
//
//                req.get("confirmPassword")
//
//        ));
//
//    }
// 
//
//    @PostMapping("/login")
//
//    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
//
//        String email = req.get("email");
//
//        String password = req.get("password");
// 
//        User user = userRepository.findByEmail(email)
//
//                .orElseThrow(() -> new RuntimeException("User not found"));
// 
//        if (!passwordEncoder.matches(password, user.getPassword())) {
//
//            throw new RuntimeException("Invalid password");
//
//        }
// 
//        user.setLastLoginTime(LocalDateTime.now());
//
//        userRepository.save(user);
// 
//        String jwtToken = jwtService.generateToken(
//
//                user.getEmail(),
//
//                Map.of("userId", user.getId(), "mobileNumber", user.getMobileNumber())
//
//        );
// 
//        return ResponseEntity.ok(Map.of(
//
//                "userId", user.getId(),
//
//                "userName", user.getUsername(),
//
//                "mobileNumber", user.getMobileNumber(),
//
//                "jwtToken", jwtToken,
//
//                "lastLogin", user.getLastLoginTime()
//
//        ));
//
//    }
//
//    @Autowired
//
//    private CandidateService candidateService;
//
//    @PostMapping("/candidateSignup")
//
//    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
//
//        try {
//
//            Candidate candidate = candidateService.signup(req);
//
//            return ResponseEntity.ok(Map.of(
//
//                "message", "Sign up successful! Please login.",
//
//                "email", candidate.getEmail()
//
//            ));
//
//        } catch (Exception e) {
//
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//
//        }
//
//    }
// 
//    @Autowired
//
//    private CandidateRepository candidateRepo;
//
//    @PostMapping("/candidateLogin")
//
//    public ResponseEntity<?> candidateLogin(@RequestBody Map<String, String> req) {
//
//        String email = req.get("email");
//
//        String password = req.get("password");
// 
//
//        Optional<Candidate> candidateOpt = candidateRepo.findByEmail(email);
//
//        if (candidateOpt.isPresent()) {
//
//            Candidate candidate = candidateOpt.get();
//
//            if (!passwordEncoder.matches(password, candidate.getPassword())) {
//
//                throw new RuntimeException("Invalid credentials");
//
//            }
//
//            candidate.setLastLogin(LocalDateTime.now());
//
//            candidateRepo.save(candidate);
// 
//            String jwt = jwtService.generateToken(email, Map.of("role", "CANDIDATE", "id", candidate.getId()));
//
//            return ResponseEntity.ok(Map.of(
//
//                "message", "Login successful",
//
//                "jwt", jwt,
//
//                "role", "CANDIDATE",
//
//                "name", candidate.getFullName(),
//                
//                "candidateId", candidate.getId()
//
//            ));
//
//        }
// 
//        User user = userRepository.findByEmail(email)
//
//                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
// 
//        if (!passwordEncoder.matches(password, user.getPassword())) {
//
//            throw new RuntimeException("Invalid credentials");
//
//        }
// 
//        user.setLastLoginTime(LocalDateTime.now());
//
//        userRepository.save(user);
// 
//        String jwt = jwtService.generateToken(email, Map.of("role", "USER", "id", user.getId()));
//
//        return ResponseEntity.ok(Map.of(
//
//            "message", "Login successful",
//
//            "jwt", jwt,
//
//            "role", "USER",
//
//            "name", user.getUsername()
//
//        ));
//
//    }
//
//
//
//    @PostMapping("/forgot-password")
//
//    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> req) {
//
//        String email = req.get("email");
// 
//
//        boolean exists = candidateRepo.existsByEmail(email) || userRepository.existsByEmail(email);
//
//        if (!exists) {
//
//            return ResponseEntity.badRequest().body(Map.of("error", "Email not registered"));
//
//        }
// 
//
//        String result = otpService.sendOtp(email, "EMAIL");
//
//        return ResponseEntity.ok(Map.of("message", result));
//
//    }
// 
//
//    @PostMapping("/reset-password")
//
//    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest req) {
//
//        try {
//
//
//            String otpResult = otpService.verifyOtp(req.getEmail(), req.getOtp(), "EMAIL");
//
//            if (!otpResult.contains("verified")) {
//
//                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired OTP"));
//
//            }
// 
//
//            if (!req.getNewPassword().equals(req.getConfirmPassword())) {
//
//                return ResponseEntity.badRequest().body(Map.of("error", "Passwords do not match"));
//
//            }
// 
//
//            if (candidateRepo.existsByEmail(req.getEmail())) {
//
//                Candidate candidate = candidateRepo.findByEmail(req.getEmail())
//
//                        .orElseThrow(() -> new RuntimeException("Candidate not found"));
//
//                candidate.setPassword(passwordEncoder.encode(req.getNewPassword()));
//
//                candidateRepo.save(candidate);
//
//            } else {
//
//                User user = userRepository.findByEmail(req.getEmail())
//
//                        .orElseThrow(() -> new RuntimeException("User not found"));
//
//                user.setPassword(passwordEncoder.encode(req.getNewPassword()));
//
//                userRepository.save(user);
//
//            }
// 
//            return ResponseEntity.ok(Map.of("message", "Password reset successfully! Please login."));
// 
//        } catch (Exception e) {
//
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//
//        }
//
//    }
//
//    @PostMapping("/update-token")
//    public ResponseEntity<String> updateToken(@RequestParam Long candidateId, @RequestParam String token) {
//      Candidate   student = candidateRepo .findById(candidateId).orElse(null);
//        if (student != null) {
//            student.setFcmDeviceToken(token);
//            candidateRepo.save(student);
//            return ResponseEntity.ok("Token updated successfully");
//        }
//        return ResponseEntity.badRequest().body("Student not found");
//    }
//
//    @GetMapping("/getCandidateById")
//    public ResponseEntity<?> getCandidateById(@RequestParam Long candidateId) {
//        Candidate candidate = candidateRepo.findById(candidateId).orElseThrow(()->new RuntimeException("User Not found"));
//        return ResponseEntity.ok(candidate);
//    }
//
//    @Autowired
//    private CandidateProfileRepository candidateProfileRepository;
//    @GetMapping("/getCandidateProfileByCandidateId")
//    public Long getCandidateProfileByCandidateId(@RequestParam Long candidateId) {
//    	Candidate candidateIdObj=candidateRepo.findById(candidateId).orElseThrow(()->new RuntimeException("candidate not found"));
//    	return candidateProfileRepository.findByCandidateId(candidateIdObj.getId()).getId();
//    	
//    }
//}
//
// 

package com.OTRAS.DemoProject.Controller;
 
import java.io.IOException;

import java.time.LocalDateTime;

import java.util.Map;

import java.util.Optional;
 
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;
 
import com.OTRAS.DemoProject.DTO.PasswordResetRequest;

import com.OTRAS.DemoProject.DTO.ProfileRequest;

import com.OTRAS.DemoProject.DTO.SignupRequest;

import com.OTRAS.DemoProject.Entity.Candidate;

import com.OTRAS.DemoProject.Entity.User;
import com.OTRAS.DemoProject.Repository.CandidateProfileRepository;
import com.OTRAS.DemoProject.Repository.CandidateRepository;

import com.OTRAS.DemoProject.Repository.UserRepository;

import com.OTRAS.DemoProject.Security.JwtService;
import com.OTRAS.DemoProject.Service.ActiveJwtService;
import com.OTRAS.DemoProject.Service.CandidateService;

import com.OTRAS.DemoProject.Service.OtpService;
import com.OTRAS.DemoProject.Service.UserService;
 
 
@RestController

@RequestMapping("/api/auth")

public class UserController {
 
    @Autowired

    private UserService userService;
 
    @Autowired

    private UserRepository userRepository;
 
    @Autowired

    private PasswordEncoder passwordEncoder;
 
    @Autowired

    private OtpService otpService;
 
    @Autowired

    private JwtService jwtService;
 

    @PostMapping("/send-email-otp")

    public ResponseEntity<String> sendEmailOtp(@RequestBody Map<String, String> req) {

        String result = otpService.sendOtp(req.get("email"), "EMAIL");

        return ResponseEntity.ok(result);

    }
 

    @PostMapping("/send-mobile-otp")

    public ResponseEntity<String> sendMobileOtp(@RequestBody Map<String, String> req) {

        String result = otpService.sendOtp(req.get("mobile"), "MOBILE");

        return ResponseEntity.ok(result);

    }
 

    @PostMapping("/verify-email-otp")

    public ResponseEntity<?> verifyEmailOtp(@RequestBody Map<String, String> req) {

        String result = otpService.verifyOtp(req.get("email"), req.get("otp"), "EMAIL");

        return ResponseEntity.ok(Map.of("message", result));

    }
 

    @PostMapping("/verify-mobile-otp")

    public ResponseEntity<?> verifyMobileOtp(@RequestBody Map<String, String> req) throws IOException {

        String result = otpService.verifyOtp(req.get("mobile"), req.get("otp"), "MOBILE");

        return ResponseEntity.ok(Map.of("message", result));

    }
 

    @PostMapping("/register")

    public ResponseEntity<String> registerUser(@RequestBody Map<String, String> req) {

        return ResponseEntity.ok(userService.registerUser(

                req.get("email"),

                req.get("emailOtp"),

                req.get("mobile"),

                req.get("mobileOtp"),

                req.get("username"),

                req.get("password"),

                req.get("confirmPassword")

        ));

    }
 

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String password = req.get("password");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);
        
        String jwtToken = jwtService.generateToken(
                user.getEmail(),
                Map.of("userId", user.getId(), "mobileNumber", user.getMobileNumber())
        );

        // Add this block
        String jti = jwtService.extractJti(jwtToken);
        activeJwtService.storeActiveJwt(user.getId(), jti, 86400000);

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "userName", user.getUsername(),
                "mobileNumber", user.getMobileNumber(),
                "jwtToken", jwtToken,
                "lastLogin", user.getLastLoginTime()
        ));
    }

    @Autowired

    private CandidateService candidateService;

    @PostMapping("/candidateSignup")

    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {

        try {

            Candidate candidate = candidateService.signup(req);

            return ResponseEntity.ok(Map.of(

                "message", "Sign up successful! Please login.",

                "email", candidate.getEmail()

            ));

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));

        }

    }
 
    @Autowired
    private CandidateRepository candidateRepo;
    
    @Autowired
    private ActiveJwtService activeJwtService;

    @PostMapping("/candidateLogin")
    public ResponseEntity<?> candidateLogin(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String password = req.get("password");

        // Try Candidate first
        Optional<Candidate> candidateOpt = candidateRepo.findByEmail(email);
        if (candidateOpt.isPresent()) {
            Candidate candidate = candidateOpt.get();
            if (!passwordEncoder.matches(password, candidate.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }
            candidate.setLastLogin(LocalDateTime.now());
            candidateRepo.save(candidate);

            String jwt = jwtService.generateToken(email, Map.of("role", "CANDIDATE", "id", candidate.getId()));

            // Store active JWT in Redis
            String jti = jwtService.extractJti(jwt);
            activeJwtService.storeActiveJwt(candidate.getId(), jti, 86400000);

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "jwt", jwt,
                    "role", "CANDIDATE",
                    "name", candidate.getFullName(),
                    "candidateId", candidate.getId()
            ));
        }

        // If not candidate, try regular User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        String jwt = jwtService.generateToken(email, Map.of("role", "USER", "id", user.getId()));

        // Store active JWT in Redis
        String jti = jwtService.extractJti(jwt);
        activeJwtService.storeActiveJwt(user.getId(), jti, 86400000);

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "jwt", jwt,
                "role", "USER",
                "name", user.getUsername()
        ));
    }


    @PostMapping("/forgot-password")

    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> req) {

        String email = req.get("email");
 

        boolean exists = candidateRepo.existsByEmail(email) || userRepository.existsByEmail(email);

        if (!exists) {

            return ResponseEntity.badRequest().body(Map.of("error", "Email not registered"));

        }
 

        String result = otpService.sendOtp(email, "EMAIL");

        return ResponseEntity.ok(Map.of("message", result));

    }
 

    @PostMapping("/reset-password")

    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest req) {

        try {


            String otpResult = otpService.verifyOtp(req.getEmail(), req.getOtp(), "EMAIL");

            if (!otpResult.contains("verified")) {

                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired OTP"));

            }
 

            if (!req.getNewPassword().equals(req.getConfirmPassword())) {

                return ResponseEntity.badRequest().body(Map.of("error", "Passwords do not match"));

            }
 

            if (candidateRepo.existsByEmail(req.getEmail())) {

                Candidate candidate = candidateRepo.findByEmail(req.getEmail())

                        .orElseThrow(() -> new RuntimeException("Candidate not found"));

                candidate.setPassword(passwordEncoder.encode(req.getNewPassword()));

                candidateRepo.save(candidate);

            } else {

                User user = userRepository.findByEmail(req.getEmail())

                        .orElseThrow(() -> new RuntimeException("User not found"));

                user.setPassword(passwordEncoder.encode(req.getNewPassword()));

                userRepository.save(user);

            }
 
            return ResponseEntity.ok(Map.of("message", "Password reset successfully! Please login."));
 
        } catch (Exception e) {

            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));

        }

    }

    @PostMapping("/update-token")
    public ResponseEntity<String> updateToken(@RequestParam Long candidateId, @RequestParam String token) {
      Candidate   student = candidateRepo .findById(candidateId).orElse(null);
        if (student != null) {
            student.setFcmDeviceToken(token);
            candidateRepo.save(student);
            return ResponseEntity.ok("Token updated successfully");
        }
        return ResponseEntity.badRequest().body("Student not found");
    }

    @GetMapping("/getCandidateById")
    public ResponseEntity<?> getCandidateById(@RequestParam Long candidateId) {
        Candidate candidate = candidateRepo.findById(candidateId).orElseThrow(()->new RuntimeException("User Not found"));
        return ResponseEntity.ok(candidate);
    }

    @Autowired
    private CandidateProfileRepository candidateProfileRepository;
    @GetMapping("/getCandidateProfileByCandidateId")
    public Long getCandidateProfileByCandidateId(@RequestParam Long candidateId) {
    	Candidate candidateIdObj=candidateRepo.findById(candidateId).orElseThrow(()->new RuntimeException("candidate not found"));
    	return candidateProfileRepository.findByCandidateId(candidateIdObj.getId()).getId();
    	
    }
    
    
}