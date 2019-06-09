package io.gitlab.mkjeldsen.prstatbucket.unresolved;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

@Controller
public class UnresolvedReviewController {

    private final UnresolvedReviewService unresolvedReviewService;

    public UnresolvedReviewController(
            UnresolvedReviewService unresolvedReviewService) {
        this.unresolvedReviewService = unresolvedReviewService;
    }

    @GetMapping("/open")
    public ModelAndView redirect(HttpServletRequest request) {
        request.setAttribute(
                View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.MOVED_PERMANENTLY);
        return new ModelAndView("redirect:/unresolved");
    }

    @GetMapping("/unresolved")
    public ModelAndView asHtml() {
        final var pullRequests = unresolvedReviewService.getOpenPullRequests();

        final var model = Map.of("pullRequests", pullRequests);

        return new ModelAndView("unresolved-review", model);
    }

    @GetMapping(
            value = "/unresolved",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<List<UnresolvedReview>> asJson() {
        final var pullRequests = unresolvedReviewService.getOpenPullRequests();

        return ResponseEntity.ok(pullRequests);
    }
}
