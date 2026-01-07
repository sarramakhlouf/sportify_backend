package com.app.sportify_backend.services;

import com.app.sportify_backend.dto.ForgotPasswordRequest;
import com.app.sportify_backend.dto.RegisterRequest;
import com.app.sportify_backend.models.Role;
import com.app.sportify_backend.models.Team;
import com.app.sportify_backend.models.TeamMember;
import com.app.sportify_backend.models.User;
import com.app.sportify_backend.repositories.TeamMemberRepository;
import com.app.sportify_backend.repositories.TeamRepository;
import com.app.sportify_backend.repositories.UserRepository;
import com.app.sportify_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       TeamRepository teamRepository,
                       TeamMemberRepository teamMemberRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       EmailService emailService){
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User registerUser(RegisterRequest request, MultipartFile image) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_ALREADY_EXISTS");
        }

        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }

        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setRegistrationDate(LocalDateTime.now());

        if (request.getRole() == Role.PLAYER) {
            user.setPlayerCode(CodeGenerator.generatePlayerCode(8));
            user.setEnabled(true);
        } else {
            user.setEnabled(false);
        }

        user = userRepository.save(user);

        if (image != null && !image.isEmpty()) {
            try {
                String fileName = user.getId() + "_" + image.getOriginalFilename();
                Path path = Paths.get("uploads/profile/" + fileName);

                Files.createDirectories(path.getParent());
                Files.write(path, image.getBytes());

                user.setProfileImage("/uploads/profile/" + fileName);
                user = userRepository.save(user);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de l'upload de l'image");
            }
        }

        return user;
    }

    public String loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        if(user.getRole() == Role.MANAGER && !user.isEnabled()) {
            throw new RuntimeException("Compte en attente de validation");
        }

        return jwtService.generateToken(user);
    }

    public User verifyManager(String userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if(user.getRole() != Role.MANAGER){
            throw new RuntimeException("Seuls les managers peuvent être vérifiés");
        }
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public User forgotPassword(ForgotPasswordRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if(request.getNewPassword().length() < 8){
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return userRepository.save(user);
    }

    public User autoLogin(String token) {
        if (!jwtService.isTokenValid(token)) {
            throw new RuntimeException("Token invalide");
        }

        String email = jwtService.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Compte désactivé");
        }

        return user;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    public void generateOtp(User user) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setResetOtp(otp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    public boolean verifyOtp(String email, String otp) {
        User user = getUserByEmail(email);

        if(user.getResetOtp() == null || !user.getResetOtp().equals(otp)) return false;

        if(user.getOtpExpiration().isBefore(LocalDateTime.now())) return false;

        // Supprime OTP après validation
        user.setResetOtp(null);
        user.setOtpExpiration(null);
        userRepository.save(user);
        return true;
    }

    public User resetPassword(String email, String newPassword) {
        if(newPassword.length() < 8) throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");

        User user = getUserByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public List<Team> getMyTeams(String userId) {
        List<TeamMember> memberships = teamMemberRepository.findByUserId(userId);

        List<String> teamIds = memberships.stream()
                .map(TeamMember::getTeamId)
                .toList();

        return teamRepository.findAllById(teamIds);
    }


}
