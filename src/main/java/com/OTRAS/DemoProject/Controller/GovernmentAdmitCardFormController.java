package com.OTRAS.DemoProject.Controller;

import com.OTRAS.DemoProject.Entity.GovernmentAdmitCardForm;
import com.OTRAS.DemoProject.Service.GovernmentAdmitCardFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/governmentAdmitCard")
@RequiredArgsConstructor
public class GovernmentAdmitCardFormController {

    private final GovernmentAdmitCardFormService service;

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<GovernmentAdmitCardForm> create(
            @RequestParam String jobCategory,
            @RequestParam String authorizedSignature,
            @RequestParam(required = false) MultipartFile uploadSignature,
            @RequestParam(required = false) MultipartFile uploadAllLocations
    ) {
        return ResponseEntity.ok(service.create(jobCategory, authorizedSignature, uploadSignature, uploadAllLocations));
    }

    @PutMapping(value = "/update", consumes = "multipart/form-data")
    public ResponseEntity<GovernmentAdmitCardForm> update(
            @RequestParam Long id,
            @RequestParam String jobCategory,
            @RequestParam String authorizedSignature,
            @RequestParam(required = false) MultipartFile uploadSignature,
            @RequestParam(required = false) MultipartFile uploadAllLocations
    ) {
        return ResponseEntity.ok(service.update(id, jobCategory, authorizedSignature, uploadSignature, uploadAllLocations));
    }

    @GetMapping("/all")
    public ResponseEntity<List<GovernmentAdmitCardForm>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/getAdmitCardById")
    public ResponseEntity<GovernmentAdmitCardForm> getOne(@RequestParam Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/deleteAdmitCard")
    public ResponseEntity<String> delete(@RequestParam Long id) {
        service.delete(id);
        return ResponseEntity.ok("Deleted Successfully");
    }
}
