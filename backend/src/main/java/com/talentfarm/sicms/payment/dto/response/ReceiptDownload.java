package com.talentfarm.sicms.payment.dto.response;

public record ReceiptDownload(byte[] content, String filename) { }