package com.example.rentcar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"app.admin.email=",
		"recaptcha.enabled=true",
		"app.admin.password=",
		"app.admin.name="
})
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
	void reportsRecaptchaConfigurationToTheFrontend() throws Exception {
		mockMvc.perform(get("/api/auth/recaptcha-site-key"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.enabled").value(true));
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
	@WithMockUser(username = "agente@example.com", roles = "AGENTE")
	void preventsAgentFromCreatingAnotherAgent() throws Exception {
		mockMvc.perform(post("/api/admin/agentes")
				.contentType("application/json")
				.content("{}"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "cliente@example.com", roles = "CLIENTE")
	void preventsClientFromCreatingAnAgent() throws Exception {
		mockMvc.perform(post("/api/admin/agentes")
				.contentType("application/json")
				.content("{}"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "cliente@example.com", roles = "CLIENTE")
	void preventsClientFromListingAgents() throws Exception {
		mockMvc.perform(get("/api/admin/agentes"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "agente@example.com", roles = "AGENTE")
	void preventsAgentFromReadingAgentProfile() throws Exception {
		mockMvc.perform(get("/api/admin/agentes/1"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "agente@example.com", roles = "AGENTE")
	void preventsAgentFromEditingAgentProfiles() throws Exception {
		mockMvc.perform(put("/api/admin/agentes/1")
				.contentType("application/json")
				.content("{}"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "admin@example.com", roles = "ADMIN")
	void allowsAdministratorToReachAgentCreationEndpoint() throws Exception {
		mockMvc.perform(post("/api/admin/agentes")
				.contentType("application/json")
				.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(username = "admin@example.com", roles = "ADMIN")
	void returnsAdministratorTypeForAuthenticatedSession() throws Exception {
		mockMvc.perform(get("/api/auth/session"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tipoUsuario").value("ADMIN"));
	}

	@Test
	@WithMockUser(username = "cliente@example.com", roles = "CLIENTE")
	void allowsClientToAccessTheirOrders() throws Exception {
		mockMvc.perform(get("/api/pedidos/me"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "agente@example.com", roles = "AGENTE")
	void allowsAgentToAccessOrdersAwaitingReview() throws Exception {
		mockMvc.perform(get("/api/agente/pedidos"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "cliente@example.com", roles = "CLIENTE")
	void preventsClientFromAccessingAgentOrderQueue() throws Exception {
		mockMvc.perform(get("/api/agente/pedidos"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "agente@example.com", roles = "AGENTE")
	void allowsAgentToReadApprovedOrdersForExecution() throws Exception {
		mockMvc.perform(get("/api/agente/pedidos/aprovados"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "agente@example.com", roles = "AGENTE")
	void allowsAgentToReadOrdersAlreadyForwardedForExecution() throws Exception {
		mockMvc.perform(get("/api/agente/pedidos/execucao"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "agente@example.com", roles = "AGENTE")
	void allowsAgentToReadFormalizedContracts() throws Exception {
		mockMvc.perform(get("/api/agente/contratos"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "cliente@example.com", roles = "CLIENTE")
	void allowsClientToReadTheirFormalizedContracts() throws Exception {
		mockMvc.perform(get("/api/contratos/me"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "cliente@example.com", roles = "CLIENTE")
	void preventsClientFromReadingAgentContracts() throws Exception {
		mockMvc.perform(get("/api/agente/contratos"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "cliente@example.com", roles = "CLIENTE")
	void preventsClientFromExecutingAContract() throws Exception {
		mockMvc.perform(post("/api/agente/contratos/pedidos/1/execucao")
				.contentType("application/json")
				.content("{}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void reportsUnauthenticatedSession() throws Exception {
		mockMvc.perform(get("/api/auth/session"))
				.andExpect(status().isUnauthorized());
	}

}
