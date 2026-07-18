package com.vasyerp.Service;

import com.vasyerp.Entity.Campaign;
import com.vasyerp.Model.CampaignRequest;
import com.vasyerp.Model.CampaignResponse;
import org.springframework.data.domain.Page;

public interface CampaignService {
    Page<CampaignResponse> list(String search, int page, int size);
    CampaignResponse create(CampaignRequest req);
    CampaignResponse update(String id, CampaignRequest req);
    void delete(String id);
    CampaignResponse toResponse(Campaign c);
}
