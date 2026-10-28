package br.com.schedule.api.controller;

import br.com.schedule.application.dto.request.AgendamentoRequestDTO;
import br.com.schedule.application.dto.request.CancelamentoRequestDTO;
import br.com.schedule.application.dto.request.ReagendamentoRequestDTO;
import br.com.schedule.application.dto.response.AgendamentoResponseDTO;
import br.com.schedule.application.service.AgendamentoService;
import br.com.schedule.domain.model.StatusAgendamento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/agendamentos")
@Tag(name = "Agendamentos", description = "Agendamento e ciclo de vida das rondas")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @PostMapping
    @Operation(summary = "Agendar uma ronda")
    @ApiResponse(responseCode = "201", description = "Agendamento criado")
    @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido")
    @ApiResponse(responseCode = "404", description = "Ronda não encontrada")
    @ApiResponse(responseCode = "409", description = "Conflito de agenda")
    @ApiResponse(responseCode = "422", description = "Regra de domínio violada")
    public ResponseEntity<AgendamentoResponseDTO> agendar(@RequestBody AgendamentoRequestDTO request) {
        AgendamentoResponseDTO agendamento = agendamentoService.agendar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(agendamento.id())
                .toUri();
        return ResponseEntity.created(location).body(agendamento);
    }

    @GetMapping
    @Operation(summary = "Listar agendamentos por status, responsável e ronda")
    @ApiResponse(responseCode = "200", description = "Agendamentos encontrados")
    @ApiResponse(responseCode = "400", description = "Parâmetro de consulta inválido")
    public ResponseEntity<List<AgendamentoResponseDTO>> listar(
            @RequestParam(required = false) StatusAgendamento status,
            @RequestParam(required = false) String responsavel,
            @RequestParam(required = false) String idRonda) {
        return ResponseEntity.ok(agendamentoService.listar(status, responsavel, idRonda));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar um agendamento por identificador")
    @ApiResponse(responseCode = "200", description = "Agendamento encontrado")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    public ResponseEntity<AgendamentoResponseDTO> buscar(@PathVariable String id) {
        return ResponseEntity.ok(agendamentoService.buscarPorId(id));
    }

    @PostMapping("/{id}/iniciar")
    @Operation(summary = "Iniciar um agendamento")
    @ApiResponse(responseCode = "200", description = "Agendamento iniciado")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    @ApiResponse(responseCode = "409", description = "Transição de status inválida")
    public ResponseEntity<AgendamentoResponseDTO> iniciar(@PathVariable String id) {
        return ResponseEntity.ok(agendamentoService.iniciar(id));
    }

    @PostMapping("/{id}/concluir")
    @Operation(summary = "Concluir um agendamento")
    @ApiResponse(responseCode = "200", description = "Agendamento concluído")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    @ApiResponse(responseCode = "409", description = "Transição de status inválida")
    public ResponseEntity<AgendamentoResponseDTO> concluir(@PathVariable String id) {
        return ResponseEntity.ok(agendamentoService.concluir(id));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar um agendamento")
    @ApiResponse(responseCode = "200", description = "Agendamento cancelado")
    @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    @ApiResponse(responseCode = "409", description = "Transição de status inválida")
    @ApiResponse(responseCode = "422", description = "Regra de domínio violada")
    public ResponseEntity<AgendamentoResponseDTO> cancelar(
            @PathVariable String id,
            @RequestBody CancelamentoRequestDTO request) {
        return ResponseEntity.ok(agendamentoService.cancelar(id, request));
    }

    @PostMapping("/{id}/reagendar")
    @Operation(summary = "Reagendar um agendamento")
    @ApiResponse(responseCode = "200", description = "Agendamento reagendado")
    @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    @ApiResponse(responseCode = "409", description = "Conflito de agenda")
    @ApiResponse(responseCode = "422", description = "Regra de domínio violada")
    public ResponseEntity<AgendamentoResponseDTO> reagendar(
            @PathVariable String id,
            @RequestBody ReagendamentoRequestDTO request) {
        return ResponseEntity.ok(agendamentoService.reagendar(id, request));
    }
}
