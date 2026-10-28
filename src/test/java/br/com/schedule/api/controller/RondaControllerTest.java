package br.com.schedule.api.controller;

import br.com.schedule.application.dto.PontoDTO;
import br.com.schedule.application.dto.request.RondaRequestDTO;
import br.com.schedule.application.dto.response.RondaResponseDTO;
import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.service.RondaService;
import br.com.schedule.domain.exception.NomeObrigatorioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RondaController.class)
class RondaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RondaService rondaService;

    @Test
    void criarRondaRetorna201ComLocation() throws Exception {
        when(rondaService.criar(any(RondaRequestDTO.class)))
                .thenReturn(new RondaResponseDTO("r-1", "Ronda Norte", "Inspeção diária", true,
                        List.of(new PontoDTO("P-01", "Portão principal"))));

        mockMvc.perform(post("/rondas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ronda Norte","descricao":"Inspeção diária",
                                 "pontos":[{"codigo":"P-01","descricao":"Portão principal"}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/rondas/r-1"))
                .andExpect(jsonPath("$.id").value("r-1"));
    }

    @Test
    void excluirRondaRetorna204() throws Exception {
        mockMvc.perform(delete("/rondas/r-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void buscarRondaInexistenteRetorna404() throws Exception {
        when(rondaService.buscarPorId("r-1"))
                .thenThrow(RecursoNaoEncontradoException.ronda("r-1"));

        mockMvc.perform(get("/rondas/r-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail")
                        .value("Ronda não encontrada para o identificador 'r-1'."));
    }

    @Test
    void domainExceptionGenericaRetorna422() throws Exception {
        when(rondaService.criar(any(RondaRequestDTO.class)))
                .thenThrow(new NomeObrigatorioException());

        mockMvc.perform(post("/rondas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.detail")
                        .value("O nome da ronda é obrigatório e não pode ser vazio."));
    }

    @Test
    void runtimeExceptionGenericaRetorna500SemMensagemOriginal() throws Exception {
        when(rondaService.buscarPorId("r-1"))
                .thenThrow(new RuntimeException("detalhe interno sensível"));

        mockMvc.perform(get("/rondas/r-1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("Erro interno inesperado"))
                .andExpect(content().string(not(containsString("detalhe interno sensível"))));
    }
}
