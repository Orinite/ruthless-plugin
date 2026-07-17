package com.ruthless.web.request;

import lombok.Builder;

@Builder
public class DonationSubmission {
    private long value;
    private String donatedAt;
}
