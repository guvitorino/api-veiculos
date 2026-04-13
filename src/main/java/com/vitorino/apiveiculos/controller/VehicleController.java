package com.vitorino.apiveiculos.controller;

import com.vitorino.apiveiculos.dto.*;
import com.vitorino.apiveiculos.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/{id}")
    public ResponseEntity<VehicleResponsetDTO> patch(
            @PathVariable UUID id,
            @RequestBody VehiclePatchRequestDTO dto
    ) {
        return ResponseEntity.ok(service.patch(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponsetDTO> findById(@PathVariable UUID id) {
        VehicleResponsetDTO response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ListPageResponseDTO<VehicleResponsetDTO>> findAll(
            @ParameterObject @ModelAttribute VehicleFilterDTO filters,
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "marca", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.findAll(filters, pageable));
    }
}
