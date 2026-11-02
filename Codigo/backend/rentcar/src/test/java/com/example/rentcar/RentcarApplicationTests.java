package com.example.rentcar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RentcarApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void listsAutomoveisWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/automoveis"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "agente@example.com", roles = { "AGENTE", "AGENTE_BANCO" })
	void returnsAgentTypeForAuthenticatedSession() throws Exception {
		mockMvc.perform(get("/api/auth/session"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tipoUsuario").value("AGENTE"))
				.andExpect(jsonPath("$.tipoAgente").value("BANCO"));
	}

	@Test
	@WithMockUser(username = "agente@example.com", roles = "AGENTE")
	void preventsAgentFromAccessingClientProfile() throws Exception {
		mockMvc.perform(get("/api/clientes/me"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "cliente@example.com", roles = "CLIENTE")
	void allowsClientToAccessTheirOrders() throws Exception {
		mockMvc.perform(get("/api/pedidos/me"))
				.andExpect(status().isOk());
	}

	@Test
	void reportsUnauthenticatedSession() throws Exception {
		mockMvc.perform(get("/api/auth/session"))
				.andExpect(status().isUnauthorized());
	}

}
