package com.example.springai.memory;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank String prompt) {}
