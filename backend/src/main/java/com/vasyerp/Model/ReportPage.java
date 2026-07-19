package com.vasyerp.Model;

import java.util.List;

public record ReportPage(List<ReportRow> content, long totalElements, int totalPages, int number, int size) {
}
