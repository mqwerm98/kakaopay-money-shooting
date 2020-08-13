package kakaopay.money.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateShootingDto {

    @Min(1)
    private long amount;

    @Min(1)
    private int count;
}
