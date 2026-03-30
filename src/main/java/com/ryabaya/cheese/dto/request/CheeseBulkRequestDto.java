package com.ryabaya.cheese.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "DTO для массового создания сыров")
public class CheeseBulkRequestDto {

    @Schema(description = "Список сыров для создания", required = true)
    @NotEmpty(message = "Список сыров не может быть пустым")
    @Size(max = 10, message = "Нельзя создать больше 10 сыров за один раз")
    @Valid
    private List<CheeseRequestDto> cheeses;

    @Schema(description = "Флаг для имитации проблемы", example = "false")
    private boolean initiatedProblem;
}