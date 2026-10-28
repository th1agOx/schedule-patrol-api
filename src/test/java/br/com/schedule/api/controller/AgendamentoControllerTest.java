package br.com.schedule.api.controller;

import br.com.schedule.application.dto.PeriodoDTO;
import br.com.schedule.application.dto.request.AgendamentoRequestDTO;
import br.com.schedule.application.dto.response.AgendamentoResponseDTO;
import br.com.schedule.application.service.AgendamentoService;
import br.com.schedule.domain.exception.ConflitoDeAgendaException;
import br.com.schedule.domain.exception.TransicaoDeStatusInvalidaException;
import br.com.schedule.domain.model.Periodo;
import br.com.schedule.domain.model.StatusAgendamento;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgendamentoController.class)
class AgendamentoControllerTest {

    private static final String AGENDAMENTO_VALIDO = """
            {"idRonda":"r-1","responsavel":"Maria",
             "periodoPrevisto":{"inicio":"2026-11-05T08:00:00-03:00",
                                "fim":"2026-11-05T10:00:00-03:00"}}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgendamentoService agendamentoService;

    @Test
    void jsonMalformadoRetorna400() throws Exception {
        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idRonda\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void statusInvalidoNaQueryRetorna400() throws Exception {
        mockMvc.perform(get("/agendamentos").param("status", "XPTO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Valor inválido para o parâmetro 'status'."));
    }

    @Test
    void conflitoDeAgendaRetorna409() throws Exception {
        when(agendamentoService.agendar(any(AgendamentoRequestDTO.class)))
                .thenThrow(new ConflitoDeAgendaException("Maria",
                        new Periodo(Instant.parse("2026-11-05T11:00:00Z"),
                                Instant.parse("2026-11-05T13:00:00Z"))));

        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AGENDAMENTO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void transicaoDeStatusInvalidaRetorna409() throws Exception {
        when(agendamentoService.iniciar("a-1"))
                .thenThrow(new TransicaoDeStatusInvalidaException(
                        StatusAgendamento.CONCLUIDA, StatusAgendamento.EM_ANDAMENTO));

        mockMvc.perform(post("/agendamentos/a-1/iniciar"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void agendarAceitaDataComOffsetEDevolveUtc() throws Exception {
        when(agendamentoService.agendar(any(AgendamentoRequestDTO.class)))
                .thenReturn(new AgendamentoResponseDTO("a-1", "r-1", "Maria",
                        new PeriodoDTO(Instant.parse("2026-11-05T11:00:00Z"),
                                Instant.parse("2026-11-05T13:00:00Z")),
                        "AGENDADA", null));
        ArgumentCaptor<AgendamentoRequestDTO> captor = ArgumentCaptor.forClass(AgendamentoRequestDTO.class);

        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AGENDAMENTO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/agendamentos/a-1"))
                .andExpect(jsonPath("$.periodoPrevisto.inicio").value("2026-11-05T11:00:00Z"))
                .andExpect(jsonPath("$.periodoPrevisto.fim").value("2026-11-05T13:00:00Z"));

        verify(agendamentoService).agendar(captor.capture());
        assertThat(captor.getValue().periodoPrevisto().inicio())
                .isEqualTo(Instant.parse("2026-11-05T11:00:00Z"));
    }
}
