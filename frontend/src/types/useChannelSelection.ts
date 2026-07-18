import { useState } from "react";
import { useSecureSession } from "@/crypto/SecureSessionContext";
import { getAllTemplates } from "@/lib/apiClient";
import type { Template, TemplateType } from "@/types/template";

export interface ChannelState {
  enabled: boolean;
  templateId: string | null;
  templates: Template[];
  loading: boolean;
}

export function initChannelState(): Record<TemplateType, ChannelState> {
  return {
    sms: { enabled: false, templateId: null, templates: [], loading: false },
    email: { enabled: false, templateId: null, templates: [], loading: false },
    push: { enabled: false, templateId: null, templates: [], loading: false },
  };
}

export function useChannelSelection() {
  const { getSession } = useSecureSession();
  const [channels, setChannels] = useState<Record<TemplateType, ChannelState>>(initChannelState());
  const [activePreview, setActivePreview] = useState<TemplateType>("sms");

  async function ensureTemplatesLoaded(type: TemplateType) {
    setChannels((c) => {
      if (c[type].templates.length > 0 || c[type].loading) return c;
      return { ...c, [type]: { ...c[type], loading: true } };
    });

    const session = await getSession();
    const templates = await getAllTemplates<Template>(session, type).catch(() => []);

    setChannels((c) => ({ ...c, [type]: { ...c[type], templates, loading: false } }));
  }

  function toggleChannel(type: TemplateType) {
    const nextEnabled = !channels[type].enabled;
    setChannels((c) => ({
      ...c,
      [type]: { ...c[type], enabled: nextEnabled, templateId: nextEnabled ? c[type].templateId : null },
    }));
    if (nextEnabled) {
      ensureTemplatesLoaded(type);
      setActivePreview(type);
    }
  }

  function selectTemplate(type: TemplateType, templateId: string) {
    setChannels((c) => ({ ...c, [type]: { ...c[type], templateId } }));
    setActivePreview(type);
  }

  function reset() {
    setChannels(initChannelState());
    setActivePreview("sms");
  }

  return {
    channels,
    setChannels,
    activePreview,
    setActivePreview,
    ensureTemplatesLoaded,
    toggleChannel,
    selectTemplate,
    reset,
  };
}