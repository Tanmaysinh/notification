package com.vasyerp.Service.Impl;

import com.vasyerp.Entity.Campaign;
import com.vasyerp.Model.CampaignRequest;
import com.vasyerp.Model.CampaignResponse;
import com.vasyerp.Repository.CampaignRepository;
import com.vasyerp.Repository.SmsTemplateRepository;
import com.vasyerp.Repository.EmailTemplateRepository;
import com.vasyerp.Repository.PushTemplateRepository;
import com.vasyerp.Service.CampaignService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final SmsTemplateRepository smsTemplateRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final PushTemplateRepository pushTemplateRepository;

    public CampaignServiceImpl(
            CampaignRepository campaignRepository,
            SmsTemplateRepository smsTemplateRepository,
            EmailTemplateRepository emailTemplateRepository,
            PushTemplateRepository pushTemplateRepository
    ) {
        this.campaignRepository = campaignRepository;
        this.smsTemplateRepository = smsTemplateRepository;
        this.emailTemplateRepository = emailTemplateRepository;
        this.pushTemplateRepository = pushTemplateRepository;
    }

    public Page<CampaignResponse> list(String search, int page, int size) {
        return campaignRepository.search(search, PageRequest.of(page, size)).map(this::toResponse);
    }

    public CampaignResponse create(CampaignRequest req) {
        Campaign campaign = new Campaign(req.getName(), req.getSmsTemplateId(), req.getEmailTemplateId(), req.getPushTemplateId());
        return toResponse(campaignRepository.save(campaign));
    }

    public CampaignResponse update(String id, CampaignRequest req) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));
        campaign.setName(req.getName());
        campaign.setSmsTemplateId(req.getSmsTemplateId());
        campaign.setEmailTemplateId(req.getEmailTemplateId());
        campaign.setPushTemplateId(req.getPushTemplateId());
        return toResponse(campaignRepository.save(campaign));
    }

    public void delete(String id) {
        campaignRepository.deleteById(id);
    }

    public CampaignResponse toResponse(Campaign c) {
        String smsName = c.getSmsTemplateId() != null
                ? smsTemplateRepository.findById(c.getSmsTemplateId()).map(t -> t.getName()).orElse(null)
                : null;
        String emailName = c.getEmailTemplateId() != null
                ? emailTemplateRepository.findById(c.getEmailTemplateId()).map(t -> t.getName()).orElse(null)
                : null;
        String pushName = c.getPushTemplateId() != null
                ? pushTemplateRepository.findById(c.getPushTemplateId()).map(t -> t.getName()).orElse(null)
                : null;

        return new CampaignResponse(
                c.getCampaignId(), c.getName(),
                c.getSmsTemplateId(), smsName,
                c.getEmailTemplateId(), emailName,
                c.getPushTemplateId(), pushName
        );
    }
}