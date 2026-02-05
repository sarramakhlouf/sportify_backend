package com.app.sportify_backend.services;

import com.app.sportify_backend.dto.ManagerRegisterRequest;
import com.app.sportify_backend.exception.AccountNotEnabledException;
import com.app.sportify_backend.models.Pitch;
import com.app.sportify_backend.models.Role;
import com.app.sportify_backend.models.User;
import com.app.sportify_backend.repositories.UserRepository;
import com.app.sportify_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PitchService pitchService;
    private final JwtService jwtService;

    /**
     * Inscription d'un manager avec UN SEUL pitch
     * Le pitch est stocké dans pendingPitch (pas encore dans la collection pitches)
     */
    public User registerManager(ManagerRegisterRequest request) {
        // Vérifications
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Un utilisateur avec ce téléphone existe déjà");
        }

        // Créer le manager
        User manager = new User();
        manager.setFirstname(request.getFirstname());
        manager.setLastname(request.getLastname());
        manager.setEmail(request.getEmail());
        manager.setPhone(request.getPhone());
        manager.setPassword(passwordEncoder.encode(request.getPassword()));
        manager.setRole(Role.MANAGER);
        manager.setRegistrationDate(LocalDateTime.now());
        manager.setEnabled(false);

        // Stocker le pitch dans pendingPitch (pas encore dans la collection pitches)
        if (request.getPitch() != null) {
            Pitch pitch = request.getPitch();
            initializePitch(pitch);
            manager.setPendingPitch(pitch);
        }

        User savedManager = userRepository.save(manager);

        log.info("Manager inscrit: {} - Pitch en attente de validation", manager.getEmail());

        return savedManager;
    }


    @Transactional
    public User enableManagerByAdmin(String managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager non trouvé"));

        if (manager.getRole() != Role.MANAGER) {
            throw new RuntimeException("Cet utilisateur n'est pas un manager");
        }

        if (manager.isEnabled()) {
            throw new RuntimeException("Ce compte est déjà activé");
        }

        manager.setEnabled(true);
        manager.setActivationDate(LocalDateTime.now());

        String pitchId = null;

        if (manager.getPendingPitch() != null) {
            Pitch createdPitch = pitchService.createPitch(
                    manager.getPendingPitch(),
                    manager.getId()
            );

            pitchId = createdPitch.getId();
            manager.setPitchId(pitchId);
            manager.setPendingPitch(null);

            log.info("Pitch {} créé dans la collection pour le manager {}",
                    pitchId,
                    manager.getId());
        }

        User activatedManager = userRepository.save(manager);

        emailService.sendManagerActivationByAdminEmail(
                manager.getEmail(),
                manager.getFirstname(),
                pitchId != null ? 1 : 0
        );

        log.info("Manager activé: {} - Pitch ID: {}", manager.getEmail(), pitchId);

        return activatedManager;
    }

    @Transactional
    public User disableManagerByAdmin(String managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager non trouvé"));

        if (manager.getRole() != Role.MANAGER) {
            throw new RuntimeException("Cet utilisateur n'est pas un manager");
        }

        if (!manager.isEnabled()) {
            throw new RuntimeException("Ce compte est déjà désactivé");
        }

        manager.setEnabled(false);

        pitchService.deactivateManagerPitch(manager.getId());

        User disabledManager = userRepository.save(manager);

        emailService.sendManagerDeactivationEmail(
                manager.getEmail(),
                manager.getFirstname()
        );

        log.info("Manager désactivé: {}", manager.getEmail());

        return disabledManager;
    }

    @Transactional
    public void deleteManager(String managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager non trouvé"));

        if (manager.getRole() != Role.MANAGER) {
            throw new RuntimeException("Cet utilisateur n'est pas un manager");
        }

        pitchService.deleteManagerPitch(manager.getId());

        userRepository.delete(manager);

        log.info("Manager et son pitch supprimés: {}", manager.getEmail());
    }

    public List<User> getPendingManagers() {
        return userRepository.findByRoleAndIsEnabled(Role.MANAGER, false);
    }

    private void initializePitch(Pitch pitch) {
        pitch.setId(UUID.randomUUID().toString());
        pitch.setCreatedAt(LocalDateTime.now());
    }
}