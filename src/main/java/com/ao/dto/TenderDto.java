package com.ao.dto;

public record TenderDto(String sourceId,
                        String reference,
                        String title,
                        String organisme,
                        String region,
                        String deadline,
                        String url) {
}
