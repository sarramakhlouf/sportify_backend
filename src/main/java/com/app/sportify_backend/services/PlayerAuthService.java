package com.app.sportify_backend.services;

import com.app.sportify_backend.dto.ForgotPasswordRequest;
import com.app.sportify_backend.dto.RegisterRequest;
import com.app.sportify_backend.dto.UpdateProfileRequest;
import com.app.sportify_backend.models.Role;
import com.app.sportify_backend.models.User;
import com.app.sportify_backend.utils.CodeGenerator;
import com.app.sportify_backend.repositories.TeamRepository;
import com.app.sportify_backend.repositories.PlayerAuthRepository;
import com.app.sportify_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PlayerAuthService {
    private final PlayerAuthRepository playerAuthRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public User registerUser(RegisterRequest request, MultipartFile image) {

        if (playerAuthRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_ALREADY_EXISTS");
        }

        if (playerAuthRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("PHONE_ALREADY_EXISTS");
        }

        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }

        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setRegistrationDate(LocalDateTime.now());

        if (request.getRole() == Role.PLAYER) {
            user.setPlayerCode(CodeGenerator.generatePlayerCode());
            user.setEnabled(true);
        } else {
            user.setEnabled(false);
        }

        user = playerAuthRepository.save(user);

        if (image != null && !image.isEmpty()) {
            try {
                String fileName = user.getId() + "_" + image.getOriginalFilename();
                Path path = Paths.get("uploads/profile/" + fileName);

                Files.createDirectories(path.getParent());
                Files.write(path, image.getBytes());

                user.setProfileImageUrl("/uploads/profile/" + fileName);
                user = playerAuthRepository.save(user);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de l'upload de l'image");
            }
        }

        return user;
    }

    public String loginUser(String email, String password) {
        User user = playerAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé avec cet email"));

        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Le mot de passe est incorrect");
        }

        if(user.getRole() == Role.MANAGER && !user.isEnabled()) {
            throw new RuntimeException("Compte en attente de validation");
        }

        return jwtService.generateToken(user);
    }

    public User verifyManager(String userId){
        User user = playerAuthRepository.findById(userId).orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if(user.getRole() != Role.MANAGER){
            throw new RuntimeException("Seuls les managers peuvent être vérifiés");
        }
        user.setEnabled(true);
        return playerAuthRepository.save(user);
    }

    public User forgotPassword(ForgotPasswordRequest request){
        User user = playerAuthRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if(request.getNewPassword().length() < 8){
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return playerAuthRepository.save(user);
    }

    public User autoLogin(String token) {
        if (!jwtService.isTokenValid(token)) {
            throw new RuntimeException("Token invalide");
        }

        String email = jwtService.extractEmail(token);

        User user = playerAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Compte désactivé");
        }

        return user;
    }

    public User getUserByEmail(String email) {
        return playerAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    public void generateOtp(User user) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setResetOtp(otp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(5));
        playerAuthRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    public boolean verifyOtp(String email, String otp) {
        User user = getUserByEmail(email);

        if(user.getResetOtp() == null || !user.getResetOtp().equals(otp)) return false;

        if(user.getOtpExpiration().isBefore(LocalDateTime.now())) return false;

        user.setResetOtp(null);
        user.setOtpExpiration(null);
        playerAuthRepository.save(user);
        return true;
    }

    public User resetPassword(String email, String newPassword) {
        if(newPassword.length() < 8) throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");

        User user = getUserByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        return playerAuthRepository.save(user);
    }

    public User updateProfile(
            String userId, UpdateProfileRequest request,
            String currentPassword, MultipartFile image) {

        User user = playerAuthRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (!user.getPhone().equals(request.getPhone()) &&
                playerAuthRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("PHONE_ALREADY_EXISTS");
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (currentPassword == null || currentPassword.isEmpty() ||
                    !passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new RuntimeException("Mot de passe actuel incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        if (image != null && !image.isEmpty()) {
            try {
                String fileName = user.getId() + "_" + image.getOriginalFilename();
                Path path = Paths.get("uploads/profile/" + fileName);

                Files.createDirectories(path.getParent());
                Files.write(path, image.getBytes());

                user.setProfileImageUrl("/uploads/profile/" + fileName);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de l'upload de l'image");
            }
        }

        return playerAuthRepository.save(user);
    }
}
