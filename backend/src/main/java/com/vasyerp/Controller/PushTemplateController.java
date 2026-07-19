
package com.vasyerp.Controller;

import com.vasyerp.Entity.NotificationTemplate;
import com.vasyerp.Entity.PushTemplate;
import com.vasyerp.Model.PageRequestDto;
import com.vasyerp.Model.TemplateRequest;
import com.vasyerp.Service.TemplateService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates/push")
public class PushTemplateController {

    private final TemplateService<PushTemplate> templateService;

    public PushTemplateController(TemplateService<PushTemplate> templateService) {
        this.templateService = templateService;
    }

    @PostMapping("/list")
    public Page<PushTemplate> list(@RequestBody PageRequestDto request) {
        return templateService.list(
                request.getSearch(),
                request.getPage(),
                request.getSize()
        );
    }

    @PostMapping
    public NotificationTemplate create(@RequestBody TemplateRequest request) {
        return templateService.create(request);
    }

    @PutMapping("/{id}")
    public NotificationTemplate update(
            @PathVariable String id,
            @RequestBody TemplateRequest request
    ) {
        return templateService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> delete(@PathVariable String id) {
        templateService.delete(id);
        return Map.of("deleted", true);
    }

    @GetMapping("/all")
    public List<PushTemplate> list1() {
        return templateService.getAll();
    }
}