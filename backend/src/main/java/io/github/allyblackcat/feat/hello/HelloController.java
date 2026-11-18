
package io.github.allyblackcat.feat.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public HelloResponse hello() {
        return new HelloResponse("Hello, World!", "Feat");
    }

    @PostMapping("/api/hello")
    public HelloResponse helloPost(@RequestBody HelloRequest request) {
        return new HelloResponse(
                "Hello, " + request.name() + "!", 
                "Feat");
    }
}