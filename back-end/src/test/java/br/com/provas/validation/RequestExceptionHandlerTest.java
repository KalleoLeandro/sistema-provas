package br.com.provas.validation;

import br.com.provas.controller.MateriaController;
import br.com.provas.controller.UsuarioAdministrativoController;
import br.com.provas.exceptions.GlobalExceptionHandler;
import br.com.provas.service.MateriaService;
import br.com.provas.service.UsuarioAdministrativoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RequestExceptionHandlerTest {
    private MockMvc mvc;
    private LocalValidatorFactoryBean validator;
    private MateriaService materiaService;
    private UsuarioAdministrativoService usuarioService;

    @BeforeEach
    void setup() {
        materiaService = mock(MateriaService.class); usuarioService = mock(UsuarioAdministrativoService.class);
        validator = new LocalValidatorFactoryBean(); validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new MateriaController(materiaService), new UsuarioAdministrativoController(usuarioService))
            .setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator).build();
    }
    @AfterEach void close() { validator.close(); }

    @Test
    void collectsErrorsAcrossFieldsWithoutEchoingCredentials() throws Exception {
        mvc.perform(post("/administradores").contentType(MediaType.APPLICATION_JSON).content("{\"login\":\"a\",\"senha\":\"xyz\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erros[*].campo", hasItems("login", "senha")))
            .andExpect(jsonPath("$.mensagem", containsString("senha")))
            .andExpect(content().string(not(containsString("xyz"))));
        verifyNoInteractions(usuarioService);
    }

    @Test
    void invalidUpdateReturnsFieldNamesAndPreventsWrite() throws Exception {
        mvc.perform(put("/materias/1").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"   \"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erros[0].campo").value("nome"));
        verifyNoInteractions(materiaService);
    }

    @Test
    void handlesMalformedBodyAndInvalidTypes() throws Exception {
        mvc.perform(post("/materias").contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erros[0].campo").value("request"));
        mvc.perform(patch("/administradores/1/status").contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":{}}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erros[0].campo").value("ativo"));
        mvc.perform(get("/materias/invalido")).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erros[0].campo").value("id"));
    }
}
