package com.vitorino.apiveiculos.controller;

import com.vitorino.apiveiculos.dto.*;
import com.vitorino.apiveiculos.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/veiculos")
@SecurityRequirement(name = "bearerAuth")
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
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                {
                                                  "message": "Forbidden",
                                                  "status": 403,
                                                  "timestamp": "2026-04-12T23:44:20.468074067"
                                                }
                                            """
                                    )
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<VehicleResponsetDTO> post(
            @Parameter(description = "Dados do veículo a ser cadastrado")
            @Valid @RequestBody VehicleRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.save(dto));
    }

    @Operation(
            summary = "Atualizer veículo",
            description = "Atualize um veículo no sistema",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Veículo atualizado com sucesso"),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                {
                                                  "message": "Forbidden",
                                                  "status": 403,
                                                  "timestamp": "2026-04-12T23:44:20.468074067"
                                                }
                                            """
                                    )
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponsetDTO> put(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleRequestDTO dto
    ) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(
            summary = "Atualizer parcialmente um veículo",
            description = "Atualiza parcialmente um veículo no sistema",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Veículo atualizado com sucesso"),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                {
                                                  "message": "Forbidden",
                                                  "status": 403,
                                                  "timestamp": "2026-04-12T23:44:20.468074067"
                                                }
                                            """
                                    )
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<VehicleResponsetDTO> patch(
            @PathVariable UUID id,
            @RequestBody VehiclePatchRequestDTO dto
    ) {
        return ResponseEntity.ok(service.patch(id, dto));
    }

    @Operation(
            summary = "Deletar um veículo",
            description = "Deleta um veículo no sistema",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Veículo deletado com sucesso"),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                {
                                                  "message": "Forbidden",
                                                  "status": 403,
                                                  "timestamp": "2026-04-12T23:44:20.468074067"
                                                }
                                            """
                                    )
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponsetDTO> findById(@PathVariable UUID id) {
        VehicleResponsetDTO response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<ListPageResponseDTO<VehicleResponsetDTO>> findAll(
            @ParameterObject @ModelAttribute VehicleFilterDTO filters,
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "marca", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.findAll(filters, pageable));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/relatorios/por-marca")
    public ResponseEntity<List<VehicleByBrandReportDTO>> getReportByBrand() {
        return ResponseEntity.ok(service.getVehicleReportByBrand());
    }
}
