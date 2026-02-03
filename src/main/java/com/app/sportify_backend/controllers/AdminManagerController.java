package com.app.sportify_backend.controllers;

import com.app.sportify_backend.models.User;
import com.app.sportify_backend.services.ManagerAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/managers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminManagerController {

    private final ManagerAuthService managerAuthService;

    @GetMapping("/pending")
    public ResponseEntity<List<User>> getPendingManagers() {
        List<User> pendingManagers = managerAuthService.getPendingManagers();
        return ResponseEntity.ok(pendingManagers);
    }

    @PostMapping("/{managerId}/enable")
    public ResponseEntity<?> enableManager(@PathVariable String managerId) {
        try {
            User manager = managerAuthService.enableManagerByAdmin(managerId);

            return ResponseEntity.ok(Map.of(
                    "message", "Manager activé avec succès",
                    "manager", manager,
                    "pitchCreated", manager.getPitchId() != null
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{managerId}/disable")
    public ResponseEntity<?> disableManager(@PathVariable String managerId) {
        try {
            User manager = managerAuthService.disableManagerByAdmin(managerId);

            return ResponseEntity.ok(Map.of(
                    "message", "Manager désactivé avec succès",
                    "manager", manager
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{managerId}")
    public ResponseEntity<?> deleteManager(@PathVariable String managerId) {
        try {
            managerAuthService.deleteManager(managerId);

            return ResponseEntity.ok(Map.of(
                    "message", "Manager et son pitch supprimés avec succès"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}