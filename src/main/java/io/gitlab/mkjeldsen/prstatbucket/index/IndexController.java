package io.gitlab.mkjeldsen.prstatbucket.index;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    public IndexController() {}

    @GetMapping("/")
    public String asHtml() {
        return "index";
    }
}
