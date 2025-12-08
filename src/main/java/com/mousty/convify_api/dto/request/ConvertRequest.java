package com.mousty.convify_api.dto.request;

import lombok.NonNull;

public record ConvertRequest(@NonNull String url,@NonNull String format) {}
