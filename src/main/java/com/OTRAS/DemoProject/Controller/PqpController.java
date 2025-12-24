package com.OTRAS.DemoProject.Controller;
 
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.OTRAS.DemoProject.DTO.PqpRequestDTO;
import com.OTRAS.DemoProject.Service.PqpService;
 
@RestController

@RequestMapping("/api/pqp")


public class PqpController {
 
    private final PqpService pqpService;
 
    public PqpController(PqpService pqpService) {

        this.pqpService = pqpService;

    }
 
    @PostMapping("/pqpupload")

    public ResponseEntity<String> uploadPqp(

            @RequestParam String jobCategory,
            @RequestParam String jobTitle,

            @RequestParam String languages,

            @RequestParam String qualifications,

            @RequestParam String pqp,

            @RequestPart("file") MultipartFile file

    ) {

        try {

            PqpRequestDTO dto = PqpRequestDTO.builder()

                    .jobCategory(jobCategory)

                    .jobTitle(jobTitle)

                    .languages(languages)

                    .qualifications(qualifications)

                    .pqp(pqp)

                    .build();
 
            String result = pqpService.uploadPqp(dto, file);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (IOException e) {

            return ResponseEntity.internalServerError().body("File upload failed!");

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());

        }

    }
 
    @GetMapping("/get/allPQPs")

    public ResponseEntity<List<PqpRequestDTO>> getAllPqps() {

        List<PqpRequestDTO> pqps = pqpService.getAllPqps();

        return ResponseEntity.ok(pqps);

    }
 
    @GetMapping("/getPQPById")

    public ResponseEntity<?> getPqpById(@RequestParam Long id) {

        Optional<PqpRequestDTO> pqp = pqpService.getPqpById(id);

        return pqp.map(ResponseEntity::ok)

                  .orElseGet(() -> ResponseEntity.notFound().build());

    }

}

 