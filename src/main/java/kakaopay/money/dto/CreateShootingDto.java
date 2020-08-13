package kakaopay.money.dto;

import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class CreateShootingDto {

    @Min(1)
    private long amount;

    @Min(1)
    private int count;
}
