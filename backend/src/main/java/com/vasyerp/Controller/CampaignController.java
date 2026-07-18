package com.vasyerp.Controller;

import com.vasyerp.Model.CampaignRequest;
import com.vasyerp.Model.CampaignResponse;
import com.vasyerp.Model.PageRequestDto;
import com.vasyerp.Service.CampaignService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping("/list")
    public Page<CampaignResponse> list(@RequestBody PageRequestDto request) {
        return campaignService.list(
                request.getSearch(),
                request.getPage(),
                request.getSize()
        );
    }

    @PostMapping
    public CampaignResponse create(@RequestBody CampaignRequest request) {
        return campaignService.create(request);
    }

    @PutMapping("/{id}")
    public CampaignResponse update(
            @PathVariable String id,
            @RequestBody CampaignRequest request
    ) {
        return campaignService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> delete(@PathVariable String id) {
        campaignService.delete(id);
        return Map.of("deleted", true);
    }
}