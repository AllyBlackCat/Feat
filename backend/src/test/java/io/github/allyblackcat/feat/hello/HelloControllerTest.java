
package io.github.allyblackcat.feat.hello;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HelloControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new HelloController())
            .build();

    @Test
    @DisplayName("JSONでHello, World!を返すことを確認する")
    void shouldReturnHelloWorldAsJson() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("Hello, World!"))
                .andExpect(jsonPath("$.application")
                        .value("Feat"));
    }

    @Test
    @DisplayName ("POSTリクエストでHello, {name}!を返すことを確認する")
    void shouldReturnGreetingFromPostRequest() throws Exception {
        mockMvc.perform(post("/api/hello")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ally\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Hello, Ally!"))
                .andExpect(jsonPath("$.application")
                        .value("Feat"));
    }


}
