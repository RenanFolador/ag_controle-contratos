package com.organization.contractmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class MetaWhatsAppApiClientTests {

    @Test
    void mapsOfficialApiSuccessResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(
                        "https://graph.example/v23.0/phone-id/messages"))
                .andExpect(header("Authorization", "Bearer secret-token"))
                .andExpect(jsonPath("$.messaging_product").value("whatsapp"))
                .andExpect(jsonPath("$.to").value("5541999999999"))
                .andExpect(jsonPath("$.text.body").value("Mensagem"))
                .andRespond(withSuccess(
                        "{\"messages\":[{\"id\":\"wamid.123\",\"message_status\":\"accepted\"}]}",
                        MediaType.APPLICATION_JSON));
        MetaWhatsAppApiClient client = client(builder);

        WhatsAppSendResult result = client.sendText("5541999999999", "Mensagem");

        assertThat(result).isEqualTo(new WhatsAppSendResult("wamid.123", "accepted"));
        server.verify();
    }

    @Test
    void mapsApiFailureWithoutLeakingRawResponseOrToken() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(
                        "https://graph.example/v23.0/phone-id/messages"))
                .andRespond(withUnauthorizedRequest().body(
                        "{\"error\":{\"message\":\"sensitive detail\",\"code\":190,"
                                + "\"fbtrace_id\":\"trace-123\"}}"));
        MetaWhatsAppApiClient client = client(builder);

        assertThatThrownBy(() -> client.sendText("5541999999999", "Mensagem"))
                .isInstanceOfSatisfying(WhatsAppProviderException.class, exception -> {
                    assertThat(exception.getHttpStatus()).isEqualTo(401);
                    assertThat(exception.getProviderCode()).isEqualTo("190");
                    assertThat(exception.getTraceId()).isEqualTo("trace-123");
                })
                .hasMessageNotContaining("sensitive detail")
                .hasMessageNotContaining("secret-token");
        server.verify();
    }

    private MetaWhatsAppApiClient client(RestClient.Builder builder) {
        return new MetaWhatsAppApiClient(
                builder, new ObjectMapper(), "https://graph.example", "v23.0",
                "phone-id", "secret-token");
    }
}
