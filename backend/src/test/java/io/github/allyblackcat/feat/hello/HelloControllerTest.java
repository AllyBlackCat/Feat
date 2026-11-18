
package io.github.allyblackcat.feat.hello;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
