package com.vasyerp.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasyerp.Model.NotificationTemplateModel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NotificationTemplateConverter implements AttributeConverter<NotificationTemplateModel, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(NotificationTemplateModel attribute) {
        if (attribute == null) return null;
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize NotificationTemplateModel", e);
        }
    }

    @Override
    public NotificationTemplateModel convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            return OBJECT_MAPPER.readValue(dbData, NotificationTemplateModel.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not deserialize NotificationTemplateModel", e);
        }
    }
}