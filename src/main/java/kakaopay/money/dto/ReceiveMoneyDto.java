package kakaopay.money.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReceiveMoneyDto {

    private long amount;

    private long userId;

}
