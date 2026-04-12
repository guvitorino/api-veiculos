package com.vitorino.apiveiculos.controller;

import com.vitorino.apiveiculos.dto.VehicleRequestDTO;
import com.vitorino.apiveiculos.dto.VehicleResponsetDTO;
import com.vitorino.apiveiculos.exception.ErrorResponse;
import com.vitorino.apiveiculos.exception.LicensePlateAlreadyExistsException;
import com.vitorino.apiveiculos.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/veiculos")
public class VehicleController {

    private final VehicleService service;

    public VehicleController(VehicleService service) {
        this.service = service;
    }

    @Operation(
            summary = "Cadastrar veículo",
            description = "Cria um novo veículo no sistema",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Veículo criado com sucesso"),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Placa já cadastrada",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Erro de placa duplicada",
                                            value = """
                                                {
                                                  "message": "Placa já cadastrada: ABC1234",
                                                  "status": 409,
                                                  "timestamp": "2026-04-12T00:00:00"
                                                }
                                            """
                                    )
                            )
                    )
            }
    )
    @PostMapping
    public ResponseEntity<VehicleResponsetDTO> post(
            @Parameter(description = "Dados do veículo a ser cadastrado")
            @Valid @RequestBody VehicleRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponsetDTO> put(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleRequestDTO dto
    ) {
        return ResponseEntity.ok(service.update(id, dto));
    }
}
