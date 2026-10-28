package br.com.schedule.api.controller;

import br.com.schedule.application.dto.request.RondaRequestDTO;
import br.com.schedule.application.dto.response.RondaResponseDTO;
import br.com.schedule.application.service.RondaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/rondas")
@Tag(name = "Rondas", description = "Gerenciamento de rondas de inspeção")
public class RondaController {

    private final RondaService rondaService;

    public RondaController(RondaService rondaService) {
        this.rondaService = rondaService;
    }

    @PostMapping
    @Operation(summary = "Criar uma ronda")
    @ApiResponse(responseCode = "201", description = "Ronda criada")
    @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido")
    @ApiResponse(responseCode = "422", description = "Regra de domínio violada")
    public ResponseEntity<RondaResponseDTO> criar(@RequestBody RondaRequestDTO request) {
        RondaResponseDTO ronda = rondaService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(ronda.id())
                .toUri();
        return ResponseEntity.created(location).body(ronda);
    }

    @GetMapping
    @Operation(summary = "Listar rondas, opcionalmente filtrando por situação ativa")
    @ApiResponse(responseCode = "200", description = "Rondas encontradas")
    @ApiResponse(responseCode = "400", description = "Parâmetro de consulta inválido")
    public ResponseEntity<List<RondaResponseDTO>> listar(
            @RequestParam(required = false) Boolean ativa) {
        return ResponseEntity.ok(rondaService.listar(ativa));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar uma ronda por identificador")
    @ApiResponse(responseCode = "200", description = "Ronda encontrada")
    @ApiResponse(responseCode = "404", description = "Ronda não encontrada")
    public ResponseEntity<RondaResponseDTO> buscar(@PathVariable String id) {
        return ResponseEntity.ok(rondaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma ronda")
    @ApiResponse(responseCode = "200", description = "Ronda atualizada")
    @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido")
    @ApiResponse(responseCode = "404", description = "Ronda não encontrada")
    @ApiResponse(responseCode = "422", description = "Regra de domínio violada")
    public ResponseEntity<RondaResponseDTO> atualizar(
            @PathVariable String id,
            @RequestBody RondaRequestDTO request) {
        return ResponseEntity.ok(rondaService.atualizar(id, request));
    }

    @PostMapping("/{id}/ativar")
    @Operation(summary = "Ativar uma ronda")
    @ApiResponse(responseCode = "200", description = "Ronda ativada")
    @ApiResponse(responseCode = "404", description = "Ronda não encontrada")
    public ResponseEntity<RondaResponseDTO> ativar(@PathVariable String id) {
        return ResponseEntity.ok(rondaService.ativar(id));
    }

    @PostMapping("/{id}/desativar")
    @Operation(summary = "Desativar uma ronda")
    @ApiResponse(responseCode = "200", description = "Ronda desativada")
    @ApiResponse(responseCode = "404", description = "Ronda não encontrada")
    public ResponseEntity<RondaResponseDTO> desativar(@PathVariable String id) {
        return ResponseEntity.ok(rondaService.desativar(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir uma ronda")
    @ApiResponse(responseCode = "204", description = "Ronda excluída")
    @ApiResponse(responseCode = "404", description = "Ronda não encontrada")
    @ApiResponse(responseCode = "422", description = "Regra de domínio violada")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        rondaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
